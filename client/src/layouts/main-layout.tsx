import React from 'react';
import Sidebar from '../components/layout/sidebar';
import { ThemeToggle } from '../components/ui/theme-toggle';
import { useAuthContext } from '../context/auth-context';

interface MainLayoutProps {
  children: React.ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const { logout } = useAuthContext();
  
  const handleLogout = () => {
    logout();
  };
  
  return (
    <div className="flex min-h-screen bg-background">
      {/* Sidebar */}
      <Sidebar />
      
      {/* Main content */}
      <div className="flex-1 flex flex-col">
        {/* Header */}
        <header className="h-16 border-b flex items-center justify-between px-6 bg-card">
          <h1 className="text-xl font-semibold">AI Insight Dashboard</h1>
          <div className="flex items-center gap-4">
            <ThemeToggle />
            <button 
              onClick={handleLogout}
              className="px-3 py-1 bg-red-500 hover:bg-red-600 text-white rounded-md"
            >
              Выйти
            </button>
          </div>
        </header>
        
        {/* Page content */}
        <main className="flex-1 overflow-y-auto">
          {children}
        </main>
      </div>
    </div>
  );
};

export default MainLayout;
