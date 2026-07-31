package com.mcp.mcp_pilot.user.exception;

import com.mcp.mcp_pilot.common.exception.BusinessException;
import com.mcp.mcp_pilot.common.exception.ErrorCode;

public class UserException extends BusinessException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
