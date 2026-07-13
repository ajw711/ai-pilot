export type KnowledgeStatus =
  | 'DRAFT'
  | 'VERIFYING'
  | 'FAILED_AT_VERIFYING'
  | 'FORMATTING'
  | 'FAILED_AT_FORMATTING'
  | 'REVIEW_READY'
  | 'REVIEW_APPROVED'
  | 'NOTION_PUBLISHING'
  | 'FAILED_AT_NOTION_PUBLISH'
  | 'VECTOR_INDEXING'
  | 'FAILED_AT_VECTOR_INDEX'
  | 'PUBLISHED'
  | 'APPROVED'
  | 'PUBLISHING'
  | 'FAILED';

export interface KnowledgeLog {
  id: number;
  title: string;
  rawContent: string;
  formattedContent: string | null;
  createDate: string;
  updateDate: string | null;
  verificationScore: number | null;
  verificationReport: string | null;
  status: KnowledgeStatus;
  verificationVersion: number;
  deleteAt: string | null;
}

export interface KnowledgeRequest {
  title: string;
  rawContent: string;
  formattedContent?: string;
  tags: string[];
  sourceUrls: string[];
}

export interface SaveKnowledgeResponse {
  knowledgeId: number;
}
