package com.mcp.mcp_pilot.document.port.in.dto;

import java.io.InputStream;

public record UploadFileCommand(
        String fileName,
        String contentType,
        long fileSize,
        InputStream inputStream,
        Long uploadBy
) {
}
