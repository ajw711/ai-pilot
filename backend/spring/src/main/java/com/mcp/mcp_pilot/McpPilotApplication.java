package com.mcp.mcp_pilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableJpaAuditing
@ConfigurationPropertiesScan
@EnableConfigurationProperties
@SpringBootApplication
public class McpPilotApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpPilotApplication.class, args);
	}

}
