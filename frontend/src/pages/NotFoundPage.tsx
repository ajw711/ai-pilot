import React from "react";
import { FiAlertTriangle } from "react-icons/fi";

interface NotFoundPageProps {
  setActiveTab: (tab: string) => void;
}

export const NotFoundPage: React.FC<NotFoundPageProps> = ({ setActiveTab }) => {
  return (
    <div className="flex flex-1 flex-col items-center justify-center min-h-[80vh] bg-[#F5F6F7] dark:bg-[#16171d] px-4 text-center transition-colors duration-200">
      <div className="max-w-md p-8 bg-white dark:bg-[#1f2028] border border-[#E4E8EB] dark:border-slate-800 rounded-lg">
        {/* 경고 아이콘 */}
        <div className="inline-flex h-14 w-14 items-center justify-center rounded-full bg-[#FFF0F0] dark:bg-rose-950/20 text-[#D83A3A] dark:text-[#f87171] mb-5">
          <FiAlertTriangle className="h-7 w-7" />
        </div>

        {/* 에러 타이틀 */}
        <h1 className="text-xl font-bold text-[#1E1E1E] dark:text-white leading-tight">
          페이지를 찾을 수 없습니다
        </h1>
        
        {/* 에러 설명 */}
        <p className="mt-3 text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
          접근하려는 페이지의 주소가 잘못되었거나, 비활성화되어 접근할 수 없습니다. 입력하신 주소를 다시 한 번 확인해 주시기 바랍니다.
        </p>

        {/* 메인 홈 이동 버튼 */}
        <div className="mt-6 pt-2 border-t border-[#E4E8EB] dark:border-slate-800">
          <button
            onClick={() => setActiveTab("home")}
            className="w-full rounded-lg bg-[#03C75A] px-4 py-2.5 text-xs font-bold text-white hover:bg-[#02b350] transition-all cursor-pointer"
          >
            메인 홈으로 이동
          </button>
        </div>
      </div>
    </div>
  );
};
