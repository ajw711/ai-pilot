import React, { useState, useRef } from "react";
import { toast } from "react-hot-toast";
import {
  FiUploadCloud,
  FiTrash2,
  FiDownload,
  FiFileText,
  FiX,
  FiCheckSquare,
  FiSquare,
} from "react-icons/fi";
import {
  useDocumentList,
  useUploadDocuments,
  useDeleteDocuments,
  downloadDocumentFile,
} from "../features/document/api";
import type { DocumentItem } from "../types/document";

export const DocumentPage: React.FC = () => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [selectedNewFiles, setSelectedNewFiles] = useState<File[]>([]);
  const [checkedIds, setCheckedIds] = useState<number[]>([]);

  const { data: documents = [], isLoading } = useDocumentList();
  const uploadMutation = useUploadDocuments();
  const deleteMutation = useDeleteDocuments();

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const filesArray = Array.from(e.target.files);
      setSelectedNewFiles((prev) => [...prev, ...filesArray]);
    }
  };

  const handleRemoveNewFile = (index: number) => {
    setSelectedNewFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleUpload = async () => {
    if (selectedNewFiles.length === 0) {
      toast.error("업로드할 파일을 선택해 주세요.");
      return;
    }

    try {
      await uploadMutation.mutateAsync(selectedNewFiles);
      toast.success(`${selectedNewFiles.length}개 파일이 업로드되었습니다.`);
      setSelectedNewFiles([]);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    } catch {
      toast.error("파일 업로드 중 오류가 발생했습니다.");
    }
  };

  const handleToggleCheck = (id: number) => {
    setCheckedIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id],
    );
  };

  const handleToggleAll = () => {
    if (checkedIds.length === documents.length) {
      setCheckedIds([]);
    } else {
      setCheckedIds(documents.map((doc) => doc.id));
    }
  };

  const handleDeleteSelected = async () => {
    if (checkedIds.length === 0) {
      toast.error("삭제할 문서를 선택해 주세요.");
      return;
    }

    if (
      !window.confirm(
        `선택한 ${checkedIds.length}개 문서를 삭제하시겠습니까?\n연관된 AI 벡터 데이터도 함께 삭제됩니다.`,
      )
    ) {
      return;
    }

    try {
      await deleteMutation.mutateAsync(checkedIds);
      toast.success("문서가 삭제되었습니다.");
      setCheckedIds([]);
    } catch {
      toast.error("문서 삭제 중 오류가 발생했습니다.");
    }
  };

  const handleDownload = async (doc: DocumentItem) => {
    try {
      await downloadDocumentFile(doc.id, doc.fileName);
    } catch {
      toast.error("파일 다운로드 중 오류가 발생했습니다.");
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  return (
    <div className="max-w-6xl mx-auto p-6 space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
          문서 관리 및 RAG 색인
        </h1>
        <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
          PDF, Word, Markdown 등의 문서를 업로드하면 자동으로 본문이 청킹되어 AI 지식 벡터로 색인됩니다.
        </p>
      </div>

      <div className="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm space-y-4">
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white flex items-center gap-2">
          <FiUploadCloud className="text-blue-500" /> 신규 파일 업로드
        </h2>

        <div
          onClick={() => fileInputRef.current?.click()}
          className="border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-8 text-center cursor-pointer hover:border-blue-500 transition-colors"
        >
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            multiple
            className="hidden"
            accept=".pdf,.doc,.docx,.txt,.md,.json"
          />
          <FiUploadCloud className="mx-auto h-10 w-10 text-gray-400" />
          <p className="mt-2 text-sm text-gray-600 dark:text-gray-300">
            클릭하여 파일을 선택하거나 여러 파일을 드래그하여 놓으세요
          </p>
          <p className="text-xs text-gray-400 mt-1">
            지원 형식: PDF, DOCX, TXT, MD 등
          </p>
        </div>

        {selectedNewFiles.length > 0 && (
          <div className="space-y-2">
            <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
              선택된 파일 목록 ({selectedNewFiles.length}개):
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {selectedNewFiles.map((file, index) => (
                <div
                  key={index}
                  className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg border border-gray-200 dark:border-gray-600 text-sm"
                >
                  <div className="flex items-center gap-2 truncate">
                    <FiFileText className="text-blue-500 shrink-0" />
                    <span className="truncate text-gray-800 dark:text-gray-200">
                      {file.name}
                    </span>
                    <span className="text-xs text-gray-400">
                      ({formatFileSize(file.size)})
                    </span>
                  </div>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleRemoveNewFile(index);
                    }}
                    className="text-gray-400 hover:text-red-500 p-1"
                  >
                    <FiX />
                  </button>
                </div>
              ))}
            </div>

            <div className="flex justify-end pt-2">
              <button
                onClick={handleUpload}
                disabled={uploadMutation.isPending}
                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors disabled:opacity-50 cursor-pointer"
              >
                {uploadMutation.isPending
                  ? "업로드 및 색인 중..."
                  : "선택한 파일 업로드"}
              </button>
            </div>
          </div>
        )}
      </div>

      <div className="bg-white dark:bg-gray-800 p-6 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
            등록된 문서 목록 ({documents.length})
          </h2>
          {checkedIds.length > 0 && (
            <button
              onClick={handleDeleteSelected}
              disabled={deleteMutation.isPending}
              className="flex items-center gap-1.5 px-3 py-1.5 bg-red-500 hover:bg-red-600 text-white text-sm font-medium rounded-lg transition-colors disabled:opacity-50 cursor-pointer"
            >
              <FiTrash2 /> 선택 삭제 ({checkedIds.length})
            </button>
          )}
        </div>

        {isLoading ? (
          <div className="text-center py-12 text-gray-500">
            문서 목록을 불러오는 중...
          </div>
        ) : documents.length === 0 ? (
          <div className="text-center py-12 text-gray-400">
            등록된 문서가 없습니다. 새 문서를 업로드해 보세요.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="text-xs text-gray-500 uppercase bg-gray-50 dark:bg-gray-700">
                <tr>
                  <th className="p-4 w-4">
                    <button
                      onClick={handleToggleAll}
                      className="text-gray-500 hover:text-gray-700 cursor-pointer"
                    >
                      {checkedIds.length === documents.length ? (
                        <FiCheckSquare />
                      ) : (
                        <FiSquare />
                      )}
                    </button>
                  </th>
                  <th className="px-6 py-3">파일명</th>
                  <th className="px-6 py-3">크기</th>
                  <th className="px-6 py-3">상태</th>
                  <th className="px-6 py-3">등록일</th>
                  <th className="px-6 py-3 text-right">작업</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                {documents.map((doc) => (
                  <tr
                    key={doc.id}
                    className="hover:bg-gray-50 dark:hover:bg-gray-750"
                  >
                    <td className="p-4">
                      <button
                        onClick={() => handleToggleCheck(doc.id)}
                        className="text-gray-500 hover:text-gray-700 cursor-pointer"
                      >
                        {checkedIds.includes(doc.id) ? (
                          <FiCheckSquare />
                        ) : (
                          <FiSquare />
                        )}
                      </button>
                    </td>
                    <td className="px-6 py-4 font-medium text-gray-900 dark:text-white flex items-center gap-2">
                      <FiFileText className="text-gray-400 shrink-0" />
                      <span className="truncate max-w-xs">{doc.fileName}</span>
                    </td>
                    <td className="px-6 py-4 text-gray-500">
                      {formatFileSize(doc.fileSize)}
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          doc.status === "UPLOADED"
                            ? "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300"
                            : doc.status === "PROCESSED"
                              ? "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300"
                              : "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300"
                        }`}
                      >
                        {doc.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-gray-500">
                      {new Date(doc.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => handleDownload(doc)}
                        className="text-blue-600 hover:text-blue-800 dark:text-blue-400 p-1 cursor-pointer"
                        title="다운로드"
                      >
                        <FiDownload />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
