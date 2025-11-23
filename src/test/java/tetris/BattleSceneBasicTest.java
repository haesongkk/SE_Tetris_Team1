package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import tetris.scene.battle.BattleScene;

/**
 * BattleScene 기본 구성 테스트
 * - BattleScene 생성 및 초기화 검증
 * - 일반 모드와 아이템 모드 지원 확인
 * - Scene 생명주기 (onEnter/onExit) 테스트
 */
@DisplayName("BattleScene 기본 구성 테스트")
public class BattleSceneBasicTest {
    
    private JFrame testFrame;
    
    @BeforeEach
    @DisplayName("테스트 환경 초기화")
    void setUp() {
        System.out.println("=== BattleScene 기본 구성 테스트 시작 ===");
        
        // 테스트용 프레임 생성 (실제 화면에 표시하지 않음)
        testFrame = new JFrame("BattleScene Basic Test Frame");
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testFrame.setSize(1200, 800); // BattleScene에 적합한 크기
        // 실제로 표시하지 않음 (setVisible(false))
    }
    
    @Test
    @DisplayName("일반 모드 BattleScene 생성 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testNormalModeBattleSceneCreation() {
        System.out.println("--- 일반 모드 BattleScene 생성 테스트 ---");
        
        BattleScene battleScene = null;
        
        assertDoesNotThrow(() -> {
            // 일반 모드 BattleScene 생성
            BattleScene scene = new BattleScene(testFrame, "일반 모드");
            assertNotNull(scene, "일반 모드 BattleScene이 정상적으로 생성되어야 합니다");
            System.out.println("✅ 일반 모드 BattleScene 생성 성공");
            
        }, "일반 모드 BattleScene 생성은 예외를 발생시키지 않아야 합니다");
    }
    
