package tetris.scene.p2p;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import tetris.GameSettings.Difficulty;
import tetris.scene.Scene;
import tetris.scene.game.GameScene;
import tetris.scene.game.blocks.Block;
import tetris.util.LineBlinkEffect;
import tetris.util.Theme;

public class P2PScene extends Scene {

    boolean isPaused = false;

    public P2PScene(JFrame frame, Difficulty diff) {
        super(frame);
        setOpaque(true);
        setBackground(Theme.BG());
        
        setLayout(new GridLayout(1,2));
        GameScene gamePanel = new GameScene(frame, diff);
        gamePanel.onEnter();
        add(gamePanel);

        JPanel sideWrapper = new JPanel(new GridBagLayout());
        sideWrapper.setOpaque(false);
        add(sideWrapper);

        JPanel sidePanel = new SidePanel(
            10,20,  
            gamePanel.getUIManager().getCellSize(),
            4,  
            gamePanel.getUIManager().getPreviewCellSize());
        //sidePanel.setOpaque(false);
        sidePanel.setBackground(Theme.BLACK);
        sidePanel.setPreferredSize(gamePanel.getUIManager().calculateGamePanelSize());
        sideWrapper.add(sidePanel, new GridBagConstraints());
        
        frame.setContentPane(this);
        frame.revalidate();
        frame.repaint();
    }

    
}

class SidePanel extends JPanel {
    // 렌더링 상수들
    private final int GAME_WIDTH;
    private final int GAME_HEIGHT;
    private final int CELL_SIZE;
    private final int PREVIEW_SIZE;
    private final int PREVIEW_CELL_SIZE;

    char[][] boardTypes;
    char[][] itemTypes;
    Block nextBlock;
    
    // 이미지 캐시
    private final Map<Character, BufferedImage> imageCache = new HashMap<>();
    
    public SidePanel(int gameWidth, int gameHeight, int cellSize, int previewSize, int previewCellSize) {
        this.GAME_WIDTH = gameWidth;
        this.GAME_HEIGHT = gameHeight;
        this.CELL_SIZE = cellSize;
        this.PREVIEW_SIZE = previewSize;
        this.PREVIEW_CELL_SIZE = previewCellSize;

        imageCache.put('C', loadImage("broom.png") );
        imageCache.put('S', loadImage("snail.png") );
        imageCache.put('F', loadImage("running.png") );
        imageCache.put('V', loadImage("visionblock.png") );
        imageCache.put('L', null ); // 'L' 심볼은 별도 렌더링

        boardTypes = new char[GAME_HEIGHT][GAME_WIDTH];
        itemTypes = new char[GAME_HEIGHT][GAME_WIDTH];
        for (int r = 0; r < GAME_HEIGHT; r++) {
            for (int c = 0; c < GAME_WIDTH; c++) {
                boardTypes[r][c] = ' ';
                itemTypes[r][c] = ' ';
            }
        }

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // 안티알리아싱 설정
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
        // 게임 보드 경계 및 그리드 렌더링
        renderBoard(g2d);
        
        // 고정된 블록들 렌더링 (점멸 중인 셀들은 제외) + 현재 블럭까지
        renderBlocks(g2d);
        
    }
    
    /**
     * 전체 게임 화면을 렌더링합니다.
     */
    public void render(
        Graphics2D g2d, 
        int panelWidth, int panelHeight, 
        LineBlinkEffect lineBlinkEffect, 
        boolean isPaused,
        boolean visionBlockActive,
        boolean cleanupBlinkingActive, 
        Set<Point> cleanupBlinkingCells
        ) {




        // 기타 패널 추가 예정
        
        // 다음 블록 미리보기 렌더링
        //renderNextBlockPreview(g2d);
        
        // 점수판 렌더링
        //renderScoreBoard(g2d);

        // 시간 보드 렌더링
        //renderTimeBoard(g2d, 20, panelHeight - 80, panelWidth - 40, 60);
        


        // 이펙트 렌더링 추가 예정

        // 줄 점멸 효과 렌더링
        // renderLineBlinkEffect(g2d, lineBlinkEffect);
        
        // 시야 차단 효과 렌더링
        // if (visionBlockActive) {
        //     renderVisionBlockEffect(g2d);
        // }
        
        // 청소 블링킹 효과 렌더링
        // if (cleanupBlinkingActive && cleanupBlinkingCells != null && !cleanupBlinkingCells.isEmpty()) {
        //     renderCleanupBlinkingEffect(g2d, cleanupBlinkingCells);
        // }
    }

    
    private void renderBoard(Graphics2D g2d) {
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
    
    private void renderBlocks(Graphics2D g2d) {
        for (int row = 0; row < GAME_HEIGHT; row++) {
            for (int col = 0; col < GAME_WIDTH; col++) {
                if (boardTypes[row][col] != ' ') { 
                    Color blockColor = Theme.Block(boardTypes[row][col]);
                    int drawX = (col + 1) * CELL_SIZE + 1;
                    int drawY = (row + 1) * CELL_SIZE + 1;
                    g2d.setColor(blockColor);
                    g2d.fillRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);

                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRect(drawX, drawY, CELL_SIZE - 2, CELL_SIZE - 2);

                    // 색맹 모드에서 패턴 추가
                    int colorBlindMode = tetris.GameSettings.getInstance().getColorBlindMode();
                    if (colorBlindMode > 0) {
                        int blockType = boardTypes[row][col];
                        if (blockType >= 0) {
                            tetris.ColorBlindHelper.drawBlockPattern(g2d, blockType, drawX, drawY, CELL_SIZE - 2, colorBlindMode, blockColor);
                        }
                    }
                }

                if(itemTypes[row][col] != ' ') {

                    BufferedImage itemImage = imageCache.get(itemTypes[row][col]);
                    int drawX = (col + 1) * CELL_SIZE + 1;
                    int drawY = (row + 1) * CELL_SIZE + 1;

                    if (itemImage != null) {
                        int imageSize = CELL_SIZE - 2;
                        int imageX = (col + 1) * CELL_SIZE + 1;
                        int imageY = (row + 1) * CELL_SIZE + 1;
                        g2d.drawImage(itemImage, imageX, imageY, imageSize, imageSize, null);
                    } else {

                        Graphics2D g = (Graphics2D) g2d.create();
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        // 흰색 'L' 글자
                        g.setColor(Color.WHITE);
                        
                        // 폰트 크기를 셀 크기에 맞게 조정 (더 크게)
                        int fontSize = Math.max(CELL_SIZE * 3 / 4, 12); // 셀 크기의 75%로 더 크게
                        g.setFont(new Font("Arial", Font.BOLD, fontSize));
                        
                        // 'L' 글자 중앙 배치
                        FontMetrics fm = g.getFontMetrics();
                        String letter = "L";
                        int letterWidth = fm.stringWidth(letter);
                        int letterHeight = fm.getAscent();
                        
                        int letterX = drawX + (CELL_SIZE - letterWidth) / 2;
                        int letterY = drawY + (CELL_SIZE + letterHeight) / 2 - fm.getDescent();
                        
                        g.drawString(letter, letterX, letterY);
                        g.dispose();
                    }
                }
            }
        }
    }

    private BufferedImage loadImage(String imagePath) {
        try {
            InputStream imageStream = getClass().getResourceAsStream("/" + imagePath);
            if (imageStream != null) {
                BufferedImage image = ImageIO.read(imageStream);
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
}
