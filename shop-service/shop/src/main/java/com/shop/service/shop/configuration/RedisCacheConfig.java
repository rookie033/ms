package com.shop.service.shop.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisCacheConfig {


    @Bean
    @Primary
    public CacheManager cacheManager(@Autowired RedisConnectionFactory connectionFactory) {
//        配置全局的缓存查询过期时间
        return RedisCacheManager
                .builder(connectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)))
                .transactionAware()
                .build();
    }

    @Bean
    public CacheManager cacheManager1D(@Autowired RedisConnectionFactory connectionFactory) {
//        配置全局的缓存查询过期时间
        return RedisCacheManager
                .builder(connectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(1)))
                .transactionAware()
                .build();
    }
}
