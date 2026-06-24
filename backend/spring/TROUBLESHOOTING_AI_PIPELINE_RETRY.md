# Troubleshooting: 2026-06-24 통합 트러블슈팅 리포트

본 문서는 **지식 검수 및 요약 시스템(ai-pilot)** 구축 과정에서 발생한 CORS 차단, 프론트엔드 렌더링 크래시, 비동기 트랜잭션 예외, 그리고 AI API 과부하 이슈에 대한 원인 분석과 해결 방안을 집대성한 통합 기술 히스토리 문서입니다.

---

## 1. CORS 차단 및 Spring Security 설정 장애

### 🚨 현상 (Symptom)
* 프론트엔드(`http://localhost:5173`)에서 백엔드 API(`http://localhost:8080`) 호출 시 브라우저에서 다음과 같은 CORS 차단 예외 발생:
  ```text
  Access to XMLHttpRequest at 'http://localhost:8080/api/v1/knowledge/list' from origin 'http://localhost:5173' 
  has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
  ```

### 🔍 원인 분석 (Root Cause)
1. **Spring Security의 선행 차단**: 백엔드에 시큐리티 의존성이 주입되어 활성화된 상태이나, [SecurityConfig.java](file:///C:/project/ai-pilot/backend/spring/src/main/java/com/mcp/mcp_pilot/common/config/SecurityConfig.java) 필터 체인에 `.cors()` 설정이 누락되어 브라우저의 OPTIONS(Preflight) 요청 및 일반 CORS 요청을 스프링 시큐리티 필터 단계에서 기본 거부함.
2. **WebMvcConfigurer 설정 누락**: 서블릿(WebMvc) 레이어인 [WebConfiguration.java](file:///C:/project/ai-pilot/backend/spring/src/main/java/com/mcp/mcp_pilot/common/config/WebConfiguration.java)에도 크로스 오리진 요청을 전역 허용하는 `addCorsMappings` 설정이 누락됨.

### 🛠️ 해결 방안 (Resolution)
* **WebMvc 설정 추가**: `WebConfiguration`에 프론트엔드 포트의 CORS 요청을 전역 허용하는 `addCorsMappings` 메서드 구현.
* **Security 필터 체인 연동**: `SecurityConfig`에 `.cors(Customizer.withDefaults())`를 명시적으로 적용하고, `CorsConfigurationSource` 빈(Bean)을 정의하여 프론트엔드 오리진(`http://localhost:5173`)을 신뢰하도록 설정함.

---

## 2. 프론트엔드 DashboardPage 렌더링 크래시

### 🚨 현상 (Symptom)
* 지식 대시보드 진입 시 화면이 렌더링되지 않고 크래시가 발생하며 React 에러 바운더리 경고 출력:
  ```text
  App.tsx:12 An error occurred in the <DashboardPage> component.
  Consider adding an error boundary to your tree to customize error handling behavior.
  ```

### 🔍 원인 분석 (Root Cause)
* **백엔드 공통 Response Wrapper 구조의 불일치**:
  * 백엔드 API `/knowledge/list`는 공통 포맷(`ApiResponse<ListKnowledgeResponse>`)으로 감싸서 응답하므로, 실제 배열은 `response.data.summaryList` 내부에 위치함.
  * 그러나 프론트엔드 [api.ts](file:///C:/project/ai-pilot/frontend/src/features/knowledge/api.ts)의 `useKnowledgeList` 훅은 서버 응답 전체(`ApiResponse`)를 `KnowledgeSummaryDto[]` 배열 타입으로 곧바로 캐스팅하여 반환함.
  * 이로 인해 `DashboardPage.tsx`에서 `sources` 변수가 배열이 아닌 일반 객체가 되었고, `sources.map()`, `sources.filter()` 등 배열 메서드 호출 과정에서 런타임 타입 에러가 발생해 컴포넌트가 크래시됨.

### 🛠️ 해결 방안 (Resolution)
* `api.ts` 내 `useKnowledgeList` 훅의 데이터 파싱 방식을 수정하여, 백엔드 공통 포맷 객체에서 실제 데이터 배열인 `.data.summaryList`를 정확히 추출하여 리턴하도록 수정함:
  ```typescript
  const { data: apiResponse } = await api.get<ApiResponse<ListKnowledgeResponse>>("/knowledge/list");
  return apiResponse.data?.summaryList || [];
  ```

---

## 3. 비동기 스레드 JPA 수정 연산 트랜잭션 유실

### 🚨 현상 (Symptom)
* 지식 원본 저장 후 백그라운드 스레드 풀에서 AI 검수 및 가공 상태를 데이터베이스에 갱신하려 할 때 예외 발생 및 가공 작업 중단:
  ```text
  org.springframework.dao.InvalidDataAccessApiUsageException: No active transaction for update or delete query
  Caused by: jakarta.persistence.TransactionRequiredException: No active transaction for update or delete query
  ```

### 🔍 원인 분석 (Root Cause)
* **비동기 스레드의 트랜잭션 전파 단절**:
  * `saveKnowledge()`는 상위에 `@Transactional`이 있어 1차 DB 저장은 성공함.
  * 하지만 `wikiExecutor.submit()`을 통해 생성된 **별도의 비동기 스레드** 내에서 실행되는 `processWikiAsync()` 및 하위 데이터 갱신 작업(`updateStatus`)은 상위 트랜잭션을 이어받지 못하고 트랜잭션이 전혀 없는 상태로 동작함.
  * JPA의 `@Modifying`이 붙은 벌크 UPDATE 쿼리는 반드시 활성 트랜잭션을 요구하므로 즉시 예외가 유발됨.

### 🛠️ 해결 방안 (Resolution)
* 데이터베이스 쓰기(Update)를 직접 수행하는 아웃바운드 영속성 어댑터인 [KnowledgePersistenceAdapter](file:///C:/project/ai-pilot/backend/spring/src/main/java/com/mcp/mcp_pilot/knowledge/adapter/out/persistence/KnowledgePersistenceAdapter.java)의 `updateStatus` 및 `updateVerificationAndSummary` 메서드 단에 `@Transactional`을 명시적으로 붙여 개별 스레드 호출 시에도 트랜잭션이 보장되도록 수정함.

---

## 4. AI API(Gemini) 과부하 극복을 위한 리트라이 설계

### 🚨 현상 (Symptom)
* 구글의 트래픽 폭증으로 인해 `503 Service Unavailable (This model is currently experiencing high demand)` 오류가 빈번히 발생하여 비동기 AI 검수가 계속 실패 상태로 마킹됨.

### 🔍 원인 분석 (Root Cause)
* 무료 요금제(Free Tier)의 컴퓨팅 자원 한계와 `application.yaml` 상의 잘못된 옵션(`thinkingBudget: 1000`)으로 인해 API 커넥션 무한 대기(Hang) 및 과부하 튕김 현상이 가중됨.

### 🛠️ 해결 방안 (Resolution - Custom Retry Engine)
스프링의 `@Retryable` 어노테이션 대신 아웃바운드 어댑터인 [SpringAiAdapter](file:///C:/project/ai-pilot/backend/spring/src/main/java/com/mcp/mcp_pilot/knowledge/adapter/out/ai/SpringAiAdapter.java)의 `executeWithThrottle` 공통 호출 메서드 내부에 커스텀 리트라이 구조를 직접 자바 코드로 구현함.

* **Try-Per-Attempt 구조 (처리량 최적화)**:
  * 락(`Semaphore`)의 획득 및 반환을 리트라이 루프 안쪽에서 매 시도 시마다 수행함.
  * 호출 실패 시 **락을 즉시 반환(release)하고 대기(sleep)**하게 함으로써, 백오프 대기 시간 동안 세마포어를 독점하지 않아 시스템의 다른 정상 요청들을 막지 않고 처리량(Throughput)을 최적화함.
* **Exponential Backoff + Random Jitter**:
  * 3회 시도를 기본으로 대기 시간(`2초 -> 4초 -> 8초`)을 늘리고, 매 대기 시 `0~500ms` 랜덤 지터를 가산하여 병목 현상(Thundering Herd)을 원천 방지함.
* **예외 분류 기반 429와 503의 명확한 분기**:
  * 예외의 원인(`getCause()`)을 재귀적으로 탐색하여 구글 SDK의 `ApiException` 상태 코드가 **429(Rate Limit)**인 경우는 리트라이 없이 **즉시 실패(Fast Fail)** 시켜 불필요한 대기와 컴퓨터 자원 낭비를 차단함.
  * **503 및 네트워크 타임아웃** 예외에 대해서만 안정적으로 3회 리트라이를 적용함.
* **최종 실패 복구**:
  * 3회 재시도가 모두 실패하면 최종 예외가 서비스단으로 던져져 DB에 `FAILED_AT_VERIFYING`으로 정상 기록되도록 보장함.

---

## 5. Spring `@Retryable` 대신 직접 자바 코드로 짠 이유

1. **동시성 제어 락(Semaphore)과의 유기적 라이프사이클 결합**
   * `@Retryable`은 프록시(AOP) 방식으로 작동하므로 락 획득 ➔ 실패 ➔ 락 반환 ➔ 대기 ➔ 재획득의 정밀한 세마포어 주기 제어가 어려움.
2. **복잡한 래핑 예외의 재귀적 파싱 가능**
   * 라이브러리 포장지에 가려진 실제 구글 원인 예외(`ApiException`)의 HTTP 코드(429/503)를 정교하게 탐색하여 분기할 수 있음.
3. **AOP 자가 참조(Self-invocation) 제약 및 의존성 격리**
   * 외부 프록시 제약 및 추가 스프링 AOP 라이브러리 종속 없이 100% 동작 신뢰성을 확보함.

---

## 6. 향후 아키텍처 발전 방향 (Future Work)

1. **분산 메시지 큐(Message Queue) 도입**: 비동기 작업을 Redis, RabbitMQ, Kafka 등으로 이전하여 Pod가 죽어도 데이터가 유실되지 않도록 영속성 강화.
2. **데드 레터 큐(DLQ) 및 재처리 UI 구축**: 최종 실패한 지식을 DLQ에 격리하고, 대시보드 화면에 실패한 리스트와 [재처리 실행] 버튼을 연동하여 관리자가 간편하게 재시도할 수 있는 수동 오퍼레이션 환경 구현.
3. **K8s / K3s 연동 및 HPA 확장**: AI 연산 병목 시 Pod를 스케일 아웃하여 대처함.
