# ==========================
# Build Stage
# ==========================
FROM golang:1.26 AS builder

# 작업 디렉토리
WORKDIR /app

# 의존성 다운로드
COPY go.mod .
COPY go.sum .
RUN go mod download

# 소스 복사
COPY . .

# linux 실행파일 빌드
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 \
    go build -ldflags="-s -w" -o cluster-agent ./cmd/agent

# ==========================
# Runtime Stage
# ==========================
FROM alpine:3.22

# HTTPS 통신을 위한 인증서
RUN apk add --no-cache ca-certificates

# 일반 사용자 생성
RUN addgroup -S agent && adduser -S agent -G agent

WORKDIR /app

# 실행파일 복사
COPY --from=builder /app/cluster-agent .

# 실행 권한 부여
RUN chmod +x cluster-agent

# 일반 사용자로 실행
USER agent

# 포트
EXPOSE 8082

# 실행
ENTRYPOINT ["./cluster-agent"]