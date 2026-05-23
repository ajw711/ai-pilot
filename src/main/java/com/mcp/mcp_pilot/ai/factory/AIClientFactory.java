package com.mcp.mcp_pilot.ai.factory;

import com.mcp.mcp_pilot.ai.enums.AIModel;
import com.mcp.mcp_pilot.ai.strategy.AiClientStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AIClientFactory {

    private final Map<String, AiClientStrategy> strategies;

    public AiClientStrategy get(AIModel aiModel) {
        AiClientStrategy strategy = strategies.get(aiModel.name());

        if (strategy == null) {
            return new IllegalArgumentException("지원하지 않은 AI 모델입니다.");
        }

        return strategy;
    }
}
