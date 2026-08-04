package model

// DiagnoseRequest는 Spring에서 전송하는 K8s 진단 요청 데이터
type DiagnoseRequest struct {
	TrackingID  string `json:"trackingId"`
	Namespace   string `json:"namespace"`
	PodName     string `json:"podName,omitempty"`
	RequestedBy int64  `json:"requestedBy"`
}

// DiagnoseResult는 K8s 진단 결과를 Spring(LLM)으로 회신할 데이터
type DiagnoseResult struct {
	TrackingID    string   `json:"trackingId"`
	Namespace     string   `json:"namespace"`
	Pods          []string `json:"pods"`
	WarningEvents []string `json:"warningEvents"`
	Logs          string   `json:"logs"`
	Status        string   `json:"status"` // SUCCESS / FAILED
	Message       string   `json:"message,omitempty"`
}
