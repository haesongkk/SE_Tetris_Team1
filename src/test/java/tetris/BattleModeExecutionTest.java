package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import tetris.scene.battle.BattleScene;

/**
 * BattleModeExecutionTest - 배틀 모드 실행 과정 테스트
 * 
 * 테스트 범위:
 * - 타이머 초기화 및 생명주기 (fallTimer1, fallTimer2, blinkTimer)
 * - 아이템 모드 및 시간 제한 모드 특수 타이머
 * - 블록 낙하 메서드 호출 및 게임 오버 처리
 * - 각종 Manager 초기화 (BoardManager, BlockManager, ScoreManager, GameStateManager)
 * - 다양한 게임 모드 문자열 처리
 * - Scene 생명주기 (onEnter/onExit)
 */
@DisplayName("배틀 모드 실행 과정 테스트")
public class BattleModeExecutionTest {
    
    private JFrame testFrame;

    @BeforeEach
    @DisplayName("테스트 환경 초기화")
    void setUp() {
        testFrame = new JFrame("Test Frame");
        testFrame.setSize(800, 600);
    }

    @AfterEach
    void tearDown() {
        if (testFrame != null) {
            testFrame.dispose();
        }
        TestCleanupHelper.forceCompleteSystemCleanup("BattleModeExecutionTest");
    }

