package tetris.scene.battle;

import java.awt.Color;
import java.util.Timer;

import javax.swing.JFrame;

import com.google.gson.Gson;

import tetris.network.P2PBase;
import tetris.scene.game.blocks.Block;
import tetris.scene.game.core.*;
import tetris.util.Theme;

// 직렬화된 게임 상태를 저장할 필드들
class SerializedGameState {

    // 게임 보드 (현재 블럭 포함)
    int[][] board;
    char[][] boardColors;
    int[][] boardTypes;

    boolean[][] bombCells;
    boolean[][] itemCells;
    String[][] itemBlockInfo;

    // 다음 블럭
    int type;

    // 기타 정보
    int score; 
    double speedMultiplier; 
    double difficultyMultiplier; 
    int elapsedSeconds;

    // 게임 오버 플래그
    boolean gameOverFlag;

    // 공격 블럭 정보
    String[] serializedAttackBlocks;

}

class SerializabledAttackBlock {
    int width;
    boolean[] pattern;
    int[] colors;
    int[] blockTypes;
    SerializabledAttackBlock(AttackBlock ab) {
        this.width = ab.getWidth();
        this.pattern = new boolean[width];
        this.colors = new int[width];
        this.blockTypes = new int[width];
        for(int c = 0; c < width; c++) {
            this.pattern[c] = ab.hasBlockAt(c);
            Color color = ab.getColorAt(c);
            if(color == null) {
                this.colors[c] = 0;
            } else {
                this.colors[c] = color.getRGB();
            }
            this.blockTypes[c] = ab.getBlockTypeAt(c);
        }
    }
    AttackBlock toAttackBlock() {
        Color[] cols = new Color[width];
        for(int c = 0; c < width; c++) {
            if(colors[c] == ' ') {
                cols[c] = null;
            } else {
                cols[c] = new Color(colors[c]);
            }
        }
        return new AttackBlock(width, pattern, cols, blockTypes);
    }
}

public class P2PBattleScene extends BattleScene {

    P2PBase p2p;
    Timer writeTimer;
    Thread readThread;

    // 블럭 타입 매핑
    final char[] blockTypes = { 'I','J','L','O','S','T','Z' };

