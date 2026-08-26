package com.mcp.mcp_pilot.document.adapter.in.web.dto;

import com.mcp.mcp_pilot.document.domain.vo.DocumentStatus;
import com.mcp.mcp_pilot.document.port.in.dto.DocumentResult;

import java.time.LocalDateTime;
import java.util.List;

public record DocumentResponse(
        Long id,
        String fileName,
        String contentType,
        long fileSize,
        DocumentStatus status,
        LocalDateTime createdAt
) {
    public static DocumentResponse from(DocumentResult result) {
        return new DocumentResponse(
                result.id(),
                result.fileName(),
                result.contentType(),
                result.fileSize(),
                result.status(),
                result.createdAt()
        );
    }
}
