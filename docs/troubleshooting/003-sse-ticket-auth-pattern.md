# [Troubleshooting] SSE 인증 시 JWT 노출 문제와 티켓(Ticket) 패턴 도입

## 상황

브라우저 표준 `EventSource(url)` API는 HTTP 요청 헤더에 `Authorization: Bearer <JWT>`를
추가할 수 없다. 이 제약을 우회하기 위해 처음에는 SSE 연결 URL의 쿼리 파라미터에 JWT를 직접
포함시키는 방식(`/sse/stream?token=<JWT>`)을 사용했다.

## 원인 / 문제

쿼리 파라미터에 담긴 값은 다음과 같은 경로로 그대로 노출된다.

- 서버 액세스 로그에 URL 전체가 기록되면서 JWT가 로그에 평문으로 남음
- 브라우저 히스토리에도 URL이 저장되므로 로컬 환경에 접근할 수 있는 사람이라면 히스토리에서
  토큰을 확인할 수 있음

JWT는 탈취되면 해당 토큰의 유효 기간 동안 사용자로 위장할 수 있는 민감한 값이기 때문에, 이
방식은 명백한 보안 취약점이었다.

> 이 문제는 [SSE 스트리밍 예외 처리 문제](./002-sse-exception-handling.md)와 마찬가지로,
> `EventSource`의 헤더 제약([SSE 선택 이유](../decisions/003-sse-choice.md) 참고)에서
> 출발한 연쇄적인 문제였다.

## 해결

단기 발급 SSE 티켓(Ticket) 패턴을 도입했다.

1. 클라이언트가 먼저 인증된 일반 REST 요청(`POST /api/v1/sse/ticket`, 헤더에 JWT 포함)으로
   1회용 티켓 발급을 요청
2. 서버는 JWT를 검증한 뒤, JWT 자체가 아니라 별도로 생성한 난수 문자열을 `TicketResponse`에
   담아 반환. 티켓은 `SecureRandom`으로 생성한 256비트(32바이트) 난수를 URL-safe Base64로
   인코딩한 값이다 (`UUID.randomUUID()`가 아님 — UUID는 약 122비트의 엔트로피만 가지는 반면,
   이 방식은 256비트 암호학적 난수를 사용해 추측 공격 가능성을 사실상 배제한다).

   ```java
   @Component
   public class SecureRandomSseTicketGenerator implements SseTicketGeneratorPort {
       private final SecureRandom secureRandom = new SecureRandom();

       @Override
       public String generate() {
           byte[] bytes = new byte[32]; // 256bit
           secureRandom.nextBytes(bytes);
           return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
       }
   }
   ```

3. 클라이언트는 이 티켓을 SSE 연결의 쿼리 파라미터(`/sse/stream?ticket=<티켓값>`)로 사용해 접속
4. 서버는 티켓의 유효성을 검증하고, 접속 즉시(또는 매우 짧은 시간 내) 티켓을 소비·파기해
   재사용을 막음

쿼리 파라미터에 값이 노출되는 것 자체는 동일하지만, 노출되는 대상이 장기간 유효한 JWT가
아니라 1회용·단기 유효 티켓이기 때문에, 설령 로그나 히스토리에 남더라도 실질적인 탈취
위험이 크게 줄어든다.

## 교훈

- 브라우저 표준 API의 제약(여기서는 `EventSource`가 커스텀 헤더를 지원하지 않는 점)을
  우회하는 방법을 고를 때는, 우회 자체가 새로운 보안 문제를 만들지 않는지 반드시 확인해야
  한다. "일단 되게 만드는 것"과 "안전하게 되게 만드는 것"은 다른 문제다.
- 쿼리 파라미터에 민감한 값을 넣어야만 하는 상황이라면, 그 값의 수명을 최대한 짧게 만들고
  일회성으로 제한하는 것이 실질적인 완화책이 될 수 있다. 근본적으로 헤더를 못 쓰는 제약을
  없앨 수는 없어도, 노출되는 값의 가치를 최소화하는 방향으로 문제를 우회했다.
