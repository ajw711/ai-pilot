package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.dto.AiRequest;
import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import com.mcp.mcp_pilot.ai.dto.ChatResponse;
import com.mcp.mcp_pilot.ai.enums.AIModel;
import com.mcp.mcp_pilot.ai.factory.AIClientFactory;
import com.mcp.mcp_pilot.ai.strategy.AiClientStrategy;
import com.mcp.mcp_pilot.ai.vector.port.VectorSearchPort;
import com.mcp.mcp_pilot.common.enums.ToolType;
import com.mcp.mcp_pilot.knowledge.port.in.KnowledgeChatUseCase;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeChatService implements KnowledgeChatUseCase {

    private final AIClientFactory aiClientFactory;
    private final VectorSearchPort vectorSearchPort;
    private final KnowledgePersistencePort knowledgePersistencePort;

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        log.info("[ChatService] 챗 요청");

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
