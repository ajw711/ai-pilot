# Troubleshooting Log

애플리케이션/코드 레벨에서 실제로 겪은 문제와 해결 과정을 기록합니다.
(인프라/클러스터 레벨 이슈는 [ai-pilot-infra의 troubleshooting](../../ai-pilot-infra/docs/troubleshooting) 참고)

각 문서는 다음 구조를 따릅니다.

- **상황**: 무엇이 문제였는가 (에러 로그, 증상)
- **원인**: 왜 발생했는가
- **해결**: 어떻게 고쳤는가
- **교훈**: 다음에 비슷한 문제를 피하려면 무엇을 알아야 하는가

## 목록

- [002. SSE 스트리밍 중 예외 발생 시 프론트 파서가 깨지는 문제](./002-sse-exception-handling.md)
- [003. SSE 인증 시 JWT 노출 문제와 티켓(Ticket) 패턴 도입](./003-sse-ticket-auth-pattern.md)
- [004. 대용량 문서 벡터 색인 중 429 Rate Limit 및 CoreDNS DNS Timeout 장애 분석](./004-vector-batch-embedding-dns-retry.md)
