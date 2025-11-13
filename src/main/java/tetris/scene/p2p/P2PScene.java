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
import java.util.Timer;

import tetris.ColorBlindHelper;
import tetris.GameSettings;
import tetris.network.P2PBase;
import tetris.scene.Scene;
import tetris.scene.game.GameScene;
import tetris.scene.game.blocks.*;
import tetris.scene.game.core.BlockManager;
import tetris.scene.game.core.BoardManager;
import tetris.scene.game.core.GameStateManager;
import tetris.scene.game.core.ScoreManager;
import tetris.scene.game.core.GameStateManager.GameState;
import tetris.util.LineBlinkEffect;
import tetris.util.Theme;

import com.google.gson.Gson;

// 통신 메시지 포멧 
class Message {
    //String state;
    char[][] boardTypes;
    char[][] itemTypes;
    char nextBlockType;
    int elapsedSeconds;
    double speedMultiplier;
    double difficultyMultiplier;
    int score;
}

public class P2PScene extends Scene {

    private P2PBase p2p;
    private Timer writeTimer;
    private Thread readThread;
    
    // 왼쪽: 자신의 게임 (조작 가능)
    private GameScene myGamePanel;
    
    // 오른쪽: 상대방의 게임 (읽기 전용 표시)
    private OpponentGamePanel opponentPanel;

    // 렌더링 상수들
    private static final int GAME_WIDTH = 10;
    private static final int GAME_HEIGHT = 20;
    private static final int PREVIEW_SIZE = 4;

