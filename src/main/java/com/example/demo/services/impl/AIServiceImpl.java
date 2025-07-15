package com.example.demo.services.impl;

import com.example.demo.dto.insight.InsightRequest;
import com.example.demo.dto.insight.InsightResponse;
import com.example.demo.dto.insight.Recommendation;
import com.example.demo.dto.insight.ResourceLink;
import com.example.demo.exceptions.AIServiceException;
import com.example.demo.services.AIService;
import com.example.demo.services.client.AIProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Реализация сервиса для работы с AI-анализом
 */
/**
 * Реализация сервиса для работы с AI-анализом
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements AIService {

    // Используем интерфейс AIProvider вместо конкретной реализации
    private final AIProvider ollamaClient;

    @Override
    @Cacheable(value = "aiResponses", key = "#request.topic + '_' + #request.language")
    public InsightResponse analyzeWithAI(InsightRequest request) {
        log.debug("Начало синхронного анализа темы: {}", request.getTopic());
        validateRequest(request);
        
        try {
            // Получаем ответ от AI API
            Map<String, Object> response = ollamaClient.sendRequest(request.getTopic(), request.getLanguage());
            InsightResponse result = processAIResponse(response, request.getTopic());
            log.debug("Завершен синхронный анализ темы: {}", request.getTopic());
            return result;
        } catch (AIServiceException e) {
            // Прокидываем исключение AIServiceException для централизованной обработки
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при анализе с помощью AI: {}", e.getMessage(), e);
            throw new AIServiceException("Произошла ошибка при анализе темы: " + e.getMessage(), e);
        }
    }
    
    @Override
    public CompletableFuture<InsightResponse> analyzeWithAIAsync(InsightRequest request) {
        log.debug("Начало асинхронного анализа темы: {}", request.getTopic());
        
        // Проверяем, есть ли результат в кэше
        InsightResponse cachedResult = getCachedAnalysis(request);
        if (cachedResult != null) {
            log.debug("Найден кэшированный результат для темы: {}", request.getTopic());
            return CompletableFuture.completedFuture(cachedResult);
        }
        
        // Сохраняем текущий контекст аутентификации
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            // Выполняем валидацию запроса
            validateRequest(request);
            
            log.debug("Пользователь {} инициировал асинхронный запрос",
                      authentication != null ? authentication.getName() : "неизвестен");
        } catch (Exception e) {
            log.error("Ошибка при подготовке асинхронного анализа: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(generateFallbackResponse(request));
        }
        
        // Проверяем доступность Ollama клиента и используем fallback если не доступен
        if (!ollamaClient.isAvailable()) {
            log.warn("Ollama API недоступен, используем резервный ответ для темы: {}", request.getTopic());
            return CompletableFuture.completedFuture(generateFallbackResponse(request));
        }
            
        try {
            // Если Ollama доступен, отправляем запрос с четкими инструкциями
            String enhancedTopic = "Проанализируй тему: " + request.getTopic() + ". \n\n"
                + "Предоставь развернутый ответ, который обязательно должен включать следующие разделы: \n"
                + "1. КРАТКОЕ РЕЗЮМЕ: подробное описание темы на 5-10 предложений \n"
                + "2. КЛЮЧЕВЫЕ КОНЦЕПЦИИ: предоставь 5-8 основных концепций, используя нумерованный список (1., 2., 3., ...) \n"
                + "3. РЕКОМЕНДУЕМЫЕ ИСТОЧНИКИ: список 3-5 источников с полными URL адресами. Используй формат: 'Название: https://...' или 'Название - https://...'"
                + "Дай подробный и качественный ответ со всеми указанными разделами.";
            
            Map<String, Object> initialResponse = ollamaClient.sendRequest(enhancedTopic, request.getLanguage());
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // Просто обрабатываем результат, полученный в основном потоке
                    InsightResponse result = processAIResponse(initialResponse, request.getTopic());
                    log.debug("Завершен асинхронный анализ темы: {}", request.getTopic());
                    return result;
                } catch (Exception e) {
                    log.error("Ошибка при асинхронном анализе с помощью AI: {}", e.getMessage(), e);
                    return generateFallbackResponse(request);
                }
            });
            
        } catch (Exception e) {
            log.error("Ошибка при инициализации запроса к Ollama: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(generateFallbackResponse(request));
        }
    }
    
    @Override
    @Cacheable(value = "aiResponses", key = "#request.topic + '_' + #request.language")
    public InsightResponse getCachedAnalysis(InsightRequest request) {
        // Этот метод всегда вернёт null при первом вызове, так как аннотация @Cacheable 
        // будет работать только после того, как метод analyzeWithAI заполнит кэш
        return null;
    }
    
    /**
     * Валидирует запрос
     */
    private void validateRequest(InsightRequest request) {
        if (request == null || request.getTopic() == null || request.getTopic().trim().isEmpty()) {
            throw new IllegalArgumentException("Тема не может быть пустой");
        }
    }
    
    /**
     * Обрабатывает ответ от AI API
     */
    private InsightResponse processAIResponse(Map<String, Object> response, String topic) {
        try {
            log.info("Обработка ответа от AI API: {}", response != null ? "ответ получен" : "ответ пустой");
            
            if (response == null) {
                log.warn("Получен пустой ответ от AI API");
                return generateFallbackResponse(InsightRequest.builder().topic(topic).build());
            }

            if (response.containsKey("message")) {
                log.info("Поле 'message' найдено в ответе AI API");
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) response.get("message");
                    
                    if (message != null && message.containsKey("content")) {
                        String content = (String) message.get("content");
                        log.info("Успешно получено содержимое ответа от AI");
                        return parseAIResponse(content, topic);
                    } else {
                        log.warn("Поле 'content' не найдено в ответе AI API");
                    }
                } catch (ClassCastException e) {
                    log.error("Ошибка при приведении типов для поля 'message': {}", e.getMessage(), e);
                }
            } else {
                log.warn("Поле 'message' не найдено в ответе AI API");
            }
            
            // Если дошли до этой точки, значит что-то пошло не так
            log.warn("Некорректный формат ответа от AI API, используем резервный ответ");
            return generateFallbackResponse(InsightRequest.builder().topic(topic).build());
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при обработке ответа AI: {} ({})", e.getMessage(), e.getClass().getName(), e);
            return generateFallbackResponse(InsightRequest.builder().topic(topic).build());
        }
    }
    
    /**
     * Парсит содержимое ответа AI и преобразует в структурированный объект InsightResponse
     */
    private InsightResponse parseAIResponse(String content, String topic) {
        String summary = "";
        List<String> keyConcepts = new ArrayList<>();
        List<ResourceLink> resourceLinks = new ArrayList<>();
        
        // Паттерн для извлечения резюме, с более строгой обработкой заголовков секций
        Pattern summaryPattern = Pattern.compile("(?i)(?:РЕЗЮМЕ|КРАТКОЕ РЕЗЮМЕ|SUMMARY):?\\s*([\\s\\S]*?)(?=\\s*(?:КЛЮЧЕВЫЕ КОНЦЕПЦИИ|KEY CONCEPTS):|\\s*(?:РЕКОМЕНДУЕМЫЕ ИСТОЧНИКИ|RECOMMENDED SOURCES):|$)");
        Matcher summaryMatcher = summaryPattern.matcher(content);
        
        if (summaryMatcher.find()) {
            summary = summaryMatcher.group(1).trim();
            // Удаляем возможные заголовки "КЛЮЧЕВЫЕ КОНЦЕПЦИИ" внутри резюме
            summary = summary.replaceAll("(?i)\\s*(?:КЛЮЧЕВЫЕ КОНЦЕПЦИИ|КЛЮЧЕВЫЕ КОНЦЕПЦИО|KEY CONCEPTS):?.*$", "");
        }
        
        // Паттерн для извлечения ключевых концепций с поддержкой различных вариантов написания
        Pattern conceptsPattern = Pattern.compile("(?i)(?:КЛЮЧЕВЫЕ КОНЦЕПЦИИ|КЛЮЧЕВЫЕ КОНЦЕПЦИО|KEY CONCEPTS):?\\s*([\\s\\S]*?)(?=\\s*(?:РЕКОМЕНДУЕМЫЕ ИСТОЧНИКИ|RECOMMENDED SOURCES):|$)");
        Matcher conceptsMatcher = conceptsPattern.matcher(content);
        
        if (conceptsMatcher.find()) {
            String conceptsBlock = conceptsMatcher.group(1).trim();
            // Разбиваем блок концепций по маркерам списка (дефисы или нумерованные пункты)
            // Ищем строки, начинающиеся с дефиса или с цифры, за которой следует точка или скобка
            Pattern listItemPattern = Pattern.compile("(?m)^\\s*(?:(?:-|\\*|•|\\d+[\\.\\)]|[a-zA-Z][\\.\\)])|(?:[А-Я][а-я]*\\.)\\s+)(.+)$");
            Matcher listItemMatcher = listItemPattern.matcher(conceptsBlock);
            
            while (listItemMatcher.find()) {
                String conceptText = listItemMatcher.group(1).trim();
                if (!conceptText.isEmpty()) {
                    // Очищаем от потенциальных префиксов маркеров вложенных списков
                    conceptText = conceptText.replaceAll("^\\s*(?:-|\\*|•|\\d+[\\.\\)]|[a-zA-Z][\\.\\)])\\s+", "");
                    keyConcepts.add(conceptText);
                }
            }
            
            // Если шаблон не нашел никаких элементов списка, разбиваем по строкам
            if (keyConcepts.isEmpty()) {
                String[] lines = conceptsBlock.split("\\n");
                for (String line : lines) {
                    String trimmedLine = line.trim();
                    if (!trimmedLine.isEmpty()) {
                        // Удаляем возможные префиксы нумерации
                        trimmedLine = trimmedLine.replaceAll("^\\s*(?:\\d+\\.\\s+|[a-zA-Z]\\.\\s+|-\\s+|\\*\\s+)", "");
                        keyConcepts.add(trimmedLine);
                    }
                }
            }
        }
        
        // Паттерн для извлечения рекомендуемых источников с поддержкой различных вариантов написания
        Pattern resourcesPattern = Pattern.compile("(?i)(?:РЕКОМЕНДУЕМЫЕ ИСТОЧНИКИ|RECOMMENDED SOURCES|ИСТОЧНИКИ|SOURCES):?\\s*([\\s\\S]*)$");
        Matcher resourcesMatcher = resourcesPattern.matcher(content);
        
        if (resourcesMatcher.find()) {
            String resourcesBlock = resourcesMatcher.group(1).trim();
            // Разбиваем блок источников по маркерам списка (дефисы или нумерованные пункты)
            // Ищем строки, начинающиеся с дефиса или с цифры, за которой следует точка
            Pattern resourceItemPattern = Pattern.compile("(?m)^\\s*(?:(?:-|\\*|•|\\d+[\\.\\)]|[a-zA-Z][\\.\\)])\\s+)(.+)$");
            Matcher resourceItemMatcher = resourceItemPattern.matcher(resourcesBlock);
            
            List<String> resourceLinesList = new ArrayList<>();
            while (resourceItemMatcher.find()) {
                resourceLinesList.add(resourceItemMatcher.group(1).trim());
            }
            
            // Если шаблон не нашел никаких элементов списка, разбиваем по строкам
            if (resourceLinesList.isEmpty()) {
                String[] lines = resourcesBlock.split("\\n");
                for (String line : lines) {
                    String trimmedLine = line.trim();
                    if (!trimmedLine.isEmpty()) {
                        // Удаляем возможные префиксы нумерации
                        trimmedLine = trimmedLine.replaceAll("^\\s*(?:\\d+\\.\\s+|[a-zA-Z]\\.\\s+|-\\s+|\\*\\s+)", "");
                        resourceLinesList.add(trimmedLine);
                    }
                }
            }
            
            String[] resourceLines = resourceLinesList.toArray(new String[0]);
            
            for (String line : resourceLines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    // Пытаемся извлечь URL и заголовок из строки рекомендации
                    String title;
                    String url = "";
                    
                    // Попытка 1: Ищем URL в формате: Название: http://... или Название - http://...
                    Pattern urlPattern1 = Pattern.compile("(?i)(.+?)(?::|\\s-\\s)\\s*(https?://\\S+)");
                    Matcher urlMatcher1 = urlPattern1.matcher(trimmedLine);
                    
                    // Попытка 2: Ищем URL в формате: [Название](URL) или [Название][URL] или в квадратных скобках
                    Pattern urlPattern2 = Pattern.compile("(?i)\\[([^\\]]+)\\](?:\\(|\\[)(https?://[^\\)\\]]+)(?:\\)|\\])");
                    Matcher urlMatcher2 = urlPattern2.matcher(trimmedLine);
                    
                    // Попытка 3: Просто ищем URL в тексте
                    Pattern urlPattern3 = Pattern.compile("(?i)(https?://\\S+)");
                    Matcher urlMatcher3 = urlPattern3.matcher(trimmedLine);
                    
                    if (urlMatcher1.find()) {
                        title = urlMatcher1.group(1).trim();
                        url = urlMatcher1.group(2).trim();
                        // Удаляем возможные скобки вокруг URL
                        url = url.replaceAll("^\\[|\\]$|\\($|\\)$", "");
                    } else if (urlMatcher2.find()) {
                        title = urlMatcher2.group(1).trim();
                        url = urlMatcher2.group(2).trim();
                    } else if (urlMatcher3.find()) {
                        // Если нашли только URL, используем текст до URL как название
                        url = urlMatcher3.group(1);
                        int urlStart = trimmedLine.indexOf(url);
                        if (urlStart > 0) {
                            title = trimmedLine.substring(0, urlStart).trim();
                            // Удаляем двоеточие или дефис в конце заголовка
                            title = title.replaceAll(":\\s*$|\\s*-\\s*$", "");
                        } else {
                            // Если URL в начале строки, пробуем получить название из домена
                            String domain = url.replaceAll("^https?://(?:www\\.)?([^/]+).*$", "$1");
                            title = "Resource from " + domain;
                        }
                    } else {
                        // Если URL не найден, используем весь текст как название
                        title = trimmedLine.trim();
                        // Удаляем любые цифры с точкой в начале (например, "1.") и очищаем от номеров
                        title = title.replaceAll("^\\d+\\.\\s+", "");
                        // Создаем URL для поиска по теме и заголовку
                        url = "https://www.google.com/search?q=" + topic.replace(" ", "+") + 
                              "+" + title.replace(" ", "+");
                    }
                    
                    // Очистка URL от любых остаточных квадратных скобок или круглых скобок
                    url = url.replaceAll("\\[|\\]|\\(|\\)$", "");
                    // Удаляем запятые или точки в конце URL
                    url = url.replaceAll("[,.\"]$", "");
                    
                    resourceLinks.add(new ResourceLink(title, url));
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
        
        List<Recommendation> recommendations = new ArrayList<>();
        for (ResourceLink link : resourceLinks) {
            recommendations.add(new Recommendation(link.getTitle(), link.getUrl()));
        }
        
        return InsightResponse.builder()
                .topic(topic)
                .summary(summary)
                .keyConcepts(keyConcepts)
                .recommendations(recommendations)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * Генерирует резервный ответ, если не удалось получить анализ от AI
     */
    private InsightResponse generateFallbackResponse(InsightRequest request) {
        String topic = request.getTopic() != null ? request.getTopic() : "неизвестная тема";
        
        List<Recommendation> recommendations = new ArrayList<>();
        recommendations.add(new Recommendation("Документация Ollama", "https://ollama.ai/docs"));
        recommendations.add(new Recommendation("Руководство по запуску", "https://ollama.ai/getting-started"));
        
        return InsightResponse.builder()
            .topic(topic)
            .summary("Не удалось получить анализ темы '" + topic + "' от локальной модели Ollama.")
            .keyConcepts(Arrays.asList(
                "Попробуйте перезапустить запрос",
                "Убедитесь, что Ollama запущена локально",
                "Проверьте настройки подключения"
            ))
            .recommendations(recommendations)
            .timestamp(Instant.now())
            .build();
    }
}
