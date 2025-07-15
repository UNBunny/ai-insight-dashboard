import React from 'react';
import { Navigate } from 'react-router-dom';
import AccessDenied from '../../pages/AccessDenied';
import { useAuthContext } from '../../context/auth-context';

interface ProtectedRouteProps {
  children: React.ReactNode;
  adminOnly?: boolean;
}

/**
 * Компонент защиты маршрутов, который гарантирует, что только авторизованные пользователи могут получить доступ к маршруту
 * Если adminOnly установлен в true, то только пользователи с ролью администратора могут получить доступ
 */
const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children,
  adminOnly = false 
}) => {
  const { isAuthenticated, isAdmin, loading } = useAuthContext();
  
  // Показываем состояние загрузки при проверке авторизации
  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
        <span className="ml-3 text-slate-600 dark:text-slate-300">Загрузка...</span>
      </div>
    );
  }
  
  // Если не авторизован, перенаправляем на страницу входа
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  // Если это маршрут только для админов, и пользователь не админ, показываем страницу отказа в доступе
  if (adminOnly && !isAdmin) {
    return <AccessDenied />;
  }
  
  // Отображаем защищенный контент
  return <>{children}</>;
};

export default ProtectedRoute;
