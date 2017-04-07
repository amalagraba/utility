package com.utility.api.core.ocr.utils;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import lombok.extern.log4j.Log4j;

import org.imgscalr.Scalr;
import org.springframework.stereotype.Component;

@Component
@Log4j
public class ImageProcessor {

	private static final int TARGET_SIZE = 2000;
	private static final int SAMPLE_SIZE = 50;

	public byte[] scaleImage(byte[] imageContent) {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(imageContent);
				ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            BufferedImage image = ImageIO.read(bis);

            if (isDark(image)) {
                RescaleOp rescale = new RescaleOp(
                        new float[]{1.4f, 1.4f, 1.4f},
                        new float[]{0f, 0f, 0f}, null);
                rescale.filter(image, image);
            }

            int originWidth = image.getWidth();
            int originHeight = image.getHeight();

            if (originHeight >= TARGET_SIZE && originWidth >= TARGET_SIZE) {
                return imageContent;
            }

            Scalr.Mode scaleMode = getPreferredScaleMode(originHeight, originWidth);
            BufferedImage outputImage = Scalr.resize(image, Scalr.Method.QUALITY, scaleMode, TARGET_SIZE);

            ImageIO.write(outputImage, SupportedMimeType.PNG.name().toLowerCase(), bos);
            image.flush();
            outputImage.flush();

            return bos.toByteArray();
        } catch (IOException e) {
			log.error("Could not scale image", e);
		}
		return imageContent;
	}

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

    private boolean isDark(int color) {
        // extract each color component
        int red   = (color >>> 16) & 0xFF;
        int green = (color >>>  8) & 0xFF;
        int blue  = (color) & 0xFF;

        // calc luminance in range 0.0 to 1.0; using SRGB luminance constants
        float luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;

        return luminance <= 0.7f;
    }

    private Scalr.Mode getPreferredScaleMode(int originHeight, int originWidth) {
        if (originHeight > originWidth) {
            return Scalr.Mode.FIT_TO_WIDTH;
        } else {
            return Scalr.Mode.FIT_TO_HEIGHT;
        }
    }

    public SupportedMimeType getMimeType(byte[] content) {
		try (InputStream input = new BufferedInputStream(new ByteArrayInputStream(content))) {
			return SupportedMimeType.fromValue(URLConnection.guessContentTypeFromStream(input));
		} catch (IOException e) {
			return null;
		}
	}
}
