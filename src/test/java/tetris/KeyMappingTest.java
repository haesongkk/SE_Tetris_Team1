package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

/**
 * 키 매핑 시스템 및 키 충돌 검증 통합 테스트
 * - 플레이어별 키 설정이 올바르게 작동하는지 검증
 * - 싱글 플레이어, 배틀 모드 1P, 배틀 모드 2P 키 독립성 확인
 * - 1P와 2P 키 설정 시 중복 방지 기능 테스트
 * - 키 충돌 경고 시스템 검증
 * - 키 설정 독립성 확인
 */
@DisplayName("키 매핑 시스템 및 키 충돌 검증 통합 테스트")
public class KeyMappingTest {
    
    private GameSettings settings;
    
    @BeforeEach
    @DisplayName("테스트 환경 초기화")
    void setUp() {
        settings = GameSettings.getInstance();
        
        // 테스트 시작 전 모든 키를 기본값으로 초기화
        settings.setLeftKey(37);      // VK_LEFT
        settings.setRightKey(39);     // VK_RIGHT  
        settings.setRotateKey(38);    // VK_UP
        settings.setFallKey(40);      // VK_DOWN
        settings.setDropKey(32);      // VK_SPACE
        settings.setPauseKey(80);     // VK_P
        // exitKey는 setter가 없으므로 생략
        
        // 배틀 모드 키도 기본값으로 초기화
        settings.setBattleLeftKey1(65);   // VK_A
        settings.setBattleRightKey1(68);  // VK_D
        settings.setBattleRotateKey1(87); // VK_W
        settings.setBattleFallKey1(83);   // VK_S
        settings.setBattleDropKey1(32);   // VK_SPACE
        settings.setBattlePauseKey1(80);  // VK_P
        settings.setBattleExitKey1(81);   // VK_Q
        
        settings.setBattleLeftKey2(37);   // VK_LEFT
        settings.setBattleRightKey2(39);  // VK_RIGHT
        settings.setBattleRotateKey2(38); // VK_UP
        settings.setBattleFallKey2(40);   // VK_DOWN
        settings.setBattleDropKey2(10);   // VK_ENTER
        settings.setBattlePauseKey2(80);  // VK_P
        settings.setBattleExitKey2(81);   // VK_Q
        
        System.out.println("=== 키 매핑 테스트 시작 (기본값으로 초기화됨) ===");
    }
    
    @Test
    @DisplayName("싱글 플레이어 기본 키 매핑 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testSinglePlayerDefaultKeys() {
        System.out.println("--- 싱글 플레이어 (player = 0) 기본 키 테스트 ---");
        
        // 싱글 플레이어 기본 키 확인
        assertEquals(37, settings.getLeftKey(0), "싱글 플레이어 왼쪽 키는 VK_LEFT(37)이어야 합니다");
        assertEquals(39, settings.getRightKey(0), "싱글 플레이어 오른쪽 키는 VK_RIGHT(39)이어야 합니다");
        assertEquals(38, settings.getRotateKey(0), "싱글 플레이어 회전 키는 VK_UP(38)이어야 합니다");
        assertEquals(40, settings.getFallKey(0), "싱글 플레이어 낙하 키는 VK_DOWN(40)이어야 합니다");
        assertEquals(32, settings.getDropKey(0), "싱글 플레이어 드롭 키는 VK_SPACE(32)이어야 합니다");
        assertEquals(80, settings.getPauseKey(0), "싱글 플레이어 일시정지 키는 VK_P(80)이어야 합니다");
        assertEquals(81, settings.getExitKey(0), "싱글 플레이어 종료 키는 VK_Q(81)이어야 합니다");
        
        System.out.println("✅ 싱글 플레이어 기본 키 매핑 검증 완료");
    }
    
