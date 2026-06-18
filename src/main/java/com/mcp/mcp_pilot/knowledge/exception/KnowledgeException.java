package com.mcp.mcp_pilot.knowledge.exception;

import com.mcp.mcp_pilot.common.exception.BusinessException;
import com.mcp.mcp_pilot.common.exception.ErrorCode;

public abstract class KnowledgeException extends BusinessException {

    protected KnowledgeException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected KnowledgeException(ErrorCode errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }  
}