    public P2PBattleScene(JFrame frame, String gameMode, P2PBase p2p) {
        super(frame, gameMode);

        this.inputHandler1 = new InputHandler(frame, new Player1Callback(), 0);

        this.gameStateManager2 = new GameStateManager(new EmptyCallback());
        this.inputHandler2 = new InputHandler(frame, new EmptyCallback(), 2); 
        this.blockManager2.resetBlock();

        this.p2p = p2p;

        // 게임 상태 전송 타이머 시작
        writeTimer = new Timer();
        writeTimer.scheduleAtFixedRate(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    String send = serializeGameState();
                    p2p.send(send);
                }
            },
            0, 100
        );

        // 게임 상태 수신 스레드 시작
        readThread = new Thread(()-> {
            while (true) {
                String receive = p2p.receive();
                if (receive != null) {
                    deserializeGameState(receive);
                }
            }
        });
        readThread.start();

    }

    // 수신된 게임 상태를 역직렬화하여 적용
    void deserializeGameState(String serialized) {
        Gson gson = new Gson();
        SerializedGameState state = gson.fromJson(serialized, SerializedGameState.class);

        boardManager2.setBoard(state.board);
        boardManager2.setBoardTypes(state.boardTypes);
        boardManager2.setBombCells(state.bombCells);
        boardManager2.setItemCells(state.itemCells);

        final int width = state.board[0].length;
        final int height = state.board.length;

        Color[][] bc = new Color[height][width];
        // ItemBlock[][] ib = new ItemBlock[bc.length][bc[0].length];
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if(state.boardColors[r][c] == ' ') {
                    bc[r][c] = null;
                } else if(state.boardColors[r][c] == 'B') {
                    bc[r][c] = Color.BLACK;
                } else if(state.boardColors[r][c] == 'G') {
                    bc[r][c] = Color.GRAY;
                } else {
                    bc[r][c] = Theme.Block(state.boardColors[r][c]);
                }
                if(state.itemCells[r][c]){}
                    // boardManager2.setItemBlockInfo(r, c, new ItemBlock(state.itemBlockInfo[r][c]));
            }
        }
        boardManager2.setBoardColors(bc);

        blockManager2.setNextBlock(state.type);

        scoreManager2.setScore(state.score);
        scoreManager2.setSpeedMultiplier(state.speedMultiplier);
        scoreManager2.setDifficultyMultiplier(state.difficultyMultiplier);
        repaint();
        //gameStateManager2.setElapsedSeconds(state.elapsedSeconds);

        if(state.gameOverFlag && !this.isGameOver) {
            this.handleGameOver(2); // 2P 패배 처리
        }

        for(int i = 0; i < state.serializedAttackBlocks.length; i++) {
            String serializedAB = state.serializedAttackBlocks[i];
            SerializabledAttackBlock sab = gson.fromJson(serializedAB, SerializabledAttackBlock.class);
            attackQueue1.add(sab.toAttackBlock());
        }

    }

    // 현재 게임 상태를 직렬화하여 전송
    String serializeGameState() {
        SerializedGameState state = new SerializedGameState();

        int[][] board = boardManager1.getBoard();
        int[][] boardTypes = boardManager1.getBoardTypes();
        boolean[][] bombCells = boardManager1.getBombCells();
        boolean[][] itemCells = boardManager1.getItemCells();
        Color[][] bc = boardManager1.getBoardColors();

        state.board = copy2DInt(board);
        state.boardTypes = copy2DInt(boardTypes);
        state.bombCells = copy2DBool(bombCells);
        state.itemCells = copy2DBool(itemCells);

        final int width = state.board[0].length;
        final int height = state.board.length;
        state.boardColors = new char[height][width];
        state.itemBlockInfo = new String[height][width];
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                for(char blockType : blockTypes) {
                    if(bc[r][c] == null) {
                        state.boardColors[r][c] = ' ';
                        break;
                    }
                    if(bc[r][c].equals(Theme.Block(blockType))) {
                        state.boardColors[r][c] = blockType;
                        break;
                    }
                    if(bc[r][c].equals(Color.BLACK)) state.boardColors[r][c] = 'B';
                    else if(bc[r][c].equals(Color.GRAY)) state.boardColors[r][c] = 'G';
                }
                if(state.itemCells[r][c]) state.itemBlockInfo[r][c] = boardManager1.getItemBlockInfo(r,c).getItemDisplayName();
            }
        }

        // 현재 낙하중인 블록 정보 추가
        Block currentBlock = blockManager1.getCurrentBlock();
        int blockX = blockManager1.getX();
        int blockY = blockManager1.getY();
        Color blockColor = blockManager1.getCurrentBlock().getColor();
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
                    state.board[blockY + r][blockX + c] = 1;
                    state.boardColors[blockY + r][blockX + c] = colorSymbol;
                    state.boardTypes[blockY + r][blockX + c] = currentBlock.getType();
                }
            }
        }

        state.type = blockManager1.getNextBlock().getType();


        state.score = scoreManager1.getScore();
        state.speedMultiplier = scoreManager1.getSpeedMultiplier();
        state.difficultyMultiplier = scoreManager1.getDifficultyMultiplier();
        state.elapsedSeconds = gameStateManager1.getElapsedTimeInSeconds();

        state.gameOverFlag = this.isGameOver;

        int qSize = attackQueue2.size();
        
        state.serializedAttackBlocks = new String[qSize];
        for(int i = 0; i < qSize; i++) {
            AttackBlock ab = attackQueue2.poll();
            SerializabledAttackBlock sab = new SerializabledAttackBlock(ab);
            Gson gson = new Gson();
            String serializedAB = gson.toJson(sab);
            state.serializedAttackBlocks[i] = serializedAB;
        }

        Gson gson = new Gson();
        return gson.toJson(state);
    }

    class EmptyCallback implements InputHandler.InputCallback, GameStateManager.StateChangeCallback {
        @Override
        public void onGameAction(InputHandler.GameAction action) { }
        
        @Override
        public boolean isGameOver() { return gameStateManager2.isGameOver(); }       
        
        @Override
        public boolean isPaused() { return gameStateManager2.isPaused(); }
        
        @Override
        public void repaintGame() { repaint(); }
        
        @Override
        public void onStateChanged(GameStateManager.GameState oldState, GameStateManager.GameState newState) { }
        
        @Override
        public void onPauseToggled(boolean isPaused) { }
        
        @Override
        public void onGameOver() { }
    }

    private int[][] copy2DInt(int[][] src) {
        int[][] dst = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i].clone();
        }
        return dst;
    }

    private boolean[][] copy2DBool(boolean[][] src) {
        boolean[][] dst = new boolean[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i].clone();
        }
        return dst;
    }

    /**
     * BattleScene의 게임 오버 다이얼로그를 P2P 전용으로 오버라이드
     */
    @Override
    protected void showBattleGameOverDialog(int winner) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            // 메인메뉴 스타일의 다이얼로그 생성
            javax.swing.JDialog dialog = new javax.swing.JDialog(m_frame, true);
            dialog.setUndecorated(true);
            dialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
            dialog.setResizable(false);
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(m_frame);
            dialog.setFocusable(true);
            
            javax.swing.JPanel dialogPanel = new javax.swing.JPanel();
            dialogPanel.setBackground(tetris.util.Theme.MenuBG());
            dialogPanel.setLayout(new java.awt.BorderLayout());
            dialogPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(tetris.util.Theme.MenuTitle(), 2),
                javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
            
            // 게임 모드에 따른 다이얼로그 구성
            if ("time_limit".equals(gameMode)) {
                // 시간제한 모드: 점수 표시 포함
                setupP2PTimeLimitModeDialog(dialogPanel, winner);
            } else {
                // 일반 모드, 아이템 모드: 승자만 표시
                setupP2PNormalModeDialog(dialogPanel, winner);
            }
            
            dialog.add(dialogPanel);
            dialog.setVisible(true);
            dialog.requestFocus();
        });
    }
    
    private void setupP2PNormalModeDialog(javax.swing.JPanel dialogPanel, int winner) {
        // 제목 라벨
        javax.swing.JLabel titleLabel = new javax.swing.JLabel("게임 종료", javax.swing.SwingConstants.CENTER);
        titleLabel.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.BOLD, 20));
        titleLabel.setForeground(tetris.util.Theme.MenuTitle());
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // 중앙 패널 (승자 정보 + 설명)
        javax.swing.JPanel centerPanel = new javax.swing.JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new java.awt.GridLayout(3, 1, 0, 10));
        
        // 승자 표시
        javax.swing.JLabel winnerLabel = new javax.swing.JLabel();
        winnerLabel.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.BOLD, 18));
        
        String winnerText;
        String modeDescription = "";
        
        if ("item".equals(gameMode)) {
            modeDescription = "아이템 모드: ";
        } else {
            modeDescription = "일반 모드: ";
        }
        
        if (winner == 1) {
            winnerText = modeDescription + "서버 승리!";
        } else if (winner == 2) {
            winnerText = modeDescription + "클라이언트 승리!";
        } else {
            winnerText = modeDescription + "무승부!";
        }
        winnerLabel.setForeground(new java.awt.Color(255, 215, 0)); // Gold color
        winnerLabel.setText(winnerText);
        winnerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        // 게임 종료 사유 표시
        javax.swing.JLabel reasonLabel = new javax.swing.JLabel("게임종료조건: 블록이 먼저 천장에 닿으면 패배");
        reasonLabel.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.PLAIN, 12));
        reasonLabel.setForeground(java.awt.Color.WHITE);
        reasonLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        centerPanel.add(winnerLabel);
        centerPanel.add(reasonLabel);
        centerPanel.add(new javax.swing.JLabel()); // 빈 공간
        
        // 버튼 패널
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new java.awt.GridLayout(1, 1, 0, 10));
        
        // 메인 메뉴로 돌아가기 버튼
        javax.swing.JButton mainMenuButton = new javax.swing.JButton("메인 메뉴로 돌아가기");
        mainMenuButton.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.BOLD, 14));
        mainMenuButton.setPreferredSize(new java.awt.Dimension(250, 35));
        mainMenuButton.setBackground(tetris.util.Theme.MenuButton());
        mainMenuButton.setForeground(java.awt.Color.WHITE);
        mainMenuButton.setFocusPainted(false);
        mainMenuButton.setBorderPainted(true);
        mainMenuButton.setBorder(javax.swing.BorderFactory.createRaisedBevelBorder());
        mainMenuButton.addActionListener(e -> {
            ((javax.swing.JDialog)dialogPanel.getTopLevelAncestor()).dispose();
            returnToMainMenu();
        });
        
        buttonPanel.add(mainMenuButton);
        
        // 컴포넌트 배치
        dialogPanel.add(titleLabel, java.awt.BorderLayout.NORTH);
        dialogPanel.add(centerPanel, java.awt.BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
    }
    
    private void setupP2PTimeLimitModeDialog(javax.swing.JPanel dialogPanel, int winner) {
        // 제목 라벨
        javax.swing.JLabel titleLabel = new javax.swing.JLabel("게임 종료", javax.swing.SwingConstants.CENTER);
        titleLabel.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.BOLD, 20));
        titleLabel.setForeground(tetris.util.Theme.MenuTitle());
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // 중앙 패널 (승자 정보 + 점수 + 설명)
        javax.swing.JPanel centerPanel = new javax.swing.JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new java.awt.GridLayout(4, 1, 0, 8));
        
        // 승자 표시
        javax.swing.JLabel winnerLabel = new javax.swing.JLabel();
        winnerLabel.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.BOLD, 18));
        
        String winnerText = "시간제한 모드: ";
        if (winner == 1) {
            winnerText += "서버 승리!";
        } else if (winner == 2) {
            winnerText += "클라이언트 승리!";
        } else {
            winnerText += "무승부!";
        }
        
        winnerLabel.setForeground(new java.awt.Color(255, 215, 0)); // Gold color
        winnerLabel.setText(winnerText);
        winnerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        // 게임 종료 사유 표시
        javax.swing.JLabel reasonLabel = new javax.swing.JLabel("<html><center>게임종료조건: 블록이 먼저 천장에 닿으면 패배<br>또는 시간 종료 후 점수가 더 높은 쪽이 승리</center></html>");
        reasonLabel.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.PLAIN, 11));
        reasonLabel.setForeground(java.awt.Color.WHITE);
        reasonLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        // 플레이어 점수 표시 패널
        javax.swing.JPanel scorePanel = new javax.swing.JPanel();
        scorePanel.setOpaque(false);
        scorePanel.setLayout(new java.awt.GridLayout(1, 2, 20, 0));
        
        int player1Score = scoreManager1.getScore();
        int player2Score = scoreManager2.getScore();
        
        javax.swing.JLabel player1ScoreLabel = new javax.swing.JLabel("서버: " + String.format("%,d", player1Score));
        player1ScoreLabel.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.BOLD, 14));
        player1ScoreLabel.setForeground(winner == 1 ? new java.awt.Color(255, 215, 0) : java.awt.Color.WHITE);
        player1ScoreLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        javax.swing.JLabel player2ScoreLabel = new javax.swing.JLabel("클라이언트: " + String.format("%,d", player2Score));
        player2ScoreLabel.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.BOLD, 14));
        player2ScoreLabel.setForeground(winner == 2 ? new java.awt.Color(255, 215, 0) : java.awt.Color.WHITE);
        player2ScoreLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        scorePanel.add(player1ScoreLabel);
        scorePanel.add(player2ScoreLabel);
        
        centerPanel.add(winnerLabel);
        centerPanel.add(reasonLabel);
        centerPanel.add(scorePanel);
        centerPanel.add(new javax.swing.JLabel()); // 빈 공간
        
        // 버튼 패널
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new java.awt.GridLayout(1, 1, 0, 10));
        
        // 메인 메뉴로 돌아가기 버튼
        javax.swing.JButton mainMenuButton = new javax.swing.JButton("메인 메뉴로 돌아가기");
        mainMenuButton.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.BOLD, 14));
        mainMenuButton.setPreferredSize(new java.awt.Dimension(250, 35));
        mainMenuButton.setBackground(tetris.util.Theme.MenuButton());
        mainMenuButton.setForeground(java.awt.Color.WHITE);
        mainMenuButton.setFocusPainted(false);
        mainMenuButton.setBorderPainted(true);
        mainMenuButton.setBorder(javax.swing.BorderFactory.createRaisedBevelBorder());
        mainMenuButton.addActionListener(e -> {
            ((javax.swing.JDialog)dialogPanel.getTopLevelAncestor()).dispose();
            returnToMainMenu();
        });
        
        buttonPanel.add(mainMenuButton);
        
        // 컴포넌트 배치
        dialogPanel.add(titleLabel, java.awt.BorderLayout.NORTH);
        dialogPanel.add(centerPanel, java.awt.BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
    }
    
    /**
     * P2P 전용 메인 메뉴 복귀 처리 (네트워크 리소스 정리 후 메인 메뉴로 복귀)
     */
    @Override
    protected void returnToMainMenu() {
        // P2P 네트워크 리소스 정리
        if (writeTimer != null) {
            writeTimer.cancel();
            writeTimer = null;
        }
        
        if (readThread != null) {
            readThread.interrupt();
            readThread = null;
        }
        
        // 부모 클래스의 메인 메뉴 복귀 로직 호출
        super.returnToMainMenu();
    }

}

