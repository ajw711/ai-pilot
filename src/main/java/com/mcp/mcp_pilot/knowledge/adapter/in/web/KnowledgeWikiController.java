package com.mcp.mcp_pilot.knowledge.adapter.in.web;

import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.KnowledgeRequest;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.SaveKnowledgeResponse;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.SearchResponse;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.mapper.KnowledgeWebMapper;
import com.mcp.mcp_pilot.knowledge.port.in.SaveKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.SearchKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/{version}/knowledge")
@RequiredArgsConstructor
public class KnowledgeWikiController {

    private final SearchKnowledgeUseCase searchKnowledgeUseCase;
    private final SaveKnowledgeUseCase saveKnowledgeUseCase;

    @GetMapping("/search")
    public SearchResponse search(@RequestParam String query) {
        log.info("Knowledge search request (Web Adapter): {}", query);
        String result = searchKnowledgeUseCase.searchWiki(query);
        return SearchResponse.of(result);
    }

    @PostMapping("/save")
    public SaveKnowledgeResponse save(@Valid @RequestBody KnowledgeRequest request) {
        log.info("지식 저장 요청 수신 (Web Adapter): {}", request.title());
        // 외부 데이터를 내부 command로 변환
        SaveKnowledgeCommand command = KnowledgeWebMapper.toCommand(request);
        Long knowledgeId =
                saveKnowledgeUseCase
                        .saveKnowledge(command)
                        .getId();
        return KnowledgeWebMapper.toResponse(knowledgeId);
    }
}
