import { api } from "../../lib/api";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import type { ApiResponse } from "../../types/api";
import type { DocumentItem, UploadDocumentResponse } from "../../types/document";

export const useDocumentList = () => {
  return useQuery<DocumentItem[]>({
    queryKey: ["documentList"],
    queryFn: async () => {
      const { data: apiResponse } =
        await api.get<ApiResponse<DocumentItem[]>>("/document");
      return apiResponse.data || [];
    },
  });
};

export const useUploadDocuments = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (files: File[]) => {
      const formData = new FormData();
      files.forEach((file) => {
        formData.append("files", file);
      });

      const { data: apiResponse } = await api.post<
        ApiResponse<UploadDocumentResponse>
      >("/document/upload", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });
      return apiResponse.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["documentList"] });
    },
  });
};

export const useDeleteDocuments = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (ids: number[]) => {
      const params = new URLSearchParams();
      ids.forEach((id) => params.append("ids", id.toString()));
      await api.delete(`/document?${params.toString()}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["documentList"] });
    },
  });
};

export const downloadDocumentFile = async (id: number, fileName: string) => {
  const response = await api.get(`/document/${id}/download`, {
    responseType: "blob",
  });

  const blob = new Blob([response.data]);
  const downloadUrl = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = downloadUrl;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(downloadUrl);
};
