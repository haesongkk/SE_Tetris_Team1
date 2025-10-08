package tetris.scene.game;

import tetris.scene.Scene;
import tetris.scene.game.blocks.*;
import tetris.scene.game.core.BoardManager;
import tetris.scene.game.core.BlockManager;
import tetris.scene.game.core.InputHandler;
import tetris.scene.game.core.GameStateManager;
import tetris.scene.game.core.RenderManager;
import tetris.scene.game.core.TimerManager;
import tetris.scene.game.core.UIManager;
import tetris.scene.game.overlay.GameOver;
import tetris.scene.game.core.ScoreManager;

import javax.swing.*;
import java.awt.*;
import javax.swing.OverlayLayout;

public class GameScene extends Scene implements InputHandler.InputCallback, GameStateManager.StateChangeCallback, TimerManager.TimerCallback {
    private JFrame m_frame;
    private static final int GAME_HEIGHT = 20; // 실제 블록이 놓이는 높이
    private static final int GAME_WIDTH = 10; // 실제 블록이 놓이는 너비
    private static final int CELL_SIZE = 30; // 각 셀의 픽셀 크기

    // 다음 블록 미리보기 관련 상수
    private static final int PREVIEW_SIZE = 4; // 미리보기 영역 크기 (4x4)
    private static final int PREVIEW_CELL_SIZE = 20; // 미리보기 셀 크기

    private final BoardManager boardManager; // 보드 관리자
    private BlockManager blockManager; // 블록 관리자
    private final InputHandler inputHandler; // 입력 처리자
    private final GameStateManager gameStateManager; // 게임 상태 관리자
    private RenderManager renderManager; // 렌더링 관리자
    private final TimerManager timerManager; // 타이머 관리자
    private final UIManager uiManager; // UI 관리자
    
    // 점수 관리자
    private final ScoreManager scoreManager;
    
    // 게임 오버 시 마지막 블록 정보 저장
    private Block lastBlock = null;
    private int lastBlockX = 0;
    private int lastBlockY = 0;
    
    // 블록 흔들림 효과 관리자
    private BlockShake blockShake;

    // ─────────────────────────────────────────────────────────────
    // Scene lifecycle
    // ─────────────────────────────────────────────────────────────

    public GameScene(JFrame frame) {
        super(frame);
        m_frame = frame;
        scoreManager = new ScoreManager();
        boardManager = new BoardManager(); // BoardManager 초기화
        gameStateManager = new GameStateManager(this); // GameStateManager 초기화
        timerManager = new TimerManager(gameStateManager, scoreManager); // TimerManager 초기화
        uiManager = new UIManager(); // UIManager 초기화
        inputHandler = new InputHandler(frame, this); // InputHandler 초기화
        // GamePlayManager는 initGameState에서 초기화 (BlockManager가 필요하므로)
        // 여기서 setContentPane 제거 - Scene 전환 시 처리하도록
    }

    @Override
    public void onEnter() {
        // Scene이 활성화될 때마다 초기화
        initUI();
        initGameState();
        
        timerManager.startTimers();
    }

    @Override
    public void onExit() {
        timerManager.stopTimers();
        if (blockShake != null) blockShake.cleanup(); // 흔들림 효과 정리
    }

    private void initUI() {
        // UIManager를 사용하여 UI 초기화
        uiManager.initializeUI(this, m_frame, inputHandler);
        
        // GamePanel을 내부 클래스로 교체
        GamePanel gamePanel = new GamePanel();
        uiManager.replaceGamePanel(gamePanel);
        
        // TimerManager 초기화
        timerManager.initialize(this);
        timerManager.setupSpeedUp();
        timerManager.setupLineBlinkEffect();
        
        // 포커스 요청
        uiManager.requestFocus(this);
    }
    
    /**
     * 게임 패널을 가져옵니다.
     */
    private JPanel getGamePanel() {
        return uiManager.getGamePanel();
    }
    
    /**
     * 게임 패널을 다시 그립니다.
     */
    private void repaintGamePanel() {
        JPanel gamePanel = getGamePanel();
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }

