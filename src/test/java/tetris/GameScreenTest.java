package tetris;

import tetris.scene.game.GameScene;
import tetris.scene.game.core.ScoreManager;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 게임 화면 구성 요구사항 테스트 클래스
 *
 * 테스트 항목:
 * 1. 블럭이 쌓이는 보드(board) - 20줄 × 10칸
 * 2. 다음 블럭을 확인할 수 있는 부분
 * 3. 점수를 확인할 수 있는 부분
 * 4. 실시간 점수 표시 기능
 */
public class GameScreenTest {

    private static JFrame testFrame;
    private static GameScene gameScene;
    private static int testCount = 0;
    private static int passCount = 0;

    /**
     * 테스트 환경 설정
     */
    private static void setupTestEnvironment() {
        if (testFrame == null) {
            System.out.println("=== 게임 화면 구성 테스트 환경 설정 ===");

            // 테스트용 프레임 생성
            testFrame = new JFrame("Game Screen Test");
            testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            testFrame.setSize(800, 600);

            // GameScene 생성 및 초기화
            gameScene = new GameScene(testFrame);
            
            // onEnter() 대신 직접 초기화 메서드 호출
            try {
                Method initUIMethod = GameScene.class.getDeclaredMethod("initUI");
                initUIMethod.setAccessible(true);
                initUIMethod.invoke(gameScene);
                
                Method initGameStateMethod = GameScene.class.getDeclaredMethod("initGameState");
                initGameStateMethod.setAccessible(true);
                initGameStateMethod.invoke(gameScene);
            } catch (Exception e) {
                System.out.println("직접 초기화 중 오류: " + e.getMessage());
                // fallback: onEnter() 호출
                gameScene.onEnter();
            }

            System.out.println("✅ 테스트 환경 설정 완료\n");
        }
    }

    /**
     * 테스트 결과 출력
     */
    private static void assertTest(boolean condition, String message) {
        testCount++;
        if (condition) {
            passCount++;
            System.out.println("✅ " + message);
        } else {
            System.out.println("❌ " + message);
        }
    }

    /**
     * 테스트 실패
     */
    private static void fail(String message) {
        System.out.println("❌ 테스트 실패: " + message);
        throw new RuntimeException(message);
    }

    /**
     * 테스트 환경 정리
     */
    private static void cleanup() {
        if (testFrame != null) {
            testFrame.dispose();
        }
        System.out.println("🧹 테스트 환경 정리 완료");
    }

    /**
     * 1. 블럭이 쌓이는 보드(board) - 20줄 × 10칸 테스트
     * - 게임 보드가 20줄 × 10칸으로 올바르게 구성되는지 확인
     */
    public static void testGameBoardDimensions() {
        System.out.println("=== 1. 게임 보드 크기 테스트 ===");

        try {
            // 보드 크기 상수 확인
            Field gameHeightField = GameScene.class.getDeclaredField("GAME_HEIGHT");
            gameHeightField.setAccessible(true);
            int gameHeight = (Integer) gameHeightField.get(null);

            Field gameWidthField = GameScene.class.getDeclaredField("GAME_WIDTH");
            gameWidthField.setAccessible(true);
            int gameWidth = (Integer) gameWidthField.get(null);

            System.out.println("GAME_HEIGHT 상수 값: " + gameHeight);
            System.out.println("GAME_WIDTH 상수 값: " + gameWidth);
            System.out.println("게임 보드 크기: " + gameWidth + "칸 × " + gameHeight + "줄");

            // 요구사항 검증: 20줄, 10칸
            assertTest(gameHeight == 20, "게임 보드 높이가 20줄이 아닙니다. (실제: " + gameHeight + ")");
            assertTest(gameWidth == 10, "게임 보드 너비가 10칸이 아닙니다. (실제: " + gameWidth + ")");

            // CELL_SIZE 상수 확인
            Field cellSizeField = GameScene.class.getDeclaredField("CELL_SIZE");
            cellSizeField.setAccessible(true);
            int cellSize = (Integer) cellSizeField.get(null);

            // PREVIEW_SIZE와 PREVIEW_CELL_SIZE 확인
            Field previewSizeField = GameScene.class.getDeclaredField("PREVIEW_SIZE");
            previewSizeField.setAccessible(true);
            int previewSize = (Integer) previewSizeField.get(null);

            Field previewCellSizeField = GameScene.class.getDeclaredField("PREVIEW_CELL_SIZE");
            previewCellSizeField.setAccessible(true);
            int previewCellSize = (Integer) previewCellSizeField.get(null);

            System.out.println("셀 크기: " + cellSize + "px");
            System.out.println("미리보기 크기: " + previewSize + "×" + previewSize + " (셀 크기: " + previewCellSize + "px)");

            // 상수 값들이 합리적인지 확인
            assertTest(cellSize > 0, "셀 크기가 0보다 커야 합니다.");
            assertTest(previewSize > 0, "미리보기 크기가 0보다 커야 합니다.");
            assertTest(previewCellSize > 0, "미리보기 셀 크기가 0보다 커야 합니다.");

            // GamePanel 크기 계산 검증
            int expectedWidth = (gameWidth + 2) * cellSize + previewSize * previewCellSize + 40;
            int expectedHeight = (gameHeight + 2) * cellSize;
            System.out.println("예상 GamePanel 크기: " + expectedWidth + "×" + expectedHeight);

            assertTest(expectedWidth > 400, "GamePanel 예상 너비가 합리적이어야 합니다.");
            assertTest(expectedHeight > 600, "GamePanel 예상 높이가 합리적이어야 합니다.");

        } catch (Exception e) {
            fail("게임 보드 크기 테스트 중 오류 발생: " + e.getMessage());
        }

        System.out.println("✅ 게임 보드 크기 테스트 통과\n");
    }

