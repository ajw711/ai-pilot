package com.mcp.mcp_pilot.knowledge.port.in;

import com.mcp.mcp_pilot.knowledge.port.in.dto.KnowledgeDetailResult;
import com.mcp.mcp_pilot.knowledge.port.in.dto.KnowledgeSummary;

import java.util.List;
import java.util.Optional;

public interface SearchKnowledgeUseCase {
    String searchWiki(String query);
    List<KnowledgeSummary> findAll();
    Optional<KnowledgeDetailResult> findById(Long id);
}
