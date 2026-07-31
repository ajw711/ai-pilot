package com.mcp.mcp_pilot.user.port.in;

import com.mcp.mcp_pilot.user.port.in.dto.LogoutCommand;

public interface LogoutUseCase {
    void logout(LogoutCommand command);
}
