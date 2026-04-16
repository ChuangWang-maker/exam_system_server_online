package com.boomsoft.exam.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * projectName: exam_system_server_online
 *
 * @author: Jon
 * @date: 2026-04-11 12:13
 * description:
 */
@Data
//@Component(饿汉)
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;// minio服务端地址
    private String accessKey;// 访问密钥
    private String secretKey;// 密钥
    private String bucketName;// 存储桶名称
}