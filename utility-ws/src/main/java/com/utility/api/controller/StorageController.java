package com.utility.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.utility.api.core.service.StorageScannerService;

import java.io.IOException;

@RestController
@RequestMapping("/storage")
class StorageController {

    private final StorageScannerService scannerService;

    @Autowired
    public StorageController(StorageScannerService scannerService) {
        this.scannerService = scannerService;
    }

    @PostMapping("/scan")
    public String scan(@RequestBody MultipartFile file) throws IOException {
        return scannerService.scan(file.getBytes());
    }
}
