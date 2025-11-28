package tetris.scene.battle;

import tetris.Game;
import tetris.scene.Scene;
import tetris.scene.menu.MainMenuScene;
import tetris.scene.game.core.BoardManager;
import tetris.scene.game.core.BlockManager;
import tetris.scene.game.core.ScoreManager;
import tetris.scene.game.core.RenderManager;
import tetris.scene.game.core.GameStateManager;
import tetris.scene.game.core.InputHandler;
import tetris.scene.game.core.ItemManager;
import tetris.scene.game.blocks.Block;
import tetris.util.LineBlinkEffect;
import tetris.GameSettings;
import javax.swing.*;
import java.awt.*;
import java.util.Stack;

/**
 * Local Battle scene - GameScene × 2
 * 각 플레이어가 독립적인 GameScene 로직을 가짐
 */
public class BattleScene extends Scene {
    private static final int GAME_HEIGHT = 20;
    private static final int GAME_WIDTH = 10;
    private static final int PREVIEW_SIZE = 4;
    
    protected final JFrame m_frame;
    
    // 선택된 게임 모드
    protected final String gameMode;
    
    // ═══════════════════════════════════════════════════════════════
    // 1P (왼쪽) - 완전한 GameScene 복제
    // ═══════════════════════════════════════════════════════════════
    public final BoardManager boardManager1;
    protected final BlockManager blockManager1;
    protected final ScoreManager scoreManager1;
    protected final tetris.scene.game.core.UIManager uiManager1;
    protected RenderManager renderManager1;
    protected InputHandler inputHandler1;
    protected GameStateManager gameStateManager1;
    protected final LineBlinkEffect lineBlinkEffect1;
    protected ItemManager itemManager1; // 아이템 모드를 위한 ItemManager
    
    // ═══════════════════════════════════════════════════════════════
    // 2P (오른쪽) - 완전한 GameScene 복제
    // ═══════════════════════════════════════════════════════════════
    public final BoardManager boardManager2;
    protected final BlockManager blockManager2;
    protected final ScoreManager scoreManager2;
    protected final tetris.scene.game.core.UIManager uiManager2;
    protected RenderManager renderManager2;
    protected InputHandler inputHandler2;
    protected GameStateManager gameStateManager2;
    protected final LineBlinkEffect lineBlinkEffect2;
    protected ItemManager itemManager2; // 아이템 모드를 위한 ItemManager
    
    // 타이머 (블록 자동 낙하)
    private Timer fallTimer1;
    private Timer fallTimer2;
    
    // 점멸 효과 전용 타이머 (GameScene의 blinkTimer와 동일)
    private Timer blinkTimer;
    private static final int BLINK_INTERVAL_MS = 50; // 점멸 효과 업데이트 주기 (밀리초)
    
    // 시간제한 모드용 타이머 (3분)
    private Timer timeLimitTimer;
    private int remainingTimeSeconds = 180; // 3분 = 180초
    private static final int TIME_LIMIT_SECONDS = 180; // 3분
    
    // 공격 대기 블록 수 (상대가 삭제한 줄 수)
    // 공격 블록 스택 (LIFO - 최근 생성된 것이 먼저 적용)
    protected Stack<AttackBlock> attackQueue1 = new Stack<>(); // 1P가 받을 공격
    protected Stack<AttackBlock> attackQueue2 = new Stack<>(); // 2P가 받을 공격
    
    // 게임 오버 상태 (어느 한쪽이라도 게임 오버되면 양쪽 모두 종료)
    protected boolean isGameOver = false;
    
    // 아이템으로 인한 줄 삭제 추적
    private boolean isItemLineClear1 = false; // Player 1의 아이템으로 인한 줄 삭제인지 추적
    private boolean isItemLineClear2 = false; // Player 2의 아이템으로 인한 줄 삭제인지 추적
    
    // 시야 차단 관련 상태
    private boolean visionBlockActive1 = false;
    private boolean visionBlockActive2 = false;
    
    // 청소 블링킹 효과 (각 플레이어별로 개별 관리)
    private java.util.Set<java.awt.Point> cleanupBlinkingCells1 = new java.util.HashSet<>(); // Player 1
    private java.util.Set<java.awt.Point> cleanupBlinkingCells2 = new java.util.HashSet<>(); // Player 2
    private boolean cleanupBlinkingActive1 = false; // Player 1 청소 블링킹 활성 상태
    private boolean cleanupBlinkingActive2 = false; // Player 2 청소 블링킹 활성 상태

    public BattleScene(JFrame frame, String gameMode) {
        super(frame);
        this.m_frame = frame;
        this.gameMode = gameMode;
        
        GameSettings.Difficulty difficulty = GameSettings.getInstance().getDifficulty();
        
        // ═══════════════════════════════════════════════════════════════
        // 1P 초기화 (GameScene과 동일)
        // ═══════════════════════════════════════════════════════════════
        this.boardManager1 = new BoardManager();
        boardManager1.setPlayerNumber(1); // Player 1 설정
        this.scoreManager1 = new ScoreManager(difficulty);
        this.blockManager1 = new BlockManager(GAME_WIDTH, GAME_HEIGHT, boardManager1, scoreManager1, difficulty);
        this.uiManager1 = new tetris.scene.game.core.UIManager();
        this.gameStateManager1 = new GameStateManager(new Player1Callback());
        this.inputHandler1 = new InputHandler(frame, new Player1Callback(), 1); // 1P 키 설정 사용
        this.lineBlinkEffect1 = new LineBlinkEffect(new BlinkCallback1());
        this.itemManager1 = isItemMode(gameMode) ? new ItemManager() : null; // 아이템 모드일 때만 생성
        
        boardManager1.reset();
        blockManager1.initializeBlocks();
        
        // 아이템 모드일 때 ItemManager를 BlockManager와 BoardManager에 설정
        if (itemManager1 != null) {
            blockManager1.setItemManager(itemManager1);
            boardManager1.setItemManager(itemManager1);
            // BoardManager에 BlockManager와 GameScene 참조 설정 (아이템 효과용)
            boardManager1.setBlockManager(blockManager1);
            boardManager1.setGameScene(this);
            // BlockManager에도 GameScene 참조 설정
            blockManager1.setGameScene(this);
            System.out.println("Player 1: Item mode initialized with BlockManager and BoardManager!");
        }
        
        // ═══════════════════════════════════════════════════════════════
        // 2P 초기화 (GameScene과 동일)
        // ═══════════════════════════════════════════════════════════════
        this.boardManager2 = new BoardManager();
        boardManager2.setPlayerNumber(2); // Player 2 설정
        this.scoreManager2 = new ScoreManager(difficulty);
        this.blockManager2 = new BlockManager(GAME_WIDTH, GAME_HEIGHT, boardManager2, scoreManager2, difficulty);
        this.uiManager2 = new tetris.scene.game.core.UIManager();
        this.gameStateManager2 = new GameStateManager(new Player2Callback());
        this.inputHandler2 = new InputHandler(frame, new Player2Callback(), 2); // 2P 키 설정 사용
        this.lineBlinkEffect2 = new LineBlinkEffect(new BlinkCallback2());
        this.itemManager2 = isItemMode(gameMode) ? new ItemManager() : null; // 아이템 모드일 때만 생성
        
        boardManager2.reset();
        blockManager2.initializeBlocks();
        
        // 아이템 모드일 때 ItemManager를 BlockManager와 BoardManager에 설정
        if (itemManager2 != null) {
            blockManager2.setItemManager(itemManager2);
            boardManager2.setItemManager(itemManager2);
            // BoardManager에 BlockManager와 GameScene 참조 설정 (아이템 효과용)
            boardManager2.setBlockManager(blockManager2);
            boardManager2.setGameScene(this);
            // BlockManager에도 GameScene 참조 설정
            blockManager2.setGameScene(this);
            System.out.println("Player 2: Item mode initialized with BlockManager and BoardManager!");
        }
        
        setupLayout(frame);
        setupTimers();
    }
    
    /**
     * 게임 모드가 아이템 모드인지 확인하는 헬퍼 메서드
     */
    private boolean isItemMode(String gameMode) {
        if (gameMode == null) return false;
        return gameMode.equals("item") || gameMode.equals("아이템 모드") || 
               gameMode.toLowerCase().contains("item") || gameMode.contains("아이템");
    }

    // ═══════════════════════════════════════════════════════════════
    // 1P InputCallback 구현
    // ═══════════════════════════════════════════════════════════════
    protected class Player1Callback implements InputHandler.InputCallback, GameStateManager.StateChangeCallback {
        @Override
        public void onGameAction(InputHandler.GameAction action) {
            if (gameStateManager1.isGameOver()) return;
            
            switch (action) {
                case MOVE_LEFT:
                    moveBlockLeft(1);
                    break;
                case MOVE_RIGHT:
                    moveBlockRight(1);
                    break;
                case MOVE_DOWN:
                    moveBlockDown(1);
                    break;
                case ROTATE:
                    rotateBlock(1);
                    break;
                case HARD_DROP:
                    hardDrop(1);
                    break;
                case PAUSE:
                    gameStateManager1.togglePause();
                    break;
                case EXIT_TO_MENU:
                    BattleScene.this.exitToMenu();
                    break;
            }
        }
        
        @Override
        public boolean isGameOver() {
            return gameStateManager1.isGameOver();
        }
        
        @Override
        public boolean isPaused() {
            return gameStateManager1.isPaused();
        }
        
        @Override
        public void repaintGame() {
            repaint();
        }
        
        @Override
        public void onStateChanged(GameStateManager.GameState oldState, GameStateManager.GameState newState) {
            // 상태 변경 시 처리
        }
        
        @Override
        public void onPauseToggled(boolean isPaused) {
            // 일시정지 토글 시 시간제한 타이머도 함께 제어
            if ("time_limit".equals(gameMode) && timeLimitTimer != null) {
                if (isPaused) {
                    timeLimitTimer.stop();
                    System.out.println("Time limit timer paused");
                } else {
                    timeLimitTimer.start();
                    System.out.println("Time limit timer resumed");
                }
            }
            
            // 퍼즈 상태 변경 시 화면 다시 그리기
            System.out.println("🎨 Player 1 pause toggled - requesting repaint. isPaused: " + isPaused);
            repaint();
        }
        
