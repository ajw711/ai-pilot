package com.mcp.mcp_pilot.common.enums;

/**
 * "storeKnowledgeData" McpTool 작성시 오타 발생이 가능함
 * 또한 이름이 바뀌어도 코드 수정이 불필요함
 * 컴파일 에러 체크 가능
 */
public enum ToolType {

    STORE_KNOWLEDGE_DATA("storeKnowledgeData");

    private final String toolName;

    ToolType(String toolName) {
        this.toolName = toolName;
    }

    public String getToolName() {
        return toolName;
    }
}
