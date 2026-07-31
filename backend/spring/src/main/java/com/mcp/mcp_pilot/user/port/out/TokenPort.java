package com.mcp.mcp_pilot.user.port.out;

import com.mcp.mcp_pilot.user.domain.entitiy.User;
import com.mcp.mcp_pilot.user.port.out.dto.TokenResult;

public interface TokenPort {
    TokenResult generateTokens(User user);
}
