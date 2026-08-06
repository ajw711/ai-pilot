package com.mcp.mcp_pilot.knowledge.port.out;

public interface KnowledgeEventPublishPort {
    void publish(String eventType, Long knowledgeId);
}