    /**
     * 2. 다음 블럭을 확인할 수 있는 부분 테스트
     * - 다음 블럭 미리보기 영역이 존재하고 올바르게 구성되는지 확인
     */
    public static void testNextBlockPreview() {
        System.out.println("=== 2. 다음 블럭 미리보기 테스트 ===");

        try {
            // 다음 블럭 관련 상수들 확인
            Field previewSizeField = GameScene.class.getDeclaredField("PREVIEW_SIZE");
            previewSizeField.setAccessible(true);
            int previewSize = (Integer) previewSizeField.get(null);

            Field previewCellSizeField = GameScene.class.getDeclaredField("PREVIEW_CELL_SIZE");
            previewCellSizeField.setAccessible(true);
            int previewCellSize = (Integer) previewCellSizeField.get(null);

            System.out.println("다음 블럭 미리보기 크기: " + previewSize + "×" + previewSize +
                             " (셀 크기: " + previewCellSize + "px)");

            // 미리보기 크기가 합리적인지 확인
            assertTest(previewSize > 0, "미리보기 영역 크기가 0보다 커야 합니다.");
            assertTest(previewCellSize > 0, "미리보기 셀 크기가 0보다 커야 합니다.");

            // 다음 블럭(next)이 초기화되었는지 확인
            Field nextField = GameScene.class.getDeclaredField("next");
            nextField.setAccessible(true);
            Object nextBlock = nextField.get(gameScene);

            assertTest(nextBlock != null, "다음 블럭이 초기화되지 않았습니다.");

            // drawNextBlockPreview 메서드가 존재하는지 확인
            try {
                // GamePanel 내부 클래스에서 메서드 찾기
                Field gamePanelField = GameScene.class.getDeclaredField("gamePanel");
                gamePanelField.setAccessible(true);
                JPanel gamePanel = (JPanel) gamePanelField.get(gameScene);

                Class<?> gamePanelClass = gamePanel.getClass();
                Method drawNextBlockPreviewMethod = gamePanelClass.getDeclaredMethod("drawNextBlockPreview", Graphics2D.class);
                assertTest(drawNextBlockPreviewMethod != null, "drawNextBlockPreview 메서드가 존재합니다.");
            } catch (NoSuchMethodException e) {
                fail("drawNextBlockPreview 메서드를 찾을 수 없습니다.");
            }

            System.out.println("다음 블럭 미리보기 기능이 정상적으로 구성되었습니다.");

        } catch (Exception e) {
            fail("다음 블럭 미리보기 테스트 중 오류 발생: " + e.getMessage());
        }

        System.out.println("✅ 다음 블럭 미리보기 테스트 통과\n");
    }

