import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Textarea } from "@/components/ui/textarea"
import { useState } from "react"
import analyticsService, { AnalysisResponse } from "../services/analytics.service"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"

export default function Dashboard() {
  const [inputText, setInputText] = useState<string>('')
  const [loading, setLoading] = useState<boolean>(false)
  const [error, setError] = useState<string | null>(null)
  const [analysis, setAnalysis] = useState<AnalysisResponse | null>(null)

  const handleAnalyze = async () => {
    if (!inputText.trim()) {
      setError('Пожалуйста, введите текст для анализа')
      return
    }

    setLoading(true)
    setError(null)
    
    try {
      const result = await analyticsService.analyzeContent({
        topic: 'Анализ пользовательского ввода',
        text: inputText
      })
      setAnalysis(result)
    } catch (err: any) {
      setError(err.message || 'Ошибка при выполнении анализа')
      console.error('Analytics error:', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 py-8 max-w-7xl">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold tracking-tight text-foreground mb-2">AI-Insight Dashboard</h1>
        </div>

        {/* Input Section */}
        <div className="mb-8">
          <Card>
            <CardContent className="pt-6">
              <div className="flex flex-col sm:flex-row gap-4">
                <div className="flex-1">
                  <Textarea 
                    placeholder="Введите текст для анализа..." 
                    className="min-h-[100px] resize-none" 
                    value={inputText}
                    onChange={(e) => setInputText(e.target.value)}
                  />
                </div>
                <div className="flex sm:flex-col justify-center">
                  <Button
                    size="lg"
                    className="whitespace-nowrap bg-blue-600 hover:bg-blue-700 text-white font-semibold px-8 py-3 transition-all duration-200 hover:shadow-lg hover:scale-105 focus:ring-4 focus:ring-blue-300"
                    onClick={handleAnalyze}
                    disabled={loading}
                  >
                    {loading ? 'Анализируем...' : 'Сгенерировать инсайты'}
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-8">
            <Alert variant="destructive">
              <AlertTitle>Ошибка</AlertTitle>
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          </div>
        )}
        
        {/* Main Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* AI Summary - Takes 2 columns on desktop */}
          <div className="lg:col-span-2">
            <Card className="h-full">
              <CardHeader>
                <CardTitle className="text-xl font-semibold">AI Summary</CardTitle>
              </CardHeader>
              <CardContent>
                {loading ? (
                  // Loading state
                  <div className="space-y-4">
                    <div className="h-4 bg-muted rounded animate-pulse"></div>
                    <div className="h-4 bg-muted rounded animate-pulse w-5/6"></div>
                    <div className="h-4 bg-muted rounded animate-pulse w-4/6"></div>
                    <div className="h-4 bg-muted rounded animate-pulse w-3/4"></div>
                    <div className="h-4 bg-muted rounded animate-pulse w-5/6"></div>
                  </div>
                ) : analysis ? (
                  // Analysis results
                  <div className="space-y-4">
                    <p className="text-base text-foreground">{analysis.summary}</p>
                    <div className="text-xs text-muted-foreground">
                      Generated at: {new Date(analysis.timestamp).toLocaleString()}
                    </div>
                  </div>
                ) : (
                  // Empty state
                  <div className="p-4 bg-muted/50 rounded-lg">
                    <p className="text-sm text-muted-foreground text-center">
                      Здесь будет отображаться AI-сгенерированное резюме вашего текста
                    </p>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Right Column - Key Concepts and Further Reading */}
          <div className="space-y-6">
            {/* Key Concepts */}
            <Card>
              <CardHeader>
                <CardTitle className="text-xl font-semibold">Key Concepts</CardTitle>
              </CardHeader>
              <CardContent>
                {loading ? (
                  // Loading state
                  <div className="space-y-3">
                    <div className="flex items-center gap-2">
                      <div className="w-2 h-2 bg-primary rounded-full"></div>
                      <div className="h-3 bg-muted rounded animate-pulse flex-1"></div>
                    </div>
                    <div className="flex items-center gap-2">
                      <div className="w-2 h-2 bg-primary rounded-full"></div>
                      <div className="h-3 bg-muted rounded animate-pulse flex-1 w-4/5"></div>
                    </div>
                    <div className="flex items-center gap-2">
                      <div className="w-2 h-2 bg-primary rounded-full"></div>
                      <div className="h-3 bg-muted rounded animate-pulse flex-1 w-3/4"></div>
                    </div>
                  </div>
                ) : analysis?.keyConcepts && analysis.keyConcepts.length > 0 ? (
                  // Analysis results
                  <div className="space-y-3">
                    {analysis.keyConcepts.map((concept, index) => (
                      <div key={index} className="flex items-center gap-2">
                        <div className="w-2 h-2 bg-primary rounded-full"></div>
                        <div className="text-sm">{concept}</div>
                      </div>
                    ))}
                  </div>
                ) : (
                  // Empty state
                  <div className="p-3 bg-muted/50 rounded-lg">
                    <p className="text-xs text-muted-foreground text-center">Ключевые концепции из текста</p>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Further Reading */}
            <Card>
              <CardHeader>
                <CardTitle className="text-xl font-semibold">Further Reading</CardTitle>
              </CardHeader>
              <CardContent>
                {loading ? (
                  // Loading state
                  <div className="space-y-3">
                    <div className="p-3 border rounded-lg hover:bg-muted/50 transition-colors">
                      <div className="h-3 bg-muted rounded animate-pulse mb-2"></div>
                      <div className="h-2 bg-muted rounded animate-pulse w-3/4"></div>
                    </div>
                    <div className="p-3 border rounded-lg hover:bg-muted/50 transition-colors">
                      <div className="h-3 bg-muted rounded animate-pulse mb-2"></div>
                      <div className="h-2 bg-muted rounded animate-pulse w-4/5"></div>
                    </div>
                  </div>
                ) : analysis?.recommendations && analysis.recommendations.length > 0 ? (
                  // Analysis results
                  <div className="space-y-3">
                    {analysis.recommendations.map((rec, index) => (
                      <div key={index} className="p-3 border rounded-lg hover:bg-muted/50 transition-colors cursor-pointer">
                        <div className="font-medium mb-1">{rec.title}</div>
                        <div className="text-sm text-muted-foreground">{rec.description}</div>
                      </div>
                    ))}
                  </div>
                ) : (
                  // Empty state
                  <div className="mt-4 p-3 bg-muted/50 rounded-lg">
                    <p className="text-xs text-muted-foreground text-center">Рекомендации для дальнейшего изучения</p>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}
