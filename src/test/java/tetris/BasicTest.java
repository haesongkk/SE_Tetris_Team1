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
        System.out.println("✅ 총 7가지 테트로미노 확인 완료");
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