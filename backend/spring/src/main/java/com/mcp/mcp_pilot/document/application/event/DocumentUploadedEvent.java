package com.mcp.mcp_pilot.document.application.event;

import com.mcp.mcp_pilot.document.domain.DocumentFile;

import java.util.List;

public record DocumentUploadedEvent(
    List<UploadedDocument> documents
) {

    public static DocumentUploadedEvent of(List<DocumentFile> documents) {
        return new DocumentUploadedEvent(
                documents.stream()
                        .map(document ->
                                new UploadedDocument(document.getId(), document.getFileName(), document.getR2Key()
                                ))
                        .toList()
        );
    }
}