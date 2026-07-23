package com.mcp.mcp_pilot.ops.port.in.dto;


public enum DeploymentStatus {
    REQUESTED,   // DB 저장 완료, 아직 NATS 발행 전

    PUBLISHED,   // NATS 발행 완료

    DEPLOYING,   // Go Agent가 처리 중

    RUNNING,     // K8s 배포 완료

    FAILED       // 최종 실패
}
