---
sidebar_position: 4
---

# API аутентификации

API для аутентификации и получения JWT токенов, необходимых для доступа к защищенным ресурсам системы.

## Аутентификация пользователя

Эндпоинт для аутентификации пользователя и получения JWT токена.

```
POST /auth/login
```

### Параметры запроса

| Поле | Тип | Описание | Обязательное |
|------|-----|----------|-------------|
| username | String | Имя пользователя | Да |
| password | String | Пароль | Да |

### Пример запроса

```json
{
  "username": "admin",
  "password": "your_password"
}
```

### Пример успешного ответа

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "roles": ["ROLE_ADMIN", "ROLE_USER"]
}
```

### Пример ответа при ошибке

```json
{
  "token": null,
  "error": "Неверное имя пользователя или пароль"
}
```

### Коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успешная аутентификация, возвращается JWT токен |
| 401 | Неверные учетные данные |
| 500 | Ошибка сервера при обработке запроса |

## Использование токена

Полученный JWT токен необходимо включать в заголовок `Authorization` для всех запросов к защищенным ресурсам:

```
Authorization: Bearer <полученный_токен>
```

### Пример использования в JavaScript

```javascript
// После успешной аутентификации:
const token = response.data.token;

// Использование токена для доступа к защищенным ресурсам
const apiResponse = await fetch('/api/protected-resource', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

### Срок действия токена

JWT токен имеет ограниченный срок действия. По истечении срока действия необходимо получить новый токен, повторно выполнив запрос аутентификации.
