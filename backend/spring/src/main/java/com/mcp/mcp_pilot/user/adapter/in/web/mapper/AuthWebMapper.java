package com.mcp.mcp_pilot.user.adapter.in.web.mapper;

import com.mcp.mcp_pilot.user.adapter.in.web.dto.LoginRequest;
import com.mcp.mcp_pilot.user.port.in.dto.LoginCommand;

public class AuthWebMapper {

    public static LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(
                request.username(),
                request.password()
        );
    }
}
