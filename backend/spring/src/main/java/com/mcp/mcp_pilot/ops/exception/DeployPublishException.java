package com.mcp.mcp_pilot.ops.exception;

import com.mcp.mcp_pilot.common.exception.BusinessException;
import com.mcp.mcp_pilot.common.exception.ErrorCode;

public class DeployPublishException extends BusinessException {
    
    public DeployPublishException(Throwable cause) {
        super(ErrorCode.DEPLOY_PUBLISH_FAILED, cause);
    }

}
