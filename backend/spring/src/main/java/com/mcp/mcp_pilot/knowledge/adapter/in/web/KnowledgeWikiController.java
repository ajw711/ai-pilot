package com.mcp.mcp_pilot.knowledge.adapter.in.web;

import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.*;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.mapper.KnowledgeWebMapper;
import com.mcp.mcp_pilot.knowledge.port.in.DeleteKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.SaveKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.SearchKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.dto.ApproveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.in.dto.KnowledgeSummary;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.out.ApproveKnowledgeUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/{version}/knowledge")
@RequiredArgsConstructor
public class KnowledgeWikiController {

    private final SearchKnowledgeUseCase searchKnowledgeUseCase;
    private final SaveKnowledgeUseCase saveKnowledgeUseCase;
    private final ApproveKnowledgeUseCase approveKnowledgeUseCase;
    private final DeleteKnowledgeUseCase deleteKnowledgeUseCase;

    @GetMapping("/list")
    public List<KnowledgeSummary> listAll() {
        log.info("Knowledge listAll (Web Adapter)");
        return searchKnowledgeUseCase.findAll();
    }

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

    /**
     * 지식 수동 검수 후 최종 승인(APPROVED) 및 발행(Notion, Vector) 트리거 API
     */
    @PatchMapping("/approve")
    public void approve(
            @RequestBody(required = false) ApproveRequest request
    ) {
        log.info("지식 수동 승인 요청 수신 (Web Adapter) - ID: {}", request.knowledgeId());
        ApproveKnowledgeCommand command = KnowledgeWebMapper.toCommand(request);
        approveKnowledgeUseCase.approve(command);
    }

    @DeleteMapping("/{knowledgeId}")
    public void delete(@PathVariable Long knowledgeId) {
        log.info("지식 삭제 요청 수신 (Web Adapter) - ID: {}", knowledgeId);
        deleteKnowledgeUseCase.delete(knowledgeId);
    }
}
