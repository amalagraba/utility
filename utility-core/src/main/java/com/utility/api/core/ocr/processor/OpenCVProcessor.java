package com.utility.api.core.ocr.processor;

import com.utility.api.core.ocr.utils.FileUtils;
import org.apache.log4j.Logger;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class OpenCVProcessor {

    private static final Logger log = Logger.getLogger(FileUtils.class);
    private static final OpenCVProcessor instance = new OpenCVProcessor();

    private OpenCVFrameConverter.ToIplImage iplConverter = new OpenCVFrameConverter.ToIplImage();
    private Java2DFrameConverter frameConverter = new Java2DFrameConverter();

    public static OpenCVProcessor getInstance() {
        return instance;
    }

    private OpenCVProcessor() {
    }

    public BufferedImage getBinaryImage(BufferedImage original) throws IOException {
        try {
            IplImage image = iplConverter.convert(frameConverter.convert(original));
            IplImage grayImage = IplImage.create(image.width(), image.height(), IPL_DEPTH_8U, 1);

            // grayscale
            cvCvtColor(image, grayImage, CV_BGR2GRAY);

            Mat matImage = new Mat(grayImage);

            GaussianBlur(matImage, matImage, new Size(5, 5), 50);

            // apply threshold
            adaptiveThreshold(matImage, matImage,255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, 9, 9);

            GaussianBlur(matImage, matImage, new Size(3, 3), 20);

            cvNot(grayImage, grayImage);
            cvDilate(grayImage, grayImage);
            cvErode(grayImage, grayImage);

            return frameConverter.getBufferedImage(iplConverter.convert(grayImage), 1);
        } catch (Exception e) {
            log.error("An error occurred while trying to obtain a binary image", e);
        }
        return original;
    }
}
