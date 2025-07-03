package com.example.demo.controllers;

import com.example.demo.dto.insight.InsightRequest;
import com.example.demo.dto.insight.InsightResponse;
import com.example.demo.dto.insight.ResourceLink;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "${spring.webmvc.cors.allowed-origins:*}")
public class AIController {

    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${ollama.api.url:http://localhost:11434/api/chat}")
    private String apiUrl;
    
    @Value("${ollama.model:llama2}")
    private String model;

    @PostMapping("/analyze")
    public ResponseEntity<InsightResponse> analyzeWithAI(@RequestBody InsightRequest request) {
        try {
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
            userMessage.put("content", "Тема для анализа: " + request.getTopic() + 
                    (request.getLanguage() != null ? ". Язык ответа: " + request.getLanguage() : ""));
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Пробуем подключиться к Ollama API
            try {
                // Выполняем запрос к API
                Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);
                
                // Обрабатываем ответ Ollama
                if (response != null && response.containsKey("message")) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> message = (Map<String, String>) response.get("message");
                    String content = message.get("content");
                    
                    // Парсим ответ и преобразуем в InsightResponse
                    return ResponseEntity.ok(parseAIResponse(content, request.getTopic()));
                }
            } catch (Exception e) {
                // Логируем ошибку подключения к Ollama
                System.err.println("Не удалось подключиться к Ollama API: " + e.getMessage());
                // Продолжаем работу с запасным вариантом
            }
            
            // Если что-то пошло не так, возвращаем резервный ответ
            return ResponseEntity.ok(generateFallbackResponse(request));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(generateFallbackResponse(request));
        }
    }
    
    /**
     * Парсит содержимое ответа AI и преобразует в структурированный объект InsightResponse
     */
    private InsightResponse parseAIResponse(String content, String topic) {
        String summary = "";
        List<String> keyConcepts = new ArrayList<>();
        List<ResourceLink> resourceLinks = new ArrayList<>();
        
        // Разделяем ответ на секции по двойным переводам строки
        String[] sections = content.split("\n\n");
        
        boolean inSummarySection = false;
        boolean inKeyConceptsSection = false;
        boolean inResourcesSection = false;
        
        for (String section : sections) {
            // Определяем, в какой секции мы находимся
            String lowercaseSection = section.toLowerCase();
            
            if (lowercaseSection.contains("резюме:") || lowercaseSection.contains("summary:")) {
                inSummarySection = true;
                inKeyConceptsSection = false;
                inResourcesSection = false;
                
                // Извлекаем текст после "РЕЗЮМЕ:"
                int startIndex = section.indexOf(':') + 1;
                if (startIndex > 0 && startIndex < section.length()) {
                    summary = section.substring(startIndex).trim();
                } else {
                    summary = section;
                }
                continue;
            }
            
            if (lowercaseSection.contains("ключев") || lowercaseSection.contains("key concept")) {
                inSummarySection = false;
                inKeyConceptsSection = true;
                inResourcesSection = false;
                continue;
            }
            
            if (lowercaseSection.contains("источник") || lowercaseSection.contains("ресурс") || 
                lowercaseSection.contains("чтен") || lowercaseSection.contains("source") || 
                lowercaseSection.contains("reading")) {
                inSummarySection = false;
                inKeyConceptsSection = false;
                inResourcesSection = true;
                continue;
            }
            
            // Обрабатываем контент в зависимости от текущей секции
            if (inSummarySection && summary.isEmpty()) {
                summary = section.trim();
            } else if (inKeyConceptsSection) {
                // Разбиваем на отдельные пункты
                String[] points = section.split("\n");
                for (String point : points) {
                    String trimmedPoint = point.trim();
                    if (trimmedPoint.startsWith("-") || trimmedPoint.startsWith("•")) {
                        keyConcepts.add(trimmedPoint.substring(1).trim());
                    } else if (!trimmedPoint.isEmpty() && !trimmedPoint.contains(":")) {
                        keyConcepts.add(trimmedPoint);
                    }
                }
            } else if (inResourcesSection) {
                // Разбиваем на отдельные ресурсы
                String[] resources = section.split("\n");
                for (String resource : resources) {
                    String trimmedResource = resource.trim();
                    if ((trimmedResource.startsWith("-") || trimmedResource.startsWith("•")) && !trimmedResource.isEmpty()) {
                        trimmedResource = trimmedResource.substring(1).trim();
                        
                        String title;
                        String url = "";
                        
                        // Проверяем наличие URL в ресурсе
                        if (trimmedResource.contains("http")) {
                            int urlIndex = trimmedResource.indexOf("http");
                            url = trimmedResource.substring(urlIndex).trim();
                            title = trimmedResource.substring(0, urlIndex).trim();
                            
                            // Удаляем разделители
                            title = title.replaceAll("[-:]+$", "").trim();
                            if (title.isEmpty()) {
                                title = "Ресурс по теме " + topic;
                            }
                        } else if (trimmedResource.contains(":")) {
                            // Если есть двоеточие, но нет URL
                            String[] parts = trimmedResource.split(":", 2);
                            title = parts[0].trim();
                            if (parts.length > 1) {
                                url = "https://example.com/search?q=" + parts[1].trim().replace(" ", "+");
                            } else {
                                url = "https://example.com/search?q=" + topic.replace(" ", "+");
                            }
                        } else {
                            title = trimmedResource;
                            url = "https://example.com/search?q=" + topic.replace(" ", "+") + 
                                  "&specific=" + title.replace(" ", "+");
                        }
                        
                        resourceLinks.add(new ResourceLink(title, url));
                    }
                }
            }
        }
        
        // Если не удалось извлечь резюме, используем тему как резюме
        if (summary.isEmpty()) {
            summary = "Анализ темы: " + topic;
        }
        
        // Если не найдены ключевые концепции, добавляем заглушки
        if (keyConcepts.isEmpty()) {
            keyConcepts.add("Основные принципы " + topic);
            keyConcepts.add("Практическое применение");
            keyConcepts.add("Современные тренды и развитие");
        }
        
        // Если не найдены источники, добавляем заглушки
        if (resourceLinks.isEmpty()) {
            resourceLinks.add(new ResourceLink("Руководство по " + topic, 
                    "https://example.com/guides/" + topic.toLowerCase().replace(" ", "-")));
            resourceLinks.add(new ResourceLink("Научные публикации", 
                    "https://scholar.google.com/scholar?q=" + topic.replace(" ", "+")));
        }
        
        return InsightResponse.builder()
                .summary(summary)
                .keyConcepts(keyConcepts)
                .furtherReading(resourceLinks)
                .build();
    }
    
    /**
     * Генерирует резервный ответ, когда API Ollama недоступно
     */
    private InsightResponse generateFallbackResponse(InsightRequest request) {
        return InsightResponse.builder()
                .summary("Анализ темы: " + request.getTopic() + "\n\n" +
                        "Этот анализ сгенерирован локально без использования Ollama. " +
                        "Для получения более качественного анализа убедитесь, что Ollama запущена и доступна по адресу " + apiUrl + ".")
                .keyConcepts(List.of(
                        "Основные аспекты темы " + request.getTopic(),
                        "Практическое применение концепций",
                        "Современные тенденции развития"
                ))
                .furtherReading(List.of(
                        new ResourceLink("Руководство по " + request.getTopic(), 
                                "https://example.com/guides/" + request.getTopic().toLowerCase().replace(" ", "-")),
                        new ResourceLink("Исследовательские работы", 
                                "https://scholar.google.com/scholar?q=" + request.getTopic().replace(" ", "+"))
                ))
                .build();
    }
}