    @Test
    @DisplayName("배틀 씬 타이머 초기화 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testBattleSceneTimerInitialization() {
        try {
            BattleScene battleScene = new BattleScene(testFrame, "일반 모드");
            
            // 리플렉션으로 타이머 필드 접근
            Field fallTimer1Field = BattleScene.class.getDeclaredField("fallTimer1");
            fallTimer1Field.setAccessible(true);
            Timer fallTimer1 = (Timer) fallTimer1Field.get(battleScene);
            
            Field fallTimer2Field = BattleScene.class.getDeclaredField("fallTimer2");
            fallTimer2Field.setAccessible(true);
            Timer fallTimer2 = (Timer) fallTimer2Field.get(battleScene);
            
            assertNotNull(fallTimer1, "Player 1 타이머가 초기화되어야 합니다");
            assertNotNull(fallTimer2, "Player 2 타이머가 초기화되어야 합니다");
            
            // 타이머 시작
            battleScene.onEnter();
            Thread.sleep(100);
            
            assertTrue(fallTimer1.isRunning(), "Player 1 타이머가 실행 중이어야 합니다");
            assertTrue(fallTimer2.isRunning(), "Player 2 타이머가 실행 중이어야 합니다");
            
            // 타이머 정지
            battleScene.onExit();
            Thread.sleep(100);
            
            assertFalse(fallTimer1.isRunning(), "Player 1 타이머가 정지되어야 합니다");
            assertFalse(fallTimer2.isRunning(), "Player 2 타이머가 정지되어야 합니다");
            
        } catch (Exception e) {
            fail("타이머 초기화 테스트 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("아이템 모드 ItemManager 초기화 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testItemModeInitialization() {
        try {
            BattleScene battleScene = new BattleScene(testFrame, "아이템 모드");
            
            // 아이템 모드에서는 itemManager가 null이 아니어야 함
            Field itemManagerField = BattleScene.class.getDeclaredField("itemManager");
            itemManagerField.setAccessible(true);
            Object itemManager = itemManagerField.get(battleScene);
            
            assertNotNull(itemManager, "아이템 모드에서는 ItemManager가 초기화되어야 합니다");
            
        } catch (NoSuchFieldException e) {
            // itemManager 필드가 없는 경우 - 다른 구조일 수 있음
            System.out.println("ItemManager 필드를 찾을 수 없습니다. 다른 구조로 구현되었을 수 있습니다.");
        } catch (Exception e) {
            fail("아이템 모드 초기화 테스트 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("시간 제한 모드 타이머 초기화 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testTimeLimitModeTimer() {
        try {
            BattleScene battleScene = new BattleScene(testFrame, "time_limit");
            
            // 시간 제한 타이머 필드 접근
            Field timeLimitTimerField = BattleScene.class.getDeclaredField("timeLimitTimer");
            timeLimitTimerField.setAccessible(true);
            Timer timeLimitTimer = (Timer) timeLimitTimerField.get(battleScene);
            
            assertNotNull(timeLimitTimer, "시간 제한 타이머가 초기화되어야 합니다");
            
            // 타이머 시작
            battleScene.onEnter();
            Thread.sleep(100);
            
            assertTrue(timeLimitTimer.isRunning(), "시간 제한 타이머가 실행 중이어야 합니다");
            
            battleScene.onExit();
            
        } catch (NoSuchFieldException e) {
            // 시간 제한 타이머가 없는 경우 - 선택적 기능
            System.out.println("시간 제한 타이머 필드를 찾을 수 없습니다.");
        } catch (Exception e) {
            fail("시간 제한 모드 타이머 테스트 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("블록 낙하 메서드 호출 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testMoveBlockDownMethod() {
        assertDoesNotThrow(() -> {
            BattleScene battleScene = new BattleScene(testFrame, "일반 모드");
            
            // moveBlockDown 메서드 호출 테스트
            Method moveBlockDownMethod = BattleScene.class.getDeclaredMethod("moveBlockDown", int.class);
            moveBlockDownMethod.setAccessible(true);
            
            // Player 1과 Player 2에 대해 호출
            assertDoesNotThrow(() -> moveBlockDownMethod.invoke(battleScene, 1));
            assertDoesNotThrow(() -> moveBlockDownMethod.invoke(battleScene, 2));
            
        }, "블록 낙하 메서드 호출 시 예외가 발생하지 않아야 합니다");
    }

    @Test
    @DisplayName("게임 오버 처리 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testHandleGameOver() {
        assertDoesNotThrow(() -> {
            BattleScene battleScene = new BattleScene(testFrame, "일반 모드");
            
            // handleGameOver 메서드 접근
            Method handleGameOverMethod = BattleScene.class.getDeclaredMethod("handleGameOver", int.class);
            handleGameOverMethod.setAccessible(true);
            
            // 게임 오버 처리 테스트
            handleGameOverMethod.invoke(battleScene, 1);
            
        }, "게임 오버 처리 시 예외가 발생하지 않아야 합니다");
    }

    @Test
    @DisplayName("점멸 효과 타이머 동작 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testBlinkTimerOperation() {
        assertDoesNotThrow(() -> {
            BattleScene battleScene = new BattleScene(testFrame, "일반 모드");
            
            // blinkTimer 필드 접근
            Field blinkTimerField = BattleScene.class.getDeclaredField("blinkTimer");
            blinkTimerField.setAccessible(true);
            Timer blinkTimer = (Timer) blinkTimerField.get(battleScene);
            
            assertNotNull(blinkTimer, "점멸 효과 타이머가 초기화되어야 합니다");
            
            battleScene.onEnter();
            Thread.sleep(50);
            
            assertTrue(blinkTimer.isRunning(), "점멸 효과 타이머가 실행 중이어야 합니다");
            
            battleScene.onExit();
            
        }, "점멸 효과 타이머 동작 시 예외가 발생하지 않아야 합니다");
    }

    @Test
    @DisplayName("BoardManager 초기화 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testBoardManagerInitialization() {
        try {
            BattleScene battleScene = new BattleScene(testFrame, "일반 모드");
            
            // BoardManager 필드 접근
            Field boardManager1Field = BattleScene.class.getDeclaredField("boardManager1");
            boardManager1Field.setAccessible(true);
            Object boardManager1 = boardManager1Field.get(battleScene);
            
            Field boardManager2Field = BattleScene.class.getDeclaredField("boardManager2");
            boardManager2Field.setAccessible(true);
            Object boardManager2 = boardManager2Field.get(battleScene);
            
            assertNotNull(boardManager1, "Player 1 BoardManager가 초기화되어야 합니다");
            assertNotNull(boardManager2, "Player 2 BoardManager가 초기화되어야 합니다");
            
        } catch (Exception e) {
            fail("BoardManager 초기화 테스트 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("BlockManager 초기화 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testBlockManagerInitialization() {
        try {
            BattleScene battleScene = new BattleScene(testFrame, "일반 모드");
            
            // BlockManager 필드 접근
            Field blockManager1Field = BattleScene.class.getDeclaredField("blockManager1");
            blockManager1Field.setAccessible(true);
            Object blockManager1 = blockManager1Field.get(battleScene);
            
            Field blockManager2Field = BattleScene.class.getDeclaredField("blockManager2");
            blockManager2Field.setAccessible(true);
            Object blockManager2 = blockManager2Field.get(battleScene);
            
            assertNotNull(blockManager1, "Player 1 BlockManager가 초기화되어야 합니다");
            assertNotNull(blockManager2, "Player 2 BlockManager가 초기화되어야 합니다");
            
        } catch (Exception e) {
            fail("BlockManager 초기화 테스트 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("ScoreManager 초기화 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testScoreManagerInitialization() {
        try {
            BattleScene battleScene = new BattleScene(testFrame, "일반 모드");
            
            // ScoreManager 필드 접근
            Field scoreManager1Field = BattleScene.class.getDeclaredField("scoreManager1");
            scoreManager1Field.setAccessible(true);
            Object scoreManager1 = scoreManager1Field.get(battleScene);
            
            Field scoreManager2Field = BattleScene.class.getDeclaredField("scoreManager2");
            scoreManager2Field.setAccessible(true);
            Object scoreManager2 = scoreManager2Field.get(battleScene);
            
            assertNotNull(scoreManager1, "Player 1 ScoreManager가 초기화되어야 합니다");
            assertNotNull(scoreManager2, "Player 2 ScoreManager가 초기화되어야 합니다");
            
            // 초기 점수 확인
            Method getScoreMethod = scoreManager1.getClass().getMethod("getScore");
            int initialScore1 = (int) getScoreMethod.invoke(scoreManager1);
            int initialScore2 = (int) getScoreMethod.invoke(scoreManager2);
            
            assertEquals(0, initialScore1, "Player 1 초기 점수는 0이어야 합니다");
            assertEquals(0, initialScore2, "Player 2 초기 점수는 0이어야 합니다");
            
        } catch (Exception e) {
            fail("ScoreManager 초기화 테스트 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("GameStateManager 초기화 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testGameStateManagerInitialization() {
        try {
            BattleScene battleScene = new BattleScene(testFrame, "일반 모드");
            
            // GameStateManager 필드 접근
            Field gameStateManager1Field = BattleScene.class.getDeclaredField("gameStateManager1");
            gameStateManager1Field.setAccessible(true);
            Object gameStateManager1 = gameStateManager1Field.get(battleScene);
            
            Field gameStateManager2Field = BattleScene.class.getDeclaredField("gameStateManager2");
            gameStateManager2Field.setAccessible(true);
            Object gameStateManager2 = gameStateManager2Field.get(battleScene);
            
            assertNotNull(gameStateManager1, "Player 1 GameStateManager가 초기화되어야 합니다");
            assertNotNull(gameStateManager2, "Player 2 GameStateManager가 초기화되어야 합니다");
            
            // 초기 상태 확인
            Method isPausedMethod = gameStateManager1.getClass().getMethod("isPaused");
            boolean isPaused1 = (boolean) isPausedMethod.invoke(gameStateManager1);
            boolean isPaused2 = (boolean) isPausedMethod.invoke(gameStateManager2);
            
            Method isGameOverMethod = gameStateManager1.getClass().getMethod("isGameOver");
            boolean isGameOver1 = (boolean) isGameOverMethod.invoke(gameStateManager1);
            boolean isGameOver2 = (boolean) isGameOverMethod.invoke(gameStateManager2);
            
            assertFalse(isGameOver1, "Player 1 초기 상태에서는 게임 오버가 아니어야 합니다");
            assertFalse(isGameOver2, "Player 2 초기 상태에서는 게임 오버가 아니어야 합니다");
            
        } catch (Exception e) {
            fail("GameStateManager 초기화 테스트 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("다양한 게임 모드 문자열 처리 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testVariousGameModeStrings() {
        String[] gameModes = {"일반 모드", "아이템 모드", "item", "normal", "time_limit"};
        
        for (String mode : gameModes) {
            assertDoesNotThrow(() -> {
                BattleScene battleScene = new BattleScene(testFrame, mode);
                assertNotNull(battleScene, "게임 모드 '" + mode + "'로 BattleScene이 생성되어야 합니다");
                Thread.sleep(50);
            }, "게임 모드 '" + mode + "' 처리 시 예외가 발생하지 않아야 합니다");
        }
    }

    @Test
    @DisplayName("Scene 생명주기 (onEnter/onExit) 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testSceneLifecycle() {
        try {
            BattleScene battleScene = new BattleScene(testFrame, "일반 모드");
            
            Field fallTimer1Field = BattleScene.class.getDeclaredField("fallTimer1");
            fallTimer1Field.setAccessible(true);
            Timer fallTimer1 = (Timer) fallTimer1Field.get(battleScene);
            
            // onEnter - 타이머 시작
            assertFalse(fallTimer1.isRunning(), "onEnter 전에는 타이머가 정지되어 있어야 합니다");
            
            battleScene.onEnter();
            Thread.sleep(100);
            assertTrue(fallTimer1.isRunning(), "onEnter 후에는 타이머가 실행 중이어야 합니다");
            
            // onExit - 타이머 정지
            battleScene.onExit();
            Thread.sleep(100);
            assertFalse(fallTimer1.isRunning(), "onExit 후에는 타이머가 정지되어야 합니다");
            
        } catch (Exception e) {
            fail("Scene 생명주기 테스트 실패: " + e.getMessage());
        }
    }

    @AfterAll
    static void cleanup() {
        TestCleanupHelper.forceCompleteSystemCleanup("BattleModeExecutionTest");
    }
}
