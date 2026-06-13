package com.mcp.mcp_pilot.knowledge.port.out;

import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeSource;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeTag;

import java.util.List;
import java.util.Optional;

public interface KnowledgePersistencePort {
    KnowledgeLog save(KnowledgeLog knowledgeLog);
    void saveSources(List<KnowledgeSource> sources);
    void saveTags(List<KnowledgeTag> tags);
    void updateSummary(Long knowledgeId, String summary);
    
    Optional<KnowledgeLog> findById(Long id);
    Optional<KnowledgeLog> findByTitle(String title);
    List<KnowledgeLog> findByTitleContaining(String keyword);
}
