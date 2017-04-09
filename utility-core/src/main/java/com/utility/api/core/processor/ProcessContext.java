package com.utility.api.core.processor;

import lombok.Data;

@Data
public class ProcessContext {

    private String rawData;

    public ProcessContext(String rawData) {
        this.rawData = rawData;
    }
}