    private void initGameState() {
        boardManager.reset(); // BoardManager를 사용하여 보드 초기화
        
        // BlockManager 생성 및 초기화
        blockManager = new BlockManager(GAME_WIDTH, GAME_HEIGHT, boardManager);
        
        // 속도 조정 관리자가 있으면 BlockManager에 설정
        if (timerManager.getSpeedUp() != null) {
            blockManager.setSpeedUp(timerManager.getSpeedUp());
        }
        
        blockManager.initializeBlocks();
        
        // RenderManager 초기화
        renderManager = new RenderManager(
            GAME_WIDTH, GAME_HEIGHT, CELL_SIZE, PREVIEW_SIZE, PREVIEW_CELL_SIZE,
            boardManager, blockManager, gameStateManager, scoreManager
        );
        
        // 점수 초기화
        scoreManager.reset();
        
        // GameStateManager 초기화
        gameStateManager.reset();
        
        // TimerManager 속도 리셋
        timerManager.resetSpeed();
        
        // 블록 흔들림 효과 초기화
        if (blockShake != null) {
            blockShake.cleanup();
        }
        blockShake = new BlockShake(new BlockShake.ShakeCallback() {
            @Override
            public void onShakeUpdate() {
                JPanel gamePanel = getGamePanel();
                if (gamePanel != null) {
                    gamePanel.repaint();
                }
            }
            
            @Override
            public void onShakeComplete() {
                JPanel gamePanel = getGamePanel();
                if (gamePanel != null) {
                    gamePanel.repaint();
                }
            }
        });
        
        // placeBlock() 호출 제거
        JPanel gamePanel = getGamePanel();
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // InputHandler.InputCallback 구현
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onGameAction(InputHandler.GameAction action) {
        switch (action) {
            case MOVE_LEFT:
                moveBlockLeft();
                break;
            case MOVE_RIGHT:
                moveBlockRight();
                break;
            case MOVE_DOWN:
                moveBlockDown();
                break;
            case ROTATE:
                rotateBlock();
                break;
            case HARD_DROP:
                executeHardDrop();
                break;
            case PAUSE:
                gameStateManager.togglePause(); // GameStateManager 사용
                break;
            case HOLD:
                // Hold 기능이 구현되어 있다면 여기에 연결
                // holdBlock(); // 예시 - 실제 구현에 따라 달라질 수 있음
                break;
            case EXIT_TO_MENU:
                // InputHandler에서 직접 처리됨
                break;
        }
    }
    
    @Override
    public boolean isGameOver() {
        return gameStateManager.isGameOver();
    }
    
    @Override
    public boolean isPaused() {
        return gameStateManager.isPaused();
    }
    
    @Override
    public void repaintGame() {
        repaintGamePanel();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 블록 이동 메서드들 (GamePlayManager에서 이동)
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * 블록을 왼쪽으로 이동시킵니다.
     */
    private void moveBlockLeft() {
        if (gameStateManager.isGameOver()) return;
        
        blockManager.moveLeft();
        repaintGamePanel();
    }
    
    /**
     * 블록을 오른쪽으로 이동시킵니다.
     */
    private void moveBlockRight() {
        if (gameStateManager.isGameOver()) return;
        
        blockManager.moveRight();
        repaintGamePanel();
    }
    
    /**
     * 블록을 아래로 이동시킵니다.
     * 
     * @return 블록이 배치되었으면 true
     */
    private boolean moveBlockDown() {
        // 게임이 종료된 상태라면 블록 이동은 하지 않음
        if (blockManager.isGameOver()) return false;
        
        boolean blockPlaced = blockManager.moveDown();
        
        if (blockPlaced) {
            handleBlockPlaced();
        }
        
        return blockPlaced;
    }
    
    /**
     * 블록을 회전시킵니다.
     */
    private void rotateBlock() {
        if (gameStateManager.isGameOver()) return;
        
        if (blockManager.getCurrentBlock() != null) {
            if (blockManager.canRotate()) {
                blockManager.rotateBlock();
                repaintGamePanel();
            } else {
                // 회전할 수 없을 때 흔들림 효과 시작
                if (blockShake != null) {
                    blockShake.startShake();
                }
            }
        }
    }
    
    /**
     * 하드 드롭을 실행합니다.
     * 
     * @return 블록이 배치되었으면 true
     */
    private boolean executeHardDrop() {
        if (gameStateManager.isGameOver()) return false;
        
        boolean blockPlaced = blockManager.executeHardDrop();
        
        if (blockPlaced) {
            handleBlockPlaced();
            repaintGamePanel();
        }
        
        return blockPlaced;
    }
    
    /**
     * 블록이 배치된 후 후속 처리를 수행합니다.
     */
    private void handleBlockPlaced() {
        // 블록이 고정되었을 때 게임 오버 체크
        if (blockManager.isGameOver()) {
            handleGameOver();
            return;
        }
        
        // 완성된 줄 확인 및 제거
        checkAndClearLines();
        
        // 보드 상태 출력 (디버그용)
        boardManager.printBoard();
        
        repaintGamePanel();
    }
    
    /**
     * 완성된 줄을 찾아서 제거 연출을 시작합니다.
     */
    private void checkAndClearLines() {
        java.util.List<Integer> completedLines = new java.util.ArrayList<>();
        
        // BoardManager를 사용하여 완성된 줄들을 찾습니다
        for (int row = 0; row < boardManager.getHeight(); row++) {
            if (boardManager.isLineFull(row)) {
                completedLines.add(row);
            }
        }
        
        if (!completedLines.isEmpty()) {
            // 줄 점멸 연출 시작 (타이머는 계속 실행하되 블록 이동만 일시정지)
            timerManager.getLineBlinkEffect().startBlinkEffect(completedLines);
        }
    }
    
    /**
     * 연출이 끝난 후 실제로 줄을 삭제합니다.
     */
    private void executeLineDeletion() {
        System.out.println("=== EXECUTING LINE DELETION ===");
        int linesClearedThisTurn = boardManager.clearCompletedLines(); // BoardManager 사용
        System.out.println("Lines cleared this turn: " + linesClearedThisTurn);
        
        if (linesClearedThisTurn > 0) {
            // 점수 업데이트 (ScoreManager 사용)
            scoreManager.addScore(linesClearedThisTurn);
            
            // 줄 삭제 수 증가 (SpeedUp 관리자 사용)
            if (timerManager.getSpeedUp() != null) {
                timerManager.getSpeedUp().onLinesCleared(linesClearedThisTurn);
            }
        }
        System.out.println("=== LINE DELETION COMPLETED ===");
    }
    
    /**
     * 게임 오버를 처리합니다.
     */
    private void handleGameOver() {
        System.out.println("GameScene: Handling game over");
        
        // 게임 종료 상태 설정
        gameStateManager.triggerGameOver();
        
        // 타이머 정지 (블록은 그대로 두고 움직임만 중단)
        timerManager.stopTimers();
    }

    // ═══════════════════════════════════════════════════════════════
    // GameStateManager.StateChangeCallback 구현
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onStateChanged(GameStateManager.GameState oldState, GameStateManager.GameState newState) {
        System.out.println("GameScene: State changed from " + oldState + " to " + newState);
        
        // 상태 변경에 따른 UI 업데이트
        repaintGamePanel();
    }
    
    @Override
    public void onGameOver() {
        System.out.println("GameScene: Game Over triggered from GameStateManager");
        
        // 마지막 블록 정보 저장 (게임 오버 후에도 화면에 보이도록)
        if (blockManager.getCurrentBlock() != null) {
            lastBlock = blockManager.getCurrentBlock();
            lastBlockX = blockManager.getX();
            lastBlockY = blockManager.getY();
        }
        
        // 게임 종료 오버레이 표시
        showGameOverOverlay();
    }
    
    /**
     * 게임 오버 오버레이를 표시합니다.
     */
    private void showGameOverOverlay() {
        int currentScore = scoreManager.getScore();
        int currentLines = scoreManager.getLinesCleared();
        int currentTime = gameStateManager.getElapsedTimeInSeconds(); // GameStateManager 사용
        String difficulty = "Normal"; // 현재 난이도 설정
        
        GameOver gameOverOverlay = new GameOver(m_frame, currentScore, currentLines, currentTime, difficulty);
        
        // 게임 종료 화면을 현재 패널에 추가
        setLayout(new OverlayLayout(this));
        add(gameOverOverlay, 0); // 맨 앞에 추가
        
        revalidate();
        repaint();
        
        System.out.println("Game Over! Score: " + currentScore + ", Lines: " + currentLines);
    }
    
    @Override
    public void onPauseToggled(boolean isPaused) {
        System.out.println("GameScene: Pause toggled - isPaused: " + isPaused);
        
        // 일시정지 상태에 따른 UI 업데이트
        repaintGamePanel();
    }
    
    /**
     * 현재까지의 경과 시간을 초 단위로 반환합니다.
     * @return 경과 시간 (초)
     */
    private int getElapsedTimeInSeconds() {
        return gameStateManager.getElapsedTimeInSeconds();
    }

    // ─────────────────────────────────────────────────────────────
    // 커스텀 패널 클래스
    // ─────────────────────────────────────────────────────────────
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // RenderManager를 사용하여 모든 렌더링 처리
            if (renderManager != null) {
                renderManager.render(g2d, getWidth(), getHeight(), timerManager.getLineBlinkEffect(), 
                                   lastBlock, lastBlockX, lastBlockY);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TimerManager.TimerCallback 구현
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void onDropTick() {
        moveBlockDown();
    }
    
    @Override
    public void onBlinkTick() {
        repaintGamePanel();
    }
    
    @Override
    public void onLineDeletion() {
        executeLineDeletion();
    }
}
