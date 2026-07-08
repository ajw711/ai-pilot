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
  const [sourceUrls, setSourceUrls] = useState<string[]>([]);
  const [currentUrl, setCurrentUrl] = useState("");

  // 원문에 포함된 쌩 이미지 URL들을 감지하여 마크다운 이미지 태그로 자동 변환하는 헬퍼 함수
  const convertRawUrlsToMarkdownImages = (text: string): string => {
    // 이미 ![]() 혹은 []() 안에 들어있는 주소는 제외하고, 이미지 확장자나 gstatic/licensed-image 등 이미지 전용 API 주소를 매칭
    const imageUrlRegex =
      /(?<!\!\[[^\]]*\]\()(?<!\[[^\]]*\]\()(https?:\/\/[^\s\)]+?\.(?:png|jpg|jpeg|gif|webp|svg)(?:\?[^\s\)]*)?|https?:\/\/[^\s\)]+?gstatic\.com\/[^\s\)]+|https?:\/\/[^\s\)]+?licensed-image[^\s\)]+)/gi;

    return text.replace(imageUrlRegex, (url) => {
      return `![이미지](${url})`;
    });
  };

  const handleAddUrl = () => {
    if (!currentUrl.trim()) return;
    // 중복 제거 후 추가
    if (!sourceUrls.includes(currentUrl.trim())) {
      setSourceUrls([...sourceUrls, currentUrl.trim()]);
    }
    setCurrentUrl("");
  };

  const handleRemoveUrl = (index: number) => {
    setSourceUrls(sourceUrls.filter((_, i) => i !== index));
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!newTitle.trim() || !newContent.trim()) return;

    // 쌩 URL 주소들을 자동으로 ![이미지](URL) 형태로 변환하여 저장 요청
    const processedContent = convertRawUrlsToMarkdownImages(newContent);

    onAdd({
      title: newTitle,
      rawContent: processedContent,
      tags: [],
      sourceUrls: sourceUrls, // 추가된 출처 리스트 전달
    });

    setNewTitle("");
    setNewContent("");
    setSourceUrls([]);
    setCurrentUrl("");
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-6 bg-slate-950/60 backdrop-blur-sm animate-fadeIn">
      <div className="w-full max-w-4xl rounded-2xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 p-8 shadow-2xl flex flex-col max-h-[90vh] transition-colors duration-200">
        {/* 모달 상단 헤더 */}
        <div className="flex items-center justify-between border-b border-slate-300 dark:border-slate-700 pb-5 mb-6">
          <div>
            <h3 className="text-xl font-bold text-slate-800 dark:text-slate-100">
              신규 지식 등록 및 AI 검수
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
              원문을 업로드하면 AI가 실시간으로 팩트 에러를 파악하고 가공합니다.
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="text-slate-400 dark:text-slate-500 hover:text-slate-600 dark:hover:text-slate-300 text-sm font-semibold transition-colors cursor-pointer"
          >
            닫기
          </button>
        </div>

        {/* 모달 내용 입력 폼 */}
        <form
          onSubmit={handleSubmit}
          className="flex-1 flex flex-col overflow-hidden"
        >
          {/* 스크롤 가능한 입력 필드 영역 */}
          <div className="flex-1 overflow-y-auto pr-1 pb-4 space-y-5 scrollbar-thin">
            {/* 지식 제목 */}
            <div>
              <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-2">
                지식 제목
              </label>
              <input
                type="text"
                required
                value={newTitle}
                onChange={(e) => setNewTitle(e.target.value)}
                placeholder="지식 문서의 핵심 제목을 입력하세요 (예: 쿠버네티스 포드 튜닝 가이드)"
                className="w-full rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-950 focus:bg-white dark:focus:bg-slate-900 px-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder-slate-400 dark:placeholder-slate-500 outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all font-semibold"
              />
            </div>

            {/* 원본 내용 */}
            <div className="flex flex-col min-h-[240px]">
              <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-2">
                원본 내용 (Raw Content)
              </label>
              <textarea
                required
                value={newContent}
                onChange={(e) => setNewContent(e.target.value)}
                placeholder="여기에 검수 및 요약할 문서의 원문 내용을 복사해서 넣어주세요."
                className="w-full flex-1 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-950 focus:bg-white dark:focus:bg-slate-900 p-5 text-sm text-slate-700 dark:text-slate-200 placeholder-slate-400 dark:placeholder-slate-500 outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all resize-none font-sans leading-relaxed"
              />
            </div>

            {/* 참고 출처 링크 입력 영역 */}
            <div>
              <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-2">
                참고 출처 링크 (Source URLs)
              </label>
              <div className="flex gap-2 mb-2">
                <input
                  type="url"
                  value={currentUrl}
                  onChange={(e) => setCurrentUrl(e.target.value)}
                  placeholder="참고한 블로그나 공식 문서 URL을 입력하세요 (예: https://kubernetes.io/...)"
                  className="flex-1 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-950 focus:bg-white dark:focus:bg-slate-900 px-4 py-2.5 text-xs text-slate-800 dark:text-slate-100 placeholder-slate-400 dark:placeholder-slate-500 outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all"
                />
                <button
                  type="button"
                  onClick={handleAddUrl}
                  className="px-4 py-2.5 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 rounded-xl text-xs font-bold transition-all active:scale-95 cursor-pointer border border-slate-300 dark:border-slate-700"
                >
                  추가
                </button>
              </div>

              {/* 등록된 출처 리스트 배지 */}
              {sourceUrls.length > 0 && (
                <div className="flex flex-wrap gap-2 p-3 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-950/20 max-h-24 overflow-y-auto">
                  {sourceUrls.map((url, idx) => (
                    <div
                      key={idx}
                      className="inline-flex items-center gap-1.5 rounded-lg bg-sky-50 dark:bg-sky-950/40 border border-sky-200 dark:border-sky-900/60 px-2.5 py-1 text-xs font-medium text-sky-700 dark:text-sky-300 shadow-sm"
                    >
                      <span className="truncate max-w-xs">{url}</span>
                      <button
                        type="button"
                        onClick={() => handleRemoveUrl(idx)}
                        className="text-sky-400 hover:text-sky-600 dark:hover:text-sky-200 font-bold focus:outline-none cursor-pointer"
                      >
                        &times;
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* 하단 버튼 세트 */}
          <div className="flex justify-end gap-3 pt-5 border-t border-slate-300 dark:border-slate-700 mt-2 bg-white dark:bg-slate-900 z-10">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl border border-slate-300 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 px-5 py-3 text-sm font-semibold transition-all active:scale-95 cursor-pointer"
            >
              취소
            </button>
            <button
              type="submit"
              className="rounded-xl bg-sky-600 hover:bg-sky-500 px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-sky-600/10 transition-all active:scale-95 cursor-pointer"
            >
              검수 요청 및 저장
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
