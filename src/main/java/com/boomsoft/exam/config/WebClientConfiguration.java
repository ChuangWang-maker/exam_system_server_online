package com.boomsoft.exam.config;

import com.boomsoft.exam.config.properties.KimiApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * projectName: exam_system_server_online
 *
 * @author: 王创
 * description: webClient加入核心容器的配置类
 */
@Configuration
@EnableConfigurationProperties(KimiApiProperties.class)
public class WebClientConfiguration {

    @Autowired
    private KimiApiProperties kimiApiProperties;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(kimiApiProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + kimiApiProperties.getApiKey())
                .build();
    }
}