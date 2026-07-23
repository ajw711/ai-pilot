package com.mcp.mcp_pilot.knowledge.tool;

import com.mcp.mcp_pilot.ai.annotation.AiTool;
import com.mcp.mcp_pilot.common.enums.ToolType;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AiTool(ToolType.SEARCH_KNOWLEDGE)
@Component
public class KnowledgeTool {

    @Tool(description = "개인 지식 베이스 검색 (현재 비활성화)")
    public String searchKnowledge(String query) {
        return "검색 기능이 현재 준비 중입니다!";
    }
}
