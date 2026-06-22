package com.mcp.mcp_pilot.common.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI springOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot 4 REST API")
                        .version("1.0.0")
                        .description("API Documentation for Spring Boot 4 Application"));

    }
}
