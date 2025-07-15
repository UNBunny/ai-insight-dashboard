package com.example.demo.services;

import com.example.demo.dto.insight.InsightRequest;
import com.example.demo.dto.insight.InsightResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Сервис для работы с AI-анализом и получением инсайтов
 */
public interface AIService {
    
    /**
     * Синхронно анализирует тему с использованием AI и возвращает структурированный ответ
     * 
     * @param request запрос с темой для анализа
     * @return структурированный ответ с анализом темы
     */
    InsightResponse analyzeWithAI(InsightRequest request);
    
    /**
     * Асинхронно анализирует тему с использованием AI и возвращает CompletableFuture с результатом
     * 
     * @param request запрос с темой для анализа
     * @return CompletableFuture со структурированным ответом анализа темы
     */
    CompletableFuture<InsightResponse> analyzeWithAIAsync(InsightRequest request);
    
    /**
     * Получает кэшированный результат анализа по ключу запроса, если доступен
     * 
     * @param request запрос с темой для анализа
     * @return структурированный ответ с анализом темы или null, если кэша нет
     */
    InsightResponse getCachedAnalysis(InsightRequest request);
}
