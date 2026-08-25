package com.mcp.mcp_pilot.document.adapter.in.web.mapper;

import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.document.exception.FileException;
import com.mcp.mcp_pilot.document.port.in.dto.UploadDocumentCommand;
import com.mcp.mcp_pilot.document.port.in.dto.UploadFileCommand;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public class DocumentWebMapper {

    public static UploadDocumentCommand toCommand(
            List<MultipartFile> files,
            Long uploadBy
    ) {
        return new UploadDocumentCommand(
                files.stream()
                        .map(file -> toCommand(file, uploadBy))
                        .toList()
        );
    }

    private static UploadFileCommand toCommand(
            MultipartFile file,
            Long uploadBy
    ) {
        try {
            return new UploadFileCommand(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream(),
                    uploadBy
            );
        } catch (IOException e) {
            throw new FileException(ErrorCode.FILE_REQUEST);
        }
    }
}