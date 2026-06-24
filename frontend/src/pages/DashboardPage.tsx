import React, { useState } from "react";
import { FiPlus, FiRefreshCw } from "react-icons/fi";
import { CreateKnowledgeModal } from "../components/CreateKnowledgeModal";
import {
  useKnowledgeList,
  useCreateKnowledge,
  useDeleteKnowledge,
} from "../features/knowledge/api";

export const DashboardPage: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  // React Query 훅 주입
  const {
    data: sources = [],
    isLoading,
    error,
    refetch,
    isRefetching,
  } = useKnowledgeList();
  const createMutation = useCreateKnowledge();
  const deleteMutation = useDeleteKnowledge();

  // 지식 라이프사이클에 따른 인덱스 상태 배지 처리
  const getStatusBadge = (status: string) => {
    const badgeStyles: Record<string, string> = {
      PUBLISHED: "bg-emerald-50 text-emerald-700 ring-emerald-600/10",
      VERIFYING: "bg-amber-50 text-amber-700 ring-amber-600/10 animate-pulse",
      DRAFT: "bg-slate-50 text-slate-700 ring-slate-600/10",
      REVIEW_READY: "bg-indigo-50 text-indigo-700 ring-indigo-600/10",
      APPROVED: "bg-sky-50 text-sky-700 ring-sky-600/10",
      FAILED: "bg-rose-50 text-rose-700 ring-rose-600/10",
    };
    return (
      <span
        className={`inline-flex items-center rounded-md px-2.5 py-1 text-xs font-semibold ring-1 ring-inset ${badgeStyles[status] || "bg-slate-50 text-slate-600"}`}
      >
        {status === "PUBLISHED"
          ? "완료"
          : status === "VERIFYING"
            ? "처리 중"
            : status}
      </span>
    );
  };

  const handleAddSource = async (newRequest: any) => {
    try {
      await createMutation.mutateAsync({
        title: newRequest.title,
        rawContent: newRequest.rawContent,
        tags: [],
        sourceUrls: [],
      });
      setIsModalOpen(false);
    } catch (err) {
      alert("지식 등록에 실패했습니다.");
    }
  };

  const handleDeleteSource = async (id: number) => {
    if (!window.confirm("정말 이 지식을 삭제하시겠습니까?")) return;
    try {
      await deleteMutation.mutateAsync(id);
    } catch (err) {
      alert("지식 삭제에 실패했습니다.");
    }
  };

  if (error) {
    return (
      <div className="p-8 text-rose-600 font-semibold">
        데이터 연결 오류가 발생했습니다. 서버가 실행 중인지 확인하세요.
      </div>
    );
  }

  return (
    <div className="p-8">
      {/* 상단 헤더 영역 */}
      <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between border-b border-slate-200 pb-6 mb-8">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-800">
            지식 대시보드
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            지식 소스를 수집하고 AI 벡터 디비 상태를 모니터링합니다.
          </p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="flex items-center gap-2 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-sky-500/20 hover:from-sky-400 hover:to-indigo-500 transition-all active:scale-95"
        >
          <FiPlus className="h-5 w-5" />새 지식 등록
        </button>
      </div>

      {/* 로딩 표시 또는 카드 그리드 */}
      {isLoading ? (
        <div className="text-slate-400 text-sm font-medium animate-pulse mb-8">
          데이터 동기화 중...
        </div>
      ) : (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 mb-8">
          {[
            {
              label: "전체 지식 소스",
              count: sources.length,
              desc: "등록된 지식 자료 수",
              color: "from-sky-500/5",
            },
            {
              label: "완료 및 연동됨",
              count: sources.filter((s) => s.status === "PUBLISHED").length,
              desc: "검색 가능한 지식",
              color: "from-emerald-500/5",
            },
            {
              label: "처리 대기 중",
              count: sources.filter((s) => s.status === "VERIFYING").length,
              desc: "벡터라이징 대기",
              color: "from-amber-500/5",
            },
          ].map((stat, idx) => (
            <div
              key={idx}
              className={`relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-6 shadow-sm bg-gradient-to-tr ${stat.color} to-transparent`}
            >
              <p className="text-sm font-semibold text-slate-500">
                {stat.label}
              </p>
              <p className="mt-2 text-3xl font-bold tracking-tight text-slate-800">
                {stat.count}
              </p>
              <p className="mt-1 text-xs text-slate-400">{stat.desc}</p>
            </div>
          ))}
        </div>
      )}

      {/* 테이블 영역 */}
      <div className="rounded-2xl border border-slate-200 bg-white shadow-sm overflow-hidden">
        <div className="flex items-center justify-between border-b border-slate-100 p-6">
          <h2 className="text-lg font-bold text-slate-800">등록된 지식 목록</h2>
          <button
            onClick={() => refetch()}
            disabled={isRefetching}
            className="flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-slate-800 disabled:opacity-50 transition-colors"
          >
            <FiRefreshCw
              className={`h-4 w-4 ${isRefetching ? "animate-spin" : ""}`}
            />
            새로고침
          </button>
        </div>
        <div className="overflow-x-auto">
          {isLoading ? (
            <div className="p-10 text-center text-slate-400">
              목록 구성 중...
            </div>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-100 bg-slate-50 text-xs font-bold text-slate-500 uppercase tracking-wider">
                  <th className="py-4 px-6">지식 제목</th>
                  <th className="py-4 px-6">벡터 상태</th>
                  <th className="py-4 px-6 text-right">관리</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-sm text-slate-600">
                {sources.map((source) => (
                  <tr
                    key={source.id}
                    className="hover:bg-slate-50/50 transition-colors"
                  >
                    <td className="py-4 px-6 font-semibold text-slate-800">
                      {source.title}
                    </td>
                    <td className="py-4 px-6">
                      {getStatusBadge(source.status)}
                    </td>
                    <td className="py-4 px-6 text-right">
                      <button className="text-xs font-bold text-sky-600 hover:text-sky-800 mr-4 transition-colors">
                        상세
                      </button>
                      <button
                        onClick={() => handleDeleteSource(source.id)}
                        disabled={deleteMutation.isPending}
                        className="text-xs font-bold text-rose-600 hover:text-rose-800 disabled:opacity-50 transition-colors"
                      >
                        삭제
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      <CreateKnowledgeModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onAdd={handleAddSource}
      />
    </div>
  );
};
