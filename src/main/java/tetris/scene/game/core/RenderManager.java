package tetris.scene.game.core;

import tetris.scene.game.blocks.Block;
import tetris.scene.game.blocks.BlockShake;
import tetris.scene.game.blocks.ItemBlock;
import tetris.scene.game.blocks.WeightItemBlock;
import tetris.util.LineBlinkEffect;
import tetris.ColorBlindHelper;
import tetris.GameSettings;

import java.awt.*;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
    
    // 이미지 캐시
    private final Map<String, BufferedImage> imageCache = new HashMap<>();
    
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
                      LineBlinkEffect lineBlinkEffect, Block lastBlock, int lastBlockX, int lastBlockY, boolean visionBlockActive,
                      boolean cleanupBlinkingActive, java.util.Set<java.awt.Point> cleanupBlinkingCells, boolean skipTimeBoard) {
        // 안티알리아싱 설정
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 배경 렌더링
        renderBackground(g2d, panelWidth, panelHeight);
        
        // 게임 보드 경계 및 그리드 렌더링
        renderGameBoardFrame(g2d);
        
        // 고정된 블록들 렌더링 (점멸 중인 셀들은 제외)
        renderFixedBlocks(g2d, lineBlinkEffect, cleanupBlinkingActive, cleanupBlinkingCells);
        
        // 줄 점멸 효과 렌더링
        renderLineBlinkEffect(g2d, lineBlinkEffect);
        
        // 점멸 효과가 진행 중이 아닐 때만 현재 블록과 고스트 블록 렌더링
        // (줄 삭제 점멸 또는 청소 아이템 점멸 모두 체크)
        if ((lineBlinkEffect == null || !lineBlinkEffect.isActive()) && !cleanupBlinkingActive) {
            // 고스트 블록 렌더링 (하드드롭 미리보기)
            renderGhostBlock(g2d);
            
            // 현재 블록 렌더링
            renderCurrentBlock(g2d, lastBlock, lastBlockX, lastBlockY);
        }
        
        // 다음 블록 미리보기 렌더링
        renderNextBlockPreview(g2d);
        
        // 점수판 렌더링
        renderScoreBoard(g2d, skipTimeBoard);
        
        // 일시정지 오버레이 렌더링
        if (gameStateManager.isPaused()) {
            renderPauseOverlay(g2d);
        }
        
        // 시야 차단 효과 렌더링
        if (visionBlockActive) {
            renderVisionBlockEffect(g2d);
        }
        
        // 청소 블링킹 효과 렌더링
        if (cleanupBlinkingActive && cleanupBlinkingCells != null && !cleanupBlinkingCells.isEmpty()) {
            renderCleanupBlinkingEffect(g2d, cleanupBlinkingCells);
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
    private void renderFixedBlocks(Graphics2D g2d, LineBlinkEffect lineBlinkEffect, 
                                  boolean cleanupBlinkingActive, java.util.Set<java.awt.Point> cleanupBlinkingCells) {
        int[][] board = boardManager.getBoard();
        Color[][] boardColors = boardManager.getBoardColors();
        
        for (int row = 0; row < GAME_HEIGHT; row++) {
            // 점멸 중인 줄은 LineBlinkEffect에서 처리하므로 건너뜁니다
            if (lineBlinkEffect != null && lineBlinkEffect.isActive() && lineBlinkEffect.isLineBlinking(row)) {
                continue;
            }
            
            for (int col = 0; col < GAME_WIDTH; col++) {
                // 청소 점멸 중인 셀은 renderCleanupBlinkingEffect에서 처리하므로 건너뜁니다
                if (cleanupBlinkingActive && cleanupBlinkingCells != null && 
                    cleanupBlinkingCells.contains(new java.awt.Point(col, row))) {
                    continue;
                }
                
                if (board[row][col] == 1) {
                    int drawX = (col + 1) * CELL_SIZE + 1;
                    int drawY = (row + 1) * CELL_SIZE + 1;
                    
                    // 아이템 셀인지 확인
                    if (boardManager.isItemCell(col, row)) {
                        // 아이템 셀 렌더링
                        ItemBlock itemBlockInfo = boardManager.getItemBlockInfo(col, row);
                        if (itemBlockInfo != null) {
                            // 시각제한 아이템의 경우 특별 처리: 아이템 효과가 활성화되었으면 일반 블록으로 렌더링
                            boolean isVisionBlockItem = false;
                            try {
                                Object itemType = itemBlockInfo.getClass().getMethod("getItemType").invoke(itemBlockInfo);
                                if (itemType != null && itemType.toString().contains("VISION_BLOCK")) {
                                    isVisionBlockItem = true;
                                }
                            } catch (Exception e) {
                                // 타입 확인 실패 시 일반 처리
                            }
                            
                            if (isVisionBlockItem && gameStateManager != null) {
                                // 시각제한 효과가 활성화된 경우 일반 블록으로 렌더링
                                try {
                                    // GameScene에서 시야 차단 활성화 상태 확인
                                    boolean visionActive = false;
                                    // GameScene 접근을 위해 임시로 일반 블록으로 렌더링
                                    Color blockColor = boardColors[row][col];
                                    if (blockColor != null) {
                                        g2d.setColor(blockColor);
                                        g2d.fillRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
                                    }
                                } catch (Exception e) {
                                    // 일반 아이템 렌더링으로 폴백
                                    itemBlockInfo.drawItemCell((Graphics2D) g2d.create(), drawX, drawY, CELL_SIZE - 2);
                                }
                            } else {
                                // 일반 아이템은 아이템 이미지로 그리기
                                itemBlockInfo.drawItemCell((Graphics2D) g2d.create(), drawX, drawY, CELL_SIZE - 2);
                            }
                        } else {
                            // 아이템 정보가 없으면 일반 색상으로 처리
                            Color blockColor = boardColors[row][col];
                            if (blockColor != null) {
                                g2d.setColor(blockColor);
                                g2d.fillRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
                            }
                        }
                    } else {
                        // 일반 셀 렌더링
                        Color blockColor = boardColors[row][col];
                        if (blockColor != null) {
                            g2d.setColor(blockColor);
                            g2d.fillRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
                            
                            // 색맹 모드에서 패턴 추가
                            int colorBlindMode = tetris.GameSettings.getInstance().getColorBlindMode();
                            if (colorBlindMode > 0) {
                                int[][] boardTypes = boardManager.getBoardTypes();
                                int blockType = boardTypes[row][col];
                                if (blockType >= 0) {
                                    tetris.ColorBlindHelper.drawBlockPattern(g2d, blockType, drawX, drawY, CELL_SIZE - 2, colorBlindMode, blockColor);
                                }
                            }
                        }
                    }
                    
                    // 블록 테두리
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
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
            int ghostY = blockManager.getGhostY(); // BlockManager의 getGhostY() 사용
            
            // 고스트 블록이 현재 블록과 다른 위치에 있고, 유효한 위치일 때만 그리기
            if (ghostY != blockY && ghostY != -1) {
                for (int blockRow = 0; blockRow < currentBlock.height(); blockRow++) {
                    for (int blockCol = 0; blockCol < currentBlock.width(); blockCol++) {
                        if (currentBlock.getShape(blockCol, blockRow) == 1) {
                            int drawX = (blockX + blockCol + 1) * CELL_SIZE + 1;
                            int drawY = (ghostY + blockRow + 1) * CELL_SIZE + 1;
                            
                            // 무게추 블록의 경우 특별한 고스트 렌더링
                            if (currentBlock instanceof WeightItemBlock) {
                                WeightItemBlock weightBlock = (WeightItemBlock) currentBlock;
                                // 투명한 무게추 고스트 블록 그리기
                                Graphics2D ghostG2d = (Graphics2D) g2d.create();
                                ghostG2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                                weightBlock.drawWeightCell(ghostG2d, drawX, drawY, CELL_SIZE - 2);
                                ghostG2d.dispose();
                            } else {
                                // 일반 블록의 경우 반투명한 색상으로 고스트 블록 그리기
                                Color originalColor = currentBlock.getColor();
                                Color ghostColor = new Color(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), 80);
                                g2d.setColor(ghostColor);
                                
                                // 고스트 블록은 외곽선만 그리기
                                g2d.setStroke(new BasicStroke(2));
                                g2d.drawRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
                            }
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
                        
                        // 블록 타입별 렌더링 처리
                        if (blockToDraw instanceof WeightItemBlock) {
                            // WeightItemBlock인 경우 무게추 셀 처리
                            WeightItemBlock weightBlock = (WeightItemBlock) blockToDraw;
                            weightBlock.drawWeightCell((Graphics2D) g2d.create(), drawX, drawY, CELL_SIZE - 2);
                        } else if (blockToDraw instanceof ItemBlock) {
                            // ItemBlock인 경우 아이템 셀 처리
                            ItemBlock itemBlock = (ItemBlock) blockToDraw;
                            if (itemBlock.isItemCell(blockCol, blockRow)) {
                                // 아이템 셀 그리기
                                itemBlock.drawItemCell((Graphics2D) g2d.create(), drawX, drawY, CELL_SIZE - 2);
                            } else {
                                // 일반 셀 그리기
                                g2d.setColor(itemBlock.getCellColor(blockCol, blockRow));
                                g2d.fillRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
                            }
                        } else {
                            // 일반 블록 그리기
                            Color blockColor = blockToDraw.getColor();
                            g2d.setColor(blockColor);
                            g2d.fillRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
                            
                            // 색맹 모드에서 패턴 추가
                            int colorBlindMode = tetris.GameSettings.getInstance().getColorBlindMode();
                            if (colorBlindMode > 0) {
                                int blockType = blockToDraw.getType();
                                tetris.ColorBlindHelper.drawBlockPattern(g2d, blockType, drawX, drawY, CELL_SIZE - 2, colorBlindMode, blockColor);
                            }
                        }
                        
                        // 현재 블록 테두리
                        g2d.setColor(Color.WHITE);
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);
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
        boolean itemImageRendered = false; // 아이템 이미지가 첫 번째 셀에 렌더링되었는지 추적
        
        for (int blockRow = 0; blockRow < blockHeight; blockRow++) {
            for (int blockCol = 0; blockCol < blockWidth; blockCol++) {
                if (nextBlock.getShape(blockCol, blockRow) == 1) {
                    int drawX = previewX + (offsetX + blockCol) * PREVIEW_CELL_SIZE + 2;
                    int drawY = previewY + (offsetY + blockRow) * PREVIEW_CELL_SIZE + 2;
                    
                    // ItemBlock인지 확인하고 첫 번째 셀에만 아이템 이미지 렌더링
                    boolean itemRenderedForThisCell = false;
                    if (!itemImageRendered) {
                        // 아이템 이미지가 렌더링되었는지 확인
                        if (renderItemImageInPreview(g2d, nextBlock, drawX, drawY, PREVIEW_CELL_SIZE - 4)) {
                            itemImageRendered = true; // 한 번만 렌더링하도록 플래그 설정
                            itemRenderedForThisCell = true;
                        }
                    }
                    
                    // 아이템 이미지가 렌더링되지 않은 셀은 일반 블록으로 렌더링
                    if (!itemRenderedForThisCell) {
                        Color blockColor = nextBlock.getColor();
                        g2d.setColor(blockColor);
                        g2d.fillRect(drawX, drawY, PREVIEW_CELL_SIZE - 4, PREVIEW_CELL_SIZE - 4);
                        
                        // 색맹 모드에서 패턴 추가
                        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
                        if (colorBlindMode > 0) {
                            int blockType = nextBlock.getType();
                            ColorBlindHelper.drawBlockPattern(g2d, blockType, drawX, drawY, PREVIEW_CELL_SIZE - 4, colorBlindMode, blockColor);
                        }
                        
                        // 블록 테두리
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new BasicStroke(1));
                        g2d.drawRect(drawX, drawY, PREVIEW_CELL_SIZE - 4, PREVIEW_CELL_SIZE - 4);
                    }
                }
            }
        }
    }
    
    /**
     * 미리보기 영역에서 아이템 이미지를 렌더링합니다.
     * @return 아이템 이미지가 렌더링되었으면 true, 그렇지 않으면 false
     */
    private boolean renderItemImageInPreview(Graphics2D g2d, Block block, int x, int y, int cellSize) {
        // ItemBlock인지 확인
        if (!block.getClass().getSimpleName().equals("ItemBlock")) {
            return false;
        }
        
        try {
            // ItemBlock의 getItemType 메서드 호출
            Object itemType = block.getClass().getMethod("getItemType").invoke(block);
            if (itemType == null) {
                return false;
            }
            
            // ItemEffectType별로 처리
            String itemTypeName = itemType.toString();
            
            // 각 아이템 타입별로 처리
            switch (itemTypeName) {
                case "SPEED_UP":
                    // 흰색 배경 그리기 (이미지 가시성을 위해)
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(x, y, cellSize, cellSize);
                    renderItemImage(g2d, "running.png", x, y, cellSize);
                    return true;
                case "SPEED_DOWN":
                    // 흰색 배경 그리기 (이미지 가시성을 위해)
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(x, y, cellSize, cellSize);
                    renderItemImage(g2d, "snail.png", x, y, cellSize);
                    return true;
                case "VISION_BLOCK":
                    // 흰색 배경 그리기 (이미지 가시성을 위해)
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(x, y, cellSize, cellSize);
                    renderItemImage(g2d, "visionblock.png", x, y, cellSize);
                    return true;
                case "CLEANUP":
                    // 흰색 배경 그리기 (이미지 가시성을 위해)
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(x, y, cellSize, cellSize);
                    renderItemImage(g2d, "broom.png", x, y, cellSize);
                    return true;
                case "LINE_CLEAR":
                    // 줄 삭제는 검정 배경에 흰색 'L' 글자 사용
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(x, y, cellSize, cellSize);
                    renderLineClearSymbol(g2d, x, y, cellSize);
                    return true;
                default:
                    return false; // 알 수 없는 타입은 이미지 없이 처리
            }
            
        } catch (Exception e) {
            // 아이템 이미지 렌더링 실패 시 조용히 무시 (일반 블록으로 표시)
            System.out.println("Failed to render item image in preview: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * PNG 이미지를 렌더링합니다 (크기 확대).
     */
    private void renderItemImage(Graphics2D g2d, String imagePath, int x, int y, int cellSize) {
        BufferedImage itemImage = loadImage(imagePath);
        if (itemImage != null) {
            // 이미지 크기를 더 크게 (여백을 2픽셀로 줄임)
            int imageSize = cellSize - 2;
            int imageX = x + (cellSize - imageSize) / 2;
            int imageY = y + (cellSize - imageSize) / 2;
            
            g2d.drawImage(itemImage, imageX, imageY, imageSize, imageSize, null);
            System.out.println("Rendered item image in preview: " + imagePath);
        } else {
            System.out.println("Failed to load image: " + imagePath);
        }
    }
    
    /**
     * LINE_CLEAR 아이템용 흰색 'L' 글자를 렌더링합니다.
     */
    private void renderLineClearSymbol(Graphics2D g2d, int x, int y, int cellSize) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 흰색 'L' 글자
        g.setColor(Color.WHITE);
        
        // 폰트 크기를 셀 크기에 맞게 조정 (더 크게)
        int fontSize = Math.max(cellSize * 3 / 4, 12); // 셀 크기의 75%로 더 크게
        g.setFont(new Font("Arial", Font.BOLD, fontSize));
        
        // 'L' 글자 중앙 배치
        FontMetrics fm = g.getFontMetrics();
        String letter = "L";
        int letterWidth = fm.stringWidth(letter);
        int letterHeight = fm.getAscent();
        
        int letterX = x + (cellSize - letterWidth) / 2;
        int letterY = y + (cellSize + letterHeight) / 2 - fm.getDescent();
        
        g.drawString(letter, letterX, letterY);
        g.dispose();
    }
    
    /**
     * 점수판을 렌더링합니다.
     */
    private void renderScoreBoard(Graphics2D g2d, boolean skipTimeBoard) {
        // 점수판 위치 계산 (다음 블록 미리보기 아래)
        int previewX = (GAME_WIDTH + 2) * CELL_SIZE + 20;
        int previewY = CELL_SIZE + 20;
        int previewAreaSize = PREVIEW_SIZE * PREVIEW_CELL_SIZE;
        
        int scoreBoardX = previewX;
        int scoreBoardY = previewY + previewAreaSize + 10; // 미리보기 아래 30px 간격
        int scoreBoardWidth = previewAreaSize;
        int scoreBoardHeight = 120; // 점수판 높이

        // ScoreManager의 drawScoreBoard 메서드 사용
        scoreManager.drawScoreBoard(g2d, scoreBoardX, scoreBoardY, scoreBoardWidth, scoreBoardHeight);
        
        // 시간 표시 (점수판 아래) - skipTimeBoard가 true면 건너뛰기
        if (!skipTimeBoard) {
            renderTimeBoard(g2d, scoreBoardX, scoreBoardY + scoreBoardHeight + 10, scoreBoardWidth, 50);
        }
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
        g2d.setFont(new Font("Arial", Font.BOLD, 10)); // 폰트 크기 줄임 (12 -> 10)
        FontMetrics fm = g2d.getFontMetrics();
        String timeLabel = "TIME";
        int labelWidth = fm.stringWidth(timeLabel);
        g2d.drawString(timeLabel, x + (width - labelWidth) / 2, y + 15); // 위치 조정 (20 -> 15)

        // 현재 시간 표시
        int elapsedSeconds = gameStateManager.getElapsedTimeInSeconds();
        String timeText = formatTime(elapsedSeconds);
        g2d.setFont(new Font("Arial", Font.BOLD, 14)); // 폰트 크기 줄임 (16 -> 14)
        fm = g2d.getFontMetrics();
        int timeWidth = fm.stringWidth(timeText);
        g2d.drawString(timeText, x + (width - timeWidth) / 2, y + 28); // 위치 조정 (40 -> 28)
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
        
        // Q 키로 메뉴로 돌아가기 안내 텍스트
        String exitText = "Press Q to return to menu";
        int exitWidth = smallFm.stringWidth(exitText);
        int exitX = gameAreaCenterX - exitWidth / 2;
        int exitY = instructionY + 25; // 재개 안내 텍스트 아래 25px
        
        g2d.drawString(exitText, exitX, exitY);
    }
    
    /**
     * 시야 차단 효과를 렌더링합니다.
     * 게임 보드의 중앙 부분을 어둡게 가립니다.
     */
    private void renderVisionBlockEffect(Graphics2D g2d) {
        // 게임 보드 중앙 영역 계산
        int boardStartX = CELL_SIZE;  // 보드 시작 X 위치 (경계 고려)
        int boardStartY = CELL_SIZE;  // 보드 시작 Y 위치 (경계 고려)
        
        // 중앙 4x20 영역을 가림 (10x20 보드의 중간 부분)
        int coverWidth = 4 * CELL_SIZE;   // 4블록 너비
        int coverHeight = 20 * CELL_SIZE;  // 20블록 높이 (전체 높이)
        int coverX = boardStartX + 3 * CELL_SIZE;  // 좌측에서 3블록 떨어진 위치 (중앙)
        int coverY = boardStartY;  // 상단부터 시작
        
        // 완전 불투명 검정 오버레이로 시야 차단
        g2d.setColor(new Color(0, 0, 0, 255)); // 완전 불투명 검정
        g2d.fillRect(coverX, coverY, coverWidth, coverHeight);
        
        // 시야 차단 효과 경계선
        g2d.setColor(new Color(255, 0, 0, 100)); // 반투명 빨강 경계선
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(coverX, coverY, coverWidth, coverHeight);
    }
    
    /**
     * 청소 블링킹 효과를 렌더링합니다 (기존 줄 삭제와 동일한 방식).
     */
    private void renderCleanupBlinkingEffect(Graphics2D g2d, java.util.Set<java.awt.Point> blinkingCells) {
        // LineBlinkEffect와 완전히 동일한 점멸 패턴 구현
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime % 900; // 900ms 전체 주기
        
        // LineBlinkEffect와 동일한 점멸 계산: 150ms마다 토글, 3번만 점멸
        int cycle = (int)(elapsed / 150); // 150ms 단위로 사이클 계산
        boolean blinkState = (cycle % 2 == 0); // 짝수 사이클에서 점멸
        
        // 3번만 점멸하도록 제한 (cycle 0,2,4만 점멸)
        if (cycle >= 6) {
            blinkState = false;
        }
        
        if (blinkState) {
            // 블링킹 상태: LineBlinkEffect와 완전히 동일한 렌더링
            int boardStartX = CELL_SIZE;
            int boardStartY = CELL_SIZE;
            
            for (java.awt.Point cell : blinkingCells) {
                int cellX = boardStartX + cell.x * CELL_SIZE;
                int cellY = boardStartY + cell.y * CELL_SIZE;
                
                // BoardManager에서 해당 위치의 원래 색상 가져오기
                Color originalColor = boardManager.getBoardColor(cell.x, cell.y);
                if (originalColor != null) {
                    // LineBlinkEffect와 동일한 색상 처리: 원래 색상을 30% 투명도로 희미하게 만들기
                    Color dimColor = new Color(
                        originalColor.getRed(),
                        originalColor.getGreen(),
                        originalColor.getBlue(),
                        80  // LineBlinkEffect와 동일한 30% 투명도
                    );
                    g2d.setColor(dimColor);
                    g2d.fillRect(cellX + 1, cellY + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                    
                    // LineBlinkEffect와 동일한 희미한 테두리
                    Color dimBorderColor = new Color(0, 0, 0, 128);
                    g2d.setColor(dimBorderColor);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRect(cellX + 1, cellY + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                }
            }
        }
        // blinkState가 false일 때는 원래 블록들이 그려지므로 별도 처리 불필요 (LineBlinkEffect와 동일)
    }
    
    /**
     * 이미지를 로드하고 캐시에 저장합니다.
     */
    private BufferedImage loadImage(String imagePath) {
        // 캐시에서 먼저 확인
        BufferedImage cachedImage = imageCache.get(imagePath);
        if (cachedImage != null) {
            return cachedImage;
        }
        
        try {
            // 리소스에서 이미지 로드
            InputStream imageStream = getClass().getResourceAsStream("/" + imagePath);
            if (imageStream != null) {
                BufferedImage image = ImageIO.read(imageStream);
                imageCache.put(imagePath, image);
                return image;
            } else {
                System.out.println("Image not found: " + imagePath);
                return null;
            }
        } catch (IOException e) {
            System.out.println("Failed to load image: " + imagePath + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 셀 크기를 반환합니다.
     */
    public int getCellSize() {
        return CELL_SIZE;
    }
    
    /**
     * 미리보기 셀 크기를 반환합니다.
     */
    public int getPreviewCellSize() {
        return PREVIEW_CELL_SIZE;
    }
}