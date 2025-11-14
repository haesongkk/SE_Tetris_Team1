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

}

public class P2PBattleScene extends BattleScene {

    P2PBase p2p;
    Timer writeTimer;
    Thread readThread;

    // 블럭 타입 매핑
    final char[] blockTypes = { 'I','J','L','O','S','T','Z' };

    public P2PBattleScene(JFrame frame, String gameMode, P2PBase p2p) {
        super(frame, gameMode);


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
                bc[r][c] = Theme.Block(state.boardColors[r][c]);
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
            dst[i] = src[i].clone();    // 각 row를 따로 clone
        }
        return dst;
    }

    private char[][] copy2DChar(char[][] src) {
        char[][] dst = new char[src.length][];
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

    private Color[][] copy2DColor(Color[][] src) {
        Color[][] dst = new Color[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i].clone();    // Color는 객체지만, 어차피 여기서는 읽기만 하니까 ok
        }
        return dst;
    }
    
    /**
     * BattleScene의 게임 오버 다이얼로그를 P2P 전용으로 오버라이드
     */
    @Override
    protected void showBattleGameOverDialog(int winner) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JDialog dialog = new javax.swing.JDialog(m_frame, "Game Over", true);
            dialog.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
            dialog.setLayout(new java.awt.GridBagLayout());
            dialog.setSize(350, 280);
            dialog.setLocationRelativeTo(m_frame);
            
            java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.insets = new java.awt.Insets(10, 10, 10, 10);
            
            // 승자 표시
            javax.swing.JLabel winnerLabel = new javax.swing.JLabel();
            winnerLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
            
            String winnerText;
            if (winner == 1) {
                winnerText = "SERVER WINS!";
                winnerLabel.setForeground(new java.awt.Color(255, 215, 0)); // Gold color
            } else {
                winnerText = "CLIENT WINS!";
                winnerLabel.setForeground(new java.awt.Color(255, 215, 0)); // Gold color
            }
            winnerLabel.setText(winnerText);
            winnerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            dialog.add(winnerLabel, gbc);
            
            // 플레이어 점수 표시
            int player1Score = scoreManager1.getScore();
            int player2Score = scoreManager2.getScore();
            
            // Server (Player 1) 점수
            javax.swing.JLabel serverScoreLabel = new javax.swing.JLabel("Server");
            serverScoreLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
            serverScoreLabel.setForeground(winner == 1 ? new java.awt.Color(255, 215, 0) : java.awt.Color.WHITE);
            serverScoreLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            dialog.add(serverScoreLabel, gbc);
            
            javax.swing.JLabel serverScoreValue = new javax.swing.JLabel(String.format("%,d", player1Score));
            serverScoreValue.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 16));
            serverScoreValue.setForeground(winner == 1 ? new java.awt.Color(255, 215, 0) : java.awt.Color.LIGHT_GRAY);
            serverScoreValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            
            gbc.gridy = 2;
            dialog.add(serverScoreValue, gbc);
            
            // Client (Player 2) 점수
            javax.swing.JLabel clientScoreLabel = new javax.swing.JLabel("Client");
            clientScoreLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
            clientScoreLabel.setForeground(winner == 2 ? new java.awt.Color(255, 215, 0) : java.awt.Color.WHITE);
            clientScoreLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            
            gbc.gridx = 1;
            gbc.gridy = 1;
            dialog.add(clientScoreLabel, gbc);
            
            javax.swing.JLabel clientScoreValue = new javax.swing.JLabel(String.format("%,d", player2Score));
            clientScoreValue.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 16));
            clientScoreValue.setForeground(winner == 2 ? new java.awt.Color(255, 215, 0) : java.awt.Color.LIGHT_GRAY);
            clientScoreValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            
            gbc.gridy = 2;
            dialog.add(clientScoreValue, gbc);
            
            // 메인 메뉴로 돌아가기 버튼
            javax.swing.JButton mainMenuButton = new javax.swing.JButton("Main Menu");
            mainMenuButton.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
            mainMenuButton.setPreferredSize(new java.awt.Dimension(120, 40));
            mainMenuButton.addActionListener(e -> {
                dialog.dispose();
                returnToMainMenu();
            });
            
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.fill = java.awt.GridBagConstraints.NONE;
            gbc.anchor = java.awt.GridBagConstraints.CENTER;
            dialog.add(mainMenuButton, gbc);
            
            // 다이얼로그 배경색 설정
            dialog.getContentPane().setBackground(new java.awt.Color(40, 40, 40));
            
            dialog.setVisible(true);
        });
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

