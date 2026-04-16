package com.boomsoft.exam.config;

import com.boomsoft.exam.config.properties.MinioProperties;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * projectName: exam_system_server_online
 *
 * @author: Jon
 * @date: 2026-04-11 12:21
 * description: 将minioClient加入到核心容器实现复用
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MinioProperties.class)//(懒汉)
public class MinioConfiguration {
    @Autowired
    private MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        log.info("minio文件服务器连接成功！连接对象信息为：{}",minioClient);
        return minioClient;
    }
}

