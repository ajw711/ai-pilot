package main

import (
	"cluster-agent/internal/config" // 패키지 불러오기
	"cluster-agent/internal/handler"
	"cluster-agent/internal/natsclient"
	"cluster-agent/internal/server" // 패키지 불러오기
	"context"
	"log"       // 콘솔 로깅 라이브러리
	"os"        // 시스템 신호 정의 등 운영체제 소통 도구
	"os/signal" // 운영체제 시그널 모니터링 라이브러리
	"syscall"   // 운영체제 시스템 콜 정의 라이브러리
	"time"

	"github.com/nats-io/nats.go"
)

func main() {
	log.Println("[agent] cluster-agent starting...")
	// 환경변수 및 설정을 읽어와 구조체 메모리 주소 받기
	cfg := config.Load()
	// 읽어온 포트 설정을 바탕으로 웹 서버 객체 메모리 주소를 생성
	httpServer := server.NewHTTPServer(cfg.HTTPPort)
	// 웹 서버를 백그라운드 고루틴으로 비동기 실행
	httpServer.Start()

	// NATS 연결
	natsClient, err := natsclient.New(cfg.NatsURL)
	if err != nil {
		log.Fatalf("[agent] failed to connect NATS: %v", err)
	}
	defer natsClient.Close()
	testHandler := handler.NewTestHandler()

	err = natsClient.Subscribe(
		"ops.test.request",
		testHandler.Handle,
	)

	if err != nil {
		log.Fatalf("[agent] failed to subscribe NATS subject: %v", err)
	}

	// 서버가 실행된 후, 종료 시그널이 오기 전까지 대기하는 함수를 실행
	waitForShutdown(httpServer, natsClient)
}

// waitForShutdown은 포인터를 매개변수로 받아 대기 작업을 수행하고 리소스를 안전하게 셧다운
func waitForShutdown(httpServer *server.HTTPServer, natsClient *natsclient.Client) {

	// 버퍼 크기가 0일 때 (바통 터치 방식)
	// 작동: 송신자(바통을 주는 사람)와 수신자(바통을 받는 사람)가 동시에 그 자리에 만나야만 데이터가 넘어감
	// 특징: 받는 사람이 아직 기다리고 있지 않으면, 주는 사람은 받는 사람이 올 때까지 그 자리에서 꼼짝 않고 서서 기다려야(Blocking)함
	// Go에서는 동기식(Synchronous) 채널임

	// 버퍼 크기가 2일 때 (우체통 방식)
	// 작동: 2개의 편지를 담을 수 있는 우체통이 중간에 생김
	// 특징: 주는 사람은 받는 사람이 자리에 없더라도, 우체통에 편지를 최대 2개까지는 그냥 쏙 집어넣고 곧바로 제 갈 길을 갈 수 있다
	// 하지만 우체통이 이미 편지 2개로 꽉 찬 상태에서 3번째 편지를 넣으려고 하면, 그때는 받는 사람이 와서 편지를 꺼내 가 우체통에 빈자리가
	// 생길 때까지 주는 사람이 그 앞차에서 대기
	// 받는 사람은 우체통에 편지가 하나라도 있으면 기다리지 않고 바로 꺼내 가고, 우체통이 텅 비어있을 때만 편지가 올
	// 때까지 그 자리에서 멈춰 서서 기다린다

	// os.Signal 데이터만 전달받을 수 있는 버퍼 크기 1짜리 비동기 통신 채널(우체통)을 메모리
	// 만약에 버퍼 크기가 0이면 데이터를 바로 전달 받아야함 이어달리기 바통 터치 느낌으로 바로 받아야함
	// make는 채널, 맵, 슬라이스의 내부 자료구조를 초기화해 할당하는 내장 함수
	stop := make(chan os.Signal, 1)

	// OS가 Ctrl+C(SIGINT) 또는 배포 플랫폼 종료 신호(SIGTERM)를 보냈을 때 프로세스가 즉시 종료되지 않고
	// 생성한 stop 채널에 신호 정보를 강제로 밀어 넣도록 예약 등록을 수행
	signal.Notify(stop, syscall.SIGINT, syscall.SIGTERM)

	// 채널에서 데이터가 들어올 때까지 무한 대기하며 프로세스를 블로킹(멈춤) 상태로 정지
	// 사용자가 아무것도 입력하지 않으면 이 라인에서 영원히 머물며 자원을 소모하지 않고 대기
	sig := <-stop
	log.Printf("[agent] shutdown signal received: %s", sig.String())

	// context.Background()라는 빈 컨텍스트 위에 5초 시간 제한 타이머를 덧씌워 타이머 컨텍스트를 만들기
	// ctx(5초 타이머 정보)와 cancel(백그라운드 타이머 리소스 취소 함수) 두 개가 반환
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)

	// 함수가 최종적으로 반환되어 종료되기 직전에 예약해 둔 cancel() 함수가 무조건 실행되도록 보장
	// 5초 타임아웃 전에 작업이 끝나면 감시 타이머 자원을 강제로 해제하여 메모리 유실을 차단
	defer cancel()

	// if문 내부 초기화 문법을 사용하여 Shutdown 메소드 결과를 err에 담고 즉시 err가 nil(null)이 아닌지 검사
	if err := httpServer.Shutdown(ctx); err != nil {
		log.Printf("[agent] http shutdown error: %v", err)
	}

	natsClient.Close()

	log.Println("[agent] cluster-agent stopped")
}
