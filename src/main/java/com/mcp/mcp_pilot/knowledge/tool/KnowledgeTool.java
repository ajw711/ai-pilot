package com.mcp.mcp_pilot.knowledge.tool;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.service.VectorMemoryService;
import com.mcp.mcp_pilot.common.dto.ToolResponse;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.KnowledgeRequest;
import com.mcp.mcp_pilot.knowledge.adapter.in.mcp.KnowledgeMcpAdapter;
import com.mcp.mcp_pilot.knowledge.port.in.SearchKnowledgeUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP (Model Context Protocol) 통신 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeTool {

    private final KnowledgeMcpAdapter knowledgeMcpAdapter;
    private final SearchKnowledgeUseCase searchKnowledgeUseCase;

    private final VectorMemoryService vectorMemoryService;

    @Tool(
            name = "storeKnowledgeData",
            description = "개발 지식을 저장합니다. 규칙: sourceUrls는 사용자가 직접 제공한 URL만 포함, URL이 없으면 빈 배열([]), URL 추론 금지"
    )
    public ToolResponse<Long> storeKnowledgeData(KnowledgeRequest request) {
        // DB트랜잭션 (Long, Source, Tag 저장)
        ToolResponse<Long> response = knowledgeMcpAdapter.execute(request);

        // 응답 상태 체크
        if (response.isSuccess()) {
           Long knowledgeId = response.data();

            // 별도 스레드(가상 스레드)에서 작업(벡터화) 수행
            Thread.ofVirtual().start(() -> {
                try {
                    log.info("[Distributed-Task] 벡터화 시작. ID: {}", knowledgeId);

                    vectorMemoryService.saveEmbedding(
                            VectorTargetType.KNOWLEDGE,
                            knowledgeId,
                            summarizeAndMerge(request)
                    );
                } catch (Exception e) {
                    log.error("[Distributed-Task] 벡터화 실패. ID: {}: {}", knowledgeId,
                            e.getMessage());
                }
            });
        }
        return response;
    }

    @Tool(
            name = "searchKnowledge",
            description = "내 개인 위키에서 지식을 검색합니다. 제목 키워드나 의미 기반 질문으로 찾을 수 있습니다."
    )
    public ToolResponse<String> searchKnowledge(String query) {
        String result = searchKnowledgeUseCase.searchWiki(query);
        if (result == null || result.contains("관련된 위키 내용을 찾을 수 없습니다.")) {
            return ToolResponse.success("검색 결과가 없습니다.", "[NOT_FOUND]");
        }
        return ToolResponse.success("검색 결과입니다.", result);
    }

    private String summarizeAndMerge(KnowledgeRequest request) {
        return String.format(
                "제목: %s\n태그: %s\n요약: %s",
                request.title(),
                String.join(", ", request.tags()),
                request.summarizedContent()
        );
    }

}
