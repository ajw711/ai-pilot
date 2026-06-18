package com.mcp.mcp_pilot.knowledge.port.out;

import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.port.out.dto.NotionPublishResult;

public interface NotionPublishPort {
    NotionPublishResult publish(KnowledgeLog knowledge);
}
