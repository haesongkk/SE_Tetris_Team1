# P2P 네트워크 오류 처리 메커니즘 분석 및 테스트 보고서

## 📋 프로젝트 개요

이 문서는 SE_Tetris_Team1 프로젝트의 P2P 네트워크 오류 처리 메커니즘을 분석하고, 해당 메커니즘이 올바르게 구현되었는지 검증하는 JUnit 기반 단위 테스트를 설명합니다.

---

## 🔍 네트워크 오류 처리 메커니즘 분석

### 1. **핵심 오류 처리 메커니즘** (`P2PBase.java`)

#### 1.1 `handleNetworkError(IOException e)`
```java
private void handleNetworkError(IOException e) {
    // 중복 오류 처리 방지
    if (isHandlingError || !bRunning) {
        return;
    }
    
    isHandlingError = true;
    bRunning = false;
    
    System.err.println("P2P: 네트워크 오류 발생 - " + e.getMessage());
    e.printStackTrace();
    
    // 사용자에게 알림 (onDisconnect 콜백 호출)
    if (onDisconnect != null) {
        try {
            onDisconnect.run();
        } catch (Exception ex) {
            System.err.println("P2P: onDisconnect 콜백 실행 중 오류: " + ex.getMessage());
        }
    }
}
```

**주요 기능:**
- ✅ 중복 오류 처리 방지 (`isHandlingError` 플래그)
- ✅ 네트워크 루프 종료 (`bRunning = false`)
- ✅ 오류 로깅 및 스택 트레이스 출력
- ✅ `onDisconnect` 콜백 안전 호출 (콜백 내 예외 처리)

#### 1.2 타임아웃 메커니즘
```java
private static final int TIMEOUT_MS = 5000;

// 타임아웃 검사
if(currentTime - lastReceiveTime > TIMEOUT_MS) {
    if(bWaitingPong) {
        if(currentTime - lastPingTime > TIMEOUT_MS) {
            System.out.println("타임아웃 발생: " + TIMEOUT_MS + "ms 이상 응답 없음");
            break;
        }
    } else {
        send(PING_MESSAGE);
        bWaitingPong = true;
        lastPingTime = currentTime;
    }
}
```

**주요 기능:**
- ✅ 5초(5000ms) 무응답 시 ping 전송
- ✅ ping 전송 후 5초 동안 pong 미수신 시 연결 종료
- ✅ 게임 상태 전송 주기(100ms)와 분리된 독립적 타임아웃 감지

#### 1.3 스트림 null 체크
```java
public void send(String message) { 
    if (out == null) {
        System.err.println("P2P: 출력 스트림이 null입니다. 메시지 전송 실패: " + message);
        handleNetworkError(new IOException("출력 스트림이 null입니다"));
        return;
    }
    // ...
}
```

**주요 기능:**
- ✅ null 스트림 감지 시 `handleNetworkError` 호출
- ✅ 예외 전파 대신 안전한 오류 처리

---

### 2. **게임 레벨 오류 처리** (`P2PBattleScene.java`)

#### 2.1 지연(Latency) 모니터링
```java
final long MAX_LATENCY_MS = 200;

private void handleLatency(long latency) {
    // 지연 히스토리 관리
    latencyHistory.offer(latency);
    if (latencyHistory.size() > LATENCY_HISTORY_SIZE) {
        latencyHistory.poll();
    }
    
    // 연결 끊김 판단: 지속적 높은 지연
    if (latency > MAX_LATENCY_MS && averageLatency > MAX_LATENCY_MS && latencyHistory.size() >= 3) {
        boolean allHighLatency = latencyHistory.stream()
            .allMatch(l -> l > MAX_LATENCY_MS);
        
        if (allHighLatency) {
            System.out.println("연결 끊김 판단: 지속적 높은 지연 (" + averageLatency + "ms 평균)");
            SwingUtilities.invokeLater(() -> {
                showDisconnectDialog();
            });
        }
    }
}
```

**주요 기능:**
- ✅ 네트워크 지연 시간 실시간 모니터링
- ✅ 평균 지연 시간 계산 (최근 10개 샘플)
- ✅ 일시적 지연과 연결 문제 구분
  - 일시적 지연: 100-200ms (게임 계속 진행, UI 경고)
  - 연결 문제: 최근 3개 샘플 모두 200ms 초과 → 연결 종료

#### 2.2 연결 끊김 처리
```java
p2p.setOnDisconnect(() -> {
    SwingUtilities.invokeLater(() -> {
        showDisconnectDialog();
    });
});

private void showDisconnectDialog() {
    if(bCloseByDisconnect) return;
    bCloseByDisconnect = true;
    
    // 연결 끊김 다이얼로그 표시
    // 사용자에게 메인 메뉴로 복귀 안내
}
```

**주요 기능:**
- ✅ UI 스레드에서 안전한 다이얼로그 표시
- ✅ 중복 다이얼로그 방지
- ✅ 리소스 정리 및 메인 메뉴 복귀

---

## 🧪 구현된 단위 테스트

### 테스트 파일: `P2PNetworkErrorHandlingTest.java`

