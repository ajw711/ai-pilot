package com.mcp.mcp_pilot.knowledge.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 출처/근거 (Domain Entity)
 */
@Getter
@AllArgsConstructor
public class KnowledgeSource {
    private final Long id;
    private final Long knowledgeLogId;
    private final String sourceUrl;

    public static KnowledgeSource create(Long knowledgeLogId, String sourceUrl) {
        return new KnowledgeSource(null, knowledgeLogId, sourceUrl);
    }
}
