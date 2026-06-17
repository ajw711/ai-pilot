package com.mcp.mcp_pilot.knowledge.adapter.out.notion;

import com.mcp.mcp_pilot.knowledge.adapter.out.notion.config.NotionProperties;
import com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto.NotionPageRequest;
import com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto.NotionPageResponse;
import com.mcp.mcp_pilot.knowledge.adapter.out.notion.mapper.NotionMapper;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.port.out.NotionPublishPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotionAdapter implements NotionPublishPort {

    private final NotionClient notionClient;
    private final NotionMapper mapper;
    private final NotionProperties properties;

    @Override
    public String publish(KnowledgeLog knowledge) {
        NotionPageRequest request = mapper.toRequest(knowledge, properties.databaseId());
        log.info("[Notion] 페이지 생성 요청 - knowledgeId={}", knowledge.getId());
        NotionPageResponse response = notionClient.createPage(request);
        return response.id();
    }
}
