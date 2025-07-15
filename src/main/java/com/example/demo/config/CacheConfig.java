package com.example.demo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Конфигурация кэширования для приложения
 * Профиль "dev" использует стандартный кэш в памяти
 * Профиль "prod" использует Redis для распределенного кэширования
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Конфигурация Redis Cache Manager для продакшн среды
     * 
     * @param connectionFactory фабрика подключений Redis
     * @return CacheManager настроенный для Redis
     */
    @Bean
    @Profile("prod")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // TTL по умолчанию для кэша
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .withCacheConfiguration("insightCache", 
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofDays(1))) // больший TTL для AI-результатов
                .build();
    }
}
