import { useState, useEffect, useCallback } from 'react';

export interface User {
  username: string;
  roles: string[];
}

export interface AuthState {
  isAuthenticated: boolean;
  token: string | null;
  user: User | null;
  loading: boolean;
  isAdmin: boolean;
}

export const useAuth = () => {
  const [state, setState] = useState<AuthState>({
    isAuthenticated: false,
    token: null,
    user: null,
    loading: true,
    isAdmin: false,
  });

  // Функция для проверки токена при загрузке приложения
  const checkAuthState = useCallback(async () => {
    setState(prev => ({ ...prev, loading: true }));
    
    try {
      const token = localStorage.getItem('auth_token');
      console.log('CheckAuthState - Найденный токен:', token ? 'Токен есть' : 'Токен отсутствует');
      
      if (!token) {
        setState({
          isAuthenticated: false,
          token: null,
          user: null,
          loading: false,
          isAdmin: false,
        });
        return;
      }
      
      // В реальном приложении здесь можно сделать запрос к API для проверки валидности токена
      // и получения информации о пользователе
      
      try {
        // Попытаемся получить данные пользователя из localStorage
        const userDataStr = localStorage.getItem('auth_user');
        let username = 'user';
        let roles: string[] = [];
        let isAdmin = false;
        
        if (userDataStr) {
          try {
            const userData = JSON.parse(userDataStr);
            username = userData.username || 'user';
            roles = userData.roles || [];
            // Проверяем, есть ли роль админа
            isAdmin = roles.some(role => role === 'ROLE_ADMIN');
          } catch (e) {
            console.error('Error parsing user data from localStorage:', e);
          }
        }
        
        setState({
          isAuthenticated: true,
          token,
          user: { username, roles },
          loading: false,
          isAdmin
        });
      } catch (error) {
        console.error('Error processing stored authentication data:', error);
        localStorage.removeItem('auth_token');
        localStorage.removeItem('auth_user');
        setState({
          isAuthenticated: false,
          token: null,
          user: null,
          loading: false,
          isAdmin: false
        });
      }
      
    } catch (error) {
      console.error('Ошибка проверки авторизации:', error);
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
      setState({
        isAuthenticated: false,
        token: null,
        user: null,
        loading: false,
        isAdmin: false
      });
    }
  }, []);
  
  // Функция для логина
  const login = useCallback(async (token: string, username: string, roles: string[]) => {
    // Сохраняем токен и данные пользователя в localStorage
    localStorage.setItem('auth_token', token);
    localStorage.setItem('auth_user', JSON.stringify({ username, roles }));
    
    console.log('Логин - Сохранен токен:', token ? 'Токен сохранен' : 'Токен пустой');
    console.log('Роли пользователя:', roles);
    
    // Проверяем, есть ли у пользователя роль админа
    const isAdmin = roles.some(role => role === 'ROLE_ADMIN');
    
    setState({
      isAuthenticated: true,
      token,
      user: { username, roles },
      loading: false,
      isAdmin
    });
  }, []);
  
  // Функция для выхода
  const logout = useCallback(() => {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    setState({
      isAuthenticated: false,
      token: null,
      user: null,
      loading: false,
      isAdmin: false
    });
    console.log('Выход - Токен удален');
  }, []);
  
  // Проверяем авторизацию при загрузке
  useEffect(() => {
    checkAuthState();
  }, [checkAuthState]);

  return {
    ...state,
    login,
    logout,
  };
};

export default useAuth;