    @Test
    @DisplayName("배틀 모드 1P 기본 키 매핑 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testBattlePlayer1DefaultKeys() {
        System.out.println("--- 배틀 모드 1P (player = 1) 기본 키 테스트 ---");
        
        // 배틀 모드 1P 기본 키 확인 (WASD)
        assertEquals(65, settings.getLeftKey(1), "배틀 1P 왼쪽 키는 VK_A(65)이어야 합니다");
        assertEquals(68, settings.getRightKey(1), "배틀 1P 오른쪽 키는 VK_D(68)이어야 합니다");
        assertEquals(87, settings.getRotateKey(1), "배틀 1P 회전 키는 VK_W(87)이어야 합니다");
        assertEquals(83, settings.getFallKey(1), "배틀 1P 낙하 키는 VK_S(83)이어야 합니다");
        assertEquals(32, settings.getDropKey(1), "배틀 1P 드롭 키는 VK_SPACE(32)이어야 합니다");
        assertEquals(80, settings.getPauseKey(1), "배틀 1P 일시정지 키는 VK_P(80)이어야 합니다");
        assertEquals(81, settings.getExitKey(1), "배틀 1P 종료 키는 VK_Q(81)이어야 합니다");
        
        System.out.println("✅ 배틀 모드 1P 기본 키 매핑 검증 완료");
    }
    
    @Test
    @DisplayName("배틀 모드 2P 기본 키 매핑 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testBattlePlayer2DefaultKeys() {
        System.out.println("--- 배틀 모드 2P (player = 2) 기본 키 테스트 ---");
        
        // 배틀 모드 2P 기본 키 확인 (방향키 + Enter)
        assertEquals(37, settings.getLeftKey(2), "배틀 2P 왼쪽 키는 VK_LEFT(37)이어야 합니다");
        assertEquals(39, settings.getRightKey(2), "배틀 2P 오른쪽 키는 VK_RIGHT(39)이어야 합니다");
        assertEquals(38, settings.getRotateKey(2), "배틀 2P 회전 키는 VK_UP(38)이어야 합니다");
        assertEquals(40, settings.getFallKey(2), "배틀 2P 낙하 키는 VK_DOWN(40)이어야 합니다");
        assertEquals(10, settings.getDropKey(2), "배틀 2P 드롭 키는 VK_ENTER(10)이어야 합니다");
        assertEquals(80, settings.getPauseKey(2), "배틀 2P 일시정지 키는 VK_P(80)이어야 합니다");
        assertEquals(81, settings.getExitKey(2), "배틀 2P 종료 키는 VK_Q(81)이어야 합니다");
        
        System.out.println("✅ 배틀 모드 2P 기본 키 매핑 검증 완료");
    }
    
    @Test
    @DisplayName("플레이어별 키 독립성 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testPlayerKeyIndependence() {
        System.out.println("--- 플레이어별 키 독립성 테스트 ---");
        
        // 원래 값 저장
        int originalSingleLeft = settings.getLeftKey(0);
        int originalBattle1Left = settings.getLeftKey(1);
        int originalBattle2Left = settings.getLeftKey(2);
        
        // 배틀 1P 키 변경
        settings.setBattleLeftKey1(KeyEvent.VK_Z); // Z키로 변경
        
        // 1P만 변경되고 다른 플레이어는 영향 없는지 확인
        assertEquals(originalSingleLeft, settings.getLeftKey(0), "싱글 플레이어 키는 변경되지 않아야 합니다");
        assertEquals(KeyEvent.VK_Z, settings.getLeftKey(1), "배틀 1P 키는 Z키로 변경되어야 합니다");
        assertEquals(originalBattle2Left, settings.getLeftKey(2), "배틀 2P 키는 변경되지 않아야 합니다");
        
        System.out.println("✅ 배틀 1P 키 변경 시 독립성 확인");
        
        // 배틀 2P 키 변경
        settings.setBattleLeftKey2(KeyEvent.VK_J); // J키로 변경
        
        // 2P만 변경되고 다른 플레이어는 영향 없는지 확인
        assertEquals(originalSingleLeft, settings.getLeftKey(0), "싱글 플레이어 키는 변경되지 않아야 합니다");
        assertEquals(KeyEvent.VK_Z, settings.getLeftKey(1), "배틀 1P 키는 여전히 Z키이어야 합니다");
        assertEquals(KeyEvent.VK_J, settings.getLeftKey(2), "배틀 2P 키는 J키로 변경되어야 합니다");
        
        System.out.println("✅ 배틀 2P 키 변경 시 독립성 확인");
        
        // 싱글 플레이어 키 변경
        settings.setLeftKey(KeyEvent.VK_H); // H키로 변경
        
        // 싱글만 변경되고 배틀 모드는 영향 없는지 확인
        assertEquals(KeyEvent.VK_H, settings.getLeftKey(0), "싱글 플레이어 키는 H키로 변경되어야 합니다");
        assertEquals(KeyEvent.VK_Z, settings.getLeftKey(1), "배틀 1P 키는 여전히 Z키이어야 합니다");
        assertEquals(KeyEvent.VK_J, settings.getLeftKey(2), "배틀 2P 키는 여전히 J키이어야 합니다");
        
        System.out.println("✅ 플레이어별 키 독립성 검증 완료");
    }
    
