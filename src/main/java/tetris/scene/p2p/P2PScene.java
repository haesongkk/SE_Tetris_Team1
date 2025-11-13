package tetris.scene.p2p;

import javax.swing.JFrame;
import java.util.Timer;
import java.util.TimerTask;

import tetris.network.P2PBase;
import tetris.network.P2PServer;
import tetris.scene.Scene;
import tetris.scene.battle.BattleScene;
import tetris.scene.game.core.BoardManager;
import tetris.scene.game.core.BlockManager;
import tetris.scene.game.blocks.Block;

/**
 * P2P 네트워크 대전 씬
 * BattleScene을 활용하여 네트워크를 통해 원격 플레이어와 대전합니다.
 * 
 * 구조:
 * - 서버: 1P 조작 가능, 2P는 클라이언트 상태 네트워크 동기화
 * - 클라이언트: 2P 조작 가능, 1P는 서버 상태 네트워크 동기화
 */
public class P2PScene extends Scene {

    private final P2PBase p2p;
    private final BattleScene battleScene;
    private final boolean isServer;
    private Timer syncTimer;
    private Thread receiveThread;
    
    private static final int GAME_WIDTH = 10;
    private static final int GAME_HEIGHT = 20;

    public P2PScene(JFrame frame, P2PBase p2p) {
        super(frame);
        this.p2p = p2p;
        this.isServer = (p2p instanceof P2PServer);
        
        // BattleScene 생성 (P2P 모드)
        this.battleScene = new BattleScene(frame, "P2P");
        
        // BattleScene을 컨텐츠로 설정
        setLayout(new java.awt.BorderLayout());
        add(battleScene, java.awt.BorderLayout.CENTER);
        
        frame.setContentPane(this);
        frame.revalidate();
        frame.repaint();
        
        // 네트워크 동기화 시작
        setupNetworkSync();
    }
    
