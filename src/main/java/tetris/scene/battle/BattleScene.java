package tetris.scene.battle;

import tetris.scene.Scene;
import tetris.scene.game.core.BoardManager;
import tetris.scene.game.core.BlockManager;
import tetris.scene.game.core.ScoreManager;
import tetris.scene.game.core.RenderManager;
import tetris.scene.game.core.GameStateManager;
import tetris.scene.game.core.InputHandler;
import tetris.scene.game.blocks.Block;
import tetris.util.LineBlinkEffect;
import tetris.GameSettings;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Queue;
import java.util.LinkedList;

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
    @SuppressWarnings("unused")
    private final String gameMode;
    
    // ═══════════════════════════════════════════════════════════════
    // 1P (왼쪽) - 완전한 GameScene 복제
    // ═══════════════════════════════════════════════════════════════
    protected final BoardManager boardManager1;
    protected final BlockManager blockManager1;
    protected final ScoreManager scoreManager1;
    protected final tetris.scene.game.core.UIManager uiManager1;
    protected RenderManager renderManager1;
    protected InputHandler inputHandler1;
    protected GameStateManager gameStateManager1;
    protected final LineBlinkEffect lineBlinkEffect1;
    
    // ═══════════════════════════════════════════════════════════════
    // 2P (오른쪽) - 완전한 GameScene 복제
    // ═══════════════════════════════════════════════════════════════
    protected final BoardManager boardManager2;
    protected final BlockManager blockManager2;
    protected final ScoreManager scoreManager2;
    protected final tetris.scene.game.core.UIManager uiManager2;
    protected RenderManager renderManager2;
    protected InputHandler inputHandler2;
    protected GameStateManager gameStateManager2;
    protected final LineBlinkEffect lineBlinkEffect2;
    
    // 타이머 (블록 자동 낙하)
    private Timer fallTimer1;
    private Timer fallTimer2;
    
    // 점멸 효과 전용 타이머 (GameScene의 blinkTimer와 동일)
    private Timer blinkTimer;
    private static final int BLINK_INTERVAL_MS = 50; // 점멸 효과 업데이트 주기 (밀리초)
    
    // 공격 대기 블록 수 (상대가 삭제한 줄 수)
    // 공격 블록 큐
    private Queue<AttackBlock> attackQueue1 = new LinkedList<>(); // 1P가 받을 공격
    private Queue<AttackBlock> attackQueue2 = new LinkedList<>(); // 2P가 받을 공격
    
    // 게임 오버 상태 (어느 한쪽이라도 게임 오버되면 양쪽 모두 종료)
    private boolean isGameOver = false;

    public BattleScene(JFrame frame, String gameMode) {
        super(frame);
        this.m_frame = frame;
        this.gameMode = gameMode;
        
        GameSettings.Difficulty difficulty = GameSettings.getInstance().getDifficulty();
        
        // ═══════════════════════════════════════════════════════════════
        // 1P 초기화 (GameScene과 동일)
        // ═══════════════════════════════════════════════════════════════
        this.boardManager1 = new BoardManager();
        this.scoreManager1 = new ScoreManager(difficulty);
        this.blockManager1 = new BlockManager(GAME_WIDTH, GAME_HEIGHT, boardManager1, scoreManager1, difficulty);
        this.uiManager1 = new tetris.scene.game.core.UIManager();
        this.gameStateManager1 = new GameStateManager(new Player1Callback());
        this.inputHandler1 = new InputHandler(frame, new Player1Callback(), 1); // 1P 키 설정 사용
        this.lineBlinkEffect1 = new LineBlinkEffect(new BlinkCallback1());
        
        boardManager1.reset();
        blockManager1.initializeBlocks();
        
        // ═══════════════════════════════════════════════════════════════
        // 2P 초기화 (GameScene과 동일)
        // ═══════════════════════════════════════════════════════════════
        this.boardManager2 = new BoardManager();
        this.scoreManager2 = new ScoreManager(difficulty);
        this.blockManager2 = new BlockManager(GAME_WIDTH, GAME_HEIGHT, boardManager2, scoreManager2, difficulty);
        this.uiManager2 = new tetris.scene.game.core.UIManager();
        this.gameStateManager2 = new GameStateManager(new Player2Callback());
        this.inputHandler2 = new InputHandler(frame, new Player2Callback(), 2); // 2P 키 설정 사용
        this.lineBlinkEffect2 = new LineBlinkEffect(new BlinkCallback2());
        
        boardManager2.reset();
        blockManager2.initializeBlocks();
        
        setupLayout(frame);
        setupTimers();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 1P InputCallback 구현
    // ═══════════════════════════════════════════════════════════════
    private class Player1Callback implements InputHandler.InputCallback, GameStateManager.StateChangeCallback {
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
                case HOLD:
                    // TODO: Hold 기능
                    break;
                case EXIT_TO_MENU:
                    // InputHandler가 자동 처리
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
            // 일시정지 토글 시 처리
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
                case HOLD:
                    // TODO: Hold 기능
                    break;
                case EXIT_TO_MENU:
                    // InputHandler가 자동 처리
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
            // 일시정지 토글 시 처리
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
            int clearedLines = lineResults[0];
            
            // 점수 추가
            if (clearedLines > 0) {
                scoreManager1.addScore(clearedLines);
                System.out.println("Player 1 cleared " + clearedLines + " lines!");
                
                // 2줄 이상 삭제 시 상대방에게 공격 블록 생성
                if (clearedLines >= 2) {
                    generateAttackBlocks(fullLines, 2); // Player 2가 공격받음
                }
            }
            
            // 다음 블록 생성
            if (!blockManager1.isGameOver()) {
                blockManager1.generateNextBlock();
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
            int clearedLines = lineResults[0];
            
            // 점수 추가
            if (clearedLines > 0) {
                scoreManager2.addScore(clearedLines);
                System.out.println("Player 2 cleared " + clearedLines + " lines!");
                
                // 2줄 이상 삭제 시 상대방에게 공격 블록 생성
                if (clearedLines >= 2) {
                    generateAttackBlocks(fullLines, 1); // Player 1이 공격받음
                }
            }
            
            // 다음 블록 생성
            if (!blockManager2.isGameOver()) {
                blockManager2.generateNextBlock();
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
    private void generateAttackBlocks(java.util.List<Integer> clearedLines, int targetPlayer) {
        Queue<AttackBlock> targetQueue = (targetPlayer == 1) ? attackQueue1 : attackQueue2;
        
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
            
            targetQueue.offer(new AttackBlock(GAME_WIDTH, pattern, colors, blockTypes));
        }
        
        System.out.println("Generated " + clearedLines.size() + " attack blocks for Player " + targetPlayer);
    }
    
    /**
     * 대기 중인 공격 블록을 게임 보드에 적용
     * @param player 공격받는 플레이어 (1 또는 2)
     */
    private void applyAttackBlocks(int player) {
        Queue<AttackBlock> attackQueue = (player == 1) ? attackQueue1 : attackQueue2;
        BoardManager boardMgr = (player == 1) ? boardManager1 : boardManager2;
        
        if (attackQueue.isEmpty()) {
            return;
        }
        
        System.out.println("Applying " + attackQueue.size() + " attack blocks to Player " + player);
        
        // 모든 대기 중인 공격 블록을 보드 하단에 추가
        while (!attackQueue.isEmpty()) {
            AttackBlock attackBlock = attackQueue.poll();
            
            // 기존 블록들을 위로 한 줄씩 이동
            int[][] board = boardMgr.getBoard();
            Color[][] boardColors = boardMgr.getBoardColors();
            int[][] boardTypes = boardMgr.getBoardTypes();
            
            for (int row = 0; row < GAME_HEIGHT - 1; row++) {
                for (int col = 0; col < GAME_WIDTH; col++) {
                    board[row][col] = board[row + 1][col];
                    boardColors[row][col] = boardColors[row + 1][col];
                    boardTypes[row][col] = boardTypes[row + 1][col];
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
            }
        }
    }
    
    /**
     * 타이머 설정 (블록 자동 낙하)
     */
    private void setupTimers() {
        GameSettings.Difficulty difficulty = GameSettings.getInstance().getDifficulty();
        int delay = getInitialDelay(difficulty);
        
        // 1P 타이머
        fallTimer1 = new Timer(delay, e -> {
            if (!isGameOver) {
                moveBlockDown(1);
            }
        });
        
        // 2P 타이머
        fallTimer2 = new Timer(delay, e -> {
            if (!isGameOver) {
                moveBlockDown(2);
            }
        });
        
        // 점멸 효과 전용 타이머 (GameScene의 blinkTimer와 동일하게 50ms마다 실행)
        blinkTimer = new Timer(BLINK_INTERVAL_MS, e -> {
            if (!isGameOver) {
                // 1P, 2P 모두 점멸 효과 업데이트
                lineBlinkEffect1.update();
                lineBlinkEffect2.update();
                repaint();
            }
        });
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
        if (isGameOver) return;
        
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
        if (isGameOver) return;
        
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
        if (isGameOver) return;
        
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
                blockMgr.generateNextBlock();
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
    private void handleGameOver(int loser) {
        if (!isGameOver) {
            isGameOver = true;
            fallTimer1.stop();
            fallTimer2.stop();
            if (blinkTimer != null) blinkTimer.stop(); // 점멸 효과 타이머 정지
            
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
            
            if (player1Score > player2Score) {
                actualWinner = 1;
            } else if (player2Score > player1Score) {
                actualWinner = 2;
            } else {
                // 점수가 같으면 게임 오버되지 않은 플레이어가 승자 (게임 오버된 플레이어가 패자)
                actualWinner = (loser == 1) ? 2 : 1;
            }
            
            // 승자 표시 다이얼로그 실행
            showBattleGameOverDialog(actualWinner);
        }
    }
    
    /**
     * 배틀 게임 오버 다이얼로그를 표시합니다
     * @param winner 승리한 플레이어 (1 또는 2)
     */
    protected void showBattleGameOverDialog(int winner) {
        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog(m_frame, "Game Over", true);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.setLayout(new GridBagLayout());
            dialog.setSize(350, 280);
            dialog.setLocationRelativeTo(m_frame);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            
            // 승자 표시
            JLabel winnerLabel = new JLabel();
            winnerLabel.setFont(new Font("Arial", Font.BOLD, 18));
            
            String winnerText;
            if (winner == 1) {
                winnerText = "Player 1 Wins!";
                winnerLabel.setForeground(new Color(255, 215, 0)); // Gold color
            } else {
                winnerText = "Player 2 Wins!";
                winnerLabel.setForeground(new Color(255, 215, 0)); // Gold color
            }
            winnerLabel.setText(winnerText);
            winnerLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            dialog.add(winnerLabel, gbc);
            
            // 플레이어 점수 표시
            int player1Score = scoreManager1.getScore();
            int player2Score = scoreManager2.getScore();
            
            // Player 1 점수
            JLabel player1ScoreLabel = new JLabel("Player 1");
            player1ScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
            player1ScoreLabel.setForeground(winner == 1 ? new Color(255, 215, 0) : Color.WHITE);
            player1ScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            dialog.add(player1ScoreLabel, gbc);
            
            JLabel player1ScoreValue = new JLabel(String.format("%,d", player1Score));
            player1ScoreValue.setFont(new Font("Arial", Font.PLAIN, 16));
            player1ScoreValue.setForeground(winner == 1 ? new Color(255, 215, 0) : Color.LIGHT_GRAY);
            player1ScoreValue.setHorizontalAlignment(SwingConstants.CENTER);
            
            gbc.gridy = 2;
            dialog.add(player1ScoreValue, gbc);
            
            // Player 2 점수
            JLabel player2ScoreLabel = new JLabel("Player 2");
            player2ScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
            player2ScoreLabel.setForeground(winner == 2 ? new Color(255, 215, 0) : Color.WHITE);
            player2ScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            gbc.gridx = 1;
            gbc.gridy = 1;
            dialog.add(player2ScoreLabel, gbc);
            
            JLabel player2ScoreValue = new JLabel(String.format("%,d", player2Score));
            player2ScoreValue.setFont(new Font("Arial", Font.PLAIN, 16));
            player2ScoreValue.setForeground(winner == 2 ? new Color(255, 215, 0) : Color.LIGHT_GRAY);
            player2ScoreValue.setHorizontalAlignment(SwingConstants.CENTER);
            
            gbc.gridy = 2;
            dialog.add(player2ScoreValue, gbc);
            
            // 메인 메뉴로 돌아가기 버튼
            JButton mainMenuButton = new JButton("Main Menu");
            mainMenuButton.setFont(new Font("Arial", Font.BOLD, 14));
            mainMenuButton.setPreferredSize(new Dimension(120, 40));
            mainMenuButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                    returnToMainMenu();
                }
            });
            
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            dialog.add(mainMenuButton, gbc);
            
            // 다이얼로그 배경색 설정
            dialog.getContentPane().setBackground(new Color(40, 40, 40));
            
            dialog.setVisible(true);
        });
    }
    
    /**
     * 메인 메뉴로 돌아갑니다
     */
    protected void returnToMainMenu() {
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

    private void setupLayout(JFrame frame) {
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
            // LineBlinkEffect를 RenderManager에 전달하여 점멸 효과가 제대로 렌더링되도록 함
            RenderManager renderMgr = (playerNum == 1) ? renderManager1 : renderManager2;
            if (renderMgr != null) {
                renderMgr.render(g2, getWidth(), getHeight(), lineBlinkEffect, 
                               null, 0, 0, false, false, new java.util.HashSet<>());
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
            
            // 공격 블록 큐 내용 표시
            Queue<AttackBlock> currentQueue = (playerNum == 1) ? attackQueue1 : attackQueue2;
            drawAttackQueue(g2, currentQueue, previewX, attackBoardY + 10, previewCellSize, attackBoardWidth, attackBoardHeight);
            
            g2.dispose();
        }
    }
    
    /**
     * 공격 블록 큐의 내용을 그립니다
     */
    private void drawAttackQueue(Graphics2D g2, Queue<AttackBlock> queue, int startX, int startY, int cellSize, int maxWidth, int maxHeight) {
        // 사각형 영역 내에서만 그리도록 클리핑 설정
        Shape originalClip = g2.getClip();
        g2.setClip(startX, startY, maxWidth - 10, maxHeight - 20);
        
        // 셀 크기를 더 크게 조정 (원래 크기의 절반 사용)
        int blockCellSize = Math.min(cellSize / 2, (maxWidth - 20) / 10); // 더 크게 표시
        
        // 대기 중인 공격 블록들을 미리보기로 표시
        int y = startY + 10; // 텍스트가 없어진 만큼 위쪽에서 시작
        int count = 0;
        int maxBlocks = Math.min(4, (maxHeight - 20) / (blockCellSize + 3)); // 텍스트 영역이 줄어든만큼 조정
        
        for (AttackBlock attackBlock : queue) {
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
}
