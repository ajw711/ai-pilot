package com.mcp.mcp_pilot.document.domain;

import com.mcp.mcp_pilot.document.domain.vo.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
public class DocumentFile {
    private final Long id;
    private final String fileName;
    private final String contentType;
    private final Long fileSize;
    private final String r2Key;
    private DocumentStatus documentStatus;
    private final LocalDateTime createDate;
    private final LocalDateTime updateDate;
    private Long uploadedBy;
    private LocalDateTime deleteAt;

    public static DocumentFile create(
            String fileName,
            String contentType,
            Long fileSize,
            String r2Key,
            DocumentStatus documentStatus,
            Long uploadedBy
    ) {
        return new DocumentFile(
                null,
                fileName,
                contentType,
                fileSize,
                r2Key,
                documentStatus,
                null,
                null,
                uploadedBy,
                null
        );
    }

    public void delete(LocalDateTime deleteAt) {
        this.deleteAt = deleteAt;
    }

}
