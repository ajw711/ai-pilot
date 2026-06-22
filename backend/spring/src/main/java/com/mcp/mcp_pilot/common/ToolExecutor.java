package com.mcp.mcp_pilot.common;

public interface ToolExecutor<REQ, RES> {
    RES execute(REQ request);
}
