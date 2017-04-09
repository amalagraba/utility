package com.utility.api.core.service.impl;

import com.utility.api.core.ocr.OcrEngine;
import com.utility.api.core.ocr.exception.OcrException;
import com.utility.api.core.processor.ProcessContext;
import com.utility.api.core.processor.RegisterProcessor;
import com.utility.api.core.service.StorageScannerService;
import com.utility.api.entity.TicketLine;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j
public class StorageScannerServiceImpl implements StorageScannerService{

    private final OcrEngine ocrEngine;
    private final RegisterProcessor<TicketLine> processor;

    @Autowired
    public StorageScannerServiceImpl(OcrEngine ocrEngine, RegisterProcessor<TicketLine> processor) {
        this.ocrEngine = ocrEngine;
        this.processor = processor;
    }

    @Override
    public List<TicketLine> scan(byte[] image) {
        try {
            String data = ocrEngine.readImage(image);

            return processor.processList(new ProcessContext(data));
        } catch (OcrException e) {
            log.error("Could not read image", e);
        }
        return new ArrayList<>(0);
    }
}