총 **11개**의 테스트 케이스가 구현되어 있으며, 다음 6가지 카테고리로 분류됩니다:

### 1. **handleNetworkError() 메커니즘 테스트** (3개)

#### 1-1. null 출력 스트림 전송 시 오류 처리
```java
@Test
void testHandleNetworkErrorOnNullOutputStream()
```
- **목적**: 연결되지 않은 상태에서 `send()` 호출 시 `handleNetworkError` 정상 동작 확인
- **검증 사항**:
  - ✅ `handleNetworkError`가 `onDisconnect` 콜백 호출
  - ✅ 예외 발생 없이 안전하게 처리

#### 1-2. 소켓 강제 종료 시 오류 처리
```java
@Test
void testHandleNetworkErrorOnSocketClose()
```
- **목적**: 네트워크 오류 시뮬레이션 (소켓 강제 종료)
- **검증 사항**:
  - ✅ 서버가 클라이언트 소켓 종료 감지
  - ✅ `handleNetworkError` → `onDisconnect` 콜백 체인 동작

#### 1-3. onDisconnect 콜백 예외 처리
```java
@Test
void testHandleNetworkErrorWithCallbackException()
```
- **목적**: 콜백 내부 예외가 프로그램 종료를 야기하지 않는지 확인
- **검증 사항**:
  - ✅ 콜백 예외 발생 시에도 프로그램 계속 실행
  - ✅ 예외가 안전하게 catch됨

---

### 2. **타임아웃 메커니즘 테스트** (2개)

#### 2-1. 타임아웃 발생 시 연결 종료
```java
@Test
void testTimeoutDisconnection()
```
- **목적**: 5초(5000ms) 무응답 시 자동 연결 종료 확인
- **검증 사항**:
  - ✅ 클라이언트 소켓 종료 후 타임아웃 감지
  - ✅ 서버의 `onDisconnect` 콜백 호출

#### 2-2. Ping/Pong으로 타임아웃 방지
```java
@Test
void testPingPongPreventTimeout()
```
- **목적**: Ping/Pong 메커니즘이 정상 작동하여 타임아웃 방지 확인
- **검증 사항**:
  - ✅ 6초 이상 연결 유지 (타임아웃 5초 초과)
  - ✅ 연결 끊김 없이 정상 작동

---

### 3. **중복 오류 처리 방지 테스트** (1개)

#### 3-1. isHandlingError 플래그 동작 확인
```java
@Test
void testDuplicateErrorHandlingPrevention()
```
- **목적**: 동일 오류에 대해 `onDisconnect` 콜백이 여러 번 호출되지 않는지 확인
- **검증 사항**:
  - ✅ 여러 번 `send()` 호출 시 `onDisconnect`는 1번만 실행
  - ✅ `isHandlingError` 플래그 정상 동작

---

### 4. **리소스 정리 테스트** (2개)

#### 4-1. release() 메서드의 안전한 리소스 정리
```java
@Test
void testSafeResourceCleanup()
```
- **목적**: `release()` 호출 시 모든 리소스가 안전하게 정리되는지 확인
- **검증 사항**:
  - ✅ 소켓 정상 종료
  - ✅ 서버 소켓 정상 종료
  - ✅ 예외 발생 없음

#### 4-2. 중복 release() 호출 안전성
```java
@Test
void testMultipleReleaseCallsSafety()
```
- **목적**: 여러 번 `release()` 호출 시 예외 발생하지 않는지 확인
- **검증 사항**:
  - ✅ 3번 연속 `release()` 호출 시 예외 없음

---

### 5. **null 메시지 수신 처리 테스트** (1개)

#### 5-1. null 메시지 수신 시 연결 종료
```java
@Test
void testNullMessageDisconnection()
```
- **목적**: 상대방 스트림 종료 시 null 메시지 감지 및 처리 확인
- **검증 사항**:
  - ✅ null 메시지 감지 시 연결 종료
  - ✅ `onDisconnect` 콜백 호출

---

### 6. **통합 시나리오 테스트** (2개)

#### 6-1. 전체 오류 복구 시나리오
```java
@Test
void testCompleteErrorRecoveryScenario()
```
- **목적**: 오류 발생 → 감지 → 처리 → 재연결 전체 시나리오 검증
- **검증 사항**:
  - ✅ 첫 번째 연결 성공
  - ✅ 소켓 강제 종료로 오류 발생
  - ✅ `handleNetworkError` → `onDisconnect` 실행
  - ✅ 새로운 서버로 재연결 성공

#### 6-2. 동시 다발적 오류 상황 처리
```java
@Test
void testConcurrentErrorHandling()
```
- **목적**: 멀티스레드 환경에서 동시 오류 발생 시 안전성 확인
- **검증 사항**:
  - ✅ 5개 스레드에서 동시 `send()` 호출
  - ✅ `onDisconnect`는 1번만 실행 (중복 방지)

---

## 📊 테스트 실행 방법

### 1. 전체 네트워크 오류 처리 테스트 실행
```powershell
.\gradlew test --tests "tetris.network.P2PNetworkErrorHandlingTest"
```

