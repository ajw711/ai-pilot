package com.mcp.mcp_pilot.knowledge.port.out;

import com.mcp.mcp_pilot.knowledge.port.in.dto.ApproveKnowledgeCommand;

public interface ApproveKnowledgeUseCase {
    /**
     * 검토 대기(REVIEW_READY) 중인 지식을 최종 승인하고 Notion/Vector 발행 이벤트를 트리거합니다.
     */
    void approve(ApproveKnowledgeCommand command);
}
