package com.boomsoft.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * projectName: exam_system_server_online
 *
 * @author: Jon
 * @date: 2026-04-09 19:53
 * description:
 */
@SpringBootApplication
public class AdminWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminWebApplication.class, args);
        System.out.println("项目启动成功~~~~");
    }
}
