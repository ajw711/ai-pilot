package com.mcp.mcp_pilot.knowledge.port.in;

import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;

public interface VectorUseCase {
    void execute(KnowledgeProcessedEvent event);
}
