import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, Users, Settings } from 'lucide-react';

interface NavigationItem {
  name: string;
  path: string;
  icon: React.ReactNode;
}

export const Sidebar: React.FC = () => {
  const location = useLocation();
  
  const navigation: NavigationItem[] = [
    {
      name: 'Dashboard',
      path: '/',
      icon: <LayoutDashboard className="h-5 w-5" />,
    },
    {
      name: 'Пользователи',
      path: '/users',
      icon: <Users className="h-5 w-5" />,
    },
    {
      name: 'Настройки',
      path: '/settings',
      icon: <Settings className="h-5 w-5" />,
    },
  ];

  return (
    <aside className="bg-card text-card-foreground w-64 min-h-screen border-r dark:border-gray-800">
      <div className="p-4">
        <h2 className="text-lg font-semibold mb-6">AI Insight Dashboard</h2>
        <nav>
          <ul className="space-y-2">
            {navigation.map((item) => (
              <li key={item.path}>
                <Link
                  to={item.path}
                  className={`flex items-center px-4 py-2 rounded-md transition-colors ${
                    location.pathname === item.path
                      ? 'bg-primary text-primary-foreground'
                      : 'hover:bg-primary/10'
                  }`}
                >
                  {item.icon}
                  <span className="ml-3">{item.name}</span>
                </Link>
              </li>
            ))}
          </ul>
        </nav>
      </div>
    </aside>
  );
};

export default Sidebar;
