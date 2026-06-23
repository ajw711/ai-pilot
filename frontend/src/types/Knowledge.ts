export type KnowledgeStatus =
  | 'DRAFT'
  | 'VERIFYING'
  | 'FORMATTING'
  | 'REVIEW_READY'
  | 'APPROVED'
  | 'PUBLISHING'
  | 'PUBLISHED'
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
