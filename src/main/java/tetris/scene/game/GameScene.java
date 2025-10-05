package tetris.scene.game;

import tetris.Game;
import tetris.GameSettings;
import tetris.ColorBlindHelper;
import tetris.scene.Scene;
import tetris.scene.game.blocks.*;
import tetris.scene.game.blocks.BlockHardDrop;
import tetris.scene.game.overlay.GameOver;
import tetris.scene.game.overlay.ScoreManager;
import tetris.scene.menu.MainMenuScene;
import tetris.util.SpeedUp;
import tetris.util.LineBlinkEffect;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.OverlayLayout;
import java.awt.event.KeyListener;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import tetris.Game;
import tetris.scene.Scene;
import tetris.scene.game.blocks.*;

public class GameScene extends Scene {
    private JFrame m_frame;
    private static final int GAME_HEIGHT = 20; // 실제 블록이 놓이는 높이
    private static final int GAME_WIDTH = 10; // 실제 블록이 놓이는 너비
    private static final int CELL_SIZE = 30; // 각 셀의 픽셀 크기
    private static final int INIT_INTERVAL_MS = 1000; // 블록 드롭 초기 속도 (밀리초)

    // 다음 블록 미리보기 관련 상수
    private static final int PREVIEW_SIZE = 4; // 미리보기 영역 크기 (4x4)
    private static final int PREVIEW_CELL_SIZE = 20; // 미리보기 셀 크기

    private GamePanel gamePanel; // 커스텀 게임 패널
    private int[][] board; // 게임 보드 상태 (0: 빈칸, 1: 블록 있음)
    private Color[][] boardColors; // 각 셀의 색상 정보 추가
    
    private Timer timer; // 블록 드롭 타이머
    private Timer blinkTimer; // 점멸 효과 전용 타이머 (50ms 주기)
    private Block curr; // 현재 떨어지고 있는 블록
    private Block next; // 다음 블록 추가
    private int x = 3; // 현재 블록의 x 위치
    private int y = 0; // 현재 블록의 y 위치

    // 점수 관리자
    private ScoreManager scoreManager;

    private boolean initialized = false;
    
    // 일시정지 상태
    private boolean isPaused = false;
    
    // 게임 종료 상태
    private boolean isGameOver = false;
    
    // 게임 오버 시 마지막 블록 정보 저장
    private Block lastBlock = null;
    private int lastBlockX = 0;
    private int lastBlockY = 0;
    
    // 시간 추적 변수들
    private long gameStartTime;
    private long pausedTotalTime = 0;
    private long pauseStartTime = 0;
    
    // 블록 흔들림 효과 관리자
    private BlockShake blockShake;
    
    // 속도 조정 관리자
    private SpeedUp speedUp;
    
    // 줄 점멸 연출 관리자
    private LineBlinkEffect lineBlinkEffect;

    // ─────────────────────────────────────────────────────────────
    // Scene lifecycle
    // ─────────────────────────────────────────────────────────────

    public GameScene(JFrame frame) {
        super(frame);
        m_frame = frame;
        scoreManager = new ScoreManager();
        // 여기서 setContentPane 제거 - Scene 전환 시 처리하도록
    }

    @Override
    public void onEnter() {
        // Scene이 활성화될 때마다 초기화
        initUI();
        initGameState();
        
        // 프레임에 이 Scene을 설정
        m_frame.setContentPane(this);
        m_frame.revalidate();
        m_frame.repaint();
        
        // 창을 화면 중앙에 위치시키기
        m_frame.setLocationRelativeTo(null);
        
        requestFocusInWindow();
        if (timer != null && !timer.isRunning()) timer.start();
        if (blinkTimer != null && !blinkTimer.isRunning()) blinkTimer.start();
    }

    @Override
    public void onExit() {
        if (timer != null) timer.stop();
        if (blinkTimer != null) blinkTimer.stop();
        if (blockShake != null) blockShake.cleanup(); // 흔들림 효과 정리
    }

