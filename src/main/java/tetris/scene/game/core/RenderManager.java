package tetris.scene.game.core;

import tetris.scene.game.blocks.Block;
import tetris.scene.game.blocks.BlockHardDrop;
import tetris.scene.game.blocks.BlockShake;
import tetris.util.LineBlinkEffect;

import java.awt.*;

/**
 * 게임 화면 렌더링을 담당하는 매니저 클래스
 * 보드, 블록, 특수 효과 등의 모든 렌더링 로직을 관리합니다.
 */
public class RenderManager {
    
    // 렌더링 상수들
    private final int GAME_WIDTH;
    private final int GAME_HEIGHT;
    private final int CELL_SIZE;
    private final int PREVIEW_SIZE;
    private final int PREVIEW_CELL_SIZE;
    
    // 의존성
    private final BoardManager boardManager;
    private final BlockManager blockManager;
    private final GameStateManager gameStateManager;
    private final ScoreManager scoreManager;
    
    /**
     * RenderManager 생성자
     */
    public RenderManager(int gameWidth, int gameHeight, int cellSize, int previewSize, int previewCellSize,
                        BoardManager boardManager, BlockManager blockManager, GameStateManager gameStateManager,
                        ScoreManager scoreManager) {
        this.GAME_WIDTH = gameWidth;
        this.GAME_HEIGHT = gameHeight;
        this.CELL_SIZE = cellSize;
        this.PREVIEW_SIZE = previewSize;
        this.PREVIEW_CELL_SIZE = previewCellSize;
        this.boardManager = boardManager;
        this.blockManager = blockManager;
        this.gameStateManager = gameStateManager;
        this.scoreManager = scoreManager;
    }
    
    /**
     * 전체 게임 화면을 렌더링합니다.
     */
    public void render(Graphics2D g2d, int panelWidth, int panelHeight, 
                      LineBlinkEffect lineBlinkEffect, Block lastBlock, int lastBlockX, int lastBlockY) {
        // 안티알리아싱 설정
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 배경 렌더링
        renderBackground(g2d, panelWidth, panelHeight);
        
        // 게임 보드 경계 및 그리드 렌더링
        renderGameBoardFrame(g2d);
        
        // 고정된 블록들 렌더링
        renderFixedBlocks(g2d, lineBlinkEffect);
        
        // 줄 점멸 효과 렌더링
        renderLineBlinkEffect(g2d, lineBlinkEffect);
        
        // 고스트 블록 렌더링 (하드드롭 미리보기)
        renderGhostBlock(g2d);
        
        // 현재 블록 렌더링
        renderCurrentBlock(g2d, lastBlock, lastBlockX, lastBlockY);
        
        // 다음 블록 미리보기 렌더링
        renderNextBlockPreview(g2d);
        
        // 점수판 렌더링
        renderScoreBoard(g2d);
        
        // 일시정지 오버레이 렌더링
        if (gameStateManager.isPaused()) {
            renderPauseOverlay(g2d);
        }
    }
    