        @Override
        public void onGameOver() {
            handleGameOver(1);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 2P InputCallback 구현
    // ═══════════════════════════════════════════════════════════════
    private class Player2Callback implements InputHandler.InputCallback, GameStateManager.StateChangeCallback {
        @Override
        public void onGameAction(InputHandler.GameAction action) {
            if (gameStateManager2.isGameOver()) return;
            
            switch (action) {
                case MOVE_LEFT:
                    moveBlockLeft(2);
                    break;
                case MOVE_RIGHT:
                    moveBlockRight(2);
                    break;
                case MOVE_DOWN:
                    moveBlockDown(2);
                    break;
                case ROTATE:
                    rotateBlock(2);
                    break;
                case HARD_DROP:
                    hardDrop(2);
                    break;
                case PAUSE:
                    gameStateManager2.togglePause();
                    break;
                case EXIT_TO_MENU:
                    BattleScene.this.exitToMenu();
                    break;
            }
        }
        
        @Override
        public boolean isGameOver() {
            return gameStateManager2.isGameOver();
        }
        
        @Override
        public boolean isPaused() {
            return gameStateManager2.isPaused();
        }
        
        @Override
        public void repaintGame() {
            repaint();
        }
        
        @Override
        public void onStateChanged(GameStateManager.GameState oldState, GameStateManager.GameState newState) {
            // 상태 변경 시 처리
        }
        
        @Override
        public void onPauseToggled(boolean isPaused) {
            // 일시정지 토글 시 시간제한 타이머도 함께 제어
            if ("time_limit".equals(gameMode) && timeLimitTimer != null) {
                if (isPaused) {
                    timeLimitTimer.stop();
                    System.out.println("Time limit timer paused");
                } else {
                    timeLimitTimer.start();
                    System.out.println("Time limit timer resumed");
                }
            }
            
            // 퍼즈 상태 변경 시 화면 다시 그리기
            System.out.println("🎨 Player 2 pause toggled - requesting repaint. isPaused: " + isPaused);
            repaint();
        }
        
        @Override
        public void onGameOver() {
            System.out.println("Player 2 Game Over detected in Callback");
            handleGameOver(2);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // LineBlinkEffect Callbacks
    // ═══════════════════════════════════════════════════════════════
    private class BlinkCallback1 implements LineBlinkEffect.BlinkEffectCallback {
        @Override
        public void onBlinkComplete() {
            // 점멸 완료 후 줄 삭제 실행
            BoardManager boardMgr = boardManager1;
            
            // 줄 삭제 전에 방해 블록을 위한 정보 수집
            java.util.List<Integer> fullLines = new java.util.ArrayList<>();
            for (int row = 0; row < GAME_HEIGHT; row++) {
                if (boardMgr.isLineFull(row)) {
                    fullLines.add(row);
                }
            }
            
            // 줄 삭제
            int[] lineResults = boardMgr.clearCompletedAndBombLinesSeparately();
            int completedLines = lineResults[0]; // 일반 완성된 줄 수
            int bombLines = lineResults[1]; // 아이템(폭탄)으로 삭제된 줄 수
            int totalClearedLines = completedLines + bombLines; // 총 삭제된 줄 수
            
            // 점수 추가 (총 삭제된 줄 수로 계산)
            if (totalClearedLines > 0) {
                scoreManager1.addScore(totalClearedLines);
                System.out.println("Player 1 cleared " + totalClearedLines + " lines! (" + completedLines + " completed + " + bombLines + " bomb lines)");
                
                // LINE_CLEAR 아이템으로 인한 줄 삭제인지 확인
                if (isItemLineClear1) {
                    System.out.println("Player 1: LINE_CLEAR item caused " + totalClearedLines + " lines to be cleared - NOT counting for item generation or attack blocks");
                    isItemLineClear1 = false; // 플래그 리셋
                } else {
                    // 아이템 모드일 때 ItemManager에 줄 삭제 알림 (총 삭제 줄 수)
                    if (itemManager1 != null) {
                        System.out.println("Player 1: Notifying ItemManager of " + totalClearedLines + " lines cleared (natural line clearing)");
                        itemManager1.onLinesCleared(totalClearedLines);
                        
                        // 아이템 블록 생성 조건 확인 - 다음 블록 생성 시 아이템 블록으로 변환하도록 플래그 설정
                        if (itemManager1.shouldCreateItemBlock()) {
                            System.out.println("Player 1: Item block will be created on next block generation!");
                        }
                    }
                    
                    // 일반 완성된 줄이 2줄 이상일 때만 상대방에게 공격 블록 생성 (자연스러운 줄 삭제만)
                    if (completedLines >= 2) {
                        generateAttackBlocks(fullLines, 2); // Player 2가 공격받음
                        System.out.println("Player 1: Generated attack blocks based on " + completedLines + " completed lines (bomb lines excluded)");
                    }
                }
            }
            
            // 다음 블록 생성
            if (!blockManager1.isGameOver()) {
                blockManager1.generateNextBlock();
                
                // 아이템 모드에서 아이템 블록 생성 조건 확인 후 다음 블록을 아이템 블록으로 변환
                if (itemManager1 != null && itemManager1.shouldCreateItemBlock()) {
                    Block nextBlock = blockManager1.getNextBlock();
                    if (nextBlock != null) {
                        Block itemBlock = itemManager1.createItemBlock(nextBlock);
                        if (itemBlock != null) {
                            // reflection을 사용하여 private nextBlock 필드에 접근
                            try {
                                java.lang.reflect.Field nextBlockField = blockManager1.getClass().getDeclaredField("nextBlock");
                                nextBlockField.setAccessible(true);
                                nextBlockField.set(blockManager1, itemBlock);
                                System.out.println("Player 1: Next block successfully converted to item block!");
                            } catch (Exception e) {
                                System.out.println("Player 1: Failed to set item block: " + e.getMessage());
                            }
                        }
                    }
                }
            }
            
            repaint();
        }
        
        @Override
        public void onEffectUpdate() {
            repaint();
        }
    }
    
    private class BlinkCallback2 implements LineBlinkEffect.BlinkEffectCallback {
        @Override
        public void onBlinkComplete() {
            // 점멸 완료 후 줄 삭제 실행
            BoardManager boardMgr = boardManager2;
            
            // 줄 삭제 전에 방해 블록을 위한 정보 수집
            java.util.List<Integer> fullLines = new java.util.ArrayList<>();
            for (int row = 0; row < GAME_HEIGHT; row++) {
                if (boardMgr.isLineFull(row)) {
                    fullLines.add(row);
                }
            }
            
            // 줄 삭제
            int[] lineResults = boardMgr.clearCompletedAndBombLinesSeparately();
            int completedLines = lineResults[0]; // 일반 완성된 줄 수
            int bombLines = lineResults[1]; // 아이템(폭탄)으로 삭제된 줄 수
            int totalClearedLines = completedLines + bombLines; // 총 삭제된 줄 수
            
            // 점수 추가 (총 삭제된 줄 수로 계산)
            if (totalClearedLines > 0) {
                scoreManager2.addScore(totalClearedLines);
                System.out.println("Player 2 cleared " + totalClearedLines + " lines! (" + completedLines + " completed + " + bombLines + " bomb lines)");
                
                // LINE_CLEAR 아이템으로 인한 줄 삭제인지 확인
                if (isItemLineClear2) {
                    System.out.println("Player 2: LINE_CLEAR item caused " + totalClearedLines + " lines to be cleared - NOT counting for item generation or attack blocks");
                    isItemLineClear2 = false; // 플래그 리셋
                } else {
                    // 아이템 모드일 때 ItemManager에 줄 삭제 알림 (총 삭제 줄 수)
                    if (itemManager2 != null) {
                        System.out.println("Player 2: Notifying ItemManager of " + totalClearedLines + " lines cleared (natural line clearing)");
                        itemManager2.onLinesCleared(totalClearedLines);
                        
                        // 아이템 블록 생성 조건 확인 - 다음 블록 생성 시 아이템 블록으로 변환하도록 플래그 설정
                        if (itemManager2.shouldCreateItemBlock()) {
                            System.out.println("Player 2: Item block will be created on next block generation!");
                        }
                    }
                    
                    // 일반 완성된 줄이 2줄 이상일 때만 상대방에게 공격 블록 생성 (자연스러운 줄 삭제만)
                    if (completedLines >= 2) {
                        generateAttackBlocks(fullLines, 1); // Player 1이 공격받음
                        System.out.println("Player 2: Generated attack blocks based on " + completedLines + " completed lines (bomb lines excluded)");
                    }
                }
            }
            
            // 다음 블록 생성
            if (!blockManager2.isGameOver()) {
                blockManager2.generateNextBlock();
                
                // 아이템 모드에서 아이템 블록 생성 조건 확인 후 다음 블록을 아이템 블록으로 변환
                if (itemManager2 != null && itemManager2.shouldCreateItemBlock()) {
                    Block nextBlock = blockManager2.getNextBlock();
                    if (nextBlock != null) {
                        Block itemBlock = itemManager2.createItemBlock(nextBlock);
                        if (itemBlock != null) {
                            // reflection을 사용하여 private nextBlock 필드에 접근
                            try {
                                java.lang.reflect.Field nextBlockField = blockManager2.getClass().getDeclaredField("nextBlock");
                                nextBlockField.setAccessible(true);
                                nextBlockField.set(blockManager2, itemBlock);
                                System.out.println("Player 2: Next block successfully converted to item block!");
                            } catch (Exception e) {
                                System.out.println("Player 2: Failed to set item block: " + e.getMessage());
                            }
                        }
                    }
                }
            }
            
            repaint();
        }
        
        @Override
        public void onEffectUpdate() {
            repaint();
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Attack Block Generation
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * 공격 블록을 생성하고 상대방 큐에 추가
     * @param clearedLines 삭제된 줄들의 행 번호
     * @param targetPlayer 공격받을 플레이어 (1 또는 2)
     */
    protected void generateAttackBlocks(java.util.List<Integer> clearedLines, int targetPlayer) {
        Stack<AttackBlock> targetStack = (targetPlayer == 1) ? attackQueue1 : attackQueue2;
        BoardManager targetBoardMgr = (targetPlayer == 1) ? boardManager1 : boardManager2;
        
        // 대상 플레이어의 현재 방해블럭 줄 수 확인
        int currentInterferenceLines = countInterferenceLines(targetBoardMgr);
        
        // 이미 10줄 이상이면 방해블럭 생성하지 않음
        if (currentInterferenceLines >= 10) {
            System.out.println("Player " + targetPlayer + " already has " + currentInterferenceLines + 
                             " interference lines (≥10). Skipping attack block generation.");
            return;
        }
        
        // 공격하는 플레이어의 BlockManager에서 마지막 배치된 블록 정보 가져오기
        BlockManager attackingBlockMgr = (targetPlayer == 1) ? blockManager2 : blockManager1; // 상대방이 공격하는 것
        
        Block lastPlacedBlock = attackingBlockMgr.getLastPlacedBlock();
        int lastPlacedX = attackingBlockMgr.getLastPlacedX();
        int lastPlacedY = attackingBlockMgr.getLastPlacedY();
        
        System.out.println("=== Attack Block Generation Debug ===");
        System.out.println("Last placed block: " + (lastPlacedBlock != null ? lastPlacedBlock.getClass().getSimpleName() : "null"));
        System.out.println("Last placed position: (" + lastPlacedX + ", " + lastPlacedY + ")");
        System.out.println("Cleared lines: " + clearedLines);
        if (lastPlacedBlock != null) {
            System.out.println("Block size: " + lastPlacedBlock.width() + "x" + lastPlacedBlock.height());
            for (int j = 0; j < lastPlacedBlock.height(); j++) {
                for (int i = 0; i < lastPlacedBlock.width(); i++) {
                    if (lastPlacedBlock.getShape(i, j) == 1) {
                        int blockBoardX = lastPlacedX + i;
                        int blockBoardY = lastPlacedY + j;
                        System.out.println("Block cell at board position: (" + blockBoardX + ", " + blockBoardY + ")");
                    }
                }
            }
        }
        
        for (int lineIndex : clearedLines) {
            // 각 삭제된 줄에 대해 AttackBlock 생성
            boolean[] pattern = new boolean[GAME_WIDTH];
            Color[] colors = new Color[GAME_WIDTH];
            int[] blockTypes = new int[GAME_WIDTH];
            
            System.out.println("Processing cleared line: " + lineIndex);
            
            // 해당 줄의 블록 정보를 복사하되, 마지막 배치된 블록 부분은 구멍으로 만들기
            for (int col = 0; col < GAME_WIDTH; col++) {
                boolean isLastPlacedBlockPosition = false;
                
                // 마지막 배치된 블록이 이 줄과 겹치는 부분인지 확인
                if (lastPlacedBlock != null && lastPlacedY != -1) {
                    for (int j = 0; j < lastPlacedBlock.height(); j++) {
                        for (int i = 0; i < lastPlacedBlock.width(); i++) {
                            if (lastPlacedBlock.getShape(i, j) == 1) {
                                int blockBoardX = lastPlacedX + i;
                                int blockBoardY = lastPlacedY + j;
                                
                                System.out.println("    Checking block cell (" + i + ", " + j + ") -> board (" + blockBoardX + ", " + blockBoardY + ") against line " + lineIndex + " col " + col);
                                
                                // 보드 범위 내에서만 검사하고, 이 줄(lineIndex)과 현재 열(col)이 마지막 배치된 블록 위치와 일치하는지 확인
                                if (blockBoardX >= 0 && blockBoardX < GAME_WIDTH && 
                                    blockBoardX == col && blockBoardY == lineIndex) {
                                    isLastPlacedBlockPosition = true;
                                    System.out.println("    MATCH! Block position found at col " + col + " line " + lineIndex);
                                    break;
                                }
                            }
                        }
                        if (isLastPlacedBlockPosition) break;
                    }
                }
                
                if (isLastPlacedBlockPosition) {
                    // 마지막 배치된 블록 부분은 구멍으로 만들기
                    pattern[col] = false;
                    colors[col] = Color.BLACK;
                    blockTypes[col] = 0;
                    System.out.println("  Hole at column " + col + " (last placed block position)");
                } else {
                    // 나머지 부분은 방해 블록으로 만들기
                    pattern[col] = true;
                    colors[col] = Color.GRAY;
                    blockTypes[col] = 8; // 방해블록 타입
                }
            }
            
            // 구멍이 하나도 없다면 안전장치로 랜덤하게 하나 만들기
            boolean hasHole = false;
            for (boolean p : pattern) {
                if (!p) {
                    hasHole = true;
                    break;
                }
            }
            
            if (!hasHole) {
                System.out.println("  No holes found, creating random hole as fallback");
                int randomCol = (int)(Math.random() * GAME_WIDTH);
                pattern[randomCol] = false;
                colors[randomCol] = Color.BLACK;
                blockTypes[randomCol] = 0;
                System.out.println("  Random hole created at column " + randomCol);
            }
            
            targetStack.push(new AttackBlock(GAME_WIDTH, pattern, colors, blockTypes));
        }
        
        System.out.println("Generated " + clearedLines.size() + " attack blocks for Player " + targetPlayer);
    }
    
    /**
     * 대기 중인 공격 블록을 게임 보드에 적용
     * @param player 공격받는 플레이어 (1 또는 2)
     */
    protected void applyAttackBlocks(int player) {
        Stack<AttackBlock> attackStack = (player == 1) ? attackQueue1 : attackQueue2;
        BoardManager boardMgr = (player == 1) ? boardManager1 : boardManager2;
        
        if (attackStack.isEmpty()) {
            return;
        }
        
        System.out.println("Applying " + attackStack.size() + " attack blocks to Player " + player);
        
        // 현재 보드에서 방해블럭이 있는 줄 수를 계산
        int currentInterferenceLines = countInterferenceLines(boardMgr);
        
        // 모든 대기 중인 공격 블록을 보드 하단에 추가하되, 최대 10줄까지만 허용
        int blocksToApply = 0;
        int stackSize = attackStack.size();
        
        // 10줄을 넘지 않도록 적용할 블록 수 계산
        int maxAllowedBlocks = Math.max(0, 10 - currentInterferenceLines);
        blocksToApply = Math.min(stackSize, maxAllowedBlocks);
        
        System.out.println("Current interference lines: " + currentInterferenceLines + 
                          ", Max allowed new blocks: " + maxAllowedBlocks + 
                          ", Will apply: " + blocksToApply + " out of " + stackSize);
        
        // 10줄을 초과하여 적용되지 못한 공격 블록들은 스택에서 제거 (최신 것부터 제거)
        if (stackSize > blocksToApply) {
            int blocksToDiscard = stackSize - blocksToApply;
            System.out.println("Discarding " + blocksToDiscard + " attack blocks due to 10-line limit");
            for (int i = 0; i < blocksToDiscard; i++) {
                attackStack.pop(); // 가장 최근 블록들 제거 (LIFO)
            }
        }
        
        // 남은 블록들을 적용 (이제 오래된 것부터 적용하기 위해 역순으로 처리)
        AttackBlock[] blocksToApplyArray = new AttackBlock[blocksToApply];
        for (int j = blocksToApply - 1; j >= 0; j--) {
            blocksToApplyArray[j] = attackStack.pop();
        }
        
        // 배열에 저장된 블록들을 순서대로 적용 (오래된 것부터)
        for (int j = 0; j < blocksToApply; j++) {
            AttackBlock attackBlock = blocksToApplyArray[j];
            
            // 기존 블록들을 위로 한 줄씩 이동
            int[][] board = boardMgr.getBoard();
            Color[][] boardColors = boardMgr.getBoardColors();
            int[][] boardTypes = boardMgr.getBoardTypes();
            boolean[][] itemCells = boardMgr.getItemCells();
            
            // 아이템 블록 정보는 개별적으로 이동해야 함
            for (int row = 0; row < GAME_HEIGHT - 1; row++) {
                for (int col = 0; col < GAME_WIDTH; col++) {
                    board[row][col] = board[row + 1][col];
                    boardColors[row][col] = boardColors[row + 1][col];
                    boardTypes[row][col] = boardTypes[row + 1][col];
                    itemCells[row][col] = itemCells[row + 1][col];
                    
                    // 아이템 블록 정보도 함께 이동
                    if (itemCells[row + 1][col]) {
                        tetris.scene.game.blocks.ItemBlock itemBlockInfo = boardMgr.getItemBlockInfo(col, row + 1);
                        if (itemBlockInfo != null) {
                            System.out.println("🔄 Moving item block info from (" + col + "," + (row + 1) + ") to (" + col + "," + row + ")");
                            boardMgr.setItemBlockInfo(col, row, itemBlockInfo);
                        }
                    } else {
                        boardMgr.setItemBlockInfo(col, row, null);
                    }
                }
            }
            
            // 맨 아래줄(GAME_HEIGHT - 1)에 공격 블록 배치
            for (int col = 0; col < GAME_WIDTH; col++) {
                if (attackBlock.hasBlockAt(col)) {
                    board[GAME_HEIGHT - 1][col] = 1;
                    boardColors[GAME_HEIGHT - 1][col] = attackBlock.getColors()[col];
                    boardTypes[GAME_HEIGHT - 1][col] = attackBlock.getBlockTypes()[col];
                } else {
                    board[GAME_HEIGHT - 1][col] = 0;
                    boardColors[GAME_HEIGHT - 1][col] = Color.BLACK;
                    boardTypes[GAME_HEIGHT - 1][col] = -1;
                }
                // 아래줄은 방해블록이므로 아이템 셀이 아님
                itemCells[GAME_HEIGHT - 1][col] = false;
                boardMgr.setItemBlockInfo(col, GAME_HEIGHT - 1, null);
            }
        }
    }
    
    /**
     * 보드에서 방해블럭이 있는 줄의 수를 계산
     * @param boardMgr 확인할 보드 매니저
     * @return 방해블럭이 있는 줄의 수
     */
    private int countInterferenceLines(BoardManager boardMgr) {
        int[][] board = boardMgr.getBoard();
        int[][] boardTypes = boardMgr.getBoardTypes();
        int interferenceLines = 0;
        
        for (int row = 0; row < GAME_HEIGHT; row++) {
            boolean hasInterference = false;
            for (int col = 0; col < GAME_WIDTH; col++) {
                // 방해블럭 타입(8)이 있는지 확인
                if (board[row][col] == 1 && boardTypes[row][col] == 8) {
                    hasInterference = true;
                    break;
                }
            }
            if (hasInterference) {
                interferenceLines++;
            }
        }
        
        return interferenceLines;
    }
    
    /**
     * 타이머 설정 (블록 자동 낙하)
     */
    private void setupTimers() {
        GameSettings.Difficulty difficulty = GameSettings.getInstance().getDifficulty();
        int delay = getInitialDelay(difficulty);
        
        // 1P 타이머
        fallTimer1 = new Timer(delay, e -> {
            if (!isGameOver && !gameStateManager1.isPaused()) {
                moveBlockDown(1);
                // 무게추 아이템 블록 업데이트 (아이템 모드일 때만)
                if ("item".equals(gameMode)) {
                    boolean shouldGenerateNext = blockManager1.updateWeightBlock();
                    if (shouldGenerateNext) {
                        // 무게추 블록이 사라졌으므로 다음 블록 생성
                        if (!blockManager1.isGameOver()) {
                            blockManager1.generateNextBlock();
                            System.out.println("Player 1 (fallTimer): Generated next block after WeightItemBlock disappeared");
                        }
                        return;
                    }
                }
            }
        });
        
        // 2P 타이머
        fallTimer2 = new Timer(delay, e -> {
            if (!isGameOver && !gameStateManager2.isPaused()) {
                moveBlockDown(2);
                // 무게추 아이템 블록 업데이트 (아이템 모드일 때만)
                if ("item".equals(gameMode)) {
                    boolean shouldGenerateNext = blockManager2.updateWeightBlock();
                    if (shouldGenerateNext) {
                        // 무게추 블록이 사라졌으므로 다음 블록 생성
                        if (!blockManager2.isGameOver()) {
                            blockManager2.generateNextBlock();
                            System.out.println("Player 2 (fallTimer): Generated next block after WeightItemBlock disappeared");
                        }
                        return;
                    }
                }
            }
        });
        
        // 점멸 효과 전용 타이머 (GameScene의 blinkTimer와 동일하게 50ms마다 실행)
        blinkTimer = new Timer(BLINK_INTERVAL_MS, e -> {
            if (!isGameOver) {
                boolean needsRepaint = false;
                
                // 일시정지되지 않은 플레이어만 점멸 효과 업데이트
                if (!gameStateManager1.isPaused()) {
                    lineBlinkEffect1.update();
                    // 아이템 모드에서 무게추 블록의 점멸 효과 및 소멸 처리
                    if ("item".equals(gameMode)) {
                        boolean shouldUpdateP1 = blockManager1.updateWeightBlock();
                        if (shouldUpdateP1) {
                            // 무게추 블록이 사라졌으므로 다음 블록 생성
                            if (!blockManager1.isGameOver()) {
                                blockManager1.generateNextBlock();
                                System.out.println("Player 1: Generated next block after WeightItemBlock disappeared");
                            }
                            needsRepaint = true;
                        }
                    }
                }
                if (!gameStateManager2.isPaused()) {
                    lineBlinkEffect2.update();
                    // 아이템 모드에서 무게추 블록의 점멸 효과 및 소멸 처리
                    if ("item".equals(gameMode)) {
                        boolean shouldUpdateP2 = blockManager2.updateWeightBlock();
                        if (shouldUpdateP2) {
                            // 무게추 블록이 사라졌으므로 다음 블록 생성
                            if (!blockManager2.isGameOver()) {
                                blockManager2.generateNextBlock();
                                System.out.println("Player 2: Generated next block after WeightItemBlock disappeared");
                            }
                            needsRepaint = true;
                        }
                    }
                }
                
                // 청소 블링킹이 활성화되어 있으면 항상 화면 갱신 (점멸 애니메이션을 위해)
                if (cleanupBlinkingActive1 || cleanupBlinkingActive2) {
                    needsRepaint = true;
                }
                
                if (needsRepaint) {
                    repaint();
                }
            }
        });
        
        // 시간제한 모드일 때만 시간 타이머 설정
        if ("time_limit".equals(gameMode)) {
            remainingTimeSeconds = TIME_LIMIT_SECONDS;
            timeLimitTimer = new Timer(1000, e -> { // 1초마다 실행
                remainingTimeSeconds--;
                repaint(); // UI 업데이트를 위해
                
                if (remainingTimeSeconds <= 0) {
                    // 시간 종료 - 점수 비교하여 승자 결정
                    timeLimitTimer.stop();
                    checkTimeLimitGameEnd();
                }
            });
            timeLimitTimer.start();
        }
    }
    
    /**
     * 난이도에 따른 초기 낙하 속도
     */
    private int getInitialDelay(GameSettings.Difficulty difficulty) {
        switch (difficulty) {
            case EASY: return 1000;
            case NORMAL: return 800;
            case HARD: return 600;
            default: return 800;
        }
    }
    
    /**
     * 블록을 왼쪽으로 이동 (GameScene의 moveBlockLeft와 동일)
     */
    private void moveBlockLeft(int player) {
        GameStateManager gameStateMgr = (player == 1) ? gameStateManager1 : gameStateManager2;
        if (isGameOver || gameStateMgr.isPaused()) return;
        
        LineBlinkEffect lineBlinkEffect = (player == 1) ? lineBlinkEffect1 : lineBlinkEffect2;
        if (lineBlinkEffect.isActive()) return; // 점멸 중에는 조작 불가
        
        BlockManager blockMgr = (player == 1) ? blockManager1 : blockManager2;
        blockMgr.moveLeft();
        repaint();
    }
    
    /**
     * 블록을 오른쪽으로 이동 (GameScene의 moveBlockRight와 동일)
     */
    private void moveBlockRight(int player) {
        GameStateManager gameStateMgr = (player == 1) ? gameStateManager1 : gameStateManager2;
        if (isGameOver || gameStateMgr.isPaused()) return;
        
        LineBlinkEffect lineBlinkEffect = (player == 1) ? lineBlinkEffect1 : lineBlinkEffect2;
        if (lineBlinkEffect.isActive()) return; // 점멸 중에는 조작 불가
        
        BlockManager blockMgr = (player == 1) ? blockManager1 : blockManager2;
        blockMgr.moveRight();
        repaint();
    }
    
    /**
     * 블록을 회전 (GameScene의 rotateBlock과 동일)
     */
    private void rotateBlock(int player) {
        GameStateManager gameStateMgr = (player == 1) ? gameStateManager1 : gameStateManager2;
        if (isGameOver || gameStateMgr.isPaused()) return;
        
        LineBlinkEffect lineBlinkEffect = (player == 1) ? lineBlinkEffect1 : lineBlinkEffect2;
        if (lineBlinkEffect.isActive()) return; // 점멸 중에는 조작 불가
        
        BlockManager blockMgr = (player == 1) ? blockManager1 : blockManager2;
        if (blockMgr.getCurrentBlock() != null) {
            blockMgr.rotateBlock();
            repaint();
        }
    }
    
    /**
     * 블록을 아래로 이동
     */
    private void moveBlockDown(int player) {
        GameStateManager gameStateMgr = (player == 1) ? gameStateManager1 : gameStateManager2;
        if (gameStateMgr.isPaused()) return;
        
        LineBlinkEffect lineBlinkEffect = (player == 1) ? lineBlinkEffect1 : lineBlinkEffect2;
        
        // 점멸 효과가 진행 중이면 블록 이동 중지
        if (lineBlinkEffect.isActive()) {
            return;
        }
        
        BlockManager blockMgr = (player == 1) ? blockManager1 : blockManager2;
        boolean blockPlaced = blockMgr.moveDown();
        
        if (blockPlaced) {
            handleBlockPlaced(player);
        }
        
        repaint();
    }
    
    /**
     * 완성된 줄을 찾아서 삭제 (점멸 효과 포함)
     */
    private void checkAndClearLines(int player, BoardManager boardMgr) {
        LineBlinkEffect lineBlinkEffect = (player == 1) ? lineBlinkEffect1 : lineBlinkEffect2;
        
        java.util.List<Integer> completedLines = new java.util.ArrayList<>();
        
        // 완성된 줄 찾기
        for (int row = 0; row < GAME_HEIGHT; row++) {
            if (boardMgr.isLineFull(row)) {
                completedLines.add(row);
            }
        }
        
        // 완성된 줄이 있으면 점멸 효과 시작
        if (!completedLines.isEmpty()) {
            System.out.println("Player " + player + " - Starting blink effect for lines: " + completedLines);
            lineBlinkEffect.startBlinkEffect(completedLines);
        } else {
            // 완성된 줄이 없으면 즉시 다음 블록 생성
            BlockManager blockMgr = (player == 1) ? blockManager1 : blockManager2;
            if (!blockMgr.isGameOver()) {
                // 무게추 아이템 블록이 사라진 경우도 여기서 처리됨
                blockMgr.generateNextBlock();
                System.out.println("Player " + player + ": Generated next block (no completed lines)");
            }
        }
        
        repaint();
    }
    
    /**
     * 하드드롭 실행
     */
    private void hardDrop(int player) {
        LineBlinkEffect lineBlinkEffect = (player == 1) ? lineBlinkEffect1 : lineBlinkEffect2;
        if (lineBlinkEffect.isActive()) return; // 점멸 중에는 조작 불가
        
        BlockManager blockMgr = (player == 1) ? blockManager1 : blockManager2;
        
        // BlockManager의 executeHardDrop 사용 (GameScene과 동일)
        boolean blockPlaced = blockMgr.executeHardDrop();
        
        if (blockPlaced) {
            handleBlockPlaced(player);
        }
        
        repaint();
    }
    
    /**
     * 블록이 배치된 후 후속 처리 (GameScene의 handleBlockPlaced와 동일)
     */
    private void handleBlockPlaced(int player) {
        BlockManager blockMgr = (player == 1) ? blockManager1 : blockManager2;
        BoardManager boardMgr = (player == 1) ? boardManager1 : boardManager2;
        GameStateManager gameStateMgr = (player == 1) ? gameStateManager1 : gameStateManager2;
        
        // 무게추 아이템 블록 처리 (아이템 모드일 때만)
        if ("item".equals(gameMode)) {
            Block currentBlock = blockMgr.getCurrentBlock();
            if (currentBlock instanceof tetris.scene.game.blocks.WeightItemBlock) {
                tetris.scene.game.blocks.WeightItemBlock weightBlock = (tetris.scene.game.blocks.WeightItemBlock) currentBlock;
                if (weightBlock.shouldDisappear()) {
                    System.out.println("Player " + player + ": WeightItemBlock should disappear, will generate next block after line check");
                    // 무게추 블록이 사라질 때는 바로 다음 블록을 생성하지 않고, 
                    // checkAndClearLines에서 처리되도록 함
                }
            }
        }
        
        // 대기 중인 공격 블록 적용
        applyAttackBlocks(player);
        
        // 게임 오버 체크
        if (blockMgr.isGameOver()) {
            gameStateMgr.triggerGameOver();
            handleGameOver(player);
            return;
        }
        
        // 완성된 줄 확인 및 삭제
        checkAndClearLines(player, boardMgr);
        
        // 보드 상태 출력 (디버그용)
        boardMgr.printBoard();
        
        repaint();
    }
    
    /**
     * 게임 오버 처리 (어느 한쪽이라도 게임 오버되면 양쪽 모두 종료)
     */
    protected void handleGameOver(int loser) {
        if (!isGameOver) {
            isGameOver = true;
            fallTimer1.stop();
            fallTimer2.stop();
            if (blinkTimer != null) blinkTimer.stop(); // 점멸 효과 타이머 정지
            if (timeLimitTimer != null) timeLimitTimer.stop(); // 시간제한 타이머 정지
            
            // 양쪽 모두 게임 오버 상태로 설정
            if (!gameStateManager1.isGameOver()) {
                gameStateManager1.triggerGameOver();
            }
            if (!gameStateManager2.isGameOver()) {
                gameStateManager2.triggerGameOver();
            }
            
            // 점수를 비교하여 실제 승자 결정
            int player1Score = scoreManager1.getScore();
            int player2Score = scoreManager2.getScore();
            int actualWinner;
            

            if (loser == 1){
                actualWinner = 2;
            }
            else if (loser == 2){
                actualWinner = 1;
            }
            else if (player1Score > player2Score) {
                actualWinner = 1;
            } 
            else if (player2Score > player1Score) {
                actualWinner = 2;
            }
            else {
                actualWinner = 0; // 무승부 처리 (필요시)
            }
            
            // 승자 표시 다이얼로그 실행
            showBattleGameOverDialog(actualWinner);
        }
    }
    
    /**
     * 시간제한 모드에서 시간 종료 시 점수 비교하여 승자 결정
     */
    private void checkTimeLimitGameEnd() {
        if (!isGameOver) {
            isGameOver = true;
            
            // 모든 타이머 정지
            fallTimer1.stop();
            fallTimer2.stop();
            if (blinkTimer != null) blinkTimer.stop();
            if (timeLimitTimer != null) timeLimitTimer.stop();
            
            // 양쪽 모두 게임 오버 상태로 설정
            if (!gameStateManager1.isGameOver()) {
                gameStateManager1.triggerGameOver();
            }
            if (!gameStateManager2.isGameOver()) {
                gameStateManager2.triggerGameOver();
            }
            
            // 점수 비교하여 승자 결정
            int score1 = scoreManager1.getScore();
            int score2 = scoreManager2.getScore();
            
            int winner;
            if (score1 > score2) {
                winner = 1; // Player 1 승리
            } else if (score2 > score1) {
                winner = 2; // Player 2 승리
            } else {
                winner = 0; // 무승부
            }
            
            System.out.println("시간 종료! 최종 점수 - Player 1: " + score1 + ", Player 2: " + score2);
            
            // 승자 대화상자 표시
            showBattleGameOverDialog(winner);
        }
    }
    
    /**
     * 배틀 게임 오버 다이얼로그를 표시합니다
     * @param winner 승리한 플레이어 (1 또는 2)
     */
    protected void showBattleGameOverDialog(int winner) {
        SwingUtilities.invokeLater(() -> {
            // 메인메뉴 스타일의 다이얼로그 생성
            JDialog dialog = createBaseDialog();
            JPanel dialogPanel = createDialogPanel();
            
            // 게임 모드에 따른 다이얼로그 구성
            if ("time_limit".equals(gameMode)) {
                // 시간제한 모드: 점수 표시 포함
                setupTimeLimitModeDialog(dialogPanel, winner);
            } else {
                // 일반 모드, 아이템 모드: 승자만 표시
                setupNormalModeDialog(dialogPanel, winner);
            }
            
            dialog.add(dialogPanel);
            dialog.setVisible(true);
            dialog.requestFocus();
        });
    }
    
    /**
     * 일반 모드와 아이템 모드용 다이얼로그 구성
     */
    private void setupNormalModeDialog(JPanel dialogPanel, int winner) {
        // 제목 라벨
        JLabel titleLabel = new JLabel("게임 종료", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        titleLabel.setForeground(tetris.util.Theme.MenuTitle());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // 중앙 패널 (승자 정보 + 설명)
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new GridLayout(3, 1, 0, 10));
        
        // 승자 표시
        JLabel winnerLabel = new JLabel();
        winnerLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        
        String winnerText;
        String modeDescription = "";
        
        if ("item".equals(gameMode)) {
            modeDescription = "아이템 모드(1줄 삭제시 아이템 등장): ";
        } else {
            modeDescription = "일반 모드: ";
        }
        
        if (winner == 1) {
            winnerText = modeDescription + "플레이어 1 승리!";
        } else if (winner == 2) {
            winnerText = modeDescription + "플레이어 2 승리!";
        } else {
            winnerText = modeDescription + "무승부!";
        }

        winnerLabel.setForeground(new Color(255, 215, 0)); // Gold color
        winnerLabel.setText(winnerText);
        winnerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 게임 종료 사유 표시
        JLabel reasonLabel = new JLabel("게임종료조건: 블록이 먼저 천장에 닿으면 패배");
        reasonLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        reasonLabel.setForeground(Color.WHITE);
        reasonLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        centerPanel.add(winnerLabel);
        centerPanel.add(reasonLabel);
        centerPanel.add(new JLabel()); // 빈 공간
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(1, 1, 0, 10));
        
        // 메인 메뉴로 돌아가기 버튼
        JButton mainMenuButton = createDialogButton("메인 메뉴로 돌아가기");
        mainMenuButton.addActionListener(e -> {
            ((JDialog)dialogPanel.getTopLevelAncestor()).dispose();
            returnToMainMenu();
        });
        
        buttonPanel.add(mainMenuButton);
        
        // 컴포넌트 배치
        dialogPanel.add(titleLabel, BorderLayout.NORTH);
        dialogPanel.add(centerPanel, BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 시간제한 모드용 다이얼로그 구성
     */
    private void setupTimeLimitModeDialog(JPanel dialogPanel, int winner) {
        // 제목 라벨
        JLabel titleLabel = new JLabel("게임 종료", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        titleLabel.setForeground(tetris.util.Theme.MenuTitle());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // 중앙 패널 (승자 정보 + 점수 + 설명)
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new GridLayout(4, 1, 0, 8));
        
        // 승자 표시
        JLabel winnerLabel = new JLabel();
        winnerLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        
        String winnerText = "시간제한 모드: ";
        if (winner == 1) {
            winnerText += "플레이어 1 승리!";
            winnerLabel.setForeground(new Color(255, 215, 0)); // Gold color
        } else if (winner == 2) {
            winnerText += "플레이어 2 승리!";
            winnerLabel.setForeground(new Color(255, 215, 0)); // Gold color
        } else {
            winnerText += "무승부!";
            winnerLabel.setForeground(new Color(192, 192, 192)); // Silver color
        }
        winnerLabel.setText(winnerText);
        winnerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 게임 종료 사유 표시
        JLabel reasonLabel = new JLabel("<html><center>게임종료조건: 블록이 먼저 천장에 닿으면 패배<br>또는 시간 종료 후 점수가 더 높은 쪽이 승리</center></html>");
        reasonLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 11));
        reasonLabel.setForeground(Color.WHITE);
        reasonLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 플레이어 점수 표시 패널
        JPanel scorePanel = new JPanel();
        scorePanel.setOpaque(false);
        scorePanel.setLayout(new GridLayout(1, 2, 20, 0));
        
        int player1Score = scoreManager1.getScore();
        int player2Score = scoreManager2.getScore();
        
        JLabel player1ScoreLabel = new JLabel("플레이어 1: " + String.format("%,d", player1Score));
        player1ScoreLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        player1ScoreLabel.setForeground(winner == 1 ? new Color(255, 215, 0) : (winner == 0 ? new Color(192, 192, 192) : Color.WHITE));
        player1ScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel player2ScoreLabel = new JLabel("플레이어 2: " + String.format("%,d", player2Score));
        player2ScoreLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        player2ScoreLabel.setForeground(winner == 2 ? new Color(255, 215, 0) : (winner == 0 ? new Color(192, 192, 192) : Color.WHITE));
        player2ScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        scorePanel.add(player1ScoreLabel);
        scorePanel.add(player2ScoreLabel);
        
        centerPanel.add(winnerLabel);
        centerPanel.add(reasonLabel);
        centerPanel.add(scorePanel);
        centerPanel.add(new JLabel()); // 빈 공간
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(1, 1, 0, 10));
        
        // 메인 메뉴로 돌아가기 버튼
        JButton mainMenuButton = createDialogButton("메인 메뉴로 돌아가기");
        mainMenuButton.addActionListener(e -> {
            ((JDialog)dialogPanel.getTopLevelAncestor()).dispose();
            returnToMainMenu();
        });
        
        buttonPanel.add(mainMenuButton);
        
        // 컴포넌트 배치
        dialogPanel.add(titleLabel, BorderLayout.NORTH);
        dialogPanel.add(centerPanel, BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 메인 메뉴로 돌아갑니다
     */
    private void returnToMainMenu() {
        // 타이머들을 완전히 정지
        if (fallTimer1 != null) {
            fallTimer1.stop();
        }
        if (fallTimer2 != null) {
            fallTimer2.stop();
        }
        if (blinkTimer != null) {
            blinkTimer.stop();
        }
        
        // 메인 메뉴 Scene으로 전환
        SwingUtilities.invokeLater(() -> {
            tetris.Game.setScene(new tetris.scene.menu.MainMenuScene(m_frame));
        });
    }

    protected void setupLayout(JFrame frame) {
        setLayout(new BorderLayout());

        // background based on colorblind theme
        int colorBlindMode = tetris.GameSettings.getInstance().getColorBlindMode();
        Color bg = tetris.ColorBlindHelper.getBackgroundColor(colorBlindMode);
        setBackground(bg);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));
        center.setOpaque(true);
        center.setBackground(bg);

        // 1P 보드 생성 (왼쪽)
        JPanel left = createBoardPanel(boardManager1, blockManager1, scoreManager1, 
                                       renderManager1, uiManager1, lineBlinkEffect1, 1);
        
        // 2P 보드 생성 (오른쪽)
        JPanel right = createBoardPanel(boardManager2, blockManager2, scoreManager2, 
                                        renderManager2, uiManager2, lineBlinkEffect2, 2);

        center.add(Box.createHorizontalGlue());
        center.add(left);
        center.add(Box.createHorizontalStrut(50)); // 두 보드 사이 간격을 30px에서 50px로 증가
        center.add(right);
        center.add(Box.createHorizontalGlue());

        add(center, BorderLayout.CENTER);
    }

    private JPanel createBoardPanel(BoardManager boardMgr, BlockManager blockMgr, 
                                     ScoreManager scoreMgr, RenderManager renderMgr,
                                     tetris.scene.game.core.UIManager uiMgr, 
                                     LineBlinkEffect lineBlinkEffect, int playerNum) {
        // 전체 컨테이너 (상단 라벨 + 게임 보드)
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());
        int colorBlindMode = tetris.GameSettings.getInstance().getColorBlindMode();
        Color bg = tetris.ColorBlindHelper.getBackgroundColor(colorBlindMode);
        container.setBackground(bg);
        container.setOpaque(true);
        
        // 래퍼 패널 생성 (GameScene의 UIManager처럼)
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(bg);

        // 게임 패널 생성 (RenderManager 사용)
        GameBoardPanel gamePanel = new GameBoardPanel(boardMgr, blockMgr, scoreMgr, lineBlinkEffect, playerNum);
        
        // 화면 크기에 따라 동적으로 셀 크기 계산
        int frameHeight = m_frame.getHeight();
        int frameWidth = m_frame.getWidth();
        
        // 배틀 모드: 두 보드가 좌우로 배치되므로 너비를 절반으로 나눔
        int availableWidth = (frameWidth - 150) / 2; // 간격과 여백을 더 크게 고려 (100 -> 150)
        int availableHeight = frameHeight - 100; // 상하 여백 고려
        
        // 셀 크기 계산 (보드 크기 + 미리보기 영역 + 공격 블록 표시 영역 고려)
        int cellSizeByHeight = availableHeight / (GAME_HEIGHT + 2);
        int cellSizeByWidth = availableWidth / (GAME_WIDTH + 2 + PREVIEW_SIZE + 4); // 보드 + 미리보기 + 공격블록 여유공간
        int cellSize = Math.min(cellSizeByHeight, cellSizeByWidth);
        cellSize = Math.max(15, Math.min(cellSize, 35)); // 15~35 사이로 제한
        
        int previewCellSize = cellSize * 2 / 3; // 셀 크기의 2/3
        
        if (playerNum == 1) {
            renderManager1 = new RenderManager(
                GAME_WIDTH, GAME_HEIGHT, cellSize, PREVIEW_SIZE, previewCellSize,
                boardMgr, blockMgr, gameStateManager1, scoreMgr
            );
        } else {
            renderManager2 = new RenderManager(
                GAME_WIDTH, GAME_HEIGHT, cellSize, PREVIEW_SIZE, previewCellSize,
                boardMgr, blockMgr, gameStateManager2, scoreMgr
            );
        }

        // 패널 크기 설정 (공격 블록 표시 영역도 고려)
        final int PREVIEW_MARGIN = 40;
        final int ATTACK_DISPLAY_MARGIN = 60; // 공격 블록 표시 영역을 위한 추가 여백
        int previewWidth = PREVIEW_SIZE * previewCellSize + PREVIEW_MARGIN;
        int attackDisplayWidth = ATTACK_DISPLAY_MARGIN; // 공격 표시 영역 너비
        
        gamePanel.setPreferredSize(new Dimension(
            (GAME_WIDTH + 2) * cellSize + previewWidth + attackDisplayWidth, // 공격 표시 영역 추가
            (GAME_HEIGHT + 4) * cellSize // 높이도 조금 더 여유롭게 (2 -> 4)
        ));
        gamePanel.setBackground(Color.BLACK);

        wrapper.add(gamePanel, new GridBagConstraints());
        container.add(wrapper, BorderLayout.CENTER);
        
        return container;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 전체 화면 퍼즈 오버레이 렌더링
        if ((gameStateManager1.isPaused() || gameStateManager2.isPaused()) && 
            !gameStateManager1.isGameOver() && !gameStateManager2.isGameOver()) {
            System.out.println("🎨 Rendering main BattleScene pause overlay");
            Graphics2D g2d = (Graphics2D) g.create();
            renderPauseOverlayFallback(g2d);
            g2d.dispose();
        }
    }

    private class GameBoardPanel extends JPanel {
        private final int playerNum;
        private final LineBlinkEffect lineBlinkEffect;

        public GameBoardPanel(BoardManager boardMgr, BlockManager blockMgr, 
                              ScoreManager scoreMgr, LineBlinkEffect lineBlinkEffect, int playerNum) {
            this.playerNum = playerNum;
            this.lineBlinkEffect = lineBlinkEffect;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            
            // RenderManager를 사용하여 GameScene과 100% 동일하게 렌더링
            // LineBlinkEffect와 청소 블링킹을 RenderManager에 전달하여 점멸 효과가 제대로 렌더링되도록 함
            RenderManager renderMgr = (playerNum == 1) ? renderManager1 : renderManager2;
            if (renderMgr != null) {
                // 각 플레이어별 청소 블링킹 상태 확인
                boolean cleanupActive = (playerNum == 1) ? cleanupBlinkingActive1 : cleanupBlinkingActive2;
                java.util.Set<java.awt.Point> cleanupCells = (playerNum == 1) ? cleanupBlinkingCells1 : cleanupBlinkingCells2;
                
                renderMgr.render(g2, getWidth(), getHeight(), lineBlinkEffect, 
                               null, 0, 0, false, cleanupActive, cleanupCells);
            }
            
            // 양쪽 중 하나라도 일시정지 상태이면 PAUSED 오버레이 표시
            // (P2P 대전에서 양쪽 동기화를 위해 필요)
            boolean isPaused1 = gameStateManager1.isPaused();
            boolean isPaused2 = gameStateManager2.isPaused();
            boolean isGameOver1 = gameStateManager1.isGameOver();
            boolean isGameOver2 = gameStateManager2.isGameOver();
            
            if ((isPaused1 || isPaused2) && !isGameOver1 && !isGameOver2) {
                System.out.println("🔍 DEBUG PAUSE: isPaused1=" + isPaused1 + ", isPaused2=" + isPaused2 + 
                                   ", isGameOver1=" + isGameOver1 + ", isGameOver2=" + isGameOver2 + 
                                   ", renderMgr=" + (renderMgr != null ? "NOT_NULL" : "NULL"));
                
                if (renderMgr != null) {
                    // RenderManager를 통한 퍼즈 화면 렌더링
                    int cellSize = renderMgr.getCellSize();
                    System.out.println("🎨 Rendering pause overlay with cellSize: " + cellSize);
                    renderPauseOverlayOnBoard(g2, cellSize);
                } else {
                    // RenderManager가 null인 경우 대안 퍼즈 화면 렌더링
                    System.out.println("🎨 Rendering fallback pause overlay");
                    renderPauseOverlayFallback(g2);
                }
            }
            
            // 시간제한 모드일 때 기존 시간 표시 영역을 덮어쓰기
            if ("time_limit".equals(gameMode)) {
                int cellSize = renderMgr.getCellSize();
                int previewCellSize = renderMgr.getPreviewCellSize();
                int previewX = (GAME_WIDTH + 2) * cellSize + 20;
                int previewY = cellSize + 20;
                int previewAreaSize = PREVIEW_SIZE * previewCellSize;
                int scoreBoardY = previewY + previewAreaSize + 30;
                int scoreBoardHeight = 120;
                int timeBoardY = scoreBoardY + scoreBoardHeight + 10;
                int timeBoardHeight = 50;
                int timeBoardWidth = PREVIEW_SIZE * previewCellSize;
                
                // 기존 시간 보드 영역을 배경색으로 지우기
                g2.setColor(new Color(40, 40, 40)); // 배경색
                g2.fillRect(previewX, timeBoardY, timeBoardWidth, timeBoardHeight);
                
                // 시간제한 보드 테두리 그리기
                g2.setColor(new Color(100, 100, 100));
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(previewX, timeBoardY, timeBoardWidth, timeBoardHeight);
                
                // TIME LIMIT 라벨
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String timeLabel = "TIME LIMIT";
                int labelWidth = fm.stringWidth(timeLabel);
                g2.drawString(timeLabel, previewX + (timeBoardWidth - labelWidth) / 2, timeBoardY + 20);
                
                // 남은 시간 표시
                int minutes = remainingTimeSeconds / 60;
                int seconds = remainingTimeSeconds % 60;
                String timeText = String.format("%02d:%02d", minutes, seconds);
                
                // 시간이 30초 이하일 때 빨간색으로 표시
                if (remainingTimeSeconds <= 30) {
                    g2.setColor(Color.RED);
                } else {
                    g2.setColor(Color.WHITE);
                }
                
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                fm = g2.getFontMetrics();
                int timeWidth = fm.stringWidth(timeText);
                g2.drawString(timeText, previewX + (timeBoardWidth - timeWidth) / 2, timeBoardY + 40);
            }
            
            // 1P/2P 표시 추가 (타이머 아래)
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            String playerText = (playerNum == 1) ? "1P" : "2P";
            
            // RenderManager에서 동적으로 계산된 셀 크기 가져오기
            int cellSize = renderMgr.getCellSize();
            int previewCellSize = renderMgr.getPreviewCellSize();
            int previewX = (GAME_WIDTH + 2) * cellSize + 20;
            int previewY = cellSize + 20;
            int previewAreaSize = PREVIEW_SIZE * previewCellSize;
            int scoreBoardY = previewY + previewAreaSize + 30;
            int scoreBoardHeight = 120;
            int timeBoardY = scoreBoardY + scoreBoardHeight + 10;
            int timeBoardHeight = 50;
            
            // 타이머 보드 아래에 1P/2P 라벨 표시
            int labelX = previewX;
            int labelY = timeBoardY + timeBoardHeight + 25; // 타이머 아래 25px 간격
            g2.drawString(playerText, labelX, labelY);
            
            // ═══════════════════════════════════════════════════════════════
            // 공격 대기 블록 표시 영역 (1P/2P 라벨 아래)
            // ═══════════════════════════════════════════════════════════════
            int attackBoardY = labelY + 30; // 라벨 아래 30px 간격
            int attackBoardWidth = PREVIEW_SIZE * previewCellSize + 40; // 너비를 20px 더 증가
            int attackBoardHeight = 250; // 높이를 50px 더 증가
            
            // 공격 대기 블록 프레임 그리기
            g2.setColor(new Color(60, 60, 60)); // 어두운 회색 배경
            g2.fillRect(previewX - 10, attackBoardY, attackBoardWidth, attackBoardHeight);
            
            // 프레임 테두리
            g2.setColor(new Color(100, 100, 100));
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(previewX - 10, attackBoardY, attackBoardWidth, attackBoardHeight);
            
            // 공격 블록 스택 내용 표시
            Stack<AttackBlock> currentStack = (playerNum == 1) ? attackQueue1 : attackQueue2;
            drawAttackQueue(g2, currentStack, previewX, attackBoardY + 10, previewCellSize, attackBoardWidth, attackBoardHeight);
            
            // 시야 제한 효과 렌더링 (일반모드와 동일한 효과 적용)
            if ((playerNum == 1 && visionBlockActive1) || (playerNum == 2 && visionBlockActive2)) {
                renderVisionBlockEffect(g2);
            }
            
            g2.dispose();
        }
        
        /**
         * 일반모드와 동일한 시야 차단 효과를 렌더링합니다.
         * 게임 보드의 중앙 4x20 영역을 완전 불투명한 검정색으로 가립니다.
         */
        private void renderVisionBlockEffect(Graphics2D g2) {
            RenderManager renderMgr = (playerNum == 1) ? renderManager1 : renderManager2;
            if (renderMgr == null) return;
            
            int cellSize = renderMgr.getCellSize();
            
            // 게임 보드 중앙 영역 계산 (일반모드와 동일)
            int boardStartX = cellSize;  // 보드 시작 X 위치 (경계 고려)
            int boardStartY = cellSize;  // 보드 시작 Y 위치 (경계 고려)
            
            // 중앙 4x20 영역을 가림 (10x20 보드의 중간 부분)
            int coverWidth = 4 * cellSize;   // 4블록 너비
            int coverHeight = 20 * cellSize;  // 20블록 높이 (전체 높이)
            int coverX = boardStartX + 3 * cellSize;  // 좌측에서 3블록 떨어진 위치 (중앙)
            int coverY = boardStartY;  // 상단부터 시작
            
            // 완전 불투명 검정 오버레이로 시야 차단 (일반모드와 동일)
            g2.setColor(new Color(0, 0, 0, 255)); // 완전 불투명 검정
            g2.fillRect(coverX, coverY, coverWidth, coverHeight);
            
            // 시야 차단 효과 경계선 (일반모드와 동일)
            g2.setColor(new Color(255, 0, 0, 100)); // 반투명 빨강 경계선
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(coverX, coverY, coverWidth, coverHeight);
        }
    }
    
    /**
     * 공격 블록 스택의 내용을 그립니다
     */
    private void drawAttackQueue(Graphics2D g2, Stack<AttackBlock> stack, int startX, int startY, int cellSize, int maxWidth, int maxHeight) {
        // 사각형 영역 내에서만 그리도록 클리핑 설정
        Shape originalClip = g2.getClip();
        g2.setClip(startX, startY, maxWidth - 10, maxHeight - 20);
        
        // 셀 크기를 더 크게 조정 (원래 크기의 절반 사용)
        int blockCellSize = Math.min(cellSize / 2, (maxWidth - 20) / 10); // 더 크게 표시
        
        // 대기 중인 공격 블록들을 미리보기로 표시
        int y = startY + 10; // 텍스트가 없어진 만큼 위쪽에서 시작
        int count = 0;
        int maxBlocks = Math.min(4, (maxHeight - 20) / (blockCellSize + 3)); // 텍스트 영역이 줄어든만큼 조정
        
        for (AttackBlock attackBlock : stack) {
            if (count >= maxBlocks) break;
            
            // 각 공격 블록 패턴을 한 줄로 표시
            for (int col = 0; col < attackBlock.getWidth() && col < 10; col++) {
                if (attackBlock.hasBlockAt(col)) {
                    g2.setColor(attackBlock.getColors()[col]);
                } else {
                    g2.setColor(Color.BLACK);
                }
                
                int x = startX + 5 + col * blockCellSize;
                g2.fillRect(x, y, blockCellSize - 1, blockCellSize - 1);
            }
            
            y += blockCellSize + 3; // 블록 간격을 3px로 증가
            count++;
        }
        
        // 클리핑 복원
        g2.setClip(originalClip);
    }
    
    @Override
    public void onEnter() {
        // Scene을 프레임의 ContentPane으로 설정
        m_frame.setContentPane(this);
        
        // InputHandler 등록 (1P, 2P 모두)
        m_frame.addKeyListener(inputHandler1);
        m_frame.addKeyListener(inputHandler2);
        
        // 프레임에 포커스 요청
        m_frame.requestFocusInWindow();
        
        // 타이머 시작
        if (fallTimer1 != null) fallTimer1.start();
        if (fallTimer2 != null) fallTimer2.start();
        if (blinkTimer != null) blinkTimer.start(); // 점멸 효과 타이머 시작
        
        revalidate();
        repaint();
    }

    /**
     * 메인 메뉴로 나가기
     */
    protected void exitToMenu() {
        try {
            Game.setScene(new MainMenuScene(m_frame));
        } catch (Exception e) {
            System.err.println("메뉴로 나가기 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onExit() {
        // InputHandler 제거
        m_frame.removeKeyListener(inputHandler1);
        m_frame.removeKeyListener(inputHandler2);
        
        // 타이머 정지
        if (fallTimer1 != null) fallTimer1.stop();
        if (fallTimer2 != null) fallTimer2.stop();
        if (blinkTimer != null) blinkTimer.stop(); // 점멸 효과 타이머 정지
    }
    
    /**
     * 메인메뉴 스타일의 기본 다이얼로그를 생성합니다
     */
    private JDialog createBaseDialog() {
        JDialog dialog = new JDialog(m_frame, true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(m_frame);
        dialog.setFocusable(true);
        return dialog;
    }
    
    /**
     * 메인메뉴 스타일의 다이얼로그 패널을 생성합니다
     */
    private JPanel createDialogPanel() {
        JPanel dialogPanel = new JPanel();
        dialogPanel.setBackground(tetris.util.Theme.MenuBG());
        dialogPanel.setLayout(new BorderLayout());
        dialogPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(tetris.util.Theme.MenuTitle(), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        return dialogPanel;
    }
    
    /**
     * 메인메뉴 스타일의 버튼을 생성합니다
     */
    private JButton createDialogButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(250, 35));
        button.setBackground(tetris.util.Theme.MenuButton());
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // 호버 효과
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(120, 120, 200));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(tetris.util.Theme.MenuButton());
                }
            }
        });
        
