package com.mcp.mcp_pilot.ops.application.service;


import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.ops.adapter.out.nats.dto.DiagnoseRequest;
import com.mcp.mcp_pilot.ops.port.in.K8sDiagnoseUseCase;
import com.mcp.mcp_pilot.ops.port.in.dto.DiagnoseCommand;
import com.mcp.mcp_pilot.ops.port.in.dto.DiagnoseResult;
import com.mcp.mcp_pilot.ops.port.out.DiagnosePort;
import com.mcp.mcp_pilot.ops.port.out.UserAuthorizationPort;
import com.mcp.mcp_pilot.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class K8sDiagnoseService implements K8sDiagnoseUseCase {

    private final DiagnosePort diagnosePort;
    private final UserAuthorizationPort userAuthorizationPort;

    @Override
    public String diagnose(DiagnoseCommand command, Long requestedBy) {
        // 인가 검증
        if (!userAuthorizationPort.canDeploy(requestedBy)) {
            log.warn("[K8sDiagnoseService] 진단 권한 없음 - UserId: {}", requestedBy);
            throw new UserException(ErrorCode.UNAUTHORIZED_USER);
        }

        String trackingId = "DIAGNOSE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[K8sDiagnoseService] 진단 프로세스 가동 - TrackingID: {}, RequestedBy: {}", trackingId, requestedBy);

        DiagnoseRequest request = new DiagnoseRequest(
                trackingId,
                normalizeNamespace(command.namespace()),
                command.podName() == null ? "" : command.podName(),
                requestedBy
        );

        DiagnoseResult result = diagnosePort.requestDiagnose(request);
        return switch (result.status()) {
            case "TIMEOUT" -> "현재 클러스터 응답이 지연되고 있습니다. 클러스터 API 상태를 확인 중이니 잠시 후 다시 시도해 주세요.";
            case "FAILED" -> "클러스터 진단 중 오류가 발생했습니다: " + result.message();
            default -> formatDiagnosisPrompt(result);
        };
    }

    private String formatDiagnosisPrompt(DiagnoseResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("진단 데이터 수집 결과 (TrackingId: %s):%n", result.trackingId()));
        sb.append(String.format("- 대상 네임스페이스: %s%n", result.namespace()));
        sb.append(String.format("- 수집된 파드 목록: %s%n%n", result.pods()));

        sb.append("=== K8s Warning 이벤트 (최신순) ===\n");
        if (result.warningEvents() == null || result.warningEvents().isEmpty()) {
            sb.append("특이 Warning 이벤트 없음.\n");
        } else {
            result.warningEvents().forEach(event -> sb.append(event).append("\n"));
        }

        sb.append("\n=== 파드 로그 (최근 200줄 / 64KB 제한) ===\n");
        if (result.logs() == null || result.logs().isBlank()) {
            sb.append("수집된 파드 로그가 없거나 정상 작동 중입니다.\n");
        } else {
            sb.append(result.logs());
        }

        sb.append("\n위 텔레메트리 데이터를 바탕으로, 장애 원인과 권장 해결책을 유저에게 명확히 설명해 주세요.");
        return sb.toString();
    }

    private String normalizeNamespace(String namespace) {
        return (namespace == null || namespace.isBlank()) ? "default" : namespace;
    }
}
