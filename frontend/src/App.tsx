import { useState, useEffect } from 'react';
import { SidebarLayout } from './components/layouts/SidebarLayout';
import { DashboardPage } from './pages/DashboardPage';
import { ChatPage } from './pages/ChatPage';

function App() {
  const [activeTab, setActiveTab] = useState<string>('dashboard');
  const [darkMode, setDarkMode] = useState<boolean>(() => {
    const saved = localStorage.getItem('theme');
    if (saved) return saved === 'dark';
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  });

  // Apply dark mode class to html element
  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }, [darkMode]);

  const renderContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return <DashboardPage />;
      case 'chat':
        return <ChatPage />;
      case 'sources':
        return (
          <div className="flex flex-1 items-center justify-center text-slate-400 dark:text-slate-500">
            <p className="text-lg">데이터 소스 관리 기능 준비 중...</p>
          </div>
        );
      case 'settings':
        return (
          <div className="flex flex-1 items-center justify-center text-slate-400 dark:text-slate-500">
            <p className="text-lg">설정 페이지 준비 중...</p>
          </div>
        );
      default:
        return <DashboardPage />;
    }
  };

  return (
    <SidebarLayout 
      activeTab={activeTab} 
      setActiveTab={setActiveTab}
      darkMode={darkMode}
      setDarkMode={setDarkMode}
    >
      {renderContent()}
    </SidebarLayout>
  );
}

export default App;
