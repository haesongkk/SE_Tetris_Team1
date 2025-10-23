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
}