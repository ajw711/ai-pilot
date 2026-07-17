package model

// DeployRequest는 스프링 등 외부 시스템에서 전송하는 배포 요청 데이터 구조체
type DeployRequest struct {
	AppName   string `json:"appName"`
	Image     string `json:"image"`
	Tag       string `json:"tag"`
	Replicas  int32  `json:"replicas"`
	NameSpace string `json:"nameSpace"`
}

// DeployResult는 배포 처리 결과를 외부 시스템(스프링)으로 리턴할 때 사용하는 구조체
type DeployResult struct {
	AppName   string `json:"appName"`
	Success   bool   `json:"success"`
	Message   string `json:"message"`
	Timestamp string `json:"timestamp"`
}
