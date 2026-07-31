package com.mcp.mcp_pilot.user.port.in;

import com.mcp.mcp_pilot.user.port.in.dto.LoginCommand;
import com.mcp.mcp_pilot.user.port.out.dto.TokenResult;

public interface LoginUseCase {
    TokenResult login(LoginCommand command);
}
