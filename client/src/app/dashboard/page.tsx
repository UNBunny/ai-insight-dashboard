import { Button } from "../../components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card"
import { Textarea } from "../../components/ui/textarea"
import { ThemeToggle } from "../../components/ui/theme-toggle"
import React, { useState, useEffect } from "react"
import api from "../../utils/api"
import { useAuthContext } from "../../context/auth-context"

interface ResourceLink {
  title: string;
  url: string;
}

interface InsightResponse {
  summary: string;
  keyConcepts: string[];
  recommendations: ResourceLink[];
  timestamp?: Date;
  topic?: string;
}

const Dashboard: React.FC = () => {
  const [topic, setTopic] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [insights, setInsights] = useState<InsightResponse | null>(null);
  const { isAuthenticated } = useAuthContext();

  useEffect(() => {
    // Проверим статус авторизации при монтировании компонента
    console.log('Dashboard компонент - статус аутентификации:', isAuthenticated ? 'Авторизован' : 'Не авторизован');
    const token = localStorage.getItem('auth_token');
    console.log('Dashboard компонент - токен в localStorage:', token ? 'Токен есть' : 'Токена нет');
  }, [isAuthenticated]);

  // Функция для анализа темы с использованием AI API
  const { logout } = useAuthContext();
  
  const analyzeWithAI = async () => {
    if (!topic.trim()) {
      setError('Пожалуйста, введите тему для анализа');
      return;
    }
    
    setIsLoading(true);
    setError(null);
    
    try {
      // Используем API утилиту с автоматической JWT авторизацией
      console.log('Отправляем запрос на анализ с токеном:', localStorage.getItem('auth_token'));
      // Используем путь относительно прокси с обновленным API путем
      const data = await api.post('/api/v1/ai/analyze', { 
        topic, 
        maxResults: 5, 
        language: 'ru' 
      });
      
      setInsights(data);
      console.log('Получены результаты анализа:', data);
    } catch (err: any) {
      console.error('Ошибка при запросе к API:', err);
      
      // Проверяем, является ли это ошибкой аутентификации
      if (err.isAuthError || err.status === 401) {
        console.log('Обнаружена ошибка авторизации, выполняем выход');
        setError('Сессия истекла, необходимо заново авторизоваться');
        // Вызываем функцию выхода
        setTimeout(() => {
          logout(); // Это очистит токен и обновит состояние приложения
        }, 2000); // Даем пользователю увидеть ошибку перед редиректом
      } else {
        setError(`Не удалось получить данные: ${err instanceof Error ? err.message : 'Неизвестная ошибка'}`);
      }
    } finally {
      setIsLoading(false);
    }
  };
  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 py-8 max-w-7xl">
        {/* Здесь был Header с переключателем темы, который теперь перенесен в PublicLayout */}

        {/* Input Section */}
        <div className="mb-8">
          <Card>
            <CardHeader>
              <CardTitle>Введите тему для анализа</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4">
                <Textarea 
                  placeholder="Опишите тему, по которой хотите получить аналитику..." 
                  className="min-h-32"
                  value={topic}
                  onChange={(e) => setTopic(e.target.value)}
                  disabled={isLoading}
                />
                {error && (
                  <div className="text-red-500 text-sm">{error}</div>
                )}
                <Button 
                  className="w-full" 
                  onClick={analyzeWithAI} 
                  disabled={isLoading || !topic.trim()}
                >
                  {isLoading ? 'Анализируем...' : 'Получить аналитику'}
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Results Section */}
        <div className="mb-8">
          <h2 className="text-2xl font-bold mb-4">Результаты анализа</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Summary */}
            <Card>
              <CardHeader>
                <CardTitle>Краткое резюме</CardTitle>
              </CardHeader>
              <CardContent>
                {isLoading ? (
                  <div className="animate-pulse flex flex-col space-y-4">
                    <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-full"></div>
                    <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-5/6"></div>
                    <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-4/6"></div>
                  </div>
                ) : insights ? (
                  <div className="whitespace-pre-wrap">{insights.summary}</div>
                ) : (
                  <div className="text-muted-foreground">
                    Введите тему и нажмите "Получить аналитику" для генерации резюме
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Key Concepts */}
            <Card>
              <CardHeader>
                <CardTitle>Ключевые концепции</CardTitle>
              </CardHeader>
              <CardContent>
                {isLoading ? (
                  <div className="animate-pulse flex flex-col space-y-2">
                    <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-3/4"></div>
                    <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-5/6"></div>
                    <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-2/3"></div>
                    <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-3/4"></div>
                  </div>
                ) : insights?.keyConcepts?.length ? (
                  <ul className="space-y-3">
                    {insights.keyConcepts.map((concept, index) => (
                      <li key={index} className="flex items-start">
                        <span className="inline-flex items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900 text-blue-600 dark:text-blue-200 h-5 w-5 mr-3 flex-shrink-0 text-xs font-medium">{index + 1}</span>
                        <span className="text-sm md:text-base">{concept}</span>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <div className="text-muted-foreground">Ключевые концепции появятся здесь</div>
                )}
              </CardContent>
            </Card>

            {/* Further Reading */}
            <Card className="md:col-span-2">
              <CardHeader>
                <CardTitle>Рекомендуемые источники</CardTitle>
              </CardHeader>
              <CardContent>
                {isLoading ? (
                  <div className="animate-pulse flex flex-col space-y-3">
                    <div className="h-5 bg-gray-200 dark:bg-gray-700 rounded w-3/4"></div>
                    <div className="h-5 bg-gray-200 dark:bg-gray-700 rounded w-4/5"></div>
                    <div className="h-5 bg-gray-200 dark:bg-gray-700 rounded w-2/3"></div>
                  </div>
                ) : insights?.recommendations?.length ? (
                  <ul className="space-y-4">
                    {insights.recommendations.map((resource, index) => (
                      <li key={index} className="border-l-2 border-blue-500 dark:border-blue-400 pl-4 py-1 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-r transition-colors duration-150">
                        <div className="mb-1">
                          <span className="font-medium text-sm text-slate-500 dark:text-slate-400">{index + 1}. </span>
                           <a 
                            href={resource.url} 
                            target="_blank" 
                            rel="noopener noreferrer"
                            className="text-primary hover:underline font-medium inline-flex items-center group cursor-pointer"
                          >
                            {resource.title}
                            <svg 
                              className="w-4 h-4 ml-1 opacity-70 group-hover:opacity-100 transition-opacity" 
                              xmlns="http://www.w3.org/2000/svg" 
                              viewBox="0 0 20 20" 
                              fill="currentColor"
                            >
                              <path fillRule="evenodd" d="M5.22 14.78a.75.75 0 001.06 0l7.22-7.22v5.69a.75.75 0 001.5 0v-7.5a.75.75 0 00-.75-.75h-7.5a.75.75 0 000 1.5h5.69l-7.22 7.22a.75.75 0 000 1.06z" clipRule="evenodd" />
                            </svg>
                          </a>
                        </div>
                        {resource.url && (
                          <a 
                            href={resource.url}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-sm text-slate-600 dark:text-slate-300 hover:text-blue-600 dark:hover:text-blue-300 transition-colors cursor-pointer underline"
                          >
                            {resource.url.replace(/^https?:\/\/(www\.)?/, '').split('/')[0]}
                          </a>
                        )}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <div className="text-muted-foreground">Рекомендуемые источники появятся здесь</div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>


      </div>
    </div>
  )
};

export default Dashboard;
