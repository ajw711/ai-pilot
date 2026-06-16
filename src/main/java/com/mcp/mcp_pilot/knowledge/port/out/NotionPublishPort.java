package com.mcp.mcp_pilot.knowledge.port.out;

import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;

public interface NotionPublishPort {
    String publish(KnowledgeLog knowledge);
}
