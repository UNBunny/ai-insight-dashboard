import React from 'react';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, PointElement, LineElement } from 'chart.js';
import { Bar } from 'react-chartjs-2';
import { ChartData } from '../interfaces';

// Register ChartJS components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

interface DashboardChartProps {
  data: ChartData;
  title: string;
  loading?: boolean;
}

const DashboardChart: React.FC<DashboardChartProps> = ({ data, title, loading = false }) => {
  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top' as const,
      },
      title: {
        display: true,
        text: title,
      },
    },
  };

  if (loading) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6 min-h-[350px] flex flex-col">
        <div className="text-lg font-medium text-gray-900 dark:text-white mb-4">{title}</div>
        <div className="flex-grow flex items-center justify-center">
          <div className="animate-pulse flex flex-col items-center">
            <div className="h-48 w-full bg-gray-200 dark:bg-gray-700 rounded"></div>
            <div className="mt-4 w-3/4 h-4 bg-gray-200 dark:bg-gray-700 rounded"></div>
            <div className="mt-2 w-1/2 h-4 bg-gray-200 dark:bg-gray-700 rounded"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6 min-h-[350px]">
      <div className="h-[300px]">
        <Bar options={options} data={data} />
      </div>
    </div>
  );
};

export default DashboardChart;
