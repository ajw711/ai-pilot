package com.mcp.mcp_pilot.service;

import com.mcp.mcp_pilot.dto.Knowledge.KnowledgeRequest;
import com.mcp.mcp_pilot.common.ToolExecutor;
import com.mcp.mcp_pilot.dto.chat.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeToolService implements ToolExecutor<KnowledgeRequest, ChatResponse> {

    @Override
    public ChatResponse execute(KnowledgeRequest request) {
        return null;
    }
}