    public P2PScene(JFrame frame, P2PBase p2p) {
        super(frame);
        this.p2p = p2p;
        setOpaque(true);
        setBackground(Theme.BG());
        
        // 레이아웃 설정: 자신의 게임(왼쪽) + 상대방 게임(오른쪽)
        setLayout(new GridLayout(1, 2, 10, 0)); // 10px 간격
        
        // 자신의 게임 패널 생성 및 초기화
        myGamePanel = new GameScene(frame, GameSettings.getInstance().getDifficulty());
        myGamePanel.onEnter();
        add(myGamePanel);
        
        // 상대방 게임 패널 생성
        int cellSize = myGamePanel.getUIManager().getCellSize();
        int previewCellSize = myGamePanel.getUIManager().getPreviewCellSize();
        
        JPanel opponentWrapper = new JPanel(new GridBagLayout());
        opponentWrapper.setOpaque(false);
        opponentWrapper.setBackground(Theme.BG());
        add(opponentWrapper);

        opponentPanel = new OpponentGamePanel(
            GAME_WIDTH, GAME_HEIGHT,  
            cellSize,
            PREVIEW_SIZE,  
            previewCellSize);
        opponentPanel.setBackground(Theme.BG());
        opponentPanel.setPreferredSize(myGamePanel.getUIManager().calculateGamePanelSize());
        opponentWrapper.add(opponentPanel, new GridBagConstraints());
        
        frame.setContentPane(this);
        frame.revalidate();
        frame.repaint();

        // 게임 상태 전송 타이머 시작
        writeTimer = new Timer();
        writeTimer.scheduleAtFixedRate(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    String json = serializeGameState();
                    p2p.send(json);
                }
            },
            0, 100
        );

        // 게임 상태 수신 스레드 시작
        readThread = new Thread(()-> {
            while (true) {
                String json = p2p.recieve();
                if (json != null) {
                    deserializeGameState(json);
                }
            }
        });
        readThread.start();
    }

    // 수신된 게임 상태를 역직렬화하여 적용
    void deserializeGameState(String json) {
        Gson gson = new Gson();
        Message message = gson.fromJson(json, Message.class);
        opponentPanel.boardTypes = message.boardTypes;
        opponentPanel.itemTypes = message.itemTypes;
        if(message.nextBlockType == 'I') 
            opponentPanel.nextBlock = new IBlock();
        else if(message.nextBlockType == 'J') 
            opponentPanel.nextBlock = new JBlock();
        else if(message.nextBlockType == 'L') 
            opponentPanel.nextBlock = new LBlock();
        else if(message.nextBlockType == 'O') 
            opponentPanel.nextBlock = new OBlock();
        else if(message.nextBlockType == 'S') 
            opponentPanel.nextBlock = new SBlock();
        else if(message.nextBlockType == 'T') 
            opponentPanel.nextBlock = new TBlock();
        else if(message.nextBlockType == 'Z') 
            opponentPanel.nextBlock = new ZBlock();
        else opponentPanel.nextBlock = new TBlock();

        opponentPanel.elapsedSeconds = message.elapsedSeconds;
        opponentPanel.speedMultiplier = message.speedMultiplier;
        opponentPanel.difficultyMultiplier = message.difficultyMultiplier;
        opponentPanel.score = message.score;
        opponentPanel.repaint();

    }

    // 현재 게임 상태를 직렬화하여 전송
    String serializeGameState() {
        BoardManager boardManager = myGamePanel.getBoardManager();
        int[][] board = boardManager.getBoard();
        Color[][] boardColors = boardManager.getBoardColors();
        char[][] boardTypes = new char[GAME_HEIGHT][GAME_WIDTH];
        char[][] itemTypes = new char[GAME_HEIGHT][GAME_WIDTH];
        final char[] blockTypes = { 'I','J','L','O','S','T','Z' };
        // 보드 상태 변환
        for (int row = 0; row < GAME_HEIGHT; row++) {
            for (int col = 0; col < GAME_WIDTH; col++) {
                if(board[row][col] != 1) {
                    boardTypes[row][col] = ' ';
                    itemTypes[row][col] = ' ';
                    continue;
                }
                // 블록 색상에 따른 타입 결정
                Color color = boardColors[row][col];

                for(char blockType : blockTypes) {
                    if(color.equals(Theme.Block(blockType))) {
                        boardTypes[row][col] = blockType;
                        break;
                    }
                }

                if(!boardManager.isItemCell(col, row)) {
                    continue;
                }
                // 아이템 셀 타입 결정
                String symbol = boardManager.getItemBlockInfo(col, row).getItemSymbol();

                if(symbol == null || symbol.isEmpty()) continue;
                itemTypes[row][col] = symbol.charAt(0);
            }
        }

        // 현재 낙하중인 블록 정보 추가
        BlockManager blockManager = myGamePanel.getBlockManager();
        Block currentBlock = blockManager.getCurrentBlock();
        int blockX = blockManager.getX();
        int blockY = blockManager.getY();
        Color blockColor = currentBlock.getColor();
        char colorSymbol = ' ';
        for(char blockType : blockTypes) {
            if(blockColor.equals(Theme.Block(blockType))) {
                colorSymbol = blockType;
                break;
            }
        }
        for (int r = 0; r < currentBlock.height(); r++) {
            for (int c = 0; c < currentBlock.width(); c++) {
                if(currentBlock.getShape(c, r) == 1) {
                    boardTypes[blockY + r][blockX + c] = colorSymbol;
                }
            }
        }

        Block nextBlock = blockManager.getNextBlock();
        Color nextBlockColor = nextBlock.getColor();
        char nextBlockType = ' ';
        for(char blockType : blockTypes) {
            if(nextBlockColor.equals(Theme.Block(blockType))) {
                nextBlockType = blockType;
                break;
            }
        }

        GameStateManager gameStateManager = myGamePanel.getGameStateManager();
        int elapsedSeconds = gameStateManager.getElapsedTimeInSeconds();

        ScoreManager scoreManager = myGamePanel.getScoreManager();
        double speedMultiplier = scoreManager.getSpeedMultiplier();
        double difficultyMultiplier = scoreManager.getDifficultyMultiplier();
        int score = scoreManager.getScore();

        Gson gson = new Gson();
        Message message = new Message();
        message.boardTypes = boardTypes;
        message.itemTypes = itemTypes;
        message.nextBlockType = nextBlockType;
        message.elapsedSeconds = elapsedSeconds;
        message.speedMultiplier = speedMultiplier;
        message.difficultyMultiplier = difficultyMultiplier;
        message.score = score;
        
        return gson.toJson(message);
    }
}


/**
 * 상대방의 게임 화면을 표시하는 패널
 * 네트워크를 통해 받은 상대방의 게임 상태를 실시간으로 렌더링합니다.
 */
