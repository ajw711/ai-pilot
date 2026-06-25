import React, { useMemo } from "react";
import { marked } from "marked";

interface MarkdownRendererProps {
  content: string;
}

export const MarkdownRenderer: React.FC<MarkdownRendererProps> = ({
  content,
}) => {
  // marked.parse를 메모이제이션하여 렌더링 성능 최적화
  const rawHtml = useMemo(() => {
    if (!content) return "";

    // GFM (GitHub Flavored Markdown) 활성화 및 줄바꿈 지원 설정
    return marked.parse(content, { gfm: true, breaks: true }) as string;
  }, [content]);

  return (
    <div className="markdown-preview-container select-text">
      {/* 
        표준 마크다운 렌더링 결과물이 Tailwind 환경에서도 
        보이도록 오버라이딩 스타일을 로컬 주입
      */}
      <style>{`
        .markdown-preview-container h1 {
          font-size: 1.5rem;
          font-weight: 800;
          margin-top: 1.5rem;
          margin-bottom: 0.75rem;
          color: #0f172a;
          border-bottom: 2px solid #cbd5e1;
          padding-bottom: 0.5rem;
        }
        .dark .markdown-preview-container h1 {
          color: #f8fafc;
          border-bottom-color: #334155;
        }
        
        .markdown-preview-container h2 {
          font-size: 1.25rem;
          font-weight: 700;
          margin-top: 1.25rem;
          margin-bottom: 0.5rem;
          color: #1e293b;
          border-bottom: 1px solid #e2e8f0;
          padding-bottom: 0.25rem;
        }
        .dark .markdown-preview-container h2 {
          color: #f1f5f9;
          border-bottom-color: #475569;
        }

        .markdown-preview-container h3 {
          font-size: 1.1rem;
          font-weight: 700;
          margin-top: 1rem;
          margin-bottom: 0.35rem;
          color: #1e293b;
        }
        .dark .markdown-preview-container h3 {
          color: #e2e8f0;
        }

        .markdown-preview-container p {
          font-size: 0.875rem;
          line-height: 1.625;
          margin-bottom: 0.75rem;
          color: #334155;
        }
        .dark .markdown-preview-container p {
          color: #cbd5e1;
        }

        .markdown-preview-container ul {
          list-style-type: disc;
          padding-left: 1.25rem;
          margin-bottom: 0.75rem;
          color: #334155;
        }
        .dark .markdown-preview-container ul {
          color: #cbd5e1;
        }
        .markdown-preview-container li {
          margin-bottom: 0.25rem;
        }

        .markdown-preview-container blockquote {
          border-left: 4px solid #0ea5e9;
          padding-left: 1rem;
          color: #475569;
          background-color: #f8fafc;
          padding-top: 0.5rem;
          padding-bottom: 0.5rem;
          margin-bottom: 0.75rem;
          border-radius: 0 0.5rem 0.5rem 0;
        }
        .dark .markdown-preview-container blockquote {
          color: #94a3b8;
          background-color: rgba(15, 23, 42, 0.3);
        }

        .markdown-preview-container pre {
          background-color: #020617;
          border: 1px solid #cbd5e1;
          padding: 1rem;
          border-radius: 0.75rem;
          overflow-x: auto;
          margin-top: 0.75rem;
          margin-bottom: 0.75rem;
        }
        .dark .markdown-preview-container pre {
          border-color: #475569;
        }
        .markdown-preview-container pre code {
          font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
          font-size: 0.775rem;
          color: #e2e8f0;
          background-color: transparent;
          padding: 0;
          border-radius: 0;
          font-weight: 500;
        }

        .markdown-preview-container code {
          font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
          font-size: 0.775rem;
          background-color: #f1f5f9;
          color: #e11d48; /* rose-600 */
          padding: 0.125rem 0.25rem;
          border-radius: 0.375rem;
          font-weight: 600;
        }
        .dark .markdown-preview-container code {
          background-color: #1e293b;
          color: #fb7185; /* rose-400 */
        }
      `}</style>
      <div
        className="markdown-body font-sans text-sm text-slate-800 dark:text-slate-200 leading-relaxed"
        dangerouslySetInnerHTML={{ __html: rawHtml }}
      />
    </div>
  );
};
