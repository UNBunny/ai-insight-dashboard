package com.example.demo.services;

import com.example.demo.dto.insight.InsightRequest;
import com.example.demo.dto.insight.InsightResponse;
import com.example.demo.dto.insight.Recommendation;
import com.example.demo.dto.insight.ResourceLink;
import com.example.demo.exceptions.AIServiceException;
import com.example.demo.services.client.OllamaClient;
import com.example.demo.services.impl.AIServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Модульные тесты для сервиса AIServiceImpl
 */
@ExtendWith(MockitoExtension.class)
public class AIServiceImplTest {

    @Mock
    private OllamaClient ollamaClient;
    
    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private AIServiceImpl aiService;

    private InsightRequest testRequest;

    @BeforeEach
    public void setup() {
        testRequest = new InsightRequest();
        testRequest.setTopic("Spring Boot");
        testRequest.setLanguage("ru");
        testRequest.setMaxResults(3);
    }

    @Test
    public void testAnalyzeWithAI_SuccessfulResponse() {
        // Подготовка
        String aiResponse = """
                РЕЗЮМЕ:
                Spring Boot - это фреймворк для быстрой разработки приложений на Java.
                
                КЛЮЧЕВЫЕ КОНЦЕПЦИИ:
                - Автоконфигурация
                - Встроенный сервер приложений
                - Стартеры для быстрой настройки
                
                РЕКОМЕНДУЕМЫЕ ИСТОЧНИКИ:
                - Официальная документация: https://spring.io/projects/spring-boot
                - Spring Boot в действии: https://www.manning.com/books/spring-boot-in-action
                - Spring Framework Guru: https://springframework.guru
                """;

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("content", aiResponse);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", messageMap);

        when(ollamaClient.sendRequest(anyString(), anyString())).thenReturn(responseMap);

        // Выполнение
        InsightResponse response = aiService.analyzeWithAI(testRequest);

        // Проверка
        assertThat(response).isNotNull();
        assertThat(response.getSummary()).contains("Spring Boot - это фреймворк для быстрой разработки приложений на Java");
        assertThat(response.getKeyConcepts()).hasSize(3);
        assertThat(response.getKeyConcepts()).contains("Автоконфигурация");
        assertThat(response.getRecommendations()).hasSize(3);
        
        // Проверяем наличие правильных ссылок
        boolean hasOfficialDocs = response.getRecommendations().stream()
                .anyMatch(rec -> rec.getDescription().contains("spring.io") && rec.getTitle().contains("Официальная документация"));
        assertThat(hasOfficialDocs).isTrue();
        
        // Проверка вызовов mock-объектов
        verify(ollamaClient, times(1)).sendRequest(eq("Spring Boot"), eq("ru"));
    }

