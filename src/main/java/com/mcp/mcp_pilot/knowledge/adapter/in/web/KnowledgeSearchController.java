package com.mcp.mcp_pilot.knowledge.adapter.in.web;

import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.SearchResponse;
import com.mcp.mcp_pilot.knowledge.port.in.SearchKnowledgeUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/{version}/knowledge")
@RequiredArgsConstructor
public class KnowledgeSearchController {

    private final SearchKnowledgeUseCase searchKnowledgeUseCase;

    @GetMapping("/search")
    public SearchResponse search(@RequestParam String query) {
        log.info("Knowledge search request (Web Adapter): {}", query);
        String result = searchKnowledgeUseCase.searchWiki(query);
        return SearchResponse.of(result);
    }
}
