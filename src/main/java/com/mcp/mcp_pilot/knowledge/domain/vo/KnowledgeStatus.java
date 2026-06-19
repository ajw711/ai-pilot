package com.mcp.mcp_pilot.knowledge.domain.vo;

/**
 * 파이프라인 전 과정(AI 검수/포맷팅/퍼블리싱)의 상태를 추적하기 위한 세부 단계
 */
public enum KnowledgeStatus {
    DRAFT,
    VERIFYING,
    FORMATTING,
    REVIEW_READY,
    APPROVED,
    PUBLISHING,
    PUBLISHED,
    FAILED
}
