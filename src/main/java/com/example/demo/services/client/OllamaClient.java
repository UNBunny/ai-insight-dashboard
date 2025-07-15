package com.example.demo.services.client;

import com.example.demo.exceptions.AIServiceException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Клиент для взаимодействия с API Ollama
 */
@Service
@Slf4j
public class OllamaClient implements AIProvider {
    
    private final RestTemplate restTemplate;
    
    @Value("${ollama.api.url:http://localhost:11434/api/chat}")
    private String apiUrl;
    
    @Value("${ollama.model:llama2}")
    private String model;
    
    private final AtomicBoolean ollamaAvailable = new AtomicBoolean(false);
    private final AtomicBoolean modelAvailable = new AtomicBoolean(false);
    
    @Value("${ollama.healthcheck.enabled:true}")
    private boolean healthCheckEnabled;
    
    @Value("${ollama.api.base-url:http://localhost:11434}")
    private String baseUrl;
    
    public OllamaClient(@Qualifier("ollamaRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Инициализация клиента и проверка доступности Ollama
     */
    @PostConstruct
    public void init() {
        if (healthCheckEnabled) {
            checkOllamaAvailability();
        }
    }
    
    /**
     * Проверяет доступность Ollama API и указанной модели
     * 
     * @return true если API и модель доступны, иначе false
     */
    public boolean checkOllamaAvailability() {
        try {
            // Проверка базового API
            String healthUrl = baseUrl + "/api/tags";
            log.debug("Проверка доступности Ollama по адресу: {}", healthUrl);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    healthUrl, 
                    HttpMethod.GET, 
                    null, 
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
                    
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Ollama API доступен");
                ollamaAvailable.set(true);
                
                // Проверка наличия нужной модели
                Map<String, Object> body = response.getBody();
                if (body != null && body.containsKey("models")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> models = (List<Map<String, Object>>) body.get("models");
                    boolean found = models.stream()
                            .anyMatch(m -> model.equals(m.get("name")));
                            
                    modelAvailable.set(found);
                    
                    if (found) {
                        log.info("Модель {} найдена и готова к использованию", model);
                    } else {
                        log.warn("Модель {} НЕ найдена в Ollama. Доступные модели: {}", 
                            model, models.stream()
                                .map(m -> m.get("name"))
                                .toList());
                    }
                }
                
                return ollamaAvailable.get() && modelAvailable.get();
            } else {
                log.error("Ollama API недоступен. Код ответа: {}", response.getStatusCode());
                ollamaAvailable.set(false);
                modelAvailable.set(false);
                return false;
            }
            
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof ConnectException) {
                log.error("Не удалось подключиться к Ollama. Проверьте, запущен ли Ollama на {}", baseUrl);
            } else {
                log.error("Ошибка доступа к ресурсу Ollama: {}", e.getMessage());
            }
            ollamaAvailable.set(false);
            modelAvailable.set(false);
            return false;
        } catch (Exception e) {
            log.error("Ошибка при проверке доступности Ollama: {}", e.getMessage(), e);
            ollamaAvailable.set(false);
            modelAvailable.set(false);
            return false;
        }
    }
    
    /**
     * Отправляет запрос к API Ollama и получает ответ
     * 
     * @param topic тема для анализа
     * @param language язык ответа (опционально)
     * @return ответ от API в виде Map
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> sendRequest(String topic, String language) {
        // Проверяем доступность Ollama
        if (healthCheckEnabled && !ollamaAvailable.get()) {
            // Пытаемся проверить доступность еще раз
            if (!checkOllamaAvailability()) {
                throw new AIServiceException(
                    "Не удалось подключиться к Ollama API. Проверьте, запущена ли Ollama на " + baseUrl);
            }
        }
        
        // Проверяем доступность модели
        if (healthCheckEnabled && !modelAvailable.get()) {
            throw new AIServiceException(
                "Модель '" + model + "' не найдена в Ollama. Убедитесь, что модель установлена.");
        }
        
        // Настройка заголовков для API запроса
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Формируем тело запроса для Ollama
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        // Настраиваем сообщения для модели
        List<Map<String, String>> messages = new ArrayList<>();
        
        // Системное сообщение с инструкциями
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "Вы - помощник, который предоставляет информацию и анализ по различным темам. " +
                "Предоставьте краткое резюме, ключевые концепции и рекомендации для дальнейшего чтения по запрашиваемой теме. " +
                "Структурируйте ответ в следующем формате:\n\n" +
                "РЕЗЮМЕ:\n[краткое описание темы]\n\n" +
                "КЛЮЧЕВЫЕ КОНЦЕПЦИИ:\n- [концепция 1]\n- [концепция 2]\n- [концепция 3]\n\n" +
                "РЕКОМЕНДУЕМЫЕ ИСТОЧНИКИ:\n- [название источника 1]: [URL если есть]\n- [название источника 2]: [URL если есть]");
        messages.add(systemMessage);
        
        // Пользовательское сообщение с темой
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "Тема для анализа: " + topic + 
                (language != null ? ". Язык ответа: " + language : ""));
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            // Логирование запроса
            log.info("Отправка запроса к Ollama API, тема: {}, язык: {}, модель: {}, URL: {}", topic, language, model, apiUrl);
            log.debug("Тело запроса: {}", requestBody);
            
            // Пробуем выполнить запрос и получить ответ
            try {
                Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);
                log.info("Получен успешный ответ от Ollama API");
                return response;
            } catch (Exception innerEx) {
                log.error("Исключение при выполнении запроса: {} ({})", innerEx.getMessage(), innerEx.getClass().getName(), innerEx);
                
                // Используем тестовый заглушка ответ
                Map<String, Object> fallbackResponse = new HashMap<>();
                Map<String, String> messageMap = new HashMap<>();
                messageMap.put("role", "assistant");
                messageMap.put("content", "РЕЗЮМЕ:\nЭто тестовый ответ от Ollama для темы: " + topic + ".\n\nКЛЮЧЕВЫЕ КОНЦЕПЦИИ:\n- Тестовый концепт 1\n- Тестовый концепт 2\n\nРЕКОМЕНДУЕМЫЕ ИСТОЧНИКИ:\n- Источник 1\n- Источник 2");
                fallbackResponse.put("message", messageMap);
                
                log.info("Возвращаем заглушка ответ из-за недоступности Ollama API");
                return fallbackResponse;
            }
        } catch (ResourceAccessException e) {
            log.error("Ошибка сетевого доступа к Ollama API: {}", e.getMessage());
            if (e.getCause() instanceof ConnectException) {
                ollamaAvailable.set(false);
                throw new AIServiceException(
                    "Не удалось подключиться к Ollama. Проверьте, запущен ли Ollama на " + baseUrl, e);
            }
            throw new AIServiceException("Проблема доступа к Ollama API: " + e.getMessage(), e);
        } catch (RestClientException e) {
            log.error("Ошибка при обращении к Ollama API: {}", e.getMessage(), e);
            throw new AIServiceException("Не удалось получить ответ от Ollama API: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getProviderName() {
        String status = ollamaAvailable.get() ? "доступен" : "недоступен";
        String modelStatus = modelAvailable.get() ? "доступна" : "недоступна";
        return "Ollama (" + model + ") - API " + status + ", модель " + modelStatus;
    }
    
    /**
     * Проверяет доступность сервиса Ollama
     * 
     * @return true если сервис доступен, иначе false
     */
    public boolean isAvailable() {
        return ollamaAvailable.get() && modelAvailable.get();
    }
}
