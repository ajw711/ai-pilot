package com.mcp.mcp_pilot.ops.port.out;

import com.mcp.mcp_pilot.ops.application.domain.entity.SseTicket;

import java.util.Optional;

public interface SseTicketRepositoryPort  {
    SseTicket save(SseTicket ticket);
    Optional<SseTicket> findByTicket(String ticket);
    void delete(String ticket);

}
