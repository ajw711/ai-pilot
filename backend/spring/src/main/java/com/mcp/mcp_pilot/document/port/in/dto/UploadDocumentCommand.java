package com.mcp.mcp_pilot.document.port.in.dto;

import java.util.List;

public record UploadDocumentCommand(
        List<UploadFileCommand> files
) {

}
