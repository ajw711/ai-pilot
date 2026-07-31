package com.mcp.mcp_pilot.user.port.in;

import com.mcp.mcp_pilot.user.port.out.dto.TokenRotationResult;

public interface TokenRefreshUseCase {
    TokenRotationResult rotate(String refreshToken);
}
