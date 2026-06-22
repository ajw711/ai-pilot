package com.mcp.mcp_pilot.knowledge.port.out;

import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import java.util.List;

public interface KnowledgeSearchPort {
    List<KnowledgeLog> search(String query);
}
