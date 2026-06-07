package com.mcp.mcp_pilot.knowledge.controller;

import com.mcp.mcp_pilot.knowledge.dto.SearchResponse;
import com.mcp.mcp_pilot.knowledge.service.KnowledgeSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/{version}/knowledge")
@RequiredArgsConstructor
public class KnowledgeSearchController {

    private final KnowledgeSearchService knowledgeSearchService;

    @GetMapping("/search")
    public SearchResponse search(@RequestParam String query) {
        String result = knowledgeSearchService.searchWiki(query);
        return SearchResponse.of(result);
    }
}
