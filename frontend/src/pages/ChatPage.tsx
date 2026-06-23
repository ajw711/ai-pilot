import React, { useState, useRef, useEffect } from 'react';
import { FiSend, FiCpu, FiUser, FiInfo } from 'react-icons/fi';

interface Message {
  id: string;
  sender: 'user' | 'ai';
  text: string;
  timestamp: string;
}

export const ChatPage: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([
    { id: '1', sender: 'ai', text: '안녕하세요! 등록된 지식 정보를 기반으로 질문에 답변해 드립니다. 궁금한 점을 물어보세요.', timestamp: '오후 2:30' },
  ]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const chatEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = () => {
    if (!input.trim()) return;

    const userMsg: Message = {
      id: Date.now().toString(),
      sender: 'user',
      text: input,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setIsLoading(true);

    // Mock API 응답 애니메이션
    setTimeout(() => {
      const aiMsg: Message = {
        id: (Date.now() + 1).toString(),
        sender: 'ai',
        text: `'${userMsg.text}'에 대한 답변 샘플입니다. (Spring Boot 백엔드의 AI / MCP 도메인이 연동되면 여기에 연동된 기밀 지식을 검색하여 실시간 답변을 출력하게 됩니다.)`,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, aiMsg]);
      setIsLoading(false);
    }, 1500);
  };

  return (
    <div className="flex flex-1 flex-col h-screen overflow-hidden bg-slate-50">
      {/* 챗 상단 바 (밝은 테마 스타일) */}
      <div className="hidden md:flex h-16 items-center justify-between border-b border-slate-200 bg-white px-6 shadow-sm">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-sky-50 text-sky-600 border border-sky-100">
            <FiCpu className="h-6 w-6" />
          </div>
          <div>
            <h2 className="text-sm font-bold text-slate-800">AI 지식 어시스턴트</h2>
            <div className="flex items-center gap-1">
              <span className="h-2 w-2 rounded-full bg-emerald-500 animate-ping"></span>
              <span className="text-xs text-emerald-600 font-semibold">실시간 지식 검색 활성화</span>
            </div>
          </div>
        </div>
      </div>

      {/* 챗 알림 배너 (파스텔톤 블루) */}
      <div className="bg-sky-50 px-6 py-3 border-b border-sky-100 flex items-center gap-3 text-xs text-sky-700 font-medium">
        <FiInfo className="h-4.5 w-4.5 flex-shrink-0 text-sky-500" />
        <span>현재 데모 답변 모드입니다. 백엔드와 연동하려면 API 통신 인터페이스를 구성해 주세요.</span>
      </div>

      {/* 메시지 영역 */}
      <div className="flex-1 overflow-y-auto p-6 space-y-6">
        {messages.map((msg) => {
          const isAi = msg.sender === 'ai';
          return (
            <div key={msg.id} className={`flex gap-4 max-w-3xl ${isAi ? '' : 'ml-auto flex-row-reverse'}`}>
              {/* 프로필 이미지 아이콘 (테마 정돈) */}
              <div className={`flex h-9 w-9 items-center justify-center rounded-xl shadow-sm ${
                isAi ? 'bg-indigo-600 text-white' : 'bg-white border border-slate-200 text-slate-600'
              }`}>
                {isAi ? <FiCpu className="h-5 w-5" /> : <FiUser className="h-5 w-5" />}
              </div>

              {/* 말풍선 */}
              <div className="space-y-1">
                <div className={`rounded-2xl px-4 py-3 text-sm leading-relaxed shadow-sm ${
                  isAi
                    ? 'bg-white border border-slate-200 text-slate-800'
                    : 'bg-gradient-to-r from-sky-500 to-indigo-600 text-white font-medium'
                }`}>
                  <p>{msg.text}</p>
                </div>
                <p className={`text-[10px] text-slate-400 ${isAi ? '' : 'text-right'}`}>
                  {msg.timestamp}
                </p>
              </div>
            </div>
          );
        })}

        {/* 로딩 표시 */}
        {isLoading && (
          <div className="flex gap-4 max-w-3xl">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-indigo-600 text-white shadow-sm">
              <FiCpu className="h-5 w-5" />
            </div>
            <div className="flex items-center gap-1.5 rounded-2xl bg-white border border-slate-200 px-4 py-3.5 shadow-sm">
              <div className="h-2 w-2 rounded-full bg-slate-300 animate-bounce" style={{ animationDelay: '0ms' }}></div>
              <div className="h-2 w-2 rounded-full bg-slate-300 animate-bounce" style={{ animationDelay: '150ms' }}></div>
              <div className="h-2 w-2 rounded-full bg-slate-300 animate-bounce" style={{ animationDelay: '300ms' }}></div>
            </div>
          </div>
        )}
        <div ref={chatEndRef} />
      </div>

      {/* 하단 입력 폼 (화이트 카드 레이아웃) */}
      <div className="border-t border-slate-200 p-6 bg-white shadow-md">
        <div className="flex items-center gap-3 max-w-4xl mx-auto bg-slate-50 border border-slate-200 rounded-2xl p-2.5 focus-within:bg-white focus-within:ring-2 focus-within:ring-sky-500/20 focus-within:border-sky-500 transition-all">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSend()}
            placeholder="AI에게 무엇이든 물어보세요... (지식 정보 조회 가능)"
            className="flex-1 bg-transparent px-3 text-sm text-slate-800 placeholder-slate-400 outline-none"
          />
          <button
            onClick={handleSend}
            className="flex h-10 w-10 items-center justify-center rounded-xl bg-sky-600 hover:bg-sky-500 text-white shadow-md shadow-sky-600/10 active:scale-95 transition-all"
          >
            <FiSend className="h-5 w-5" />
          </button>
        </div>
      </div>
    </div>
  );
};
