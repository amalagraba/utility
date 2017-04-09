package com.utility.api.core.ocr.utils;

import com.google.common.io.Files;
import com.utility.api.core.ocr.exception.OcrException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileUtils {

    private static final Logger log = Logger.getLogger(FileUtils.class);

    private static final String TMP_PREFIX = "tmp";

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
    public static File createTemporaryFile(byte[] image) throws IOException {
        File file = new File(TMP_PREFIX + UUID.randomUUID() + ".png");
        try {
            Files.write(image, file);
        } catch (IOException e) {
            deleteTemporaryFile(file);
            throw new IOException("Could not create temporary file for image scan", e);
        }
        return file;
    }

    /**
     * Deletes a file. Used to delete temporary files created for the engine to process. This method is nullSafe
     *
     * @param file  File to be deleted
     */
    public static void deleteTemporaryFile(File file) {
        if (file != null) {
            if (!file.delete()) {
                log.warn("Could not delete tmp file");
            }
        }
    }
}
