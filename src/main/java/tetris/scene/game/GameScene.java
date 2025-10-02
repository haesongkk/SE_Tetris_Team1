package tetris.scene.game;

import tetris.Game;
import tetris.GameSettings;
import tetris.ColorBlindHelper;
import tetris.scene.Scene;
import tetris.scene.game.blocks.*;
import tetris.scene.game.overlay.GameOver;
import tetris.scene.game.overlay.ScoreManager;
import tetris.scene.menu.MainMenuScene;

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
    }

    @Override
    public void onExit() {
        if (timer != null) timer.stop();
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
                if (!isPaused && !isGameOver) {  // 일시정지나 게임 종료 상태가 아닐 때만 블록 이동
                    moveDown();
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
        // placeBlock() 호출 제거
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }

    private Block getRandomBlock() {
        Random rnd = new Random(System.currentTimeMillis());
        int block = rnd.nextInt(7); // 0~6
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
        
        // 게임 종료 조건 확인: 블록이 천장(y=0 위치)에 고정되면 게임 종료
        if (y <= 0) {
            triggerGameOver();
            return;
        }
        
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
        
        // 완성된 줄 확인 및 제거
        clearCompletedLines();
        
        printBoard();
    }

    // 완성된 줄을 찾아서 제거하고 위의 블록들을 내리는 메서드 (수정됨)
    private void clearCompletedLines() {
        int linesClearedThisTurn = clearAllCompletedLines(); // 개선된 메서드 사용
        
        if (linesClearedThisTurn > 0) {
            // 점수 업데이트 (ScoreManager 사용)
            scoreManager.addScore(linesClearedThisTurn);
        }
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
        if (curr == null || isGameOver) return;
        
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
        
        if (gamePanel != null) {
            gamePanel.repaint();
        }
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
            int fixedBlockCount = 0;
            // System.out.println("Rendering fixed blocks..."); // 디버그 (주석 처리)
            
            for (int row = 0; row < GAME_HEIGHT; row++) {
                for (int col = 0; col < GAME_WIDTH; col++) {
                    if (board[row][col] == 1) {
                        fixedBlockCount++;
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

            // 현재 블록 그리기
            if (curr != null) {
                g2d.setColor(curr.getColor());
                for (int blockRow = 0; blockRow < curr.height(); blockRow++) {
                    for (int blockCol = 0; blockCol < curr.width(); blockCol++) {
                        if (curr.getShape(blockCol, blockRow) == 1) {
                            int drawX = (GameScene.this.x + blockCol + 1) * CELL_SIZE + 1;
                            int drawY = (GameScene.this.y + blockRow + 1) * CELL_SIZE + 1;
                            
                            g2d.fillRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
                            
                            // 현재 블록 테두리
                            g2d.setColor(Color.WHITE);
                            g2d.setStroke(new BasicStroke(2));
                            g2d.drawRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
                            g2d.setColor(curr.getColor());
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

            // 디버그 정보 표시
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("Game Area: " + GAME_WIDTH + "x" + GAME_HEIGHT, 5, 15);
            g2d.drawString("Fixed blocks: " + fixedBlockCount, 5, 30);
            g2d.drawString("Current pos: (" + GameScene.this.x + "," + GameScene.this.y + ")", 5, 45);
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
            
            // ESC 키는 항상 처리 (메인 메뉴로 이동)
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                Game.setScene(new MainMenuScene(m_frame));
                return;
            }
            
            // 게임이 종료된 상태일 때는 ESC를 제외한 모든 키 입력 무시
            if (isGameOver) {
                return;
            }
            
            // P 키는 일시정지 상태와 관계없이 처리 (게임이 진행 중일 때만)
            if (e.getKeyCode() == KeyEvent.VK_P) {
                togglePause();
                return;
            }
            
            // 일시정지 상태일 때는 다른 키 입력 무시
            if (isPaused) {
                return;
            }
            
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DOWN:
                    moveDown();
                    gamePanel.repaint();
                    break;
                case KeyEvent.VK_RIGHT:
                    moveRight();
                    gamePanel.repaint();
                    break;
                case KeyEvent.VK_LEFT:
                    moveLeft();
                    gamePanel.repaint();
                    break;
                case KeyEvent.VK_UP:
                    if (curr != null && canRotate()) {
                        curr.rotate();
                        gamePanel.repaint();
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    hardDrop();
                    break;
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
        
        // 현재 블록과 다음 블록을 null로 설정하여 더 이상 그려지지 않도록 함
        curr = null;
        next = null;
        
        // 타이머 정지
        if (timer != null) {
            timer.stop();
        }
        
        // 게임 종료 오버레이 표시
        int currentScore = scoreManager.getScore();
        int currentLines = scoreManager.getLinesCleared();
        int currentTime = 0; // 시간 기능이 구현되지 않았으므로 0으로 설정
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
