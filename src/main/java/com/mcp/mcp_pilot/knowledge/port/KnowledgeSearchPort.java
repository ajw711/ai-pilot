package com.mcp.mcp_pilot.knowledge.port;

import com.mcp.mcp_pilot.knowledge.entity.KnowledgeLogEntity;

import java.util.List;

public interface KnowledgeSearchPort {

    /**
     * 키워드를 기반으로 지식 검색
     */
    List<KnowledgeLogEntity> search(String query);
}
