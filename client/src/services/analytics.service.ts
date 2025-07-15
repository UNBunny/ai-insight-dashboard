import api from '../utils/api';

/**
 * Service for AI analytics functionality
 */
export interface AnalysisRequest {
  topic: string;
  text?: string;
}

export interface AnalysisResponse {
  topic: string;
  summary: string;
  keyConcepts: string[];
  recommendations: Array<{
    title: string;
    description: string;
  }>;
  timestamp: string;
}

/**
 * Analytics service for getting AI insights
 */
export const analyticsService = {
  /**
   * Get AI analysis for a topic or text
   * This is a public endpoint that doesn't require authentication
   */
  async analyzeContent(request: AnalysisRequest): Promise<AnalysisResponse> {
    try {
      // Force skipAuth: true to ensure this works for public users
      return await api.post<AnalysisResponse>('/api/v1/ai/analyze', request, { skipAuth: true });
    } catch (error) {
      console.error('Error analyzing content:', error);
      throw error;
    }
  },
};

export default analyticsService;
