package com.utility.api.core.ocr;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="ocr.engine")
@Data
public class OcrEngineProperties {

    private String configPath;
    private String trainedLocale;

}