        return button;
    }
    
    /**
     * 게임 보드에 일시정지 오버레이를 렌더링합니다.
     * (RenderManager와 별도로 BattleScene에서 사용)
     */
    private void renderPauseOverlayOnBoard(Graphics2D g2d, int cellSize) {
        // 게임 영역에 반투명 오버레이
        g2d.setColor(new Color(0, 0, 0, 150)); // 반투명 검은색
        g2d.fillRect(cellSize, cellSize, GAME_WIDTH * cellSize, GAME_HEIGHT * cellSize);
        
        // PAUSED 텍스트
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String pausedText = "PAUSED";
        int textWidth = fm.stringWidth(pausedText);
        int textHeight = fm.getHeight();
        
        // 게임 영역 중앙에 텍스트 배치
        int gameAreaCenterX = cellSize + (GAME_WIDTH * cellSize) / 2;
        int gameAreaCenterY = cellSize + (GAME_HEIGHT * cellSize) / 2;
        
        int textX = gameAreaCenterX - textWidth / 2;
        int textY = gameAreaCenterY + textHeight / 4; // 텍스트 베이스라인 조정
        
        g2d.drawString(pausedText, textX, textY);
        
        // 부가 안내 텍스트
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        FontMetrics smallFm = g2d.getFontMetrics();
        String instructionText = "Press P to resume";
        int instructionWidth = smallFm.stringWidth(instructionText);
        int instructionX = gameAreaCenterX - instructionWidth / 2;
        int instructionY = textY + 60; // PAUSED 텍스트 아래 60px
        
        g2d.drawString(instructionText, instructionX, instructionY);
        
        // Q 키로 메뉴로 돌아가기 안내 텍스트
        String exitText = "Press Q to return to menu";
        int exitWidth = smallFm.stringWidth(exitText);
        int exitX = gameAreaCenterX - exitWidth / 2;
        int exitY = instructionY + 25; // 재개 안내 텍스트 아래 25px
        
        g2d.drawString(exitText, exitX, exitY);
    }
    
