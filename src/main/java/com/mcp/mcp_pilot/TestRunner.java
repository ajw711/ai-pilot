package com.mcp.mcp_pilot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestRunner {

    private final EmbeddingModel embeddingModel;

    @PostConstruct
    public void test() {
        log.info("Embedding Model = {}", embeddingModel);
    }
}