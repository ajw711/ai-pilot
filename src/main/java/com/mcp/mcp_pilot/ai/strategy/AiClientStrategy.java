package com.mcp.mcp_pilot.ai.strategy;

import com.mcp.mcp_pilot.ai.dto.AiRequest;

public interface AiClientStrategy {

    String call(AiRequest request);
}