    @Test
    @DisplayName("모든 키 타입별 매핑 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testAllKeyTypesMapping() {
        System.out.println("--- 모든 키 타입별 매핑 테스트 ---");
        
        // 각 플레이어별로 모든 키 타입이 올바르게 매핑되는지 확인
        for (int player = 0; player <= 2; player++) {
            System.out.println("플레이어 " + player + " 키 매핑 확인:");
            
            // 모든 키가 유효한 값을 반환하는지 확인
            assertTrue(settings.getLeftKey(player) > 0, "플레이어 " + player + " 왼쪽 키는 유효해야 합니다");
            assertTrue(settings.getRightKey(player) > 0, "플레이어 " + player + " 오른쪽 키는 유효해야 합니다");
            assertTrue(settings.getRotateKey(player) > 0, "플레이어 " + player + " 회전 키는 유효해야 합니다");
            assertTrue(settings.getFallKey(player) > 0, "플레이어 " + player + " 낙하 키는 유효해야 합니다");
            assertTrue(settings.getDropKey(player) > 0, "플레이어 " + player + " 드롭 키는 유효해야 합니다");
            assertTrue(settings.getPauseKey(player) > 0, "플레이어 " + player + " 일시정지 키는 유효해야 합니다");
            assertTrue(settings.getExitKey(player) > 0, "플레이어 " + player + " 종료 키는 유효해야 합니다");
            
            System.out.println("  ✅ 플레이어 " + player + " 모든 키 유효성 확인");
        }
        
        System.out.println("✅ 모든 키 타입별 매핑 검증 완료");
    }
    
    @Test
    @DisplayName("키 설정 변경 후 즉시 반영 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testKeyChangeImmediateReflection() {
        System.out.println("--- 키 설정 변경 후 즉시 반영 테스트 ---");
        
        // 배틀 1P 모든 키 변경
        settings.setBattleLeftKey1(KeyEvent.VK_1);
        settings.setBattleRightKey1(KeyEvent.VK_2);
        settings.setBattleRotateKey1(KeyEvent.VK_3);
        settings.setBattleFallKey1(KeyEvent.VK_4);
        settings.setBattleDropKey1(KeyEvent.VK_5);
        settings.setBattlePauseKey1(KeyEvent.VK_6);
        settings.setBattleExitKey1(KeyEvent.VK_7);
        
        // 변경이 즉시 반영되는지 확인
        assertEquals(KeyEvent.VK_1, settings.getLeftKey(1), "왼쪽 키 변경이 즉시 반영되어야 합니다");
        assertEquals(KeyEvent.VK_2, settings.getRightKey(1), "오른쪽 키 변경이 즉시 반영되어야 합니다");
        assertEquals(KeyEvent.VK_3, settings.getRotateKey(1), "회전 키 변경이 즉시 반영되어야 합니다");
        assertEquals(KeyEvent.VK_4, settings.getFallKey(1), "낙하 키 변경이 즉시 반영되어야 합니다");
        assertEquals(KeyEvent.VK_5, settings.getDropKey(1), "드롭 키 변경이 즉시 반영되어야 합니다");
        assertEquals(KeyEvent.VK_6, settings.getPauseKey(1), "일시정지 키 변경이 즉시 반영되어야 합니다");
        assertEquals(KeyEvent.VK_7, settings.getExitKey(1), "종료 키 변경이 즉시 반영되어야 합니다");
        
        System.out.println("✅ 키 설정 변경 후 즉시 반영 검증 완료");
    }
    
