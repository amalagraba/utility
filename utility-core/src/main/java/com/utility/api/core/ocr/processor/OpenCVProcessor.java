package com.utility.api.core.ocr.processor;

import lombok.extern.log4j.Log4j;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

@Log4j
@Component
public class OpenCVProcessor {

    private OpenCVFrameConverter.ToIplImage iplConverter = new OpenCVFrameConverter.ToIplImage();
    private Java2DFrameConverter frameConverter = new Java2DFrameConverter();

    /**
     * Processes an image in order to optimize it before using an OCR scan.
     * The following steps are used :
     *
     * - Image is converted into a grayscale image
     * - Then a GaussianBlur is applied
     * - Image is turned into a binary version using an AdaptativeThreshold
     * - A lesser GaussianBlur is applied
     * - Image negative is obtained
     * - Dilation is applied
     * - And finally Erosion
     *
     * This process is probably going to suffer some modifications over time in order to optimize text recognition
     *
     * @param original  BufferedImage holding the image to be processed
     *
     * @throws IOException
     */
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
