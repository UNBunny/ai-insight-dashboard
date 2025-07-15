package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Configuration
@EnableCaching
@EnableAsync
public class AppConfig implements WebMvcConfigurer {
    
    @Value("${ollama.api.timeout:120000}")
    private int ollamaApiTimeout;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("${spring.webmvc.cors.allowed-origins:http://localhost:8080,http://localhost:3000}")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
    
    /**
     * Создает RestTemplate с настроенными таймаутами для AI сервисов
     * с увеличенным таймаутом для долгих запросов к Ollama API
     */
    @Bean(name = "aiRestTemplate")
    public RestTemplate aiRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofMillis(ollamaApiTimeout)) // Используем таймаут из конфигурации
                .requestFactory(this::clientHttpRequestFactory)
                .build();
    }
    
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);                // 10 секунд на подключение
        factory.setReadTimeout(ollamaApiTimeout);        // Таймаут из конфигурации (по умолчанию 120 секунд)
        return factory;
    }
    
    /**
     * Настройка менеджера кэша для приложения
     */
    @Bean
    public CacheManager cacheManager() {
        // В production следует заменить на Redis или другое распределенное решение
        return new ConcurrentMapCacheManager("aiResponses", "userProfiles");
    }
}
