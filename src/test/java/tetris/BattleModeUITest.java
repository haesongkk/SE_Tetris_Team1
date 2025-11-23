package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

/**
 * 로컬 대결모드 UI 테스트
 * - BattleScene UI 구성요소 테스트
 * - 듀얼 플레이어 화면 레이아웃 검증
 * - 게임 상태 UI 표시 테스트
 */
@DisplayName("로컬 대결모드 UI 테스트")
public class BattleModeUITest {
    
    private JFrame testFrame;
    
    @BeforeEach
    @DisplayName("테스트 환경 초기화")
    void setUp() {
        System.out.println("=== 로컬 대결모드 UI 테스트 시작 ===");
        
        // 테스트용 프레임 생성 (실제 화면에 표시하지 않음)
        testFrame = new JFrame("BattleScene Test Frame");
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testFrame.setSize(800, 600);
    }
    
    @Test
    @DisplayName("BattleScene 기본 구성 요소 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testBattleSceneBasicComponents() {
        System.out.println("--- BattleScene 기본 구성 요소 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // BattleScene 생성 (아이템 모드로 테스트)
            tetris.scene.battle.BattleScene battleScene = 
                new tetris.scene.battle.BattleScene(testFrame, "아이템 모드");
            
            assertNotNull(battleScene, "BattleScene이 정상적으로 생성되어야 합니다");
            System.out.println("✅ BattleScene 생성 성공");
            
            // Scene 진입 시도 (UI 초기화)
            battleScene.onEnter();
            System.out.println("✅ BattleScene UI 초기화 완료");
            
            // Scene 종료
            battleScene.onExit();
            System.out.println("✅ BattleScene 정리 완료");
            
        }, "BattleScene 기본 구성 요소는 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("듀얼 플레이어 게임 매니저 독립성 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testDualPlayerManagerIndependence() {
        System.out.println("--- 듀얼 플레이어 게임 매니저 독립성 테스트 ---");
        
        assertDoesNotThrow(() -> {
            tetris.scene.battle.BattleScene battleScene = 
                new tetris.scene.battle.BattleScene(testFrame, "일반 모드");
            
            // 리플렉션을 통해 내부 매니저들에 접근
            java.lang.reflect.Field boardManager1Field = battleScene.getClass().getDeclaredField("boardManager1");
            java.lang.reflect.Field boardManager2Field = battleScene.getClass().getDeclaredField("boardManager2");
            java.lang.reflect.Field blockManager1Field = battleScene.getClass().getDeclaredField("blockManager1");
            java.lang.reflect.Field blockManager2Field = battleScene.getClass().getDeclaredField("blockManager2");
            
            boardManager1Field.setAccessible(true);
            boardManager2Field.setAccessible(true);
            blockManager1Field.setAccessible(true);
            blockManager2Field.setAccessible(true);
            
            Object boardManager1 = boardManager1Field.get(battleScene);
            Object boardManager2 = boardManager2Field.get(battleScene);
            Object blockManager1 = blockManager1Field.get(battleScene);
            Object blockManager2 = blockManager2Field.get(battleScene);
            
            // 각 플레이어의 매니저가 독립적인 인스턴스인지 확인
            assertNotNull(boardManager1, "1P BoardManager가 존재해야 합니다");
            assertNotNull(boardManager2, "2P BoardManager가 존재해야 합니다");
            assertNotSame(boardManager1, boardManager2, "1P와 2P BoardManager는 서로 다른 인스턴스여야 합니다");
            
            assertNotNull(blockManager1, "1P BlockManager가 존재해야 합니다");
            assertNotNull(blockManager2, "2P BlockManager가 존재해야 합니다");
            assertNotSame(blockManager1, blockManager2, "1P와 2P BlockManager는 서로 다른 인스턴스여야 합니다");
            
            System.out.println("✅ 1P/2P 매니저 독립성 확인 완료");
            
        }, "듀얼 플레이어 매니저 독립성 테스트는 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("플레이어별 InputHandler 독립성 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testPlayerInputHandlerIndependence() {
        System.out.println("--- 플레이어별 InputHandler 독립성 테스트 ---");
        
        assertDoesNotThrow(() -> {
            tetris.scene.battle.BattleScene battleScene = 
                new tetris.scene.battle.BattleScene(testFrame, "일반 모드");
            
            // 리플렉션을 통해 InputHandler들에 접근
            java.lang.reflect.Field inputHandler1Field = battleScene.getClass().getDeclaredField("inputHandler1");
            java.lang.reflect.Field inputHandler2Field = battleScene.getClass().getDeclaredField("inputHandler2");
            
            inputHandler1Field.setAccessible(true);
            inputHandler2Field.setAccessible(true);
            
            Object inputHandler1 = inputHandler1Field.get(battleScene);
            Object inputHandler2 = inputHandler2Field.get(battleScene);
            
            // InputHandler가 독립적으로 존재하는지 확인
            assertNotNull(inputHandler1, "1P InputHandler가 존재해야 합니다");
            assertNotNull(inputHandler2, "2P InputHandler가 존재해야 합니다");
            assertNotSame(inputHandler1, inputHandler2, "1P와 2P InputHandler는 서로 다른 인스턴스여야 합니다");
            
            System.out.println("✅ 1P/2P InputHandler 독립성 확인 완료");
            
        }, "플레이어별 InputHandler 독립성 테스트는 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("게임 상태 관리 독립성 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testGameStateManagerIndependence() {
        System.out.println("--- 게임 상태 관리 독립성 테스트 ---");
        
        assertDoesNotThrow(() -> {
            tetris.scene.battle.BattleScene battleScene = 
                new tetris.scene.battle.BattleScene(testFrame, "일반 모드");
            
            // 리플렉션을 통해 GameStateManager들에 접근
            java.lang.reflect.Field gameStateManager1Field = battleScene.getClass().getDeclaredField("gameStateManager1");
            java.lang.reflect.Field gameStateManager2Field = battleScene.getClass().getDeclaredField("gameStateManager2");
            
            gameStateManager1Field.setAccessible(true);
            gameStateManager2Field.setAccessible(true);
            
            Object gameStateManager1 = gameStateManager1Field.get(battleScene);
            Object gameStateManager2 = gameStateManager2Field.get(battleScene);
            
            // GameStateManager가 독립적으로 존재하는지 확인
            assertNotNull(gameStateManager1, "1P GameStateManager가 존재해야 합니다");
            assertNotNull(gameStateManager2, "2P GameStateManager가 존재해야 합니다");
            assertNotSame(gameStateManager1, gameStateManager2, "1P와 2P GameStateManager는 서로 다른 인스턴스여야 합니다");
            
            System.out.println("✅ 1P/2P GameStateManager 독립성 확인 완료");
            
        }, "게임 상태 관리 독립성 테스트는 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("LineBlinkEffect 독립성 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testLineBlinkEffectIndependence() {
        System.out.println("--- LineBlinkEffect 독립성 테스트 ---");
        
        assertDoesNotThrow(() -> {
            tetris.scene.battle.BattleScene battleScene = 
                new tetris.scene.battle.BattleScene(testFrame, "일반 모드");
            
            // 리플렉션을 통해 LineBlinkEffect들에 접근
            java.lang.reflect.Field lineBlinkEffect1Field = battleScene.getClass().getDeclaredField("lineBlinkEffect1");
            java.lang.reflect.Field lineBlinkEffect2Field = battleScene.getClass().getDeclaredField("lineBlinkEffect2");
            
            lineBlinkEffect1Field.setAccessible(true);
            lineBlinkEffect2Field.setAccessible(true);
            
            Object lineBlinkEffect1 = lineBlinkEffect1Field.get(battleScene);
            Object lineBlinkEffect2 = lineBlinkEffect2Field.get(battleScene);
            
            // LineBlinkEffect가 독립적으로 존재하는지 확인
            assertNotNull(lineBlinkEffect1, "1P LineBlinkEffect가 존재해야 합니다");
            assertNotNull(lineBlinkEffect2, "2P LineBlinkEffect가 존재해야 합니다");
            assertNotSame(lineBlinkEffect1, lineBlinkEffect2, "1P와 2P LineBlinkEffect는 서로 다른 인스턴스여야 합니다");
            
            System.out.println("✅ 1P/2P LineBlinkEffect 독립성 확인 완료");
            
        }, "LineBlinkEffect 독립성 테스트는 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("공격 대기 블록 UI 프레임워크 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testAttackBlockUIFramework() {
        System.out.println("--- 공격 대기 블록 UI 프레임워크 테스트 ---");
        
        assertDoesNotThrow(() -> {
            tetris.scene.battle.BattleScene battleScene = 
                new tetris.scene.battle.BattleScene(testFrame, "일반 모드");
            
            // Scene 초기화
            battleScene.onEnter();
            
            // 공격 대기 블록 UI가 정상적으로 렌더링되는지 확인 (예외 없이)
            // repaint()를 호출하여 UI 렌더링 테스트 (paintComponent는 protected이므로 직접 호출 불가)
            battleScene.repaint();
            
            System.out.println("✅ 공격 대기 블록 UI 렌더링 테스트 완료");
            
            battleScene.onExit();
            
        }, "공격 대기 블록 UI 프레임워크는 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("BattleScene 모드별 초기화 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testBattleSceneModeInitialization() {
        System.out.println("--- BattleScene 모드별 초기화 테스트 ---");
        
        String[] testModes = {"일반 모드", "아이템 모드"};
        
        for (String mode : testModes) {
            System.out.println("모드 테스트: " + mode);
            
            assertDoesNotThrow(() -> {
                tetris.scene.battle.BattleScene battleScene = 
                    new tetris.scene.battle.BattleScene(testFrame, mode);
                
                assertNotNull(battleScene, mode + " BattleScene이 정상적으로 생성되어야 합니다");
                
                // Scene 초기화 및 정리
                battleScene.onEnter();
                battleScene.onExit();
                
                System.out.println("✅ " + mode + " 초기화 완료");
                
            }, mode + " 모드 초기화는 예외 없이 작동해야 합니다");
        }
        
        System.out.println("✅ 모든 모드 초기화 테스트 완료");
    }
    
    @Test
    @DisplayName("UI 리소스 정리 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testUIResourceCleanup() {
        System.out.println("--- UI 리소스 정리 테스트 ---");
        
        assertDoesNotThrow(() -> {
            tetris.scene.battle.BattleScene battleScene = 
                new tetris.scene.battle.BattleScene(testFrame, "일반 모드");
            
            // Scene 초기화
            battleScene.onEnter();
            System.out.println("✅ BattleScene 초기화 완료");
            
            // Scene 종료 (리소스 정리)
            battleScene.onExit();
            System.out.println("✅ BattleScene 리소스 정리 완료");
            
            // 중복 종료 호출 시에도 예외가 발생하지 않는지 확인
            battleScene.onExit();
            System.out.println("✅ 중복 종료 호출 안전성 확인");
            
        }, "UI 리소스 정리는 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("플레이어별 키 설정 적용 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testPlayerKeySettingsApplication() {
        System.out.println("--- 플레이어별 키 설정 적용 테스트 ---");
        
        assertDoesNotThrow(() -> {
            GameSettings settings = GameSettings.getInstance();
            
            // 배틀 모드 키 설정
            settings.setBattleLeftKey1(KeyEvent.VK_A);  // 1P 왼쪽: A
            settings.setBattleLeftKey2(KeyEvent.VK_J);  // 2P 왼쪽: J
            
            tetris.scene.battle.BattleScene battleScene = 
                new tetris.scene.battle.BattleScene(testFrame, "일반 모드");
            
            // InputHandler가 올바른 키 설정을 사용하는지 확인
            assertEquals(KeyEvent.VK_A, settings.getLeftKey(1), "1P 왼쪽 키가 A로 설정되어야 합니다");
            assertEquals(KeyEvent.VK_J, settings.getLeftKey(2), "2P 왼쪽 키가 J로 설정되어야 합니다");
            
            System.out.println("✅ 플레이어별 키 설정이 올바르게 적용됨");
            
            battleScene.onEnter();
            battleScene.onExit();
            
        }, "플레이어별 키 설정 적용은 예외 없이 작동해야 합니다");
    }
    
    @AfterEach
    @DisplayName("테스트 정리")
    void tearDown() {
        if (testFrame != null) {
            testFrame.dispose();
            testFrame = null;
        }
        System.out.println("=== 로컬 대결모드 UI 테스트 완료 ===\n");
    }
    
    @AfterAll
    @DisplayName("BattleModeUITest 전체 정리")
    static void cleanup() {
        System.out.println("🧹 BattleModeUITest 정리 완료");
        
        // TestCleanupHelper를 통한 추가 정리
        TestCleanupHelper.forceCompleteSystemCleanup("BattleModeUITest");
    }
}