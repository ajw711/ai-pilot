package com.mcp.mcp_pilot.document.port.in.dto;

import com.mcp.mcp_pilot.document.domain.DocumentFile;
import com.mcp.mcp_pilot.document.domain.vo.DocumentStatus;

import java.time.LocalDateTime;

public record DocumentResult(
        Long id,
        String fileName,
        String contentType,
        long fileSize,
        DocumentStatus status,
        LocalDateTime createdAt
) {
    public static DocumentResult from(DocumentFile doc) {
        return new DocumentResult(
                doc.getId(),
                doc.getFileName(),
                doc.getContentType(),
                doc.getFileSize(),
                doc.getDocumentStatus(),
                doc.getCreateDate()
        );
    }
}