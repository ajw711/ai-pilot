package com.mcp.mcp_pilot.knowledge.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 기술 태그 (Domain Entity)
 */
@Getter
@AllArgsConstructor
public class KnowledgeTag {
    private final Long id;
    private final Long knowledgeLogId;
    private final String tagName;

    public static KnowledgeTag create(Long knowledgeLogId, String tagName) {
        return new KnowledgeTag(null, knowledgeLogId, tagName.trim().toLowerCase());
    }
}
