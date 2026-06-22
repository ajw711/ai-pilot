package com.mcp.mcp_pilot.knowledge.adapter.out.notion;

import com.mcp.mcp_pilot.knowledge.adapter.out.notion.config.NotionProperties;
import com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto.NotionPageRequest;
import com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto.NotionPageResponse;
import com.mcp.mcp_pilot.knowledge.adapter.out.notion.mapper.NotionMapper;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.port.out.NotionPublishPort;
import com.mcp.mcp_pilot.knowledge.port.out.dto.NotionPublishResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotionAdapter implements NotionPublishPort {

    private final NotionClient notionClient;
    private final NotionMapper mapper;
    private final NotionProperties properties;

    /**
     * 노션 발행은 외부 통신이므로 인프라 계층(Adapter)에서 재시도 정책을 관리.
     * https://medium.com/spring-boot-world/spring-boot-4-and-resilience-533ef0ef3577
     */
    @Override
    @Retryable(
            includes = {Exception.class},
            maxRetries = 3,
            delayString = "1000ms",
            multiplier = 2.0 // 실패할 때마다 대기 시간을 2배씩 늘려가며 다시 시도
    )
    public NotionPublishResult publish(KnowledgeLog knowledge) {
        NotionPageRequest request = mapper.toRequest(knowledge, properties.databaseId());
        log.info("[NotionAdapter] 페이지 생성 요청 - knowledgeId={}", knowledge.getId());
        
        NotionPageResponse response = notionClient.createPage(request);
        return new NotionPublishResult(response.id(), response.url());
    }
}
