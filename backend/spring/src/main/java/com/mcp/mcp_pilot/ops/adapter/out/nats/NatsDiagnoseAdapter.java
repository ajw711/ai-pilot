package com.mcp.mcp_pilot.ops.adapter.out.nats;

import com.mcp.mcp_pilot.common.config.NatsConnectionHolder;
import com.mcp.mcp_pilot.ops.adapter.out.nats.dto.DiagnoseRequest;
import com.mcp.mcp_pilot.ops.port.in.dto.DiagnoseResult;
import com.mcp.mcp_pilot.ops.port.out.DiagnosePort;
import io.nats.client.Connection;
import io.nats.client.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class NatsDiagnoseAdapter implements DiagnosePort {

    private static final String DIAGNOSE_SUBJECT = "ops.diagnose.request";
    private static final Duration DIAGNOSE_TIMEOUT = Duration.ofSeconds(10);

    private final NatsConnectionHolder connectionHolder;
    private final JsonMapper jsonMapper;


    @Override
    public DiagnoseResult requestDiagnose(DiagnoseRequest request) {
        log.info("[NatsDiagnoseAdapter] NATS 동기 진단 요청 시작 (boundedElastic 격리). TrackingID: {}", request.trackingId());
        Thread t = Thread.currentThread();
        log.info("[THREAD-CHECK] NatsAdapter: name={}, isVirtual={}", t.getName(), t.isVirtual());

        // 가상 스레드가 NATS blocking 호출을 직접 수용하여 처리 (Reactor 레이어 불필요)
        return executeNatsRequest(request);
    }

    private DiagnoseResult executeNatsRequest(DiagnoseRequest request) {
        Connection natsConnection  = connectionHolder.getConnection();
        if (natsConnection  == null || natsConnection .getStatus() != Connection.Status.CONNECTED) {
            log.warn("[NatsDiagnoseAdapter] NATS 미연결 상태 - 진단 일시 비활성화. TrackingID: {}", request.trackingId());
            return failResult(request.trackingId(), request.namespace(), "FAILED", "진단 서비스가 일시적으로 이용 불가능합니다. (인프라 연결 대기 중)");
        }

        byte[] data;
        try {
            data = jsonMapper.writeValueAsBytes(request);
        } catch (Exception e) {
            log.error("[NatsDiagnoseAdapter] 요청 직렬화 실패 - TrackingID: {}", request.trackingId(), e);
            return failResult(request.trackingId(), request.namespace(), "FAILED", "요청 데이터 직렬화 실패: " + e.getMessage());
        }

        Message reply;
        try {
            reply = natsConnection .request(DIAGNOSE_SUBJECT, data, DIAGNOSE_TIMEOUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[NatsDiagnoseAdapter] 요청 스레드 인터럽트 - TrackingID: {}", request.trackingId());
            return failResult(request.trackingId(), request.namespace(), "FAILED", "요청이 중단되었습니다.");
        } catch (Exception e) {
            log.error("[NatsDiagnoseAdapter] NATS 브로커 통신 실패 - TrackingID: {}", request.trackingId(), e);
            return failResult(request.trackingId(), request.namespace(), "FAILED", "NATS 브로커 통신 실패: " + e.getMessage());
        }

        if (reply == null || reply.getData() == null) {
            log.warn("[NatsDiagnoseAdapter] NATS 응답 없음(null) - TrackingID: {}", request.trackingId());
            return failResult(request.trackingId(), request.namespace(), "TIMEOUT", "진단 데이터 수집 타임아웃 (10초 초과)");
        }

        try {
            return jsonMapper.readValue(reply.getData(), DiagnoseResult.class); // ★ jsonMapper 로 통일
        } catch (Exception e) {
            log.error("[NatsDiagnoseAdapter] 응답 역직렬화 실패 - TrackingID: {}, raw={}",
                    request.trackingId(), new String(reply.getData(), StandardCharsets.UTF_8), e);
            return failResult(request.trackingId(), request.namespace(), "FAILED", "응답 데이터 파싱 실패: " + e.getMessage());
        }
    }

    private DiagnoseResult failResult(String trackingId, String namespace, String status, String message) {
        return new DiagnoseResult(trackingId, namespace, null, null, null, status, message);
    }
}
