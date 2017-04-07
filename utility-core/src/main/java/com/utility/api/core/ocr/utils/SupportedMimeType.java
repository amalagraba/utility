package com.utility.api.core.ocr.utils;

public enum SupportedMimeType {

    PNG("image/png"),
    JPEG("image/jpeg"),
    TIFF("image/tiff"),
    PDF("application/pdf"),
    BMP("image/bmp");

    private String value;

    SupportedMimeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SupportedMimeType fromValue(String value) {
        for (SupportedMimeType mime : values()) {
            if (mime.getValue().equals(value)) {
                return mime;
            }
        }
        return null;
    }
}
