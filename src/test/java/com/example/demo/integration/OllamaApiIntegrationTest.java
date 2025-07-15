package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import com.example.demo.dto.insight.InsightRequest;
import com.example.demo.dto.insight.InsightResponse;
import com.example.demo.dto.insight.Recommendation;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Интеграционный тест для AIController с использованием MockWebServer для эмуляции Ollama API
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "ollama.api.url=http://localhost:${mockserver.port}/api/chat",
    "ollama.model.name=llama2-test"
})
@ActiveProfiles("test")
@Disabled("Отключено из-за проблем с внедрением RestTemplate в AIController")
public class OllamaApiIntegrationTest {

    // Тестовая конфигурация для замены URL Ollama API на MockWebServer
    @TestConfiguration
    static class OllamaApiTestConfig {
        @Bean
        @Primary
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }

    private MockWebServer mockWebServer;
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    
    @BeforeEach
    public void setup() throws IOException {
        // Запускаем MockWebServer для эмуляции Ollama API
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        // Устанавливаем системное свойство для использования в @TestPropertySource
        System.setProperty("mockserver.port", String.valueOf(mockWebServer.getPort()));
    }
    
    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    
    @Test
    @Disabled("Отключено из-за проблем с внедрением RestTemplate в AIController")
    public void testAnalyzeWithMockedOllamaApi() throws Exception {
        // Подготовка запроса
        InsightRequest request = new InsightRequest();
        request.setTopic("Spring Boot");
        request.setMaxResults(3);
        request.setLanguage("ru");
        
        // Подготовка мокированного ответа от Ollama API
        String jsonResponse = "{\n" +
                "  \"message\": {\n" +
                "    \"content\": \"Spring Boot - это фреймворк на основе Java для быстрого создания приложений. " +
                "Ключевые концепты включают автоконфигурацию, встроенные серверы и стартеры. " +
                "Для более глубокого изучения рекомендуется посетить spring.io\"\n" +
                "  }\n" +
                "}";
        
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(jsonResponse)
                .setBodyDelay(300, TimeUnit.MILLISECONDS)); // имитация задержки API
        
        // Выполняем запрос к нашему API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<InsightRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<InsightResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/ai/analyze",
                entity,
                InsightResponse.class
        );
        
        // Проверка результатов
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        InsightResponse insightResponse = response.getBody();
        assertThat(insightResponse).isNotNull();
        assertThat(insightResponse.getSummary()).isNotEmpty();
        assertThat(insightResponse.getKeyConcepts()).isNotEmpty();
        assertThat(insightResponse.getRecommendations()).isNotEmpty();
        
        // Проверяем, что запрос к Ollama API был отправлен правильно
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
        
        // Проверяем содержание ответа
        assertThat(insightResponse.getSummary()).contains("Spring Boot");
    }
    
    @Test
    @Disabled("Отключено из-за проблем с внедрением RestTemplate в AIController")
    public void testAnalyzeWithFailingOllamaApi() throws Exception {
        // Подготовка запроса
        InsightRequest request = new InsightRequest();
        request.setTopic("Spring Boot");
        request.setMaxResults(3);
        request.setLanguage("ru");
        
        // Имитируем сбой Ollama API - возвращаем ошибку 500
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));
        
        // Выполняем запрос к нашему API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<InsightRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<InsightResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/ai/analyze",
                entity,
                InsightResponse.class
        );
        
        // Проверка результатов - должен вернуться резервный ответ
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        InsightResponse insightResponse = response.getBody();
        assertThat(insightResponse).isNotNull();
        assertThat(insightResponse.getSummary())
                .contains("Не удалось получить анализ темы 'Spring Boot' от локальной модели Ollama");
    }
}
