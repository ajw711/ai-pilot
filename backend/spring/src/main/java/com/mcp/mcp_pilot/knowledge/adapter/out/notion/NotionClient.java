package com.mcp.mcp_pilot.knowledge.adapter.out.notion;

import com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto.NotionPageRequest;
import com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto.NotionPageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class NotionClient {

    private final RestClient restClient;

    public NotionClient(@Qualifier("notionRestClient") RestClient restClient) {
        this.restClient  = restClient;
    }

    public NotionPageResponse createPage(NotionPageRequest request) {
        return restClient.post()
                .uri("/v1/pages")
                .body(request)
                .retrieve()
                .body(NotionPageResponse.class);
    }

}
