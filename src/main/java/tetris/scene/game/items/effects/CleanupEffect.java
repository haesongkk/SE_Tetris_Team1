package tetris.scene.game.items.effects;

import tetris.scene.game.items.*;

/**
 * 청소 아이템 효과
 * 아이템을 중심으로 3x3 영역의 블록을 모두 제거합니다.
 */
public class CleanupEffect extends AbstractItemEffect {
    
    public CleanupEffect() {
        super(ItemEffectType.CLEANUP, 0); // 즉시 효과
    }
    
    @Override
    protected void doActivate(ItemEffectContext context) {
        int centerX = context.getItemX();
        int centerY = context.getItemY();
        
        System.out.println("Cleanup effect activated at (" + centerX + ", " + centerY + ")");
        
        // 먼저 점멸 효과를 시작하고, 점멸 완료 후 블록 삭제 및 중력 적용
        addBlinkingEffectWithCallback(context, centerX, centerY);
    }
    
    /**
     * BoardManager를 통해 3x3 영역의 아이템 셀 정보를 정리합니다.
     */
    private void cleanupItemCells(ItemEffectContext context, int centerX, int centerY) {
        Object boardManager = context.getBoardManager();
        if (boardManager == null) {
            return;
        }
        
        try {
            // 3x3 영역의 아이템 셀 정보 제거
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int targetX = centerX + dx;
                    int targetY = centerY + dy;
                    
                    // BoardManager의 setItemCell 메서드 호출하여 아이템 셀 정보 제거
                    boardManager.getClass()
                        .getMethod("setItemCell", int.class, int.class, boolean.class)
                        .invoke(boardManager, targetX, targetY, false);
                }
            }
            System.out.println("Cleaned up item cell information in 3x3 area");
        } catch (Exception e) {
            System.out.println("Failed to cleanup item cells: " + e.getMessage());
        }
    }
    
    /**
     * 점멸 효과를 시작하고, 완료 후 콜백으로 블록 삭제 및 중력 적용을 수행합니다.
     */
    private void addBlinkingEffectWithCallback(ItemEffectContext context, int centerX, int centerY) {
        Object gameScene = context.getGameScene();
        if (gameScene == null) {
            // GameScene이 없으면 즉시 블록 처리 수행
            performBlockCleanup(context, centerX, centerY);
            return;
        }
        
        try {
            // 3x3 영역에서 실제로 블록이 있는 셀들만 개별적으로 점멸 (줄 전체가 아닌 해당 셀만)
            int[][] board = context.getBoard();
            java.util.Set<java.awt.Point> blinkCells = new java.util.HashSet<>();
            
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int targetX = centerX + dx;
                    int targetY = centerY + dy;
                    
                    // 게임 보드 범위 내에 있고, 실제 블록이 있는 셀만 추가
                    if (targetX >= 0 && targetX < 10 && targetY >= 0 && targetY < 20) {
                        if (isValidPosition(board, targetX, targetY) && board[targetY][targetX] != 0) {
                            blinkCells.add(new java.awt.Point(targetX, targetY));
                        }
                    }
                }
            }
            
            // GameScene의 청소 블링킹 시작 (개별 셀 점멸, 줄 삭제와 동일한 900ms 지속시간)
            if (!blinkCells.isEmpty()) {
                // BattleScene인지 확인하고 플레이어별 메서드 호출
                String gameSceneType = gameScene.getClass().getSimpleName();
                if ("BattleScene".equals(gameSceneType)) {
                    // BattleScene에서는 플레이어를 구분해야 함
                    int playerNumber = determinePlayerNumber(context);
                    if (playerNumber == 1) {
                        gameScene.getClass()
                            .getMethod("startCleanupBlinking1", java.util.Set.class)
                            .invoke(gameScene, blinkCells);
                        System.out.println("Started Player 1 cleanup blinking effect for " + blinkCells.size() + " cells");
                    } else if (playerNumber == 2) {
                        gameScene.getClass()
                            .getMethod("startCleanupBlinking2", java.util.Set.class)
                            .invoke(gameScene, blinkCells);
                        System.out.println("Started Player 2 cleanup blinking effect for " + blinkCells.size() + " cells");
                    } else {
                        // 플레이어 구분 실패시 기본적으로 Player 1 사용
                        gameScene.getClass()
                            .getMethod("startCleanupBlinking1", java.util.Set.class)
                            .invoke(gameScene, blinkCells);
                        System.out.println("⚠️ Player determination failed, using Player 1 for cleanup blinking");
                    }
                } else {
                    // GeneralScene 등 다른 씬에서는 기존 메서드 사용
                    gameScene.getClass()
                        .getMethod("startCleanupBlinking", java.util.Set.class)
                        .invoke(gameScene, blinkCells);
                    System.out.println("Started cleanup blinking effect for " + blinkCells.size() + " cells");
                }
                
                // 900ms 후에 블록 처리 수행 (줄 삭제와 동일한 타이밍)
                java.util.Timer timer = new java.util.Timer();
                timer.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        try {
                            // BattleScene인지 확인하고 플레이어별 중지 메서드 호출
                            if ("BattleScene".equals(gameSceneType)) {
                                int playerNumber = determinePlayerNumber(context);
                                if (playerNumber == 1) {
                                    gameScene.getClass()
                                        .getMethod("stopCleanupBlinking1")
                                        .invoke(gameScene);
                                    System.out.println("Stopped Player 1 cleanup blinking effect");
                                } else if (playerNumber == 2) {
                                    gameScene.getClass()
                                        .getMethod("stopCleanupBlinking2")
                                        .invoke(gameScene);
                                    System.out.println("Stopped Player 2 cleanup blinking effect");
                                } else {
                                    // 플레이어 구분 실패시 기본적으로 Player 1 사용
                                    gameScene.getClass()
                                        .getMethod("stopCleanupBlinking1")
                                        .invoke(gameScene);
                                    System.out.println("⚠️ Player determination failed, stopped Player 1 cleanup blinking");
                                }
                            } else {
                                // GeneralScene 등 다른 씬에서는 기존 메서드 사용
                                gameScene.getClass()
                                    .getMethod("stopCleanupBlinking")
                                    .invoke(gameScene);
                                System.out.println("Stopped cleanup blinking effect");
                            }
                            
                            // 점멸 완료 후 블록 삭제 및 중력 적용
                            performBlockCleanup(context, centerX, centerY);
                        } catch (Exception e) {
                            System.out.println("Failed to stop cleanup blinking: " + e.getMessage());
                            // 에러가 발생해도 블록 처리는 수행
                            performBlockCleanup(context, centerX, centerY);
                        }
                    }
                }, 900);
            } else {
                // 삭제할 블록이 없으면 즉시 완료
                performBlockCleanup(context, centerX, centerY);
            }
        } catch (Exception e) {
            System.out.println("Failed to add cleanup blinking effect: " + e.getMessage());
            // 에러가 발생하면 즉시 블록 처리 수행
            performBlockCleanup(context, centerX, centerY);
        }
    }
    
    /**
     * ItemEffectContext에서 플레이어 번호를 가져옵니다.
     * @param context ItemEffectContext
     * @return 1 (Player 1) 또는 2 (Player 2)
     */
    private int determinePlayerNumber(ItemEffectContext context) {
        int playerNumber = context.getPlayerNumber();
        
        // 추가 디버그 정보
        Object boardManager = context.getBoardManager();
        Object gameScene = context.getGameScene();
        
        System.out.println("🔍 DEBUG CleanupEffect player determination:");
        System.out.println("   - ItemEffectContext playerNumber: " + playerNumber);
        
        if (boardManager != null) {
            try {
                int bmPlayerNumber = (Integer) boardManager.getClass().getMethod("getPlayerNumber").invoke(boardManager);
                System.out.println("   - BoardManager playerNumber: " + bmPlayerNumber);
            } catch (Exception e) {
                System.out.println("   - Failed to get BoardManager playerNumber: " + e.getMessage());
            }
        }
        
        if (gameScene != null) {
            System.out.println("   - GameScene type: " + gameScene.getClass().getSimpleName());
        }
        
        System.out.println("✅ Player number from ItemEffectContext: " + playerNumber);
        return playerNumber;
    }
    
    /**
     * 실제 블록 삭제, 중력 적용, 아이템 셀 정리, 점수 추가를 수행합니다.
     */
    private void performBlockCleanup(ItemEffectContext context, int centerX, int centerY) {
        int[][] board = context.getBoard();
        int cleanedBlocks = 0;
        
        // 3x3 영역의 경계 계산
        int minX = Math.max(0, centerX - 1);
        int maxX = Math.min(9, centerX + 1); // GAME_WIDTH = 10
        int minY = Math.max(0, centerY - 1);
        int maxY = Math.min(19, centerY + 1); // GAME_HEIGHT = 20
        
        // 3x3 영역 정리 (아이템을 중심으로 주변 8칸 + 중심 1칸)
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int targetX = centerX + dx;
                int targetY = centerY + dy;
                
                if (isValidPosition(board, targetX, targetY)) {
                    if (board[targetY][targetX] != 0) {
                        board[targetY][targetX] = 0;
                        cleanedBlocks++;
                    }
                }
            }
        }
        
        System.out.println("Cleanup effect removed " + cleanedBlocks + " blocks in 3x3 area");
        
        // 블록 삭제 후 각 열에 중력 적용
        if (cleanedBlocks > 0) {
            System.out.println("Applying gravity to affected columns...");
            applyGravityToColumns(context, minX, maxX, minY, maxY);
        }
        
        // BoardManager를 통해 아이템 셀 정보도 정리
        cleanupItemCells(context, centerX, centerY);
        
        // 정리된 블록 수에 비례한 점수 추가
        if (context.getScoreManager() != null && cleanedBlocks > 0) {
            try {
                // 블록당 10점씩 추가
                int bonusScore = cleanedBlocks * 10;
                // addBlockFallScore를 여러 번 호출하거나 별도 메서드 사용
                for (int i = 0; i < cleanedBlocks; i++) {
                    context.getScoreManager().getClass()
                        .getMethod("addBlockFallScore")
                        .invoke(context.getScoreManager());
                }
                System.out.println("Added " + bonusScore + " points for cleanup effect");
            } catch (Exception e) {
                System.out.println("Failed to add score for cleanup: " + e.getMessage());
            }
        }
    }
    
    /**
     * 지정된 영역에서 삭제된 블록들로 인해 위에 있는 블록들을 아래로 내립니다.
     * 각 열별로 중력을 적용하여 빈 공간을 채웁니다.
     */
    private void applyGravityToColumns(ItemEffectContext context, int minX, int maxX, int minY, int maxY) {
        try {
            Object boardManager = context.getBoardManager();
            if (boardManager != null) {
                // BoardManager의 compactColumns 메서드 호출
                boardManager.getClass()
                    .getMethod("compactColumns", int.class, int.class, int.class, int.class)
                    .invoke(boardManager, minX, maxX, minY, maxY);
                System.out.println("Successfully applied gravity to columns");
            } else {
                System.out.println("BoardManager is null, cannot apply gravity");
            }
        } catch (Exception e) {
            System.out.println("Failed to apply gravity to columns: " + e.getMessage());
            // 직접 중력 적용 (fallback)
            applyGravityDirectly(context, minX, maxX, maxY);
        }
    }
    
    /**
     * BoardManager 메서드 호출이 실패할 경우 직접 중력을 적용합니다.
     */
    private void applyGravityDirectly(ItemEffectContext context, int minX, int maxX, int maxY) {
        int[][] board = context.getBoard();
        System.out.println("Applying gravity directly to columns " + minX + "-" + maxX);
        
        // 각 열별로 중력 적용
        for (int col = minX; col <= maxX; col++) {
            if (col < 0 || col >= 10) continue; // GAME_WIDTH = 10
            
            int writeRow = maxY; // 아래부터 채워나갈 위치
            
            // 아래에서 위로 올라가면서 빈 공간이 아닌 블록들만 아래로 이동
            for (int readRow = maxY; readRow >= 0; readRow--) {
                if (board[readRow][col] != 0) {
                    // 블록이 있으면 writeRow 위치로 이동
                    if (writeRow != readRow) {
                        System.out.println("Moving block from (" + col + "," + readRow + ") to (" + col + "," + writeRow + ")");
                        board[writeRow][col] = board[readRow][col];
                        board[readRow][col] = 0;
                    }
                    writeRow--; // 다음에 채울 위치로 이동
                }
            }
        }
        
        System.out.println("Direct gravity application completed");
    }
}