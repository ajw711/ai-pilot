package com.mcp.mcp_pilot.document.port.out;

import com.mcp.mcp_pilot.document.port.in.dto.UploadFileCommand;

import java.io.InputStream;

public interface FileStoragePort {
    String upload(UploadFileCommand file);

    InputStream download(String r2Key);
}
