package com.utility.api.core.service.impl;

import com.utility.api.core.ocr.OcrEngine;
import com.utility.api.core.ocr.exception.OcrException;
import com.utility.api.core.service.StorageScannerService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j
public class StorageScannerServiceImpl implements StorageScannerService{

    private final OcrEngine ocrEngine;

    @Autowired
    public StorageScannerServiceImpl(OcrEngine ocrEngine) {
        this.ocrEngine = ocrEngine;
    }

    @Override
    public String scan(byte[] image) {
        try {
            return ocrEngine.readImage(image);
        } catch (OcrException e) {
            log.error("Could not read image", e);
            return e.getMessage();
        }
    }
}
