package com.mcp.mcp_pilot.knowledge.application.event;

import java.time.Instant;

public record KnowledgeProcessedEvent(
        Long knowledgeId,
        Instant publishedAt) {

    public static KnowledgeProcessedEvent of(Long knowledgeId) {
        return new KnowledgeProcessedEvent(knowledgeId, Instant.now());
    }
}
