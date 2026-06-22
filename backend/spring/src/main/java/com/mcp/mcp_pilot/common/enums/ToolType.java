package com.mcp.mcp_pilot.common.enums;

import lombok.Getter;

/**
 * "storeKnowledgeData" McpTool 작성시 오타 발생이 가능함
 * 또한 이름이 바뀌어도 코드 수정이 불필요함
 * 컴파일 에러 체크 가능
 */
@Getter
public enum ToolType {

    STORE_KNOWLEDGE_DATA("storeKnowledgeData"),
    SEARCH_KNOWLEDGE("searchKnowledge");

    private final String toolName;

    ToolType(String toolName) {
        this.toolName = toolName;
    }
}
