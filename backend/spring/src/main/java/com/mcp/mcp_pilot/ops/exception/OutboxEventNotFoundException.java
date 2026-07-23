package com.mcp.mcp_pilot.ops.exception;

import com.mcp.mcp_pilot.common.exception.BusinessException;
import com.mcp.mcp_pilot.common.exception.ErrorCode;

public class OutboxEventNotFoundException extends BusinessException {
    public OutboxEventNotFoundException() {
        super(ErrorCode.OUTBOX_EVENT_NOT_FOUND);
    }
}