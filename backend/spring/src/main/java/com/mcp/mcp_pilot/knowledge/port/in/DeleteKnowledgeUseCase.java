package com.mcp.mcp_pilot.knowledge.port.in;

import com.mcp.mcp_pilot.knowledge.port.in.dto.DeleteKnowledgeResult;

public interface DeleteKnowledgeUseCase {
    DeleteKnowledgeResult delete(Long knowledgeId);
}
