package com.mcp.mcp_pilot.knowledge.exception;

import com.mcp.mcp_pilot.common.exception.ErrorCode;

public class KnowledgeRetryContentChangeException  extends KnowledgeException {

    public KnowledgeRetryContentChangeException() {
        super(ErrorCode.INVALID_INPUT_VALUE);
    }
}
