package com.mcp.mcp_pilot.knowledge.adapter.out.notion;

import com.mcp.mcp_pilot.knowledge.adapter.out.notion.config.NotionProperties;
import com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto.NotionPageResponse;
import com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto.NotionPublishPayload;
import com.mcp.mcp_pilot.knowledge.adapter.out.notion.mapper.NotionMapper;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.port.out.NotionPublishPort;
import com.mcp.mcp_pilot.knowledge.port.out.dto.NotionPublishResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotionAdapter implements NotionPublishPort {

    private final NotionClient notionClient;
    private final NotionMapper mapper;
    private final NotionProperties properties;

    @Override
    @Retryable(
            includes = {Exception.class},
            maxRetries = 3,
            delayString = "1000ms",
            multiplier = 2.0
    )
    public NotionPublishResult publish(KnowledgeLog knowledge, List<String> tags) {
        NotionPublishPayload payload = mapper.toPublishPayload(knowledge, tags, properties.databaseId());
        log.info("[NotionAdapter] Creating page for knowledgeId: {}", knowledge.getId());
        
        NotionPageResponse response = notionClient.createPage(payload.pageRequest());
        String pageId = response.id();

        if (!payload.remainingChunks().isEmpty()) {
            log.info("[NotionAdapter] Appending remaining content blocks for pageId: {}", pageId);
            for (List<Map<String, Object>> chunk : payload.remainingChunks()) {
                notionClient.appendPageChildren(pageId, chunk);
            }
        }

        return new NotionPublishResult(response.id(), response.url());
    }
}
