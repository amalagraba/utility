package com.utility.api.core.ocr.processor;

import com.utility.api.core.ocr.utils.SupportedMimeType;
import lombok.extern.log4j.Log4j;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.*;
import java.net.URLConnection;

@Log4j
@Component
public class ImageProcessor {

	private static final int SAMPLE_SIZE = 50;
    private static final int TARGET_SIZE = 2000;
    private static final int MIN_SIZE = 1000;

    private final OpenCVProcessor processor;

    @Autowired
    public ImageProcessor(OpenCVProcessor processor) {
        this.processor = processor;
    }

    /**
     * Converts the image into a binary image and scales it accordingly to optimize it for OCR purposes
     *
     * @param imageContent  Image content to be processed
     *
     * @return  Processed image content
     */
    public byte[] processAndScale(byte[] imageContent) {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(imageContent);
				ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            BufferedImage image = ImageIO.read(bis);

            if (isDark(image)) {
                RescaleOp rescale = new RescaleOp(
                        new float[]{1.6f, 1.6f, 1.6f},
                        new float[]{0f, 0f, 0f}, null);
                rescale.filter(image, image);
            }
            int originWidth = image.getWidth();
            int originHeight = image.getHeight();
            Scalr.Mode scaleMode = getPreferredScaleMode(originHeight, originWidth);

            image = getProcessedImage(Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, scaleMode, MIN_SIZE));
            image = Scalr.resize(image, Scalr.Method.QUALITY, scaleMode, TARGET_SIZE);
            ImageIO.write(image, SupportedMimeType.PNG.name().toLowerCase(), bos);
            image.flush();

            return bos.toByteArray();
        } catch (IOException e) {
			log.error("Could not process image", e);
		}
		return imageContent;
	}

    /**
     * Calls {@link OpenCVProcessor#getBinaryImage(BufferedImage)} in order to obtain a binary image from the specified
     * image
     *
     * @param image     Image to be processed
     *
     * @return  Another instance of BufferedImage holding the processed image
     *
     * @throws IOException
     */
	private BufferedImage getProcessedImage(BufferedImage image) throws IOException {
        ByteArrayInputStream bis = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            // Rewrite to process with openCV
            ImageIO.write(image, SupportedMimeType.PNG.name().toLowerCase(), bos);
            bis = new ByteArrayInputStream(bos.toByteArray());

            return processor.getBinaryImage(ImageIO.read(bis));
        } finally {
            if (bis != null) {
                bis.close();
            }
            image.flush();
        }
    }

    /**
     * Grabs a 50x50 sample from the image to determine if it lacks brightness by analyzing pixel luminance
     *
     * @param image     Image to be analyzed
     *
     * @return  True if the image is too dark
     */
    private boolean isDark(BufferedImage image) {
        int darkCount = 0;

        for (int x = 0; x < SAMPLE_SIZE; x++) {
            for (int y = 0; y < SAMPLE_SIZE; y++) {
                if (isDark(image.getRGB(x, y))) {
                    darkCount ++;
                }
            }
        }
        return darkCount > SAMPLE_SIZE * SAMPLE_SIZE / 2;
    }

    /**
     * Returns whether a pixel is under a luminance threshold
     *
     * @param color     pixel to be analyzed
     */
    private boolean isDark(int color) {
        // extract each color component
        int red   = (color >>> 16) & 0xFF;
        int green = (color >>>  8) & 0xFF;
        int blue  = (color) & 0xFF;

        // calc luminance in range 0.0 to 1.0; using SRGB luminance constants
        float luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;

        return luminance <= 0.7f;
    }

    /**
     * Decides if a picture should be scaled to width or height by comparing these two attributes
     *
     * @param height    Image height
     * @param width     Image width
     *
     * @return  FIT_TO_WIDTH if the image width is less than its height, FIT_TO_HEIGHT otherwise
     */
    private Scalr.Mode getPreferredScaleMode(int height, int width) {
        if (height > width) {
            return Scalr.Mode.FIT_TO_WIDTH;
        } else {
            return Scalr.Mode.FIT_TO_HEIGHT;
        }
    }

    /**
     * Analyzes the MimeType of a file by its content using {@link URLConnection#guessContentTypeFromStream(InputStream)}
     *
     * @param content   File content
     *
     * @return MimeType of the file by its content
     */
    public SupportedMimeType getMimeType(byte[] content) {
		try (InputStream input = new BufferedInputStream(new ByteArrayInputStream(content))) {
			return SupportedMimeType.fromValue(URLConnection.guessContentTypeFromStream(input));
		} catch (IOException e) {
			return null;
		}
	}
}
