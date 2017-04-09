package com.utility.api.core.service;

import com.utility.api.entity.TicketLine;

import java.util.List;

public interface StorageScannerService {

    List<TicketLine> scan(byte[] image);
}
