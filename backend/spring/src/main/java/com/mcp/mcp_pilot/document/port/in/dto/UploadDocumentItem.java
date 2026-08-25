package com.mcp.mcp_pilot.document.port.in.dto;

public record UploadDocumentItem(
        Long documentFileId,
        String fileName
) {
}
