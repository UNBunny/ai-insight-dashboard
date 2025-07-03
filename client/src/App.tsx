import React from 'react';
import Dashboard from './app/dashboard/page';
import './index.css';
import { ThemeProvider } from './context/theme-provider';

function App() {
  return (
    <ThemeProvider defaultTheme="light" storageKey="ui-theme-preference">
      <div className="App min-h-screen bg-background text-foreground">
        <Dashboard />
      </div>
    </ThemeProvider>
  );
}

export default App;
