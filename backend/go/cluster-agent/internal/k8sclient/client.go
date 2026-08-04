package k8sclient

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"path/filepath"
	"sort"
	"time"

	corev1 "k8s.io/api/core/v1"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/util/homedir"
)

// 로컬 PC의 kubeconfig(~/.kube/config)를 읽어와 Kubernetes와 연결하고 리소스를 조회/제어하는 파일
type Client struct {
	clientset *kubernetes.Clientset
}

// DeploymentParams는 Deployment 생성/업데이트에 필요한 값들을 묶은 구조체.
// k8sclient 패키지는 model 패키지(NATS DTO)에 의존하지 않도록 별도로 정의.
type DeploymentParams struct {
	Namespace string
	AppName   string
	Image     string
	Tag       string
	Replicas  int32
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

func (p DeploymentParams) fullImage() string {
	if p.Tag == "" {
		return p.Image
	}
	return fmt.Sprintf("%s:%s", p.Image, p.Tag)
}

func (c *Client) GetPodLogs(ctx context.Context, namespace, podName, containerName string, tailLines int64, previous bool) (string, error) {
	if tailLines <= 0 {
		tailLines = 200 // 기본값 200줄
	}

	opts := &corev1.PodLogOptions{
		TailLines: &tailLines,
		Previous:  previous,
	}
	// 멀티 컨테이너 파드일 경우 특정 컨테이너명 지정
	if containerName != "" {
		opts.Container = containerName
	}

	req := c.clientset.CoreV1().Pods(namespace).GetLogs(podName, opts)
	podLogs, err := req.Stream(ctx)
	if err != nil {
		// k8s 명확한 에러 분기 (AI가 원인을 오해하지 않도록 처리)
		if apierrors.IsNotFound(err) {
			return "", fmt.Errorf("POD_NOT_FOUND: pod '%s' does not exist in namespace '%s'", podName, namespace)
		}
		if apierrors.IsForbidden(err) {
			return "", fmt.Errorf("PERMISSION_DENIED: no permission to read logs for pod '%s'", podName)
		}
		return "", fmt.Errorf("failed to open log stream for pod %s: %w", podName, err)
	}
	defer podLogs.Close()

	// 64KB 안전 바이트 제한 (초장문 single-line 로그 방지)
	limitedReader := io.LimitReader(podLogs, 64*1024)

	buf := new(bytes.Buffer)
	_, err = io.Copy(buf, limitedReader)
	if err != nil {
		return "", fmt.Errorf("failed to read pod log stream: %w", err)
	}

	return buf.String(), nil
}

// GetWarningEvents는 Warning 이벤트를 수집하여 최신순으로 정렬 후 최대 30개만 반환
func (c *Client) GetWarningEvents(ctx context.Context, namespace string) ([]string, error) {
	opts := metav1.ListOptions{
		FieldSelector: "type=Warning", // Warning 이벤트만 필터링
	}

	events, err := c.clientset.CoreV1().Events(namespace).List(ctx, opts)
	if err != nil {
		if apierrors.IsForbidden(err) {
			return nil, fmt.Errorf("PERMISSION_DENIED: no permission to list events in namespace '%s'", namespace)
		}
		return nil, fmt.Errorf("failed to list warning events in namespace %s: %w", namespace, err)
	}

	if len(events.Items) == 0 {
		return []string{}, nil
	}

	// 1. 최신 시간순 정렬 (LastTimestamp 기준 내림차순)
	sort.Slice(events.Items, func(i, j int) bool {
		return events.Items[i].LastTimestamp.After(events.Items[j].LastTimestamp.Time)
	})

	// 2. 최대 30개로 캡핑
	const maxEvents = 30
	items := events.Items
	if len(items) > maxEvents {
		items = items[:maxEvents]
	}

	var eventMessages []string
	for _, event := range items {
		msg := fmt.Sprintf("[%s] Reason: %s | Message: %s | Object: %s/%s (Count: %d)",
			event.LastTimestamp.Format(time.RFC3339),
			event.Reason,
			event.Message,
			event.InvolvedObject.Kind,
			event.InvolvedObject.Name,
			event.Count,
		)
		eventMessages = append(eventMessages, msg)
	}

	return eventMessages, nil
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
