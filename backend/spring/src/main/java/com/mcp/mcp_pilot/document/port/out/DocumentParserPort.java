package com.mcp.mcp_pilot.document.port.out;

import com.mcp.mcp_pilot.ai.vector.dto.RawChunk;

import java.io.InputStream;
import java.util.List;

public interface DocumentParserPort {
    List<RawChunk> parse(InputStream inputStream, String fileName);
}
