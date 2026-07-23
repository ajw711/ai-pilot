package com.mcp.mcp_pilot.ops.exception;

import com.mcp.mcp_pilot.common.exception.BusinessException;
import com.mcp.mcp_pilot.common.exception.ErrorCode;

public class DeployPersistenceException extends BusinessException {
    public DeployPersistenceException(Throwable cause) {
        super(ErrorCode.DEPLOY_PERSISTENCE_FAILED, cause);
    }
}