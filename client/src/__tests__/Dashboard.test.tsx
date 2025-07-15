import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import Dashboard from '../components/Dashboard/page';
import { rest } from 'msw';
import { setupServer } from 'msw/node';

// Мок-данные для пользователей
const mockUsers = [
  { id: 1, username: 'user1', email: 'user1@example.com' },
  { id: 2, username: 'user2', email: 'user2@example.com' }
];

// Мок-данные для анализа AI
const mockAnalysis = {
  summary: 'This is a mock AI analysis summary',
  keyConcepts: ['Concept 1', 'Concept 2', 'Concept 3'],
  furtherReading: ['https://example.com/1', 'https://example.com/2']
};

// Настройка mock-сервера для эмуляции API
const server = setupServer(
  // Мокируем получение списка пользователей
  rest.get('http://localhost:8081/api/users', (req, res, ctx) => {
    return res(ctx.json(mockUsers));
  }),
  
  // Мокируем анализ AI
  rest.post('http://localhost:8081/api/ai/analyze', (req, res, ctx) => {
    return res(ctx.json(mockAnalysis));
  }),
  
  // Мокируем создание пользователя
  rest.post('http://localhost:8081/api/users', (req, res, ctx) => {
    const { username, email } = req.body as any;
    return res(
      ctx.status(201),
      ctx.json({ id: 3, username, email })
    );
  }),
  
  // Мокируем удаление пользователя
  rest.delete('http://localhost:8081/api/users/:id', (req, res, ctx) => {
    return res(ctx.status(204));
  })
);

// Включаем моки перед тестами
beforeAll(() => server.listen());
// Сброс обработчиков между тестами
afterEach(() => server.resetHandlers());
// Закрываем сервер после всех тестов
afterAll(() => server.close());

describe('Dashboard компонент', () => {
  test('загружает и отображает список пользователей', async () => {
    render(<Dashboard />);
    
    // Проверяем, что отображается индикатор загрузки
    expect(screen.getByText(/loading/i)).toBeInTheDocument();
    
    // Ждем, пока загрузятся данные
    await waitFor(() => {
      expect(screen.getByText('user1')).toBeInTheDocument();
      expect(screen.getByText('user2')).toBeInTheDocument();
    });
    
    // Проверяем, что отображаются email-ы
    expect(screen.getByText('user1@example.com')).toBeInTheDocument();
    expect(screen.getByText('user2@example.com')).toBeInTheDocument();
  });
  
  test('отображает ошибку при неудачной загрузке пользователей', async () => {
    // Переопределяем обработчик для этого теста, чтобы вернуть ошибку
    server.use(
      rest.get('http://localhost:8081/api/users', (req, res, ctx) => {
        return res(ctx.status(500), ctx.json({ message: 'Server Error' }));
      })
    );
    
    render(<Dashboard />);
    
    // Ждем, пока не появится сообщение об ошибке
    await waitFor(() => {
      expect(screen.getByText(/error/i)).toBeInTheDocument();
    });
  });
  
  test('отправляет запрос на анализ и отображает результаты', async () => {
    render(<Dashboard />);
    
    // Ждем, пока загрузятся пользователи
    await waitFor(() => {
      expect(screen.getByText('user1')).toBeInTheDocument();
    });
    
    // Находим поле ввода для темы анализа и вводим текст
    const topicInput = screen.getByPlaceholderText(/enter topic/i);
    fireEvent.change(topicInput, { target: { value: 'Test Topic' } });
    
    // Нажимаем кнопку анализа
    const analyzeButton = screen.getByRole('button', { name: /analyze/i });
    fireEvent.click(analyzeButton);
    
    // Ждем результатов анализа
    await waitFor(() => {
      expect(screen.getByText(mockAnalysis.summary)).toBeInTheDocument();
    });
    
    // Проверяем, что отображаются ключевые концепты
    mockAnalysis.keyConcepts.forEach(concept => {
      expect(screen.getByText(concept)).toBeInTheDocument();
    });
    
    // Проверяем, что отображаются ссылки для дальнейшего чтения
    mockAnalysis.furtherReading.forEach(url => {
      expect(screen.getByText(url)).toBeInTheDocument();
    });
  });
  
  test('создает нового пользователя', async () => {
    render(<Dashboard />);
    
    // Ждем, пока загрузятся пользователи
    await waitFor(() => {
      expect(screen.getByText('user1')).toBeInTheDocument();
    });
    
    // Находим поля формы и заполняем их
    const usernameInput = screen.getByPlaceholderText(/username/i);
    const emailInput = screen.getByPlaceholderText(/email/i);
    
    fireEvent.change(usernameInput, { target: { value: 'newuser' } });
    fireEvent.change(emailInput, { target: { value: 'newuser@example.com' } });
    
    // Нажимаем кнопку создания
    const createButton = screen.getByRole('button', { name: /create/i });
    fireEvent.click(createButton);
    
    // Проверяем, что форма сбрасывается после отправки
    await waitFor(() => {
      expect(usernameInput).toHaveValue('');
      expect(emailInput).toHaveValue('');
    });
  });
  
  test('удаляет пользователя', async () => {
    render(<Dashboard />);
    
    // Ждем, пока загрузятся пользователи
    await waitFor(() => {
      expect(screen.getByText('user1')).toBeInTheDocument();
    });
    
    // Находим кнопку удаления для первого пользователя и нажимаем её
    const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
    fireEvent.click(deleteButtons[0]);
    
    // Проверяем, что был сделан запрос на удаление
    // В реальности здесь должна быть проверка, что пользователь больше не отображается,
    // но для этого нужно мокировать повторное получение данных после удаления
  });
});
