package com.mcp.mcp_pilot.knowledge.exception;

import com.mcp.mcp_pilot.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class KnowledgeNotFoundException extends KnowledgeException{

    private final Long knowledgeId;

    public KnowledgeNotFoundException(Long knowledgeId) {
        super(ErrorCode.KNOWLEDGE_NOT_FOUND);
        this.knowledgeId = knowledgeId;
    }

}