class OpponentGamePanel extends JPanel {
    private final int GAME_WIDTH;
    private final int GAME_HEIGHT;
    private final int CELL_SIZE;
    private final int PREVIEW_SIZE;
    private final int PREVIEW_CELL_SIZE;

    char[][] boardTypes;
    char[][] itemTypes;
    Block nextBlock;
    int elapsedSeconds = 20;
    double speedMultiplier = 1.2f;
    double difficultyMultiplier = 1.0f;
    int score = 100;

    // 추가 플래그: 게임 종료, 중단, 라인삭제 이펙트, 아이템 이펙트
    
    // 이미지 캐시
    private final Map<Character, BufferedImage> imageCache = new HashMap<>();
    
    public OpponentGamePanel(int gameWidth, int gameHeight, int cellSize, int previewSize, int previewCellSize) {
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

        nextBlock = new TBlock();

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

        renderNextBlockPreview(g2d);

        renderScoreBoard(g2d);
        
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


    private void renderNextBlockPreview(Graphics2D g2d) {
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
    private void renderScoreBoard(Graphics2D g2d) {
        // 점수판 위치 계산 (다음 블록 미리보기 아래)
        int previewX = (GAME_WIDTH + 2) * CELL_SIZE + 20;
        int previewY = CELL_SIZE + 20;
        int previewAreaSize = PREVIEW_SIZE * PREVIEW_CELL_SIZE;
        
        int scoreBoardX = previewX;
        int scoreBoardY = previewY + previewAreaSize + 30; // 미리보기 아래 30px 간격
        int scoreBoardWidth = previewAreaSize;
        int scoreBoardHeight = 120; // 점수판 높이

        drawScoreBoard(g2d, scoreBoardX, scoreBoardY, scoreBoardWidth, scoreBoardHeight);
        
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
        String timeText = formatTime(elapsedSeconds);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        fm = g2d.getFontMetrics();
        int timeWidth = fm.stringWidth(timeText);
        g2d.drawString(timeText, x + (width - timeWidth) / 2, y + 40);
    }

    /**
     * 점수판을 그립니다.
     * @param g2d Graphics2D 객체
     * @param scoreBoardX 점수판 x 좌표
     * @param scoreBoardY 점수판 y 좌표
     * @param scoreBoardWidth 점수판 너비
     * @param scoreBoardHeight 점수판 높이
     */
    public void drawScoreBoard(Graphics2D g2d, int scoreBoardX, int scoreBoardY, 
                              int scoreBoardWidth, int scoreBoardHeight) {
        // 점수판 배경
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(scoreBoardX, scoreBoardY, scoreBoardWidth, scoreBoardHeight);

        // 점수판 테두리
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(scoreBoardX, scoreBoardY, scoreBoardWidth, scoreBoardHeight);

        // 점수 정보 표시
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();

        // "SCORE" 라벨
        String scoreLabel = "SCORE";
        int labelWidth = fm.stringWidth(scoreLabel);
        g2d.drawString(scoreLabel, scoreBoardX + (scoreBoardWidth - labelWidth) / 2, scoreBoardY + 20);

        // 현재 점수
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        fm = g2d.getFontMetrics();
        String scoreText = String.format("%,d", score);
        int scoreWidth = fm.stringWidth(scoreText);
        g2d.drawString(scoreText, scoreBoardX + (scoreBoardWidth - scoreWidth) / 2, scoreBoardY + 45);
        
        // 배율 정보 표시 (디버깅용)
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        fm = g2d.getFontMetrics();
        
        // 속도 배율
        String speedMultiplierText = String.format("Speed: %.1fx", speedMultiplier);
        int speedMultiplierWidth = fm.stringWidth(speedMultiplierText);
        g2d.drawString(speedMultiplierText, scoreBoardX + (scoreBoardWidth - speedMultiplierWidth) / 2, scoreBoardY + 65);
        
        // 난이도 배율
        String difficultyMultiplierText = String.format("Difficulty: %.1fx", difficultyMultiplier);
        int difficultyMultiplierWidth = fm.stringWidth(difficultyMultiplierText);
        g2d.drawString(difficultyMultiplierText, scoreBoardX + (scoreBoardWidth - difficultyMultiplierWidth) / 2, scoreBoardY + 78);
        
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
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
