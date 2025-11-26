package tetris.scene.battle;

import java.awt.Color;
import java.util.Timer;
import java.util.Queue;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;

import com.google.gson.Gson;

import tetris.Game;
import tetris.network.P2PBase;
import tetris.scene.game.blocks.Block;
import tetris.scene.game.blocks.ItemBlock;
import tetris.scene.game.blocks.WeightItemBlock;
import tetris.scene.game.core.*;
import tetris.scene.game.items.ItemEffectType;
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
    String nextItemEffect; // 아이템 블록 프리뷰 동기화를 위한 아이템 타입 정보
    boolean nextIsWeightBlock; // 무게추 아이템 블록 프리뷰 동기화용

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

    /**
     * 최대 허용 지연 시간 (밀리초)
     * 
     * 기준 설정 근거:
     * - R82 요구사항: 키 입력 → 화면 표시까지 지연 200ms 이하
     * - 게임 플레이에 영향을 주지 않는 수준의 지연 허용
     * - 이 값을 초과하면 게임 경험 저하
     * 
     * 연결 끊김과의 관계:
     * - 지연 100ms 미만: 정상 (초록색 표시)
     * - 지연 100-150ms: 주의 (노란색 표시)
     * - 지연 150-200ms: 경고 (주황색 표시)
     * - 지연 200ms 이상: 위험 (빨간색 표시)
     * - 지연이 지속적으로 200ms 초과 (최근 3개 샘플 모두 초과): 연결 끊김 처리
     * - P2PBase.TIMEOUT_MS(5000ms) 초과: 연결 끊김 처리
     * 
     * 지연(랙)과 연결 끊김의 구분:
     * - 지연(랙): 일시적 높은 지연 (100-200ms) - 게임은 계속 진행, UI에 경고 표시
     * - 연결 끊김: 
     *   * 지속적 높은 지연 (최근 3개 샘플 모두 200ms 초과)
     *   * 또는 P2PBase.TIMEOUT_MS(5000ms) 이상 응답 없음
     *   * → 연결 종료 처리 및 사용자 알림
     */
    final long MAX_LATENCY_MS = 200;
    
    // 지연 시간 모니터링 관련 필드
    private long currentLatency = 0;
    private long averageLatency = 0;
    private Queue<Long> latencyHistory = new LinkedList<>();
    private static final int LATENCY_HISTORY_SIZE = 10;
    private JLabel latencyLabel;

    // 블럭 타입 매핑
    final char[] blockTypes = { 'I','J','L','O','S','T','Z' };

    public P2PBattleScene(JFrame frame, String gameMode, P2PBase p2p) {
        super(frame, gameMode);

        this.inputHandler1 = new InputHandler(frame, new Player1Callback(), 0);

        this.gameStateManager2 = new GameStateManager(new EmptyCallback());
        this.inputHandler2 = new InputHandler(frame, new EmptyCallback(), 2); 
        this.blockManager2.resetBlock();

        // setupLayout은 BattleScene 생성자에서 호출되며, 
        // P2PBattleScene의 오버라이드된 setupLayout이 실행됨

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

        ItemBlock[][] ib = new ItemBlock[height][width];
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
                if(state.itemCells[r][c]){
                    ItemEffectType itemType = null;
                    String itemName = (state.itemBlockInfo != null) ? state.itemBlockInfo[r][c] : null;

                    if("줄 삭제".equals(itemName)) {
                        itemType = ItemEffectType.LINE_CLEAR;
                    } else if("청소".equals(itemName)) {
                        itemType = ItemEffectType.CLEANUP;
                    } else if("속도 감소".equals(itemName)) {
                        itemType = ItemEffectType.SPEED_DOWN;
                    } else if("속도 증가".equals(itemName)) {
                        itemType = ItemEffectType.SPEED_UP;
                    } else if("시야 제한".equals(itemName)) {
                        itemType = ItemEffectType.VISION_BLOCK;
                    }

                    if(itemType != null) {
                        ib[r][c] = new ItemBlock(itemType);
                    } else {
                        ib[r][c] = null;
                    }
                }
            }
        }
        boardManager2.setItemBlockInfo(ib);
        boardManager2.setBoardColors(bc);

        // 기본 다음 블록 생성
        blockManager2.setNextBlock(state.type);

        try {
            java.lang.reflect.Field nextBlockField = blockManager2.getClass().getDeclaredField("nextBlock");
            nextBlockField.setAccessible(true);

            // 1순위: 무게추 아이템 블록이면 그대로 WeightItemBlock 생성
            if (state.nextIsWeightBlock) {
                WeightItemBlock weightNext = new WeightItemBlock();
                nextBlockField.set(blockManager2, weightNext);
            }
            // 2순위: 일반 아이템 블록이면 ItemBlock으로 감싸기
            else if (state.nextItemEffect != null) {
                ItemEffectType nextItemType = ItemEffectType.valueOf(state.nextItemEffect);
                Block baseNextBlock = blockManager2.getNextBlock();
                if (baseNextBlock != null) {
                    ItemBlock itemNextBlock = new ItemBlock(baseNextBlock, nextItemType);
                    nextBlockField.set(blockManager2, itemNextBlock);
                }
            }
        } catch (Exception e) {
            // 아이템 정보 복원 실패 시에는 그냥 일반 블록으로 사용
            System.out.println("Failed to restore next special block in P2P: " + e.getMessage());
        }

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
                if(state.itemCells[r][c]) {
                    tetris.scene.game.blocks.ItemBlock itemBlock = boardManager1.getItemBlockInfo(c, r);
                    if(itemBlock != null) {
                        state.itemBlockInfo[r][c] = itemBlock.getItemDisplayName();
                    } else {
                        state.itemBlockInfo[r][c] = null;
                    }
                }
            }
        }

        // 현재 낙하중인 블록 정보 추가
        Block currentBlock = blockManager1.getCurrentBlock();
        if (currentBlock != null) {
            int blockX = blockManager1.getX();
            int blockY = blockManager1.getY();

            // 색 심볼 계산
            char colorSymbol = ' ';
            Color blockColor = currentBlock.getColor();

            // 일반 테트리스 블록(I,J,L,O,S,T,Z)은 Theme.Block 매핑으로 심볼을 찾고,
            // 그 외(예: WeightItemBlock)는 그대로 보드 색을 사용하거나 회색(G)으로 처리
            boolean mapped = false;
            for(char blockType : blockTypes) {
                if(blockColor.equals(Theme.Block(blockType))) {
                    colorSymbol = blockType;
                    mapped = true;
                    break;
                }
            }
            if (!mapped) {
                // 매핑되지 않는 특수 블록(무게추 등)은 회색 심볼로 표시
                if (Color.GRAY.equals(blockColor)) {
                    colorSymbol = 'G';
                } else if (Color.BLACK.equals(blockColor)) {
                    colorSymbol = 'B';
                } else {
                    // 알 수 없는 색은 심볼 없이 색만 보드에 반영
                    colorSymbol = ' ';
                }
            }

            for (int r = 0; r < currentBlock.height(); r++) {
                for (int c = 0; c < currentBlock.width(); c++) {
                    if(currentBlock.getShape(c, r) == 1) {
                        int br = blockY + r;
                        int bc2 = blockX + c;

                        // 보드 범위를 벗어나면 전송하지 않음 (안전장치)
                        if (br < 0 || br >= height || bc2 < 0 || bc2 >= width) continue;

                        state.board[br][bc2] = 1;

                        // 특수 블록이면 실제 색을 그대로 반영하고,
                        // 일반 블록이면 심볼 기반으로 색을 복원할 수 있도록 심볼을 기록
                        if (mapped) {
                            state.boardColors[br][bc2] = colorSymbol;
                        } else {
                            // 회색/기타는 그대로 색상으로만 사용되도록, 심볼은 비워 둠
                            state.boardColors[br][bc2] = colorSymbol;
                        }
                        state.boardTypes[br][bc2] = currentBlock.getType();

                        // 현재 블록이 ItemBlock인 경우, 내려오는 블록의 아이템 정보도 함께 전송
                        if (currentBlock instanceof ItemBlock) {
                            ItemBlock itemBlock = (ItemBlock) currentBlock;
                            if (itemBlock.isItemCell(c, r)) {
                                state.itemCells[br][bc2] = true;
                                state.itemBlockInfo[br][bc2] = itemBlock.getItemDisplayName();
                            }
                        }
                    }
                }
            }
        }

        Block nextBlock = blockManager1.getNextBlock();
        state.type = nextBlock.getType();

        // 무게추 아이템 / 일반 아이템 여부를 함께 전송
        if (nextBlock instanceof WeightItemBlock) {
            state.nextIsWeightBlock = true;
            state.nextItemEffect = null;
        } else if (nextBlock instanceof ItemBlock) {
            state.nextIsWeightBlock = false;
            ItemBlock itemNext = (ItemBlock) nextBlock;
            ItemEffectType itemType = itemNext.getItemType();
            state.nextItemEffect = (itemType != null) ? itemType.name() : null;
        } else {
            state.nextIsWeightBlock = false;
            state.nextItemEffect = null;
        }


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
        int beforeSize = attackQueue2.size();
        super.generateAttackBlocks(clearedLines, targetPlayer);
        if(targetPlayer == 1) return;

        int afterSize = attackQueue2.size();

        for(int i = beforeSize; i < afterSize; i++) {
            AttackBlock ab = attackQueue2.get(i);
            SerializabledAttackBlock sab = new SerializabledAttackBlock(ab);
            Gson gson = new Gson();
            String serializedAB = gson.toJson(sab);
            p2p.send("attack-generate:" + serializedAB);
        }
    }

    private void handleLatency(long latency) {
        currentLatency = latency;
        
        // 지연 히스토리 관리
        latencyHistory.offer(latency);
        if (latencyHistory.size() > LATENCY_HISTORY_SIZE) {
            latencyHistory.poll();
        }
        
        // 평균 지연 시간 계산
        if (!latencyHistory.isEmpty()) {
            averageLatency = (long) latencyHistory.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
        } else {
            averageLatency = latency;
        }
        
        // 연결 끊김 판단: 지속적으로 높은 지연만 연결 끊김 처리
        // 기준: 최근 3개 이상의 샘플이 모두 MAX_LATENCY_MS(200ms) 초과
        // 이는 일시적 지연과 실제 연결 문제를 구분하기 위함
        // 참고: P2PBase.TIMEOUT_MS(5000ms) 초과 시에도 연결 끊김 처리됨
        if (latency > MAX_LATENCY_MS && averageLatency > MAX_LATENCY_MS && latencyHistory.size() >= 3) {
            boolean allHighLatency = latencyHistory.stream()
                .allMatch(l -> l > MAX_LATENCY_MS);
            if (allHighLatency) {
                System.out.println("연결 끊김 판단: 지속적 높은 지연 (" + averageLatency + "ms 평균)");
                SwingUtilities.invokeLater(() -> {
                    showDisconnectDialog();
                });
            }
        }
        
        // UI 업데이트 (지연 시간 표시)
        updateLatencyDisplay();
    }
    
    /**
     * 지연 시간 표시 UI 업데이트
     */
    private void updateLatencyDisplay() {
        if (latencyLabel != null) {
            SwingUtilities.invokeLater(() -> {
                String latencyText = String.format("네트워크 지연: %dms (평균: %dms)", 
                    currentLatency, averageLatency);
                latencyLabel.setText(latencyText);
                
                // 지연 시간에 따른 색상 설정
                if (currentLatency >= MAX_LATENCY_MS) {
                    latencyLabel.setForeground(Color.RED); // 200ms 이상: 빨간색
                } else if (currentLatency >= 150) {
                    latencyLabel.setForeground(new Color(255, 165, 0)); // 150ms 이상: 주황색
                } else if (currentLatency >= 100) {
                    latencyLabel.setForeground(Color.YELLOW); // 100ms 이상: 노란색
                } else {
                    latencyLabel.setForeground(Color.GREEN); // 100ms 미만: 초록색
                }
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
            p2p.removeCallback("board:");
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
            p2p.removeCallback("board:");
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
    
    /**
     * setupLayout 오버라이드하여 지연 시간 표시 추가
     */
    @Override
    protected void setupLayout(JFrame frame) {
        // 부모 클래스의 setupLayout 호출
        super.setupLayout(frame);
        
        // 지연 시간 표시 라벨 생성
        latencyLabel = new JLabel("네트워크 지연: 측정 중...");
        latencyLabel.setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.BOLD, 14));
        latencyLabel.setForeground(Color.GREEN);
        latencyLabel.setHorizontalAlignment(JLabel.CENTER);
        latencyLabel.setOpaque(true);
        latencyLabel.setBackground(new Color(0, 0, 0, 180)); // 반투명 검은 배경
        latencyLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // 기존 레이아웃을 유지하면서 상단에 지연 시간 표시 추가
        java.awt.BorderLayout layout = (java.awt.BorderLayout) getLayout();
        if (layout != null) {
            // CENTER에 있는 기존 컴포넌트를 임시로 제거
            java.awt.Component center = layout.getLayoutComponent(java.awt.BorderLayout.CENTER);
            if (center != null) {
                remove(center);
                // 지연 시간 표시와 기존 컴포넌트를 다시 추가
                add(latencyLabel, java.awt.BorderLayout.NORTH);
                add(center, java.awt.BorderLayout.CENTER);
            } else {
                // CENTER 컴포넌트가 없으면 그냥 추가
                add(latencyLabel, java.awt.BorderLayout.NORTH);
            }
        }
        
        revalidate();
        repaint();
    }

}

