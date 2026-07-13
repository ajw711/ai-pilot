import React, { useState, useEffect } from "react";
import { 
  useKnowledgeDetail, 
  useApproveKnowledge 
} from "../features/knowledge/api";
import { 
  FiAlertTriangle, 
  FiCheckCircle, 
  FiInfo, 
  FiCpu, 
  FiFileText, 
  FiChevronRight, 
  FiEdit3,
  FiEye
} from "react-icons/fi";
import { MarkdownRenderer } from "./MarkdownRenderer";

interface KnowledgeDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  knowledgeId: number | null;
}

interface Issue {
  severity: "CRITICAL" | "WARNING" | "SUGGESTION";
  targetText: string;
  message: string;
}

interface VerificationReport {
  issues?: Issue[];
}

export const KnowledgeDetailModal: React.FC<KnowledgeDetailModalProps> = ({
  isOpen,
  onClose,
  knowledgeId,
}) => {
  const { data: detail, isLoading, error } = useKnowledgeDetail(knowledgeId);
  const approveMutation = useApproveKnowledge();

  const [editedContent, setEditedContent] = useState("");
  const [activeTab, setActiveTab] = useState<"report" | "compare">("compare");
  const [isSuccess, setIsSuccess] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [previewMode, setPreviewMode] = useState<"edit" | "preview">("edit");

  // Initialize status on modal open
  useEffect(() => {
    if (isOpen) {
      setIsSuccess(false);
      setErrorMsg(null);
      setPreviewMode("edit");
    }
  }, [isOpen]);

  // Load formatted content when detail is loaded
  useEffect(() => {
    if (detail) {
      setEditedContent(detail.formattedContent || "");
    }
  }, [detail]);

  if (!isOpen || knowledgeId === null) return null;

  // Status rendering helpers
  const getStatusInfo = (status: string) => {
    const statusMap: Record<string, { label: string; style: string }> = {
      DRAFT: { label: "초안", style: "bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 ring-slate-600/10 dark:ring-slate-500/25" },
      VERIFYING: { label: "AI 검수 진행 중", style: "bg-amber-100 dark:bg-amber-950/30 text-amber-700 dark:text-amber-400 ring-amber-600/10 dark:ring-amber-500/20 animate-pulse" },
      FORMATTING: { label: "AI 포맷팅 진행 중", style: "bg-amber-100 dark:bg-amber-950/30 text-amber-700 dark:text-amber-400 ring-amber-600/10 dark:ring-amber-500/20 animate-pulse" },
      REVIEW_READY: { label: "검토 대기 (승인 가능)", style: "bg-indigo-100 dark:bg-indigo-950/30 text-indigo-700 dark:text-indigo-400 ring-indigo-600/10 dark:ring-indigo-500/20 font-bold" },
      REVIEW_APPROVED: { label: "검토 완료/승인됨", style: "bg-sky-100 dark:bg-sky-950/30 text-sky-700 dark:text-sky-400 ring-sky-600/10 dark:ring-sky-500/20" },
      NOTION_PUBLISHING: { label: "노션 발행 중", style: "bg-sky-100 dark:bg-sky-950/30 text-sky-700 dark:text-sky-400 ring-sky-600/10 dark:ring-sky-500/20 animate-pulse" },
      VECTOR_INDEXING: { label: "벡터 인덱싱 중", style: "bg-sky-100 dark:bg-sky-950/30 text-sky-700 dark:text-sky-400 ring-sky-600/10 dark:ring-sky-500/20 animate-pulse" },
      PUBLISHED: { label: "발행 완료 (검색 가능)", style: "bg-emerald-100 dark:bg-emerald-950/30 text-emerald-800 dark:text-emerald-400 ring-emerald-600/20 dark:ring-emerald-500/20 font-bold" },
      FAILED_AT_VERIFYING: { label: "검수 실패", style: "bg-rose-100 dark:bg-rose-950/30 text-rose-700 dark:text-rose-400 ring-rose-600/10 dark:ring-rose-500/20" },
      FAILED_AT_NOTION_PUBLISH: { label: "노션 발행 실패", style: "bg-rose-100 dark:bg-rose-950/30 text-rose-700 dark:text-rose-400 ring-rose-600/10 dark:ring-rose-500/20" },
      FAILED_AT_VECTOR_INDEX: { label: "벡터 저장 실패", style: "bg-rose-100 dark:bg-rose-950/30 text-rose-700 dark:text-rose-400 ring-rose-600/10 dark:ring-rose-500/20" },
      FAILED: { label: "실패", style: "bg-rose-100 dark:bg-rose-950/30 text-rose-700 dark:text-rose-400 ring-rose-600/10 dark:ring-rose-500/20" },
    };
    return statusMap[status] || { label: status, style: "bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300" };
  };

  // Parsing JSON verification report
  let parsedReport: VerificationReport = {};
  if (detail?.verificationReport) {
    try {
      parsedReport = JSON.parse(detail.verificationReport);
    } catch (e) {
      console.error("Failed to parse verification report", e);
    }
  }
  const issues = parsedReport.issues || [];

  const handleApprove = async () => {
    if (!detail) return;
    const isApprovable = detail.status === "REVIEW_READY" || detail.status === "FAILED_AT_NOTION_PUBLISH" || detail.status === "FAILED_AT_VECTOR_INDEX";
    if (!isApprovable) {
      setErrorMsg("승인 또는 재발행 가능한 상태가 아닙니다.");
      return;
    }
    if (!editedContent.trim()) {
      setErrorMsg("최종 가공 본문 내용을 채워주세요.");
      return;
    }

    try {
      setErrorMsg(null);
      await approveMutation.mutateAsync({
        knowledgeId: detail.id,
        finalFormattedContent: editedContent,
      });
      setIsSuccess(true);
    } catch (err) {
      setErrorMsg("승인 처리에 실패했습니다. 서버 로그를 확인해 주세요.");
    }
  };

  const statusInfo = detail ? getStatusInfo(detail.status) : { label: "", style: "" };

  if (isSuccess) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center p-6 bg-slate-950/60 backdrop-blur-sm animate-fadeIn">
        <div className="w-full max-w-md rounded-2xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 p-8 shadow-2xl flex flex-col items-center text-center animate-scaleUp">
          <div className="w-16 h-16 bg-emerald-50 dark:bg-emerald-950/30 rounded-full flex items-center justify-center text-emerald-500 mb-4 animate-pulse">
            <FiCheckCircle className="w-10 h-10" />
          </div>
          <h3 className="text-xl font-bold text-slate-800 dark:text-slate-100 mb-2">최종 승인 완료</h3>
          <p className="text-sm text-slate-500 dark:text-slate-400 mb-6 leading-relaxed">
            노션 페이지 발행 및 AI 벡터 데이터베이스 저장이 시작되었습니다.<br />
            잠시 후 대시보드에서 완료 상태를 확인하실 수 있습니다.
          </p>
          <button
            onClick={() => {
              setIsSuccess(false);
              onClose();
            }}
            className="w-full py-3.5 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 font-bold text-white shadow-lg shadow-sky-500/20 hover:from-sky-400 hover:to-indigo-500 transition-all active:scale-95 cursor-pointer"
          >
            확인
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-6 bg-slate-950/60 backdrop-blur-sm animate-fadeIn">
      <div className="w-full max-w-6xl h-[85vh] rounded-2xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 shadow-2xl flex flex-col overflow-hidden transition-colors duration-200">
        
        {/* 모달 헤더 */}
        <div className="bg-slate-50 dark:bg-slate-950/30 border-b border-slate-300 dark:border-slate-600 p-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          {isLoading ? (
            <div className="h-10 w-48 bg-slate-200 dark:bg-slate-800 rounded animate-pulse" />
          ) : detail ? (
            <div>
              <div className="flex items-center gap-3">
                <h3 className="text-xl font-bold text-slate-800 dark:text-slate-100">{detail.title}</h3>
                <span className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold ring-1 ring-inset ${statusInfo.style}`}>
                  {statusInfo.label}
                </span>
                {detail.verificationScore !== null && (
                  <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-bold ${
                    detail.verificationScore >= 80 
                      ? "bg-emerald-50 dark:bg-emerald-950/30 text-emerald-700 dark:text-emerald-400 ring-emerald-600/10 dark:ring-emerald-500/20" 
                      : detail.verificationScore >= 50 
                        ? "bg-amber-50 dark:bg-amber-950/30 text-amber-700 dark:text-amber-400 ring-amber-600/10 dark:ring-amber-500/20"
                        : "bg-rose-50 dark:bg-rose-950/30 text-rose-700 dark:text-rose-400 ring-rose-600/10 dark:ring-rose-500/20"
                  }`}>
                    AI 신뢰 등급: {detail.verificationScore}점
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">ID: {detail.id} | 원본 지식 문서의 AI 분석 검수 리포트를 검토하고 승인하세요.</p>
            </div>
          ) : (
            <div className="text-slate-500 dark:text-slate-400">데이터가 없습니다.</div>
          )}

          <div className="flex items-center gap-2">
            <button
              onClick={onClose}
              className="rounded-xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 px-4 py-2 text-sm font-semibold transition-all cursor-pointer"
            >
              닫기
            </button>
          </div>
        </div>

        {/* 로딩 / 에러 / 상세 화면 분기 */}
        {isLoading ? (
          <div className="flex-1 flex items-center justify-center text-slate-400 dark:text-slate-500 bg-white dark:bg-slate-900">
            <div className="flex flex-col items-center gap-2">
              <div className="w-10 h-10 border-4 border-sky-600 border-t-transparent rounded-full animate-spin" />
              <p className="text-sm font-medium">지식 정보를 불러오는 중...</p>
            </div>
          </div>
        ) : error ? (
          <div className="flex-1 flex items-center justify-center text-rose-600 dark:text-rose-400 font-semibold p-8 bg-white dark:bg-slate-900">
            지식을 조회하는 도중 오류가 발생했습니다.
          </div>
        ) : detail ? (
          <div className="flex-1 flex flex-col overflow-hidden">
            {/* 에러 메시지 배너 */}
            {errorMsg && (
              <div className="mx-6 mt-4 p-4 rounded-xl bg-rose-50 dark:bg-rose-950/30 border border-rose-300 dark:border-rose-800 text-rose-800 dark:text-rose-200 flex items-center justify-between text-sm shadow-sm animate-fadeIn">
                <div className="flex items-center gap-2">
                  <FiAlertTriangle className="h-5 w-5 text-rose-600 flex-shrink-0" />
                  <span>{errorMsg}</span>
                </div>
                <button 
                  onClick={() => setErrorMsg(null)}
                  className="text-xs font-bold underline hover:text-rose-900 ml-4 flex-shrink-0"
                >
                  닫기
                </button>
              </div>
            )}

            {/* 탭 네비게이션 */}
            <div className="flex border-b border-slate-300 dark:border-slate-600 px-6 bg-slate-50 dark:bg-slate-950/20">
              <button
                onClick={() => setActiveTab("compare")}
                className={`py-3 px-4 text-sm font-semibold border-b-2 transition-all flex items-center gap-2 cursor-pointer ${
                  activeTab === "compare"
                    ? "border-sky-600 text-sky-600 dark:text-sky-400"
                    : "border-transparent text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200"
                }`}
              >
                <FiFileText className="h-4.5 w-4.5" />
                원문 및 포맷팅 대조/수정
              </button>
              <button
                onClick={() => setActiveTab("report")}
                className={`py-3 px-4 text-sm font-semibold border-b-2 transition-all flex items-center gap-2 cursor-pointer ${
                  activeTab === "report"
                    ? "border-sky-600 text-sky-600 dark:text-sky-400"
                    : "border-transparent text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200"
                }`}
              >
                <FiCpu className="h-4.5 w-4.5" />
                AI 팩트 검수 리포트 ({issues.length}건)
              </button>
            </div>

            {/* 탭 컨텐츠 */}
            <div className="flex-1 overflow-hidden p-6 flex flex-col bg-white dark:bg-slate-900">
              {activeTab === "compare" ? (
                <div className="flex-1 grid grid-cols-1 md:grid-cols-2 gap-6 overflow-hidden">
                  {/* Left: Raw Content */}
                  <div className="flex flex-col border border-slate-300 dark:border-slate-600 rounded-xl overflow-hidden bg-slate-50 dark:bg-slate-950/20">
                    <div className="bg-slate-100 dark:bg-slate-950/50 px-4 py-2.5 border-b border-slate-300 dark:border-slate-600 text-xs font-bold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                      원본 내용 (Raw Content)
                    </div>
                    <div className="flex-1 p-4 overflow-y-auto text-sm text-slate-700 dark:text-slate-300 font-sans whitespace-pre-wrap leading-relaxed">
                      {detail.rawContent}
                    </div>
                  </div>

                  {/* Right: Formatted Markdown (Editable & Preview) */}
                  <div className="flex flex-col border border-slate-300 dark:border-slate-600 rounded-xl overflow-hidden bg-white dark:bg-slate-900 focus-within:border-sky-500 focus-within:ring-1 focus-within:ring-sky-500 transition-all">
                    <div className="bg-slate-100 dark:bg-slate-950/50 px-4 py-2 flex items-center justify-between border-b border-slate-300 dark:border-slate-600 text-xs font-bold text-slate-600 dark:text-slate-400 uppercase tracking-wider">
                      <span className="flex items-center gap-1.5">
                        {previewMode === "edit" ? <FiEdit3 className="h-4 w-4" /> : <FiEye className="h-4 w-4" />}
                        {previewMode === "edit" ? "최종 가공 본문 (Markdown - 편집 가능)" : "최종 가공 본문 (미리보기)"}
                      </span>
                      
                      <div className="flex items-center gap-1 bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 rounded-lg p-0.5">
                        <button
                          type="button"
                          onClick={() => setPreviewMode("edit")}
                          className={`px-2 py-1 rounded-md text-[10px] font-extrabold transition-all cursor-pointer ${
                            previewMode === "edit"
                              ? "bg-slate-100 dark:bg-slate-750 text-slate-800 dark:text-slate-100 shadow-sm"
                              : "text-slate-400 hover:text-slate-600 dark:hover:text-slate-300"
                          }`}
                        >
                          편집
                        </button>
                        <button
                          type="button"
                          onClick={() => setPreviewMode("preview")}
                          className={`px-2 py-1 rounded-md text-[10px] font-extrabold transition-all cursor-pointer ${
                            previewMode === "preview"
                              ? "bg-slate-100 dark:bg-slate-750 text-slate-800 dark:text-slate-100 shadow-sm"
                              : "text-slate-400 hover:text-slate-600 dark:hover:text-slate-300"
                          }`}
                        >
                          미리보기
                        </button>
                      </div>
                    </div>
                    
                    {previewMode === "edit" ? (
                      <textarea
                        value={editedContent}
                        onChange={(e) => setEditedContent(e.target.value)}
                        disabled={detail.status !== "REVIEW_READY"}
                        placeholder="AI가 가공한 포맷팅 결과가 여기에 나타납니다. 직접 편집하여 올바른 마크다운으로 승인할 수도 있습니다."
                        className="flex-1 p-4 overflow-y-auto text-sm text-slate-800 dark:text-slate-100 font-mono leading-relaxed outline-none resize-none bg-white dark:bg-slate-900 disabled:bg-slate-50 dark:disabled:bg-slate-950/40 disabled:text-slate-500 dark:disabled:text-slate-400"
                      />
                    ) : (
                      <div className="flex-1 p-5 overflow-y-auto bg-slate-50/50 dark:bg-slate-950/10 min-h-0 select-text">
                        <MarkdownRenderer content={editedContent} />
                      </div>
                    )}
                  </div>
                </div>
              ) : (
                /* Report Panel */
                <div className="flex-1 overflow-y-auto border border-slate-300 dark:border-slate-600 rounded-xl bg-slate-50 dark:bg-slate-950/20 p-6 space-y-4">
                  <h4 className="text-sm font-bold text-slate-800 dark:text-slate-200 flex items-center gap-2 mb-2">
                    <FiCpu className="h-5 w-5 text-indigo-600" />
                    AI 기술 문서 팩트체킹 분석 결과
                  </h4>
                  {issues.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-12 text-slate-400 dark:text-slate-500">
                      <FiCheckCircle className="h-12 w-12 text-emerald-500 mb-2" />
                      <p className="font-semibold text-slate-700 dark:text-slate-300">팩트 및 개념적 오류가 발견되지 않았습니다.</p>
                      <p className="text-xs text-slate-400 dark:text-slate-500 mt-1">즉시 승인하여 발행하는 것을 권장합니다.</p>
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {issues.map((issue, idx) => {
                        const isCritical = issue.severity === "CRITICAL";
                        const isWarning = issue.severity === "WARNING";

                        return (
                          <div 
                            key={idx} 
                            className={`p-4 rounded-xl border flex gap-3.5 leading-relaxed ${
                              isCritical 
                                ? "bg-rose-50 dark:bg-rose-950/30 border-rose-200 dark:border-rose-900/60 text-rose-900 dark:text-rose-200" 
                                : isWarning 
                                  ? "bg-amber-50 dark:bg-amber-950/30 border-amber-200 dark:border-amber-900/60 text-amber-900 dark:text-amber-200" 
                                  : "bg-slate-100 dark:bg-slate-800/60 border-slate-300 dark:border-slate-600 text-slate-800 dark:text-slate-200"
                            }`}
                          >
                            <div className="mt-0.5">
                              {isCritical || isWarning ? (
                                <FiAlertTriangle className={`h-5 w-5 ${isCritical ? "text-rose-600" : "text-amber-600"}`} />
                              ) : (
                                <FiInfo className="h-5 w-5 text-slate-500" />
                              )}
                            </div>
                            <div className="space-y-1">
                              <div className="flex items-center gap-2 flex-wrap">
                                <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider ${
                                  isCritical 
                                    ? "bg-rose-600 text-white" 
                                    : isWarning 
                                      ? "bg-amber-500 text-white" 
                                      : "bg-slate-500 text-white"
                                }`}>
                                  {issue.severity === "CRITICAL" ? "심각 오류" : issue.severity === "WARNING" ? "주의 경고" : "의견 추천"}
                                </span>
                                {issue.targetText && (
                                  <code className="text-xs px-1.5 py-0.5 rounded bg-white/70 dark:bg-slate-800/80 border border-black/10 dark:border-white/10 font-mono whitespace-pre-wrap break-all">
                                    "{issue.targetText}"
                                  </code>
                                )}
                              </div>
                              <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">{issue.message}</p>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* 승인 작업 하단 바 */}
            <div className="bg-slate-50 dark:bg-slate-950/30 border-t border-slate-300 dark:border-slate-600 p-6 flex items-center justify-between">
              <div>
                {(detail.status === "REVIEW_READY" || detail.status === "FAILED_AT_NOTION_PUBLISH" || detail.status === "FAILED_AT_VECTOR_INDEX") ? (
                  <p className="text-xs text-slate-500 dark:text-slate-400">
                    * 승인 버튼 클릭 시, <strong>Notion API</strong>와 <strong>벡터 임베딩 모델(Google Gemini)</strong>로 데이터가 실시간 전송됩니다.
                  </p>
                ) : (
                  <p className="text-xs text-rose-600 dark:text-rose-400 font-semibold">
                    * 이 지식은 현재 승인할 수 없는 상태입니다. (상태: {statusInfo.label})
                  </p>
                )}
              </div>

              <div className="flex gap-2">
                <button
                  onClick={onClose}
                  className="rounded-xl border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 px-5 py-3 text-sm font-semibold transition-all active:scale-95 cursor-pointer"
                >
                  취소
                </button>
                
                {(detail.status === "REVIEW_READY" || detail.status === "FAILED_AT_NOTION_PUBLISH" || detail.status === "FAILED_AT_VECTOR_INDEX") && (
                  <button
                    onClick={handleApprove}
                    disabled={approveMutation.isPending}
                    className="flex items-center gap-2 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 px-6 py-3 text-sm font-bold text-white shadow-lg shadow-sky-500/20 hover:from-sky-400 hover:to-indigo-500 transition-all active:scale-95 disabled:opacity-50 cursor-pointer"
                  >
                    {approveMutation.isPending 
                      ? "처리 중..." 
                      : (detail.status === "REVIEW_READY" ? "최종 승인 및 발행" : "재발행 시도")}
                    <FiChevronRight className="h-4.5 w-4.5" />
                  </button>
                )}
              </div>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
};
