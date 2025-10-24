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
        int[][] board = context.getBoard();
        int centerX = context.getItemX();
        int centerY = context.getItemY();
        
        System.out.println("Cleanup effect activated at (" + centerX + ", " + centerY + ")");
        
        int cleanedBlocks = 0;
        
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
        
        // Blinking 효과 추가
        addBlinkingEffect(context, centerX, centerY);
        
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
     * 청소 효과에 blinking 효과를 추가합니다 (기존 줄 삭제와 동일한 시스템 사용).
     */
    private void addBlinkingEffect(ItemEffectContext context, int centerX, int centerY) {
        Object gameScene = context.getGameScene();
        if (gameScene == null) {
            return;
        }
        
        try {
            // 3x3 영역의 모든 셀 좌표를 GameScene에 전달하여 기존 줄 삭제와 동일한 블링킹 적용
            java.util.Set<java.awt.Point> blinkCells = new java.util.HashSet<>();
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int targetX = centerX + dx;
                    int targetY = centerY + dy;
                    
                    // 게임 보드 범위 내에 있는 셀만 추가
                    if (targetX >= 0 && targetX < 10 && targetY >= 0 && targetY < 20) {
                        blinkCells.add(new java.awt.Point(targetX, targetY));
                    }
                }
            }
            
            // GameScene의 청소 블링킹 시작 (기존 줄 삭제와 동일한 900ms 지속시간)
            if (!blinkCells.isEmpty()) {
                gameScene.getClass()
                    .getMethod("startCleanupBlinking", java.util.Set.class)
                    .invoke(gameScene, blinkCells);
                
                System.out.println("Started cleanup blinking effect for " + blinkCells.size() + " cells around (" + centerX + ", " + centerY + ")");
                
                // 900ms 후에 블링킹 중지 (기존 line blinking과 동일한 시간)
                java.util.Timer timer = new java.util.Timer();
                timer.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        try {
                            gameScene.getClass()
                                .getMethod("stopCleanupBlinking")
                                .invoke(gameScene);
                            System.out.println("Stopped cleanup blinking effect");
                        } catch (Exception e) {
                            System.out.println("Failed to stop cleanup blinking: " + e.getMessage());
                        }
                    }
                }, 900); // 900ms로 변경
            }
        } catch (Exception e) {
            System.out.println("Failed to add cleanup blinking effect: " + e.getMessage());
        }
    }
}