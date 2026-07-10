package config

import (
	"log"
	"os"

	"github.com/joho/godotenv"
)

type Config struct {
	AppName  string
	HTTPPort string
	NatsURL  string
}

/*
godotenv 사용법: https://github.com/joho/godotenv
해당 함수 접근제어자는 public 이유는 함수명 대문자
*/
func Load() *Config {
	// 로컬 개발 환경에서는 .env 로드
	// 운영(Kubernetes, Docker)에서는 OS 환경변수 사용 가능
	err := godotenv.Load()
	if err != nil {
		log.Println("[config] .env 파일이 없어 OS 환경변수를 사용합니다.")
	}

	cfg := &Config{
		AppName:  getEnv("APP_NAME", "cluster-agnet"),
		HTTPPort: getEnv("HTTP_PORT", "8082"),
		NatsURL:  getEnv("NATS_URL", "nats://localhost:4222"),
	}

	log.Printf("[config] loaded appName=%s, httpPort=%s, natsURL=%s",
		cfg.AppName,
		cfg.HTTPPort,
		cfg.NatsURL,
	)

	return cfg
}

// 접근 제어자 private 이유는 함수명 소문자
func getEnv(key string, defaultValue string) string {
	value := os.Getenv(key)

	if value == "" {
		return defaultValue
	}
	return value
}
