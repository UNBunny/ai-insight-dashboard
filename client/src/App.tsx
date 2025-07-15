import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import Dashboard from './app/dashboard/page';
import UsersPage from './pages/users';
import Login from './pages/Login';
import TestAuth from './pages/TestAuth';
import AccessDenied from './pages/AccessDenied';
import './index.css';
import { ThemeProvider } from './context/theme-provider';
import { QueryProvider } from './context/query-provider';
import { AuthProvider } from './context/auth-context';
import { useAuthContext } from './context/auth-context';
import { ThemeToggle } from './components/ui/theme-toggle';
import { Menu, ChevronDown, ChevronRight, Users, Home, Settings, BarChart3, LogOut, Shield, UserCircle } from 'lucide-react';
import ProtectedRoute from './components/auth/ProtectedRoute';

// Main application layout component with sidebar for authenticated admin users
const AdminLayout: React.FC = () => {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activePage, setActivePage] = useState('dashboard');
  const { logout, user, isAdmin } = useAuthContext();
  
  const handleNavigation = (page: string) => {
    setActivePage(page);
  };
  
  const handleLogout = (e: React.MouseEvent) => {
    e.preventDefault();
    logout();
  };

  return (
    <div className="flex min-h-screen bg-background">
      {/* Admin Sidebar */}
      <aside 
        className={`bg-slate-900 text-white min-h-screen transition-all duration-300 ${sidebarCollapsed ? 'w-20' : 'w-64'}`}
      >
              <div className="p-4">
                <div className="flex items-center justify-between mb-8">
                  {!sidebarCollapsed && (
                    <h2 className="text-lg font-semibold">Admin Panel</h2>
                  )}
                  <button
                    onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
                    className="p-2 rounded-md hover:bg-slate-800 transition-colors"
                    aria-label={sidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
                  >
                    <Menu size={20} />
                  </button>
                </div>
                
                <nav>
                  <ul className="space-y-1">
                    {/* Dashboard Link */}
                    <li>
                      <a 
                        href="/admin/dashboard"
                        onClick={() => handleNavigation('dashboard')}
                        className={`flex items-center px-4 py-3 rounded-md transition-colors ${activePage === 'dashboard' ? 'bg-blue-700' : 'hover:bg-slate-800'}`}
                      >
                        <Home size={20} />
                        {!sidebarCollapsed && <span className="ml-3">Dashboard</span>}
                      </a>
                    </li>
                    
                    {/* Users Link */}
                    <li>
                      <a 
                        href="/admin/users"
                        onClick={() => handleNavigation('users')} 
                        className={`flex items-center px-4 py-3 rounded-md transition-colors ${activePage === 'users' ? 'bg-blue-700' : 'hover:bg-slate-800'}`}
                      >
                        <Users size={20} />
                        {!sidebarCollapsed && <span className="ml-3">Users</span>}
                      </a>
                    </li>
                    
                    {/* Analytics Link */}
                    <li>
                      <a 
                        href="/admin/analytics"
                        onClick={() => handleNavigation('analytics')}
                        className={`flex items-center px-4 py-3 rounded-md transition-colors ${activePage === 'analytics' ? 'bg-blue-700' : 'hover:bg-slate-800'}`}
                      >
                        <BarChart3 size={20} />
                        {!sidebarCollapsed && <span className="ml-3">Analytics</span>}
                      </a>
                    </li>
                    
                    {/* Settings Link */}
                    <li>
                      <a 
                        href="/admin/settings"
                        onClick={() => handleNavigation('settings')}
                        className={`flex items-center px-4 py-3 rounded-md transition-colors ${activePage === 'settings' ? 'bg-blue-700' : 'hover:bg-slate-800'}`}
                      >
                        <Settings size={20} />
                        {!sidebarCollapsed && <span className="ml-3">Settings</span>}
                      </a>
                    </li>
                    
                    {/* Test Auth Link */}
                    <li>
                      <a 
                        href="/admin/test-auth"
                        onClick={() => handleNavigation('test-auth')}
                        className={`flex items-center px-4 py-3 rounded-md transition-colors ${activePage === 'test-auth' ? 'bg-blue-700' : 'hover:bg-slate-800'}`}
                      >
                        <Shield size={20} />
                        {!sidebarCollapsed && <span className="ml-3">Test Auth</span>}
                      </a>
                    </li>
                  </ul>
                  
                  {/* Logout Section */}
                  <div className="mt-8 pt-4 border-t border-slate-700">
                    <a 
                      href="#"
                      onClick={handleLogout}
                      className="flex items-center px-4 py-3 rounded-md transition-colors hover:bg-slate-800 text-slate-300 hover:text-white"
                    >
                      <LogOut size={20} />
                      {!sidebarCollapsed && <span className="ml-3">Logout</span>}
                    </a>
                  </div>
                </nav>
              </div>
            </aside>
            
            {/* Main content area */}
            <div className="flex-1 flex flex-col">
              {/* Admin Header */}
              <header className="bg-white dark:bg-slate-800 shadow-sm h-16 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between px-6">
                <div className="flex items-center">
                  <h1 className="text-xl font-semibold text-slate-900 dark:text-white">AI Insight Admin</h1>
                </div>
                
                <div className="flex items-center gap-4">
                  {/* User Profile info */}
                  <div className="flex items-center mr-4 text-sm">
                    <UserCircle size={20} className="mr-2" />
                    <span className="font-medium">{user?.username}</span>
                    {isAdmin && (
                      <span className="ml-2 px-2 py-0.5 text-xs bg-blue-600 text-white rounded-full">
                        Admin
                      </span>
                    )}
                  </div>
                  <ThemeToggle />
                </div>
              </header>
              
              {/* Page content */}
              <main className="flex-1 overflow-y-auto bg-slate-50 dark:bg-slate-900 p-6">
                <Outlet />
              </main>
            </div>
          </div>
  );
};

