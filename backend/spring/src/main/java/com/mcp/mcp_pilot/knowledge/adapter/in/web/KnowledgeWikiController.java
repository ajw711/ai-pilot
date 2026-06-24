package com.mcp.mcp_pilot.knowledge.adapter.in.web;

import com.mcp.mcp_pilot.common.dto.ApiResponse;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.*;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.mapper.KnowledgeWebMapper;
import com.mcp.mcp_pilot.knowledge.port.in.DeleteKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.SaveKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.SearchKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.dto.*;
import com.mcp.mcp_pilot.knowledge.port.in.ApproveKnowledgeUseCase;
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

    @GetMapping(path = "/list", version = "v1")
    public ApiResponse<ListKnowledgeResponse> listAll() {
        log.info("Knowledge listAll (Web Adapter)");
        List<KnowledgeSummary> result = searchKnowledgeUseCase.findAll();
        return ApiResponse.success(KnowledgeWebMapper.toResponse(result));
    }

    @GetMapping(path = "/search", version = "v1")
    public SearchResponse search(@RequestParam String query) {
        log.info("Knowledge search request (Web Adapter): {}", query);
        String result = searchKnowledgeUseCase.searchWiki(query);
        return SearchResponse.of(result);
    }

    @PostMapping(path = "/save",  version = "v1")
    public ApiResponse<SaveKnowledgeResponse> save(@Valid @RequestBody KnowledgeRequest request) {
        log.info("지식 저장 요청 수신 (Web Adapter): {}", request.title());
        // 외부 데이터를 내부 command로 변환
        SaveKnowledgeCommand command = KnowledgeWebMapper.toCommand(request);
        SaveKnowledgeResult result = saveKnowledgeUseCase.saveKnowledge(command);
        return ApiResponse.success(SaveKnowledgeResponse.from(result));
    }

    /**
     * 지식 수동 검수 후 최종 승인(APPROVED) 및 발행(Notion, Vector) 트리거 API
     */
    @PatchMapping(path = "/approve", version = "v1")
    public ApiResponse<ApproveResponse> approve(
            @RequestBody(required = false) ApproveRequest request
    ) {
        log.info("지식 수동 승인 요청 수신 (Web Adapter) - ID: {}", request.knowledgeId());
        ApproveKnowledgeCommand command = KnowledgeWebMapper.toCommand(request);
        ApproveKnowledgeResult result = approveKnowledgeUseCase.approve(command);
        return ApiResponse.success(ApproveResponse.from(result));
    }

    @DeleteMapping(path = "/{knowledgeId}", version = "v1")
    public ApiResponse<DeleteKnowledgeResponse> delete(@PathVariable Long knowledgeId) {
        log.info("지식 삭제 요청 수신 (Web Adapter) - ID: {}", knowledgeId);
        DeleteKnowledgeResult result = deleteKnowledgeUseCase.delete(knowledgeId);
        return ApiResponse.success(DeleteKnowledgeResponse.from(result));
    }
}
