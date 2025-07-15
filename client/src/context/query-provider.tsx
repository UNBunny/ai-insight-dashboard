import React from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';

// Создаем экземпляр клиента для React Query
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Настройки по умолчанию для запросов
      refetchOnWindowFocus: false, // Не обновлять данные при фокусе окна
      retry: 1,                    // Количество повторов при ошибке запроса
      staleTime: 5 * 60 * 1000,    // Время в мс, через которое данные считаются устаревшими (5 минут)
    },
  },
});

interface QueryProviderProps {
  children: React.ReactNode;
}

export const QueryProvider: React.FC<QueryProviderProps> = ({ children }) => {
  return (
    <QueryClientProvider client={queryClient}>
      {children}
      {process.env.NODE_ENV === 'development' && <ReactQueryDevtools initialIsOpen={false} />}
    </QueryClientProvider>
  );
};

export default QueryProvider;
