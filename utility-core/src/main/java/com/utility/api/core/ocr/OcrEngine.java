package com.utility.api.core.ocr;

import com.google.common.io.Files;
import lombok.extern.log4j.Log4j;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;

@Log4j
@Component
public class OcrEngine {

    private static final String TMP_PREFIX = "tmp";

    private tesseract.TessBaseAPI engine;

    @Autowired
    public OcrEngine(OcrEngineProperties properties) throws OcrException {
        engine = new tesseract.TessBaseAPI();
        // Initialize tesseract-ocr
        if (engine.Init(properties.getConfigPath() , properties.getTrainedLocale()) != 0) {
            throw new OcrException("Could not initialize OcrEngine");
        }
    }

    @PreDestroy
    public void stopEngine() {
        engine.End();
    }

    /**
     * Reads the text from an image. Supported formats are PNG, JPEG, TIFF AND BITMAP
     *
     * @param image     Image content
     *
     * @return Recognized text from the image in UTF-8 encoding
     *
     * @throws OcrException when the image cannot be processed
     */
    public String readImage(byte[] image) throws OcrException {
        //Create temporary file
        File file = createTemporaryFile(image);

        // Open input image with leptonica library
        lept.PIX pix = pixRead(file.getAbsolutePath());
        engine.SetImage(pix);

        // Get OCR result
        String result = getResult(engine.GetUTF8Text());

        deleteTemporaryFile(file);
        pixDestroy(pix);

        return result;
    }

    /**
     * Returns the value of the text recognized by the engine in UTF-8 format
     *
     * @param pointer     Engine process result
     */
    private String getResult(BytePointer pointer) {
        if (pointer == null) {
            return "";
        }
        String result;
        try {
            result = new String(pointer.getStringBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            result = pointer.getString();
        }
        pointer.deallocate();

        return result;
    }

    /**
     * Creates a temporary image file to be used by Tesseract to recognize text
     *
     * @param image     Image content
     *
     * @return A java.io.File object containing the specified image
     *
     * @throws OcrException If the file cannot be created properly and therefore no file
     *         is available for the engine to process
     */
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

    /**
     * Deletes a file. Used to delete temporary files created for the engine to process. This method is nullSafe
     *
     * @param file  File to be deleted
     */
    private void deleteTemporaryFile(File file) {
        if (file != null) {
            if (!file.delete()) {
                log.warn("Could not delete tmp file");
            }
        }
    }
}
