# Реализация AIController

Данная техническая документация описывает архитектурные особенности и детали реализации `AIController` - основного контроллера, отвечающего за взаимодействие с AI-сервисами в проекте AI Insight Dashboard.

## Архитектурный обзор

`AIController` является ключевым компонентом в архитектуре приложения, обеспечивающим доступ к функциям искусственного интеллекта через REST API. Контроллер взаимодействует с сервисным слоем (`AIService`), который инкапсулирует бизнес-логику и обращение к внешним сервисам (Ollama).

```
+-----------------+     +----------------+     +----------------+     +----------------+
|                 |     |                |     |                |     |                |
| Client          +---->+ AIController   +---->+ AIService      +---->+ OllamaClient   |
| (Frontend/API)  |     |                |     |                |     |                |
|                 |     |                |     |                |     |                |
+-----------------+     +----------------+     +----------------+     +----------------+
```

## Структура контроллера

```java
@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "${spring.webmvc.cors.allowed-origins:*}")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final AIService aiService;

    // Эндпоинты контроллера
    @PostMapping(value = "/analyze", ...)
    public ResponseEntity<?> analyzeWithAI(...) { ... }

    @GetMapping("/manage/topics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getManageableTopics() { ... }

    @GetMapping("/manage/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatus() { ... }

    @GetMapping("/test/fallback")
    public InsightResponse testFallbackResponse() { ... }

    @PostMapping("/test/analyze")
    public ResponseEntity<InsightResponse> testAnalyzeEndpoint(...) { ... }
}
```

## Эндпоинты и их назначение

| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| POST | /ai/analyze | Все пользователи | Асинхронный анализ темы с использованием AI |
| GET | /ai/manage/topics | ADMIN | Получение управляемых аналитических тем |
| GET | /ai/manage/status | ADMIN | Проверка состояния AI-сервиса |
| GET | /ai/test/fallback | Все пользователи | Тестовый эндпоинт для генерации фолбэк-ответа |
| POST | /ai/test/analyze | Все пользователи | Тестовый анализ без вызова реальных AI-сервисов |

## Диаграмма последовательности асинхронной обработки

```
┌───────┐          ┌─────────────┐          ┌──────────┐          ┌─────────────┐
│Client │          │AIController │          │AIService │          │OllamaClient │
└───┬───┘          └──────┬──────┘          └─────┬────┘          └──────┬──────┘
    │                     │                       │                      │
    │ POST /ai/analyze    │                       │                      │
    │────────────────────>│                       │                      │
    │                     │                       │                      │
    │                     │ analyzeWithAIAsync()  │                      │
    │                     │──────────────────────>│                      │
    │                     │                       │                      │
    │                     │ CompletableFuture     │                      │
    │                     │<──────────────────────│                      │
    │                     │                       │  API запрос          │
    │                     │                       │─────────────────────>│
    │                     │                       │                      │
    │      Ответ          │                       │                      │
    │<────────────────────│                       │                      │
    │                     │                       │                      │
    │                     │                       │     API ответ        │
    │                     │                       │<─────────────────────│
    │                     │                       │                      │
    │                     │       future.complete(response)              │
    │                     │<──────────────────────│                      │
    │                     │                       │                      │
```

## Детали реализации асинхронной обработки запросов

Асинхронная обработка запросов реализована с использованием `CompletableFuture` из Java. Это позволяет не блокировать основной поток выполнения и обрабатывать запросы параллельно.

```java
@PostMapping(value = "/analyze", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseStatus(HttpStatus.OK)
public ResponseEntity<?> analyzeWithAI(@Valid @RequestBody InsightRequest request) {
    log.info("Processing public AI analysis request for topic: {}", request.getTopic());
    
    try {
        // Валидация запроса
        if (request.getTopic() == null || request.getTopic().isEmpty()) {
            log.warn("Invalid request - missing topic");
            return ResponseEntity
                .badRequest()
                .body(Map.of("error", "Topic is required"));
        }
        
        // Асинхронный вызов AIService
        log.info("Calling AIService.analyzeWithAIAsync for topic: {}", request.getTopic());
        CompletableFuture<InsightResponse> future = aiService.analyzeWithAIAsync(request);
        
        // Ожидание результата
        InsightResponse response = future.join();
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        // Обработка ошибок
        // ...
    }
}
```

### Цикл выполнения асинхронного запроса

1. Клиент отправляет запрос на эндпоинт `/ai/analyze`
2. `AIController` валидирует входные данные
3. Вызывается асинхронный метод `AIService.analyzeWithAIAsync()`
4. Метод возвращает `CompletableFuture<InsightResponse>`
5. Контроллер ожидает завершения операции с помощью `future.join()`
6. По завершении операции результат возвращается клиенту или генерируется фолбэк-ответ при ошибке

