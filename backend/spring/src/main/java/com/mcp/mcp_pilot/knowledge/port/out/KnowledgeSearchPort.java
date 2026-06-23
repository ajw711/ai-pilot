package com.mcp.mcp_pilot.knowledge.port.out;

import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import java.util.List;
import java.util.Optional;

public interface KnowledgeSearchPort {
    List<KnowledgeLog> search(String query);
    List<KnowledgeLog> findAll();
    Optional<KnowledgeLog> findById(Long id);
    Optional<KnowledgeLog> findByTitle(String title);
    List<KnowledgeLog> findByTitleContaining(String keyword);
}
