import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { useAuthContext } from '../context/auth-context';
import api from '../utils/api';

/**
 * Test component for verifying our authentication and authorization configuration
 */
const TestAuth: React.FC = () => {
  const { isAuthenticated, isAdmin, user } = useAuthContext();
  const [results, setResults] = useState<{endpoint: string; status: string; data?: any; error?: string}[]>([]);
  const [loading, setLoading] = useState(false);

  // Test various endpoints to verify authorization
  const runTests = async () => {
    setLoading(true);
    setResults([]);
    const testEndpoints = [
      { name: 'Public Health Endpoint', path: '/actuator/health', publicAccess: true },
      { name: 'User Profile', path: '/api/v1/users/profile', requiresAuth: true },
      { name: 'AI Analysis', path: '/api/v1/ai/status', requiresAdmin: true },
      { name: 'All Users List', path: '/api/v1/users', requiresAdmin: true },
      { name: 'Admin Status', path: '/api/v1/admin/status', requiresAdmin: true }
    ];

    for (const endpoint of testEndpoints) {
      try {
        const response = await api.get(endpoint.path);
        setResults(prev => [...prev, {
          endpoint: endpoint.name,
          status: 'SUCCESS',
          data: response
        }]);
      } catch (error: any) {
        setResults(prev => [...prev, {
          endpoint: endpoint.name,
          status: `ERROR (${error.status || 'unknown'})`,
          error: error.message
        }]);
      }
    }
    setLoading(false);
  };

  return (
    <Card className="max-w-4xl mx-auto my-8">
      <CardHeader>
        <CardTitle>Authentication & Authorization Test</CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid gap-2">
          <div><strong>Authentication Status:</strong> {isAuthenticated ? 'Authenticated' : 'Not Authenticated'}</div>
          <div><strong>User:</strong> {user?.username || 'None'}</div>
          <div><strong>Admin Privileges:</strong> {isAdmin ? 'Yes' : 'No'}</div>
          <div><strong>Roles:</strong> {user?.roles?.join(', ') || 'None'}</div>
        </div>

        <Button onClick={runTests} disabled={loading}>
          {loading ? 'Running Tests...' : 'Test Endpoints'}
        </Button>

        {results.length > 0 && (
          <div className="mt-4">
            <h3 className="font-semibold mb-2">Test Results:</h3>
            <div className="border rounded-md divide-y">
              {results.map((result, index) => (
                <div key={index} className="p-3">
                  <div className="flex justify-between">
                    <strong>{result.endpoint}</strong>
                    <span className={result.status.includes('ERROR') ? 'text-red-500' : 'text-green-500'}>
                      {result.status}
                    </span>
                  </div>
                  {result.error && (
                    <div className="text-sm text-red-500 mt-1">{result.error}</div>
                  )}
                  {result.data && (
                    <div className="text-sm mt-1">
                      <pre className="bg-gray-100 p-2 rounded overflow-x-auto">
                        {JSON.stringify(result.data, null, 2)}
                      </pre>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default TestAuth;
