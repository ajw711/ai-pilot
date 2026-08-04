package service

import (
	"cluster-agent/internal/k8sclient"
	"cluster-agent/internal/model"
	"context"
	"fmt"
	"log"
	"strings"
)

type DiagnoseService struct {
	k8sClient *k8sclient.Client
}

func NewDiagnoseService(k8sClient *k8sclient.Client) *DiagnoseService {
	return &DiagnoseService{k8sClient: k8sClient}
}

// Diagnose는 K8s 클러스터의 Warning 이벤트 및 파드 로그를 진단 수집합니다.
func (s *DiagnoseService) Diagnose(ctx context.Context, req model.DiagnoseRequest) (model.DiagnoseResult, error) {
	result := model.DiagnoseResult{
		TrackingID: req.TrackingID,
		Namespace:  req.Namespace,
		Status:     "SUCCESS",
	}

	if s.k8sClient == nil {
		log.Printf("[service] [MOCK MODE] diagnose request: %+v", req)
		result.Pods = []string{"mock-pod-1", "mock-pod-2"}
		result.WarningEvents = []string{"[MOCK] Reason: OOMKilled | Message: Memory limit exceeded"}
		result.Logs = "[MOCK LOG] java.lang.OutOfMemoryError: Java heap space"
		return result, nil
	}

	// Warning 이벤트 수집 (최신 30개 정렬)
	warningEvents, err := s.k8sClient.GetWarningEvents(ctx, req.Namespace)
	if err != nil {
		log.Printf("[diagnose-service] failed to get warning events: %v", err)
	}
	result.WarningEvents = warningEvents

	// 진단 대상 파드 결정
	targetPods := []string{}
	if req.PodName != "" {
		targetPods = append(targetPods, req.PodName)
	} else {
		// podName이 비어있으면 Warning 이벤트 메시지에서 파드 이름을 추출하여 상위 2개만 캡핑
		targetPods = s.extractPodsFromEvents(warningEvents, 2)
	}

	// Warning 파드가 없으면 해당 네임스페이스의 전체 파드 목록 조회
	if len(targetPods) == 0 {
		pods, err := s.k8sClient.GetPods(req.Namespace)
		if err == nil && len(pods) > 0 {
			// 파드 목록 중 최대 2개만 수집
			if len(pods) > 2 {
				targetPods = pods[:2]
			} else {
				targetPods = pods
			}
		}
	}
	result.Pods = targetPods

	// 대상 파드들의 로그 수집 (TailLines 200줄, Previous=true 크래시 직전 로그)
	var logsBuilder strings.Builder
	for _, pod := range targetPods {
		// 먼저 크래시 직전(previous=true) 로그 시도 후, 없으면 현재 로그(previous=false) 시도
		podLog, err := s.k8sClient.GetPodLogs(ctx, req.Namespace, pod, "", 200, true)
		if err != nil || strings.TrimSpace(podLog) == "" {
			podLog, _ = s.k8sClient.GetPodLogs(ctx, req.Namespace, pod, "", 200, false)
		}

		if podLog != "" {
			logsBuilder.WriteString(fmt.Sprintf("=== Logs for Pod: %s ===\n%s\n\n", pod, podLog))
		}
	}
	result.Logs = logsBuilder.String()

	log.Printf("[diagnose-service] diagnose completed for trackingId: %s (Pods: %d, Events: %d)",
		req.TrackingID, len(result.Pods), len(result.WarningEvents))

	return result, nil
}

// extractPodsFromEvents는 이벤트 메시지에서 대상 파드명을 추출하여 최대 maxCount개만 반환합니다.
func (s *DiagnoseService) extractPodsFromEvents(events []string, maxCount int) []string {
	seen := make(map[string]bool)
	var podNames []string

	for _, event := range events {
		// 이벤트 포맷 예: "... | Object: Pod/my-app-pod-123 (Count: 1)"
		if idx := strings.Index(event, "Object: Pod/"); idx != -1 {
			sub := event[idx+len("Object: Pod/"):]
			fields := strings.Fields(sub)
			if len(fields) > 0 {
				podName := fields[0]
				if !seen[podName] {
					seen[podName] = true
					podNames = append(podNames, podName)
					if len(podNames) >= maxCount {
						break
					}
				}
			}
		}
	}
	return podNames
}
