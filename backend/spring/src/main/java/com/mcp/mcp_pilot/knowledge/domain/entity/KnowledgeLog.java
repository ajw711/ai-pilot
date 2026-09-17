package com.mcp.mcp_pilot.knowledge.domain.entity;

import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.exception.InvalidKnowledgeStatusException;
import com.mcp.mcp_pilot.knowledge.exception.KnowledgeRetryContentChangeException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 지식 원문 및 요약 (Domain Entity)
 * 핵심 비즈니스 로직을 포함하며 외부 기술(JPA 등)에 의존하지 않음.
 */
@Getter
@AllArgsConstructor
public class KnowledgeLog {
    private final Long id;
    private final String title;
    private final String rawContent;
    private String formattedContent;
    private final LocalDateTime createDate;
    private final LocalDateTime updateDate;
    private Integer verificationScore;
    private String verificationReport;
    private KnowledgeStatus status;
    private Integer verificationVersion;
    private LocalDateTime deleteAt;

    public static KnowledgeLog create(String title, String rawContent, String formattedContent) {
        return new KnowledgeLog(null, title, rawContent, formattedContent, null, null, null, null, KnowledgeStatus.DRAFT, 0, null);
    }

    public boolean isReviewReady() {
        return this.status == KnowledgeStatus.REVIEW_READY;
    }

    public void approve(String finalFormattedContent) {
        if (!isReviewReady()) {
            throw new InvalidKnowledgeStatusException();
        }

        if (finalFormattedContent != null && !finalFormattedContent.isBlank()) {
            this.formattedContent = finalFormattedContent;
        }
        this.status = KnowledgeStatus.REVIEW_APPROVED;
    }

    // 재시도에서는 상태와 본문을 변경하지 않음
    public void validateRetry(String requestedContent) {
        if (this.status != KnowledgeStatus.FAILED_AT_NOTION_PUBLISH &&
        this.status != KnowledgeStatus.FAILED_AT_VECTOR_INDEX) {
            throw  new InvalidKnowledgeStatusException();
        }
        // 재시도 요청에서 변경된 본문을 조용히 무시하지 않도록 검증
        if (requestedContent != null && !requestedContent.isBlank() && !Objects.equals(requestedContent, this.formattedContent)) {
            throw new KnowledgeRetryContentChangeException();
        }
    }

    public void delete(LocalDateTime deleteAt) {
        this.deleteAt = deleteAt;
    }
}
