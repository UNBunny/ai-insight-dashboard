import { useState, useEffect, useCallback } from 'react';
import { ApiResponse } from '../interfaces';

// Custom hook for fetching data from the API
const useFetchData = (initialFetch: boolean = true) => {
  const [data, setData] = useState<ApiResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(initialFetch);
  const [error, setError] = useState<string | null>(null);

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await fetch('/api/data');
      
      if (!response.ok) {
        throw new Error(`Error: ${response.status} - ${response.statusText}`);
      }
      
      const result = await response.json();
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unexpected error occurred');
      console.error('Error fetching data:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (initialFetch) {
      fetchData();
    }
  }, [initialFetch, fetchData]);

  return { data, loading, error, fetchData };
};

export default useFetchData;
