package com.mcp.mcp_pilot.ops.adapter.out.ticket;

import com.mcp.mcp_pilot.ops.port.out.SseTicketGeneratorPort;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecureRandomSseTicketGenerator implements SseTicketGeneratorPort {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        byte[] bytes = new byte[32]; // 256bit
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
