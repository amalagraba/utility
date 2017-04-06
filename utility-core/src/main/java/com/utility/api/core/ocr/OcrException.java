package com.utility.api.core.ocr;

public class OcrException extends Exception {

    public OcrException() {
        super();
    }

    public OcrException(String message) {
        super(message);
    }

    public OcrException(String message, Throwable e) {
        super(message, e);
    }
}
