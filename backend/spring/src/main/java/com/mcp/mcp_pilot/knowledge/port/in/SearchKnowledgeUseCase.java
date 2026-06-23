package com.mcp.mcp_pilot.knowledge.port.in;

import com.mcp.mcp_pilot.knowledge.port.in.dto.KnowledgeSummary;

import java.util.List;

public interface SearchKnowledgeUseCase {
    String searchWiki(String query);
    List<KnowledgeSummary> findAll();
}
