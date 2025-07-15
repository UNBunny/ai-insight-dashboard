package com.example.demo.services.client;

import java.util.Map;

/**
 * Интерфейс для всех провайдеров искусственного интеллекта
 * Позволяет легко заменять одну реализацию на другую (Strategy Pattern)
 */
public interface AIProvider {
    
    /**
     * Отправляет запрос к AI API и получает ответ
     * 
     * @param topic тема для анализа
     * @param language язык ответа (опционально)
     * @return ответ от API в виде Map
     */
    Map<String, Object> sendRequest(String topic, String language);
    
    /**
     * Получить название провайдера
     * 
     * @return название провайдера
     */
    String getProviderName();
    
    /**
     * Проверяет доступность сервиса AI
     * 
     * @return true если сервис доступен, иначе false
     */
    boolean isAvailable();
}
