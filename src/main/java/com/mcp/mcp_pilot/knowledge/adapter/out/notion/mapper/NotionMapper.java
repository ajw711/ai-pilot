package com.mcp.mcp_pilot.knowledge.adapter.out.notion.mapper;

import com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto.NotionPageRequest;
import com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto.Parent;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class NotionMapper {

    public NotionPageRequest toRequest(
            KnowledgeLog log,
            String databaseId
    ) {

        Map<String, Object> properties = Map.of(
                "Title",
                Map.of(
                        "title",
                        List.of(
                                Map.of(
                                        "text",
                                        Map.of(
                                                "content",
                                                log.getTitle()
                                        )
                                )
                        )
                )
        );

        return new NotionPageRequest(
                new Parent(databaseId),
                properties
        );
    }
}