    /**
     * RenderManager가 null인 경우 대안 퍼즈 화면을 렌더링합니다.
     */
    private void renderPauseOverlayFallback(Graphics2D g2d) {
        // 전체 화면에 반투명 오버레이
        g2d.setColor(new Color(0, 0, 0, 150)); // 반투명 검은색
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // PAUSED 텍스트
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String pausedText = "PAUSED";
        int textWidth = fm.stringWidth(pausedText);
        int textHeight = fm.getHeight();
        
        // 화면 중앙에 텍스트 배치
        int screenCenterX = getWidth() / 2;
        int screenCenterY = getHeight() / 2;
        
        int textX = screenCenterX - textWidth / 2;
        int textY = screenCenterY + textHeight / 4; // 텍스트 베이스라인 조정
        
        g2d.drawString(pausedText, textX, textY);
        
        // 부가 안내 텍스트
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        FontMetrics smallFm = g2d.getFontMetrics();
        String instructionText = "Press P to resume";
        int instructionWidth = smallFm.stringWidth(instructionText);
        int instructionX = screenCenterX - instructionWidth / 2;
        int instructionY = textY + 60; // PAUSED 텍스트 아래 60px
        
        g2d.drawString(instructionText, instructionX, instructionY);
        
        // Q 키로 메뉴로 돌아가기 안내 텍스트
        String exitText = "Press Q to return to menu";
        int exitWidth = smallFm.stringWidth(exitText);
        int exitX = screenCenterX - exitWidth / 2;
        int exitY = instructionY + 25; // 재개 안내 텍스트 아래 25px
        
        g2d.drawString(exitText, exitX, exitY);
    }
    
