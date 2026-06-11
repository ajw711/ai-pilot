package com.mcp.mcp_pilot.knowledge.port.in;

import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;

public interface SaveKnowledgeUseCase {
    KnowledgeLog saveKnowledge(SaveKnowledgeCommand command);
}
