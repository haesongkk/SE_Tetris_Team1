package tetris.scene.battle;

import java.awt.Color;
import java.util.Timer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.google.gson.Gson;

import tetris.Game;
import tetris.network.P2PBase;
import tetris.scene.game.blocks.Block;
import tetris.scene.game.core.*;
import tetris.scene.menu.MainMenuScene;
import tetris.scene.menu.P2PRoomDialog;
import tetris.util.Theme;

// 직렬화된 게임 상태를 저장할 필드들
class SerializedGameState {

    // 전송 시간
    long timestamp;

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

    // 일시정지 플래그 (상태)
    boolean pauseFlag;

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

    // 상대방이 보낸 pauseFlag의 "마지막 값"을 기억
    private boolean hasRemotePauseState = false;
    private boolean lastRemotePauseState = false;

    private boolean prevPauseState = false;

    boolean bCloseByGameOver = false;
    boolean bCloseByDisconnect = false;

    final long MAX_LATENCY_MS = 200;

    // 블럭 타입 매핑
    final char[] blockTypes = { 'I','J','L','O','S','T','Z' };

    public P2PBattleScene(JFrame frame, String gameMode, P2PBase p2p) {
        super(frame, gameMode);

        this.inputHandler1 = new InputHandler(frame, new Player1Callback(), 0);

        this.gameStateManager2 = new GameStateManager(new EmptyCallback());
        this.inputHandler2 = new InputHandler(frame, new EmptyCallback(), 2); 
        this.blockManager2.resetBlock();

        // 매니저 설정을 덮어씌운 후 다시 호출해야함
        super.setupLayout(frame);

        this.p2p = p2p;

        // 게임 상태 전송 타이머 시작
        writeTimer = new Timer();
        writeTimer.scheduleAtFixedRate(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    String send = serializeGameState();
                    p2p.send("board:" + send);
                }
            },
            100, 100
        );

        p2p.addCallback("board:", (serialized) -> {
            deserializeGameState(serialized);
        });
        p2p.addCallback("attack-generate:", (serialized) -> {
            Gson gson = new Gson();
            SerializabledAttackBlock sab = gson.fromJson(serialized, SerializabledAttackBlock.class);
            AttackBlock ab = sab.toAttackBlock();
            attackQueue1.push(ab);
        });
        p2p.addCallback("attack-apply", (s) -> {
            attackQueue2.clear();
        });
        p2p.setOnDisconnect(() -> {
            SwingUtilities.invokeLater(() -> {
                showDisconnectDialog();
            });
        });

    }

    // 수신된 게임 상태를 역직렬화하여 적용
    void deserializeGameState(String serialized) {
        Gson gson = new Gson();
        SerializedGameState state = gson.fromJson(serialized, SerializedGameState.class);

        long currentTimestamp = System.currentTimeMillis();
        long latency = currentTimestamp - state.timestamp;
        handleLatency(latency);

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
        gameStateManager2.setFixedElapsedTime(state.elapsedSeconds);

        if(state.gameOverFlag && !this.isGameOver) {
            this.handleGameOver(2); // 2P 패배 처리
        }

        // 일시정지 상태 동기화 (게임 오버가 아닐 때만)
        if (!this.isGameOver && !gameStateManager1.isGameOver()) {
            boolean remoteIsPaused = state.pauseFlag;

            // 1) "상대방이 보낸 값"이 이전과 달라질 때만 딱 한 번 반응
            if (!hasRemotePauseState || lastRemotePauseState != remoteIsPaused) {

                hasRemotePauseState = true;
                lastRemotePauseState = remoteIsPaused;


                if (gameStateManager1.isPaused() != remoteIsPaused) {
                    gameStateManager1.togglePause();
                }
            }
        }

    }

    // 현재 게임 상태를 직렬화하여 전송
    String serializeGameState() {
        SerializedGameState state = new SerializedGameState();

        // 현재 시간 기록
        state.timestamp = System.currentTimeMillis();

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
        state.pauseFlag = gameStateManager1.isPaused();
        if(prevPauseState != gameStateManager1.isPaused()) gameStateManager2.togglePause();
        prevPauseState = gameStateManager1.isPaused();

        

        Gson gson = new Gson();
        return gson.toJson(state);
    }

    @Override
    protected void applyAttackBlocks(int player) {
        super.applyAttackBlocks(player);
        if(player != 1) return;
        p2p.send("attack-apply");
    }

    @Override
    protected void generateAttackBlocks(java.util.List<Integer> clearedLines, int targetPlayer) {
        super.generateAttackBlocks(clearedLines, targetPlayer);
        if(targetPlayer == 1) return;

        for(int i = 0; i < attackQueue2.size(); i++) {
            AttackBlock ab = attackQueue2.get(i);
            SerializabledAttackBlock sab = new SerializabledAttackBlock(ab);
            Gson gson = new Gson();
            String serializedAB = gson.toJson(sab);
            p2p.send("attack-generate:" + serializedAB);
        }
    }

    private void handleLatency(long latency) {
        //System.out.println("지연 시간: " + latency + " ms");
        if(latency > MAX_LATENCY_MS) {
            // 지연 시간이 높을 때 처리
            SwingUtilities.invokeLater(() -> {
                showDisconnectDialog();
            });
        }
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
        if(bCloseByDisconnect || bCloseByGameOver) return;
        bCloseByGameOver = true;
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
            winnerText = modeDescription + "승리!";
        } else if (winner == 2) {
            winnerText = modeDescription + "패배!";
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
            exit(false);
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
            winnerText += "승리!";
        } else if (winner == 2) {
            winnerText += "패배!";
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
        
        javax.swing.JLabel player1ScoreLabel = new javax.swing.JLabel("당신: " + String.format("%,d", player1Score));
        player1ScoreLabel.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.BOLD, 14));
        player1ScoreLabel.setForeground(winner == 1 ? new java.awt.Color(255, 215, 0) : java.awt.Color.WHITE);
        player1ScoreLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        javax.swing.JLabel player2ScoreLabel = new javax.swing.JLabel("상대: " + String.format("%,d", player2Score));
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
            exit(false);
        });
        
        buttonPanel.add(mainMenuButton);
        
        // 컴포넌트 배치
        dialogPanel.add(titleLabel, java.awt.BorderLayout.NORTH);
        dialogPanel.add(centerPanel, java.awt.BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
    }

    private void showDisconnectDialog() {
        if(bCloseByDisconnect || bCloseByGameOver) return;
        bCloseByDisconnect = true;
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
        // 제목 라벨
        javax.swing.JLabel titleLabel = new javax.swing.JLabel("서버 연결 오류", javax.swing.SwingConstants.CENTER);
        titleLabel.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.BOLD, 20));
        titleLabel.setForeground(tetris.util.Theme.MenuTitle());
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // 중앙 패널 (승자 정보 + 설명)
        javax.swing.JPanel centerPanel = new javax.swing.JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new java.awt.GridLayout(3, 1, 0, 10));
        
        javax.swing.JLabel description = new javax.swing.JLabel();
        description.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.BOLD, 18));
        
        description.setForeground(new java.awt.Color(255, 215, 0)); // Gold color
        description.setText("상대방과 연결이 끊어졌습니다.");
        description.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        centerPanel.add(description);
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
            exit(true);
        });
        
        buttonPanel.add(mainMenuButton);
        
        // 컴포넌트 배치
        dialogPanel.add(titleLabel, java.awt.BorderLayout.NORTH);
        dialogPanel.add(centerPanel, java.awt.BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        dialog.add(dialogPanel);
        dialog.setVisible(true);
        dialog.requestFocus();
    }

    // 게임 종료 또는 연결 끊김으로 인한 메인 메뉴 복귀 처리
    private void exit(boolean exitWithDisconnect) {
        // 리소스 정리
        if(p2p != null) {
            p2p.removeCallback("game:");
            p2p.removeCallback("attack-generate:");
            p2p.removeCallback("attack-apply");
            p2p.setOnDisconnect(null); // onDisconnect 콜백 제거
        }
        if (writeTimer != null) { 
            writeTimer.cancel(); 
            writeTimer.purge(); // 완전히 정리
        }
        
        if(exitWithDisconnect) {
            // 연결 끊김: p2p를 release하고 새로운 연결 시작
            MainMenuScene nextScene = new MainMenuScene(m_frame);
            Game.setScene(nextScene);

            boolean wasServer = (p2p instanceof tetris.network.P2PServer);
            if(p2p != null) { p2p.release(); }

            if(wasServer) { nextScene.showServerMode(); } 
            else { nextScene.showClientMode(); }

        } else {
            // 정상 종료: p2p 연결은 유지하고 같은 상대와 다시 게임 가능
            new P2PRoomDialog(m_frame, p2p);
        }
    }


    // 게임 중 나가기 액션으로 인한 메인 메뉴 복귀 처리
    @Override
    protected void exitToMenu() {
        // 리소스 정리
        if(p2p != null) {
            p2p.removeCallback("game:");
            p2p.removeCallback("attack-generate:");
            p2p.removeCallback("attack-apply");
            p2p.setOnDisconnect(null); 
            p2p.release();
        }
        if (writeTimer != null) { 
            writeTimer.cancel(); 
            writeTimer.purge(); // 완전히 정리
        }
        super.exitToMenu();
    }

}

