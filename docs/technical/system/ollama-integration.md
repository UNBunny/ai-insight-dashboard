# Интеграция с Ollama

Данная техническая документация описывает архитектурные особенности и детали реализации интеграции AI Insight Dashboard с Ollama - локальным сервисом для запуска языковых моделей.

## Обзор архитектуры

Интеграция с Ollama реализована через отдельный сервисный слой, который обеспечивает взаимодействие с Ollama API и предоставляет абстрактный интерфейс для остальных компонентов приложения. Основной класс интеграции - `OllamaClient`, который инкапсулирует всю логику взаимодействия с Ollama.

```
+------------------+     +------------------+     +------------------+
|                  |     |                  |     |                  |
| AIController     +---->+ AIService        +---->+ OllamaClient     |
|                  |     |                  |     |                  |
+------------------+     +------------------+     +--------+---------+
                                                         |
                                                         v
                                                  +------+-------+
                                                  |              |
                                                  | Ollama API   |
                                                  |              |
                                                  +--------------+
```

## Компоненты интеграции

### OllamaClient

Основной класс для взаимодействия с Ollama API.

**Основные функции:**

- `isAvailable()` - проверка доступности Ollama сервиса
- `checkOllamaAvailability()` - перепроверка соединения с Ollama
- `getProviderName()` - получение имени провайдера
- `generateCompletion()` - отправка запроса на генерацию текста к Ollama

### OllamaStatusController

Контроллер, предоставляющий API для мониторинга и управления соединением с Ollama.

**Эндпоинты:**

| Метод | Путь | Описание |
|-------|------|----------|
| GET | /system/ollama/status | Получение текущего статуса Ollama |
| POST | /system/ollama/check | Перепроверка доступности Ollama |

## Детали реализации

### Конфигурация

Конфигурация Ollama хранится в `application.properties`:

```properties
# Ollama configuration
ollama.api.url=http://localhost:11434
ollama.api.timeout=30000
ollama.default.model=llama2
```

### Обработка ошибок

Система реализует многоуровневую стратегию обработки ошибок:

1. **Проверка доступности перед операциями**:
   ```java
   if (!ollamaClient.isAvailable()) {
       throw new ServiceUnavailableException("Ollama service is not available");
   }
   ```

2. **Тайм-ауты и повторные попытки**:
   ```java
   // Пример настройки RestTemplate с повторными попытками
   RestTemplate restTemplate = new RestTemplateBuilder()
       .setConnectTimeout(Duration.ofMillis(ollamaConfig.getTimeout()))
       .setReadTimeout(Duration.ofMillis(ollamaConfig.getTimeout()))
       .additionalInterceptors(new RetryInterceptor(3, 1000))
       .build();
   ```

3. **Логирование ошибок**:
   ```java
   try {
       // Запрос к Ollama
   } catch (Exception e) {
       log.error("Error while communicating with Ollama: {}", e.getMessage(), e);
       throw new ServiceException("Failed to process request", e);
   }
   ```

### Фолбэк-стратегии

При недоступности Ollama или возникновении ошибок система использует следующие фолбэк-стратегии:

1. **Статичные ответы** - предопределенные ответы для частых запросов
2. **Локальные модели** - возможность переключения на легковесные локальные модели
3. **Очередь запросов** - отложенная обработка запросов при восстановлении соединения

## Мониторинг

Система предоставляет метрики для мониторинга взаимодействия с Ollama:

- Время отклика API
- Количество успешных/неуспешных запросов
- Состояние соединения
- Использование ресурсов (память, CPU)

## Безопасность

Взаимодействие с Ollama осуществляется с учетом следующих аспектов безопасности:

1. **Валидация входных данных** - все запросы от клиентов проходят валидацию перед отправкой в Ollama
2. **Ограничение доступа** - административные функции доступны только пользователям с ролью ADMIN
3. **Изоляция сервиса** - Ollama запускается в изолированной среде с ограниченными привилегиями

## Схема взаимодействия с Ollama

```
┌─────────────┐         ┌───────────────┐         ┌──────────────┐         ┌────────────┐
│             │         │               │         │              │         │            │
│ Controller  ├────────►│ AIService     ├────────►│ OllamaClient ├────────►│ Ollama API │
│             │         │               │         │              │         │            │
└─────────────┘         └───────────────┘         └──────────────┘         └────────────┘
       ▲                       ▲                        ▲                         │
       │                       │                        │                         │
       │                       │                        │                         │
       │                       │                        │                         ▼
┌──────┴──────┐         ┌─────┴───────┐         ┌──────┴─────┐         ┌────────────┐
│             │         │             │         │            │         │            │
│ Frontend    │         │ DTO/Models  │         │ Config     │         │ LLM Model  │
│             │         │             │         │            │         │            │
└─────────────┘         └─────────────┘         └────────────┘         └────────────┘
```

## Примеры использования

### Проверка доступности Ollama

```java
@Autowired
private OllamaClient ollamaClient;

public void performOperation() {
    if (ollamaClient.isAvailable()) {
        // Выполняем операцию
    } else {
        // Используем альтернативную стратегию
    }
}
```

### Обработка запроса анализа

```java
@Autowired
private OllamaClient ollamaClient;

public InsightResponse analyze(InsightRequest request) {
    try {
        return ollamaClient.generateCompletion(request);
    } catch (OllamaUnavailableException e) {
        // Логируем ошибку
        log.error("Ollama unavailable: {}", e.getMessage());
        // Возвращаем фолбэк-ответ
        return createFallbackResponse(request);
    }
}
```

## Зависимости и требования

- Java 21+
- Spring Boot 3.x
- Ollama сервис (рекомендуемая версия: 0.1.x или выше)
- Минимум 8 ГБ RAM для работы с базовыми моделями
- Рекомендуемая конфигурация: 16+ ГБ RAM, NVIDIA GPU с 8+ ГБ VRAM
