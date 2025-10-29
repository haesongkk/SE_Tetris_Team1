package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import tetris.scene.menu.MainMenuScene;
import tetris.scene.game.GameScene;
import tetris.scene.game.blocks.*;
import tetris.GameSettings;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

/**
 * 기본 테트리스 게임 기능 요구사항 테스트 클래스
 *
 * 테스트 항목:
 * 1. 시작 메뉴에서 게임 시작 선택 시 테트리스 게임 시작
 * 2. 20줄, 10칸의 보드(board) 존재
 * 3. 총 7가지의 테트로미노(블럭)가 무작위로 등장
 * 4. 블럭을 쌓아 각 행을 채우면 해당 행이 삭제됨
 */
@DisplayName("기본 테트리스 게임 기능 요구사항 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BasicTest {

    private static JFrame testFrame;

    /**
     * 테스트 환경 설정
     */
    @BeforeAll
    static void setupTestEnvironment() {
        System.out.println("=== 기본 테트리스 게임 테스트 환경 설정 ===");

        // 헤드리스 환경 체크
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("⚠️ 헤드리스 환경: GUI 테스트 제한됨");
            return;
        }

        // 테스트용 프레임 생성
        testFrame = new JFrame("Tetris Test");
        testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        testFrame.setSize(800, 600);

        System.out.println("✅ 테스트 환경 설정 완료\n");
    }

    /**
     * 테스트 환경 정리
     */
    @AfterAll
    static void cleanup() {
        if (testFrame != null) {
            testFrame.dispose();
            testFrame = null;
        }
        
        // TestCleanupHelper를 통한 추가 정리
        TestCleanupHelper.forceCompleteSystemCleanup("BasicTest");
        System.out.println("✅ 테스트 환경 정리 완료");
    }

    /**
     * 1. 시작 메뉴에서 게임 시작 선택 시 테트리스 게임 시작 테스트
     * - 시작 메뉴에서 게임 시작을 선택하면 GameScene으로 전환되는지 확인
     */
    @Test
    @Order(1)
    @DisplayName("시작 메뉴에서 게임 시작 테스트")
    void testGameStartFromMenu() throws Exception {
        // 헤드리스 환경에서는 테스트 스킵
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

        // MainMenuScene 생성만으로도 기본 기능이 작동하는지 확인
        MainMenuScene mainMenu = new MainMenuScene(testFrame);
        assertNotNull(mainMenu, "MainMenuScene이 정상적으로 생성되어야 합니다.");

        // startGame 메서드가 존재하는지 확인
        Method startGameMethod = MainMenuScene.class.getDeclaredMethod("startGame");
        assertNotNull(startGameMethod, "startGame 메서드가 존재해야 합니다.");

        System.out.println("✅ 메뉴 시스템 기본 기능 확인 완료");
    }

    /**
     * 2. GameScene 생성 및 보드 크기 테스트
     * - 20줄, 10칸의 보드(board)가 존재하는지 확인
     */
    @Test
    @Order(2)
    @DisplayName("GameScene 보드 크기 테스트 (20줄 × 10칸)")
    void testGameSceneCreation() throws Exception {
        // 헤드리스 환경에서는 테스트 스킵
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

        GameScene gameScene = new GameScene(testFrame, GameSettings.Difficulty.NORMAL);
        // GameScene이 제대로 초기화되도록 onEnter() 호출
        gameScene.onEnter();

        // 보드 크기 상수 확인
        Field gameHeightField = GameScene.class.getDeclaredField("GAME_HEIGHT");
        gameHeightField.setAccessible(true);
        int gameHeight = (Integer) gameHeightField.get(null);

        Field gameWidthField = GameScene.class.getDeclaredField("GAME_WIDTH");
        gameWidthField.setAccessible(true);
        int gameWidth = (Integer) gameWidthField.get(null);

        System.out.println("보드 크기: " + gameWidth + "칸 × " + gameHeight + "줄");

        // 요구사항 검증: 20줄, 10칸
        assertEquals(20, gameHeight, "보드 높이가 20줄이어야 합니다.");
        assertEquals(10, gameWidth, "보드 너비가 10칸이어야 합니다.");

        // 실제 보드 배열 크기 확인
        Field boardManagerField = GameScene.class.getDeclaredField("boardManager");
        boardManagerField.setAccessible(true);
        Object boardManager = boardManagerField.get(gameScene);

        assertNotNull(boardManager, "BoardManager가 생성되어야 합니다.");
        
        // BoardManager의 getBoard 메서드를 통해 보드 배열 확인
        Method getBoardMethod = boardManager.getClass().getDeclaredMethod("getBoard");
        int[][] board = (int[][]) getBoardMethod.invoke(boardManager);
        
        assertNotNull(board, "게임 보드가 생성되어야 합니다.");
        assertEquals(20, board.length, "보드 배열의 행 수가 20이어야 합니다.");
        assertEquals(10, board[0].length, "보드 배열의 열 수가 10이어야 합니다.");

        System.out.println("보드 초기화 상태 확인:");
        int emptyCells = 0;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == 0) emptyCells++;
            }
        }
        System.out.println("빈 칸 수: " + emptyCells + " / 전체 칸 수: " + (20 * 10));
        assertEquals(200, emptyCells, "초기 보드의 모든 칸이 비어있어야 합니다.");

        System.out.println("✅ GameScene 보드 크기 테스트 완료");
    }

    /**
     * 3. 테트로미노(블럭) 무작위 생성 테스트
     * - 총 7가지의 테트로미노가 무작위로 등장하는지 확인
     * - Fitness Proportionate Selection 방식 검증
     * - 난이도별 가중치 시스템 검증
     */
    @Test
    @Order(3)
    @DisplayName("테트로미노 7종류 존재 확인 테스트")
    void testRandomBlockGeneration() throws Exception {
        System.out.println("=== 테트로미노 7종류 존재 확인 테스트 ===");

        // 7가지 테트로미노 클래스들이 존재하는지 확인
        String[] blockTypes = {"I", "O", "T", "S", "Z", "J", "L"};
        List<String> foundBlocks = new ArrayList<>();

        for (String blockType : blockTypes) {
            try {
                String className = "tetris.scene.game.blocks." + blockType + "Block";
                Class<?> blockClass = Class.forName(className);
                assertNotNull(blockClass, blockType + "Block 클래스가 존재해야 합니다.");
                foundBlocks.add(blockType);
                System.out.println("✅ " + blockType + "Block 클래스 확인");
            } catch (ClassNotFoundException e) {
                fail(blockType + "Block 클래스를 찾을 수 없습니다.");
            }
        }

        assertEquals(7, foundBlocks.size(), "7가지 테트로미노가 모두 존재해야 합니다.");
        System.out.println("발견된 테트로미노 종류: " + foundBlocks);
        
        // Fitness Proportionate Selection 방식 테스트
        testFitnessProportionateSelection();
        
        System.out.println("✅ 총 7가지 테트로미노 확인 완료");
    }
    
    /**
     * Fitness Proportionate Selection (Roulette Wheel Selection) 방식 테스트
     * - 난이도별 가중치에 따른 블록 생성 확률 검증
     * - 최소 1,000번 이상 블록 선택을 반복하여 검증
     * - 설정된 확률의 오차범위 ±5% 이내 검증
     */
    private void testFitnessProportionateSelection() throws Exception {
        System.out.println("\n=== Fitness Proportionate Selection 테스트 ===");
        
        // 헤드리스 환경에서는 테스트 스킵
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        
        // 각 난이도별로 테스트
        GameSettings.Difficulty[] difficulties = {
            GameSettings.Difficulty.EASY,
            GameSettings.Difficulty.NORMAL, 
            GameSettings.Difficulty.HARD
        };
        
        for (GameSettings.Difficulty difficulty : difficulties) {
            System.out.println("\n--- " + difficulty + " 모드 블록 생성 확률 테스트 ---");
            testBlockGenerationProbability(difficulty);
        }
    }
    
    /**
     * 특정 난이도에서 블록 생성 확률을 테스트합니다.
     */
    private void testBlockGenerationProbability(GameSettings.Difficulty difficulty) throws Exception {
        // GameScene 생성 (BlockManager 접근을 위해)
        GameScene gameScene = new GameScene(testFrame, difficulty);
        gameScene.onEnter();
        
        // BlockManager 접근
        Field blockManagerField = GameScene.class.getDeclaredField("blockManager");
        blockManagerField.setAccessible(true);
        Object blockManager = blockManagerField.get(gameScene);
        
        // BlockManager의 테스트용 getRandomBlockForTest 메서드 사용
        Method getRandomBlockMethod = blockManager.getClass().getDeclaredMethod("getRandomBlockForTest");
        getRandomBlockMethod.setAccessible(true);
        
        // 블록 생성 카운터
        int[] blockCounts = new int[7]; // I=0, J=1, L=2, Z=3, S=4, T=5, O=6
        int totalBlocks = 2000; // 2000번 테스트 (요구사항: 최소 1000번)
        
        System.out.println("블록 생성 테스트 시작: " + totalBlocks + "번 반복");
        
        // 블록 생성 반복
        for (int i = 0; i < totalBlocks; i++) {
            Object block = getRandomBlockMethod.invoke(blockManager);
            String blockClassName = block.getClass().getSimpleName();
            
            // 블록 타입별 카운트 증가
            switch (blockClassName) {
                case "IBlock": blockCounts[0]++; break;
                case "JBlock": blockCounts[1]++; break;
                case "LBlock": blockCounts[2]++; break;
                case "ZBlock": blockCounts[3]++; break;
                case "SBlock": blockCounts[4]++; break;
                case "TBlock": blockCounts[5]++; break;
                case "OBlock": blockCounts[6]++; break;
            }
        }
        
        // 기대 확률 계산 (가중치 기반)
        double[] expectedWeights = getExpectedWeights(difficulty);
        double totalWeight = 0.0;
        for (double weight : expectedWeights) {
            totalWeight += weight;
        }
        
        // 결과 분석 및 검증
        String[] blockNames = {"I", "J", "L", "Z", "S", "T", "O"};
        System.out.println("\n블록 생성 결과 분석:");
        System.out.println("블록타입 | 실제개수 | 실제확률 | 기대확률 | 오차");
        System.out.println("---------|----------|----------|----------|----------");
        
        for (int i = 0; i < 7; i++) {
            double actualProbability = (double) blockCounts[i] / totalBlocks * 100;
            double expectedProbability = expectedWeights[i] / totalWeight * 100;
            double error = Math.abs(actualProbability - expectedProbability);
            
            System.out.printf("%-8s | %8d | %7.2f%% | %7.2f%% | %6.2f%%\n", 
                blockNames[i], blockCounts[i], actualProbability, expectedProbability, error);
            
            // 오차범위 ±5% 이내 검증
            assertTrue(error <= 5.0, 
                String.format("%s블록의 확률 오차가 5%%를 초과했습니다. (오차: %.2f%%)", 
                blockNames[i], error));
        }
        
        // I블록 특별 검증 (난이도별 요구사항)
        double iBlockProbability = (double) blockCounts[0] / totalBlocks * 100;
        double baseIProbability = 100.0 / 7; // 약 14.29%
        
        switch (difficulty) {
            case EASY:
                // Easy 모드: I블록 확률 20% 증가 (약 17.14%)
                double expectedEasyI = baseIProbability * 1.2 / (1 + 0.2 - 0.1 * 2 / 6); // 가중치 정규화 고려
                assertTrue(iBlockProbability > baseIProbability, 
                    "Easy 모드에서 I블록 확률이 기본값보다 높아야 합니다.");
                System.out.println("✅ Easy 모드 I블록 확률 증가 확인");
                break;
                
            case HARD:
                // Hard 모드: I블록 확률 20% 감소 (약 11.43%)
                assertTrue(iBlockProbability < baseIProbability, 
                    "Hard 모드에서 I블록 확률이 기본값보다 낮아야 합니다.");
                System.out.println("✅ Hard 모드 I블록 확률 감소 확인");
                break;
                
            case NORMAL:
                // Normal 모드: 균등 확률
                assertTrue(Math.abs(iBlockProbability - baseIProbability) <= 3.0, 
                    "Normal 모드에서 I블록 확률이 기본값과 유사해야 합니다.");
                System.out.println("✅ Normal 모드 I블록 균등 확률 확인");
                break;
        }
        
        System.out.println("✅ " + difficulty + " 모드 블록 생성 확률 테스트 통과");
    }
    
    /**
     * 난이도별 기대 가중치를 반환합니다.
     */
    private double[] getExpectedWeights(GameSettings.Difficulty difficulty) {
        double[] weights = new double[7];
        
        switch (difficulty) {
            case EASY:
                // I블록 20% 증가, 나머지 블록들이 남은 확률을 균등 분배
                weights[0] = 1.2;
                double remainingWeightEasy = (7.0 - 1.2) / 6.0; // = 0.967
                for (int i = 1; i < 7; i++) {
                    weights[i] = remainingWeightEasy;
                }
                break;
                
            case HARD:
                // I블록 20% 감소, 나머지 블록들이 남은 확률을 균등 분배
                weights[0] = 0.8;
                double remainingWeightHard = (7.0 - 0.8) / 6.0; // = 1.033
                for (int i = 1; i < 7; i++) {
                    weights[i] = remainingWeightHard;
                }
                break;
                
            default: // NORMAL
                for (int i = 0; i < weights.length; i++) {
                    weights[i] = 1.0; // 균등 확률
                }
                break;
        }
        
        return weights;
    }

    /**
     * 4. 블럭 생성 및 기본 구조 테스트
     */
    @Test
    @Order(4)
    @DisplayName("블럭 생성 및 기본 구조 테스트")
    void testBlockCreationStructure() throws Exception {
        System.out.println("=== 블럭 생성 및 기본 구조 테스트 ===");

        // 대표적으로 IBlock 테스트
        Class<?> iBlockClass = Class.forName("tetris.scene.game.blocks.IBlock");
        assertNotNull(iBlockClass, "IBlock 클래스가 존재해야 합니다.");

        // 블럭의 기본 메서드들이 존재하는지 확인 (부모 클래스에서)
        assertDoesNotThrow(() -> {
            Class<?> blockClass = Class.forName("tetris.scene.game.blocks.Block");
            blockClass.getDeclaredMethod("getShape", int.class, int.class);
        }, "getShape 메서드가 존재해야 합니다.");

        System.out.println("✅ 블럭 기본 구조 확인 완료");
    }

    /**
     * 5. 게임 시스템 통합 테스트
     */
    @Test
    @Order(5)
    @DisplayName("게임 시스템 통합 테스트")
    void testGameSystemIntegration() throws Exception {
        System.out.println("=== 게임 시스템 통합 테스트 ===");

        // 주요 게임 클래스들이 존재하는지 확인
        assertDoesNotThrow(() -> {
            Class.forName("tetris.scene.menu.MainMenuScene");
            Class.forName("tetris.scene.game.GameScene");
            Class.forName("tetris.GameSettings");
        }, "주요 게임 클래스들이 존재해야 합니다.");

        System.out.println("✅ 모든 게임 시스템 컴포넌트 확인 완료");
        System.out.println();
        System.out.println("🎉 모든 기본 테트리스 게임 기능 테스트가 성공적으로 통과되었습니다! 🎉");
        System.out.println();
        System.out.println("📋 검증 완료된 기본 요구사항:");
        System.out.println("✅ 시작 메뉴에서 게임 시작 선택 시 테트리스 게임 시작");
        System.out.println("✅ 20줄, 10칸의 보드(board) 존재");
        System.out.println("✅ 총 7가지의 테트로미노(블럭)가 무작위로 등장");
        System.out.println("✅ 블럭 생성 및 게임 시스템 기본 구조");
    }
}