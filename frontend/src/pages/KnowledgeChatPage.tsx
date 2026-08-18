import React, { useState, useRef, useEffect } from "react";
import { FiSend, FiBook, FiUser } from "react-icons/fi";
import { fetchKnowledgeChatStream } from "../features/knowledge/api";
import { MarkdownRenderer } from "../components/MarkdownRenderer";

interface Message {
  id: string;
  sender: "user" | "ai";
  text: string;
  timestamp: string;
}

export const KnowledgeChatPage: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: "1",
      sender: "ai",
      text: "안녕하세요! 등록된 지식 저장소를 기반으로 답변해 드립니다. 궁금한 점을 물어보세요.",
      timestamp: "",
    },
  ]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const chatEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim() || isLoading) return;

    const userQuery = input;
    setInput("");
    setIsLoading(true);

    const userMsg: Message = {
      id: `user-${Date.now()}`,
      sender: "user",
      text: userQuery,
      timestamp: new Date().toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
      }),
    };

    const aiMessageId = `ai-${Date.now()}`;
    const initialAiMsg: Message = {
      id: aiMessageId,
      sender: "ai",
      text: "",
      timestamp: new Date().toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
      }),
    };

    setMessages((prev) => [...prev, userMsg, initialAiMsg]);

    let accumulatedText = "";

    try {
      await fetchKnowledgeChatStream(userQuery, {
        onMessage: (event: any) => {
          try {
            if (event.data) {
              const eventData = JSON.parse(event.data);
              if (eventData.type === "TOKEN" && eventData.message) {
                accumulatedText += eventData.message;
                setMessages((prev) =>
                  prev.map((msg) =>
                    msg.id === aiMessageId
                      ? { ...msg, text: accumulatedText }
                      : msg,
                  ),
                );
              } else if (eventData.type === "COMPLETE") {
                setIsLoading(false);
              }
            }
          } catch (e) {
            console.error("[KnowledgeChatPage] 파싱 에러:", e);
          }
        },
        onComplete: () => setIsLoading(false),
        onError: () => {
          setMessages((prev) =>
            prev.map((msg) =>
              msg.id === aiMessageId
                ? { ...msg, text: "죄송합니다. 답변 수신 중 오류가 발생했습니다." }
                : msg,
            ),
          );
          setIsLoading(false);
        },
      });
    } catch {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-1 flex-col h-full overflow-hidden bg-[#F5F6F7] dark:bg-[#16171d] transition-colors duration-200">
      {/* 상단 바 */}
      <div className="hidden md:flex h-16 items-center justify-between border-b border-[#E4E8EB] dark:border-[#2e303a] bg-white dark:bg-[#16171d] px-6">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-[#F4F6F8] dark:bg-[#1f2028] text-[#03C75A] border border-[#E4E8EB] dark:border-[#2e303a]">
            <FiBook className="h-6 w-6" />
          </div>
          <div>
            <h2 className="text-sm font-bold text-[#1E1E1E] dark:text-[#f3f4f6]">
              지식 AI 어시스턴트
            </h2>
            <p className="text-xs text-slate-400">
              개인 지식 저장소 기반 RAG 답변
            </p>
          </div>
        </div>
      </div>

      {/* 메시지 영역 */}
      <div className="flex-1 overflow-y-auto p-3 sm:p-6 space-y-4 sm:space-y-5">
        {messages.map((msg) => {
          const isAi = msg.sender === "ai";
          return (
            <div
              key={msg.id}
              className={`flex gap-3 max-w-3xl ${isAi ? "" : "ml-auto flex-row-reverse"}`}
            >
              <div
                className={`flex h-9 w-9 items-center justify-center rounded-lg ${
                  isAi
                    ? "bg-[#03C75A] text-white"
                    : "bg-white dark:bg-[#1f2028] border border-[#E4E8EB] dark:border-[#2e303a] text-[#404040] dark:text-slate-300"
                }`}
              >
                {isAi ? (
                  <FiBook className="h-5 w-5" />
                ) : (
                  <FiUser className="h-5 w-5" />
                )}
              </div>
              <div className="space-y-1">
                <div
                  className={`rounded-lg px-4 py-2.5 text-sm leading-relaxed ${
                    isAi
                      ? "bg-white dark:bg-[#1f2028] border border-[#E4E8EB] dark:border-[#2e303a] text-[#1E1E1E] dark:text-[#f3f4f6]"
                      : "bg-[#03C75A] text-white font-normal"
                  }`}
                >
                  {isAi ? (
                    <MarkdownRenderer content={msg.text} />
                  ) : (
                    <p className="whitespace-pre-wrap">{msg.text}</p>
                  )}
                </div>
                <p
                  className={`text-[10px] text-slate-400 ${isAi ? "" : "text-right"}`}
                >
                  {msg.timestamp}
                </p>
              </div>
            </div>
          );
        })}

        {/* 로딩 표시 */}
        {isLoading && (
          <div className="flex gap-3 max-w-3xl">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-[#03C75A] text-white">
              <FiBook className="h-5 w-5" />
            </div>
            <div className="flex items-center gap-1.5 rounded-lg bg-white dark:bg-[#1f2028] border border-[#E4E8EB] dark:border-[#2e303a] px-4 py-3">
              <div
                className="h-1.5 w-1.5 rounded-full bg-slate-300 dark:bg-slate-600 animate-bounce"
                style={{ animationDelay: "0ms" }}
              />
              <div
                className="h-1.5 w-1.5 rounded-full bg-slate-300 dark:bg-slate-600 animate-bounce"
                style={{ animationDelay: "150ms" }}
              />
              <div
                className="h-1.5 w-1.5 rounded-full bg-slate-300 dark:bg-slate-600 animate-bounce"
                style={{ animationDelay: "300ms" }}
              />
            </div>
          </div>
        )}
        <div ref={chatEndRef} />
      </div>

      {/* 하단 입력 폼 */}
      <div className="border-t border-[#E4E8EB] dark:border-[#2e303a] p-4 bg-white dark:bg-[#16171d]">
        <div
          className={`flex items-center gap-2 max-w-4xl mx-auto border rounded-lg p-1.5 transition-all ${
            isLoading
              ? "bg-[#F4F6F8] dark:bg-[#16171d] border-[#E4E8EB] dark:border-[#2e303a] cursor-not-allowed"
              : "bg-white dark:bg-[#1f2028] border-[#E4E8EB] dark:border-[#2e303a] focus-within:border-[#03C75A]"
          }`}
        >
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSend()}
            disabled={isLoading}
            placeholder={
              isLoading
                ? "답변을 대기 중입니다..."
                : "지식 저장소에서 검색할 내용을 입력하세요..."
            }
            className={`flex-1 bg-transparent px-2.5 text-sm text-[#1E1E1E] dark:text-[#f3f4f6] placeholder-slate-400 outline-none ${
              isLoading ? "cursor-not-allowed opacity-60" : ""
            }`}
          />
          <button
            onClick={handleSend}
            disabled={isLoading}
            className={`flex h-9 w-9 items-center justify-center rounded-lg text-white transition-all ${
              isLoading
                ? "bg-slate-300 dark:bg-slate-700 cursor-not-allowed"
                : "bg-[#03C75A] hover:bg-[#02b350] active:scale-95"
            }`}
          >
            <FiSend className="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  );
};
