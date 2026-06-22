import React, { useState } from 'react';
import { FiPlus, FiServer, FiLink, FiFileText, FiRefreshCw } from 'react-icons/fi';

interface KnowledgeSource {
  id: string;
  title: string;
  type: 'Text' | 'Web' | 'File' | 'Notion';
  createdAt: string;
  status: 'Published' | 'Processing' | 'Failed';
}

export const DashboardPage: React.FC = () => {
  const [sources, setSources] = useState<KnowledgeSource[]>([
    { id: '1', title: '쿠버네티스 오퍼레이터 개발 지침서', type: 'File', createdAt: '2026-06-21', status: 'Published' },
    { id: '2', title: 'Spring AI 핵심 아키텍처 공식 문서', type: 'Web', createdAt: '2026-06-20', status: 'Published' },
    { id: '3', title: 'Model Context Protocol (MCP) 연동 규격', type: 'Notion', createdAt: '2026-06-19', status: 'Processing' },
    { id: '4', title: '임시 AI 지식 분석 메모', type: 'Text', createdAt: '2026-06-18', status: 'Failed' },
  ]);

  const getIcon = (type: string) => {
    switch (type) {
      case 'File': return <FiFileText className="text-emerald-400" />;
      case 'Web': return <FiLink className="text-sky-400" />;
      default: return <FiServer className="text-indigo-400" />;
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'Published':
        return <span className="inline-flex items-center rounded-md bg-emerald-500/10 px-2.5 py-0.5 text-xs font-medium text-emerald-400 ring-1 ring-inset ring-emerald-500/20">완료</span>;
      case 'Processing':
        return <span className="inline-flex items-center rounded-md bg-amber-500/10 px-2.5 py-0.5 text-xs font-medium text-amber-400 ring-1 ring-inset ring-amber-500/20 animate-pulse">처리 중</span>;
      default:
        return <span className="inline-flex items-center rounded-md bg-rose-500/10 px-2.5 py-0.5 text-xs font-medium text-rose-400 ring-1 ring-inset ring-rose-500/20">실패</span>;
    }
  };

  return (
    <div className="p-8">
      {/* 상단 타이틀 */}
      <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between border-b border-slate-800 pb-6 mb-8">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">지식 대시보드</h1>
          <p className="text-sm text-slate-400 mt-1">지식 소스를 수집하고 AI 벡터 디비 상태를 모니터링합니다.</p>
        </div>
        <button className="flex items-center gap-2 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-500 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-sky-500/25 hover:from-sky-400 hover:to-indigo-400 transition-all hover:scale-105 active:scale-95">
          <FiPlus className="h-5 w-5" />
          새 지식 등록
        </button>
      </div>

      {/* 요약 현황 카드 그리드 */}
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 mb-8">
        {[
          { label: '전체 지식 소스', count: sources.length, desc: '등록된 지식 자료 수', color: 'from-sky-500/20' },
          { label: '완료 및 연동됨', count: sources.filter(s => s.status === 'Published').length, desc: '검색 가능한 지식', color: 'from-emerald-500/20' },
          { label: '처리 대기 중', count: sources.filter(s => s.status === 'Processing').length, desc: '벡터라이징 대기', color: 'from-amber-500/20' },
        ].map((stat, idx) => (
          <div key={idx} className={`relative overflow-hidden rounded-2xl border border-slate-800 bg-slate-900/40 p-6 backdrop-blur-sm bg-gradient-to-tr ${stat.color} to-transparent`}>
            <p className="text-sm font-medium text-slate-400">{stat.label}</p>
            <p className="mt-2 text-3xl font-bold tracking-tight text-white">{stat.count}</p>
            <p className="mt-1 text-xs text-slate-500">{stat.desc}</p>
          </div>
        ))}
      </div>

      {/* 테이블 영역 */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/20 backdrop-blur-md overflow-hidden">
        <div className="flex items-center justify-between border-b border-slate-800 p-6">
          <h2 className="text-lg font-semibold text-white">등록된 지식 목록</h2>
          <button className="flex items-center gap-1.5 text-xs font-medium text-slate-400 hover:text-slate-200 transition-colors">
            <FiRefreshCw className="h-4.5 w-4.5" />
            새로고침
          </button>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-800/60 bg-slate-900/30 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                <th className="py-4 px-6">유형</th>
                <th className="py-4 px-6">지식 제목</th>
                <th className="py-4 px-6">등록일자</th>
                <th className="py-4 px-6">벡터 상태</th>
                <th className="py-4 px-6 text-right">관리</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/40 text-sm text-slate-300">
              {sources.map((source) => (
                <tr key={source.id} className="hover:bg-slate-800/10 transition-colors">
                  <td className="py-4.5 px-6">
                    <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-800/60 ring-1 ring-slate-700/30">
                      {getIcon(source.type)}
                    </div>
                  </td>
                  <td className="py-4.5 px-6 font-medium text-white">{source.title}</td>
                  <td className="py-4.5 px-6 text-slate-400">{source.createdAt}</td>
                  <td className="py-4.5 px-6">{getStatusBadge(source.status)}</td>
                  <td className="py-4.5 px-6 text-right">
                    <button className="text-xs font-semibold text-sky-400 hover:text-sky-300 mr-4">상세</button>
                    <button className="text-xs font-semibold text-rose-500 hover:text-rose-400">삭제</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
