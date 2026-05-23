package com.mcp.mcp_pilot.knowledge.tool;

import com.mcp.mcp_pilot.common.dto.ToolResponse;
import com.mcp.mcp_pilot.dto.Knowledge.KnowledgeRequest;
import com.mcp.mcp_pilot.knowledge.service.KnowledgeToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

/**
 * MCP (Model Context Protocol) 통신 컴포넌트
 * * @McpTool [통신: LLM ➔ 백엔드 실행]
 * 제미나이가 문맥을 파악해 파라미터 JSON을 쏴주면,
 * 백엔드가 이를 받아 실제 DB에 데이터 적재(CUD) 및 인프라 제어를 수행하는 '실행 스위치'.
 * * @McpResource [통신: LLM ➔ 백엔드 조회]
 * 제미나이가 기억하고 있는 ID(예: 12)를 조합해 동적 URI(knowledge://logs/12)로 요청을 보내면,
 * 백엔드가 DB에서 원문 텍스트를 SELECT하여 LLM에게 반환해주는 '읽기 전용 파이프'.
 * * @McpPrompt [통신: 백엔드 ➔ 프론트엔드 UI ➔ 백엔드 문자열 조립]
 * 백엔드 파라미터 구조를 JSON 명세로 뱉어 프론트엔드가 '입력창 UI'를 자동으로 그리게 하고,
 * 유저가 값을 채우면 이를 받아 최종 프롬프트 문자열(String)을 조립해주는 'UI 메타데이터 템플릿'.
 * * @McpComplete [통신: 프론트엔드 ➔ 백엔드 단발성 API]
 * 사용자가 프론트엔드 입력창에 글자를 칠 때마다 (예: 'k'),
 * 프론트엔드가 백엔드 API를 찔러 추천 단어 리스트(예: ["k8s", "kafka"])를 받아가는 '검색어 자동완성 API'.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeTool {

    private final KnowledgeToolService toolService;

    @McpTool(
            name = "storeKnowledgeData",
            description = """
                    개발 지식을 저장합니다.
                    규칙:
                    - sourceUrls는 사용자가 직접 제공한 URL만 포함
                    - URL이 없으면 빈 배열([])
                    - URL 추론 금지
                    """
    )

    public ToolResponse<Kn> storeKnowledgeData(KnowledgeRequest request) {}

}
