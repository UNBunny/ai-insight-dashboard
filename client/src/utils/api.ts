/**
 * Утилита для работы с API с автоматическим добавлением JWT токена
 * Поддерживает опцию пропуска авторизации для публичных эндпоинтов
 */

interface FetchOptions extends RequestInit {
  skipAuth?: boolean;
}

/**
 * Определяет, является ли URL публичным эндпоинтом (не требующим авторизации)
 */
const isPublicEndpoint = (url: string): boolean => {
  const publicEndpoints = [
    '/api/v1/ai/analyze',
    '/auth/login',
    '/auth/register'
  ];
  
  return publicEndpoints.some(endpoint => url.includes(endpoint));
}

/**
 * Обертка над fetch с автоматическим добавлением JWT токена
 */
export const api = {
  /**
   * Выполнить GET запрос с JWT авторизацией
   */
  async get<T = any>(url: string, options: FetchOptions = {}): Promise<T> {
    return this.request<T>(url, { ...options, method: 'GET' });
  },
  
  /**
   * Выполнить POST запрос с JWT авторизацией и JSON телом
   */
  async post<T = any>(url: string, data: any, options: FetchOptions = {}): Promise<T> {
    return this.request<T>(url, {
      ...options,
      method: 'POST',
      body: JSON.stringify(data),
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {})
      }
    });
  },
  
  /**
   * Базовый метод для выполнения запросов с JWT авторизацией
   */
  async request<T = any>(url: string, options: FetchOptions = {}): Promise<T> {
    // Проверяем, является ли эндпоинт публичным
    const isPublic = isPublicEndpoint(url);
    
    // Если в опциях явно не указан skipAuth, устанавливаем его на основе источника URL
    const { skipAuth = isPublic, ...fetchOptions } = options;
    
    // Добавляем токен авторизации, если есть и не указан skipAuth
    if (!skipAuth) {
      const token = localStorage.getItem('auth_token');
      if (token) {
        // Важно! Добавляем префикс "Bearer" как ожидается на бэкенде в JwtRequestFilter
        fetchOptions.headers = {
          ...fetchOptions.headers,
          'Authorization': `Bearer ${token}`
        };
        console.log('Added Authorization header:', `Bearer ${token}`);
      } else if (!isPublic) {
        console.warn('Auth required but no token found for:', url);
      }
    }
    
    // Выполняем запрос
    const response = await fetch(url, fetchOptions);
    
    // Обрабатываем ошибки
    if (!response.ok) {
      if (response.status === 401) {
        // Детальная диагностика для отладки JWT токена
        const token = localStorage.getItem('auth_token');
        console.error(`JWT токен отклонен бэкендом (401 Unauthorized)`);
        console.log(`URL запроса: ${url}`);
        console.log(`Настройки бэкенда: порт 8080, контекст /api/v1`);
        console.log(`Заголовок Authorization: Bearer ${token?.substring(0, 15)}... (часть токена)`);
        
        // Попытка выполнить прямой запрос без прокси для диагностики
        fetch('/api/v1/users', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }).then(resp => {
          console.log(`Прямой запрос на бэкенд вернул статус: ${resp.status}`);
        }).catch(err => {
          console.error(`Ошибка прямого запроса: ${err.message}`);
        });
        
        // Создаем специальную ошибку для обработки в приложении
        const authError = new Error(`Ошибка аутентификации - JWT токен отклонен бэкендом. Проверьте консоль браузера.`);
        (authError as any).isAuthError = true;
        (authError as any).status = response.status;
        throw authError;
      }
      
      // Пытаемся получить текст ошибки из ответа
      const errorText = await response.text().catch(() => 'Unknown error');
      let errorJson;
      
      try {
        errorJson = JSON.parse(errorText);
      } catch (e) {
        // Если не JSON, используем текст как есть
      }
      
      const error = new Error(
        errorJson?.message || errorJson?.error || `API Error: ${response.status}`
      );
      
      // Добавляем служебные свойства к объекту ошибки
      (error as any).status = response.status;
      (error as any).response = response;
      (error as any).data = errorJson;
      
      throw error;
    }
    
    // Для пустых ответов (например, 204 No Content)
    if (response.status === 204) {
      return {} as T;
    }
    
    // Парсим JSON для обычных ответов
    return response.json();
  }
};

export default api;
