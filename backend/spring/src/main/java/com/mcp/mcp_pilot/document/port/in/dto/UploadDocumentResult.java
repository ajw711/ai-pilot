package com.mcp.mcp_pilot.document.port.in.dto;

import com.mcp.mcp_pilot.document.domain.DocumentFile;

import java.util.List;

public record UploadDocumentResult(
        List<UploadDocumentItem> files
) {

    public static UploadDocumentResult from(List<DocumentFile> documentFiles) {
        return new UploadDocumentResult(
                documentFiles.stream()
                        .map(document -> new UploadDocumentItem(
                                    document.getId(),
                                    document.getFileName()
                            )).toList()
        );
    }
}
