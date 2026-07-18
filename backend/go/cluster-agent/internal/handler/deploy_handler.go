package handler

import (
	"cluster-agent/internal/model"
	"cluster-agent/internal/natsclient"
	"cluster-agent/internal/service"
	"encoding/json"
	"log"
	"time"

	"github.com/nats-io/nats.go"
)

type DeployHandler struct {
	deployService *service.DeploymentService
	natsClient    *natsclient.Client
}

func NewDeployHandler(deployService *service.DeploymentService, natsClient *natsclient.Client) *DeployHandler {
	return &DeployHandler{
		deployService: deployService,
		natsClient:    natsClient,
	}
}

// Handle은 NATS Subscribe로 유입된 메시지를 받아 비즈니스 로직 처리 및 결과 응답을 보냅
func (h *DeployHandler) Handle(msg *nats.Msg) {
	log.Printf("[handler] received deploy request raw data: %s", string(msg.Data))
	var req model.DeployRequest
	// 수신한 바이트 데이터를 구조체로 파싱
	if err := json.Unmarshal(msg.Data, &req); err != nil {
		log.Printf("[handler] failed to unmarshal deploy request: %v", err)
		h.publishResult(model.DeployResult{
			Success:   false,
			Message:   err.Error(),
			Timestamp: time.Now().Format(time.RFC3339)})
	}
	err := h.deployService.Deploy(req)
	result := model.DeployResult{
		AppName:   req.AppName,
		Timestamp: time.Now().Format(time.RFC3339),
	}

	// 서비스 계층 호출
	if err != nil {
		log.Printf("[handler] deployment failed: %v", err)
		result.Success = false
		result.Message = err.Error()
	} else {
		log.Printf("[handler] deployment succeeded for AppName: %s", req.AppName)
		result.Success = true
		result.Message = "Deployment successful"
	}

	// 최종 결과를 NATS로 발행
	h.publishResult(result)
}
func (h *DeployHandler) publishResult(result model.DeployResult) {
	data, err := json.Marshal(result)
	if err != nil {
		log.Printf("[handler] failed to marshal deploy result: %v", err)
		return
	}

	err = h.natsClient.Publish("deploy.result", data)
	if err != nil {
		log.Printf("[handler] failed to publish deploy result: %v", err)
	}
}
