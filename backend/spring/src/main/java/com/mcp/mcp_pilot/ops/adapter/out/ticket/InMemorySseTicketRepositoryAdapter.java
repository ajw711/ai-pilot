package com.mcp.mcp_pilot.ops.adapter.out.ticket;


import com.mcp.mcp_pilot.ops.application.domain.entity.SseTicket;
import com.mcp.mcp_pilot.ops.port.out.SseTicketRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemorySseTicketRepositoryAdapter implements SseTicketRepositoryPort {

    private final ConcurrentHashMap<String, SseTicket> store = new ConcurrentHashMap<>();

    @Override
    public SseTicket save(SseTicket ticket) {
        store.put(ticket.getTicket(), ticket);
        return ticket;
    }

    @Override
    public Optional<SseTicket> findByTicket(String ticket) {
        return Optional.ofNullable(store.get(ticket));
    }

    @Override
    public void delete(String ticket) {
        store.remove(ticket);
    }
}