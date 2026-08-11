import React from "react";
import { FiMessageSquare, FiGrid, FiActivity, FiServer, FiCpu } from "react-icons/fi";

interface HomePageProps {
  setActiveTab: (tab: string) => void;
}

export const HomePage: React.FC<HomePageProps> = ({ setActiveTab }) => {
  return (
    <div className="p-4 sm:p-6 lg:p-8 bg-[#F5F6F7] dark:bg-[#16171d] min-h-full transition-colors duration-200">
      {/* 1. 상단 타이틀 헤더 */}
      <div className="border-b border-[#E4E8EB] dark:border-slate-800 pb-6 mb-8">
        <h1 className="text-2xl font-bold tracking-tight text-[#1E1E1E] dark:text-slate-100">
          AI-PILOT 통합 운영 콘솔
        </h1>
        <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
          지식 정보 수집 모니터링 및 Kubernetes 인프라 비동기 제어를 수행하는 통합 제어 화면입니다.
        </p>
      </div>

      {/* 2. 시스템 상태 요약 대시보드 */}
      <div className="grid gap-6 md:grid-cols-3 mb-8">
        {[
          {
            title: "Kubernetes API",
            status: "정상 작동 중",
            color: "text-[#03C75A]",
            icon: FiServer,
          },
          {
            title: "AI 벡터 데이터베이스",
            status: "동기화 완료",
            color: "text-[#03C75A]",
            icon: FiCpu,
          },
          {
            title: "실시간 알림 세션 (SSE)",
            status: "대기 상태",
            color: "text-[#FF9500]",
            icon: FiActivity,
          },
        ].map((sys, idx) => {
          const Icon = sys.icon;
          return (
            <div
              key={idx}
              className="p-5 bg-white dark:bg-[#1f2028] border border-[#E4E8EB] dark:border-slate-800 rounded-lg flex items-center gap-4"
            >
              <div className="p-3 bg-[#F5F6F7] dark:bg-[#16171d] rounded-lg">
                <Icon className="h-6 w-6 text-[#666666] dark:text-slate-400" />
              </div>
              <div>
                <p className="text-xs font-bold text-slate-400 dark:text-slate-500">{sys.title}</p>
                <p className={`text-sm font-extrabold mt-0.5 ${sys.color}`}>{sys.status}</p>
              </div>
            </div>
          );
        })}
      </div>

      {/* 3. 퀵 바로가기 2구 카드 그리드 */}
      <h2 className="text-base font-bold text-[#1E1E1E] dark:text-slate-100 mb-4">서비스 바로가기</h2>
      <div className="grid gap-6 sm:grid-cols-2">
        <button
          onClick={() => setActiveTab("chat")}
          className="p-6 bg-white dark:bg-[#1f2028] border border-[#E4E8EB] dark:border-slate-800 rounded-lg text-left hover:border-[#03C75A] transition-all cursor-pointer group"
        >
          <FiMessageSquare className="h-8 w-8 text-[#03C75A] mb-3" />
          <h3 className="text-base font-bold text-[#1E1E1E] dark:text-white group-hover:text-[#03C75A] transition-colors">
            AI 지식 챗봇 ➔
          </h3>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            등록된 문서를 조회하고 Kubernetes Pod 배포, 재시작 및 스케일 아웃 명령을 수행합니다.
          </p>
        </button>

        <button
          onClick={() => setActiveTab("dashboard")}
          className="p-6 bg-white dark:bg-[#1f2028] border border-[#E4E8EB] dark:border-slate-800 rounded-lg text-left hover:border-[#03C75A] transition-all cursor-pointer group"
        >
          <FiGrid className="h-8 w-8 text-[#03C75A] mb-3" />
          <h3 className="text-base font-bold text-[#1E1E1E] dark:text-white group-hover:text-[#03C75A] transition-colors">
            지식 대시보드 ➔
          </h3>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            지식 문서를 수집하고 데이터 검수 상태 및 실시간 벡터라이징 가공 과정을 모니터링합니다.
          </p>
        </button>
      </div>
    </div>
  );
};
