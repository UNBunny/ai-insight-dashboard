import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import LoginForm from '../components/auth/LoginForm';
import { useAuthContext } from '../context/auth-context';
import { ThemeToggle } from '../components/ui/theme-toggle';

/**
 * Компонент страницы авторизации в админ-панель
 */
const LoginPage: React.FC = () => {
  const { isAuthenticated, login, isAdmin } = useAuthContext();
  const navigate = useNavigate();

  // Перенаправление, если пользователь уже авторизован
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/admin/dashboard');
    }
  }, [isAuthenticated, navigate]);

  // Обработчик успешного входа
  const handleLoginSuccess = (token: string, username: string, roles: string[]) => {
    // Авторизация через контекст
    login(token, username, roles);
    // Перенаправление произойдет через useEffect когда изменится isAuthenticated
  };

  return (
    <div className="flex flex-col min-h-screen bg-slate-50 dark:bg-slate-900">
      {/* Шапка страницы авторизации */}
      <header className="bg-white dark:bg-slate-800 shadow-sm">
        <div className="container mx-auto px-4">
          <div className="flex justify-between items-center py-4">
            <a href="/" className="text-2xl font-bold text-slate-900 dark:text-white">
              AI Insight Dashboard
            </a>
            <ThemeToggle />
          </div>
        </div>
      </header>
      
      {/* Содержимое страницы - форма авторизации */}
      <div className="flex-1 flex items-center justify-center p-6">
        <div className="w-full max-w-md">
          <h1 className="text-2xl font-bold text-center mb-6 text-slate-900 dark:text-white">
            Вход в панель администратора
          </h1>
          <p className="text-center mb-6 text-slate-600 dark:text-slate-300">
            Введите учетные данные для доступа к панели администратора
          </p>
          <LoginForm onLoginSuccess={handleLoginSuccess} />
          <div className="text-center mt-6">
            <a href="/" className="text-blue-600 hover:underline dark:text-blue-400">
              &larr; Вернуться на публичную страницу
            </a>
          </div>
        </div>
      </div>
      
      {/* Подвал */}
      <footer className="py-4 border-t border-slate-200 dark:border-slate-800">
        <div className="container mx-auto px-4 text-center text-slate-500 dark:text-slate-400">
          © {new Date().getFullYear()} AI Insight Dashboard. Все права защищены.
        </div>
      </footer>
    </div>
  );
};

export default LoginPage;
