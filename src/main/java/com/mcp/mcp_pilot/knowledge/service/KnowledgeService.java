package com.mcp.mcp_pilot.knowledge.service;

import com.mcp.mcp_pilot.ai.dto.AiRequest;
import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import com.mcp.mcp_pilot.ai.dto.ChatResponse;
import com.mcp.mcp_pilot.ai.enums.AIModel;
import com.mcp.mcp_pilot.ai.factory.AIClientFactory;
import com.mcp.mcp_pilot.ai.strategy.AiClientStrategy;
import com.mcp.mcp_pilot.common.enums.ToolType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final AIClientFactory aiClientFactory;

    /**
     * 메인 챗 에이전트 사령탑(AI 채팅)
     */
    public ChatResponse chat(ChatRequest chatRequest) {

        log.info("Knowledge chat request!!");

        AiRequest aiRequest = AiRequest.of(
                chatRequest.message(),
                AIModel.GEMINI,
                List.of(
                        ToolType.STORE_KNOWLEDGE_DATA,
                        ToolType.SEARCH_KNOWLEDGE
                )
        );
        AiClientStrategy strategy = aiClientFactory.get(aiRequest.model());

        String answer = strategy.call(aiRequest);
        return ChatResponse.of(answer);
    }
}