    /**
     * 네트워크 동기화 설정
     * - 서버: 1P 상태 전송, 클라이언트 상태 받아서 2P에 적용
     * - 클라이언트: 2P 상태 전송, 서버 상태 받아서 1P에 적용
     */
    private void setupNetworkSync() {
        // 상태 전송 타이머 (100ms마다 자신의 플레이어 상태 전송)
        syncTimer = new Timer();
        syncTimer.scheduleAtFixedRate(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    try {
                        String stateData = isServer ? serializePlayer1State() : serializePlayer2State();
                        p2p.send(stateData);
                    } catch (Exception e) {
                        System.err.println("Error sending P2P state: " + e.getMessage());
                    }
                }
            },
            0, 100
        );
        
        // 상태 수신 스레드 (원격 플레이어 상태를 받아 적용)
        receiveThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String data = p2p.recieve();
                    if (data != null) {
                        if (isServer) {
                            applyToPlayer2(data);
                        } else {
                            applyToPlayer1(data);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error receiving P2P data: " + e.getMessage());
                    break;
                }
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }
    
    /**
     * 1P(로컬 플레이어)의 게임 상태를 직렬화하여 JSON으로 반환
     */
    private String serializePlayer1State() {
        BoardManager boardMgr = battleScene.getBoardManager1();
        BlockManager blockMgr = battleScene.getBlockManager1();
        
        int[][] board = boardMgr.getBoard();
        int[][] boardTypes = boardMgr.getBoardTypes();
        
        // 현재 낙하 중인 블록 정보
        Block currentBlock = blockMgr.getCurrentBlock();
        int blockX = blockMgr.getX();
        int blockY = blockMgr.getY();
        int currentBlockType = currentBlock.getType();
        
        // 간단한 텍스트 형식으로 직렬화
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < GAME_HEIGHT; row++) {
            for (int col = 0; col < GAME_WIDTH; col++) {
                // 현재 낙하 중인 블록 위치 체크
                boolean isCurrentBlock = false;
                for (int r = 0; r < currentBlock.height(); r++) {
                    for (int c = 0; c < currentBlock.width(); c++) {
                        if (currentBlock.getShape(c, r) == 1) {
                            int boardRow = blockY + r;
                            int boardCol = blockX + c;
                            if (boardRow == row && boardCol == col && 
                                boardRow >= 0 && boardRow < GAME_HEIGHT && 
                                boardCol >= 0 && boardCol < GAME_WIDTH) {
                                isCurrentBlock = true;
                            }
                        }
                    }
                }
                
                if (isCurrentBlock) {
                    sb.append(currentBlockType).append(",");
                } else {
                    sb.append(board[row][col] == 1 ? boardTypes[row][col] : -1).append(",");
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * 2P 상태를 직렬화 (클라이언트가 전송)
     */
    private String serializePlayer2State() {
        BoardManager boardMgr = battleScene.getBoardManager2();
        BlockManager blockMgr = battleScene.getBlockManager2();
        
        int[][] board = boardMgr.getBoard();
        int[][] boardTypes = boardMgr.getBoardTypes();
        
        Block currentBlock = blockMgr.getCurrentBlock();
        int blockX = blockMgr.getX();
        int blockY = blockMgr.getY();
        int currentBlockType = currentBlock.getType();
        
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < GAME_HEIGHT; row++) {
            for (int col = 0; col < GAME_WIDTH; col++) {
                boolean isCurrentBlock = false;
                for (int r = 0; r < currentBlock.height(); r++) {
                    for (int c = 0; c < currentBlock.width(); c++) {
                        if (currentBlock.getShape(c, r) == 1) {
                            int boardRow = blockY + r;
                            int boardCol = blockX + c;
                            if (boardRow == row && boardCol == col && 
                                boardRow >= 0 && boardRow < GAME_HEIGHT && 
                                boardCol >= 0 && boardCol < GAME_WIDTH) {
                                isCurrentBlock = true;
                            }
                        }
                    }
                }
                
                if (isCurrentBlock) {
                    sb.append(currentBlockType).append(",");
                } else {
                    sb.append(board[row][col] == 1 ? boardTypes[row][col] : -1).append(",");
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * 1P에 원격 상태 적용 (클라이언트가 서버 상태 받음)
     */
    private void applyToPlayer1(String data) {
        try {
            String[] tokens = data.split(",");
            if (tokens.length != GAME_HEIGHT * GAME_WIDTH) {
                return;
            }
            
            BoardManager boardMgr = battleScene.getBoardManager1();
            
            int[][] board = boardMgr.getBoard();
            int[][] boardTypes = boardMgr.getBoardTypes();
            
            int idx = 0;
            for (int row = 0; row < GAME_HEIGHT; row++) {
                for (int col = 0; col < GAME_WIDTH; col++) {
                    int value = Integer.parseInt(tokens[idx++]);
                    if (value >= 0) {
                        board[row][col] = 1;
                        boardTypes[row][col] = value;
                    } else {
                        board[row][col] = 0;
                        boardTypes[row][col] = -1;
                    }
                }
            }
            
            battleScene.repaint();
            
        } catch (Exception e) {
            System.err.println("Error applying P2P state to Player1: " + e.getMessage());
        }
    }
    
    /**
     * 2P에 원격 상태 적용 (서버가 클라이언트 상태 받음)
     */
    private void applyToPlayer2(String data) {
        try {
            String[] tokens = data.split(",");
            if (tokens.length != GAME_HEIGHT * GAME_WIDTH) {
                return;
            }
            
            BoardManager boardMgr = battleScene.getBoardManager2();
            
            // 2P 보드 전체 업데이트
            int[][] board = boardMgr.getBoard();
            int[][] boardTypes = boardMgr.getBoardTypes();
            
            int idx = 0;
            for (int row = 0; row < GAME_HEIGHT; row++) {
                for (int col = 0; col < GAME_WIDTH; col++) {
                    int value = Integer.parseInt(tokens[idx++]);
                    if (value >= 0) {
                        board[row][col] = 1;
                        boardTypes[row][col] = value;
                    } else {
                        board[row][col] = 0;
                        boardTypes[row][col] = -1;
                    }
                }
            }
            
            // 화면 갱신
            battleScene.repaint();
            
        } catch (Exception e) {
            System.err.println("Error applying P2P state: " + e.getMessage());
        }
    }
    
    
    @Override
    public void onEnter() {
        battleScene.onEnter();
        
        // onEnter() 이후에 비활성화 (타이머가 시작된 후)
        if (isServer) {
            battleScene.disablePlayer2AutoPlay();
        } else {
            battleScene.disablePlayer1AutoPlay();
        }
    }
    
    @Override
    public void onExit() {
        // 네트워크 동기화 중지
        if (syncTimer != null) {
            syncTimer.cancel();
        }
        if (receiveThread != null) {
            receiveThread.interrupt();
        }
        
        battleScene.onExit();
    }
}
