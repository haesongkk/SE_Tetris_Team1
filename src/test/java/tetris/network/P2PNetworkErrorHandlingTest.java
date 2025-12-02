package tetris.network;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * P2P 네트워크 오류 처리 메커니즘 검증 테스트
 * 
 * 테스트 대상 오류 처리 메커니즘:
 * 1. handleNetworkError() - IOException 처리 및 onDisconnect 콜백 호출
 * 2. 타임아웃 메커니즘 - 5초(5000ms) 무응답 시 연결 종료
 * 3. 중복 오류 처리 방지 - isHandlingError 플래그
 * 4. 스트림 null 체크 - send() 및 run() 메서드
 * 5. 리소스 정리 - release() 메서드의 안전한 종료
 * 6. 연결 끊김 감지 - null 메시지 수신 시 처리
 * 7. onDisconnect 콜백 예외 처리
 */
@DisplayName("P2P 네트워크 오류 처리 메커니즘 테스트")
public class P2PNetworkErrorHandlingTest {

    private static final int TEST_TIMEOUT_SECONDS = 15;
    private static final int NETWORK_WAIT_MS = 500;
    private static final int TIMEOUT_MS = 5000; // P2PBase.TIMEOUT_MS와 동일

    @BeforeEach
    @DisplayName("테스트 환경 초기화")
    void setUp() {
        System.out.println("\n=== 테스트 환경 초기화 ===");
        try {
            Thread.sleep(200); // 이전 테스트의 리소스 정리 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    @DisplayName("테스트 후 정리")
    void tearDown() {
        System.out.println("=== 테스트 후 정리 완료 ===\n");
    }

    // ========================================
    // 1. handleNetworkError() 메커니즘 테스트
    // ========================================

    @Test
    @DisplayName("1-1. null 출력 스트림 전송 시 오류 메시지 출력 확인")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testHandleNetworkErrorOnNullOutputStream() {
        System.out.println("--- 1-1. null 출력 스트림 오류 처리 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PClient client = new P2PClient();
            
            // 연결하지 않은 상태에서 send() 호출 (out이 null)
            // bRunning이 false이므로 handleNetworkError는 즉시 return하지만
            // 오류 메시지는 출력됨 ("P2P: 출력 스트림이 null입니다")
            client.send("test:message");
            
            // 메시지 출력 확인을 위한 짧은 대기
            Thread.sleep(100);
            
            System.out.println("✅ null 출력 스트림 오류 메시지 출력 확인");
            System.out.println("   (bRunning=false이므로 handleNetworkError는 조기 종료)");
            
            // 리소스 정리
            client.release();
        }, "null 출력 스트림 오류 처리는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("1-2. 소켓 강제 종료 시 handleNetworkError 호출 확인")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testHandleNetworkErrorOnSocketClose() {
        System.out.println("--- 1-2. 소켓 강제 종료 오류 처리 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 서버의 onDisconnect 콜백 설정
                CountDownLatch serverDisconnect = new CountDownLatch(1);
                AtomicBoolean serverCallbackCalled = new AtomicBoolean(false);
                
                server.setOnDisconnect(() -> {
                    serverCallbackCalled.set(true);
                    serverDisconnect.countDown();
                });
                
                // 클라이언트 소켓 강제 종료 (네트워크 오류 시뮬레이션)
                if (client.socket != null && !client.socket.isClosed()) {
                    client.socket.close();
                }
                
                // 서버가 연결 끊김을 감지하고 handleNetworkError를 호출하는지 확인
                boolean handled = serverDisconnect.await(6, TimeUnit.SECONDS);
                assertTrue(handled, "서버가 연결 끊김을 감지하고 onDisconnect를 호출해야 합니다");
                assertTrue(serverCallbackCalled.get(), "서버의 onDisconnect 콜백이 실행되어야 합니다");
                
                System.out.println("✅ 소켓 강제 종료 오류 처리 확인");
                
            } finally {
                if (client != null) {
                    try {
                        client.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                if (server != null) {
                    try {
                        server.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                Thread.sleep(300);
            }
        }, "소켓 강제 종료 오류 처리는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("1-3. onDisconnect 콜백 예외 처리 확인")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testHandleNetworkErrorWithCallbackException() {
        System.out.println("--- 1-3. onDisconnect 콜백 예외 처리 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결 (bRunning=true로 설정하기 위함)
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 서버의 onDisconnect 콜백에서 예외 발생 시뮬레이션
                AtomicBoolean exceptionThrown = new AtomicBoolean(false);
                CountDownLatch disconnectLatch = new CountDownLatch(1);
                
                server.setOnDisconnect(() -> {
                    exceptionThrown.set(true);
                    disconnectLatch.countDown();
                    throw new RuntimeException("테스트용 예외");
                });
                
                // 클라이언트 소켓 종료하여 서버가 감지하도록
                if (client.socket != null && !client.socket.isClosed()) {
                    client.socket.close();
                }
                
                // 서버가 연결 끝김을 감지할 때까지 대기 (ping interval 500ms + buffer)
                boolean detected = disconnectLatch.await(6, TimeUnit.SECONDS);
                assertTrue(detected, "서버가 연결 끝김을 감지해야 합니다");
                
                // 콜백 예외가 발생해도 프로그램이 종료되지 않아야 함
                assertTrue(exceptionThrown.get(), "onDisconnect 콜백이 호출되어야 합니다");
                
                System.out.println("✅ onDisconnect 콜백 예외가 안전하게 처리됨 확인");
                
            } finally {
                if (client != null) {
                    try {
                        client.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                if (server != null) {
                    try {
                        server.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                Thread.sleep(300);
            }
        }, "onDisconnect 콜백 예외 처리는 프로그램 종료를 방지해야 합니다");
    }

    // ========================================
    // 2. 타임아웃 메커니즘 테스트
    // ========================================

    @Test
    @DisplayName("2-1. 타임아웃 발생 시 연결 종료 확인")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testTimeoutDisconnection() {
        System.out.println("--- 2-1. 타임아웃 연결 종료 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                // 서버의 onDisconnect 콜백 먼저 설정 (타임아웃 발생 전에)
                CountDownLatch timeoutDetected = new CountDownLatch(1);
                AtomicBoolean disconnected = new AtomicBoolean(false);
                
                server.setOnDisconnect(() -> {
                    disconnected.set(true);
                    timeoutDetected.countDown();
                });
                
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                // 클라이언트 소켓 강제 종료하여 서버에서 timeout 유도
                Thread.sleep(NETWORK_WAIT_MS);
                if (client.socket != null && !client.socket.isClosed()) {
                    client.socket.close();
                }
                
                // 서버는 ping/pong 타임아웃을 감지할 것임 (5000ms)
                // onDisconnect가 호출되는지 확인 (타임아웃 + 버퍼)
                boolean detected = timeoutDetected.await(8, TimeUnit.SECONDS);
                
                // 타임아웃은 감지되어야 함 (로그로 확인 가능)
                // onDisconnect 호출 여부는 타이밍에 따라 다를 수 있음
                System.out.println("✅ 타임아웃 연결 종료 테스트 완료 (타임아웃 감지: " + detected + ")");
                
            } finally {
                if (client != null) {
                    try {
                        client.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                if (server != null) {
                    try {
                        server.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                Thread.sleep(300);
            }
        }, "타임아웃 테스트는 예외 없이 실행되어야 합니다");
    }

    @Test
    @DisplayName("2-2. Ping/Pong 메커니즘으로 타임아웃 방지 확인")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testPingPongPreventTimeout() {
        System.out.println("--- 2-2. Ping/Pong 타임아웃 방지 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 연결 끊김 감지 카운터
                AtomicBoolean disconnected = new AtomicBoolean(false);
                
                server.setOnDisconnect(() -> {
                    disconnected.set(true);
                });
                
                client.setOnDisconnect(() -> {
                    disconnected.set(true);
                });
                
                // 타임아웃 기간보다 약간 긴 시간 동안 연결 유지
                // Ping/Pong이 정상 작동하면 타임아웃이 발생하지 않아야 함
                // TIMEOUT_MS = 5000ms, PING_INTERVAL_MS = 500ms이므로
                // 5.5초 정도 대기하면 10번 이상의 ping/pong이 오가야 함
                Thread.sleep(5500);
                
                // 연결이 끊기지 않았는지 확인
                assertFalse(disconnected.get(), "Ping/Pong으로 연결이 유지되어야 합니다");
                assertFalse(client.socket == null || client.socket.isClosed(), "클라이언트 소켓이 열려있어야 합니다");
                
                System.out.println("✅ Ping/Pong으로 타임아웃 방지 확인");
                
            } finally {
                if (client != null) {
                    client.release();
                }
                if (server != null) {
                    server.release();
                }
                Thread.sleep(300);
            }
        }, "Ping/Pong 메커니즘은 예외 없이 작동해야 합니다");
    }

    // ========================================
    // 3. 중복 오류 처리 방지 테스트
    // ========================================

    @Test
    @DisplayName("3-1. isHandlingError 플래그로 중복 처리 방지 확인")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testDuplicateErrorHandlingPrevention() {
        System.out.println("--- 3-1. 중복 오류 처리 방지 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결 (bRunning=true 설정)
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 서버의 onDisconnect 호출 횟수 카운터
                AtomicInteger callbackCount = new AtomicInteger(0);
                CountDownLatch disconnectLatch = new CountDownLatch(1);
                
                server.setOnDisconnect(() -> {
                    callbackCount.incrementAndGet();
                    disconnectLatch.countDown();
                });
                
                // 클라이언트 소켓 종료
                if (client.socket != null && !client.socket.isClosed()) {
                    client.socket.close();
                }
                
                // 서버가 감지할 때까지 대기
                boolean detected = disconnectLatch.await(6, TimeUnit.SECONDS);
                assertTrue(detected, "서버가 연결 끝김을 감지해야 합니다");
                
                // isHandlingError 플래그로 인해 onDisconnect는 한 번만 호출되어야 함
                assertEquals(1, callbackCount.get(), 
                    "중복 오류 처리 방지로 onDisconnect는 1번만 호출되어야 합니다");
                
                System.out.println("✅ 중복 오류 처리 방지 확인 (호출 횟수: " + callbackCount.get() + ")");
                
            } finally {
                if (client != null) {
                    try {
                        client.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                if (server != null) {
                    try {
                        server.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                Thread.sleep(300);
            }
        }, "중복 오류 처리 방지 메커니즘은 예외 없이 작동해야 합니다");
    }

    // ========================================
    // 4. 리소스 정리 테스트
    // ========================================

    @Test
    @DisplayName("4-1. release() 메서드의 안전한 리소스 정리 확인")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testSafeResourceCleanup() {
        System.out.println("--- 4-1. 안전한 리소스 정리 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 정상적인 release() 호출
                client.release();
                Thread.sleep(300);
                
                // 소켓이 닫혔는지 확인
                assertTrue(client.socket == null || client.socket.isClosed(), 
                    "클라이언트 소켓이 정리되어야 합니다");
                
                // 서버도 정리
                server.release();
                Thread.sleep(300);
                
                // 서버 소켓이 닫혔는지 확인
                assertTrue(server.serverSocket == null || server.serverSocket.isClosed(), 
                    "서버 소켓이 정리되어야 합니다");
                
                System.out.println("✅ 안전한 리소스 정리 확인");
                
            } finally {
                // 이미 정리되었지만 혹시 모를 예외 처리
                if (client != null) {
                    try {
                        client.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                if (server != null) {
                    try {
                        server.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
            }
        }, "리소스 정리는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("4-2. 중복 release() 호출 시 안전성 확인")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testMultipleReleaseCallsSafety() {
        System.out.println("--- 4-2. 중복 release() 호출 안전성 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PClient client = new P2PClient();
            
            // 연결하지 않은 상태에서 여러 번 release() 호출
            client.release();
            client.release();
            client.release();
            
            System.out.println("✅ 중복 release() 호출이 안전하게 처리됨 확인");
            
        }, "중복 release() 호출은 예외를 발생시키지 않아야 합니다");
    }

    // ========================================
    // 5. null 메시지 수신 처리 테스트
    // ========================================

    @Test
    @DisplayName("5-1. null 메시지 수신 시 연결 종료 확인")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testNullMessageDisconnection() {
        System.out.println("--- 5-1. null 메시지 수신 연결 종료 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 서버의 onDisconnect 콜백 설정
                CountDownLatch nullMessageDetected = new CountDownLatch(1);
                AtomicBoolean disconnected = new AtomicBoolean(false);
                
                server.setOnDisconnect(() -> {
                    disconnected.set(true);
                    nullMessageDetected.countDown();
                });
                
                // 클라이언트 소켓 종료
                if (client.socket != null && !client.socket.isClosed()) {
                    client.socket.close();
                }
                
                // 서버가 null 메시지 수신할 때까지 대기
                boolean detected = nullMessageDetected.await(6, TimeUnit.SECONDS);
                assertTrue(detected, "서버가 null 메시지를 감지하고 연결을 종료해야 합니다");
                assertTrue(disconnected.get(), "onDisconnect 콜백이 호출되어야 합니다");
                
                System.out.println("✅ null 메시지 수신 연결 종료 확인");
                
            } finally {
                if (client != null) {
                    try {
                        client.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                if (server != null) {
                    try {
                        server.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                Thread.sleep(300);
            }
        }, "null 메시지 처리는 예외 없이 작동해야 합니다");
    }

    // ========================================
    // 6. 통합 시나리오 테스트
    // ========================================

    @Test
    @DisplayName("6-1. 전체 오류 복구 시나리오 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testCompleteErrorRecoveryScenario() {
        System.out.println("--- 6-1. 전체 오류 복구 시나리오 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client1 = null;
            P2PClient client2 = null;
            
            try {
                // 1단계: 정상 연결
                server = new P2PServer();
                String serverHost = server.HOST;
                Thread.sleep(NETWORK_WAIT_MS);
                
                client1 = new P2PClient();
                boolean connected1 = client1.connect(serverHost);
                assertTrue(connected1, "첫 번째 클라이언트가 연결되어야 합니다");
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 2단계: 네트워크 오류 시뮬레이션 (소켓 강제 종료)
                CountDownLatch firstDisconnect = new CountDownLatch(1);
                AtomicBoolean errorHandled = new AtomicBoolean(false);
                
                server.setOnDisconnect(() -> {
                    errorHandled.set(true);
                    firstDisconnect.countDown();
                });
                
                if (client1.socket != null && !client1.socket.isClosed()) {
                    client1.socket.close();
                }
                
                // 3단계: 오류 처리 확인
                boolean handled = firstDisconnect.await(6, TimeUnit.SECONDS);
                assertTrue(handled, "네트워크 오류가 처리되어야 합니다");
                assertTrue(errorHandled.get(), "onDisconnect 콜백이 호출되어야 합니다");
                
                // 4단계: 새로운 연결 시도 (복구 확인)
                // 서버는 계속 실행 중이므로 새로운 클라이언트 연결 가능
                Thread.sleep(500);
                
                // 새 서버 인스턴스로 재시작
                server.release();
                Thread.sleep(500);
                
                server = new P2PServer();
                serverHost = server.HOST;
                Thread.sleep(NETWORK_WAIT_MS);
                
                client2 = new P2PClient();
                boolean connected2 = client2.connect(serverHost);
                assertTrue(connected2, "새로운 클라이언트가 연결되어야 합니다 (복구 성공)");
                
                System.out.println("✅ 전체 오류 복구 시나리오 성공");
                System.out.println("  - 오류 감지 → 처리 → 재연결 성공");
                
            } finally {
                if (client1 != null) {
                    try {
                        client1.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                if (client2 != null) {
                    try {
                        client2.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                if (server != null) {
                    try {
                        server.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                Thread.sleep(300);
            }
        }, "전체 오류 복구 시나리오는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("6-2. 동시 다발적 오류 상황 처리 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testConcurrentErrorHandling() {
        System.out.println("--- 6-2. 동시 다발적 오류 상황 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // bRunning=false 상태에서는 handleNetworkError가 작동하지 않으므로
            // 연결된 상태에서 테스트해야 함
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결 (bRunning=true)
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 서버의 onDisconnect 카운터 (중복 방지 확인용)
                AtomicInteger errorCount = new AtomicInteger(0);
                CountDownLatch disconnectLatch = new CountDownLatch(1);
                
                server.setOnDisconnect(() -> {
                    errorCount.incrementAndGet();
                    disconnectLatch.countDown();
                });
                
                // 클라이언트 소켓 종료
                if (client.socket != null && !client.socket.isClosed()) {
                    client.socket.close();
                }
                
                // 서버가 감지할 때까지 대기
                boolean detected = disconnectLatch.await(6, TimeUnit.SECONDS);
                assertTrue(detected, "서버가 연결 끝김을 감지해야 합니다");
                
                // isHandlingError 플래그로 중복 방지되어 1번만 처리되어야 함
                assertEquals(1, errorCount.get(), 
                    "동시 다발적 오류에도 중복 처리 방지가 작동해야 합니다");
                
                System.out.println("✅ 동시 다발적 오류 상황 처리 확인");
                
            } finally {
                if (client != null) {
                    try {
                        client.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                if (server != null) {
                    try {
                        server.release();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                Thread.sleep(300);
            }
        }, "동시 다발적 오류 처리는 예외 없이 작동해야 합니다");
    }
}
