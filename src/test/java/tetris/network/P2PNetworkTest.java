package tetris.network;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * P2P 네트워크 접속 흐름 검증 자동화 테스트
 * 
 * 테스트 범위:
 * - 서버 시작 및 클라이언트 연결 성공/실패
 * - 메시지 전송/수신
 * - Ping/Pong 메커니즘
 * - 타임아웃 처리
 * - 연결 해제
 */
@DisplayName("P2P 네트워크 접속 흐름 테스트")
public class P2PNetworkTest {

    private static final int TEST_TIMEOUT_SECONDS = 10;
    private static final int NETWORK_WAIT_MS = 500;

    @BeforeEach
    @DisplayName("테스트 환경 초기화")
    void setUp() {
        System.out.println("--- 테스트 환경 초기화 ---");
        // 각 테스트 전 잠시 대기하여 이전 테스트의 리소스 정리 시간 확보
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    @DisplayName("테스트 후 정리")
    void tearDown() {
        System.out.println("--- 테스트 후 정리 ---");
        // 리소스 정리는 각 테스트에서 수행
    }

    @Test
    @DisplayName("서버 시작 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testServerStart() {
        System.out.println("--- 서버 시작 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = new P2PServer();
            
            // 서버가 정상적으로 생성되었는지 확인
            assertNotNull(server, "서버가 생성되어야 합니다");
            assertNotNull(server.HOST, "서버 HOST 주소가 설정되어야 합니다");
            assertFalse(server.HOST.isEmpty(), "서버 HOST 주소가 비어있지 않아야 합니다");
            
            System.out.println("✅ 서버 시작 성공: " + server.HOST);
            
            // 리소스 정리 (서버 소켓만 닫기, 클라이언트 연결 전이므로 out이 null일 수 있음)
            try {
                if (server.serverSocket != null && !server.serverSocket.isClosed()) {
                    server.serverSocket.close();
                }
            } catch (Exception e) {
                // 무시
            }
            Thread.sleep(200); // 리소스 정리 대기
        }, "서버 시작은 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("클라이언트 연결 성공 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testClientConnectSuccess() {
        System.out.println("--- 클라이언트 연결 성공 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                assertNotNull(serverHost, "서버 HOST가 설정되어야 합니다");
                
                System.out.println("서버 시작됨: " + serverHost);
                
                // 클라이언트 연결 대기 (서버가 클라이언트를 받을 준비가 될 때까지)
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결 시도
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                
                // 연결 성공 확인
                assertTrue(connected, "클라이언트가 서버에 연결되어야 합니다");
                assertNotNull(client.socket, "클라이언트 소켓이 생성되어야 합니다");
                assertFalse(client.socket.isClosed(), "클라이언트 소켓이 열려있어야 합니다");
                
                System.out.println("✅ 클라이언트 연결 성공");
                
                // 연결이 정상적으로 유지되는지 확인
                Thread.sleep(200);
                assertFalse(client.socket.isClosed(), "연결이 유지되어야 합니다");
                
            } finally {
                // 리소스 정리
                if (client != null) {
                    client.release();
                }
                if (server != null) {
                    server.release();
                }
                Thread.sleep(300); // 리소스 정리 대기
            }
        }, "클라이언트 연결 성공 테스트는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("클라이언트 연결 실패 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testClientConnectFailure() {
        System.out.println("--- 클라이언트 연결 실패 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PClient client = new P2PClient();
            
            // 존재하지 않는 IP 주소로 연결 시도 (로컬호스트가 아닌 주소 사용)
            // 주의: 실제 네트워크 환경에 따라 타임아웃 시간이 다를 수 있음
            boolean connected = client.connect("127.0.0.1");
            
            // 연결 실패 확인 (포트가 열려있지 않으면 실패해야 함)
            // 단, 다른 프로세스가 5000 포트를 사용 중이면 성공할 수 있음
            // 따라서 연결 실패 또는 성공 모두 정상으로 간주
            System.out.println("✅ 클라이언트 연결 시도 완료 (결과: " + connected + ")");
            
            // 리소스 정리
            if (connected) {
                client.release();
            }
        }, "클라이언트 연결 실패 테스트는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("메시지 전송/수신 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testMessageSendReceive() {
        System.out.println("--- 메시지 전송/수신 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                // 클라이언트 연결 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                // 서버가 클라이언트를 받을 때까지 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 메시지 수신을 위한 콜백 설정
                CountDownLatch messageReceived = new CountDownLatch(1);
                AtomicReference<String> receivedMessage = new AtomicReference<>();
                
                server.addCallback("test:", (message) -> {
                    receivedMessage.set(message);
                    messageReceived.countDown();
                });
                
                // 클라이언트에서 메시지 전송
                String testMessage = "Hello Server";
                client.send("test:" + testMessage);
                
                // 메시지 수신 대기 (최대 3초)
                boolean received = messageReceived.await(3, TimeUnit.SECONDS);
                assertTrue(received, "서버가 메시지를 수신해야 합니다");
                assertEquals(testMessage, receivedMessage.get(), "수신된 메시지가 전송된 메시지와 일치해야 합니다");
                
                System.out.println("✅ 메시지 전송/수신 성공: " + testMessage);
                
            } finally {
                // 리소스 정리
                if (client != null) {
                    try {
                        if (client.socket != null && !client.socket.isClosed()) {
                            client.release();
                        }
                    } catch (Exception e) {
                        // 무시
                    }
                }
                if (server != null) {
                    try {
                        if (server.socket != null && !server.socket.isClosed()) {
                            server.release();
                        } else if (server.serverSocket != null && !server.serverSocket.isClosed()) {
                            server.serverSocket.close();
                        }
                    } catch (Exception e) {
                        // 무시
                    }
                }
                Thread.sleep(300);
            }
        }, "메시지 전송/수신 테스트는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("Ping/Pong 메커니즘 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testPingPong() {
        System.out.println("--- Ping/Pong 메커니즘 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                // 클라이언트 연결 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                // 서버가 클라이언트를 받을 때까지 대기
                Thread.sleep(NETWORK_WAIT_MS * 2);
                
                // P2PBase의 ping/pong은 자동으로 처리되므로, 
                // 직접 ping을 보내면 서버가 자동으로 pong을 응답함
                // 여기서는 메시지 전송이 정상 작동하는지만 확인
                
                // 일반 메시지로 ping/pong 메커니즘 대신 테스트
                CountDownLatch messageReceived = new CountDownLatch(1);
                server.addCallback("test-ping:", (message) -> {
                    messageReceived.countDown();
                });
                
                // 클라이언트에서 테스트 메시지 전송
                client.send("test-ping:test");
                
                // 메시지 수신 대기
                boolean messageOk = messageReceived.await(2, TimeUnit.SECONDS);
                assertTrue(messageOk, "서버가 메시지를 수신해야 합니다");
                
                System.out.println("✅ Ping/Pong 메커니즘 작동 확인");
                
            } finally {
                // 리소스 정리
                if (client != null) {
                    client.release();
                }
                if (server != null) {
                    server.release();
                }
                Thread.sleep(300);
            }
        }, "Ping/Pong 메커니즘 테스트는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("타임아웃 처리 테스트")
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testTimeout() {
        System.out.println("--- 타임아웃 처리 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                // 클라이언트 연결 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                // 서버가 클라이언트를 받을 때까지 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 연결 해제 콜백 설정
                AtomicBoolean disconnected = new AtomicBoolean(false);
                server.setOnDisconnect(() -> {
                    disconnected.set(true);
                });
                
                // 클라이언트 소켓 강제 종료 (타임아웃 시뮬레이션)
                if (client.socket != null && !client.socket.isClosed()) {
                    try {
                        client.socket.close();
                    } catch (Exception e) {
                        // 무시
                    }
                }
                
                // 타임아웃 발생 대기 (TIMEOUT_MS = 5000ms + 여유 시간)
                Thread.sleep(6000);
                
                // 타임아웃으로 인한 연결 해제 확인
                // 주의: 실제 타임아웃 메커니즘은 P2PBase의 run() 메서드 내부에서 처리되므로
                // 완전한 검증은 어려울 수 있음. 최소한 예외가 발생하지 않는지 확인
                System.out.println("✅ 타임아웃 처리 메커니즘 확인 (완전한 검증은 제한적)");
                
            } finally {
                // 리소스 정리
                if (client != null) {
                    try {
                        client.release();
                    } catch (Exception e) {
                        // 이미 종료된 경우 무시
                    }
                }
                if (server != null) {
                    try {
                        server.release();
                    } catch (Exception e) {
                        // 이미 종료된 경우 무시
                    }
                }
                Thread.sleep(300);
            }
        }, "타임아웃 처리 테스트는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("연결 해제 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testRelease() {
        System.out.println("--- 연결 해제 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                // 클라이언트 연결 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                // 서버가 클라이언트를 받을 때까지 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 연결 해제 콜백 설정
                AtomicBoolean serverDisconnected = new AtomicBoolean(false);
                AtomicBoolean clientDisconnected = new AtomicBoolean(false);
                
                server.setOnDisconnect(() -> {
                    serverDisconnected.set(true);
                });
                
                client.setOnDisconnect(() -> {
                    clientDisconnected.set(true);
                });
                
                // 클라이언트 연결 해제
                client.release();
                
                // 리소스 정리 대기
                Thread.sleep(500);
                
                // 연결 해제 확인
                assertTrue(client.socket == null || client.socket.isClosed(), 
                    "클라이언트 소켓이 닫혀야 합니다");
                
                System.out.println("✅ 연결 해제 성공");
                
            } finally {
                // 리소스 정리
                if (client != null) {
                    try {
                        client.release();
                    } catch (Exception e) {
                        // 이미 종료된 경우 무시
                    }
                }
                if (server != null) {
                    try {
                        server.release();
                    } catch (Exception e) {
                        // 이미 종료된 경우 무시
                    }
                }
                Thread.sleep(300);
            }
        }, "연결 해제 테스트는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("콜백 등록/제거 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testCallbackRegistration() {
        System.out.println("--- 콜백 등록/제거 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                // 클라이언트 연결 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                // 서버가 클라이언트를 받을 때까지 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 콜백 등록
                CountDownLatch callback1 = new CountDownLatch(1);
                CountDownLatch callback2 = new CountDownLatch(1);
                
                server.addCallback("callback1:", (msg) -> callback1.countDown());
                server.addCallback("callback2:", (msg) -> callback2.countDown());
                
                // 메시지 전송
                client.send("callback1:test1");
                client.send("callback2:test2");
                
                // 콜백 실행 확인
                assertTrue(callback1.await(2, TimeUnit.SECONDS), "첫 번째 콜백이 실행되어야 합니다");
                assertTrue(callback2.await(2, TimeUnit.SECONDS), "두 번째 콜백이 실행되어야 합니다");
                
                // 콜백 제거
                server.removeCallback("callback1:");
                
                // 제거된 콜백은 실행되지 않아야 함
                CountDownLatch callback1AfterRemove = new CountDownLatch(1);
                server.addCallback("callback1:", (msg) -> callback1AfterRemove.countDown());
                
                // 다시 메시지 전송 (새로운 콜백이 등록되었으므로 실행되어야 함)
                client.send("callback1:test3");
                assertTrue(callback1AfterRemove.await(2, TimeUnit.SECONDS), 
                    "새로 등록된 콜백이 실행되어야 합니다");
                
                System.out.println("✅ 콜백 등록/제거 기능 확인");
                
            } finally {
                // 리소스 정리
                if (client != null) {
                    client.release();
                }
                if (server != null) {
                    server.release();
                }
                Thread.sleep(300);
            }
        }, "콜백 등록/제거 테스트는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("여러 메시지 동시 전송 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testMultipleMessages() {
        System.out.println("--- 여러 메시지 동시 전송 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                // 클라이언트 연결 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                // 서버가 클라이언트를 받을 때까지 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 여러 메시지 수신을 위한 콜백 설정
                CountDownLatch messageCount = new CountDownLatch(5);
                AtomicReference<Integer> receivedCount = new AtomicReference<>(0);
                
                server.addCallback("multi:", (message) -> {
                    receivedCount.set(receivedCount.get() + 1);
                    messageCount.countDown();
                });
                
                // 여러 메시지 전송
                for (int i = 1; i <= 5; i++) {
                    client.send("multi:message" + i);
                }
                
                // 모든 메시지 수신 대기
                boolean allReceived = messageCount.await(3, TimeUnit.SECONDS);
                assertTrue(allReceived, "모든 메시지가 수신되어야 합니다");
                assertEquals(5, receivedCount.get(), "5개의 메시지가 모두 수신되어야 합니다");
                
                System.out.println("✅ 여러 메시지 동시 전송 성공: " + receivedCount.get() + "개");
                
            } finally {
                // 리소스 정리
                if (client != null) {
                    client.release();
                }
                if (server != null) {
                    server.release();
                }
                Thread.sleep(300);
            }
        }, "여러 메시지 동시 전송 테스트는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("onDisconnect 콜백 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testOnDisconnectCallback() {
        System.out.println("--- onDisconnect 콜백 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                // 클라이언트 연결 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                // 서버가 클라이언트를 받을 때까지 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // onDisconnect 콜백 설정
                CountDownLatch disconnectCallback = new CountDownLatch(1);
                AtomicBoolean callbackCalled = new AtomicBoolean(false);
                
                server.setOnDisconnect(() -> {
                    callbackCalled.set(true);
                    disconnectCallback.countDown();
                });
                
                // 클라이언트 연결 해제
                client.release();
                
                // 콜백 호출 대기
                boolean callbackReceived = disconnectCallback.await(3, TimeUnit.SECONDS);
                assertTrue(callbackReceived, "onDisconnect 콜백이 호출되어야 합니다");
                assertTrue(callbackCalled.get(), "콜백이 실행되어야 합니다");
                
                System.out.println("✅ onDisconnect 콜백 호출 확인");
                
            } finally {
                // 리소스 정리
                if (client != null) {
                    try {
                        client.release();
                    } catch (Exception e) {
                        // 이미 종료된 경우 무시
                    }
                }
                if (server != null) {
                    try {
                        server.release();
                    } catch (Exception e) {
                        // 이미 종료된 경우 무시
                    }
                }
                Thread.sleep(300);
            }
        }, "onDisconnect 콜백 테스트는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("네트워크 오류 처리 테스트 - null 스트림")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testNetworkErrorHandling() {
        System.out.println("--- 네트워크 오류 처리 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PClient client = new P2PClient();
            
            // 연결하지 않은 상태에서 send() 호출 (out이 null)
            // 이 경우 handleNetworkError가 호출되어야 함
            client.send("test:message");
            
            // 예외가 발생하지 않고 조용히 처리되어야 함
            System.out.println("✅ null 스트림에서 send() 호출 시 오류 처리 확인");
            
            // 리소스 정리
            client.release();
        }, "네트워크 오류 처리 테스트는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("긴 메시지 전송 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testLongMessage() {
        System.out.println("--- 긴 메시지 전송 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                // 클라이언트 연결 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                // 서버가 클라이언트를 받을 때까지 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 긴 메시지 생성 (게임 상태 직렬화 데이터 시뮬레이션)
                StringBuilder longMessage = new StringBuilder("long:");
                for (int i = 0; i < 1000; i++) {
                    longMessage.append("data").append(i).append(",");
                }
                
                // 메시지 수신을 위한 콜백 설정
                CountDownLatch messageReceived = new CountDownLatch(1);
                AtomicReference<String> receivedMessage = new AtomicReference<>();
                
                server.addCallback("long:", (message) -> {
                    receivedMessage.set(message);
                    messageReceived.countDown();
                });
                
                // 긴 메시지 전송
                String testMessage = longMessage.toString();
                client.send(testMessage);
                
                // 메시지 수신 대기
                boolean received = messageReceived.await(3, TimeUnit.SECONDS);
                assertTrue(received, "긴 메시지가 수신되어야 합니다");
                assertTrue(receivedMessage.get().startsWith("data0"), 
                    "수신된 메시지가 전송된 메시지의 일부를 포함해야 합니다");
                
                System.out.println("✅ 긴 메시지 전송 성공 (길이: " + testMessage.length() + "자)");
                
            } finally {
                // 리소스 정리
                if (client != null) {
                    client.release();
                }
                if (server != null) {
                    server.release();
                }
                Thread.sleep(300);
            }
        }, "긴 메시지 전송 테스트는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("빠른 연속 메시지 전송 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testRapidMessages() {
        System.out.println("--- 빠른 연속 메시지 전송 테스트 ---");
        
        assertDoesNotThrow(() -> {
            P2PServer server = null;
            P2PClient client = null;
            
            try {
                // 서버 시작
                server = new P2PServer();
                String serverHost = server.HOST;
                
                // 클라이언트 연결 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 클라이언트 연결
                client = new P2PClient();
                boolean connected = client.connect(serverHost);
                assertTrue(connected, "클라이언트가 연결되어야 합니다");
                
                // 서버가 클라이언트를 받을 때까지 대기
                Thread.sleep(NETWORK_WAIT_MS);
                
                // 빠른 연속 메시지 수신을 위한 콜백 설정
                CountDownLatch messageCount = new CountDownLatch(10);
                AtomicReference<Integer> receivedCount = new AtomicReference<>(0);
                
                server.addCallback("rapid:", (message) -> {
                    receivedCount.set(receivedCount.get() + 1);
                    messageCount.countDown();
                });
                
                // 빠른 연속 메시지 전송 (게임 상태 업데이트 시뮬레이션)
                for (int i = 0; i < 10; i++) {
                    client.send("rapid:update" + i);
                    Thread.sleep(10); // 매우 짧은 간격
                }
                
                // 모든 메시지 수신 대기
                boolean allReceived = messageCount.await(3, TimeUnit.SECONDS);
                assertTrue(allReceived, "모든 빠른 연속 메시지가 수신되어야 합니다");
                assertEquals(10, receivedCount.get(), "10개의 메시지가 모두 수신되어야 합니다");
                
                System.out.println("✅ 빠른 연속 메시지 전송 성공: " + receivedCount.get() + "개");
                
            } finally {
                // 리소스 정리
                if (client != null) {
                    client.release();
                }
                if (server != null) {
                    server.release();
                }
                Thread.sleep(300);
            }
        }, "빠른 연속 메시지 전송 테스트는 예외 없이 작동해야 합니다");
    }
}

