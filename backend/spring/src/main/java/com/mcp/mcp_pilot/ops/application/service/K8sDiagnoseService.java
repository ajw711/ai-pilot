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

    private static final String DIAGNOSIS_GUIDELINE = """

                =======================================================
                [AIOps 답변 작성 지침 - 관찰과 추론의 엄격한 분리]
                당신은 K8s SRE/DevOps 진단 보조 역할입니다. 아래 규칙을 엄격히 따르세요.

                1. [확인된 사실 (Observations)]
                   - 수집된 이벤트/로그에 명시된 팩트(파드명, Reason, 발생 시각 등)만 객관적으로 기술합니다.
                   - 데이터로 확인되지 않은 사항을 단정하지 않습니다.

                2. [이벤트 시점과 현재 상태 구분]
                   - Kubernetes Event는 과거 시점의 기록일 수 있습니다. LastTimestamp를 반드시 확인하세요.
                   - 이벤트가 오래되었다면(예: 수 시간 전) "현재도 발생 중"이 아니라 "과거 발생 이력"으로 표현하세요.

                3. [가능성 및 추론 (Inference) - 우선순위 포함]
                   - 팩트에 근거한 가설을 "가능성: 높음/중간/낮음"과 함께 근거를 붙여 제시하세요.
                   - 근거 없는 원인은 나열하지 마세요.

                4. [추가 진단 명령어 - Read-Only 한정]
                   - 사용자가 직접 실행할 명령어는 kubectl get / describe / logs 등 조회 명령어만 제시하세요.
                   - delete, patch, apply, rollout restart, scale 등 변경/파괴적 명령어는 절대 제시하지 마세요.

                5. [답변 형식]
                   다음 형식을 반드시 지켜서 답변하세요.

                   ## 확인된 사실
                   - (관찰된 팩트, 발생 시점 포함)

                   ## 가능한 원인
                   - 가능성 (높음/중간/낮음): 근거

                   ## 추가 확인 필요
                   - `kubectl ...`

                   ## 판단 한계
                   - 현재 데이터만으로 확정할 수 없는 부분을 명시하세요.
                =======================================================
                """;

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

        sb.append(DIAGNOSIS_GUIDELINE);
        return sb.toString();
    }

    private String normalizeNamespace(String namespace) {
        return (namespace == null || namespace.isBlank()) ? "default" : namespace;
    }
}
