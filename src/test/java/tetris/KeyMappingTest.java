package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

/**
 * 키 매핑 시스템 테스트
 * - 플레이어별 키 설정이 올바르게 작동하는지 검증
 * - 싱글 플레이어, 배틀 모드 1P, 배틀 모드 2P 키 독립성 확인
 */
@DisplayName("키 매핑 시스템 테스트")
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