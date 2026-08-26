package com.mcp.mcp_pilot.document.port.in.dto;

import com.mcp.mcp_pilot.document.domain.DocumentFile;

import java.io.InputStream;

public record DocumentDownloadResult(
        String fileName,
        String contentType,
        long fileSize,
        InputStream inputStream
) {

    public static DocumentDownloadResult from(DocumentFile documentFile, InputStream inputStream) {
        return new DocumentDownloadResult(
                documentFile.getFileName(),
                documentFile.getContentType(),
                documentFile.getFileSize(),
                inputStream
        );
    }
}

