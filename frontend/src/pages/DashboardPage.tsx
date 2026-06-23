import React, { useState } from 'react';
import { FiPlus, FiRefreshCw } from 'react-icons/fi';
import type { KnowledgeLog, KnowledgeRequest } from '../types/knowledge';
import { CreateKnowledgeModal } from '../components/CreateKnowledgeModal';

export const DashboardPage: React.FC = () => {
  const [sources, setSources] = useState<KnowledgeLog[]>([
    {
      id: 1,
      title: '쿠버네티스 오퍼레이터 개발 지침서',
      rawContent: '쿠버네티스 커스텀 리소스를 제어하는 오퍼레이터 패턴에 대한 설명...',
      formattedContent: null,
      createDate: '2026-06-21',
      updateDate: null,
      verificationScore: 95,
      verificationReport: null,
      status: 'PUBLISHED',
      verificationVersion: 1,
      deleteAt: null,
    },
    {
      id: 2,
      title: 'Spring AI 핵심 아키텍처 공식 문서',
      rawContent: 'Spring AI에서 LLM 연동을 위한 클라이언트 아키텍처 분석...',
      formattedContent: null,
      createDate: '2026-06-20',
      updateDate: null,
      verificationScore: 82,
      verificationReport: null,
      status: 'PUBLISHED',
      verificationVersion: 1,
      deleteAt: null,
    },
  ]);

  const [isModalOpen, setIsModalOpen] = useState(false);

  // 진행 상태 표시 배지
  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PUBLISHED':
        return <span className="inline-flex items-center rounded-md bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700 ring-1 ring-inset ring-emerald-600/10">완료</span>;
      case 'VERIFYING':
        return <span className="inline-flex items-center rounded-md bg-amber-50 px-2.5 py-1 text-xs font-semibold text-amber-700 ring-1 ring-inset ring-amber-600/10 animate-pulse">처리 중</span>;
      default:
        return <span className="inline-flex items-center rounded-md bg-rose-50 px-2.5 py-1 text-xs font-semibold text-rose-700 ring-1 ring-inset ring-rose-600/10">실패</span>;
    }
  };

  const handleAddSource = (newRequest: KnowledgeRequest) => {
    const newLog: KnowledgeLog = {
      id: sources.length + 1,
      title: newRequest.title,
      rawContent: newRequest.rawContent,
      formattedContent: null,
      createDate: new Date().toISOString().split('T')[0],
      updateDate: null,
      verificationScore: null,
      verificationReport: null,
      status: 'VERIFYING',
      verificationVersion: 1,
      deleteAt: null,
    };

    setSources([newLog, ...sources]);
    setIsModalOpen(false);
  };

  return (
    <div className="p-8">
      {/* 상단 타이틀 및 등록 버튼 영역 */}
      <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between border-b border-slate-200 pb-6 mb-8">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-800">지식 대시보드</h1>
          <p className="text-sm text-slate-500 mt-1">지식 소스를 수집하고 AI 벡터 디비 상태를 모니터링합니다.</p>
        </div>
        <button 
          onClick={() => setIsModalOpen(true)}
          className="flex items-center gap-2 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-sky-500/20 hover:from-sky-400 hover:to-indigo-500 transition-all hover:scale-105 active:scale-95"
        >
          <FiPlus className="h-5 w-5" />
          새 지식 등록
        </button>
      </div>

      {/* 대시보드 요약 현황 카드 그리드 */}
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 mb-8">
        {[
          { label: '전체 지식 소스', count: sources.length, desc: '등록된 지식 자료 수', color: 'from-sky-500/5' },
          { label: '완료 및 연동됨', count: sources.filter(s => s.status === 'PUBLISHED').length, desc: '검색 가능한 지식', color: 'from-emerald-500/5' },
          { label: '처리 대기 중', count: sources.filter(s => s.status === 'VERIFYING').length, desc: '벡터라이징 대기', color: 'from-amber-500/5' },
        ].map((stat, idx) => (
          <div key={idx} className={`relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-6 shadow-sm bg-gradient-to-tr ${stat.color} to-transparent`}>
            <p className="text-sm font-semibold text-slate-500">{stat.label}</p>
            <p className="mt-2 text-3xl font-bold tracking-tight text-slate-800">{stat.count}</p>
            <p className="mt-1 text-xs text-slate-400">{stat.desc}</p>
          </div>
        ))}
      </div>

      {/* 테이블 영역 - 불필요한 '유형' 컬럼 제거 */}
      <div className="rounded-2xl border border-slate-200 bg-white shadow-sm overflow-hidden">
        <div className="flex items-center justify-between border-b border-slate-100 p-6">
          <h2 className="text-lg font-bold text-slate-800">등록된 지식 목록</h2>
          <button className="flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-slate-800 transition-colors">
            <FiRefreshCw className="h-4 w-4" />
            새로고침
          </button>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50 text-xs font-bold text-slate-500 uppercase tracking-wider">
                <th className="py-4 px-6">지식 제목</th>
                <th className="py-4 px-6">등록일자</th>
                <th className="py-4 px-6">벡터 상태</th>
                <th className="py-4 px-6 text-right">관리</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm text-slate-600">
              {sources.map((source) => (
                <tr key={source.id} className="hover:bg-slate-50/50 transition-colors">
                  <td className="py-4 px-6 font-semibold text-slate-800">{source.title}</td>
                  <td className="py-4 px-6 text-slate-500 font-mono text-xs">{source.createDate}</td>
                  <td className="py-4 px-6">{getStatusBadge(source.status)}</td>
                  <td className="py-4 px-6 text-right">
                    <button className="text-xs font-bold text-sky-600 hover:text-sky-800 mr-4 transition-colors">상세</button>
                    <button className="text-xs font-bold text-rose-600 hover:text-rose-800 transition-colors">삭제</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
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
