package com.mcp.mcp_pilot.knowledge.port.out;

import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.port.out.dto.NotionPublishResult;
import java.util.List;

public interface NotionPublishPort {
    NotionPublishResult publish(KnowledgeLog knowledge, List<String> tags);
}
