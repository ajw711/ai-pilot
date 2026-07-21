import React, { useState } from "react";
import { FiPlus, FiRefreshCw, FiSearch, FiChevronLeft, FiChevronRight } from "react-icons/fi";
import { CreateKnowledgeModal } from "../components/CreateKnowledgeModal";
import { KnowledgeDetailModal } from "../components/KnowledgeDetailModal";
import {
  useKnowledgeList,
  useCreateKnowledge,
  useDeleteKnowledge,
} from "../features/knowledge/api";

export const DashboardPage: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedKnowledgeId, setSelectedKnowledgeId] = useState<number | null>(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);

  // Search & Pagination States
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

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

  // Search filtering logic
  const filteredSources = sources.filter((source) =>
    source.title.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // Pagination logic
  const totalPages = Math.max(1, Math.ceil(filteredSources.length / itemsPerPage));
  const activePage = Math.min(currentPage, totalPages);
  const paginatedSources = filteredSources.slice(
    (activePage - 1) * itemsPerPage,
    activePage * itemsPerPage
  );

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(e.target.value);
    setCurrentPage(1); // Reset to first page on search
  };

  // 지식 라이프사이클에 따른 인덱스 상태 배지 처리
  // 지식 라이프사이클에 따른 인덱스 상태 배지 처리
  const getStatusBadge = (status: string) => {
    const badgeStyles: Record<string, string> = {
      PUBLISHED: "bg-emerald-50 dark:bg-emerald-950/30 text-emerald-700 dark:text-emerald-400 ring-emerald-600/10 dark:ring-emerald-500/20 font-bold",
      VERIFYING: "bg-amber-50 dark:bg-amber-950/30 text-amber-700 dark:text-amber-400 ring-amber-600/10 dark:ring-amber-500/20 animate-pulse",
      FORMATTING: "bg-amber-50 dark:bg-amber-950/30 text-amber-700 dark:text-amber-400 ring-amber-600/10 dark:ring-amber-500/20 animate-pulse",
      DRAFT: "bg-slate-50 dark:bg-slate-800 text-slate-700 dark:text-slate-300 ring-slate-600/10 dark:ring-slate-500/25",
      REVIEW_READY: "bg-indigo-50 dark:bg-indigo-950/30 text-indigo-700 dark:text-indigo-400 ring-indigo-600/10 dark:ring-indigo-500/20 font-bold",
      REVIEW_APPROVED: "bg-sky-50 dark:bg-sky-950/30 text-sky-700 dark:text-sky-400 ring-sky-600/10 dark:ring-sky-500/20",
      NOTION_PUBLISHING: "bg-sky-50 dark:bg-sky-950/30 text-sky-700 dark:text-sky-400 ring-sky-600/10 dark:ring-sky-500/20 animate-pulse",
      VECTOR_INDEXING: "bg-sky-50 dark:bg-sky-950/30 text-sky-700 dark:text-sky-400 ring-sky-600/10 dark:ring-sky-500/20 animate-pulse",
      FAILED_AT_VERIFYING: "bg-rose-50 dark:bg-rose-950/30 text-rose-700 dark:text-rose-400 ring-rose-600/10 dark:ring-rose-500/20",
      FAILED_AT_FORMATTING: "bg-rose-50 dark:bg-rose-950/30 text-rose-700 dark:text-rose-400 ring-rose-600/10 dark:ring-rose-500/20",
      FAILED_AT_NOTION_PUBLISH: "bg-rose-50 dark:bg-rose-950/30 text-rose-700 dark:text-rose-400 ring-rose-600/10 dark:ring-rose-500/20",
      FAILED_AT_VECTOR_INDEX: "bg-rose-50 dark:bg-rose-950/30 text-rose-700 dark:text-rose-400 ring-rose-600/10 dark:ring-rose-500/20",
      FAILED: "bg-rose-50 dark:bg-rose-950/30 text-rose-700 dark:text-rose-400 ring-rose-600/10 dark:ring-rose-500/20",
    };

    const statusLabels: Record<string, string> = {
      PUBLISHED: "완료",
      VERIFYING: "AI 검수 중",
      FORMATTING: "AI 가공 중",
      DRAFT: "초안",
      REVIEW_READY: "검토 대기",
      REVIEW_APPROVED: "승인됨",
      NOTION_PUBLISHING: "노션 발행 중",
      VECTOR_INDEXING: "인덱싱 중",
      FAILED_AT_VERIFYING: "검수 실패",
      FAILED_AT_FORMATTING: "가공 실패",
      FAILED_AT_NOTION_PUBLISH: "노션 발행 실패",
      FAILED_AT_VECTOR_INDEX: "인덱싱 실패",
      FAILED: "실패",
    };

    return (
      <span
        className={`inline-flex items-center rounded-md px-2.5 py-1 text-xs font-semibold ring-1 ring-inset ${badgeStyles[status] || "bg-slate-50 dark:bg-slate-800 text-slate-600 dark:text-slate-300"}`}
      >
        {statusLabels[status] || status}
      </span>
    );
  };

  const handleAddSource = async (newRequest: any) => {
    try {
      await createMutation.mutateAsync({
        title: newRequest.title,
        rawContent: newRequest.rawContent,
        tags: [],
        sourceUrls: newRequest.sourceUrls || [],
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
      <div className="p-8 text-rose-600 dark:text-rose-400 font-semibold bg-slate-50 dark:bg-slate-950 min-h-screen">
        데이터 연결 오류가 발생했습니다. 서버가 실행 중인지 확인하세요.
      </div>
    );
  }

  return (
    <div className="p-8 bg-slate-50 dark:bg-slate-950 min-h-screen transition-colors duration-200">
      {/* 상단 헤더 영역 */}
      <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between border-b border-slate-300 dark:border-slate-600 pb-6 mb-8">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-800 dark:text-slate-100">
            지식 대시보드
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            지식 소스를 수집하고 AI 벡터 디비 상태를 모니터링합니다.
          </p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="flex items-center gap-2 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-sky-500/20 hover:from-sky-400 hover:to-indigo-500 transition-all active:scale-95 cursor-pointer"
        >
          <FiPlus className="h-5 w-5" />새 지식 등록
        </button>
      </div>

      {/* 로딩 표시 또는 카드 그리드 */}
      {isLoading ? (
        <div className="text-slate-400 dark:text-slate-500 text-sm font-medium animate-pulse mb-8">
          데이터 동기화 중...
        </div>
      ) : (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 mb-8">
          {[
            {
              label: "전체 지식 소스",
              count: sources.length,
              desc: "등록된 지식 자료 수",
              color: "from-sky-500/5 dark:from-sky-500/10",
            },
            {
              label: "완료 및 연동됨",
              count: sources.filter((s) => s.status === "PUBLISHED").length,
              desc: "검색 가능한 지식",
              color: "from-emerald-500/5 dark:from-emerald-500/10",
            },
            {
              label: "처리 대기 중",
              count: sources.filter((s) => s.status === "VERIFYING").length,
              desc: "벡터라이징 대기",
              color: "from-amber-500/5 dark:from-amber-500/10",
            },
          ].map((stat, idx) => (
            <div
              key={idx}
              className={`relative overflow-hidden rounded-2xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 p-6 shadow-sm bg-gradient-to-tr ${stat.color} to-transparent`}
            >
              <p className="text-sm font-semibold text-slate-500 dark:text-slate-400">
                {stat.label}
              </p>
              <p className="mt-2 text-3xl font-bold tracking-tight text-slate-800 dark:text-slate-100">
                {stat.count}
              </p>
              <p className="mt-1 text-xs text-slate-400 dark:text-slate-500">{stat.desc}</p>
            </div>
          ))}
        </div>
      )}

      {/* 테이블 영역 */}
      <div className="rounded-2xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 shadow-sm overflow-hidden">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between border-b border-slate-300 dark:border-slate-600 p-6 gap-4">
          <div>
            <h2 className="text-lg font-bold text-slate-800 dark:text-slate-100">등록된 지식 목록</h2>
            <p className="text-xs text-slate-400 dark:text-slate-500 mt-0.5">
              {filteredSources.length !== sources.length 
                ? `검색 결과: ${filteredSources.length}개 / 전체: ${sources.length}개`
                : `전체: ${sources.length}개`}
            </p>
          </div>
          
          <div className="flex items-center gap-3 flex-1 sm:max-w-md justify-end">
            <div className="relative w-full">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400 dark:text-slate-500">
                <FiSearch className="h-4 w-4" />
              </span>
              <input
                type="text"
                placeholder="지식 제목으로 검색..."
                value={searchQuery}
                onChange={handleSearchChange}
                className="w-full pl-9 pr-4 py-2 border border-slate-300 dark:border-slate-600 rounded-xl text-sm focus:outline-none focus:ring-1 focus:ring-sky-500 focus:border-sky-500 transition-all text-slate-700 dark:text-slate-200 bg-slate-50 dark:bg-slate-950 hover:bg-slate-50/50"
              />
            </div>
            <button
              onClick={() => refetch()}
              disabled={isRefetching}
              className="flex items-center gap-1.5 text-xs font-semibold text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200 disabled:opacity-50 transition-colors border border-slate-300 dark:border-slate-600 rounded-xl px-3 py-2 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800 flex-shrink-0 cursor-pointer"
            >
              <FiRefreshCw
                className={`h-3.5 w-3.5 ${isRefetching ? "animate-spin" : ""}`}
              />
              새로고침
            </button>
          </div>
        </div>
        
        <div className="overflow-x-auto">
          {isLoading ? (
            <div className="p-10 text-center text-slate-400 dark:text-slate-500">
              목록 구성 중...
            </div>
          ) : filteredSources.length === 0 ? (
            <div className="flex flex-col items-center justify-center p-12 text-center text-slate-400 dark:text-slate-500 bg-slate-50/30 dark:bg-slate-950/20">
              <FiSearch className="h-10 w-10 text-slate-300 dark:text-slate-600 mb-2" />
              <p className="font-semibold text-slate-600 dark:text-slate-300">검색 조건에 맞는 지식이 없습니다.</p>
              <p className="text-xs text-slate-400 dark:text-slate-500 mt-1">검색어를 다르게 입력하거나 새 지식을 등록해 보세요.</p>
            </div>
          ) : (
            <>
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-300 dark:border-slate-600 bg-slate-50 dark:bg-slate-950/40 text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                    <th className="py-4 px-6">지식 제목</th>
                    <th className="py-4 px-6">벡터 상태</th>
                    <th className="py-4 px-6 text-right">관리</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-300 dark:divide-slate-600 text-sm text-slate-600 dark:text-slate-300">
                  {paginatedSources.map((source) => (
                    <tr
                      key={source.id}
                      className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition-colors"
                    >
                      <td className="py-4 px-6 font-semibold text-slate-800 dark:text-slate-200">
                        {source.title}
                      </td>
                      <td className="py-4 px-6">
                        {getStatusBadge(source.status)}
                      </td>
                      <td className="py-4 px-6 text-right">
                        <button 
                          onClick={() => {
                            setSelectedKnowledgeId(source.id);
                            setIsDetailModalOpen(true);
                          }}
                          className="text-xs font-bold text-sky-600 dark:text-sky-400 hover:text-sky-800 dark:hover:text-sky-300 mr-4 transition-colors cursor-pointer"
                        >
                          상세
                        </button>
                        <button
                          onClick={() => handleDeleteSource(source.id)}
                          disabled={deleteMutation.isPending}
                          className="text-xs font-bold text-rose-600 dark:text-rose-400 hover:text-rose-800 dark:hover:text-rose-300 disabled:opacity-50 transition-colors cursor-pointer"
                        >
                          삭제
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {/* 페이징 네비게이션 영역 */}
              {filteredSources.length > 0 && (
                <div className="flex flex-col sm:flex-row items-center justify-between border-t border-slate-300 dark:border-slate-600 bg-slate-50/50 dark:bg-slate-950/20 px-6 py-4 gap-4">
                  <div className="text-xs text-slate-500 dark:text-slate-400">
                    전체 <strong>{filteredSources.length}</strong>개 중 <strong>{(activePage - 1) * itemsPerPage + 1}</strong> ~ <strong>{Math.min(activePage * itemsPerPage, filteredSources.length)}</strong> 표시 중
                  </div>
                  
                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => setCurrentPage(1)}
                      disabled={activePage === 1}
                      className="p-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-40 disabled:hover:bg-white dark:disabled:hover:bg-slate-900 text-slate-500 dark:text-slate-400 transition-colors cursor-pointer disabled:cursor-not-allowed"
                      title="첫 페이지"
                    >
                      <FiChevronLeft className="h-4 w-4 stroke-[3px]" />
                    </button>
                    <button
                      onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
                      disabled={activePage === 1}
                      className="px-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-40 disabled:hover:bg-white dark:disabled:hover:bg-slate-900 text-xs font-semibold text-slate-600 dark:text-slate-300 transition-colors cursor-pointer disabled:cursor-not-allowed"
                    >
                      이전
                    </button>
                    
                    {/* 페이지 번호 목록 */}
                    {Array.from({ length: totalPages }, (_, i) => i + 1)
                      .filter((page) => Math.abs(page - activePage) <= 2)
                      .map((page) => (
                        <button
                          key={page}
                          onClick={() => setCurrentPage(page)}
                          className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                            page === activePage
                              ? "bg-gradient-to-r from-sky-500 to-indigo-600 text-white shadow-sm"
                              : "border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
                          }`}
                        >
                          {page}
                        </button>
                      ))}
                    
                    <button
                      onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))}
                      disabled={activePage === totalPages}
                      className="px-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-40 disabled:hover:bg-white dark:disabled:hover:bg-slate-900 text-xs font-semibold text-slate-600 dark:text-slate-300 transition-colors cursor-pointer disabled:cursor-not-allowed"
                    >
                      다음
                    </button>
                    <button
                      onClick={() => setCurrentPage(totalPages)}
                      disabled={activePage === totalPages}
                      className="p-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-40 disabled:hover:bg-white dark:disabled:hover:bg-slate-900 text-slate-500 dark:text-slate-400 transition-colors cursor-pointer disabled:cursor-not-allowed"
                      title="마지막 페이지"
                    >
                      <FiChevronRight className="h-4 w-4 stroke-[3px]" />
                    </button>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>

      <CreateKnowledgeModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onAdd={handleAddSource}
      />

      <KnowledgeDetailModal
        isOpen={isDetailModalOpen}
        onClose={() => {
          setIsDetailModalOpen(false);
          setSelectedKnowledgeId(null);
        }}
        knowledgeId={selectedKnowledgeId}
      />
    </div>
  );
};
