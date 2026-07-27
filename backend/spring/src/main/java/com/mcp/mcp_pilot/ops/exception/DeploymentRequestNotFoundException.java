package com.mcp.mcp_pilot.ops.exception;

import com.mcp.mcp_pilot.common.exception.BusinessException;
import com.mcp.mcp_pilot.common.exception.ErrorCode;

public class DeploymentRequestNotFoundException extends BusinessException {
    public DeploymentRequestNotFoundException() {
        super(ErrorCode.DEPLOYMENT_REQUEST_NOT_FOUND);
    }
}