import { api } from "../../lib/api";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import type { KnowledgeStatus } from "../../types/Knowledge";
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