    @Test
    @DisplayName("아이템 모드 BattleScene 생성 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testItemModeBattleSceneCreation() {
        System.out.println("--- 아이템 모드 BattleScene 생성 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 아이템 모드 BattleScene 생성
            BattleScene scene = new BattleScene(testFrame, "아이템 모드");
            assertNotNull(scene, "아이템 모드 BattleScene이 정상적으로 생성되어야 합니다");
            System.out.println("✅ 아이템 모드 BattleScene 생성 성공");
            
        }, "아이템 모드 BattleScene 생성은 예외를 발생시키지 않아야 합니다");
    }
    
    @Test
    @DisplayName("BattleScene onEnter 생명주기 테스트")
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testBattleSceneOnEnterLifecycle() {
        System.out.println("--- BattleScene onEnter 생명주기 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // BattleScene 생성
            BattleScene battleScene = new BattleScene(testFrame, "일반 모드");
            assertNotNull(battleScene, "BattleScene이 생성되어야 합니다");
            System.out.println("✅ BattleScene 생성 완료");
            
            // Scene 진입 (UI 초기화, 타이머 시작 등)
            battleScene.onEnter();
            System.out.println("✅ BattleScene onEnter 호출 성공");
            
            // Scene이 활성화된 상태에서 간단한 작업 수행
            // (실제 UI 업데이트나 게임 로직 실행 없이 구조만 확인)
            Thread.sleep(100); // 짧은 대기로 초기화 완료 확인
            System.out.println("✅ BattleScene 활성 상태 유지 확인");
            
            // Scene 종료
            battleScene.onExit();
            System.out.println("✅ BattleScene onExit 호출 성공");
            
        }, "BattleScene 생명주기 테스트는 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("BattleScene 중복 onEnter/onExit 호출 안정성 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testBattleSceneDuplicateLifecycleCalls() {
        System.out.println("--- BattleScene 중복 생명주기 호출 안정성 테스트 ---");
        
        assertDoesNotThrow(() -> {
            BattleScene battleScene = new BattleScene(testFrame, "일반 모드");
            
            // 중복 onEnter 호출
            battleScene.onEnter();
            System.out.println("✅ 첫 번째 onEnter 호출");
            
            battleScene.onEnter(); // 중복 호출
            System.out.println("✅ 중복 onEnter 호출 (예외 없음)");
            
            // 중복 onExit 호출
            battleScene.onExit();
            System.out.println("✅ 첫 번째 onExit 호출");
            
            battleScene.onExit(); // 중복 호출
            System.out.println("✅ 중복 onExit 호출 (예외 없음)");
            
        }, "중복 생명주기 호출은 안전하게 처리되어야 합니다");
    }
    
    @Test
    @DisplayName("BattleScene 필수 컴포넌트 존재 확인 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testBattleSceneEssentialComponents() {
        System.out.println("--- BattleScene 필수 컴포넌트 존재 확인 테스트 ---");
        
        assertDoesNotThrow(() -> {
            BattleScene battleScene = new BattleScene(testFrame, "일반 모드");
            
            // 리플렉션을 통해 필수 컴포넌트들이 초기화되었는지 확인
            Class<?> battleSceneClass = battleScene.getClass();
            
            // 1P 컴포넌트들 확인
            assertNotNull(getField(battleScene, battleSceneClass, "boardManager1"), 
                "1P BoardManager가 초기화되어야 합니다");
            assertNotNull(getField(battleScene, battleSceneClass, "blockManager1"), 
                "1P BlockManager가 초기화되어야 합니다");
            assertNotNull(getField(battleScene, battleSceneClass, "scoreManager1"), 
                "1P ScoreManager가 초기화되어야 합니다");
            assertNotNull(getField(battleScene, battleSceneClass, "gameStateManager1"), 
                "1P GameStateManager가 초기화되어야 합니다");
            assertNotNull(getField(battleScene, battleSceneClass, "inputHandler1"), 
                "1P InputHandler가 초기화되어야 합니다");
            assertNotNull(getField(battleScene, battleSceneClass, "lineBlinkEffect1"), 
                "1P LineBlinkEffect가 초기화되어야 합니다");
            
            System.out.println("✅ 1P 컴포넌트들 초기화 확인");
            
            // 2P 컴포넌트들 확인
            assertNotNull(getField(battleScene, battleSceneClass, "boardManager2"), 
                "2P BoardManager가 초기화되어야 합니다");
            assertNotNull(getField(battleScene, battleSceneClass, "blockManager2"), 
                "2P BlockManager가 초기화되어야 합니다");
            assertNotNull(getField(battleScene, battleSceneClass, "scoreManager2"), 
                "2P ScoreManager가 초기화되어야 합니다");
            assertNotNull(getField(battleScene, battleSceneClass, "gameStateManager2"), 
                "2P GameStateManager가 초기화되어야 합니다");
            assertNotNull(getField(battleScene, battleSceneClass, "inputHandler2"), 
                "2P InputHandler가 초기화되어야 합니다");
            assertNotNull(getField(battleScene, battleSceneClass, "lineBlinkEffect2"), 
                "2P LineBlinkEffect가 초기화되어야 합니다");
            
            System.out.println("✅ 2P 컴포넌트들 초기화 확인");
            
        }, "BattleScene 필수 컴포넌트 확인은 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("아이템 모드 전용 컴포넌트 확인 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testItemModeSpecificComponents() {
        System.out.println("--- 아이템 모드 전용 컴포넌트 확인 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 일반 모드에서는 ItemManager가 null이어야 함
            BattleScene normalBattleScene = new BattleScene(testFrame, "일반 모드");
            Class<?> battleSceneClass = normalBattleScene.getClass();
            
            Object itemManager1Normal = getField(normalBattleScene, battleSceneClass, "itemManager1");
            Object itemManager2Normal = getField(normalBattleScene, battleSceneClass, "itemManager2");
            
            assertNull(itemManager1Normal, "일반 모드에서 1P ItemManager는 null이어야 합니다");
            assertNull(itemManager2Normal, "일반 모드에서 2P ItemManager는 null이어야 합니다");
            System.out.println("✅ 일반 모드 ItemManager null 확인");
            
            // 아이템 모드에서는 ItemManager가 초기화되어야 함
            BattleScene itemBattleScene = new BattleScene(testFrame, "아이템 모드");
            
            Object itemManager1Item = getField(itemBattleScene, battleSceneClass, "itemManager1");
            Object itemManager2Item = getField(itemBattleScene, battleSceneClass, "itemManager2");
            
            assertNotNull(itemManager1Item, "아이템 모드에서 1P ItemManager가 초기화되어야 합니다");
            assertNotNull(itemManager2Item, "아이템 모드에서 2P ItemManager가 초기화되어야 합니다");
            System.out.println("✅ 아이템 모드 ItemManager 초기화 확인");
            
        }, "아이템 모드 컴포넌트 확인은 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("다양한 게임 모드 문자열 처리 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testVariousGameModeStrings() {
        System.out.println("--- 다양한 게임 모드 문자열 처리 테스트 ---");
        
        String[] testModes = {
            "일반 모드", 
            "아이템 모드", 
            "item", 
            "normal", 
            "invalid_mode", 
            "", 
            null
        };
        
        for (String mode : testModes) {
            System.out.println("모드 테스트: " + (mode != null ? "\"" + mode + "\"" : "null"));
            
            assertDoesNotThrow(() -> {
                BattleScene battleScene = new BattleScene(testFrame, mode);
                assertNotNull(battleScene, "모든 모드에서 BattleScene이 생성되어야 합니다");
                
                // Scene 초기화 및 정리 테스트
                battleScene.onEnter();
                battleScene.onExit();
                
            }, "모드 \"" + mode + "\"에서 BattleScene은 예외 없이 작동해야 합니다");
        }
        
        System.out.println("✅ 모든 게임 모드 문자열 처리 확인 완료");
    }
    
    /**
     * 리플렉션을 통해 private 필드 값을 가져오는 헬퍼 메서드
     */
    private Object getField(Object instance, Class<?> clazz, String fieldName) {
        try {
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            System.err.println("필드 접근 실패: " + fieldName + " - " + e.getMessage());
            return null;
        }
    }
    
    @AfterEach
    @DisplayName("테스트 정리")
    void tearDown() {
        if (testFrame != null) {
            testFrame.dispose();
            testFrame = null;
        }
        
        // 강제 가비지 컬렉션으로 메모리 정리
        System.gc();
        System.out.println("=== BattleScene 기본 구성 테스트 완료 ===\n");
    }
    
    @AfterAll
    @DisplayName("BattleSceneBasicTest 전체 정리")
    static void cleanup() {
        System.out.println("🧹 BattleSceneBasicTest 정리 완료");
        
        // TestCleanupHelper를 통한 추가 정리
        TestCleanupHelper.forceCompleteSystemCleanup("BattleSceneBasicTest");
    }
}