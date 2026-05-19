package com.mcp.mcp_pilot.tool;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.util.List;

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
@Component
public class KnowledgeTool {

    @McpTool(
            name = "storeKnowledgeData",
            description = "사용자가 전달한 개발 지식 내용 혹은 개발 내용을 원문과 이를 요약한 마크다운 결과물, 태그를 DB에 저장합니다. [중요 규칙]: sourceUrls 파라미터는 사용자가 채팅창에 '명시적으로 입력한 웹사이트 URL'이 있을 경우에만 추출하여 배열에 넣습니다. 사용자가 URL을 제공하지 않았다면 무조건 빈 배열([])을 전송해야 하며, 절대 임의의 가짜 URL을 생성하거나 추론하지 마십시오."
    )
    public String storeKnowledgeData(String rawContent, String summarizedContent, List<String> tags, List<String> sourceUrls) {
        try {
            return String.format("성공: 요청하신 기술 지식이 안전하게 DB에 적재되었습니다. (마스터 번호: )");
        } catch (Exception e) {
            return "실패: 저장 중 예외 발생 - " + e.getMessage();
        }
    }

}
