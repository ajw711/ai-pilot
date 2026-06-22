import { useState } from 'react';
import { SidebarLayout } from './components/layouts/SidebarLayout';
import { DashboardPage } from './pages/DashboardPage';
import { ChatPage } from './pages/ChatPage';

function App() {
  const [activeTab, setActiveTab] = useState<string>('dashboard');

  const renderContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return <DashboardPage />;
      case 'chat':
        return <ChatPage />;
      case 'sources':
        return (
          <div className="flex flex-1 items-center justify-center text-slate-400">
            <p className="text-lg">데이터 소스 관리 기능 준비 중...</p>
          </div>
        );
      case 'settings':
        return (
          <div className="flex flex-1 items-center justify-center text-slate-400">
            <p className="text-lg">설정 페이지 준비 중...</p>
          </div>
        );
      default:
        return <DashboardPage />;
    }
  };

  return (
    <SidebarLayout activeTab={activeTab} setActiveTab={setActiveTab}>
      {renderContent()}
    </SidebarLayout>
  );
}

export default App;
