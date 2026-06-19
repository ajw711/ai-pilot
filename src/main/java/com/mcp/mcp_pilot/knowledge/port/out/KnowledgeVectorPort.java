package com.mcp.mcp_pilot.knowledge.port.out;

public interface KnowledgeVectorPort {
    /**
     * 특정 지식 데이터가 벡터 스토어에 임베딩 적재 완료되었는지 확인
     */
    boolean isVectorStored(Long knowledgeId);
}
