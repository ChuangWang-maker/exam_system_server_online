package com.boomsoft.exam.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
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

    @Bean
    public MybatisPlusInterceptor plusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //mybatis-plus分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
