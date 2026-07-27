package com.mcp.mcp_pilot.ops.adapter.in.web.dto;

public record ChatEvent(
        EventType type,
        String message
) {

    public static ChatEvent token(String text) {
        return new ChatEvent(EventType.TOKEN, text);
    }

    public static ChatEvent status(String statusMsg) {
        return new ChatEvent(EventType.STATUS, statusMsg);
    }

    public static ChatEvent error(String errorMsg) {
        return new ChatEvent(EventType.ERROR, errorMsg);
    }
}
