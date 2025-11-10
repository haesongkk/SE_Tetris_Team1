package tetris.scene.battle;

import tetris.scene.Scene;
import tetris.scene.game.core.BoardManager;
import tetris.scene.game.core.BlockManager;
import tetris.scene.game.core.ScoreManager;
import tetris.scene.game.core.RenderManager;
import tetris.scene.game.core.GameStateManager;
import tetris.scene.game.core.InputHandler;
import tetris.util.LineBlinkEffect;
import tetris.GameSettings;
import javax.swing.*;
import java.awt.*;

/**
 * Local Battle scene - GameScene × 2
 * 각 플레이어가 독립적인 GameScene 로직을 가짐
 */
public class BattleScene extends Scene {
    private static final int GAME_HEIGHT = 20;
    private static final int GAME_WIDTH = 10;
    private static final int PREVIEW_SIZE = 4;
    
    private final JFrame m_frame;
    
    // 선택된 게임 모드
    @SuppressWarnings("unused")
    private final String gameMode;
    
    // ═══════════════════════════════════════════════════════════════
    // 1P (왼쪽) - 완전한 GameScene 복제
    // ═══════════════════════════════════════════════════════════════
    private final BoardManager boardManager1;
    private final BlockManager blockManager1;
    private final ScoreManager scoreManager1;
    private final tetris.scene.game.core.UIManager uiManager1;
    private RenderManager renderManager1;
    private final InputHandler inputHandler1;
    private final GameStateManager gameStateManager1;
    private final LineBlinkEffect lineBlinkEffect1;
    
    // ═══════════════════════════════════════════════════════════════
    // 2P (오른쪽) - 완전한 GameScene 복제
    // ═══════════════════════════════════════════════════════════════
    private final BoardManager boardManager2;
    private final BlockManager blockManager2;
    private final ScoreManager scoreManager2;
    private final tetris.scene.game.core.UIManager uiManager2;
    private RenderManager renderManager2;
    private final InputHandler inputHandler2;
    private final GameStateManager gameStateManager2;
    private final LineBlinkEffect lineBlinkEffect2;
    
    // 타이머 (블록 자동 낙하)
    private Timer fallTimer1;
    private Timer fallTimer2;
    
    // 점멸 효과 전용 타이머 (GameScene의 blinkTimer와 동일)
    private Timer blinkTimer;
    private static final int BLINK_INTERVAL_MS = 50; // 점멸 효과 업데이트 주기 (밀리초)
    
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
            
            // 줄 삭제
            int[] lineResults = boardMgr.clearCompletedAndBombLinesSeparately();
            int clearedLines = lineResults[0];
            
            // 점수 추가
            if (clearedLines > 0) {
                scoreManager1.addScore(clearedLines);
                System.out.println("Player 1 cleared " + clearedLines + " lines!");
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
            
            // 줄 삭제
            int[] lineResults = boardMgr.clearCompletedAndBombLinesSeparately();
            int clearedLines = lineResults[0];
            
            // 점수 추가
            if (clearedLines > 0) {
                scoreManager2.addScore(clearedLines);
                System.out.println("Player 2 cleared " + clearedLines + " lines!");
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
            
            // TODO: 승자 표시 (loser가 1이면 2P 승리, loser가 2이면 1P 승리)
            System.out.println("Game Over! Player " + (loser == 1 ? "2" : "1") + " wins!");
        }
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
        center.add(Box.createHorizontalStrut(30));
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
        int availableWidth = (frameWidth - 100) / 2; // 간격 및 여백 고려
        int availableHeight = frameHeight - 100; // 상하 여백 고려
        
        // 셀 크기 계산 (보드 크기 + 미리보기 영역 고려)
        int cellSizeByHeight = availableHeight / (GAME_HEIGHT + 2);
        int cellSizeByWidth = availableWidth / (GAME_WIDTH + 2 + PREVIEW_SIZE + 2); // 보드 + 미리보기
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

        // 패널 크기 설정
        final int PREVIEW_MARGIN = 40;
        int previewWidth = PREVIEW_SIZE * previewCellSize + PREVIEW_MARGIN;
        gamePanel.setPreferredSize(new Dimension(
            (GAME_WIDTH + 2) * cellSize + previewWidth,
            (GAME_HEIGHT + 2) * cellSize
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
            
            g2.dispose();
        }
    }    @Override
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
