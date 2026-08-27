export type DocumentStatus = 'UPLOADED' | 'PROCESSED' | 'FAILED';

export interface DocumentItem {
  id: number;
  fileName: string;
  contentType: string;
  fileSize: number;
  status: DocumentStatus;
  createdAt: string;
}

export interface UploadDocumentResponse {
  documents: DocumentItem[];
}
