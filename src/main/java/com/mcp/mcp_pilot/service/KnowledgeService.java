package com.mcp.mcp_pilot.service;

import com.mcp.mcp_pilot.repository.KnowledgeLogRepository;
import com.mcp.mcp_pilot.repository.KnowledgeSourceRepository;
import com.mcp.mcp_pilot.repository.KnowledgeTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeLogRepository logRepository;
    private final KnowledgeSourceRepository sourceRepository;
    private final KnowledgeTagRepository tagRepository;

    private final EmbeddingModel embeddingModel;

}
