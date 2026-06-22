package com.mcp.mcp_pilot.knowledge.domain.entity;

import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.exception.InvalidKnowledgeStatusException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

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

    public void updateVerificationResult(String formattedContent, int score, String reportJson, KnowledgeStatus status) {
        this.formattedContent = formattedContent;
        this.verificationScore = score;
        this.verificationReport = reportJson;
        this.status = status;
        this.verificationVersion = (this.verificationVersion == null ? 0 : this.verificationVersion) + 1;
    }

    public void approve(String finalFormattedContent) {
        if (this.status != KnowledgeStatus.REVIEW_READY) {
            throw new InvalidKnowledgeStatusException();
        }

        if (finalFormattedContent != null && !finalFormattedContent.isBlank()) {
            this.formattedContent = finalFormattedContent;
        }
        this.status = KnowledgeStatus.APPROVED;
    }

    public void delete(LocalDateTime deleteAt) {
        this.deleteAt = deleteAt;
    }

    public boolean isDeleted() {
        return this.deleteAt != null;
    }
}
