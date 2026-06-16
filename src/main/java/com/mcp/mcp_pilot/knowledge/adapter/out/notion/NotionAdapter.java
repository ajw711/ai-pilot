package com.mcp.mcp_pilot.knowledge.adapter.out.notion;

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

    @Override
    public String publish(KnowledgeLog knowledge) {
        // 노션 api 호출
        return "";
    }
}
