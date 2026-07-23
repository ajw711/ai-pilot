package com.mcp.mcp_pilot.ops.application.service;

import com.mcp.mcp_pilot.ai.dto.AiRequest;
import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import com.mcp.mcp_pilot.ai.dto.ChatResponse;
import com.mcp.mcp_pilot.ai.enums.AIModel;
import com.mcp.mcp_pilot.ai.factory.AIClientFactory;
import com.mcp.mcp_pilot.ai.strategy.AiClientStrategy;
import com.mcp.mcp_pilot.common.enums.ToolType;
import com.mcp.mcp_pilot.ops.port.in.PilotChatUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PilotChatService implements PilotChatUseCase {

    private final AIClientFactory aiClientFactory;

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        log.info("[PilotChatService] 운영 비서 Agent 구동");

        // 이 비서는 지식 검색과 배포 액션 툴을 둘 다 장착하여 뇌(Gemini)에 전달합니다.
        AiRequest aiRequest = AiRequest.of(
                chatRequest.message(),
                AIModel.GEMINI,
                List.of(
                        ToolType.SEARCH_KNOWLEDGE,
                        ToolType.DEPLOY_APP  // 배포 툴 추가
                )
        );

        AiClientStrategy strategy = aiClientFactory.get(aiRequest.model());
        String answer = strategy.call(aiRequest);
        return ChatResponse.of(answer);
    }
}
