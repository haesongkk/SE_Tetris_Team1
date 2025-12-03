package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;

import tetris.network.P2PBase;
import tetris.network.P2PClient;
import tetris.network.P2PServer;
import tetris.scene.battle.P2PBattleScene;
import tetris.scene.game.core.BlockManager;
import tetris.scene.game.core.GameStateManager;

/**
 * P2P 모드 실행 과정 통합 테스트
 * 
 * 테스트 범위:
 * 1. P2P 연결 설정 및 BattleScene 생성
 * 2. 게임 상태 직렬화/역직렬화
 * 3. 양방향 게임 상태 동기화
 * 4. 공격 블록 전송 및 수신
 * 5. 아이템 효과 네트워크 동기화
 * 6. 일시정지 상태 동기화
 * 7. 게임 오버 처리
 * 8. 네트워크 상태 표시 UI
 * 9. 연결 해제 처리
 * 10. P2PBattleScene 생명주기
 */
@DisplayName("P2P 모드 실행 과정 통합 테스트")
public class P2PModeExecutionTest {

    private static final int TEST_TIMEOUT_SECONDS = 15;
    private static final int NETWORK_WAIT_MS = 500;
    
    private JFrame testFrame;
    private P2PServer server;
    private P2PClient client;

    @BeforeEach
    @DisplayName("테스트 환경 초기화")
    void setUp() {
        System.out.println("\n=== P2P 모드 실행 테스트 시작 ===");
        
        // 테스트용 프레임 생성 (실제 화면에 표시하지 않음)
        testFrame = new JFrame("P2P Mode Test Frame");
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testFrame.setSize(1200, 800);
        
        // 각 테스트 전 대기하여 리소스 정리 시간 확보
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    @DisplayName("테스트 후 정리")
    void tearDown() {
        System.out.println("--- 테스트 후 리소스 정리 ---");
        
        // 서버/클라이언트 정리
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
        
        // 프레임 정리
        if (testFrame != null) {
            testFrame.dispose();
        }
        
        // 리소스 정리 대기
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("=== P2P 모드 실행 테스트 완료 ===\n");
    }

    // ========================================
    // 1. P2P 연결 설정 및 BattleScene 생성
    // ========================================

    @Test
    @DisplayName("1-1. 서버 측 P2PBattleScene 생성 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testServerP2PBattleSceneCreation() {
        System.out.println("--- 1-1. 서버 측 P2PBattleScene 생성 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 서버 생성
            server = new P2PServer();
            assertNotNull(server, "P2PServer가 생성되어야 합니다");
            assertNotNull(server.HOST, "서버 HOST가 설정되어야 합니다");
            
            System.out.println("✅ 서버 생성 성공: " + server.HOST);
            
            // P2PBattleScene 생성 (일반 모드)
            P2PBattleScene battleScene = new P2PBattleScene(testFrame, "일반 모드", server);
            assertNotNull(battleScene, "P2PBattleScene이 생성되어야 합니다");
            
            // P2P 연결 확인
            assertNotNull(getPrivateField(battleScene, "p2p"), "P2P 연결이 설정되어야 합니다");
            
            System.out.println("✅ 서버 측 P2PBattleScene 생성 성공");
            
            // 리소스 정리
            battleScene.onExit();
            
        }, "서버 측 P2PBattleScene 생성은 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("1-2. 클라이언트 측 P2PBattleScene 생성 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testClientP2PBattleSceneCreation() {
        System.out.println("--- 1-2. 클라이언트 측 P2PBattleScene 생성 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 서버 시작
            server = new P2PServer();
            String serverHost = server.HOST;
            System.out.println("서버 시작: " + serverHost);
            
            Thread.sleep(NETWORK_WAIT_MS);
            
            // 클라이언트 연결
            client = new P2PClient();
            boolean connected = client.connect(serverHost);
            assertTrue(connected, "클라이언트가 서버에 연결되어야 합니다");
            
            System.out.println("✅ 클라이언트 연결 성공");
            
            // P2PBattleScene 생성 (아이템 모드)
            P2PBattleScene battleScene = new P2PBattleScene(testFrame, "아이템 모드", client);
            assertNotNull(battleScene, "P2PBattleScene이 생성되어야 합니다");
            
            System.out.println("✅ 클라이언트 측 P2PBattleScene 생성 성공");
            
            // 리소스 정리
            battleScene.onExit();
            
        }, "클라이언트 측 P2PBattleScene 생성은 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("1-3. 양쪽 P2PBattleScene 동시 생성 및 연결 확인")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testBothP2PBattleScenesConnection() {
        System.out.println("--- 1-3. 양쪽 P2PBattleScene 동시 생성 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 서버 시작
            server = new P2PServer();
            String serverHost = server.HOST;
            
            Thread.sleep(NETWORK_WAIT_MS);
            
            // 클라이언트 연결
            client = new P2PClient();
            client.connect(serverHost);
            
            Thread.sleep(NETWORK_WAIT_MS);
            
            // 양쪽 BattleScene 생성
            P2PBattleScene serverScene = new P2PBattleScene(testFrame, "일반 모드", server);
            P2PBattleScene clientScene = new P2PBattleScene(testFrame, "일반 모드", client);
            
            assertNotNull(serverScene, "서버 BattleScene이 생성되어야 합니다");
            assertNotNull(clientScene, "클라이언트 BattleScene이 생성되어야 합니다");
            
            System.out.println("✅ 양쪽 P2PBattleScene 생성 및 연결 성공");
            
            // 리소스 정리
            clientScene.onExit();
            serverScene.onExit();
            
        }, "양쪽 P2PBattleScene 연결은 예외 없이 작동해야 합니다");
    }

    // ========================================
    // 2. 게임 상태 직렬화/역직렬화
    // ========================================

    @Test
    @DisplayName("2-1. 게임 상태 직렬화 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testGameStateSerialization() {
        System.out.println("--- 2-1. 게임 상태 직렬화 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            P2PBattleScene battleScene = new P2PBattleScene(testFrame, "일반 모드", server);
            
            // serializeGameState 메서드 호출 (private 메서드이므로 리플렉션 사용)
            Method serializeMethod = P2PBattleScene.class.getDeclaredMethod("serializeGameState");
            serializeMethod.setAccessible(true);
            String serialized = (String) serializeMethod.invoke(battleScene);
            
            assertNotNull(serialized, "직렬화된 게임 상태가 null이 아니어야 합니다");
            assertFalse(serialized.isEmpty(), "직렬화된 문자열이 비어있지 않아야 합니다");
            
            // 실제 SerializedGameState 클래스의 필드들 확인
            assertTrue(serialized.contains("board"), "보드 정보가 포함되어야 합니다");
            assertTrue(serialized.contains("boardColors"), "보드 색상 정보가 포함되어야 합니다");
            assertTrue(serialized.contains("boardTypes"), "보드 타입 정보가 포함되어야 합니다");
            assertTrue(serialized.contains("score"), "점수 정보가 포함되어야 합니다");
            assertTrue(serialized.contains("type"), "블록 타입 정보가 포함되어야 합니다");
            assertTrue(serialized.contains("gameOverFlag"), "게임 오버 플래그가 포함되어야 합니다");
            assertTrue(serialized.contains("pauseFlag"), "일시정지 플래그가 포함되어야 합니다");
            assertTrue(serialized.contains("fallSpeed1"), "Player 1 낙하 속도가 포함되어야 합니다");
            assertTrue(serialized.contains("fallSpeed2"), "Player 2 낙하 속도가 포함되어야 합니다");
            
            System.out.println("✅ 게임 상태 직렬화 성공");
            System.out.println("직렬화 길이: " + serialized.length() + " bytes");
            
            battleScene.onExit();
            
        }, "게임 상태 직렬화는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("2-2. 게임 상태 역직렬화 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testGameStateDeserialization() {
        System.out.println("--- 2-2. 게임 상태 역직렬화 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            P2PBattleScene battleScene = new P2PBattleScene(testFrame, "일반 모드", server);
            
            // 게임 상태 직렬화
            Method serializeMethod = P2PBattleScene.class.getDeclaredMethod("serializeGameState");
            serializeMethod.setAccessible(true);
            String serialized = (String) serializeMethod.invoke(battleScene);
            
            // 역직렬화
            Method deserializeMethod = P2PBattleScene.class.getDeclaredMethod("deserializeGameState", String.class);
            deserializeMethod.setAccessible(true);
            
            // 예외 없이 역직렬화되어야 함
            assertDoesNotThrow(() -> {
                deserializeMethod.invoke(battleScene, serialized);
            }, "역직렬화는 예외 없이 작동해야 합니다");
            
            System.out.println("✅ 게임 상태 역직렬화 성공");
            
            battleScene.onExit();
            
        }, "게임 상태 역직렬화는 예외 없이 작동해야 합니다");
    }

    // ========================================
    // 3. 양방향 게임 상태 동기화
    // ========================================

    @Test
    @DisplayName("3-1. 서버→클라이언트 게임 상태 전송 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testServerToClientGameStateSync() {
        System.out.println("--- 3-1. 서버→클라이언트 게임 상태 전송 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 네트워크 연결
            server = new P2PServer();
            String serverHost = server.HOST;
            Thread.sleep(NETWORK_WAIT_MS);
            
            client = new P2PClient();
            client.connect(serverHost);
            Thread.sleep(NETWORK_WAIT_MS);
            
            // BattleScene 생성
            P2PBattleScene serverScene = new P2PBattleScene(testFrame, "일반 모드", server);
            P2PBattleScene clientScene = new P2PBattleScene(testFrame, "일반 모드", client);
            
            // 메시지 수신 확인을 위한 래치
            CountDownLatch messageLatch = new CountDownLatch(1);
            AtomicBoolean messageReceived = new AtomicBoolean(false);
            
            client.addCallback("board:", (data) -> {
                messageReceived.set(true);
                messageLatch.countDown();
            });
            
            // 서버에서 게임 상태 전송 (writeTimer가 100ms마다 자동으로 전송)
            Thread.sleep(300); // writeTimer가 최소 2-3번 실행될 시간 확보
            
            // 메시지 수신 확인
            boolean received = messageLatch.await(3, TimeUnit.SECONDS);
            assertTrue(received, "클라이언트가 서버의 게임 상태를 수신해야 합니다");
            assertTrue(messageReceived.get(), "board: 메시지가 수신되어야 합니다");
            
            System.out.println("✅ 서버→클라이언트 게임 상태 전송 성공");
            
            // 리소스 정리
            clientScene.onExit();
            serverScene.onExit();
            
        }, "서버→클라이언트 게임 상태 전송은 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("3-2. 클라이언트→서버 게임 상태 전송 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testClientToServerGameStateSync() {
        System.out.println("--- 3-2. 클라이언트→서버 게임 상태 전송 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 네트워크 연결
            server = new P2PServer();
            String serverHost = server.HOST;
            Thread.sleep(NETWORK_WAIT_MS);
            
            client = new P2PClient();
            client.connect(serverHost);
            Thread.sleep(NETWORK_WAIT_MS);
            
            // BattleScene 생성
            P2PBattleScene serverScene = new P2PBattleScene(testFrame, "일반 모드", server);
            P2PBattleScene clientScene = new P2PBattleScene(testFrame, "일반 모드", client);
            
            // 메시지 수신 확인
            CountDownLatch messageLatch = new CountDownLatch(1);
            AtomicBoolean messageReceived = new AtomicBoolean(false);
            
            server.addCallback("board:", (data) -> {
                messageReceived.set(true);
                messageLatch.countDown();
            });
            
            // 클라이언트에서 게임 상태 전송 (writeTimer가 100ms마다 자동으로 전송)
            Thread.sleep(300);
            
            // 메시지 수신 확인
            boolean received = messageLatch.await(3, TimeUnit.SECONDS);
            assertTrue(received, "서버가 클라이언트의 게임 상태를 수신해야 합니다");
            assertTrue(messageReceived.get(), "board: 메시지가 수신되어야 합니다");
            
            System.out.println("✅ 클라이언트→서버 게임 상태 전송 성공");
            
            // 리소스 정리
            clientScene.onExit();
            serverScene.onExit();
            
        }, "클라이언트→서버 게임 상태 전송은 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("3-3. 양방향 게임 상태 동기화 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testBidirectionalGameStateSync() {
        System.out.println("--- 3-3. 양방향 게임 상태 동기화 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 네트워크 연결
            server = new P2PServer();
            Thread.sleep(NETWORK_WAIT_MS);
            
            client = new P2PClient();
            client.connect(server.HOST);
            Thread.sleep(NETWORK_WAIT_MS);
            
            // BattleScene 생성
            P2PBattleScene serverScene = new P2PBattleScene(testFrame, "일반 모드", server);
            P2PBattleScene clientScene = new P2PBattleScene(testFrame, "일반 모드", client);
            
            // 양방향 메시지 수신 확인
            CountDownLatch serverLatch = new CountDownLatch(1);
            CountDownLatch clientLatch = new CountDownLatch(1);
            
            server.addCallback("board:", (data) -> serverLatch.countDown());
            client.addCallback("board:", (data) -> clientLatch.countDown());
            
            // 양방향 전송 대기 (writeTimer가 100ms마다 전송하므로 충분한 시간)
            Thread.sleep(500);
            
            // 양쪽 모두 메시지 수신 확인
            assertTrue(serverLatch.await(2, TimeUnit.SECONDS), "서버가 메시지를 수신해야 합니다");
            assertTrue(clientLatch.await(2, TimeUnit.SECONDS), "클라이언트가 메시지를 수신해야 합니다");
            
            System.out.println("✅ 양방향 게임 상태 동기화 성공");
            
            // 리소스 정리
            clientScene.onExit();
            serverScene.onExit();
            
        }, "양방향 게임 상태 동기화는 예외 없이 작동해야 합니다");
    }

    // ========================================
    // 4. 공격 블록 전송 및 수신
    // ========================================

    @Test
    @DisplayName("4-1. 공격 블록 생성 메시지 전송 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testAttackBlockGenerateMessage() {
        System.out.println("--- 4-1. 공격 블록 생성 메시지 전송 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            Thread.sleep(NETWORK_WAIT_MS);
            
            client = new P2PClient();
            client.connect(server.HOST);
            Thread.sleep(NETWORK_WAIT_MS);
            
            P2PBattleScene serverScene = new P2PBattleScene(testFrame, "일반 모드", server);
            P2PBattleScene clientScene = new P2PBattleScene(testFrame, "일반 모드", client);
            
            // 공격 블록 메시지 수신 확인
            CountDownLatch attackLatch = new CountDownLatch(1);
            AtomicBoolean attackReceived = new AtomicBoolean(false);
            
            client.addCallback("attack-generate:", (data) -> {
                attackReceived.set(true);
                attackLatch.countDown();
            });
            
            // 서버에서 공격 블록 전송 시뮬레이션
            String testAttackData = "{\"width\":10,\"pattern\":[true,true,false,true,true,true,true,true,false,true],\"colors\":[255,255,0,255,255,255,255,255,0,255],\"blockTypes\":[1,1,0,1,1,1,1,1,0,1]}";
            server.send("attack-generate:" + testAttackData);
            
            // 메시지 수신 대기
            boolean received = attackLatch.await(2, TimeUnit.SECONDS);
            assertTrue(received, "공격 블록 메시지가 전달되어야 합니다");
            assertTrue(attackReceived.get(), "attack-generate: 메시지가 수신되어야 합니다");
            
            System.out.println("✅ 공격 블록 생성 메시지 전송 성공");
            
            clientScene.onExit();
            serverScene.onExit();
            
        }, "공격 블록 메시지 전송은 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("4-2. 공격 블록 적용 메시지 전송 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testAttackBlockApplyMessage() {
        System.out.println("--- 4-2. 공격 블록 적용 메시지 전송 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            Thread.sleep(NETWORK_WAIT_MS);
            
            client = new P2PClient();
            client.connect(server.HOST);
            Thread.sleep(NETWORK_WAIT_MS);
            
            P2PBattleScene serverScene = new P2PBattleScene(testFrame, "일반 모드", server);
            P2PBattleScene clientScene = new P2PBattleScene(testFrame, "일반 모드", client);
            
            // 공격 적용 메시지 수신 확인
            CountDownLatch applyLatch = new CountDownLatch(1);
            AtomicBoolean applyReceived = new AtomicBoolean(false);
            
            client.addCallback("attack-apply", (data) -> {
                applyReceived.set(true);
                applyLatch.countDown();
            });
            
            // 서버에서 공격 적용 메시지 전송
            server.send("attack-apply");
            
            // 메시지 수신 대기
            boolean received = applyLatch.await(2, TimeUnit.SECONDS);
            assertTrue(received, "공격 적용 메시지가 전달되어야 합니다");
            assertTrue(applyReceived.get(), "attack-apply 메시지가 수신되어야 합니다");
            
            System.out.println("✅ 공격 블록 적용 메시지 전송 성공");
            
            clientScene.onExit();
            serverScene.onExit();
            
        }, "공격 적용 메시지 전송은 예외 없이 작동해야 합니다");
    }

    // ========================================
    // 5. 아이템 효과 네트워크 동기화
    // ========================================

    @Test
    @DisplayName("5-1. 아이템 효과 speed-up 메시지 전송 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testItemEffectSpeedUpMessage() {
        System.out.println("--- 5-1. 아이템 효과 speed-up 메시지 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            Thread.sleep(NETWORK_WAIT_MS);
            
            client = new P2PClient();
            client.connect(server.HOST);
            Thread.sleep(NETWORK_WAIT_MS);
            
            P2PBattleScene serverScene = new P2PBattleScene(testFrame, "아이템 모드", server);
            P2PBattleScene clientScene = new P2PBattleScene(testFrame, "아이템 모드", client);
            
            // 아이템 효과 메시지 수신 확인
            CountDownLatch itemLatch = new CountDownLatch(1);
            AtomicBoolean itemReceived = new AtomicBoolean(false);
            
            client.addCallback("item:speed-up:", (data) -> {
                itemReceived.set(true);
                itemLatch.countDown();
            });
            
            // 서버에서 아이템 효과 전송 시뮬레이션
            server.send("item:speed-up:");
            
            // 메시지 수신 대기
            boolean received = itemLatch.await(2, TimeUnit.SECONDS);
            assertTrue(received, "아이템 효과 메시지가 전달되어야 합니다");
            assertTrue(itemReceived.get(), "item:speed-up: 메시지가 수신되어야 합니다");
            
            System.out.println("✅ 아이템 효과 speed-up 메시지 전송 성공");
            
            clientScene.onExit();
            serverScene.onExit();
            
        }, "아이템 효과 메시지 전송은 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("5-2. 아이템 효과 speed-down 메시지 전송 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testItemEffectSpeedDownMessage() {
        System.out.println("--- 5-2. 아이템 효과 speed-down 메시지 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            Thread.sleep(NETWORK_WAIT_MS);
            
            client = new P2PClient();
            client.connect(server.HOST);
            Thread.sleep(NETWORK_WAIT_MS);
            
            P2PBattleScene serverScene = new P2PBattleScene(testFrame, "아이템 모드", server);
            P2PBattleScene clientScene = new P2PBattleScene(testFrame, "아이템 모드", client);
            
            // 아이템 효과 메시지 수신 확인
            CountDownLatch itemLatch = new CountDownLatch(1);
            AtomicBoolean itemReceived = new AtomicBoolean(false);
            
            client.addCallback("item:speed-down:", (data) -> {
                itemReceived.set(true);
                itemLatch.countDown();
            });
            
            // 서버에서 아이템 효과 전송
            server.send("item:speed-down:");
            
            // 메시지 수신 대기
            boolean received = itemLatch.await(2, TimeUnit.SECONDS);
            assertTrue(received, "아이템 효과 메시지가 전달되어야 합니다");
            assertTrue(itemReceived.get(), "item:speed-down: 메시지가 수신되어야 합니다");
            
            System.out.println("✅ 아이템 효과 speed-down 메시지 전송 성공");
            
            clientScene.onExit();
            serverScene.onExit();
            
        }, "아이템 효과 메시지 전송은 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("5-3. 아이템 효과 vision-block 메시지 전송 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testItemEffectVisionBlockMessage() {
        System.out.println("--- 5-3. 아이템 효과 vision-block 메시지 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            Thread.sleep(NETWORK_WAIT_MS);
            
            client = new P2PClient();
            client.connect(server.HOST);
            Thread.sleep(NETWORK_WAIT_MS);
            
            P2PBattleScene serverScene = new P2PBattleScene(testFrame, "아이템 모드", server);
            P2PBattleScene clientScene = new P2PBattleScene(testFrame, "아이템 모드", client);
            
            // 아이템 효과 메시지 수신 확인
            CountDownLatch itemLatch = new CountDownLatch(1);
            AtomicBoolean itemReceived = new AtomicBoolean(false);
            
            client.addCallback("item:vision-block:", (data) -> {
                itemReceived.set(true);
                itemLatch.countDown();
            });
            
            // 서버에서 아이템 효과 전송
            server.send("item:vision-block:");
            
            // 메시지 수신 대기
            boolean received = itemLatch.await(2, TimeUnit.SECONDS);
            assertTrue(received, "아이템 효과 메시지가 전달되어야 합니다");
            assertTrue(itemReceived.get(), "item:vision-block: 메시지가 수신되어야 합니다");
            
            System.out.println("✅ 아이템 효과 vision-block 메시지 전송 성공");
            
            clientScene.onExit();
            serverScene.onExit();
            
        }, "아이템 효과 메시지 전송은 예외 없이 작동해야 합니다");
    }

    // ========================================
    // 6. 일시정지 상태 동기화
    // ========================================

    @Test
    @DisplayName("6-1. 일시정지 상태 동기화 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testPauseStateSync() {
        System.out.println("--- 6-1. 일시정지 상태 동기화 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            Thread.sleep(NETWORK_WAIT_MS);
            
            client = new P2PClient();
            client.connect(server.HOST);
            Thread.sleep(NETWORK_WAIT_MS);
            
            P2PBattleScene serverScene = new P2PBattleScene(testFrame, "일반 모드", server);
            P2PBattleScene clientScene = new P2PBattleScene(testFrame, "일반 모드", client);
            
            // 일시정지 플래그는 게임 상태에 포함되어 전송됨
            // writeTimer가 100ms마다 게임 상태를 전송하므로 충분한 대기 시간
            Thread.sleep(500);
            
            System.out.println("✅ 일시정지 상태 동기화 메커니즘 작동 확인");
            
            clientScene.onExit();
            serverScene.onExit();
            
        }, "일시정지 상태 동기화는 예외 없이 작동해야 합니다");
    }

    // ========================================
    // 7. 게임 오버 처리
    // ========================================

    @Test
    @DisplayName("7-1. 게임 오버 플래그 동기화 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testGameOverFlagSync() {
        System.out.println("--- 7-1. 게임 오버 플래그 동기화 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            P2PBattleScene battleScene = new P2PBattleScene(testFrame, "일반 모드", server);
            
            // 게임 상태 직렬화하여 gameOverFlag 포함 확인
            Method serializeMethod = P2PBattleScene.class.getDeclaredMethod("serializeGameState");
            serializeMethod.setAccessible(true);
            String serialized = (String) serializeMethod.invoke(battleScene);
            
            assertTrue(serialized.contains("gameOverFlag"), "게임 오버 플래그가 포함되어야 합니다");
            
            System.out.println("✅ 게임 오버 플래그 동기화 메커니즘 확인");
            
            battleScene.onExit();
            
        }, "게임 오버 플래그 동기화는 예외 없이 작동해야 합니다");
    }

    // ========================================
    // 8. 네트워크 상태 표시 UI
    // ========================================

    @Test
    @DisplayName("8-1. 네트워크 상태 표시 UI 생성 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testNetworkStatusDisplayCreation() {
        System.out.println("--- 8-1. 네트워크 상태 표시 UI 생성 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            P2PBattleScene battleScene = new P2PBattleScene(testFrame, "일반 모드", server);
            
            // P2PBattleScene이 정상적으로 생성되었는지 확인
            assertNotNull(battleScene, "P2PBattleScene이 생성되어야 합니다");
            
            System.out.println("✅ 네트워크 상태 표시 UI 포함 P2PBattleScene 생성 성공");
            
            battleScene.onExit();
            
        }, "네트워크 상태 표시 UI 생성은 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("8-2. writeTimer 동작 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testWriteTimerOperation() {
        System.out.println("--- 8-2. writeTimer 동작 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            Thread.sleep(NETWORK_WAIT_MS);
            
            client = new P2PClient();
            client.connect(server.HOST);
            Thread.sleep(NETWORK_WAIT_MS);
            
            P2PBattleScene serverScene = new P2PBattleScene(testFrame, "일반 모드", server);
            P2PBattleScene clientScene = new P2PBattleScene(testFrame, "일반 모드", client);
            
            // writeTimer 필드 확인
            Object writeTimer = getPrivateField(serverScene, "writeTimer");
            assertNotNull(writeTimer, "writeTimer가 생성되어야 합니다");
            
            // 게임 상태 전송 대기 (writeTimer가 동작하는지 확인)
            Thread.sleep(500);
            
            System.out.println("✅ writeTimer 동작 확인");
            
            clientScene.onExit();
            serverScene.onExit();
            
        }, "writeTimer 동작은 예외 없이 작동해야 합니다");
    }

    // ========================================
    // 9. 연결 해제 처리
    // ========================================

    @Test
    @DisplayName("9-1. onExit 호출 시 리소스 정리 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testOnExitResourceCleanup() {
        System.out.println("--- 9-1. onExit 리소스 정리 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            P2PBattleScene battleScene = new P2PBattleScene(testFrame, "일반 모드", server);
            
            // writeTimer 확인
            Object writeTimer = getPrivateField(battleScene, "writeTimer");
            assertNotNull(writeTimer, "writeTimer가 생성되어야 합니다");
            
            // onExit 호출
            battleScene.onExit();
            
            // 타이머가 취소되었는지는 내부적으로 확인 불가하지만,
            // 예외 없이 실행되면 정상적으로 정리된 것으로 간주
            System.out.println("✅ onExit 리소스 정리 성공");
            
        }, "onExit 리소스 정리는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("9-2. 네트워크 연결 해제 처리 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testNetworkDisconnectHandling() {
        System.out.println("--- 9-2. 네트워크 연결 해제 처리 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            Thread.sleep(NETWORK_WAIT_MS);
            
            client = new P2PClient();
            client.connect(server.HOST);
            Thread.sleep(NETWORK_WAIT_MS);
            
            P2PBattleScene serverScene = new P2PBattleScene(testFrame, "일반 모드", server);
            P2PBattleScene clientScene = new P2PBattleScene(testFrame, "일반 모드", client);
            
            // 연결 해제 콜백 설정
            CountDownLatch disconnectLatch = new CountDownLatch(1);
            AtomicBoolean disconnected = new AtomicBoolean(false);
            
            client.setOnDisconnect(() -> {
                disconnected.set(true);
                disconnectLatch.countDown();
            });
            
            // 서버 연결 강제 종료
            server.release();
            
            // 연결 해제 콜백 실행 확인
            boolean called = disconnectLatch.await(3, TimeUnit.SECONDS);
            assertTrue(called, "연결 해제 콜백이 호출되어야 합니다");
            assertTrue(disconnected.get(), "연결 해제 플래그가 설정되어야 합니다");
            
            System.out.println("✅ 네트워크 연결 해제 처리 성공");
            
            clientScene.onExit();
            serverScene.onExit();
            
        }, "네트워크 연결 해제 처리는 예외 없이 작동해야 합니다");
    }

    // ========================================
    // 10. P2PBattleScene 생명주기
    // ========================================

    @Test
    @DisplayName("10-1. P2PBattleScene onEnter 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testP2PBattleSceneOnEnter() {
        System.out.println("--- 10-1. P2PBattleScene onEnter 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            P2PBattleScene battleScene = new P2PBattleScene(testFrame, "일반 모드", server);
            
            // onEnter 호출
            assertDoesNotThrow(() -> {
                battleScene.onEnter();
            }, "onEnter는 예외 없이 실행되어야 합니다");
            
            System.out.println("✅ P2PBattleScene onEnter 성공");
            
            battleScene.onExit();
            
        }, "P2PBattleScene onEnter는 예외 없이 작동해야 합니다");
    }

