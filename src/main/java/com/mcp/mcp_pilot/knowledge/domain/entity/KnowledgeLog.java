package com.mcp.mcp_pilot.knowledge.domain.entity;

import lombok.AllArgsConstructor;
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
    private String summarizedContent;
    private final LocalDateTime createDate;
    private final LocalDateTime updateDate;

    public static KnowledgeLog create(String title, String rawContent, String summarizedContent) {
        return new KnowledgeLog(null, title, rawContent, summarizedContent, null, null);
    }
}
