package com.mcp.mcp_pilot.knowledge.domain.vo;

/**
 * 파이프라인 전 과정(AI 검수/포맷팅/퍼블리싱)의 상태를 추적하기 위한 세부 단계
 */
public enum KnowledgeStatus {
    /** 최초 등록 상태 (초안) */
    DRAFT,

    /** AI를 통한 팩트체크 및 검수가 진행 중인 상태 */
    VERIFYING,

    /** AI 검수 도중 장애가 발생한 상태 (재시도 대상) */
    FAILED_AT_VERIFYING,

    /** AI 검수 완료 후 본문을 마크다운 형식으로 가공(포맷팅) 중인 상태 */
    FORMATTING,

    /** AI 포맷팅 가공 도중 장애가 발생한 상태 (재시도 대상) */
    FAILED_AT_FORMATTING,

    /** AI 가공이 끝나 사용자의 최종 확인 및 승인을 기다리는 대기 상태 */
    REVIEW_READY,

    /** 사용자가 최종 승인하여 외부 채널 발행 이벤트를 대기 중인 상태 (도메인 승인 완료) */
    REVIEW_APPROVED,

    /** Notion API 발행이 진행 중인 상태 */
    NOTION_PUBLISHING,

    /** Notion API 발행 도중 장애가 발생한 상태 (노션 재시도 대상) */
    FAILED_AT_NOTION_PUBLISH,

    /** Notion 발행은 성공했고, 벡터 임베딩 생성 및 DB 적재가 진행 중인 상태 */
    VECTOR_INDEXING,

    /** 벡터 적재 도중 장애가 발생한 상태 (벡터 재시도 대상) */
    FAILED_AT_VECTOR_INDEX,

    /** Notion 발행 및 벡터 DB 적재가 모두 최종 성공하여 활성화된 완료 상태 */
    PUBLISHED
}
