package com.boomsoft.exam.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * projectName: exam_system_server_online
 *
 * @author: Jon
 * @date: 2026-04-09 20:09
 * description:
 */
@MapperScan(basePackages = "com.boomsoft.exam.mapper")
@Configuration
public class MybatisPlusConfiguration {
}