## Логика обработки исключений

Контроллер реализует комплексную стратегию обработки исключений:

```java
try {
    // Основная логика обработки запроса
} catch (CompletionException ce) {
    log.error("CompletionException in future: {}", ce.getMessage(), ce);
    Throwable cause = ce.getCause() != null ? ce.getCause() : ce;
    if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
    } else {
        throw new RuntimeException("Error in AI analysis: " + cause.getMessage(), cause);
    }
} catch (Exception e) {
    log.error("Error analyzing with AI: {} ({})", e.getMessage(), e.getClass().getName(), e);
    
    // Создание фолбэк-ответа
    InsightResponse fallbackResponse = createFallbackResponse(request, e);
    return ResponseEntity.ok(fallbackResponse);
}
```

### Типы обрабатываемых исключений

- `CompletionException` - исключение, связанное с асинхронной обработкой
- `OllamaUnavailableException` - исключение при недоступности Ollama-сервиса
- `ValidationException` - исключение при ошибках валидации запроса
- `TimeoutException` - исключение при превышении времени ожидания ответа
- Другие общие исключения (IOException, RuntimeException и т.д.)

## Стратегии фолбэка

Контроллер реализует несколько стратегий фолбэка в случае ошибок:

1. **Предопределенный ответ** - возврат заранее подготовленного ответа с информацией об ошибке:

```java
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
```

2. **Повторные попытки** - автоматическое повторение запроса при временных ошибках:

```java
// Реализация в AIService
public CompletableFuture<InsightResponse> analyzeWithAIAsync(InsightRequest request) {
    return CompletableFuture.supplyAsync(() -> {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                return ollamaClient.generateCompletion(request);
            } catch (OllamaUnavailableException e) {
                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    throw e;
                }
                // Экспоненциальная задержка между попытками
                Thread.sleep(1000 * (long)Math.pow(2, retryCount));
            }
        }
        throw new RuntimeException("Failed after " + MAX_RETRIES + " retries");
    });
}
```

3. **Альтернативный сервис** - переключение на альтернативный AI-сервис при недоступности основного

## Административные эндпоинты

Контроллер предоставляет административные эндпоинты, доступные только пользователям с ролью ADMIN:

```java
@GetMapping("/manage/topics")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getManageableTopics() {
    // Реализация
}

@GetMapping("/manage/status")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getStatus() {
    // Реализация
}
```

## Оптимизация производительности

- **Пул потоков** - использование настраиваемого пула потоков для асинхронной обработки
- **Тайм-ауты** - настраиваемые тайм-ауты для предотвращения длительных блокировок
- **Кэширование** - кэширование часто запрашиваемых результатов
- **Дроссель запросов** - ограничение количества одновременных запросов к AI-сервису

## Мониторинг и логирование

Контроллер включает обширное логирование операций для облегчения диагностики:

```java
log.info("Processing public AI analysis request for topic: {}", request.getTopic());
log.info("Calling AIService.analyzeWithAIAsync for topic: {}", request.getTopic());
log.info("Waiting for response from AIService...");
log.info("Response received from AIService: {}", response != null ? "valid response" : "null");
log.error("Error analyzing with AI: {} ({})", e.getMessage(), e.getClass().getName(), e);
```

## Безопасность

- **Валидация входных данных** - все входные данные валидируются с использованием аннотаций `@Valid`
- **Контроль доступа на основе ролей** - административные эндпоинты защищены с помощью `@PreAuthorize`
- **Cross-Origin Resource Sharing (CORS)** - настройка CORS для контроля доступа с разных источников
- **Защита от инъекций** - все запросы проходят валидацию и санитизацию

## Тестовые эндпоинты

Для целей тестирования и диагностики контроллер предоставляет дополнительные эндпоинты:

```java
@GetMapping("/test/fallback")
public InsightResponse testFallbackResponse() {
    // Реализация
}

@PostMapping("/test/analyze")
public ResponseEntity<InsightResponse> testAnalyzeEndpoint(@Valid @RequestBody InsightRequest request) {
    // Реализация
}
```

## Интеграция с фронтендом

Контроллер предоставляет API, используемый фронтенд-приложением через HTTP-запросы:

```javascript
// Пример вызова API с фронтенда
async function analyzeWithAI(topic) {
  try {
    const response = await fetch('/ai/analyze', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ topic })
    });
    
    if (!response.ok) {
      throw new Error('API request failed');
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error analyzing with AI:', error);
    return null;
  }
}
```

## Конфигурация

Контроллер использует настройки из конфигурационных файлов:

```properties
# application.properties
spring.webmvc.cors.allowed-origins=http://localhost:3000,https://example.com
ai.analysis.timeout=30000
ai.analysis.max-retries=3
```