    @Test
    public void testAnalyzeWithAI_EmptyTopic() {
        // Подготовка
        InsightRequest emptyTopicRequest = new InsightRequest();
        emptyTopicRequest.setTopic("");
        emptyTopicRequest.setLanguage("ru");

        // Выполнение и проверка
        assertThatThrownBy(() -> aiService.analyzeWithAI(emptyTopicRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Topic cannot be null or empty");

        // Проверяем, что метод клиента не вызывался
        verify(ollamaClient, never()).sendRequest(anyString(), anyString());
    }

    @Test
    public void testAnalyzeWithAI_NullTopic() {
        // Подготовка
        InsightRequest nullTopicRequest = new InsightRequest();
        nullTopicRequest.setTopic(null);

        // Выполнение и проверка
        assertThatThrownBy(() -> aiService.analyzeWithAI(nullTopicRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Topic cannot be null or empty");

        // Проверяем, что метод клиента не вызывался
        verify(ollamaClient, never()).sendRequest(anyString(), anyString());
    }

    @Test
    public void testAnalyzeWithAI_ClientReturnsNull() {
        // Подготовка
        when(ollamaClient.sendRequest(anyString(), anyString())).thenReturn(null);

        // Выполнение
        InsightResponse response = aiService.analyzeWithAI(testRequest);

        // Проверка: должен вернуться fallback-ответ
        assertThat(response).isNotNull();
        assertThat(response.getSummary()).contains("Не удалось получить анализ темы");
        assertThat(response.getKeyConcepts()).hasSize(3);
        assertThat(response.getRecommendations()).hasSize(2);
    }

    @Test
    public void testAnalyzeWithAI_ClientThrowsException() {
        // Подготовка
        when(ollamaClient.sendRequest(anyString(), anyString()))
                .thenThrow(new RuntimeException("API недоступен"));

        // Выполнение
        InsightResponse response = aiService.analyzeWithAI(testRequest);

        // Проверка: должен вернуться fallback-ответ
        assertThat(response).isNotNull();
        assertThat(response.getSummary()).contains("Не удалось получить анализ темы");
        assertThat(response.getKeyConcepts()).hasSize(3);
        assertThat(response.getKeyConcepts()).contains("Попробуйте перезапустить запрос");
    }

    @Test
    public void testAnalyzeWithAI_IncompleteResponse() {
        // Подготовка - только резюме без других секций
        String incompleteResponse = "РЕЗЮМЕ:\nSpring Boot - это фреймворк для разработки.";

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("content", incompleteResponse);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", messageMap);

        when(ollamaClient.sendRequest(anyString(), anyString())).thenReturn(responseMap);

        // Выполнение
        InsightResponse response = aiService.analyzeWithAI(testRequest);

        // Проверка
        assertThat(response).isNotNull();
        assertThat(response.getSummary()).contains("Spring Boot - это фреймворк для разработки");
        // Проверяем, что были созданы заглушки для отсутствующих данных
        assertThat(response.getKeyConcepts()).isNotEmpty();
        assertThat(response.getRecommendations()).isNotEmpty();
    }

    @Test
    public void testAnalyzeWithAI_InvalidJsonStructure() {
        // Подготовка - некорректная структура ответа
        Map<String, Object> invalidResponse = new HashMap<>();
        invalidResponse.put("error", "Invalid request");
        
        when(ollamaClient.sendRequest(anyString(), anyString())).thenReturn(invalidResponse);

        // Выполнение
        InsightResponse response = aiService.analyzeWithAI(testRequest);

        // Проверка: должен вернуться fallback-ответ
        assertThat(response).isNotNull();
        assertThat(response.getSummary()).contains("Не удалось получить анализ");
        assertThat(response.getKeyConcepts()).isNotEmpty();
        assertThat(response.getRecommendations()).isNotEmpty();
    }

    @Test
    public void testAnalyzeWithAI_ComplexResourceLinks() {
        // Подготовка - ответ с разными форматами ссылок
        String responseWithLinks = """
                РЕЗЮМЕ:
                Тестовый ответ.
                
                КЛЮЧЕВЫЕ КОНЦЕПЦИИ:
                - Концепция 1
                - Концепция 2
                
                РЕКОМЕНДУЕМЫЕ ИСТОЧНИКИ:
                - Ссылка без URL
                - Ссылка с [URL](https://example.com)
                - Другой формат: https://another-example.com
                - Официальный сайт Spring: https://spring.io
                """;

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("content", responseWithLinks);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", messageMap);

        when(ollamaClient.sendRequest(anyString(), anyString())).thenReturn(responseMap);

        // Выполнение
        InsightResponse response = aiService.analyzeWithAI(testRequest);

        // Проверка
        assertThat(response).isNotNull();
        List<Recommendation> recommendations = response.getRecommendations();
        
        // Проверяем, что рекомендации правильно распарсены
        assertThat(recommendations.size()).isGreaterThanOrEqualTo(3);
        
        // Проверяем, что есть рекомендация со spring.io
        boolean hasSpringUrl = recommendations.stream()
                .anyMatch(rec -> rec.getDescription().contains("spring.io"));
        assertThat(hasSpringUrl).isTrue();
        
        // Проверяем, что для рекомендации без URL сгенерирован URL по умолчанию
        boolean hasDefaultUrl = recommendations.stream()
                .anyMatch(rec -> rec.getDescription().contains("example.com/search"));
        assertThat(hasDefaultUrl).isTrue();
    }
    
    @Test
    public void testAnalyzeWithAIAsync_SuccessfulResponse() throws ExecutionException, InterruptedException, TimeoutException {
        // Подготовка
        String aiResponse = """
                РЕЗЮМЕ:
                Spring Boot - это фреймворк для быстрой разработки приложений на Java.
                
                КЛЮЧЕВЫЕ КОНЦЕПЦИИ:
                - Автоконфигурация
                - Встроенный сервер приложений
                - Стартеры для быстрой настройки
                
                РЕКОМЕНДУЕМЫЕ ИСТОЧНИКИ:
                - Официальная документация: https://spring.io/projects/spring-boot
                - Spring Boot в действии: https://www.manning.com/books/spring-boot-in-action
                - Spring Framework Guru: https://springframework.guru
                """;

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("content", aiResponse);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", messageMap);

        when(ollamaClient.sendRequest(anyString(), anyString())).thenReturn(responseMap);

        // Выполнение
        CompletableFuture<InsightResponse> futureResponse = aiService.analyzeWithAIAsync(testRequest);
        InsightResponse response = futureResponse.get(5, TimeUnit.SECONDS); // Ожидаем результат с таймаутом

        // Проверка
        assertThat(response).isNotNull();
        assertThat(response.getSummary()).contains("Spring Boot - это фреймворк для быстрой разработки");
        assertThat(response.getKeyConcepts()).hasSize(3);
        assertThat(response.getKeyConcepts()).contains("Автоконфигурация");
        
        // Проверка вызовов mock-объектов
        verify(ollamaClient, times(1)).sendRequest(eq("Spring Boot"), eq("ru"));
    }
    
    @Test
    public void testAnalyzeWithAIAsync_ExceptionHandling() {
        // Подготовка - моделируем исключение при вызове API
        when(ollamaClient.sendRequest(anyString(), anyString())).thenThrow(new RuntimeException("API error"));

        // Выполнение
        CompletableFuture<InsightResponse> futureResponse = aiService.analyzeWithAIAsync(testRequest);
        
        // Проверка - проверяем, что исключение корректно обработано и обернуто в AIServiceException
        assertThatThrownBy(() -> futureResponse.join())
                .hasCauseInstanceOf(AIServiceException.class)
                .hasMessageContaining("An error occurred during async AI analysis");
    }
    
    @Test
    public void testGetCachedAnalysis_CacheHit() {
        // Подготовка - моделируем кэш
        ConcurrentMapCache insightCache = new ConcurrentMapCache("insightCache");
        when(cacheManager.getCache("insightCache")).thenReturn(insightCache);
        
        // Создаем результат для кэширования
        InsightResponse cachedResponse = new InsightResponse();
        cachedResponse.setSummary("Cached summary for Spring Boot");
        
        // Ключ кэша должен соответствовать тому, что используется в сервисе
        String cacheKey = "Spring Boot:ru";
        insightCache.put(cacheKey, cachedResponse);
        
        // Выполнение
        InsightResponse result = aiService.getCachedAnalysis(testRequest);
        
        // Проверка
        assertThat(result).isNotNull();
        assertThat(result.getSummary()).isEqualTo("Cached summary for Spring Boot");
        
        // Проверяем, что API не вызывался, так как результат взят из кэша
        verify(ollamaClient, never()).sendRequest(anyString(), anyString());
    }
    
    @Test
    public void testGetCachedAnalysis_CacheMiss() {
        // Подготовка - моделируем пустой кэш
        ConcurrentMapCache insightCache = new ConcurrentMapCache("insightCache");
        when(cacheManager.getCache("insightCache")).thenReturn(insightCache);
        
        // Выполнение и проверка
        assertThat(aiService.getCachedAnalysis(testRequest)).isNull();
        
        // Проверяем, что API не вызывался при промахе кэша
        verify(ollamaClient, never()).sendRequest(anyString(), anyString());
    }
}
