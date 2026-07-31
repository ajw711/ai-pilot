import React, { useState } from "react";
import { loginApi } from "../features/auth/api";
import { FiLock, FiUser, FiCheckSquare, FiSquare } from "react-icons/fi";

interface LoginPageProps {
  onLoginSuccess: () => void;
}

export const LoginPage: React.FC<LoginPageProps> = ({ onLoginSuccess }) => {
  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [rememberId, setRememberId] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  const handleLogin = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!userId.trim() || !password.trim()) {
      setErrorMsg("아이디와 비밀번호를 모두 입력해 주세요.");
      return;
    }

    try {
      // 캡슐화된 loginApi 우회 호출
      const data = await loginApi({
        username: userId,
        password: password,
      });

      localStorage.setItem("access_token", data.accessToken);
      localStorage.setItem("isLoggedIn", "true");
      localStorage.setItem("userId", userId);
      onLoginSuccess();
    } catch (err: any) {
      console.error(err);
      setErrorMsg(
        err.response?.data?.error?.message || "로그인에 실패했습니다.",
      );
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-[#F5F6F7] dark:bg-[#16171d] px-4 py-12 transition-colors duration-200">
      <div className="w-full max-w-[460px] space-y-8 bg-white dark:bg-[#1f2028] p-8 border border-[#E4E8EB] dark:border-slate-800 rounded-lg shadow-xs">
        {/* 상단 브랜드 헤더 */}
        <div className="text-center">
          <div className="inline-flex h-12 w-12 items-center justify-center rounded-lg bg-[#03C75A] font-bold text-white text-xl mb-3">
            AI
          </div>
          <h2 className="text-2xl font-bold tracking-wider text-[#1E1E1E] dark:text-white">
            AI-PILOT
          </h2>
          <p className="text-xs text-slate-400 dark:text-slate-500 mt-1">
            통합 운영 어드민 포털 시스템 로그인
          </p>
        </div>

        {/* 에러 메시지 알림 */}
        {errorMsg && (
          <div className="bg-[#FFF0F0] border border-[#FCD4D4] dark:bg-rose-950/20 dark:border-rose-900/30 text-xs text-[#D83A3A] dark:text-[#f87171] p-3 rounded-lg font-semibold text-center">
            {errorMsg}
          </div>
        )}

        {/* 로그인 폼 */}
        <form className="mt-8 space-y-4" onSubmit={handleLogin}>
          {/* 아이디 인풋 */}
          <div className="space-y-1">
            <label className="text-xs font-bold text-[#666666] dark:text-slate-400">
              아이디
            </label>
            <div className="flex items-center gap-2 border border-[#E4E8EB] dark:border-slate-800 rounded-lg p-2.5 bg-slate-50 dark:bg-[#16171d] focus-within:border-[#03C75A] focus-within:bg-white dark:focus-within:bg-[#1f2028] transition-all">
              <FiUser className="text-slate-400 h-4.5 w-4.5" />
              <input
                type="text"
                placeholder="아이디를 입력하세요 (test-user)"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                className="flex-1 bg-transparent text-sm text-[#1E1E1E] dark:text-[#f3f4f6] placeholder-slate-400 outline-none"
              />
            </div>
          </div>

          {/* 비밀번호 인풋 */}
          <div className="space-y-1">
            <label className="text-xs font-bold text-[#666666] dark:text-slate-400">
              비밀번호
            </label>
            <div className="flex items-center gap-2 border border-[#E4E8EB] dark:border-slate-800 rounded-lg p-2.5 bg-slate-50 dark:bg-[#16171d] focus-within:border-[#03C75A] focus-within:bg-white dark:focus-within:bg-[#1f2028] transition-all">
              <FiLock className="text-slate-400 h-4.5 w-4.5" />
              <input
                type="password"
                placeholder="비밀번호를 입력하세요 (1234)"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="flex-1 bg-transparent text-sm text-[#1E1E1E] dark:text-[#f3f4f6] placeholder-slate-400 outline-none"
              />
            </div>
          </div>

          {/* 하단 옵션 영역 (로그인 상태 유지 토글) */}
          <div className="flex items-center justify-between pt-1">
            <button
              type="button"
              onClick={() => setRememberId(!rememberId)}
              className="flex items-center gap-1.5 text-xs font-semibold text-[#666666] dark:text-slate-400 hover:text-[#1E1E1E] dark:hover:text-white transition-colors cursor-pointer"
            >
              {rememberId ? (
                <FiCheckSquare className="h-4.5 w-4.5 text-[#03C75A]" />
              ) : (
                <FiSquare className="h-4.5 w-4.5 text-slate-400" />
              )}
              로그인 상태 유지
            </button>
          </div>

          {/* 로그인 버튼 */}
          <div className="pt-4">
            <button
              type="submit"
              className="w-full flex items-center justify-center rounded-lg bg-[#03C75A] px-4 py-3 text-sm font-bold text-white hover:bg-[#02b350] active:scale-98 transition-all cursor-pointer"
            >
              로그인
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
