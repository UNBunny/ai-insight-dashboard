package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import org.springframework.lang.NonNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * Конфигурация для работы с Ollama API
 */
@Configuration
@Slf4j
public class OllamaConfig {

    @Value("${ollama.api.timeout:30000}")
    private int apiTimeout;

    /**
     * Создает RestTemplate с настроенными таймаутами и обработчиком ошибок для Ollama API
     *
     * @return настроенный экземпляр RestTemplate
     */
    @Bean(name = "ollamaRestTemplate")
    public RestTemplate ollamaRestTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(5000))
                .setReadTimeout(Duration.ofMillis(apiTimeout))
                .errorHandler(new DetailedErrorHandler())
                .build();
    }

    /**
     * Обработчик ошибок, который логирует детальную информацию о проблемах с API
     */
    private static class DetailedErrorHandler implements ResponseErrorHandler {
        @Override
        public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
            return response.getStatusCode().is4xxClientError() || 
                   response.getStatusCode().is5xxServerError();
        }

        @Override
        public void handleError(@NonNull ClientHttpResponse response) throws IOException {
            // Читаем тело ответа для логирования
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                
                String responseBody = reader.lines()
                        .collect(Collectors.joining("\n"));
                
                log.error("Ошибка при обращении к Ollama API. Статус: {}, Тело ответа: {}", 
                         response.getStatusCode(), responseBody);
            }
        }
    }
}
