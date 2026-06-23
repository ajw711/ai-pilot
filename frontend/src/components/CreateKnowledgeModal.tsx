import React, { useState } from "react";
import type { KnowledgeRequest } from "../types/knowledge";

interface CreateKnowledgeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAdd: (newSource: KnowledgeRequest) => void;
}

export const CreateKnowledgeModal: React.FC<CreateKnowledgeModalProps> = ({
  isOpen,
  onClose,
  onAdd,
}) => {
  const [newTitle, setNewTitle] = useState("");
  const [newContent, setNewContent] = useState("");

  const handleSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!newTitle.trim() || !newContent.trim()) return;

    onAdd({
      title: newTitle,
      rawContent: newContent,
      tags: [],
      sourceUrls: [],
    });

    setNewTitle("");
    setNewContent("");
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-6 bg-slate-900/40 backdrop-blur-sm animate-fadeIn">
      <div className="w-full max-w-4xl rounded-2xl border border-slate-200 bg-white p-8 shadow-2xl flex flex-col max-h-[90vh]">
        
        {/* 모달 상단 헤더 */}
        <div className="flex items-center justify-between border-b border-slate-100 pb-5 mb-6">
          <div>
            <h3 className="text-xl font-bold text-slate-800">신규 지식 등록 및 AI 검수</h3>
            <p className="text-xs text-slate-500 mt-1">원문을 업로드하면 AI가 실시간으로 팩트 에러를 파악하고 가공합니다.</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="text-slate-400 hover:text-slate-600 text-sm font-semibold transition-colors"
          >
            닫기
          </button>
        </div>

        {/* 모달 내용 입력 폼 */}
        <form onSubmit={handleSubmit} className="flex-1 flex flex-col gap-5 overflow-hidden">
          {/* 지식 제목 - 유형 컬럼을 제거하고 단독 행으로 풀 스크린 배치 */}
          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">
              지식 제목
            </label>
            <input
              type="text"
              required
              value={newTitle}
              onChange={(e) => setNewTitle(e.target.value)}
              placeholder="지식 문서의 핵심 제목을 입력하세요 (예: 쿠버네티스 포드 튜닝 가이드)"
              className="w-full rounded-xl border border-slate-200 bg-slate-50 focus:bg-white px-4 py-3 text-sm text-slate-800 placeholder-slate-400 outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all"
            />
          </div>

          {/* 원본 내용 */}
          <div className="flex-1 flex flex-col min-h-[300px]">
            <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">
              원본 내용 (Raw Content)
            </label>
            <textarea
              required
              value={newContent}
              onChange={(e) => setNewContent(e.target.value)}
              placeholder="여기에 검수 및 요약할 문서의 원문 내용을 복사해서 넣어주세요. (줄바꿈이 많이 포함된 긴 내용도 정상 작동합니다.)"
              className="w-full flex-1 rounded-xl border border-slate-200 bg-slate-50 focus:bg-white p-5 text-sm text-slate-700 placeholder-slate-400 outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all resize-none font-sans leading-relaxed"
            />
          </div>

          {/* 하단 버튼 세트 */}
          <div className="flex justify-end gap-3 pt-5 border-t border-slate-100 mt-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl border border-slate-200 text-slate-600 hover:bg-slate-100 px-5 py-3 text-sm font-semibold transition-all active:scale-95"
            >
              취소
            </button>
            <button
              type="submit"
              className="rounded-xl bg-sky-600 hover:bg-sky-500 px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-sky-600/10 transition-all active:scale-95"
            >
              검수 요청 및 저장
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