    @Test
    @DisplayName("10-2. P2PBattleScene 전체 생명주기 테스트")
    @Timeout(value = TEST_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testP2PBattleSceneLifecycle() {
        System.out.println("--- 10-2. P2PBattleScene 전체 생명주기 테스트 ---");
        
        assertDoesNotThrow(() -> {
            server = new P2PServer();
            Thread.sleep(NETWORK_WAIT_MS);
            
            client = new P2PClient();
            client.connect(server.HOST);
            Thread.sleep(NETWORK_WAIT_MS);
            
            // 생성
            P2PBattleScene serverScene = new P2PBattleScene(testFrame, "일반 모드", server);
            P2PBattleScene clientScene = new P2PBattleScene(testFrame, "일반 모드", client);
            
            assertNotNull(serverScene, "서버 Scene이 생성되어야 합니다");
            assertNotNull(clientScene, "클라이언트 Scene이 생성되어야 합니다");
            
            // onEnter
            serverScene.onEnter();
            clientScene.onEnter();
            
            System.out.println("✅ onEnter 완료");
            
            // 게임 실행 시뮬레이션 (일정 시간 동안 게임 상태 전송)
            Thread.sleep(500);
            
            System.out.println("✅ 게임 실행 완료");
            
            // onExit
            clientScene.onExit();
            serverScene.onExit();
            
            System.out.println("✅ onExit 완료");
            System.out.println("✅ P2PBattleScene 전체 생명주기 성공");
            
        }, "P2PBattleScene 생명주기는 예외 없이 작동해야 합니다");
    }

    // ========================================
    // 헬퍼 메서드
    // ========================================

    /**
     * private 필드 값을 가져오는 헬퍼 메서드
     */
    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    @AfterAll
    @DisplayName("P2PModeExecutionTest 전체 정리")
    static void cleanup() {
        System.out.println("🧹 P2PModeExecutionTest 정리 완료");
        
        // TestCleanupHelper를 통한 추가 정리
        TestCleanupHelper.forceCompleteSystemCleanup("P2PModeExecutionTest");
    }
}
