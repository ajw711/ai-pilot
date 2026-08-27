import React, { useState } from "react";
import { logoutApi } from "../../features/auth/api";
import {
  FiMessageSquare,
  FiGrid,
  FiMenu,
  FiX,
  FiSun,
  FiMoon,
  FiHome,
  FiLogOut,
  FiBookOpen,
  FiFileText,
} from "react-icons/fi";

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
    { id: "home", label: "홈", icon: FiHome },
    { id: "dashboard", label: "지식 대시보드", icon: FiGrid },
    { id: "document", label: "문서 관리", icon: FiFileText },
    { id: "chat", label: "Ops AI 챗봇", icon: FiMessageSquare },
    { id: "knowledge-chat", label: "지식 AI 챗봇", icon: FiBookOpen },
  ];

  const handleMenuClick = (id: string) => {
    setActiveTab(id);
    setIsOpen(false); // 모바일에서 메뉴 선택 시 사이드바를 자동으로 닫음
  };

  return (
    <div className="flex h-screen w-full overflow-hidden bg-[#F5F6F7] dark:bg-[#16171d] text-[#1E1E1E] dark:text-slate-100 font-sans transition-colors duration-200">
      {/* 1. 모바일용 Backdrop 오버레이 */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/10 backdrop-blur-xs md:hidden"
          onClick={() => setIsOpen(false)}
        />
      )}

      {/* 2. 반응형 사이드바 (가독성 높은 백그라운드 및 옅은 그레이 보더) */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-[#E4E8EB] dark:border-slate-800 bg-white dark:bg-slate-900 transition-transform duration-300 ease-in-out md:relative md:translate-x-0 ${
          isOpen ? "translate-x-0" : "-translate-x-full md:translate-x-0"
        }`}
      >
        {/* 사이드바 헤더 */}
        <div className="flex h-16 items-center justify-between border-b border-[#E4E8EB] dark:border-slate-800 px-6 bg-white dark:bg-slate-900">
          <div
            onClick={() => handleMenuClick("home")}
            className="flex items-center gap-2 cursor-pointer hover:opacity-90 transition-opacity"
          >
            <div className="h-8 w-8 rounded-md bg-[#03C75A] flex items-center justify-center font-bold text-white">
              AI
            </div>
            <span className="text-base font-bold tracking-wider text-[#1E1E1E] dark:text-white">
              AI-PILOT
            </span>
          </div>
          {/* 모바일 닫기 버튼 */}
          <button
            onClick={() => setIsOpen(false)}
            className="rounded-md p-1.5 text-slate-400 hover:bg-[#F5F6F7] dark:hover:bg-slate-800 md:hidden transition-colors"
          >
            <FiX className="h-6 w-6" />
          </button>
        </div>

        {/* 메뉴 목록 (단정한 텍스트/아이콘 컬러 스위칭) */}
        <nav className="flex-1 space-y-1.5 p-4 bg-white dark:bg-slate-900 overflow-y-auto">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => handleMenuClick(item.id)}
                className={`flex w-full items-center gap-3 rounded-lg px-4 py-2.5 text-sm font-bold transition-all group cursor-pointer ${
                  isActive
                    ? "text-[#03C75A] bg-[#E6F7ED] hover:bg-[#D4F0DF] dark:bg-[#1E3A27] dark:hover:bg-[#152e1c]"
                    : "text-[#333333] dark:text-slate-300 hover:bg-[#EBECEF] dark:hover:bg-[#25272f] hover:text-[#1E1E1E] dark:hover:text-white"
                }`}
              >
                <Icon
                  className={`h-5 w-5 ${
                    isActive
                      ? "text-[#03C75A]"
                      : "text-[#666666] dark:text-slate-400 group-hover:text-[#1E1E1E] dark:group-hover:text-white"
                  }`}
                />
                {item.label}
              </button>
            );
          })}
        </nav>

        {/* 하단 테마 전환 및 설정 영역 (고대비 및 정렬) */}
        <div className="border-t border-[#E4E8EB] dark:border-slate-800 p-4 space-y-1.5 bg-white dark:bg-slate-900">
          {/* 테마 토글 버튼 */}
          <button
            onClick={() => setDarkMode(!darkMode)}
            className="flex w-full items-center gap-3 rounded-lg px-4 py-2.5 text-sm font-bold text-[#333333] dark:text-slate-300 hover:bg-[#EBECEF] dark:hover:bg-[#25272f] hover:text-[#1E1E1E] dark:hover:text-white transition-all cursor-pointer"
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
          {/* 로그아웃 버튼 */}
          <button
            onClick={async () => {
              try {
                await logoutApi();
              } catch (err) {
                console.error("서버 로그아웃 무효화 에러:", err);
              } finally {
                localStorage.removeItem("isLoggedIn");
                localStorage.removeItem("access_token");
                localStorage.removeItem("userId");
                window.location.reload();
              }
            }}
            className="flex w-full items-center gap-3 rounded-lg px-4 py-2.5 text-sm font-bold text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/20 transition-all cursor-pointer"
          >
            <FiLogOut className="h-5 w-5" />
            로그아웃
          </button>
        </div>
      </aside>

      {/* 3. 메인 콘텐츠 영역 */}
      <main className="flex flex-1 flex-col overflow-hidden bg-[#F5F6F7] dark:bg-[#16171d] transition-colors duration-200">
        {/* 모바일용 상단 헤더 */}
        <header className="flex h-16 items-center justify-between border-b border-[#E4E8EB] dark:border-slate-800 bg-white dark:bg-slate-900 px-4 md:hidden">
          <button
            onClick={() => setIsOpen(true)}
            className="rounded-lg p-2 text-slate-500 hover:bg-[#F5F6F7] transition-colors"
          >
            <FiMenu className="h-6 w-6" />
          </button>
          <div
            onClick={() => {
              setActiveTab("home");
              setIsOpen(false);
            }}
            className="flex items-center gap-2 cursor-pointer hover:opacity-90 transition-opacity"
          >
            <div className="h-7 w-7 rounded-md bg-[#03C75A] flex items-center justify-center font-bold text-white text-sm">
              AI
            </div>
            <span className="text-sm font-bold tracking-wider text-[#1E1E1E] dark:text-white">
              AI-PILOT
            </span>
          </div>
          <div className="w-10"></div>
        </header>

        {/* 페이지별 실제 콘텐츠 영역 */}
        <div className="flex-1 overflow-y-auto">{children}</div>
      </main>
    </div>
  );
};
