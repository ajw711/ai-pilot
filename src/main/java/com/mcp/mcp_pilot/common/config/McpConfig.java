package com.mcp.mcp_pilot.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    ChatClient chatClient(ChatModel chatModel, SyncMcpToolCallbackProvider syncMcpToolCallbackProvider) {
        return ChatClient
                .builder(chatModel)
                .defaultToolCallbacks(syncMcpToolCallbackProvider)
                .build();
    }
}
