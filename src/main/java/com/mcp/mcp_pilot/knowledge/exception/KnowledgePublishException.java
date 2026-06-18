package com.mcp.mcp_pilot.knowledge.exception;

import com.mcp.mcp_pilot.common.exception.ErrorCode;

public class KnowledgePublishException  extends KnowledgeException {
    public KnowledgePublishException (Throwable cause) {
        super(ErrorCode.KNOWLEDGE_PUBLISH_FAILURE, cause);
    }
}
