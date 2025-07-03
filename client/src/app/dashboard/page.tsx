import { Button } from "../../components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card"
import { Textarea } from "../../components/ui/textarea"
import { ThemeToggle } from "../../components/ui/theme-toggle"
import React, { useState } from "react"

interface ResourceLink {
  title: string;
  url: string;
}

interface InsightResponse {
  summary: string;
  keyConcepts: string[];
  furtherReading: ResourceLink[];
}

const Dashboard: React.FC = () => {
  const [topic, setTopic] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [insights, setInsights] = useState<InsightResponse | null>(null);
  
  // Функция для анализа темы с использованием AI API
  const analyzeWithAI = async () => {
    if (!topic.trim()) {
      setError('Пожалуйста, введите тему для анализа');
      return;
    }
    
    setIsLoading(true);
    setError(null);
    
    try {
      const response = await fetch('http://localhost:8081/api/ai/analyze', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ topic, maxResults: 5, language: 'ru' })
      });
      
      if (!response.ok) {
        throw new Error(`Ошибка запроса: ${response.status}`);
      }
      
      const data = await response.json();
      setInsights(data);
    } catch (err) {
      console.error('Ошибка при запросе к API:', err);
      setError(`Не удалось получить данные: ${err instanceof Error ? err.message : 'Неизвестная ошибка'}`);
    } finally {
      setIsLoading(false);
    }
  };
  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 py-8 max-w-7xl">
        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-4xl font-bold tracking-tight text-foreground mb-2">AI-Insight Dashboard</h1>
          <div className="flex items-center space-x-2">
            <ThemeToggle />
          </div>
        </div>

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
                  <ul className="list-disc list-inside space-y-1">
                    {insights.keyConcepts.map((concept, index) => (
                      <li key={index}>{concept}</li>
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
                ) : insights?.furtherReading?.length ? (
                  <ul className="space-y-2">
                    {insights.furtherReading.map((resource, index) => (
                      <li key={index}>
                        <a 
                          href={resource.url} 
                          target="_blank" 
                          rel="noopener noreferrer"
                          className="text-primary hover:underline"
                        >
                          {resource.title}
                        </a>
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
