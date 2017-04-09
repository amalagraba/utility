package com.utility.api.core.ocr;

import com.utility.api.core.ocr.exception.OcrException;
import com.utility.api.core.ocr.processor.ImageProcessor;
import com.utility.api.core.ocr.utils.FileUtils;
import com.utility.api.core.ocr.utils.SupportedMimeType;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract.TessBaseAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.UnsupportedEncodingException;

import static com.utility.api.core.ocr.utils.FileUtils.deleteTemporaryFile;
import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;

@Log4j
@Component
public class OcrEngine {

    private static final String UTF_8_ENCODING = "UTF-8";

    private final TessBaseAPI engine;
    private final ImageProcessor processor;
    private final boolean keepFiles;


    @Autowired
    public OcrEngine(OcrEngineProperties properties, ImageProcessor processor) throws OcrException {
        this.processor = processor;
        this.engine = new TessBaseAPI();
        this.keepFiles = BooleanUtils.isTrue(properties.getKeepTempFiles());
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
        try {
            //Create temporary file
            File file = FileUtils.createTemporaryFile(getProcessedImage(image));

            // Open input image with leptonica library
            lept.PIX pix = pixRead(file.getAbsolutePath());
            engine.SetImage(pix);

            // Get OCR result
            String result = getResult(engine.GetUTF8Text());

            if (!keepFiles) {
                deleteTemporaryFile(file);
            }
            pixDestroy(pix);

            return result;
        } catch (Exception e) {
            throw new OcrException("Could not process image for text recognition", e);
        }
    }

    /**
     * Check the media type of the file to be processed. If a PDF is found, then it's converted to a TIFF image for the
     * engine to process
     *
     * Images are scaled using {@link ImageProcessor#processAndScale(byte[])} before returned to optimize the engine recognition
     *
     * @param image    The image to be processed
     *
     * @return An optimized version of the image to be processed by the engine
     *
     * @throws OcrException if an unsupported media type is detected
     */
    private byte[] getProcessedImage(byte[] image) throws OcrException {
        SupportedMimeType mime = processor.getMimeType(image);
        if (mime == null) {
            throw new OcrException("Unsupported media type");
        }
        return processor.processAndScale(image);
    }

    /**
     * Returns the value of the text recognized by the engine in UTF-8 format
     *
     * @param pointer     Engine process result
     */
    private String getResult(BytePointer pointer) {
        if (pointer == null) {
            return StringUtils.EMPTY;
        }
        String result;
        try {
            result = new String(pointer.getStringBytes(), UTF_8_ENCODING);
        } catch (UnsupportedEncodingException e) {
            result = pointer.getString();
        }
        pointer.deallocate();

        return result;
    }
}
