package server

import (
	"context"  // 제한 시간 및 취소 신호 전달을 담당하는 표준 라이브러리
	"fmt"      // 문자열 포맷팅 및 출력을 담당하는 표준 라이브러리
	"log"      // 콘솔에 날짜 및 시간과 함께 로그를 남기는 표준 라이브러리
	"net/http" // 내장 웹 서버 및 라우팅 기능을 제공하는 웹 코어 라이브러리
	"time"     // 타임아웃 설정을 위해 초 단위를 다루는 시간 라이브러리
)

// HTTPServer 구조체는 net/http 엔진의 주소를 품는 사용자 정의 객체
type HTTPServer struct {
	//  net/http  라이브러리가 제공하는 실제 웹 서버 엔진의 메모리 주소(포인터)를 변수로 품고 있겠다는 뜻
	server *http.Server
}

// NewHTTPServer는 새로운 HTTP 웹 서버 인스턴스를 메모리에 할당하고 주소값을 리턴하는 일반 함수
func NewHTTPServer(port string) *HTTPServer {
	// http.NewServeMux() : 자바 스프링의 DispatcherServlet(라우터)와 같다.
	// 특정 URL 경로로 요청이 들어왔을 때, 어떤 메소드로 보낼지
	// 교통정리를 해주는 객체( 멀티플렉서 )를 만들기
	mux := http.NewServeMux()

	// healthz 경로로 요청이 오면 상태 코드 200과 ok 문자열을 바이너리 슬라이스로 변환하여 전송
	mux.HandleFunc("/healthz", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("ok"))
	})

	// readyz 경로로 요청이 오면 헬스체크용 200 상태 코드를 반환
	mux.HandleFunc("/readyz", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("ok"))
	})

	// 포트, 라우터, 읽기/쓰기 대기 시간 등의 속성을 지정하여 실제 로우레벨 웹 서버 엔진 설정을 메모리에 구성하고 주소를 생성
	srv := &http.Server{
		Addr:         fmt.Sprintf(":%s", port),
		Handler:      mux,
		ReadTimeout:  5 * time.Second,
		WriteTimeout: 5 * time.Second,
	}

	// 사용자 정의 구조체의 주소를 반환 타입에 맞춰 리턴
	return &HTTPServer{
		server: srv,
	}
}

// Start는 객체 인스턴스를 바탕으로 가동되는 메소드이며 포인터 수신기를 사용
func (h *HTTPServer) Start() {
	// health check
	http.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, err := w.Write([]byte("OK"))
		if err != nil {
			return
		}
	})

	log.Printf("[http] server starting on %s", h.server.Addr)

	// ListenAndServe() 함수는 호출되면 차단되므로 새로운 고루틴(비동기 독립 스레드)을 열어 기동
	go func() {
		// 정상 셧다운 시 반환되는 http.ErrServerClosed 예외는 정상 종료 상태이므로 무시하고 그 외의 구동 실패 예외만 처리
		if err := h.server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("[http] failed to start server: %v", err)
		}
	}()
}

// Shutdown은 서버 가동을 자원 유실 없이 안전하게 대기시키며 중지하는 메소드
func (h *HTTPServer) Shutdown(ctx context.Context) error {
	log.Println("[http] server shutting down")
	// 상위 타임아웃 컨텍스트(제한 시간) 범위 안에서 커넥션을 정리하며 꺼지도록 명령을 보냄
	return h.server.Shutdown(ctx)
}
