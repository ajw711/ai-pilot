package com.mcp.mcp_pilot.user.port.out.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class TokenRotationResult {
    private final String accessToken;
    private final String refreshToken;
    private final LocalDateTime expiredAt;
}