    // ========== 상대방 아이템 효과 처리 메서드들 ==========
    
    /**
     * 상대방에게 낙하속도 증가 효과 적용
     */
    public void applySpeedUpToOpponent(int sourcePlayer) {
        System.out.println("🚀 applySpeedUpToOpponent called by Player " + sourcePlayer);
        
        if (sourcePlayer == 1) {
            // Player 2의 속도를 빠르게
            if (fallTimer2 != null) {
                fallTimer2.setDelay(400);
            }
            System.out.println("🚀 Player 1이 Player 2에게 낙하속도 증가 적용!");
        } else {
            // Player 1의 속도를 빠르게  
            if (fallTimer1 != null) {
                fallTimer1.setDelay(400);
            }
            System.out.println("🚀 Player 2가 Player 1에게 낙하속도 증가 적용!");
        }
        
        // 5초 후 원래 속도로 복구
        Timer restoreTimer = new Timer(5000, e -> {
            GameSettings.Difficulty difficulty = GameSettings.getInstance().getDifficulty();
            int normalSpeed = getInitialDelay(difficulty);
            if (sourcePlayer == 1) {
                if (fallTimer2 != null) {
                    fallTimer2.setDelay(normalSpeed);
                }
                System.out.println("🔄 Player 2 속도 복구 완료");
            } else {
                if (fallTimer1 != null) {
                    fallTimer1.setDelay(normalSpeed);
                }
                System.out.println("🔄 Player 1 속도 복구 완료");
            }
        });
        restoreTimer.setRepeats(false);
        restoreTimer.start();
    }
    
