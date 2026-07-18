package service

import (
	"cluster-agent/internal/k8sclient"
	"cluster-agent/internal/model"
	"log"
)

type DeploymentService struct {
	k8sClient *k8sclient.Client // k8sClient 필드 주입 받기
}

func NewDeploymentService(k8sClient *k8sclient.Client) *DeploymentService {
	return &DeploymentService{k8sClient: k8sClient}
}

// Deploy는 배포 요청을 받아 비즈니스 로직을 수행 (현재는 로그만 출력)
func (s *DeploymentService) Deploy(req model.DeployRequest) error {
	// client-go 연동 실습 테스트
	// Namespace 조회 테스트
	namespaces, err := s.k8sClient.GetNamespaces()
	if err != nil {
		log.Printf("[service] failed to get namespaces: %v", err)
	} else {
		log.Printf("[service] cluster namespaces: %v", namespaces)
	}

	// Pod 조회 테스트
	pods, err := s.k8sClient.GetPods(req.Namespace)
	if err != nil {
		log.Printf("[service] failed to get pods: %v", err)
	} else {
		log.Printf("[service] pods in namespace '%s': %v", req.Namespace, pods)
	}

	// Deployment 조회 테스트
	deploys, err := s.k8sClient.GetDeployments(req.Namespace)
	if err != nil {
		log.Printf("[service] failed to get deployments: %v", err)
	} else {
		log.Printf("[service] deployments in namespace '%s': %v", req.Namespace, deploys)
	}

	return nil
}
