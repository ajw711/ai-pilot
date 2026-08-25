package com.mcp.mcp_pilot.document.application.event;

public record UploadedDocument(
        Long documentFileId,
        String fileName,
        String r2Key
){
}
