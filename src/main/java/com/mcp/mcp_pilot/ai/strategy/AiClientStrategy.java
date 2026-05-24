package com.mcp.mcp_pilot.ai.strategy;

import com.mcp.mcp_pilot.ai.dto.AiRequest;

/**
 *
 * 1. 외부 AI(Gemini) 연동 존재
 * 2. MCP Tool이라는 외부 호출 인터페이스 존재
 * 3. 앞으로 Tool 확장 예정
 * 4. AI 모델 교체 가능성 존재
 * 5. Service 순환 의존 X
 * 6. 외부 시스템 의존성이 큼
 *
 * 만약에 Layered Architecture만 쓰면 생기는 문제가 있음
 * KnowledgeService
 *   ↔ ChatService
 *       ↔ ToolService
 *           ↔ EmbeddingService
 *               ↔ SearchService
 * 즉 Service-to-Service 순환
 * 도메인과 외부 의존성을 분리를 원하기 때문
 *
 *
 * AiClientStrategy는 "내 시스템이 원하는 기능 명세(interface)"
 */
public interface AiClientStrategy {

    String call(AiRequest request);
}
