package com.boomsoft.exam.config;

/**
 * projectName: exam_system_server_online
 *
 * @author: Ethan Brooks
 * @date: 2026-04-20 16:19
 * @description: redis的配置类，我们想自定义序列化器
 */

import org.checkerframework.checker.units.qual.C;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        //连接的位置就是我们在application配置
        redisTemplate.setConnectionFactory(factory);

        //定义序列化器
        StringRedisSerializer stringRedisSerializer = StringRedisSerializer.UTF_8;
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);

        return redisTemplate;
    }

}
