package com.mcp.mcp_pilot.ops.port.in.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DeployCommand(
        @JsonPropertyDescription("배포할 애플리케이션의 고유 이름 (예: my-web-service)")
        String appName,
        @JsonPropertyDescription("컨테이너 이미지 이름 (예: nginx, tomcat)")
        String image,
        @JsonPropertyDescription("컨테이너 이미지의 태그 버전 (기본값: latest)")
        String tag,
        @JsonPropertyDescription("실행할 컨테이너 인스턴스의 개수 (기본값: 1)")
        int replicas,
        @JsonPropertyDescription("쿠버네티스 네임스페이스 (기본값: default)")
        String namespace
) {}