# 트랜잭션 이벤트 리스너(TransactionalEventListener) 이슈 분석 및 해결

## 🚨 문제 상황
`KnowledgeSaveService`에서 지식 저장이 완료된 후 `KnowledgeProcessedEvent`를 발행하고, 이를 수신한 `KnowledgeVectorListener`에서 `VectorMemoryService`를 통해 벡터 데이터를 저장(DB Write)하려고 했으나 실패함.

- **발생 에러**: `No active transaction` 또는 저장 로직이 아무런 에러 없이 무시됨.
- **리스너 설정**: `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`

## 🔍 원인 분석 (Root Cause)

문제의 본질은 **DB 트랜잭션의 커밋 시점과 스프링 트랜잭션 컨텍스트의 종료 시점 간의 불일치(Mismatch)**에 있습니다.

### 1. Spring의 트랜잭션 커밋 라이프사이클 (`AbstractPlatformTransactionManager.processCommit()`)
1.  **실제 DB 커밋**: `doCommit(status)` 호출. (이 순간 물리적인 DB 트랜잭션은 종료되고 닫힘)
2.  **이벤트 발생**: `triggerAfterCommit(status)` 호출. (`@TransactionalEventListener(AFTER_COMMIT)`가 이때 실행됨)
3.  **트랜잭션 정리**: `cleanupAfterCompletion(status)` 호출. (스프링의 `ThreadLocal` 트랜잭션 메타데이터 정리)

### 2. Zombie Transaction Context
`AFTER_COMMIT` 리스너가 실행되는 '2번' 시점에는 물리적 DB 문은 닫혔지만, 스프링 메모리 상에는 아직 "현재 트랜잭션 중"이라는 상태(동기화 정보)가 남아있습니다.

이 상태에서 기본 `@Transactional(propagation = Propagation.REQUIRED)`이 걸린 서비스 메서드를 호출하면, 스프링은 **"아직 트랜잭션이 살아있네? 기존 트랜잭션에 묻어가자"**라고 판단하여 새로운 트랜잭션을 생성하지 않습니다.
하지만 하이버네이트(JPA)가 실제 DB에 쿼리를 날리려 하면, "이미 커밋되어 닫힌 세션"이므로 `No active transaction` 에러를 던지거나 영속성 컨텍스트 내에서만 변경되고 물리적 저장은 일어나지 않게 됩니다.

## 🛠️ 해결 방안 (Solution)

**`AFTER_COMMIT` 시점에서 새로운 DB 쓰기 작업이 필요하다면, 반드시 기존의 닫힌 트랜잭션 컨텍스트를 무시하고 완전히 새로운 트랜잭션을 강제로 열어야 합니다.**

### 해결 코드: `Propagation.REQUIRES_NEW` 적용
호출되는 대상 서비스(`VectorMemoryService`)의 메서드에 `@Transactional(propagation = Propagation.REQUIRES_NEW)`를 명시합니다.

```java
@Service
@RequiredArgsConstructor
public class VectorMemoryService {
    
    // ...

    /**
     * AFTER_COMMIT 리스너에서 호출되므로, 
     * 기존의 죽은 트랜잭션 컨텍스트를 무시하고 무조건 새로운 물리적 트랜잭션을 생성함.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveEmbedding(VectorTargetType targetType, Long targetId, String content) {
        float[] vector = embeddingModel.embed(content);
        VectorStoreEntity entity = VectorStoreEntity.createVectorStore(targetType, targetId, vector);
        vectorStoreRepository.save(entity);
    }
}
```

## 💡 아키텍처적 이점 (Architectural Value)
이러한 분리(Decoupling)와 새로운 트랜잭션 생성(`REQUIRES_NEW`)은 단순한 에러 해결을 넘어, 헥사고날 아키텍처에서 시스템의 결합도를 낮추는 데 큰 기여를 합니다.

1.  **데이터 정합성 보장**: 메인 도메인(`Knowledge`) 데이터가 DB에 완벽히 커밋된 이후에만 후속 작업(`Vector`)이 실행됨.
2.  **독립적 실패 격리**: 벡터 저장에 실패하더라도, 이미 커밋된 원본 지식 데이터는 롤백되지 않고 안전하게 보존됨. (후속 조치나 재시도 가능)
3.  **플러그인 구조 완성**: `SaveService`는 후속 작업의 성공 여부를 신경 쓰지 않고 이벤트만 던지며, 각 리스너는 자신의 독립된 트랜잭션 내에서 작업을 완수함.
