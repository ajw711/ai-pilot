package com.mcp.mcp_pilot.ai.factory;

import com.mcp.mcp_pilot.ai.enums.AIModel;
import com.mcp.mcp_pilot.ai.strategy.AiClientStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adapter 선택기
 * 어떤 Adapter를 사용할지 결
 */
@Component
@RequiredArgsConstructor
public class AIClientFactory {

    private final Map<String, AiClientStrategy> strategies;

    public AiClientStrategy get(AIModel model) {
        AiClientStrategy strategy = strategies.get(model.name());

        if (strategy == null) {
            throw  new IllegalArgumentException("지원하지 않은 AI 모델입니다.");
        }

        return strategy;
    }
}