    /**
     * 배경을 렌더링합니다.
     */
    private void renderBackground(Graphics2D g2d, int panelWidth, int panelHeight) {
        // 전체 배경을 검은색으로
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, panelWidth, panelHeight);
    }
    
    /**
     * 게임 보드의 경계와 그리드를 렌더링합니다.
     */
    private void renderGameBoardFrame(Graphics2D g2d) {
        // 외부 경계 영역 (회색)
        g2d.setColor(Color.GRAY);
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
        renderGridLines(g2d);
    }
    
    /**
     * 그리드 라인을 렌더링합니다.
     */
    private void renderGridLines(Graphics2D g2d) {
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
    }
    
    /**
     * 고정된 블록들을 렌더링합니다.
     */
    private void renderFixedBlocks(Graphics2D g2d, LineBlinkEffect lineBlinkEffect) {
        int[][] board = boardManager.getBoard();
        Color[][] boardColors = boardManager.getBoardColors();
        
        for (int row = 0; row < GAME_HEIGHT; row++) {
            // 점멸 중인 줄은 LineBlinkEffect에서 처리하므로 건너뜁니다
            if (lineBlinkEffect != null && lineBlinkEffect.isActive() && lineBlinkEffect.isLineBlinking(row)) {
                continue;
            }
            
            for (int col = 0; col < GAME_WIDTH; col++) {
                if (board[row][col] == 1) {
                    Color blockColor = boardColors[row][col];
                    
                    if (blockColor != null) {
                        g2d.setColor(blockColor);
                        g2d.fillRect((col + 1) * CELL_SIZE + 1, (row + 1) * CELL_SIZE + 1, 
                                    CELL_SIZE - 2, CELL_SIZE - 2);
                    
                        // 블록 테두리
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new BasicStroke(1));
                        g2d.drawRect((col + 1) * CELL_SIZE + 1, (row + 1) * CELL_SIZE + 1, 
                                    CELL_SIZE - 2, CELL_SIZE - 2);
                    }
                }
            }
        }
    }
    
    /**
     * 줄 점멸 효과를 렌더링합니다.
     */
    private void renderLineBlinkEffect(Graphics2D g2d, LineBlinkEffect lineBlinkEffect) {
        if (lineBlinkEffect != null && lineBlinkEffect.isActive()) {
            lineBlinkEffect.draw(g2d, CELL_SIZE, GAME_WIDTH, boardManager.getBoard(), boardManager.getBoardColors());
        }
    }
    
    /**
     * 고스트 블록을 렌더링합니다 (하드드롭 미리보기).
     */
    private void renderGhostBlock(Graphics2D g2d) {
        Block currentBlock = blockManager.getCurrentBlock();
        if (currentBlock != null && !blockManager.isGameOver()) {
            int blockX = blockManager.getX();
            int blockY = blockManager.getY();
            int ghostY = BlockHardDrop.calculateGhostPosition(currentBlock, blockX, blockY, 
                boardManager.getBoard(), GAME_WIDTH, GAME_HEIGHT);
            
            // 고스트 블록이 현재 블록과 다른 위치에 있을 때만 그리기
            if (ghostY != blockY) {
                // 반투명한 색상으로 고스트 블록 그리기
                Color originalColor = currentBlock.getColor();
                Color ghostColor = new Color(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), 80);
                g2d.setColor(ghostColor);
                
                for (int blockRow = 0; blockRow < currentBlock.height(); blockRow++) {
                    for (int blockCol = 0; blockCol < currentBlock.width(); blockCol++) {
                        if (currentBlock.getShape(blockCol, blockRow) == 1) {
                            int drawX = (blockX + blockCol + 1) * CELL_SIZE + 1;
                            int drawY = (ghostY + blockRow + 1) * CELL_SIZE + 1;
                            
                            // 고스트 블록은 외곽선만 그리기
                            g2d.setStroke(new BasicStroke(2));
                            g2d.drawRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 현재 블록을 렌더링합니다.
     */
    private void renderCurrentBlock(Graphics2D g2d, Block lastBlock, int lastBlockX, int lastBlockY) {
        Block currentBlock = blockManager.getCurrentBlock();
        Block blockToDraw = currentBlock;
        int blockX = blockManager.getX();
        int blockY = blockManager.getY();
        
        // 게임 오버 상태이고 마지막 블록이 저장되어 있다면 마지막 블록 그리기
        if (blockManager.isGameOver() && lastBlock != null) {
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
                        BlockShake blockShake = blockManager.getBlockShake();
                        if (blockToDraw == currentBlock && blockShake != null && blockShake.isShaking()) {
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
    }
    
    /**
     * 다음 블록 미리보기를 렌더링합니다.
     */
    private void renderNextBlockPreview(Graphics2D g2d) {
        Block nextBlock = blockManager.getNextBlock();
        if (nextBlock == null) return;

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
        int blockWidth = nextBlock.width();
        int blockHeight = nextBlock.height();
        int offsetX = (PREVIEW_SIZE - blockWidth) / 2;
        int offsetY = (PREVIEW_SIZE - blockHeight) / 2;

        // 다음 블록 그리기
        g2d.setColor(nextBlock.getColor());
        for (int blockRow = 0; blockRow < blockHeight; blockRow++) {
            for (int blockCol = 0; blockCol < blockWidth; blockCol++) {
                if (nextBlock.getShape(blockCol, blockRow) == 1) {
                    int drawX = previewX + (offsetX + blockCol) * PREVIEW_CELL_SIZE + 2;
                    int drawY = previewY + (offsetY + blockRow) * PREVIEW_CELL_SIZE + 2;
                    
                    g2d.fillRect(drawX, drawY, PREVIEW_CELL_SIZE - 4, PREVIEW_CELL_SIZE - 4);
                    
                    // 블록 테두리
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRect(drawX, drawY, PREVIEW_CELL_SIZE - 4, PREVIEW_CELL_SIZE - 4);
                    g2d.setColor(nextBlock.getColor());
                }
            }
        }
    }
    
    /**
     * 점수판을 렌더링합니다.
     */
    private void renderScoreBoard(Graphics2D g2d) {
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
        renderTimeBoard(g2d, scoreBoardX, scoreBoardY + scoreBoardHeight + 10, scoreBoardWidth, 50);
    }
    
    /**
     * 시간 정보를 표시하는 보드를 렌더링합니다.
     */
    private void renderTimeBoard(Graphics2D g2d, int x, int y, int width, int height) {
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
        int elapsedSeconds = gameStateManager.getElapsedTimeInSeconds();
        String timeText = formatTime(elapsedSeconds);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        fm = g2d.getFontMetrics();
        int timeWidth = fm.stringWidth(timeText);
        g2d.drawString(timeText, x + (width - timeWidth) / 2, y + 40);
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
    
    /**
     * 일시정지 오버레이를 렌더링합니다.
     */
    private void renderPauseOverlay(Graphics2D g2d) {
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