    /**
     * 상대방에게 낙하속도 감소 효과 적용
     */
    public void applySpeedDownToOpponent(int sourcePlayer) {
        System.out.println("🐌 applySpeedDownToOpponent called by Player " + sourcePlayer);
        
        if (sourcePlayer == 1) {
            // Player 2의 속도를 느리게
            if (fallTimer2 != null) {
                fallTimer2.setDelay(1500);
            }
            System.out.println("🐌 Player 1이 Player 2에게 낙하속도 감소 적용!");
        } else {
            // Player 1의 속도를 느리게
            if (fallTimer1 != null) {
                fallTimer1.setDelay(1500);
            }
            System.out.println("🐌 Player 2가 Player 1에게 낙하속도 감소 적용!");
        }
        
        // 5초 후 원래 속도로 복구
        Timer restoreTimer = new Timer(5000, e -> {
            GameSettings.Difficulty difficulty = GameSettings.getInstance().getDifficulty();
            int normalSpeed = getInitialDelay(difficulty);
            if (sourcePlayer == 1) {
                if (fallTimer2 != null) {
                    fallTimer2.setDelay(normalSpeed);
                }
                System.out.println("🔄 Player 2 속도 복구 완료");
            } else {
                if (fallTimer1 != null) {
                    fallTimer1.setDelay(normalSpeed);
                }
                System.out.println("🔄 Player 1 속도 복구 완료");
            }
        });
        restoreTimer.setRepeats(false);
        restoreTimer.start();
    }
    
