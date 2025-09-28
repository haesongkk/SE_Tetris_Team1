package tetris.scene.game;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import javax.swing.*;
import tetris.Game;
import tetris.scene.Scene;
import tetris.scene.game.blocks.*;
import tetris.scene.test.TestScene;

public class GameScene extends Scene {
    private JFrame m_frame;
    private static final int GAME_HEIGHT = 20; // 실제 블록이 놓이는 높이
    private static final int GAME_WIDTH = 10; // 실제 블록이 놓이는 너비
    private static final int CELL_SIZE = 30; // 각 셀의 픽셀 크기
    private static final int INIT_INTERVAL_MS = 1000; // 블록 드롭 초기 속도 (밀리초)

    private GamePanel gamePanel; // 커스텀 게임 패널
    private int[][] board; // 게임 보드 상태 (0: 빈칸, 1: 블록 있음)
    private Color[][] boardColors; // 각 셀의 색상 정보 추가
    
    private Timer timer; // 블록 드롭 타이머
    private Block curr; // 현재 떨어지고 있는 블록
    private int x = 3; // 현재 블록의 x 위치
    private int y = 0; // 현재 블록의 y 위치

    private boolean initialized = false;

    // ─────────────────────────────────────────────────────────────
    // Scene lifecycle
    // ─────────────────────────────────────────────────────────────

    public GameScene(JFrame frame) {
        super(frame);
        m_frame = frame;
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
        setBackground(Color.BLACK);

        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(
            (GAME_WIDTH + 2) * CELL_SIZE,
            (GAME_HEIGHT + 2) * CELL_SIZE
        ));
        gamePanel.setBackground(Color.BLACK);
        add(gamePanel, BorderLayout.CENTER);

        // 메인 패널(this)에 키 리스너 추가
        addKeyListener(new PlayerKeyListener());
        setFocusable(true);

        // 드롭 타이머
        if (timer != null) timer.stop();
        timer = new Timer(INIT_INTERVAL_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveDown();
                gamePanel.repaint();
            }
        });
    }

    private void initGameState() {
        board = new int[GAME_HEIGHT][GAME_WIDTH];
        boardColors = new Color[GAME_HEIGHT][GAME_WIDTH]; // 색상 배열 초기화
        curr = getRandomBlock();
        x = 3;
        y = 0;
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

    // 완성된 줄을 찾아서 제거하고 위의 블록들을 내리는 메서드
    private void clearCompletedLines() {
        int linesCleared = 0;

        // 아래쪽부터 위쪽으로 검사 (GAME_HEIGHT-1부터 0까지)
        for (int row = GAME_HEIGHT - 1; row >= 0; row--) {
            if (isLineFull(row)) {
                // 완성된 줄 제거
                removeLine(row);
                linesCleared++;
                
                // 제거한 줄 위치에서 다시 검사 (같은 row를 다시 검사)
                row++; // for문의 row--와 상쇄되어 같은 위치를 다시 검사
            }
        }
        
        if (linesCleared > 0) {
            System.out.println("Cleared " + linesCleared + " lines!");
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

    // 여러 줄이 동시에 완성된 경우를 위한 개선된 버전 (선택사항)
    private int clearAllCompletedLines() {
        int linesCleared = 0;
        boolean[] linesToClear = new boolean[GAME_HEIGHT];
        
        // 1단계: 완성된 모든 줄 찾기
        for (int row = 0; row < GAME_HEIGHT; row++) {
            if (isLineFull(row)) {
                linesToClear[row] = true;
                linesCleared++;
            }
        }
        
        // 2단계: 완성된 줄들 제거 및 블록들 재배치
        if (linesCleared > 0) {
            int writeRow = GAME_HEIGHT - 1; // 새로 배치할 위치
            
            // 아래에서 위로 올라가면서 완성되지 않은 줄들만 복사
            for (int readRow = GAME_HEIGHT - 1; readRow >= 0; readRow--) {
                if (!linesToClear[readRow]) {
                    // 완성되지 않은 줄이면 아래쪽으로 이동
                    for (int col = 0; col < GAME_WIDTH; col++) {
                        board[writeRow][col] = board[readRow][col];
                        boardColors[writeRow][col] = boardColors[readRow][col];
                    }
                    writeRow--;
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
            
            System.out.println("Cleared " + linesCleared + " lines simultaneously!");
        }
        
        return linesCleared;
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
        // Block 클래스에 임시 회전 메서드가 필요하거나, 
        // 현재는 단순히 경계 체크만 수행
        int currentWidth = curr.width();
        int currentHeight = curr.height();
        
        // 회전 후 예상 크기 (width와 height가 바뀜)
        int newWidth = currentHeight;
        int newHeight = currentWidth;
        
        // 회전 후 경계를 벗어나는지 확인
        if (x + newWidth > GAME_WIDTH || y + newHeight > GAME_HEIGHT) {
            return false;
        }
        
        return true;
    }

    private void moveDown() {
        if (curr == null) return;
        
        if (canMoveDown()) {
            y++;
        } else {
            // 블록이 바닥에 닿았을 때만 보드에 고정
            placeBlockPermanently();
            curr = getRandomBlock();
            x = 3;
            y = 0;
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
            g2d.fillRect(0, 0, (WIDTH + 2) * CELL_SIZE, CELL_SIZE);
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

            // 게임 보드 그리기 (고정된 블록들) - 디버그 추가
            int fixedBlockCount = 0;
            System.out.println("Rendering fixed blocks..."); // 디버그
            
            for (int row = 0; row < GAME_HEIGHT; row++) {
                for (int col = 0; col < GAME_WIDTH; col++) {
                    if (board[row][col] == 1) {
                        fixedBlockCount++;
                        Color blockColor = boardColors[row][col];
                        
                        System.out.println("Found fixed block at [" + row + "][" + col + "] color=" + blockColor); // 디버그
                        
                        if (blockColor != null) {
                            g2d.setColor(blockColor);
                            g2d.fillRect((col + 1) * CELL_SIZE + 1, (row + 1) * CELL_SIZE + 1, 
                                        CELL_SIZE - 2, CELL_SIZE - 2);
                        
                            // 블록 테두리
                            g2d.setColor(Color.BLACK);
                            g2d.setStroke(new BasicStroke(1));
                            g2d.drawRect((col + 1) * CELL_SIZE + 1, (row + 1) * CELL_SIZE + 1, 
                                        CELL_SIZE - 2, CELL_SIZE - 2);
                            
                            System.out.println("Drew fixed block at screen pos: " + 
                                             ((col + 1) * CELL_SIZE + 1) + "," + ((row + 1) * CELL_SIZE + 1)); // 디버그
                        } else {
                            System.out.println("Block color is null!"); // 디버그
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

            // 디버그 정보 표시
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("Game Area: " + GAME_WIDTH + "x" + GAME_HEIGHT, 5, 15);
            g2d.drawString("Fixed blocks: " + fixedBlockCount, 5, 30);
            g2d.drawString("Current pos: (" + GameScene.this.x + "," + GameScene.this.y + ")", 5, 45);
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
                case KeyEvent.VK_ESCAPE:
                    Game.setScene(new TestScene(m_frame));
                    break;
            }
        }
        @Override public void keyReleased(KeyEvent e) {}
    }
}
