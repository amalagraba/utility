package com.utility.api.core.ocr;

import com.google.common.io.Files;
import lombok.extern.log4j.Log4j;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;

@Log4j
@Component
public class OcrEngine {

    private static final String TMP_PREFIX = "tmp";

    private tesseract.TessBaseAPI engine;
    private OcrEngineProperties properties;

    @Autowired
    public OcrEngine(OcrEngineProperties properties) {
        this.properties = properties;
        engine = new tesseract.TessBaseAPI();
    }


    public String readImage(byte[] image) throws OcrException {
        // Initialize tesseract-ocr
        if (engine.Init(properties.getConfigPath() , properties.getTrainedLocale()) != 0) {
            throw new OcrException("Could not initialize OcrEngine");
        }

        //Create temporary file
        File file = createTemporaryFile(image);

        // Open input image with leptonica library
        lept.PIX pix = pixRead(file.getAbsolutePath());
        engine.SetImage(pix);

        // Get OCR result
        BytePointer outText = engine.GetUTF8Text();

        if (outText == null) {
            return "";
        }
        String result = outText.getString();
        engine.End();
        deleteTemporaryFile(file);
        outText.deallocate();
        pixDestroy(pix);

        return result;
    }

    private File createTemporaryFile(byte[] image) throws OcrException {
        File file = new File(TMP_PREFIX + UUID.randomUUID());
        try {
            Files.write(image, file);
        } catch (IOException e) {
            deleteTemporaryFile(file);
            throw new OcrException("Could not create temporary file for image scan", e);
        }
        return file;
    }

    private void deleteTemporaryFile(File file) {
        if (file != null) {
            if (!file.delete()) {
                log.warn("Could not delete tmp file");
            }
        }
    }
}