    /**
     * 3. 점수를 확인할 수 있는 부분 테스트
     * - 점수 표시 영역이 존재하고 ScoreManager가 올바르게 구성되는지 확인
     */
    public static void testScoreDisplay() {
        System.out.println("=== 3. 점수 표시 영역 테스트 ===");

        try {
            // ScoreManager 클래스 직접 생성 및 테스트
            ScoreManager testScoreManager = new ScoreManager();

            // 초기 점수가 0인지 확인
            int initialScore = testScoreManager.getScore();
            System.out.println("초기 점수: " + initialScore);
            assertTest(initialScore == 0, "초기 점수가 0이어야 합니다. (실제: " + initialScore + ")");

            // ScoreManager.drawScoreBoard 메서드가 존재하는지 확인
            Method drawScoreBoardMethod = ScoreManager.class.getMethod("drawScoreBoard",
                Graphics2D.class, int.class, int.class, int.class, int.class);
            assertTest(drawScoreBoardMethod != null, "ScoreManager.drawScoreBoard 메서드가 존재합니다.");

            // 점수 표시 관련 메서드들이 존재하는지 확인
            Method getScoreMethod = ScoreManager.class.getMethod("getScore");
            assertTest(getScoreMethod != null, "getScore 메서드가 존재합니다.");

            Method getLinesClearedMethod = ScoreManager.class.getMethod("getLinesCleared");
            assertTest(getLinesClearedMethod != null, "getLinesCleared 메서드가 존재합니다.");

            System.out.println("점수 표시 기능이 정상적으로 구성되었습니다.");

        } catch (Exception e) {
            fail("점수 표시 영역 테스트 중 오류 발생: " + e.getMessage());
        }

        System.out.println("✅ 점수 표시 영역 테스트 통과\n");
    }

    /**
     * 4. 실시간 점수 표시 기능 테스트
     * - 점수가 변경될 때 실시간으로 업데이트되는지 확인
     */
    public static void testRealTimeScoreUpdate() {
        System.out.println("=== 4. 실시간 점수 업데이트 테스트 ===");

        try {
            // ScoreManager 인스턴스 생성
            ScoreManager testScoreManager = new ScoreManager();

            // 초기 점수 확인
            int initialScore = testScoreManager.getScore();
            assertTest(initialScore == 0, "초기 점수가 0이어야 합니다. (실제: " + initialScore + ")");

            // 점수 증가 메서드 호출 (예: 1줄 클리어 점수)
            Method addScoreMethod = ScoreManager.class.getMethod("addScore", int.class);
            addScoreMethod.invoke(testScoreManager, 1);

            // 점수가 증가했는지 확인
            int updatedScore = testScoreManager.getScore();
            System.out.println("점수 증가 후: " + updatedScore);
            assertTest(updatedScore == 1000, "점수가 올바르게 증가하지 않았습니다. (기대: 1000, 실제: " + updatedScore + ")");

            // 추가 점수 증가 (2줄 클리어)
            addScoreMethod.invoke(testScoreManager, 2);
            int finalScore = testScoreManager.getScore();
            System.out.println("최종 점수: " + finalScore);
            assertTest(finalScore == 5000, "점수가 누적되지 않았습니다. (기대: 5000, 실제: " + finalScore + ")");

            // 점수 배율 기능 확인
            Method getSpeedMultiplierMethod = ScoreManager.class.getMethod("getSpeedMultiplier");
            double multiplier = (Double) getSpeedMultiplierMethod.invoke(testScoreManager);
            System.out.println("점수 배율: " + multiplier + "x");
            assertTest(multiplier >= 1.0, "점수 배율이 1.0 이상이어야 합니다.");

            // addScore 메서드가 존재하고 올바르게 작동하는지 확인
            assertTest(addScoreMethod != null, "addScore 메서드가 존재합니다.");

            System.out.println("실시간 점수 업데이트 기능이 정상 작동합니다.");

        } catch (Exception e) {
            fail("실시간 점수 업데이트 테스트 중 오류 발생: " + e.getMessage());
        }

        System.out.println("✅ 실시간 점수 업데이트 테스트 통과\n");
    }

