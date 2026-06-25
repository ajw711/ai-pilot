import React, { useState } from 'react';
import { FiDatabase, FiMessageSquare, FiSettings, FiGrid, FiMenu, FiX, FiSun, FiMoon } from 'react-icons/fi';

interface SidebarLayoutProps {
  children: React.ReactNode;
  activeTab: string;
  setActiveTab: (tab: string) => void;
  darkMode: boolean;
  setDarkMode: (dark: boolean) => void;
}

export const SidebarLayout: React.FC<SidebarLayoutProps> = ({
  children,
  activeTab,
  setActiveTab,
  darkMode,
  setDarkMode,
}) => {
  const [isOpen, setIsOpen] = useState(false); // 모바일 사이드바 토글 상태

  const menuItems = [
    { id: 'dashboard', label: '지식 대시보드', icon: FiGrid },
    { id: 'chat', label: 'AI 지식 챗봇', icon: FiMessageSquare },
    { id: 'sources', label: '데이터 소스 관리', icon: FiDatabase },
  ];

  const handleMenuClick = (id: string) => {
    setActiveTab(id);
    setIsOpen(false); // 모바일에서 메뉴 선택 시 사이드바를 자동으로 닫음
  };

  return (
    <div className="flex h-screen w-full overflow-hidden bg-slate-50 dark:bg-slate-950 text-slate-800 dark:text-slate-100 font-sans transition-colors duration-200">
      
      {/* 1. 모바일용 Backdrop 오버레이 (사이드바 오픈 시 뒷배경 어둡게 처리) */}
      {isOpen && (
        <div 
          className="fixed inset-0 z-40 bg-slate-900/20 backdrop-blur-sm md:hidden"
          onClick={() => setIsOpen(false)}
        />
      )}

      {/* 2. 반응형 사이드바 (반응형 다크/라이트 테마) */}
      <aside className={`fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 transition-transform duration-300 ease-in-out md:relative md:translate-x-0 ${
        isOpen ? 'translate-x-0' : '-translate-x-full'
      }`}>
        {/* 사이드바 헤더 및 닫기 버튼 */}
        <div className="flex h-16 items-center justify-between border-b border-slate-300 dark:border-slate-600 px-6">
          <div className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center font-bold text-white shadow-md shadow-sky-500/10">
              AI
            </div>
            <span className="text-lg font-bold tracking-wider bg-gradient-to-r from-sky-500 to-indigo-500 bg-clip-text text-transparent">
              AI-PILOT
            </span>
          </div>
          {/* 모바일 닫기 버튼 */}
          <button 
            onClick={() => setIsOpen(false)}
            className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-700 md:hidden transition-colors"
          >
            <FiX className="h-6 w-6" />
          </button>
        </div>

        {/* 메뉴 목록 (다크/라이트 테마 스타일) */}
        <nav className="flex-1 space-y-1.5 p-4 overflow-y-auto">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => handleMenuClick(item.id)}
                className={`flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition-all duration-200 group cursor-pointer ${
                  isActive
                    ? 'bg-sky-50 dark:bg-sky-950/40 text-sky-600 dark:text-sky-400 border-l-4 border-sky-500 shadow-sm'
                    : 'text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800/40 hover:text-slate-900 dark:hover:text-slate-100'
                }`}
              >
                <Icon
                  className={`h-5 w-5 transition-transform duration-200 group-hover:scale-110 ${
                    isActive ? 'text-sky-600 dark:text-sky-400' : 'text-slate-400 dark:text-slate-500 group-hover:text-slate-600 dark:group-hover:text-slate-300'
                  }`}
                />
                {item.label}
              </button>
            );
          })}
        </nav>

        {/* 하단 테마 전환 및 설정 영역 */}
        <div className="border-t border-slate-300 dark:border-slate-600 p-4 space-y-1.5">
          {/* 테마 토글 버튼 */}
          <button
            onClick={() => setDarkMode(!darkMode)}
            className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800/40 hover:text-slate-900 dark:hover:text-slate-100 transition-all cursor-pointer"
          >
            {darkMode ? (
              <>
                <FiSun className="h-5 w-5 text-amber-500" />
                <span>라이트 모드로 보기</span>
              </>
            ) : (
              <>
                <FiMoon className="h-5 w-5 text-indigo-500" />
                <span>다크 모드로 보기</span>
              </>
            )}
          </button>

          <button
            onClick={() => handleMenuClick('settings')}
            className={`flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition-all cursor-pointer ${
              activeTab === 'settings'
                ? 'bg-sky-50 dark:bg-sky-950/40 text-sky-600 dark:text-sky-400 border-l-4 border-sky-500'
                : 'text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800/40 hover:text-slate-900 dark:hover:text-slate-100'
            }`}
          >
            <FiSettings className="h-5 w-5" />
            설정
          </button>
        </div>
      </aside>

      {/* 3. 메인 콘텐츠 영역 (반응형 테마 배경) */}
      <main className="flex flex-1 flex-col overflow-hidden bg-slate-50 dark:bg-slate-950 transition-colors duration-200">
        
        {/* 모바일용 상단 헤더 (햄버거 버튼) */}
        <header className="flex h-16 items-center justify-between border-b border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 px-4 md:hidden">
          <button
            onClick={() => setIsOpen(true)}
            className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 transition-colors"
          >
            <FiMenu className="h-6 w-6" />
          </button>
          <div className="flex items-center gap-2">
            <div className="h-7 w-7 rounded-md bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center font-bold text-white text-sm">
              AI
            </div>
            <span className="text-sm font-bold tracking-wider text-slate-800 dark:text-slate-100">AI-PILOT</span>
          </div>
          <div className="w-10"></div>
        </header>

        {/* 페이지별 실제 콘텐츠 영역 */}
        <div className="flex-1 overflow-y-auto">
          {children}
        </div>
      </main>
    </div>
  );
};
