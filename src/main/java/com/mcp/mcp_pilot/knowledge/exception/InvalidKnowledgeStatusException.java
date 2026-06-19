package com.mcp.mcp_pilot.knowledge.exception;

import com.mcp.mcp_pilot.common.exception.ErrorCode;

public class InvalidKnowledgeStatusException extends KnowledgeException {
    public InvalidKnowledgeStatusException() {
        super(ErrorCode.INVALID_KNOWLEDGE_STATUS);
    }
}
