import { api } from "../../lib/api";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { fetchSseStream, getAccessToken } from "../../lib/api";
import type { SseStreamOptions } from "../../lib/api";
import type { KnowledgeStatus } from "../../types/knowledge";
import type { ApiResponse } from "../../types/api";

export interface KnowledgeSummaryDto {
  id: number;
  title: string;
  status: KnowledgeStatus;
}

//저장 DTO(KnowledgeRequest) 스펙 매핑 인터페이스
export interface SaveKnowledgeRequestDto {
  title: string;
  rawContent: string;
  formattedContent?: string;
  tags: string[]; // Spring Boot @NotNull 대응
  sourceUrls: string[]; // Spring Boot @NotNull 대응
}

export interface ListKnowledgeResponse {
  summaryList: KnowledgeSummaryDto[];
}

//React Query 전용 커스텀 훅 정의
export const useKnowledgeList = () => {
  return useQuery<KnowledgeSummaryDto[]>({
    queryKey: ["knowledgeList"],
    queryFn: async () => {
      const { data: apiResponse } =
        await api.get<ApiResponse<ListKnowledgeResponse>>("/knowledge/list");
      return apiResponse.data?.summaryList || [];
    },
  });
};

export interface KnowledgeDetailDto {
  id: number;
  title: string;
  rawContent: string;
  formattedContent: string;
  verificationScore: number | null;
  verificationReport: string | null;
  status: KnowledgeStatus;
}

export const useKnowledgeDetail = (id: number | null) => {
  return useQuery<KnowledgeDetailDto | null>({
    queryKey: ["knowledgeDetail", id],
    queryFn: async () => {
      if (id === null) return null;
      const { data: apiResponse } = await api.get<
        ApiResponse<KnowledgeDetailDto>
      >(`/knowledge/${id}`);
      return apiResponse.data || null;
    },
    enabled: id !== null,
  });
};

export const useCreateKnowledge = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: SaveKnowledgeRequestDto) => {
      await api.post("/knowledge/save", payload);
    },
    onSuccess: () => {
      // 캐시 무효화(Invalidate)를 통해 목록을 자동으로 조용히 새로고침
      queryClient.invalidateQueries({ queryKey: ["knowledgeList"] });
    },
  });
};

export const useApproveKnowledge = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: {
      knowledgeId: number;
      finalFormattedContent: string;
    }) => {
      await api.patch("/knowledge/approve", payload);
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["knowledgeList"] });
      queryClient.invalidateQueries({
        queryKey: ["knowledgeDetail", variables.knowledgeId],
      });
    },
  });
};

export const useDeleteKnowledge = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/knowledge/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["knowledgeList"] });
    },
  });
};

export const fetchKnowledgeChatStream = async (
  message: string,
  options: SseStreamOptions,
): Promise<void> => {
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";
  const token = getAccessToken();
  const headers: Record<string, string> = {
    Accept: "text/event-stream",
  };
  if (token) headers["Authorization"] = `Bearer ${token}`;
  // GET + query param (SSE는 GET만 지원)
  const url = `${API_BASE_URL}/api/v1/knowledge/chat?message=${encodeURIComponent(message)}`;
  const response = await fetch(url, { method: "GET", headers });
  if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
  // 이하 스트리밍 처리는 동일 — fetchSseStream 내부 로직 재사용 불가하므로 인라인
  const reader = response.body?.getReader();
  const decoder = new TextDecoder("utf-8");
  if (!reader) throw new Error("ReadableStream reader를 사용할 수 없습니다.");
  let lineBuffer = "";
  let currentEventName = "";
  let currentDataBuffer = "";
  let currentId = "";
  const dispatchEvent = () => {
    if (currentDataBuffer) {
      options.onMessage({
        event: currentEventName || "message",
        data: currentDataBuffer.trim(),
        id: currentId,
      });
      currentDataBuffer = "";
      currentEventName = "";
    }
  };
  const processLine = (line: string) => {
    if (line.startsWith(":") || line === "") {
      if (line === "") dispatchEvent();
      return;
    }
    const colonIndex = line.indexOf(":");
    const field = colonIndex !== -1 ? line.substring(0, colonIndex) : line;
    let value = colonIndex !== -1 ? line.substring(colonIndex + 1) : "";
    if (value.startsWith(" ")) value = value.substring(1);
    if (field === "data") currentDataBuffer += value + "\n";
    else if (field === "event") currentEventName = value;
    else if (field === "id") currentId = value;
  };
  try {
    while (true) {
      const { value, done } = await reader.read();
      if (done) {
        dispatchEvent();
        options.onComplete?.();
        break;
      }
      lineBuffer += decoder.decode(value, { stream: true });
      let idx: number;
      while ((idx = lineBuffer.indexOf("\n")) !== -1) {
        processLine(lineBuffer.substring(0, idx).replace(/\r$/, ""));
        lineBuffer = lineBuffer.substring(idx + 1);
      }
    }
  } catch (error) {
    const err = error instanceof Error ? error : new Error(String(error));
    options.onError?.(err);
    throw err;
  }
};
