package com.mcp.mcp_pilot.ai.exception;

import com.mcp.mcp_pilot.common.exception.BusinessException;
import com.mcp.mcp_pilot.common.exception.ErrorCode;

public class AiException extends BusinessException {

    public AiException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AiException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}