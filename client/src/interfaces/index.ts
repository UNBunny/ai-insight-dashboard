// Data interface for API responses
export interface ApiResponse {
  success: boolean;
  data: DataPoint[];
  timestamp: string;
}

export interface DataPoint {
  id: number;
  label: string;
  value: number;
  category: string;
  trend: 'up' | 'down' | 'stable';
  percentage: number;
}

// Props for layout component
export interface LayoutProps {
  children: React.ReactNode;
}

// Stats card props
export interface StatCardProps {
  title: string;
  value: number | string;
  icon?: React.ReactNode;
  trend?: 'up' | 'down' | 'stable';
  percentage?: number;
  loading?: boolean;
}

// Chart data props
export interface ChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    backgroundColor: string | string[];
    borderColor?: string | string[];
    borderWidth?: number;
  }[];
}
