# ADR-002: 비동기 메시징 솔루션을 어떻게 선택할 것인가

## Overview

ai-pilot은 Kubernetes 환경에서 AI를 활용해 장애를 진단하고 운영 작업을 지원하는 DevOps
Platform이자, 기술 지식을 검수하고 저장하는 개인 지식 시스템입니다.

시스템 내부에서는 Spring Boot가 전체 작업의 Orchestration을 담당하고, Go 기반 Worker가
Kubernetes(k3s) 리소스 제어 및 비동기 작업을 담당하는 구조로 구성했습니다.

두 컴포넌트 사이의 작업 흐름을 설계하면서 단순한 작업 큐(Job Queue)를 사용할 것인지, 아니면
시스템의 상태 변화와 작업 결과를 전달하는 이벤트 기반 구조(Event-Driven Workflow)를 사용할
것인지 고민했고, Redis / RabbitMQ / Apache Kafka / NATS를 후보로 두고 비교했습니다.

## Motivation

ai-pilot의 지식 처리 과정은 하나의 요청으로 끝나는 단순한 작업이 아니라, 각 단계마다 실패
가능성을 포함해 여러 상태를 거치며 진행됩니다.

```
DRAFT
  ↓
VERIFYING ──────→ FAILED_AT_VERIFYING
  ↓
FORMATTING ─────→ FAILED_AT_FORMATTING
  ↓
REVIEW_READY
  ↓
REVIEW_APPROVED
  ↓
NOTION_PUBLISHING ─→ FAILED_AT_NOTION_PUBLISH
  ↓
VECTOR_INDEXING ───→ FAILED_AT_VECTOR_INDEX
  ↓
PUBLISHED
```

주요 단계(VERIFYING, FORMATTING, NOTION*PUBLISHING, VECTOR_INDEXING)마다 대응하는
`FAILED_AT*\*` 상태를 별도로 두어, 파이프라인의 어느 지점에서 실패했는지 구분할 수 있도록
설계했습니다.

또한 Spring Boot와 Go는 서로 다른 언어와 역할을 가지고 있습니다.

- Spring Boot → 전체 작업 Orchestration
- Go → Kubernetes(k3s) 리소스 제어 및 비동기 Worker

따라서 두 컴포넌트 사이의 통신을 단순히 일감을 전달하는 Job Queue로만 볼 것인지, 각 단계의
이벤트와 작업 결과를 전달하는 메시징 백본으로 구성할 것인지 결정할 필요가 있었습니다.

특히 시스템이 k3s 환경(로컬 홈랩, 저사양 단일 노드)에서 동작하기 때문에, 대규모 인프라를
전제로 하는 메시징 시스템보다는 제한된 리소스에서도 가볍게 운영할 수 있는 솔루션을
우선적으로 고려했습니다.

## 검토한 대안

### Redis

Redis Streams / List를 이용하면 비교적 간단하게 작업 큐를 구성할 수 있습니다. 이미 익숙한
데이터 저장소를 이용해 빠르게 구현할 수 있다는 장점이 있지만, 단순 작업 큐보다는 서비스 간
이벤트 기반 워크플로우를 구성하는 것이 목표였기 때문에, Redis를 이벤트 메시징 백본으로
확장했을 때의 적합성을 우선순위에서 낮게 판단했습니다.

### RabbitMQ

AMQP 기반 메시지 브로커로 Exchange, Routing, Queue, Dead Letter 등의 기능을 제공해 전통적인
비동기 작업 분배와 실패 재처리에 강점이 있습니다. 다만 현재 시스템의 요구 수준에 비해 이런
기능이 어느 정도까지 필요한지, 그리고 k3s 환경에서 추가 메시징 인프라를 운영하는 비용을
함께 고려해야 했습니다.

### Apache Kafka

