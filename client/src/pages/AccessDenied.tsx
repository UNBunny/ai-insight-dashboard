import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Shield, AlertTriangle, Home } from 'lucide-react';
import { useAuthContext } from '../context/auth-context';

/**
 * Access Denied page shown when a user tries to access a restricted page
 */
const AccessDenied: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuthContext();
  
  const handleGoHome = () => {
    navigate('/');
  };
  
  const handleGoToLogin = () => {
    if (isAuthenticated) {
      logout(); // Log out the current user first
    }
    navigate('/login');
  };
  
  return (
    <div className="flex items-center justify-center min-h-screen bg-slate-50 dark:bg-slate-900">
      <div className="w-full max-w-md p-8 bg-white dark:bg-slate-800 rounded-lg shadow-md">
        <div className="flex justify-center mb-6">
          <div className="rounded-full bg-red-100 dark:bg-red-900 p-3">
            <AlertTriangle size={48} className="text-red-500 dark:text-red-300" />
          </div>
        </div>
        
        <h1 className="text-2xl font-bold text-center mb-2 text-slate-900 dark:text-white">
          Access Denied
        </h1>
        
        <p className="text-center mb-6 text-slate-600 dark:text-slate-300">
          You don't have permission to access this page. This area requires administrative privileges.
        </p>
        
        <div className="flex flex-col gap-3">
          <Button onClick={handleGoHome} className="w-full flex items-center justify-center">
            <Home className="mr-2" size={18} />
            Go to Dashboard
          </Button>
          
          <Button 
            onClick={handleGoToLogin} 
            variant="outline" 
            className="w-full flex items-center justify-center"
          >
            <Shield className="mr-2" size={18} />
            {isAuthenticated ? "Log out and Sign in as Admin" : "Sign in as Admin"}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default AccessDenied;