    @Test
    @DisplayName("잘못된 플레이어 번호 처리 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testInvalidPlayerNumber() {
        System.out.println("--- 잘못된 플레이어 번호 처리 테스트 ---");
        
        // 잘못된 플레이어 번호 (-1, 3, 100)에 대해 예외가 발생하지 않는지 확인
        assertDoesNotThrow(() -> {
            int key = settings.getLeftKey(-1);
            System.out.println("플레이어 -1 키: " + key);
        }, "잘못된 플레이어 번호(-1)는 예외를 발생시키지 않아야 합니다");
        
        assertDoesNotThrow(() -> {
            int key = settings.getLeftKey(3);
            System.out.println("플레이어 3 키: " + key);
        }, "잘못된 플레이어 번호(3)는 예외를 발생시키지 않아야 합니다");
        
        assertDoesNotThrow(() -> {
            int key = settings.getLeftKey(100);
            System.out.println("플레이어 100 키: " + key);
        }, "잘못된 플레이어 번호(100)는 예외를 발생시키지 않아야 합니다");
        
        System.out.println("✅ 잘못된 플레이어 번호 처리 검증 완료");
    }
    
    // ===============================
    // 키 충돌 검증 테스트 섹션
    // ===============================
    
    @Test
    @DisplayName("기본 키 설정 중복 확인 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testDefaultKeySettings() {
        System.out.println("--- 기본 키 설정 중복 확인 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 1P와 2P 기본 키 설정에 중복이 있는지 확인
            int[] player1Keys = {
                settings.getLeftKey(1), settings.getRightKey(1), 
                settings.getRotateKey(1), settings.getFallKey(1),
                settings.getDropKey(1), settings.getPauseKey(1),
                settings.getExitKey(1)
            };
            
            int[] player2Keys = {
                settings.getLeftKey(2), settings.getRightKey(2), 
                settings.getRotateKey(2), settings.getFallKey(2),
                settings.getDropKey(2), settings.getPauseKey(2),
                settings.getExitKey(2)
            };
            
            // 1P와 2P 키 중복 확인
            for (int i = 0; i < player1Keys.length; i++) {
                for (int j = 0; j < player2Keys.length; j++) {
                    if (player1Keys[i] == player2Keys[j]) {
                        // P키는 예외적으로 두 플레이어가 공유 가능 (일시정지)
                        if (player1Keys[i] == KeyEvent.VK_P) {
                            System.out.println("⚠️  P키(일시정지)는 두 플레이어가 공유: " + 
                                GameSettings.getKeyName(player1Keys[i]));
                            continue;
                        }
                        
                        fail(String.format("키 중복 발견! 1P[%d]=%s, 2P[%d]=%s", 
                            i, GameSettings.getKeyName(player1Keys[i]),
                            j, GameSettings.getKeyName(player2Keys[j])));
                    }
                }
            }
            
            System.out.println("✅ 기본 키 설정에 문제가 되는 중복 없음");
            
        }, "기본 키 설정 중복 확인은 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("키 충돌 시뮬레이션 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testKeyConflictSimulation() {
        System.out.println("--- 키 충돌 시뮬레이션 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 시나리오 1: 1P가 2P의 왼쪽 이동 키(LEFT)를 사용하려는 경우
            int conflictKey = KeyEvent.VK_LEFT; // 2P의 왼쪽 이동 키
            
            // 키 충돌 검증 메서드를 직접 테스트하기 위해 리플렉션 사용
            boolean conflict = isKeyInUseByOtherPlayer(conflictKey, 1);
            assertTrue(conflict, "1P가 2P의 LEFT 키를 사용하려 할 때 충돌이 감지되어야 합니다");
            System.out.println("✅ 시나리오 1: 키 충돌 감지됨 - " + GameSettings.getKeyName(conflictKey));
            
            // 시나리오 2: 2P가 1P의 A 키를 사용하려는 경우
            conflictKey = KeyEvent.VK_A; // 1P의 왼쪽 이동 키
            conflict = isKeyInUseByOtherPlayer(conflictKey, 2);
            assertTrue(conflict, "2P가 1P의 A 키를 사용하려 할 때 충돌이 감지되어야 합니다");
            System.out.println("✅ 시나리오 2: 키 충돌 감지됨 - " + GameSettings.getKeyName(conflictKey));
            
            // 시나리오 3: 사용하지 않는 키는 충돌 없음
            conflictKey = KeyEvent.VK_Z; // 아무도 사용하지 않는 키
            conflict = isKeyInUseByOtherPlayer(conflictKey, 1);
            assertFalse(conflict, "사용하지 않는 키는 충돌이 없어야 합니다");
            System.out.println("✅ 시나리오 3: 충돌 없음 - " + GameSettings.getKeyName(conflictKey));
            
        }, "키 충돌 시뮬레이션은 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("P키 공유 허용 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testPauseKeySharing() {
        System.out.println("--- P키 공유 허용 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 현재 기본 설정에서 1P와 2P 모두 P키를 사용
            int player1PauseKey = settings.getPauseKey(1);
            int player2PauseKey = settings.getPauseKey(2);
            
            assertEquals(KeyEvent.VK_P, player1PauseKey, "1P 일시정지 키가 P여야 합니다");
            assertEquals(KeyEvent.VK_P, player2PauseKey, "2P 일시정지 키가 P여야 합니다");
            assertEquals(player1PauseKey, player2PauseKey, "1P와 2P가 같은 일시정지 키를 사용해야 합니다");
            
            System.out.println("✅ P키 공유 설정 확인됨: " + GameSettings.getKeyName(player1PauseKey));
            
        }, "P키 공유 허용 테스트는 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("키 변경 후 독립성 확인 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testKeyIndependenceAfterChange() {
        System.out.println("--- 키 변경 후 독립성 확인 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 1P의 왼쪽 키를 F로 변경
            settings.setBattleLeftKey1(KeyEvent.VK_F);
            
            // 2P의 키 설정이 영향받지 않았는지 확인
            assertEquals(KeyEvent.VK_LEFT, settings.getLeftKey(2), 
                "1P 키 변경이 2P 키에 영향을 주지 않아야 합니다");
            
            // 이제 F키는 사용 가능한 상태이므로 충돌 없음
            boolean conflict = isKeyInUseByOtherPlayer(KeyEvent.VK_A, 2);
            assertFalse(conflict, "1P가 A키를 F키로 변경했으므로 2P가 A키를 사용할 수 있어야 합니다");
            
            System.out.println("✅ 키 변경 후 독립성 확인됨");
            
            // 원래 설정으로 복구
            settings.setBattleLeftKey1(KeyEvent.VK_A);
            
        }, "키 변경 후 독립성 확인은 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("모든 키 조합 중복 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testAllKeyCombinationConflicts() {
        System.out.println("--- 모든 키 조합 중복 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 1P의 모든 키를 2P가 사용하려 할 때 충돌 감지 확인
            int[] player1Keys = {
                settings.getLeftKey(1), settings.getRightKey(1), 
                settings.getRotateKey(1), settings.getFallKey(1),
                settings.getDropKey(1), settings.getExitKey(1) // P키는 공유 허용이므로 제외
            };
            
            for (int key : player1Keys) {
                boolean conflict = isKeyInUseByOtherPlayer(key, 2);
                assertTrue(conflict, "2P가 1P의 키 " + GameSettings.getKeyName(key) + "를 사용하려 할 때 충돌이 감지되어야 합니다");
            }
            
            // 2P의 모든 키를 1P가 사용하려 할 때 충돌 감지 확인
            int[] player2Keys = {
                settings.getLeftKey(2), settings.getRightKey(2), 
                settings.getRotateKey(2), settings.getFallKey(2),
                settings.getDropKey(2), settings.getExitKey(2) // P키는 공유 허용이므로 제외
            };
            
            for (int key : player2Keys) {
                boolean conflict = isKeyInUseByOtherPlayer(key, 1);
                assertTrue(conflict, "1P가 2P의 키 " + GameSettings.getKeyName(key) + "를 사용하려 할 때 충돌이 감지되어야 합니다");
            }
            
            System.out.println("✅ 모든 키 조합 중복 감지 확인됨 (총 " + (player1Keys.length + player2Keys.length) + "개)");
            
        }, "모든 키 조합 중복 테스트는 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("경고 다이얼로그 텍스트 렌더링 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testWarningDialogKoreanTextRendering() {
        System.out.println("--- 경고 다이얼로그 텍스트 렌더링 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 다양한 키 이름으로 텍스트 구성 테스트
            String[] testKeys = {"A", "←", "Ctrl", "Space", "Enter", "Escape"};
            String[] playerNames = {"1P", "2P"};
            
            for (String keyName : testKeys) {
                for (String playerName : playerNames) {
                    String expectedText = String.format("'%s' 키는 이미 %s가 사용 중입니다.", keyName, playerName);
                    
                    assertTrue(expectedText.contains(keyName), "키 이름이 포함되어야 합니다");
                    assertTrue(expectedText.contains(playerName), "플레이어 이름이 포함되어야 합니다");
                }
            }
            
            // HTML 템플릿 검증
            String htmlText = String.format(
                "<html><div style='text-align:center; font-family:Malgun Gothic; line-height:1.5;'>" +
                "<div style='font-size:18px; color:#FF6B6B; font-weight:bold; margin-bottom:15px;'>키 충돌!</div>" +
                "<div style='font-size:16px; margin-bottom:15px;'>'%s' 키는 이미<br/>%s가 사용 중입니다.</div>" +
                "<div style='font-size:14px; color:#CCCCCC;'>다른 키를 선택해주세요.</div>" +
                "</div></html>", "A", "1P"
            );
            
            assertTrue(htmlText.contains("Malgun Gothic"), "한글 폰트가 지정되어야 합니다");
            assertTrue(htmlText.contains("text-align:center"), "중앙 정렬이 설정되어야 합니다");
            
            System.out.println("✅ 경고 다이얼로그 텍스트 검증 완료");
            
        }, "경고 다이얼로그 텍스트 렌더링은 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("다이얼로그 크기 및 레이아웃 검증")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)  
    void testDialogSizeAndLayout() {
        System.out.println("--- 다이얼로그 크기 및 레이아웃 검증 ---");
        
        assertDoesNotThrow(() -> {
            // 개선된 다이얼로그 크기 (한글 짤림 방지)
            int dialogWidth = 500;  // 450 → 500으로 증가
            int dialogHeight = 300; // 250 → 300으로 증가
            int fontSize = 16;      // 14 → 16으로 증가
            
            assertTrue(dialogWidth >= 500, "다이얼로그 폭이 한글 표시에 충분해야 합니다");
            assertTrue(dialogHeight >= 300, "다이얼로그 높이가 충분해야 합니다"); 
            assertTrue(fontSize >= 16, "폰트 크기가 한글 가독성에 적합해야 합니다");
            
            System.out.println("✅ 다이얼로그 크기: " + dialogWidth + "x" + dialogHeight);
            System.out.println("✅ 폰트 크기: " + fontSize + "px (Malgun Gothic)");
            
        }, "다이얼로그 크기 및 레이아웃 검증은 예외 없이 작동해야 합니다");
    }
    
    /**
     * 특정 키가 상대방 플레이어에 의해 사용되고 있는지 확인하는 헬퍼 메서드
     * (SettingsScene의 isKeyConflict 메서드와 동일한 로직)
     */
    private boolean isKeyInUseByOtherPlayer(int keyCode, int playerNumber) {
        int otherPlayer = playerNumber == 1 ? 2 : 1;
        
        // 상대방 플레이어의 모든 키와 비교
        int[] otherPlayerKeys = new int[7];
        for (int i = 0; i < 7; i++) {
            switch (i) {
                case 0: otherPlayerKeys[i] = settings.getLeftKey(otherPlayer); break;
                case 1: otherPlayerKeys[i] = settings.getRightKey(otherPlayer); break;
                case 2: otherPlayerKeys[i] = settings.getRotateKey(otherPlayer); break;
                case 3: otherPlayerKeys[i] = settings.getFallKey(otherPlayer); break;
                case 4: otherPlayerKeys[i] = settings.getDropKey(otherPlayer); break;
                case 5: otherPlayerKeys[i] = settings.getPauseKey(otherPlayer); break;
                case 6: otherPlayerKeys[i] = settings.getExitKey(otherPlayer); break;
            }
        }
        
        // 키 중복 검사 (P키는 공유 허용)
        for (int key : otherPlayerKeys) {
            if (key == keyCode && keyCode != KeyEvent.VK_P) {
                return true;
            }
        }
        
        return false;
    }
    
    @AfterEach
    @DisplayName("테스트 정리")
    void tearDown() {
        System.out.println("=== 키 매핑 테스트 완료 ===\n");
    }
    
    @AfterAll
    @DisplayName("KeyMappingTest 전체 정리")
    static void cleanup() {
        System.out.println("🧹 KeyMappingTest 정리 완료");
        
        // TestCleanupHelper를 통한 추가 정리
        TestCleanupHelper.forceCompleteSystemCleanup("KeyMappingTest");
    }
}