### 2. 특정 테스트만 실행
```powershell
# handleNetworkError 테스트만 실행
.\gradlew test --tests "tetris.network.P2PNetworkErrorHandlingTest.testHandleNetworkErrorOnNullOutputStream"

# 타임아웃 테스트만 실행
.\gradlew test --tests "tetris.network.P2PNetworkErrorHandlingTest.testTimeoutDisconnection"

# 통합 시나리오 테스트만 실행
.\gradlew test --tests "tetris.network.P2PNetworkErrorHandlingTest.testCompleteErrorRecoveryScenario"
```

### 3. 전체 네트워크 테스트 실행 (기존 + 새 테스트)
```powershell
.\gradlew test --tests "tetris.network.*"
```

---

## ✅ 테스트 결과 예상

모든 테스트가 성공하면 다음과 같은 결과를 볼 수 있습니다:

```
P2P 네트워크 오류 처리 메커니즘 테스트 > 1-1. null 출력 스트림 전송 시 handleNetworkError 호출 확인 PASSED
P2P 네트워크 오류 처리 메커니즘 테스트 > 1-2. 소켓 강제 종료 시 handleNetworkError 호출 확인 PASSED
P2P 네트워크 오류 처리 메커니즘 테스트 > 1-3. onDisconnect 콜백 예외 처리 확인 PASSED
P2P 네트워크 오류 처리 메커니즘 테스트 > 2-1. 타임아웃 발생 시 연결 종료 확인 PASSED
P2P 네트워크 오류 처리 메커니즘 테스트 > 2-2. Ping/Pong 메커니즘으로 타임아웃 방지 확인 PASSED
P2P 네트워크 오류 처리 메커니즘 테스트 > 3-1. isHandlingError 플래그로 중복 처리 방지 확인 PASSED
P2P 네트워크 오류 처리 메커니즘 테스트 > 4-1. release() 메서드의 안전한 리소스 정리 확인 PASSED
P2P 네트워크 오류 처리 메커니즘 테스트 > 4-2. 중복 release() 호출 시 안전성 확인 PASSED
P2P 네트워크 오류 처리 메커니즘 테스트 > 5-1. null 메시지 수신 시 연결 종료 확인 PASSED
P2P 네트워크 오류 처리 메커니즘 테스트 > 6-1. 전체 오류 복구 시나리오 테스트 PASSED
P2P 네트워크 오류 처리 메커니즘 테스트 > 6-2. 동시 다발적 오류 상황 처리 테스트 PASSED

✅ 모든 테스트가 성공했습니다!
```

---

## 🔧 구현 품질 평가

### ✅ **잘 구현된 부분**

1. **중복 오류 처리 방지**
   - `isHandlingError` 플래그로 안전하게 구현
   - 멀티스레드 환경에서도 안정적

2. **onDisconnect 콜백 예외 처리**
   - try-catch로 콜백 내 예외를 안전하게 처리
   - 콜백 예외가 전체 시스템에 영향을 주지 않음

3. **타임아웃 메커니즘**
   - Ping/Pong으로 정확한 연결 상태 감지
   - 5초 타임아웃은 게임 플레이에 적절한 값

4. **리소스 정리**
   - `release()` 메서드가 안전하게 구현됨
   - 중복 호출 시에도 예외 발생하지 않음

5. **지연 모니터링**
   - 일시적 지연과 실제 연결 문제를 구분
   - 최근 3개 샘플 기반 판단으로 false positive 방지

### ⚠️ **개선 가능한 부분**

1. **로깅 개선**
   - `System.err.println()` 대신 Logger 프레임워크 사용 고려
   - 로그 레벨 구분 (ERROR, WARN, INFO, DEBUG)

2. **테스트 커버리지**
   - 현재 테스트는 기본 시나리오 위주
   - 엣지 케이스 추가 가능 (매우 빠른 연속 오류, 부분 패킷 손실 등)

---

## 📝 결론

SE_Tetris_Team1 프로젝트의 P2P 네트워크 오류 처리 메커니즘은 다음과 같이 평가됩니다:

### ✅ **강점**
- 체계적인 오류 감지 및 처리 구조
- 사용자 친화적인 오류 알림 (onDisconnect 콜백)
- 안전한 리소스 정리
- 중복 처리 방지로 안정성 확보

### 🎯 **테스트 커버리지**
- **11개** 단위 테스트로 주요 오류 시나리오 검증
- handleNetworkError, 타임아웃, 리소스 정리, 복구 등 핵심 기능 테스트
- 통합 테스트로 전체 시나리오 검증

### 📈 **권장 사항**
1. 현재 테스트 정기적으로 실행하여 회귀 방지
2. 실제 네트워크 환경에서 추가 테스트 수행
3. 로깅 시스템 개선 고려
4. 성능 테스트 추가 (대량 메시지 전송, 장시간 연결 등)

---

## 📚 참고 자료

- `P2PBase.java` - 핵심 네트워크 오류 처리 로직
- `P2PBattleScene.java` - 게임 레벨 오류 처리 및 지연 모니터링
- `P2PNetworkTest.java` - 기존 네트워크 기능 테스트
- `P2PNetworkErrorHandlingTest.java` - 새로 추가된 오류 처리 테스트