// Settings page component
const SettingsPage: React.FC = () => {
  return (
    <div className="p-4 bg-white dark:bg-slate-800 rounded-lg shadow">
      <h2 className="text-xl font-bold mb-4">Admin Settings</h2>
      <p>Settings content will go here.</p>
    </div>
  );
};

// Analytics Admin page component
const AnalyticsAdminPage: React.FC = () => {
  return (
    <div className="p-4 bg-white dark:bg-slate-800 rounded-lg shadow">
      <h2 className="text-xl font-bold mb-4">Analytics Management</h2>
      <p>Admin tools for managing analytics and creating content</p>
    </div>
  );
};

// Компонент удален, так как его функциональность теперь разделена между PublicLayout и HomePage

// Публичный лейаут для обычных пользователей
const PublicLayout: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900">
      <header className="bg-white dark:bg-slate-800 shadow-sm">
        <div className="container mx-auto px-4">
          <div className="flex justify-between items-center py-4">
            <a href="/" className="text-2xl font-bold text-slate-900 dark:text-white">
              AI Insight Dashboard
            </a>
            <div className="flex items-center gap-4">
              <ThemeToggle />
              <a 
                href="/admin" 
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
              >
                Admin Login
              </a>
            </div>
          </div>
        </div>
      </header>
      
      <main>
        <Outlet />
      </main>
      
      <footer className="mt-12 py-6 border-t border-slate-200 dark:border-slate-800">
        <div className="container mx-auto px-4 text-center text-slate-500 dark:text-slate-400">
          © {new Date().getFullYear()} AI Insight Dashboard. All rights reserved.
        </div>
      </footer>
    </div>
  );
};

// Страница домашней аналитики для обычных пользователей
const HomePage: React.FC = () => {
  return (
    <div className="container mx-auto px-4 py-8">
      <Dashboard />
    </div>
  );
};

const App: React.FC = () => {
  return (
    <ThemeProvider defaultTheme="system" storageKey="ui-theme-preference">
      <QueryProvider>
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              {/* Публичные маршруты, доступные без авторизации */}
              <Route path="/" element={<PublicLayout />}>
                <Route index element={<HomePage />} />
                {/* Здесь можно добавить другие публичные страницы */}
              </Route>
              
              {/* Маршруты авторизации */}
              <Route path="/login" element={<Login />} />
              <Route path="/access-denied" element={<AccessDenied />} />
              
              {/* Маршруты админ-панели - все защищенные */}
              <Route path="/admin" element={
                <ProtectedRoute adminOnly>
                  <AdminLayout />
                </ProtectedRoute>
              }>
                <Route index element={<Navigate to="/admin/dashboard" />} />
                <Route path="dashboard" element={<Dashboard />} />
                <Route path="users" element={
                  <ProtectedRoute adminOnly>
                    <UsersPage />
                  </ProtectedRoute>
                } />
                <Route path="settings" element={
                  <ProtectedRoute adminOnly>
                    <SettingsPage />
                  </ProtectedRoute>
                } />
                <Route path="analytics" element={
                  <ProtectedRoute adminOnly>
                    <AnalyticsAdminPage />
                  </ProtectedRoute>
                } />
                <Route path="test-auth" element={
                  <ProtectedRoute>
                    <TestAuth />
                  </ProtectedRoute>
                } />
              </Route>
              
              {/* Запасной маршрут */}
              <Route path="*" element={<Navigate to="/" />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </QueryProvider>
    </ThemeProvider>
  );
};

export default App;
