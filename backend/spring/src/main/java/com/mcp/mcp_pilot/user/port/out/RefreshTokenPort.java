package com.mcp.mcp_pilot.user.port.out;

import java.time.LocalDateTime;

public interface RefreshTokenPort {
    void save(Long userId, String tokenHash, LocalDateTime expiredAt);
    void revoke(String tokenHash);
}