대규모 이벤트 스트리밍과 장기간의 이벤트 로그 보존에 강점이 있는 솔루션입니다. 다만 현재
처리하려는 이벤트 규모와 시스템 목적을 고려했을 때, Kafka가 제공하는 대규모 스트리밍
플랫폼으로서의 기능은 과했습니다. 저사양 단일 노드로 구성된 k3s 환경에서는 운영 복잡성과
리소스 요구량 자체가 중요한 판단 기준이었습니다.

### NATS

Go로 개발된 클라우드 네이티브 메시징 시스템으로, Pub/Sub와 Request-Response 패턴을 모두
제공하며 JetStream을 통해 메시지 영속화와 재처리도 구성할 수 있습니다. Go 기반 Worker와
자연스럽게 결합할 수 있고, k3s 환경에서 상대적으로 가볍게 운영할 수 있다는 점이 요구사항과
잘 맞았습니다.

## 결정

NATS를 선택했습니다.

### 1. Event-Driven Workflow 구성

지식 처리 도메인은 `knowledge.>` 와일드카드 subject로 구독하여, Spring Boot와 Go Worker가
서로의 내부 구현에 직접 의존하지 않고 이벤트 기준으로 연동합니다. K8s 진단처럼 즉각적인
응답이 필요한 요청은 `ops.diagnose.request`처럼 별도의 subject 네임스페이스(`ops.*`)를 두어,
지식 처리(이벤트 기반 워크플로우)와 운영 진단(request-reply)을 구조적으로 분리했습니다.

### 2. Go Worker와의 결합

Go 기반 Worker가 Kubernetes 리소스를 제어하는 역할을 담당하기 때문에, Go 생태계와 자연스럽게
결합할 수 있는 메시징 시스템이라는 점도 중요한 선택 기준이었습니다.

### 3. k3s 환경과 리소스 제약

ai-pilot은 로컬에 직접 설치한 저사양 단일 노드 k3s 환경에서 동작하며, 트래픽 규모나 처리하는
데이터 양 자체도 크지 않습니다. 이런 환경에서 메시징 시스템 자체의 리소스 사용량과 운영
부담을 최소화하는 것이 중요했고, NATS는 이 조건에 맞는 선택지였습니다.

### 4. JetStream을 통한 영속화

실제로 JetStream을 사용하고 있으며, 아래와 같이 Connection에서 JetStream 컨텍스트를 얻어
메시지 영속화와 재처리가 필요한 흐름에 활용합니다.

```java
public JetStream getJetStream() throws IOException {
    return connection.jetStream();
}
```

## 결과 / 트레이드오프

**얻은 것**

- Spring Boot의 Orchestration 책임과 Go Worker의 Kubernetes 제어 책임을 이벤트 기준으로
  분리했다.
- `knowledge.>`(이벤트 기반 워크플로우)와 `ops.*`(request-reply)로 subject 네임스페이스를
  나눠, 성격이 다른 두 통신 패턴을 한 메시징 시스템 안에서 명확히 구분해 사용하고 있다.
- 별도의 메시징 인프라 추가 없이 Pub/Sub, Request-Response, JetStream 영속화까지 하나의
  기술로 커버했다.

**포기한 것 / 한계**

- RabbitMQ의 Dead Letter Queue 같은 정교한 실패 재처리 기능은 NATS core에 기본으로 없어,
  필요한 부분은 JetStream이나 애플리케이션 레벨에서 직접 구성해야 한다.
- 지금까지는 트래픽과 데이터 규모가 크지 않아 NATS/JetStream이 리소스나 처리량 측면에서
  한계를 드러낸 적이 없다. 즉 이 선택이 대규모 트래픽 환경에서도 유효한지는 실제로
  검증되지 않았고, 현재 규모에서의 적합성만 확인된 상태다.
- Kafka 대비 생태계·모니터링 도구 성숙도가 낮아, 향후 운영 복잡도가 늘어나면 별도 도구를
  직접 구축해야 할 수 있다.
