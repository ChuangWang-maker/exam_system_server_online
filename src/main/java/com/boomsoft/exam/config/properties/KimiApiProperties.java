package com.boomsoft.exam.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "kimi.api")
public class KimiApiProperties {
    private String baseUrl;
    private String apiKey;
    private String model;
    private Double temperature; // 温度是double类型
    private Integer maxTokens;// 最大生成字数
}