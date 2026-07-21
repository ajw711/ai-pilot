package k8sclient

import (
	"context"
	"fmt"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/util/homedir"
	"path/filepath"
	"time"
)

// 로컬 PC의 kubeconfig(~/.kube/config)를 읽어와 Kubernetes와 연결하고 리소스를 조회/제어하는 파일
type Client struct {
	clientset *kubernetes.Clientset
}

// 로컬 kubeconfig를 로드하여 Kubernetes Clientset을 초기화
func New() (*Client, error) {
	var kubeconfig string
	if home := homedir.HomeDir(); home != "" {
		kubeconfig = filepath.Join(home, ".kube", "config")
	} else {
		return nil, fmt.Errorf("user home directory not found")
	}

	// kubeconfig 설정을 로드
	config, err := clientcmd.BuildConfigFromFlags("", kubeconfig)
	if err != nil {
		// 로컬 kubeconfig 로드 실패 시, k8s 클러스터 내부(InCluster) 구동 설정을 시도
		config, err = rest.InClusterConfig()
		if err != nil {
			return nil, fmt.Errorf("failed to load kubeconfig: %v", err)
		}
	}

	// Clientset 초기화
	clientset, err := kubernetes.NewForConfig(config)
	if err != nil {
		return nil, fmt.Errorf("failed to create kubernetes clientset: %v", err)
	}

	return &Client{
		clientset: clientset,
	}, nil
}

// GetNamespaces는 클러스터 내의 모든 Namespace 이름을 조회
func (c *Client) GetNamespaces() ([]string, error) {
	nsList, err := c.clientset.CoreV1().Namespaces().List(context.TODO(), metav1.ListOptions{})
	if err != nil {
		return nil, err
	}
	var list []string
	for _, ns := range nsList.Items {
		list = append(list, ns.Name)
	}
	return list, nil
}

// GetDeployments는 특정 Namespace 내의 모든 Deployment 이름을 조회
func (c *Client) GetDeployments(namespace string) ([]string, error) {
	deployList, err := c.clientset.AppsV1().Deployments(namespace).List(context.TODO(), metav1.ListOptions{})
	if err != nil {
		return nil, err
	}
	var list []string
	for _, deploy := range deployList.Items {
		list = append(list, deploy.Name)
	}
	return list, nil
}

// GetPods는 특정 Namespace 내의 모든 Pod 이름을 조회
func (c *Client) GetPods(namespace string) ([]string, error) {
	podList, err := c.clientset.CoreV1().Pods(namespace).List(context.TODO(), metav1.ListOptions{})
	if err != nil {
		return nil, err
	}

	var list []string
	for _, pod := range podList.Items {
		list = append(list, pod.Name)
	}
	return list, nil
}

// ScaleDeployment는 지정된 Deployment의 Replicas(파드 개수)를 조절
func (c *Client) ScaleDeployment(namespace string, deployName string, replicas int32) error {
	scale, err := c.clientset.AppsV1().Deployments(namespace).GetScale(context.TODO(), deployName, metav1.
		GetOptions{})

	if err != nil {
		return fmt.Errorf("failed to get scale: %v", err)
	}
	scale.Spec.Replicas = replicas

	_, err = c.clientset.AppsV1().Deployments(namespace).UpdateScale(context.TODO(), deployName, scale, metav1.
		UpdateOptions{})
	if err != nil {
		return fmt.Errorf("failed to update scale: %v", err)
	}
	return nil
}

// RestartDeployment는 Deployment의 annotation을 임의 수정(rollout restart)하여 파드를 순차 재시작
func (c *Client) RestartDeployment(namespace string, deployName string) error {
	// kubectl rollout restart deployment와 동일하게, Template의 restart 시각 annotation을 강제 주입해 롤링 업데이트를 트리거한다.
	now := time.Now().Format(time.RFC3339)
	patchData := fmt.Sprintf(`{"spec":{"template":{"metadata":{"annotations":{"kubectl.kubernetes.
  io/restartedAt":"%s"}}}}}`, now)

	_, err := c.clientset.AppsV1().Deployments(namespace).Patch(
		context.TODO(),
		deployName,
		types.StrategicMergePatchType,
		[]byte(patchData),
		metav1.PatchOptions{},
	)
	if err != nil {
		return fmt.Errorf("failed to restart deployment: %v", err)
	}
	return nil
}
