package model

// DeployRequest는 스프링 등 외부 시스템에서 전송하는 배포 요청 데이터 구조체
type DeployRequest struct {
	TrackingId string `json:"trackingId"`
	AppName    string `json:"appName"`
	Image      string `json:"image"`
	Tag        string `json:"tag"`
	Replicas   int32  `json:"replicas"`
	Namespace  string `json:"namespace"`
	UserId     string `json:"userId"`
}

// DeployResult는 배포 처리 결과를 외부 시스템(스프링)으로 리턴할 때 사용하는 구조체
type DeployResult struct {
	TrackingID string `json:"trackingId"`
	AppName    string `json:"appName"`
	Status     string `json:"status"`
	Message    string `json:"message"`
	Timestamp  string `json:"timestamp"`
	UserId     string `json:"userId"`
}