    private void initUI() {
        // 기존 컴포넌트들 제거
        removeAll();
        
        setLayout(new BorderLayout());
        
        // 색맹 모드에 따른 배경색 적용
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        Color backgroundColor = ColorBlindHelper.getBackgroundColor(colorBlindMode);
        Color borderColor = ColorBlindHelper.getBorderColor(colorBlindMode);
        
        setBackground(backgroundColor);

        gamePanel = new GamePanel();
        // 다음 블록 미리보기 공간을 포함한 크기로 조정
        int previewWidth = PREVIEW_SIZE * PREVIEW_CELL_SIZE + 40; // 여백 포함
        gamePanel.setPreferredSize(new Dimension(
            (GAME_WIDTH + 2) * CELL_SIZE + previewWidth,
            (GAME_HEIGHT + 2) * CELL_SIZE
        ));
        gamePanel.setBackground(Color.BLACK);
        
        // 게임 패널을 중앙에 배치하기 위한 래퍼 패널 생성
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(backgroundColor);
        wrapperPanel.add(gamePanel, new GridBagConstraints());
        
        add(wrapperPanel, BorderLayout.CENTER);

        // 메인 패널(this)에 키 리스너 추가
        addKeyListener(new PlayerKeyListener());
        setFocusable(true);

        // 드롭 타이머
        if (timer != null) timer.stop();
        timer = new Timer(INIT_INTERVAL_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 점멸 연출 중이 아니고, 일시정지나 게임 종료 상태가 아닐 때만 블록 이동
                boolean isBlinking = (lineBlinkEffect != null && lineBlinkEffect.isActive());
                if (!isPaused && !isGameOver && !isBlinking) {
                    moveDown();
                }
            }
        });
        
        // 점멸 효과 전용 타이머 (50ms 주기로 빠른 업데이트)
        if (blinkTimer != null) blinkTimer.stop();
        blinkTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 게임오버 상태가 아닐 때만 점멸 업데이트 및 화면 갱신
                if (!isGameOver) {
                    // 줄 점멸 연출 업데이트
                    if (lineBlinkEffect != null) {
                        lineBlinkEffect.update();
                    }
                    gamePanel.repaint(); // 화면 갱신
                }
            }
        });
        blinkTimer.start(); // 점멸 타이머는 항상 실행
        
        // 속도 조정 관리자 초기화 (점수 배율 증가 콜백 포함)
        speedUp = new SpeedUp(timer, new SpeedUp.SpeedIncreaseCallback() {
            @Override
            public void onSpeedIncrease() {
                // 속도가 증가할 때마다 점수 배율도 증가
                scoreManager.onSpeedIncrease();
            }
        });
        
        // 줄 점멸 연출 관리자 초기화
        lineBlinkEffect = new LineBlinkEffect(new LineBlinkEffect.BlinkEffectCallback() {
            @Override
            public void onBlinkComplete() {
                // 연출이 끝나면 실제로 줄을 삭제 (타이머는 자동으로 재개됨)
                executeLineDeletion();
            }
            
            @Override
            public void onEffectUpdate() {
                // 연출 업데이트 시 화면 갱신
                if (gamePanel != null) {
                    gamePanel.repaint();
                }
            }
        });
    }

    private void initGameState() {
        board = new int[GAME_HEIGHT][GAME_WIDTH];
        boardColors = new Color[GAME_HEIGHT][GAME_WIDTH]; // 색상 배열 초기화
        curr = getRandomBlock();
        next = getRandomBlock(); // 다음 블록 초기화
        x = 3;
        y = 0;
        // 점수 초기화
        scoreManager.reset();
        
        // 게임 상태 초기화
        isGameOver = false;
        
        // 시간 추적 초기화
        gameStartTime = System.currentTimeMillis();
        pausedTotalTime = 0;
        pauseStartTime = 0;
        
        // 타이머 딜레이를 초기값으로 리셋
        if (timer != null) {
            timer.setDelay(INIT_INTERVAL_MS);
        }
        
        // 속도 조정 관리자 초기화
        if (speedUp != null) {
            speedUp.reset();
        }
        
        // 블록 흔들림 효과 초기화
        if (blockShake != null) {
            blockShake.cleanup();
        }
        blockShake = new BlockShake(new BlockShake.ShakeCallback() {
            @Override
            public void onShakeUpdate() {
                if (gamePanel != null) {
                    gamePanel.repaint();
                }
            }
            
            @Override
            public void onShakeComplete() {
                if (gamePanel != null) {
                    gamePanel.repaint();
                }
            }
        });
        
        // placeBlock() 호출 제거
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }

    private Block getRandomBlock() {
        Random rnd = new Random(System.currentTimeMillis());
        int block = rnd.nextInt(7); // 0~6
        
        // 블록 생성 수 증가 (SpeedUp 관리자 사용)
        if (speedUp != null) {
            speedUp.onBlockGenerated(isGameOver);
        }
        
        switch (block) {
            case 0: return new IBlock();
            case 1: return new JBlock();
            case 2: return new LBlock();
            case 3: return new ZBlock();
            case 4: return new SBlock();
            case 5: return new TBlock();
            case 6: return new OBlock();
        }
        return new LBlock();
    }

    private void placeBlockPermanently() {
        System.out.println("Placing block permanently at x=" + x + ", y=" + y);
        
        // 블록을 영구적으로 보드에 고정
        for (int j = 0; j < curr.height(); j++) {
            for (int i = 0; i < curr.width(); i++) {
                if (y + j >= 0 && y + j < GAME_HEIGHT && x + i >= 0 && x + i < GAME_WIDTH) {
                    if (curr.getShape(i, j) == 1) {
                        board[y + j][x + i] = 1;
                        boardColors[y + j][x + i] = curr.getColor();
                        System.out.println("Placed block at board[" + (y + j) + "][" + (x + i) + "] with color " + curr.getColor());
                    }
                }
            }
        }
        
        // 게임 종료 조건 확인: 블록이 천장(y=0 위치)에 고정되면 게임 종료
        if (y <= 0) {
            triggerGameOver();
            return;
        }
        
        // 완성된 줄 확인 및 제거
        clearCompletedLines();
        
        printBoard();
    }

    // 완성된 줄을 찾아서 제거하고 위의 블록들을 내리는 메서드 (수정됨)
    private void clearCompletedLines() {
        List<Integer> completedLines = new ArrayList<>();
        
        // 완성된 줄들을 찾습니다
        for (int row = 0; row < GAME_HEIGHT; row++) {
            if (isLineFull(row)) {
                completedLines.add(row);
            }
        }
        
        if (!completedLines.isEmpty()) {
            // 줄 점멸 연출 시작 (타이머는 계속 실행하되 블록 이동만 일시정지)
            lineBlinkEffect.startBlinkEffect(completedLines);
        }
    }
    
    /**
     * 연출이 끝난 후 실제로 줄을 삭제하는 메서드
     */
    private void executeLineDeletion() {
        System.out.println("=== EXECUTING LINE DELETION ===");
        int linesClearedThisTurn = clearAllCompletedLines(); // 개선된 메서드 사용
        System.out.println("Lines cleared this turn: " + linesClearedThisTurn);
        
        if (linesClearedThisTurn > 0) {
            // 점수 업데이트 (ScoreManager 사용)
            scoreManager.addScore(linesClearedThisTurn);
            
            // 줄 삭제 수 증가 (SpeedUp 관리자 사용)
            if (speedUp != null) {
                speedUp.onLinesCleared(linesClearedThisTurn);
            }
        }
        System.out.println("=== LINE DELETION COMPLETED ===");
    }

    // 특정 줄이 완전히 채워져 있는지 확인
    private boolean isLineFull(int row) {
        for (int col = 0; col < GAME_WIDTH; col++) {
            if (board[row][col] == 0) {
                return false; // 빈 칸이 하나라도 있으면 완성되지 않음
            }
        }
        return true; // 모든 칸이 채워져 있음
    }

    // 특정 줄을 제거하고 위의 모든 줄들을 한 칸씩 아래로 이동
    private void removeLine(int lineToRemove) {
        System.out.println("Removing line: " + lineToRemove);
        
        // 제거할 줄부터 위쪽으로 모든 줄을 한 칸씩 아래로 이동
        for (int row = lineToRemove; row > 0; row--) {
            for (int col = 0; col < GAME_WIDTH; col++) {
                // 위 줄의 데이터를 현재 줄로 복사
                board[row][col] = board[row - 1][col];
                boardColors[row][col] = boardColors[row - 1][col];
            }
        }
        
        // 맨 위 줄은 빈 줄로 설정
        for (int col = 0; col < GAME_WIDTH; col++) {
            board[0][col] = 0;
            boardColors[0][col] = null;
        }
    }

    // 여러 줄이 동시에 완성된 경우를 위한 개선된 버전 (기존 코드에서 변수명 수정)
    private int clearAllCompletedLines() {
        int linesClearedCount = 0;
        boolean[] linesToClear = new boolean[GAME_HEIGHT];
        
        // 1단계: 완성된 모든 줄 찾기
        for (int row = 0; row < GAME_HEIGHT; row++) {
            if (isLineFull(row)) {
                linesToClear[row] = true;
                linesClearedCount++;
                System.out.println("Line " + row + " is complete and will be cleared.");
            }
        }
        
        // 2단계: 완성된 줄들 제거 및 블록들 재배치
        if (linesClearedCount > 0) {
            int writeRow = GAME_HEIGHT - 1; // 새로 배치할 위치
            
            // 아래에서 위로 올라가면서 완성되지 않은 줄들만 복사
            for (int readRow = GAME_HEIGHT - 1; readRow >= 0; readRow--) {
                if (!linesToClear[readRow]) {
                    // 완성되지 않은 줄이면 아래쪽으로 이동
                    if (writeRow != readRow) {
                        System.out.println("Moving line " + readRow + " to line " + writeRow);
                    }
                    for (int col = 0; col < GAME_WIDTH; col++) {
                        board[writeRow][col] = board[readRow][col];
                        boardColors[writeRow][col] = boardColors[readRow][col];
                    }
                    writeRow--;
                } else {
                    System.out.println("Skipping completed line " + readRow);
                }
            }
            
            // 위쪽의 남은 줄들은 빈 줄로 설정
            while (writeRow >= 0) {
                for (int col = 0; col < GAME_WIDTH; col++) {
                    board[writeRow][col] = 0;
                    boardColors[writeRow][col] = null;
                }
                writeRow--;
            }
            
            System.out.println("Cleared " + linesClearedCount + " lines simultaneously!");
        }
        
        return linesClearedCount;
    }

    private void printBoard() {
        System.out.println("Current board state:");
        for (int i = 0; i < GAME_HEIGHT; i++) {
            for (int j = 0; j < GAME_WIDTH; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("---");
    }

    private boolean canMoveDown() {
        if (y + curr.height() >= GAME_HEIGHT) return false;
        for (int j = 0; j < curr.height(); j++) {
            for (int i = 0; i < curr.width(); i++) {
                if (curr.getShape(i, j) == 1) {
                    int newY = y + j + 1;
                    int newX = x + i;
                    if (newY >= GAME_HEIGHT || (newY >= 0 && newX >= 0 && newX < GAME_WIDTH && board[newY][newX] == 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean canMoveLeft() {
        for (int j = 0; j < curr.height(); j++) {
            for (int i = 0; i < curr.width(); i++) {
                if (curr.getShape(i, j) == 1) {
                    int newX = x + i - 1;
                    int newY = y + j;
                    if (newX < 0 || (newY >= 0 && newY < GAME_HEIGHT && board[newY][newX] == 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean canMoveRight() {
        for (int j = 0; j < curr.height(); j++) {
            for (int i = 0; i < curr.width(); i++) {
                if (curr.getShape(i, j) == 1) {
                    int newX = x + i + 1;
                    int newY = y + j;
                    if (newX >= GAME_WIDTH || (newY >= 0 && newY < GAME_HEIGHT && board[newY][newX] == 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean canRotate() {
        return BlockRotation.canRotate(curr, x, y, board, GAME_WIDTH, GAME_HEIGHT);
    }

    private void moveDown() {
        if (curr == null) return;
        
        // 게임이 종료된 상태라면 블록 이동은 하지 않음
        if (isGameOver) return;
        
        if (canMoveDown()) {
            y++;
        } else {
            // 블록이 바닥에 닿았을 때만 보드에 고정
            placeBlockPermanently();
            
            // 게임이 종료되지 않은 경우에만 다음 블록 생성
            if (!isGameOver) {
                // 다음 블록을 현재 블록으로, 새로운 다음 블록 생성
                curr = next;
                next = getRandomBlock();
                x = 3;
                y = 0;
            }
        }
    }

    private void moveRight() {
        if (curr == null) return;
        
        if (canMoveRight()) {
            x++;
        }
    }

    private void moveLeft() {
        if (curr == null) return;
        
        if (canMoveLeft()) {
            x--;
        }
    }

    /**
     * 하드 드롭: 현재 블록을 가능한 한 아래까지 즉시 떨어뜨립니다.
     */
    private void hardDrop() {
        if (curr == null) return;
        
        // BlockHardDrop 클래스를 사용하여 하드 드롭 실행
        int newY = BlockHardDrop.executeHardDrop(curr, x, y, board, GAME_WIDTH, GAME_HEIGHT);
        y = newY;
        
        // 블록이 바닥에 닿았으므로 즉시 고정
        placeBlockPermanently();
        
        // 다음 블록을 현재 블록으로, 새로운 다음 블록 생성
        curr = next;
        next = getRandomBlock();
        x = 3;
        y = 0;
        
        // 화면 업데이트
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }

    /**
     * 게임 일시정지 상태를 토글합니다.
     */
    private void togglePause() {
        isPaused = !isPaused;
        System.out.println("Game " + (isPaused ? "PAUSED" : "RESUMED"));
        
        // 일시정지 시간 추적
        if (isPaused) {
            // 일시정지 시작 시간 기록
            pauseStartTime = System.currentTimeMillis();
        } else {
            // 일시정지 해제 시 총 일시정지 시간에 추가
            if (pauseStartTime > 0) {
                pausedTotalTime += System.currentTimeMillis() - pauseStartTime;
                pauseStartTime = 0;
            }
        }
        
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }
    
    /**
     * 현재까지의 경과 시간을 초 단위로 반환합니다.
     * @return 경과 시간 (초)
     */
    private int getElapsedTimeInSeconds() {
        if (gameStartTime == 0) return 0;
        
        long currentTime = System.currentTimeMillis();
        long totalElapsed = currentTime - gameStartTime;
        
        // 현재 일시정지 상태인 경우
        if (isPaused && pauseStartTime > 0) {
            totalElapsed -= (currentTime - pauseStartTime);
        }
        
        // 총 일시정지 시간 제외
        totalElapsed -= pausedTotalTime;
        
        return (int) (totalElapsed / 1000);
    }
    
    /**
     * 시간을 MM:SS 형식으로 포맷팅합니다.
     * @param seconds 초 단위 시간
     * @return MM:SS 형식의 문자열
     */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    // ─────────────────────────────────────────────────────────────
    // 커스텀 패널 클래스
    // ─────────────────────────────────────────────────────────────
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 전체 배경을 검은색으로
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // 외부 경계 영역 (회색으로 변경하고 크기 수정)
            g2d.setColor(Color.GRAY); // 빨간색에서 회색으로 변경
            // 왼쪽 경계
            g2d.fillRect(0, 0, CELL_SIZE, (GAME_HEIGHT + 2) * CELL_SIZE);
            // 오른쪽 경계
            g2d.fillRect((GAME_WIDTH + 1) * CELL_SIZE, 0, CELL_SIZE, (GAME_HEIGHT + 2) * CELL_SIZE);
            // 위쪽 경계
            g2d.fillRect(0, 0, (GAME_WIDTH + 2) * CELL_SIZE, CELL_SIZE);
            // 아래쪽 경계
            g2d.fillRect(0, (GAME_HEIGHT + 1) * CELL_SIZE, (GAME_WIDTH + 2) * CELL_SIZE, CELL_SIZE);

            // 게임 영역 배경 (어두운 회색)
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(CELL_SIZE, CELL_SIZE, GAME_WIDTH * CELL_SIZE, GAME_HEIGHT * CELL_SIZE);

            // 게임 영역 경계선 (흰색 테두리)
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(CELL_SIZE - 1, CELL_SIZE - 1, GAME_WIDTH * CELL_SIZE + 1, GAME_HEIGHT * CELL_SIZE + 1);

            // 그리드 라인 그리기 (얇은 회색 선)
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(1));
            
            // 세로 그리드 라인
            for (int i = 1; i < GAME_WIDTH; i++) {
                int gridX = (i + 1) * CELL_SIZE;
                g2d.drawLine(gridX, CELL_SIZE, gridX, (GAME_HEIGHT + 1) * CELL_SIZE);
            }
            
            // 가로 그리드 라인
            for (int i = 1; i < GAME_HEIGHT; i++) {
                int gridY = (i + 1) * CELL_SIZE;
                g2d.drawLine(CELL_SIZE, gridY, (GAME_WIDTH + 1) * CELL_SIZE, gridY);
            }

            // 게임 보드 그리기 (고정된 블록들)
            // System.out.println("Rendering fixed blocks..."); // 디버그 (주석 처리)
            
            for (int row = 0; row < GAME_HEIGHT; row++) {
                // 점멸 중인 줄은 LineBlinkEffect에서 처리하므로 건너뜁니다
                if (lineBlinkEffect != null && lineBlinkEffect.isActive() && lineBlinkEffect.isLineBlinking(row)) {
                    continue;
                }
                
                for (int col = 0; col < GAME_WIDTH; col++) {
                    if (board[row][col] == 1) {
                        Color blockColor = boardColors[row][col];
                        
                        // System.out.println("Found fixed block at [" + row + "][" + col + "] color=" + blockColor); // 디버그 (주석 처리)
                        
                        if (blockColor != null) {
                            g2d.setColor(blockColor);
                            g2d.fillRect((col + 1) * CELL_SIZE + 1, (row + 1) * CELL_SIZE + 1, 
                                        CELL_SIZE - 2, CELL_SIZE - 2);
                        
                            // 블록 테두리
                            g2d.setColor(Color.BLACK);
                            g2d.setStroke(new BasicStroke(1));
                            g2d.drawRect((col + 1) * CELL_SIZE + 1, (row + 1) * CELL_SIZE + 1, 
                                        CELL_SIZE - 2, CELL_SIZE - 2);
                            
                            // System.out.println("Drew fixed block at screen pos: " + 
                            //                  ((col + 1) * CELL_SIZE + 1) + "," + ((row + 1) * CELL_SIZE + 1)); // 디버그 (주석 처리)
                        } else {
                            // System.out.println("Block color is null!"); // 디버그 (주석 처리)
                        }
                    }
                }
            }
            
            // 줄 점멸 연출 그리기
            if (lineBlinkEffect != null && lineBlinkEffect.isActive()) {
                lineBlinkEffect.draw(g2d, CELL_SIZE, GAME_WIDTH, board, boardColors);
            }

            // 고스트 블록 그리기 (하드드롭 미리보기)
            if (curr != null && !isGameOver) {
                int ghostY = tetris.scene.game.blocks.BlockHardDrop.calculateGhostPosition(curr, x, y, board, GAME_WIDTH, GAME_HEIGHT);
                
                // 고스트 블록이 현재 블록과 다른 위치에 있을 때만 그리기
                if (ghostY != y) {
                    // 반투명한 색상으로 고스트 블록 그리기
                    Color originalColor = curr.getColor();
                    Color ghostColor = new Color(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), 80); // 투명도 80
                    g2d.setColor(ghostColor);
                    
                    for (int blockRow = 0; blockRow < curr.height(); blockRow++) {
                        for (int blockCol = 0; blockCol < curr.width(); blockCol++) {
                            if (curr.getShape(blockCol, blockRow) == 1) {
                                int drawX = (x + blockCol + 1) * CELL_SIZE + 1;
                                int drawY = (ghostY + blockRow + 1) * CELL_SIZE + 1;
                                
                                // 고스트 블록은 외곽선만 그리기
                                g2d.setStroke(new BasicStroke(2));
                                g2d.drawRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
                            }
                        }
                    }
                }
            }

            // 현재 블록 그리기 (게임 오버 시에는 마지막 블록 그리기)
            Block blockToDraw = curr;
            int blockX = x;
            int blockY = y;
            
            // 게임 오버 상태이고 마지막 블록이 저장되어 있다면 마지막 블록 그리기
            if (isGameOver && lastBlock != null) {
                blockToDraw = lastBlock;
                blockX = lastBlockX;
                blockY = lastBlockY;
            }
            
            if (blockToDraw != null) {
                g2d.setColor(blockToDraw.getColor());
                for (int blockRow = 0; blockRow < blockToDraw.height(); blockRow++) {
                    for (int blockCol = 0; blockCol < blockToDraw.width(); blockCol++) {
                        if (blockToDraw.getShape(blockCol, blockRow) == 1) {
                            int drawX = (blockX + blockCol + 1) * CELL_SIZE + 1;
                            int drawY = (blockY + blockRow + 1) * CELL_SIZE + 1;
                            
                            // 흔들림 효과 적용 (현재 블록이고 흔들리는 중일 때만)
                            if (blockToDraw == curr && blockShake != null && blockShake.isShaking()) {
                                drawX += blockShake.getShakeOffsetX();
                                drawY += blockShake.getShakeOffsetY();
                            }
                            
                            g2d.fillRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
                            
                            // 현재 블록 테두리
                            g2d.setColor(Color.WHITE);
                            g2d.setStroke(new BasicStroke(2));
                            g2d.drawRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
                            g2d.setColor(blockToDraw.getColor());
                        }
                    }
                }
            }

            // 다음 블록 미리보기 그리기
            drawNextBlockPreview(g2d);

            // 점수판 그리기 (ScoreManager 사용)
            drawScoreBoard(g2d);

            // 일시정지 오버레이 그리기
            if (isPaused) {
                drawPauseOverlay(g2d);
            }
        }

        private void drawNextBlockPreview(Graphics2D g2d) {
            if (next == null) return;

            // 미리보기 영역 위치 계산 (게임 보드 오른쪽)
            int previewX = (GAME_WIDTH + 2) * CELL_SIZE + 20;
            int previewY = CELL_SIZE + 20;
            int previewAreaSize = PREVIEW_SIZE * PREVIEW_CELL_SIZE;

            // 미리보기 영역 배경
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(previewX, previewY, previewAreaSize, previewAreaSize);

            // 미리보기 영역 테두리
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(previewX, previewY, previewAreaSize, previewAreaSize);

            // "NEXT" 라벨
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            String nextLabel = "NEXT";
            int labelWidth = fm.stringWidth(nextLabel);
            g2d.drawString(nextLabel, previewX + (previewAreaSize - labelWidth) / 2, previewY - 5);

            // 다음 블록을 중앙에 배치하기 위한 오프셋 계산
            int blockWidth = next.width();
            int blockHeight = next.height();
            int offsetX = (PREVIEW_SIZE - blockWidth) / 2;
            int offsetY = (PREVIEW_SIZE - blockHeight) / 2;

            // 다음 블록 그리기
            g2d.setColor(next.getColor());
            for (int blockRow = 0; blockRow < blockHeight; blockRow++) {
                for (int blockCol = 0; blockCol < blockWidth; blockCol++) {
                    if (next.getShape(blockCol, blockRow) == 1) {
                        int drawX = previewX + (offsetX + blockCol) * PREVIEW_CELL_SIZE + 2;
                        int drawY = previewY + (offsetY + blockRow) * PREVIEW_CELL_SIZE + 2;
                        
                        g2d.fillRect(drawX, drawY, PREVIEW_CELL_SIZE - 4, PREVIEW_CELL_SIZE - 4);
                        
                        // 블록 테두리
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new BasicStroke(1));
                        g2d.drawRect(drawX, drawY, PREVIEW_CELL_SIZE - 4, PREVIEW_CELL_SIZE - 4);
                        g2d.setColor(next.getColor());
                    }
                }
            }
        }

        private void drawScoreBoard(Graphics2D g2d) {
            // 점수판 위치 계산 (다음 블록 미리보기 아래)
            int previewX = (GAME_WIDTH + 2) * CELL_SIZE + 20;
            int previewY = CELL_SIZE + 20;
            int previewAreaSize = PREVIEW_SIZE * PREVIEW_CELL_SIZE;
            
            int scoreBoardX = previewX;
            int scoreBoardY = previewY + previewAreaSize + 30; // 미리보기 아래 30px 간격
            int scoreBoardWidth = previewAreaSize;
            int scoreBoardHeight = 120; // 점수판 높이

            // ScoreManager의 drawScoreBoard 메서드 사용
            scoreManager.drawScoreBoard(g2d, scoreBoardX, scoreBoardY, scoreBoardWidth, scoreBoardHeight);
            
            // 시간 표시 (점수판 아래)
            drawTimeBoard(g2d, scoreBoardX, scoreBoardY + scoreBoardHeight + 10, scoreBoardWidth, 50);
        }
        
        /**
         * 시간 정보를 표시하는 보드를 그립니다.
         */
        private void drawTimeBoard(Graphics2D g2d, int x, int y, int width, int height) {
            // 시간 보드 배경
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(x, y, width, height);

            // 시간 보드 테두리
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(x, y, width, height);

            // "TIME" 라벨
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String timeLabel = "TIME";
            int labelWidth = fm.stringWidth(timeLabel);
            g2d.drawString(timeLabel, x + (width - labelWidth) / 2, y + 20);

            // 현재 시간 표시
            int elapsedSeconds = getElapsedTimeInSeconds();
            String timeText = formatTime(elapsedSeconds);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            fm = g2d.getFontMetrics();
            int timeWidth = fm.stringWidth(timeText);
            g2d.drawString(timeText, x + (width - timeWidth) / 2, y + 40);
        }

        /**
         * 일시정지 오버레이를 그립니다.
         */
        private void drawPauseOverlay(Graphics2D g2d) {
            // 게임 영역에 반투명 오버레이
            g2d.setColor(new Color(0, 0, 0, 150)); // 반투명 검은색
            g2d.fillRect(CELL_SIZE, CELL_SIZE, GAME_WIDTH * CELL_SIZE, GAME_HEIGHT * CELL_SIZE);
            
            // PAUSED 텍스트
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics fm = g2d.getFontMetrics();
            String pausedText = "PAUSED";
            int textWidth = fm.stringWidth(pausedText);
            int textHeight = fm.getHeight();
            
            // 게임 영역 중앙에 텍스트 배치
            int gameAreaCenterX = CELL_SIZE + (GAME_WIDTH * CELL_SIZE) / 2;
            int gameAreaCenterY = CELL_SIZE + (GAME_HEIGHT * CELL_SIZE) / 2;
            
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
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Input
    // ─────────────────────────────────────────────────────────────
    private class PlayerKeyListener implements KeyListener {
        @Override public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            System.out.println("Key pressed: " + e.getKeyCode());
            
            GameSettings settings = GameSettings.getInstance();
            int keyCode = e.getKeyCode();
            
            // ESC 키는 항상 처리 (메인 메뉴로 이동)
            if (keyCode == KeyEvent.VK_ESCAPE) {
                Game.setScene(new MainMenuScene(m_frame));
                return;
            }
            
            // 게임이 종료된 상태일 때는 ESC를 제외한 모든 키 입력 무시
            if (isGameOver) {
                return;
            }
            
            // 일시정지 키 처리 (게임이 진행 중일 때만)
            if (keyCode == settings.getPauseKey()) {
                togglePause();
                return;
            }
            
            // 일시정지 상태일 때는 다른 키 입력 무시
            if (isPaused) {
                return;
            }
            
            // 사용자 설정 키에 따른 동작 처리
            if (keyCode == settings.getFallKey()) {
                moveDown();
                gamePanel.repaint();
            } else if (keyCode == settings.getRightKey()) {
                moveRight();
                gamePanel.repaint();
            } else if (keyCode == settings.getLeftKey()) {
                moveLeft();
                gamePanel.repaint();
            } else if (keyCode == settings.getRotateKey()) {
                if (curr != null) {
                    if (canRotate()) {
                        curr.rotate();
                        gamePanel.repaint();
                    } else {
                        // 회전할 수 없을 때 흔들림 효과 시작
                        if (blockShake != null) {
                            blockShake.startShake();
                        }
                    }
                }
            } else if (keyCode == settings.getDropKey()) {
                hardDrop();
            } else if (keyCode == settings.getHoldKey()) {
                // Hold 기능이 구현되어 있다면 여기에 연결
                // holdBlock(); // 예시 - 실제 구현에 따라 달라질 수 있음
            }
        }
        @Override public void keyReleased(KeyEvent e) {}
    }
    
    /**
     * 게임 종료를 처리하는 메서드
     */
    private void triggerGameOver() {
        // 게임 종료 상태 설정
        isGameOver = true;
        
        // 마지막 블록 정보 저장 (게임 오버 후에도 화면에 보이도록)
        if (curr != null) {
            lastBlock = curr;
            lastBlockX = x;
            lastBlockY = y;
        }
        
        // 타이머 정지 (블록은 그대로 두고 움직임만 중단)
        if (timer != null) {
            timer.stop();
        }
        
        // 점멸 타이머도 정지 (GameOver 창 점멸 방지)
        if (blinkTimer != null) {
            blinkTimer.stop();
        }
        
        // 게임 종료 오버레이 표시
        int currentScore = scoreManager.getScore();
        int currentLines = scoreManager.getLinesCleared();
        int currentTime = getElapsedTimeInSeconds(); // 실제 경과 시간 사용
        String difficulty = "Normal"; // 현재 난이도 설정
        
        GameOver gameOverOverlay = new GameOver(m_frame, currentScore, currentLines, currentTime, difficulty);
        
        // 게임 종료 화면을 현재 패널에 추가
        setLayout(new OverlayLayout(this));
        add(gameOverOverlay, 0); // 맨 앞에 추가
        
        revalidate();
        repaint();
        
        System.out.println("Game Over! Score: " + currentScore + ", Lines: " + currentLines);
    }
}
