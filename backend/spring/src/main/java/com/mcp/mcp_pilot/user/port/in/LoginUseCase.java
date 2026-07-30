package com.mcp.mcp_pilot.user.port.in;

import com.mcp.mcp_pilot.user.port.in.dto.LoginCommand;
import com.mcp.mcp_pilot.user.port.in.dto.LoginResult;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
