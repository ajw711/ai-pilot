package com.mcp.mcp_pilot.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean(destroyMethod = "close")
    public ExecutorService wikiExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
