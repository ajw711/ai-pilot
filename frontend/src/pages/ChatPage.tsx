import React, { useState, useRef, useEffect } from "react";
import { FiSend, FiCpu, FiUser, FiInfo } from "react-icons/fi";
import { fetchPilotChatStream, useOpsNotification } from "../features/ops/api";
import { MarkdownRenderer } from "../components/MarkdownRenderer";

export interface ChatRequestDto {
  message: string;
}

export interface ChatEventDto {
  type: "TOKEN" | "COMPLETE";
  message?: string;
}

export interface Message {
  id: string;
  sender: "user" | "ai";
  text: string;
  timestamp: string;
  trackingId?: string;
  deployStatus?: "RUNNING" | "SUCCESS" | "FAILED";
  deployMessage?: string;
}

export const ChatPage: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: "1",
      sender: "ai",
      text: "안녕하세요! 등록된 지식 정보를 기반으로 질문에 답변해 드립니다. 궁금한 점을 물어보세요.",
      timestamp: "오후 2:30",
    },
  ]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [pendingResults, setPendingResults] = useState<
    Record<string, { status: string; message: string }>
  >({});
  const pendingResultsRef = useRef<
    Record<string, { status: string; message: string }>
  >({});
  const chatEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // 실시간 SSE 인프라 운영 피드백 수신 리스너 장착 (1:1 타겟 갱신)
  useOpsNotification("test-user", (payload) => {
    if (payload.type !== "DEPLOY") return;

    setMessages((prev) => {
      const isExist = prev.some((msg) => msg.trackingId === payload.trackingId);
      if (!isExist) {
        setPendingResults((prevPending) => {
          const nextPending = {
            ...prevPending,
            [payload.trackingId]: {
              status: payload.status,
              message: payload.message,
            },
          };
          pendingResultsRef.current = nextPending;
          return nextPending;
        });
        return prev;
      }
      return prev.map((msg) =>
        msg.trackingId === payload.trackingId
          ? {
              ...msg,
              deployStatus:
                payload.status === "RUNNING"
                  ? "SUCCESS"
                  : payload.status === "DEPLOYING"
                    ? "RUNNING"
                    : (payload.status as any),
              deployMessage: payload.message,
            }
          : msg,
      );
    });
  });

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
    let trackingIdExtracted = false;

    try {
      await fetchPilotChatStream(userQuery, {
        onMessage: (event) => {
          try {
            const eventData: ChatEventDto = JSON.parse(event.data);
            if (eventData.type === "TOKEN" && eventData.message) {
              accumulatedText += eventData.message;

              const match = accumulatedText.match(/DEPLOY-[A-Z0-9]{8}/);

              if (match && !trackingIdExtracted) {
                const extractedId = match[0];
                trackingIdExtracted = true;

                setMessages((prev) =>
                  prev.map((msg) => {
                    if (msg.id === aiMessageId) {
                      const pending = pendingResultsRef.current[extractedId];
                      let finalStatus: "RUNNING" | "SUCCESS" | "FAILED" =
                        "RUNNING";
                      let finalMessage = "";

                      if (pending) {
                        finalStatus =
                          pending.status === "RUNNING"
                            ? "SUCCESS"
                            : (pending.status as any);
                        finalMessage = pending.message;

                        setPendingResults((prevPending) => {
                          const copy = { ...prevPending };
                          delete copy[extractedId];
                          pendingResultsRef.current = copy;
                          return copy;
                        });
                      }

                      return {
                        ...msg,
                        text: accumulatedText,
                        trackingId: extractedId,
                        deployStatus: finalStatus,
                        deployMessage: finalMessage,
                      };
                    }
                    return msg;
                  }),
                );
              } else {
                setMessages((prev) =>
                  prev.map((msg) =>
                    msg.id === aiMessageId
                      ? { ...msg, text: accumulatedText }
                      : msg,
                  ),
                );
              }
            } else if (eventData.type === "COMPLETE") {
              setIsLoading(false);
            }
          } catch (e) {
            console.error("[ChatPage] Event Data 파싱 에러:", event.data, e);
          }
        },
        onComplete: () => {
          setIsLoading(false);
          if (!trackingIdExtracted) {
            const match = accumulatedText.match(/DEPLOY-[A-Z0-9]{8}/);
            if (match) {
              const extractedId = match[0];
              const pending = pendingResultsRef.current[extractedId];
              let finalStatus: "RUNNING" | "SUCCESS" | "FAILED" = "RUNNING";
              let finalMessage = "";

              if (pending) {
                finalStatus =
                  pending.status === "RUNNING"
                    ? "SUCCESS"
                    : (pending.status as any);
                finalMessage = pending.message;

                setPendingResults((prevPending) => {
                  const copy = { ...prevPending };
                  delete copy[extractedId];
                  pendingResultsRef.current = copy;
                  return copy;
                });
              }

              setMessages((prev) =>
                prev.map((msg) =>
                  msg.id === aiMessageId
                    ? {
                        ...msg,
                        trackingId: extractedId,
                        deployStatus: finalStatus,
                        deployMessage: finalMessage,
                      }
                    : msg,
                ),
              );
            }
          }
        },
        onError: (error) => {
          console.error("스트리밍 에러 발생:", error);
          setMessages((prev) =>
            prev.map((msg) =>
              msg.id === aiMessageId
                ? {
                    ...msg,
                    text: "죄송합니다. 답변 수신 중 오류가 발생했습니다.",
                  }
                : msg,
            ),
          );
          setIsLoading(false);
        },
      });
    } catch (e) {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-1 flex-col h-screen overflow-hidden bg-[#F5F6F7] dark:bg-[#16171d] transition-colors duration-200">
      {/* 챗 상단 바 (네이버 테마 스타일) */}
      <div className="hidden md:flex h-16 items-center justify-between border-b border-[#E4E8EB] dark:border-[#2e303a] bg-white dark:bg-[#16171d] px-6">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-[#F4F6F8] dark:bg-[#1f2028] text-[#03C75A] border border-[#E4E8EB] dark:border-[#2e303a]">
            <FiCpu className="h-6 w-6" />
          </div>
          <div>
            <h2 className="text-sm font-bold text-[#1E1E1E] dark:text-[#f3f4f6]">
              Ops AI 어시스턴트
            </h2>
          </div>
        </div>
      </div>


      {/* 메시지 영역 */}
      <div className="flex-1 overflow-y-auto p-6 space-y-5">
        {messages.map((msg) => {
          const isAi = msg.sender === "ai";
          return (
            <div
              key={msg.id}
              className={`flex gap-3 max-w-3xl ${isAi ? "" : "ml-auto flex-row-reverse"}`}
            >
              {/* 프로필 이미지 아이콘 */}
              <div
                className={`flex h-9 w-9 items-center justify-center rounded-lg ${
                  isAi
                    ? "bg-[#03C75A] text-white"
                    : "bg-white dark:bg-[#1f2028] border border-[#E4E8EB] dark:border-[#2e303a] text-[#404040] dark:text-slate-300"
                }`}
              >
                {isAi ? (
                  <FiCpu className="h-5 w-5" />
                ) : (
                  <FiUser className="h-5 w-5" />
                )}
              </div>

              {/* 말풍선 */}
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
                {msg.trackingId && (
                  <div
                    className={`mt-2 p-3.5 rounded-lg border transition-all ${
                      msg.deployStatus === "SUCCESS"
                        ? "bg-[#F0FAF5] border-[#D1F2E1] dark:bg-emerald-950/20 dark:border-emerald-900/30 text-[#098243] dark:text-[#a3e635]"
                        : msg.deployStatus === "FAILED"
                          ? "bg-[#FFF0F0] border-[#FCD4D4] dark:bg-rose-950/20 dark:border-rose-900/30 text-[#D83A3A] dark:text-[#f87171]"
                          : "bg-[#FFF9EB] border-[#FFE9C4] dark:bg-amber-950/20 dark:border-amber-900/30 text-[#8F6B00] dark:text-[#fbbf24]"
                    }`}
                  >
                    <div className="flex items-center justify-between font-bold text-xs">
                      <span>배포 추적 ID: {msg.trackingId}</span>
                      <span className="uppercase px-2 py-0.5 rounded-md text-[10px] bg-white dark:bg-[#16171d] border border-[#E4E8EB] dark:border-[#2e303a] text-[#1E1E1E] dark:text-[#f3f4f6]">
                        {msg.deployStatus}
                      </span>
                    </div>
                    <p className="text-xs mt-1.5 font-medium">
                      {msg.deployStatus === "SUCCESS" &&
                        "🟢 " + (msg.deployMessage || "배포 완료")}
                      {msg.deployStatus === "FAILED" &&
                        "🔴 " + (msg.deployMessage || "배포 실패")}
                      {msg.deployStatus === "RUNNING" &&
                        "🟡 kubectl apply 및 Rollout 진행 중..."}
                    </p>
                  </div>
                )}
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
              <FiCpu className="h-5 w-5" />
            </div>
            <div className="flex items-center gap-1.5 rounded-lg bg-white dark:bg-[#1f2028] border border-[#E4E8EB] dark:border-[#2e303a] px-4 py-3">
              <div
                className="h-1.5 w-1.5 rounded-full bg-slate-300 dark:bg-slate-600 animate-bounce"
                style={{ animationDelay: "0ms" }}
              ></div>
              <div
                className="h-1.5 w-1.5 rounded-full bg-slate-300 dark:bg-slate-600 animate-bounce"
                style={{ animationDelay: "150ms" }}
              ></div>
              <div
                className="h-1.5 w-1.5 rounded-full bg-slate-300 dark:bg-slate-600 animate-bounce"
                style={{ animationDelay: "300ms" }}
              ></div>
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
                : "명령어 또는 문의 사항을 입력하세요..."
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
            <FiSend className="h-4.5 w-4.5" />
          </button>
        </div>
      </div>
    </div>
  );
};
