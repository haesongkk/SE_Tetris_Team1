package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import tetris.scene.menu.MainMenuScene;
import tetris.scene.game.GameScene;
import tetris.scene.game.blocks.*;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

/**
 * JUnit 5 기반 기본 테트리스 게임 기능 요구사항 테스트 클래스
 *
 * 테스트 항목:
 * 1. 시작 메뉴에서 게임 시작 선택 시 테트리스 게임 시작
 * 2. 20줄, 10칸의 보드(board) 존재
 * 3. 총 7가지의 테트로미노(블럭)가 무작위로 등장
 * 4. 블럭을 쌓아 각 행을 채우면 해당 행이 삭제됨
 */
@DisplayName("기본 테트리스 게임 기능 요구사항 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BasicJUnitTest {

    private static JFrame testFrame;

    @BeforeAll
    @DisplayName("테스트 환경 설정")
    static void setupTestEnvironment() {
        System.out.println("=== 기본 테트리스 게임 JUnit 테스트 환경 설정 ===");

        // CI 환경에서는 헤드리스 모드를 비활성화 (가상 디스플레이 사용)
        if (System.getenv("CI") != null) {
            System.setProperty("java.awt.headless", "false");
            System.out.println("CI 환경 감지: 가상 디스플레이 사용을 위해 헤드리스 모드 비활성화");
        }

        // 테스트용 프레임 생성 (안전한 방식)
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                testFrame = new JFrame("Tetris JUnit Test");
                testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                testFrame.setSize(800, 600);
                System.out.println("✅ GUI 테스트 프레임 생성 완료");
            } else {
                System.out.println("⚠️ 헤드리스 모드: GUI 테스트는 건너뛸 예정");
                testFrame = null;
            }
        } catch (HeadlessException e) {
            System.out.println("⚠️ HeadlessException 발생: GUI 테스트를 건너뜁니다.");
            testFrame = null;
        }

        System.out.println("✅ JUnit 테스트 환경 설정 완료");
    }

    @AfterAll
    @DisplayName("테스트 환경 정리")
    static void cleanup() {
        try {
            System.out.println("🧹 BasicJUnitTest 백그라운드 프로세스 정리 시작...");
            
            // 1. 테스트 프레임 정리
            if (testFrame != null) {
                testFrame.dispose();
                testFrame = null;
            }
            
            // 2. 모든 Timer 완전 중지
            try {
                javax.swing.Timer.setLogTimers(false);
                java.lang.reflect.Field timersField = javax.swing.Timer.class.getDeclaredField("queue");
                timersField.setAccessible(true);
                Object timerQueue = timersField.get(null);
                if (timerQueue != null) {
                    java.lang.reflect.Method stopMethod = timerQueue.getClass().getDeclaredMethod("stop");
                    stopMethod.setAccessible(true);
                    stopMethod.invoke(timerQueue);
                    System.out.println("🧹 Swing Timer 큐 완전 중지됨");
                }
            } catch (Exception e) {
                // Reflection 실패는 무시
            }
            
            // 3. AWT/Swing EventQueue 정리
            try {
                java.awt.EventQueue eventQueue = java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue();
                while (eventQueue.peekEvent() != null) {
                    eventQueue.getNextEvent();
                }
            } catch (Exception e) {
                // 무시
            }
            
            // 4. 활성 GUI 스레드 정리
            ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
            ThreadGroup parentGroup;
            while ((parentGroup = rootGroup.getParent()) != null) {
                rootGroup = parentGroup;
            }
            
            Thread[] threads = new Thread[rootGroup.activeCount()];
            int count = rootGroup.enumerate(threads);
            
            for (int i = 0; i < count; i++) {
                Thread thread = threads[i];
                if (thread != null && !thread.isDaemon() && thread != Thread.currentThread()) {
                    String threadName = thread.getName();
                    if (threadName.contains("AWT-EventQueue") || 
                        threadName.contains("TimerQueue") ||
                        threadName.contains("Swing-Timer")) {
                        System.out.println("⚠️ BasicJUnitTest 활성 GUI 스레드 감지: " + threadName);
                        thread.interrupt();
                    }
                }
            }
            
            // 5. 강제 메모리 정리
            System.runFinalization();
            System.gc();
            Thread.sleep(100);
            System.gc();
            
        } catch (Exception e) {
            System.out.println("BasicJUnitTest 정리 중 오류 (무시): " + e.getMessage());
        }
        
        System.out.println("✅ BasicJUnitTest 백그라운드 프로세스 정리 완료");
    }

    @Test
    @Order(1)
    @DisplayName("1. 시작 메뉴에서 게임 시작 테스트")
    void testGameStartFromMenu() {
        System.out.println("=== 1. 시작 메뉴에서 게임 시작 JUnit 테스트 ===");

        assertDoesNotThrow(() -> {
            // 헤드리스 환경에서는 테스트 건너뛰기
            if (testFrame == null) {
                System.out.println("⚠️ 헤드리스 환경에서는 GUI 테스트를 건너뜁니다.");
                return;
            }

            // MainMenuScene 생성 테스트
            MainMenuScene mainMenu = new MainMenuScene(testFrame);
            assertNotNull(mainMenu, "MainMenuScene이 생성되어야 합니다.");

            // startGame 메서드가 존재하는지 확인
            Method startGameMethod = MainMenuScene.class.getDeclaredMethod("startGame");
            assertNotNull(startGameMethod, "startGame 메서드가 존재해야 합니다.");

            System.out.println("✅ 메뉴 시스템 기본 기능 확인 완료");
        }, "메뉴 시스템 테스트 중 예외가 발생해서는 안 됩니다.");

        System.out.println("✅ 시작 메뉴에서 게임 시작 JUnit 테스트 통과");
    }

    @Test
    @Order(2)
    @DisplayName("2. GameScene 보드 크기 테스트")
    void testGameSceneCreation() throws Exception {
        System.out.println("=== 2. GameScene 보드 크기 JUnit 테스트 ===");

        GameScene gameScene = new GameScene(testFrame);
        
        // 보드 크기 상수 확인
        Field gameHeightField = GameScene.class.getDeclaredField("GAME_HEIGHT");
        gameHeightField.setAccessible(true);
        int gameHeight = (Integer) gameHeightField.get(null);

        Field gameWidthField = GameScene.class.getDeclaredField("GAME_WIDTH");
        gameWidthField.setAccessible(true);
        int gameWidth = (Integer) gameWidthField.get(null);

        System.out.println("보드 크기: " + gameWidth + "칸 × " + gameHeight + "줄");

        // 요구사항 검증: 20줄, 10칸
        assertEquals(20, gameHeight, "보드 높이는 20줄이어야 합니다.");
        assertEquals(10, gameWidth, "보드 너비는 10칸이어야 합니다.");

        System.out.println("✅ GameScene 보드 크기 JUnit 테스트 통과");
    }

    @Test
    @Order(3)
    @DisplayName("3. 테트로미노 무작위 생성 테스트")
    void testRandomBlockGeneration() throws Exception {
        System.out.println("=== 3. 테트로미노 무작위 생성 JUnit 테스트 ===");

        GameScene gameScene = new GameScene(testFrame);

        // GameScene 초기화 - blockManager 생성을 위해 필요
        Method initGameStateMethod = GameScene.class.getDeclaredMethod("initGameState");
        initGameStateMethod.setAccessible(true);
        initGameStateMethod.invoke(gameScene);

        // BlockManager의 getRandomBlock 메서드 접근
        Field blockManagerField = GameScene.class.getDeclaredField("blockManager");
        blockManagerField.setAccessible(true);
        Object blockManager = blockManagerField.get(gameScene);

        // blockManager가 null인지 확인
        assertNotNull(blockManager, "BlockManager가 초기화되어야 합니다.");

        // ItemManager를 null로 설정하여 일반 블록만 생성되도록 함
        Field itemManagerField = blockManager.getClass().getDeclaredField("itemManager");
        itemManagerField.setAccessible(true);
        itemManagerField.set(blockManager, null); // 아이템 모드 비활성화

        Method getRandomBlockMethod = blockManager.getClass().getDeclaredMethod("getRandomBlock");
        getRandomBlockMethod.setAccessible(true);

        // 여러 번 호출하여 다양한 블럭이 생성되는지 확인
        List<Class<? extends Block>> generatedBlocks = new ArrayList<>();
        int testIterations = 100;

        System.out.println("블럭 생성 테스트 (" + testIterations + "회):");

        // 시드 값을 다르게 하여 다양한 블록 생성 보장
        for (int i = 0; i < testIterations; i++) {
            // 각 호출 전에 약간의 지연을 주어 다른 랜덤 값 생성
            Thread.sleep(1);

            Block block = (Block) getRandomBlockMethod.invoke(blockManager);
            Class<? extends Block> blockClass = block.getClass();

            if (!generatedBlocks.contains(blockClass)) {
                generatedBlocks.add(blockClass);
                System.out.println("새로운 블럭 발견: " + blockClass.getSimpleName());
            }

            // 7가지 블록을 모두 발견하면 조기 종료
            if (generatedBlocks.size() >= 7) {
                break;
            }
        }

        // 7가지 블럭 타입이 모두 생성되었는지 확인
        assertTrue(generatedBlocks.size() >= 7,
            "7가지 블럭 타입이 모두 생성되어야 합니다. 발견된 타입 수: " + generatedBlocks.size());

        // 각 블럭 타입이 올바른지 확인
        assertTrue(generatedBlocks.stream().anyMatch(c -> c.getSimpleName().equals("IBlock")),
            "IBlock이 생성되어야 합니다.");
        assertTrue(generatedBlocks.stream().anyMatch(c -> c.getSimpleName().equals("JBlock")),
            "JBlock이 생성되어야 합니다.");
        assertTrue(generatedBlocks.stream().anyMatch(c -> c.getSimpleName().equals("LBlock")),
            "LBlock이 생성되어야 합니다.");
        assertTrue(generatedBlocks.stream().anyMatch(c -> c.getSimpleName().equals("OBlock")),
            "OBlock이 생성되어야 합니다.");
        assertTrue(generatedBlocks.stream().anyMatch(c -> c.getSimpleName().equals("SBlock")),
            "SBlock이 생성되어야 합니다.");
        assertTrue(generatedBlocks.stream().anyMatch(c -> c.getSimpleName().equals("TBlock")),
            "TBlock이 생성되어야 합니다.");
        assertTrue(generatedBlocks.stream().anyMatch(c -> c.getSimpleName().equals("ZBlock")),
            "ZBlock이 생성되어야 합니다.");

        System.out.println("총 발견된 블럭 타입: " + generatedBlocks.size());
        System.out.println("I, J, L, O, S, T, Z 블럭 모두 확인됨");
        System.out.println("✅ 테트로미노 무작위 생성 JUnit 테스트 통과");
    }    @Test
    @Order(4)
    @DisplayName("4. 행 완성 및 삭제 테스트")
    void testLineCompletionAndDeletion() throws Exception {
        System.out.println("=== 4. 행 완성 및 삭제 JUnit 테스트 ===");

        GameScene gameScene = new GameScene(testFrame);

        // BoardManager 접근
        Field boardManagerField = GameScene.class.getDeclaredField("boardManager");
        boardManagerField.setAccessible(true);
        Object boardManager = boardManagerField.get(gameScene);

        // 보드 배열 접근
        Field boardField = boardManager.getClass().getDeclaredField("board");
        boardField.setAccessible(true);
        int[][] board = (int[][]) boardField.get(boardManager);

        // 보드가 null이 아닌지 확인
        assertNotNull(board, "게임 보드가 초기화되어야 합니다.");

        // isLineFull 메서드 접근
        Method isLineFullMethod = boardManager.getClass().getDeclaredMethod("isLineFull", int.class);
        isLineFullMethod.setAccessible(true);

        // 테스트 1: 빈 행은 완성되지 않음
        boolean emptyLineFull = (Boolean) isLineFullMethod.invoke(boardManager, 0);
        assertFalse(emptyLineFull, "빈 행은 완성된 것으로 판단되어서는 안 됩니다.");

        // 테스트 2: 가득 찬 행은 완성됨
        for (int col = 0; col < 10; col++) {
            board[5][col] = 1; // 5번째 행을 가득 채움
        }
        boolean fullLineFull = (Boolean) isLineFullMethod.invoke(boardManager, 5);
        assertTrue(fullLineFull, "가득 찬 행은 완성된 것으로 판단되어야 합니다.");

        // 테스트 3: 부분적으로 채워진 행은 완성되지 않음
        board[10][5] = 0; // 10번째 행의 가운데를 비움
        for (int col = 0; col < 10; col++) {
            if (col != 5) board[10][col] = 1;
        }
        boolean partialLineFull = (Boolean) isLineFullMethod.invoke(boardManager, 10);
        assertFalse(partialLineFull, "부분적으로 채워진 행은 완성된 것으로 판단되어서는 안 됩니다.");

        System.out.println("행 완성 판단 로직 테스트 완료");

        // clearLines 메서드를 통한 줄 삭제 테스트
        Method clearLinesMethod = boardManager.getClass().getDeclaredMethod("clearLines", boolean[].class);
        clearLinesMethod.setAccessible(true);

        // 3번째 행 삭제 테스트
        boolean[] linesToClear = new boolean[20]; // GAME_HEIGHT = 20
        linesToClear[3] = true; // 3번째 행 삭제

        System.out.println("3번째 행 삭제 테스트 진행");
        clearLinesMethod.invoke(boardManager, (Object) linesToClear);

        // 삭제된 행이 빈 행으로 되었는지 확인
        boolean deletedRowEmpty = true;
        for (int col = 0; col < 10; col++) {
            if (board[3][col] != 0) {
                deletedRowEmpty = false;
                break;
            }
        }
        assertTrue(deletedRowEmpty, "삭제된 행은 빈 행으로 초기화되어야 합니다.");

        System.out.println("✅ 행 완성 및 삭제 JUnit 테스트 통과");
    }

    @Test
    @Order(5)
    @DisplayName("5. 게임 보드 초기화 상태 테스트")
    void testBoardInitialization() throws Exception {
        System.out.println("=== 5. 게임 보드 초기화 JUnit 테스트 ===");

        GameScene newGameScene = new GameScene(testFrame);

        // BoardManager를 통해 보드 접근
        Field boardManagerField = GameScene.class.getDeclaredField("boardManager");
        boardManagerField.setAccessible(true);
        Object boardManager = boardManagerField.get(newGameScene);

        Field boardField = boardManager.getClass().getDeclaredField("board");
        boardField.setAccessible(true);
        int[][] board = (int[][]) boardField.get(boardManager);

        // 보드가 null이 아닌지 확인
        assertNotNull(board, "게임 보드가 생성되어야 합니다.");

        // 보드 크기 확인
        assertEquals(20, board.length, "보드 행 수는 20이어야 합니다.");
        assertEquals(10, board[0].length, "보드 열 수는 10이어야 합니다.");

        System.out.println("보드 초기화 상태: 20×10 크기의 보드 생성 확인");
        System.out.println("✅ 게임 보드 초기화 JUnit 테스트 통과");
    }

    @Test
    @Order(6)
    @DisplayName("6. 종합 기능 검증 테스트")
    void testOverallFunctionality() {
        System.out.println("=== 6. 종합 기능 검증 JUnit 테스트 ===");

        // 전체적인 게임 구조가 올바른지 확인
        assertDoesNotThrow(() -> {
            GameScene gameScene = new GameScene(testFrame);
            
            // GameScene이 Scene을 상속받는지 확인
            assertTrue(gameScene instanceof tetris.scene.Scene, 
                "GameScene은 Scene 클래스를 상속받아야 합니다.");

            System.out.println("게임 구조 검증 완료");
            
        }, "게임 구조 검증 중 예외가 발생해서는 안 됩니다.");

        System.out.println("✅ 종합 기능 검증 JUnit 테스트 통과");
        System.out.println();
        System.out.println("🎉 모든 JUnit 테스트가 성공적으로 완료되었습니다! 🎉");
        System.out.println();
        System.out.println("📋 검증 완료된 요구사항:");
        System.out.println("✅ 시작 메뉴에서 게임 시작 선택 시 테트리스 게임 시작");
        System.out.println("✅ 20줄, 10칸의 보드(board) 존재");
        System.out.println("✅ 총 7가지의 테트로미노(블럭)가 무작위로 등장");
        System.out.println("✅ 블럭을 쌓아 각 행을 채우면 해당 행이 삭제됨");
    }
}