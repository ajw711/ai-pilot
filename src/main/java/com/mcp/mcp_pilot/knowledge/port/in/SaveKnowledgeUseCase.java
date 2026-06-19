package com.mcp.mcp_pilot.knowledge.port.in;

import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;

public interface SaveKnowledgeUseCase {

    /**
     * 지식 원본 및 메타데이터 1차 저장 (이후 비동기 분석 실행)
     */
    KnowledgeLog saveKnowledge(SaveKnowledgeCommand command);
    
}
