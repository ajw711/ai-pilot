import { useState, useEffect } from "react";
import { Toaster } from "react-hot-toast";
import { SidebarLayout } from "./components/layouts/SidebarLayout";
import { api, setAccessToken } from "./lib/api";
import { DashboardPage } from "./pages/DashboardPage";
import { ChatPage } from "./pages/ChatPage";
import { KnowledgeChatPage } from "./pages/KnowledgeChatPage";
import { DocumentPage } from "./pages/DocumentPage";
import { HomePage } from "./pages/HomePage";
import { LoginPage } from "./pages/LoginPage";
import { NotFoundPage } from "./pages/NotFoundPage";

function App() {
  const [isInitializing, setIsInitializing] = useState<boolean>(true);
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(() => {
    return localStorage.getItem("isLoggedIn") === "true";
  });
  const [activeTab, setActiveTab] = useState<string>("home");
  const [darkMode, setDarkMode] = useState<boolean>(() => {
    const saved = localStorage.getItem("theme");
    if (saved) return saved === "dark";
    return window.matchMedia("(prefers-color-scheme: dark)").matches;
  });

  // Apply dark mode class to html element
  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add("dark");
      localStorage.setItem("theme", "dark");
    } else {
      document.documentElement.classList.remove("dark");
      localStorage.setItem("theme", "light");
    }
  }, [darkMode]);

  // 새로고침 시 인메모리 토큰 복원 (Silent Refresh)
  useEffect(() => {
    const recoverSession = async () => {
      if (isLoggedIn) {
        try {
          const res = await api.post(
            "/auth/refresh",
            {},
            { withCredentials: true },
          );
          const { accessToken } = res.data.data;
          setAccessToken(accessToken);
        } catch (err) {
          console.error("세션 초기화 실패: 재로그인이 필요합니다.", err);
          setAccessToken(null);
          setIsLoggedIn(false);
          localStorage.removeItem("isLoggedIn");
        } finally {
          setIsInitializing(false);
        }
      } else {
        setIsInitializing(false);
      }
    };
    recoverSession();
  }, []);

  if (isInitializing) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[#F5F6F7] dark:bg-[#16171d] text-slate-500 dark:text-slate-400">
        세션을 복구하고 있습니다...
      </div>
    );
  }

  const renderContent = () => {
    switch (activeTab) {
      case "home":
        return <HomePage setActiveTab={setActiveTab} />;
      case "dashboard":
        return <DashboardPage />;
      case "document":
        return <DocumentPage />;
      case "chat":
        return <ChatPage />;
      case "knowledge-chat":
        return <KnowledgeChatPage />;
      default:
        return <NotFoundPage setActiveTab={setActiveTab} />;
    }
  };

  if (!isLoggedIn) {
    return (
      <>
        <Toaster position="top-right" reverseOrder={false} />
        <LoginPage onLoginSuccess={() => setIsLoggedIn(true)} />
      </>
    );
  }

  return (
    <>
      <Toaster position="top-right" reverseOrder={false} />

      <SidebarLayout
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        darkMode={darkMode}
        setDarkMode={setDarkMode}
      >
        {renderContent()}
      </SidebarLayout>
    </>
  );
}

export default App;