    /**
     * 상대방에게 시야제한 효과 적용
     */
    public void applyVisionBlockToOpponent(int sourcePlayer) {
        System.out.println("👁️ applyVisionBlockToOpponent called by Player " + sourcePlayer);
        
        if (sourcePlayer == 1) {
            setVisionBlockActive2(true);
            System.out.println("👁️ Player 1이 Player 2에게 시야제한 적용!");
        } else {
            setVisionBlockActive1(true);
            System.out.println("👁️ Player 2가 Player 1에게 시야제한 적용!");
        }
        
        // 3초 후 시야제한 해제
        Timer restoreTimer = new Timer(3000, e -> {
            if (sourcePlayer == 1) {
                setVisionBlockActive2(false);
                System.out.println("🔄 Player 2 시야제한 해제!");
            } else {
                setVisionBlockActive1(false);
                System.out.println("🔄 Player 1 시야제한 해제!");
            }
            repaint();
        });
        restoreTimer.setRepeats(false);
        restoreTimer.start();
        repaint();
    }
    
    // ========== 시야 차단 관련 헬퍼 메서드들 ==========
    
    private void setVisionBlockActive1(boolean active) {
        this.visionBlockActive1 = active;
        System.out.println("🔍 DEBUG: Player 1 vision block set to " + active);
        repaint();
    }
    
