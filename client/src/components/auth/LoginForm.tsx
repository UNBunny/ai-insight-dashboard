import React, { useState } from 'react';
import { Button } from '../ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import api from '../../utils/api';

interface LoginFormProps {
  onLoginSuccess: (token: string, username: string, roles: string[]) => void;
}

const LoginForm: React.FC<LoginFormProps> = ({ onLoginSuccess }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!username || !password) {
      setError('Введите имя пользователя и пароль');
      return;
    }
    
    setIsLoading(true);
    setError(null);
    
    try {
      // Используем api.post без авторизации для логина
      const response = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username,
          password,
        }),
      });
      
      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || data.error || 'Ошибка авторизации');
      }
      
      // Проверяем, что токен действительно получен
      if (!data.token) {
        throw new Error('Токен не получен от сервера');
      }
      
      console.log('Получен токен:', data.token);
      console.log('Данные пользователя:', data);
      
      // Извлекаем роли и имя пользователя
      const userUsername = data.username || username;
      const roles = data.roles || [];
      
      // Надежно сохраняем токен в localStorage СНАЧАЛА
      localStorage.setItem('auth_token', data.token);
      localStorage.setItem('auth_user', JSON.stringify({ username: userUsername, roles }));
      
      // Проверяем сохранение токена
      const savedToken = localStorage.getItem('auth_token');
      console.log('Токен сохранен в localStorage:', savedToken === data.token ? 'Успешно' : 'Ошибка сохранения');
      console.log('Роли пользователя:', roles);
      
      // ТОЛЬКО ПОСЛЕ успешного сохранения вызываем коллбэк
      onLoginSuccess(data.token, userUsername, roles);
      
      // Делаем тестовый запрос для проверки авторизации
      setTimeout(async () => {
        try {
          console.log('Выполняем тестовый запрос к API...');
          const testResponse = await api.get('/api/v1/api/data');
          console.log('Тестовый запрос выполнен успешно:', testResponse);
          
          // Дополнительно проверяем заголовок Authorization
          const testFetch = await fetch('/api/v1/api/data', {
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
            }
          });
          if (testFetch.ok) {
            console.log('Прямой fetch запрос также успешен');
          } else {
            console.error('Прямой fetch запрос вернул ошибку:', testFetch.status);
          }
          
        } catch (error) {
          console.error('Ошибка тестового запроса:', error);
        }
      }, 1000);
      
    } catch (err) {
      console.error('Ошибка входа:', err);
      setError(err instanceof Error ? err.message : 'Не удалось войти в систему');
      // Убедимся, что при ошибке токен удаляется
      localStorage.removeItem('auth_token');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader>
        <CardTitle className="text-center">Вход в систему</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleLogin} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="username">Имя пользователя</Label>
            <Input
              id="username"
              type="text"
              placeholder="Введите имя пользователя"
              value={username}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setUsername(e.target.value)}
              disabled={isLoading}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="password">Пароль</Label>
            <Input
              id="password"
              type="password"
              placeholder="Введите пароль"
              value={password}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setPassword(e.target.value)}
              disabled={isLoading}
            />
          </div>
          {error && (
            <div className="text-red-500 text-sm">{error}</div>
          )}
          <Button
            type="submit"
            className="w-full"
            disabled={isLoading}
          >
            {isLoading ? 'Входим...' : 'Войти'}
          </Button>
          <div className="text-sm text-center mt-4 text-muted-foreground">
            <p>Доступные аккаунты для тестирования:</p>
            <p>Админ: admin / admin123</p>
            <p>Пользователь: user / user123</p>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

export default LoginForm;
