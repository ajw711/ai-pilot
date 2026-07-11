package handler

import (
	"log"

	"github.com/nats-io/nats.go"
)

type TestHandler struct{}

func NewTestHandler() *TestHandler {
	return &TestHandler{}
}

func (h *TestHandler) Handle(msg *nats.Msg) {
	log.Printf(
		"[handler] received subject=%s data=%s",
		msg.Subject,
		string(msg.Data),
	)
}