    private void setVisionBlockActive2(boolean active) {
        this.visionBlockActive2 = active;
        System.out.println("🔍 DEBUG: Player 2 vision block set to " + active);
        repaint();
    }
    
    // ========== 청소 블링킹 관련 메서드들 ==========
    
    /**
     * Player 1의 청소 블링킹 효과를 시작합니다.
     * @param cells 블링킹할 셀 좌표들
     */
    public void startCleanupBlinking1(java.util.Set<java.awt.Point> cells) {
        if (cells.isEmpty()) return;
        cleanupBlinkingCells1.clear();
        cleanupBlinkingCells1.addAll(cells);
        cleanupBlinkingActive1 = true;
        System.out.println("🧹 Player 1 cleanup blinking started for " + cells.size() + " cells");
    }
    
    /**
     * Player 2의 청소 블링킹 효과를 시작합니다.
     * @param cells 블링킹할 셀 좌표들
     */
    public void startCleanupBlinking2(java.util.Set<java.awt.Point> cells) {
        if (cells.isEmpty()) return;
        cleanupBlinkingCells2.clear();
        cleanupBlinkingCells2.addAll(cells);
        cleanupBlinkingActive2 = true;
        System.out.println("🧹 Player 2 cleanup blinking started for " + cells.size() + " cells");
    }
    
    /**
     * Player 1의 청소 블링킹 효과를 중지합니다.
     */
    public void stopCleanupBlinking1() {
        cleanupBlinkingActive1 = false;
        cleanupBlinkingCells1.clear();
        System.out.println("🚫 Player 1 cleanup blinking stopped");
    }
    
    /**
     * Player 2의 청소 블링킹 효과를 중지합니다.
     */
    public void stopCleanupBlinking2() {
        cleanupBlinkingActive2 = false;
        cleanupBlinkingCells2.clear();
        System.out.println("🚫 Player 2 cleanup blinking stopped");
    }
    
    /**
     * LINE_CLEAR 아이템이 사용되었음을 표시합니다.
     * 이후 줄 삭제는 아이템으로 인한 것으로 간주되어 아이템 생성 카운트와 방해블록 생성에서 제외됩니다.
     */
    public void markItemLineClear() {
        // 현재 활성화된 플레이어의 아이템 줄 삭제 플래그를 설정
        // LineClearEffect에서 호출되므로 어느 플레이어의 효과인지 확인 필요
        // 일단 둘 다 설정하고, 실제 줄 삭제 시 해당 플레이어만 처리
        isItemLineClear1 = true;
        isItemLineClear2 = true;
        System.out.println("BattleScene: Marked next line clearing as item-caused for both players");
    }
    
    /**
     * 특정 플레이어의 LINE_CLEAR 아이템이 사용되었음을 표시합니다.
     * @param playerNumber 플레이어 번호 (1 또는 2)
     */
    public void markItemLineClear(int playerNumber) {
        if (playerNumber == 1) {
            isItemLineClear1 = true;
            System.out.println("BattleScene: Marked next line clearing as item-caused for Player 1");
        } else if (playerNumber == 2) {
            isItemLineClear2 = true;
            System.out.println("BattleScene: Marked next line clearing as item-caused for Player 2");
        }
    }
    
    /**
     * 시야 제한 효과를 적용합니다. (VisionBlockEffect 호환성을 위한 메서드)
     * ItemEffectContext의 playerNumber를 확인하여 적절한 플레이어에게 효과를 적용합니다.
     * @param active 시야 제한 활성화 여부
     */
    public void setVisionBlockActive(boolean active) {
        // 이 메서드는 VisionBlockEffect에서 호출되므로,
        // 실제로는 어느 플레이어의 효과인지를 구분할 수 없습니다.
        // 따라서 VisionBlockEffect가 배틀 모드에서 플레이어별로 호출되도록 수정이 필요합니다.
        System.out.println("⚠️ BattleScene.setVisionBlockActive called but player not specified");
        
        // 임시 방편으로 두 플레이어 모두에게 적용 (이는 올바르지 않으므로 VisionBlockEffect 수정 필요)
        setVisionBlockActive1(active);
        setVisionBlockActive2(active);
    }
    
    /**
     * 특정 플레이어에게 시야 제한 효과를 적용합니다.
     * @param playerNumber 플레이어 번호 (1 또는 2)
     * @param active 시야 제한 활성화 여부
     */
    public void setVisionBlockActive(int playerNumber, boolean active) {
        if (playerNumber == 1) {
            setVisionBlockActive1(active);
        } else if (playerNumber == 2) {
            setVisionBlockActive2(active);
        }
        System.out.println("👁️ BattleScene: Set vision block for Player " + playerNumber + " to " + active);
    }
}
