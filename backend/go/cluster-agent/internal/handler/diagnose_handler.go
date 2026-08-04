package handler

import (
	"cluster-agent/internal/model"
	"cluster-agent/internal/service"
	"context"
	"encoding/json"
	"log"
	"time"

	"github.com/nats-io/nats.go"
)

type DiagnoseHandler struct {
	diagnoseService *service.DiagnoseService
}

func NewDiagnoseHandler(diagnoseService *service.DiagnoseService) *DiagnoseHandler {
	return &DiagnoseHandler{diagnoseService: diagnoseService}
}

func (h *DiagnoseHandler) Handle(msg *nats.Msg) {
	log.Printf("[diagnose-handler] received diagnose request raw data: %s", string(msg.Data))

	var req model.DiagnoseRequest
	if err := json.Unmarshal(msg.Data, &req); err != nil {
		log.Printf("[diagnose-handler] failed to unmarshal request: %v", err)
		h.respondError(msg, "", err.Error())
		return
	}
	log.Printf("[diagnose-handler] received diagnose request: trackingId=%s, namespace=%s, requestedBy=%d",
		req.TrackingID, req.Namespace, req.RequestedBy)

	ctx, cancel := context.WithTimeout(context.Background(), 8*time.Second)
	defer cancel()

	result, err := h.diagnoseService.Diagnose(ctx, req)
	if err != nil {
		log.Printf("[diagnose-handler] diagnose failed: %v", err)
		h.respondError(msg, req.TrackingID, err.Error())
		return
	}

	data, err := json.Marshal(result)
	if err != nil {
		log.Printf("[diagnose-handler] failed to marshal result: %v", err)
		return
	}

	// NATS Request-Reply 동기 응답 전달
	if msg.Reply != "" {
		if err := msg.Respond(data); err != nil {
			log.Printf("[diagnose-handler] failed to send NATS respond: %v", err)
		}
	}
}

func (h *DiagnoseHandler) respondError(msg *nats.Msg, trackingID string, errorMsg string) {
	result := model.DiagnoseResult{
		TrackingID: trackingID,
		Status:     "FAILED",
		Message:    errorMsg,
	}
	data, _ := json.Marshal(result)
	if msg.Reply != "" {
		_ = msg.Respond(data)
	}
}