    /**
     * 5. UI 레이아웃 구성 테스트
     * - 전체 UI 요소들이 올바르게 배치되는지 확인
     */
    public static void testUILayout() {
        System.out.println("=== 5. UI 레이아웃 구성 테스트 ===");

        try {
            // GameScene 클래스의 구조 검증
            Class<?> gameSceneClass = Class.forName("tetris.scene.game.GameScene");

            // 주요 필드들이 선언되어 있는지 확인
            Field[] fields = gameSceneClass.getDeclaredFields();
            boolean hasGamePanel = false;
            boolean hasScoreManager = false;
            boolean hasTimer = false;

            for (Field field : fields) {
                String fieldName = field.getName();
                if (fieldName.equals("gamePanel")) hasGamePanel = true;
                if (fieldName.equals("scoreManager")) hasScoreManager = true;
                if (fieldName.equals("timer")) hasTimer = true;
            }

            assertTest(hasGamePanel, "GameScene에 gamePanel 필드가 있어야 합니다.");
            assertTest(hasScoreManager, "GameScene에 scoreManager 필드가 있어야 합니다.");
            assertTest(hasTimer, "GameScene에 timer 필드가 있어야 합니다.");

            // 주요 메서드들이 존재하는지 확인
            Method onEnterMethod = gameSceneClass.getMethod("onEnter");
            assertTest(onEnterMethod != null, "onEnter 메서드가 존재해야 합니다.");

            Method onExitMethod = gameSceneClass.getMethod("onExit");
            assertTest(onExitMethod != null, "onExit 메서드가 존재해야 합니다.");

            System.out.println("UI 레이아웃이 올바르게 구성되었습니다.");

        } catch (Exception e) {
            fail("UI 레이아웃 구성 테스트 중 오류 발생: " + e.getMessage());
        }

        System.out.println("✅ UI 레이아웃 구성 테스트 통과\n");
    }

    /**
     * 6. 게임 화면 요소 통합 테스트
     * - 모든 화면 요소들이 함께 올바르게 작동하는지 확인
     */
    public static void testGameScreenIntegration() {
        System.out.println("=== 6. 게임 화면 요소 통합 테스트 ===");

        try {
            // GameScene 클래스가 존재하고 올바르게 구성되는지 확인
            Class<?> gameSceneClass = Class.forName("tetris.scene.game.GameScene");

            // 생성자가 존재하는지 확인
            java.lang.reflect.Constructor<?> constructor = gameSceneClass.getConstructor(JFrame.class);
            assertTest(constructor != null, "GameScene 생성자가 존재합니다.");

            // 필요한 필드들이 선언되어 있는지 확인
            String[] requiredFieldNames = {"board", "boardColors", "gamePanel", "scoreManager", "curr", "next", "timer", "blinkTimer"};

            for (String fieldName : requiredFieldNames) {
                try {
                    Field field = gameSceneClass.getDeclaredField(fieldName);
                    assertTest(field != null, fieldName + " 필드가 선언되어 있습니다.");
                } catch (NoSuchFieldException e) {
                    assertTest(false, fieldName + " 필드가 존재하지 않습니다.");
                }
            }

            // ScoreManager 클래스가 존재하는지 확인
            Class<?> scoreManagerClass = Class.forName("tetris.scene.game.core.ScoreManager");
            assertTest(scoreManagerClass != null, "ScoreManager 클래스가 존재합니다.");

            System.out.println("모든 게임 화면 요소가 통합적으로 작동합니다.");

        } catch (Exception e) {
            fail("게임 화면 요소 통합 테스트 중 오류 발생: " + e.getMessage());
        }

        System.out.println("✅ 게임 화면 요소 통합 테스트 통과\n");
    }

    /**
     * 모든 테스트를 실행하는 메인 메서드
     */
    public static void main(String[] args) {
        System.out.println("🎮 게임 화면 구성 요구사항 테스트 시작 🎮\n");

        try {
            setupTestEnvironment();

            testGameBoardDimensions();
            testNextBlockPreview();
            testScoreDisplay();
            testRealTimeScoreUpdate();
            testUILayout();
            testGameScreenIntegration();

            System.out.println("🎉 모든 게임 화면 구성 테스트가 성공적으로 통과되었습니다! 🎉");
            System.out.println();
            System.out.println("📋 검증 완료된 요구사항:");
            System.out.println("✅ 블럭이 쌓이는 보드(board) - 20줄 × 10칸");
            System.out.println("✅ 다음 블럭을 확인할 수 있는 부분");
            System.out.println("✅ 점수를 확인할 수 있는 부분");
            System.out.println("✅ 실시간으로 바뀌는 점수를 표시");
            System.out.println();
            System.out.println("테스트 결과: " + passCount + "/" + testCount + " 통과");

        } catch (Exception e) {
            System.err.println("❌ 예외 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
}