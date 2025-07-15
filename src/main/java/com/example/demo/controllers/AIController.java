package com.example.demo.controllers;

import com.example.demo.dto.insight.InsightRequest;
import com.example.demo.dto.insight.InsightResponse;
import com.example.demo.dto.insight.Recommendation;
import com.example.demo.services.AIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Контроллер для работы с AI-анализом
 * Публичные методы для анализа доступны всем пользователям
 * Управление и административные функции доступны только администраторам
 */
@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "${spring.webmvc.cors.allowed-origins:*}")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final AIService aiService;

    /**
     * Асинхронный анализ темы с использованием AI
     * 
     * @param request запрос с темой для анализа
     * @return CompletableFuture с результатом анализа
     */
    @PostMapping(value = "/analyze", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> analyzeWithAI(@Valid @RequestBody InsightRequest request) {
        log.info("Processing public AI analysis request for topic: {}", request.getTopic());
        
        try {
            // Validate the request manually
            if (request.getTopic() == null || request.getTopic().isEmpty()) {
                log.warn("Invalid request - missing topic");
                return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Topic is required"));
            }
            
            // Use the actual AIService to analyze with Ollama
            log.info("Calling AIService.analyzeWithAIAsync for topic: {}", request.getTopic());
            CompletableFuture<InsightResponse> future = aiService.analyzeWithAIAsync(request);
            log.info("Waiting for response from AIService...");
            
            try {
                InsightResponse response = future.join();
                log.info("Response received from AIService: {}", response != null ? "valid response" : "null");
                
                if (response == null) {
                    throw new RuntimeException("Null response received from AIService");
                }
                
                return ResponseEntity.ok(response);
            } catch (CompletionException ce) {
                log.error("CompletionException in future: {}", ce.getMessage(), ce);
                Throwable cause = ce.getCause() != null ? ce.getCause() : ce;
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new RuntimeException("Error in AI analysis: " + cause.getMessage(), cause);
                }
            }
        } catch (Exception e) {
            log.error("Error analyzing with AI: {} ({})", e.getMessage(), e.getClass().getName(), e);
            
            // Create a simple fallback response in controller when something fails
            InsightResponse fallbackResponse = InsightResponse.builder()
                .topic(request.getTopic())
                .summary("Не удалось проанализировать тему из-за технической проблемы: " + e.getMessage())
                .keyConcepts(Arrays.asList(
                    "Попробуйте позднее",
                    "Свяжитесь с администратором"
                ))
                .recommendations(Arrays.asList(
                    new Recommendation("Документация по Spring Boot", "https://spring.io/projects/spring-boot"),
                    new Recommendation("Справочный центр", "https://example.com/help")
                ))
                .timestamp(java.time.Instant.now())
                .build();
            
            return ResponseEntity.ok(fallbackResponse);
        }
    }
    
    /**
     * Управление аналитическими данными - доступно только для администраторов
     * 
     * @return список управляемых аналитических тем
     */
    @GetMapping("/manage/topics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getManageableTopics() {
        // В реальном приложении здесь было бы получение списка тем из базы данных
        return ResponseEntity.ok(Map.of(
            "topics", List.of(
                Map.of("id", 1, "name", "Рыночные тренды", "status", "active"),
                Map.of("id", 2, "name", "Анализ конкурентов", "status", "active"),
                Map.of("id", 3, "name", "Финансовые показатели", "status", "draft")
            )
        ));
    }
    
    /**
     * Проверка состояния AI-сервиса - доступно только для администраторов
     * 
     * @return статус сервиса
     */
    @GetMapping("/manage/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "running",
            "uptime", "4h 23m",
            "requests", 42,
            "errors", 2,
            "lastRequest", "2025-07-11T12:30:15"
        ));
    }
    
    /**
     * Test endpoint to generate a fallback response directly
     * 
     * @return a sample fallback response
     */
    @GetMapping("/test/fallback")
    public InsightResponse testFallbackResponse() {
        // Simple test endpoint to verify the fallback response format works
        log.info("Returning test fallback response");
        
        return InsightResponse.builder()
            .topic("Test Topic")
            .summary("This is a test fallback response")
            .keyConcepts(List.of("Test concept 1", "Test concept 2"))
            .recommendations(List.of(new Recommendation("Test recommendation", "Test description")))
            .timestamp(Instant.now())
            .build();
    }
    
    @PostMapping("/test/analyze")
    public ResponseEntity<InsightResponse> testAnalyzeEndpoint(@Valid @RequestBody InsightRequest request) {
        log.info("Processing test analyze endpoint for topic: {}", request.getTopic());
        
        // Create a direct response without going through AIService
        InsightResponse response = InsightResponse.builder()
            .topic(request.getTopic())
            .summary("This is a test response for topic: " + request.getTopic())
            .keyConcepts(Arrays.asList(
                "Test key concept 1",
                "Test key concept 2",
                "Topic: " + request.getTopic()
            ))
            .recommendations(Arrays.asList(
                new Recommendation("Test recommendation 1", "https://example.com/1"),
                new Recommendation("Test recommendation 2", "https://example.com/2")
            ))
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
}
