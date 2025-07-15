import { renderHook, act } from '@testing-library/react-hooks';
import { rest } from 'msw';
import { setupServer } from 'msw/node';
import { useFetchData } from '../hooks/useFetchData';

// Настройка mock-сервера для эмуляции API
const server = setupServer(
  // Мокируем успешный ответ от API
  rest.get('http://localhost:8081/api/test-endpoint', (req, res, ctx) => {
    return res(
      ctx.json({ data: 'mock data' })
    )
  }),
  
  // Мокируем POST-запрос
  rest.post('http://localhost:8081/api/test-post', (req, res, ctx) => {
    return res(
      ctx.status(201),
      ctx.json({ id: 1, result: 'success' })
    )
  }),
  
  // Мокируем ошибку
  rest.get('http://localhost:8081/api/error', (req, res, ctx) => {
    return res(
      ctx.status(500),
      ctx.json({ message: 'Internal Server Error' })
    )
  })
);

// Включаем моки перед тестами
beforeAll(() => server.listen());
// Сброс обработчиков между тестами
afterEach(() => server.resetHandlers());
// Закрываем сервер после всех тестов
afterAll(() => server.close());

describe('useFetchData хук', () => {
  test('успешно загружает данные', async () => {
    const { result, waitForNextUpdate } = renderHook(() => useFetchData('http://localhost:8081/api/test-endpoint'));
    
    // Первоначальное состояние
    expect(result.current.data).toBeNull();
    expect(result.current.error).toBeNull();
    expect(result.current.isLoading).toBeTruthy();
    
    // Ждем завершения запроса
    await waitForNextUpdate();
    
    // Проверяем результат после загрузки
    expect(result.current.data).toEqual({ data: 'mock data' });
    expect(result.current.error).toBeNull();
    expect(result.current.isLoading).toBeFalsy();
  });
  
  test('обрабатывает ошибки', async () => {
    const { result, waitForNextUpdate } = renderHook(() => useFetchData('http://localhost:8081/api/error'));
    
    // Первоначальное состояние
    expect(result.current.data).toBeNull();
    expect(result.current.error).toBeNull();
    expect(result.current.isLoading).toBeTruthy();
    
    // Ждем завершения запроса
    await waitForNextUpdate();
    
    // Проверяем результат после ошибки
    expect(result.current.data).toBeNull();
    expect(result.current.error).not.toBeNull();
    expect(result.current.isLoading).toBeFalsy();
  });
  
  test('отправляет POST-запрос с данными', async () => {
    const { result } = renderHook(() => useFetchData());
    
    // Выполняем запрос через хук
    act(() => {
      result.current.postData('http://localhost:8081/api/test-post', { name: 'Test' });
    });
    
    // Проверяем изначальное состояние
    expect(result.current.isLoading).toBeTruthy();
    
    // Ждем завершения запроса
    await new Promise(resolve => setTimeout(resolve, 100));
    
    // Проверяем результат
    expect(result.current.data).toEqual({ id: 1, result: 'success' });
    expect(result.current.error).toBeNull();
    expect(result.current.isLoading).toBeFalsy();
  });
});
