package tetris.scene.game.items.effects;

import tetris.scene.game.items.*;

/**
 * 줄 삭제 아이템 효과 (기존 폭탄 아이템을 대체)
 * 아이템이 포함된 줄을 즉시 삭제합니다.
 */
public class LineClearEffect extends AbstractItemEffect {
    
    public LineClearEffect() {
        super(ItemEffectType.LINE_CLEAR, 0); // 즉시 효과
    }
    
    @Override
    protected void doActivate(ItemEffectContext context) {
        int itemY = context.getItemY();
        
        System.out.println("Line clear effect activated at row " + itemY);
        
        // 즉시 줄 삭제 수행 (블링킹은 기존 시스템이 처리)
        performLineClear(context, itemY);
    }
    
    /**
     * 실제 줄 삭제를 수행합니다.
     */
    private void performLineClear(ItemEffectContext context, int itemY) {
        System.out.println("LINE_CLEAR item: Starting line clear for row " + itemY);
        
        // 1단계: 해당 줄을 완전히 채워서 완성된 줄로 만들기
        fillLineForDeletion(context, itemY);
        
        // 2단계: 일반 줄 삭제 시스템이 처리하도록 함 (별도 블링킹 시작하지 않음)
        System.out.println("LINE_CLEAR item: Line " + itemY + " filled and ready for normal line clearing system");
        
        // 점수 추가는 일반 줄 삭제 시스템에서 처리됨
        // addScoreForLineClear(context); // 제거: 이중 점수 방지
    }
    
    /**
     * LINE_CLEAR 아이템을 위해 해당 줄을 완전히 채웁니다.
     */
    private void fillLineForDeletion(ItemEffectContext context, int itemY) {
        int[][] board = context.getBoard();
        if (itemY >= 0 && itemY < board.length) {
            // 해당 줄의 모든 빈 셀을 1로 채움 (이미 채워진 셀은 그대로 유지)
            for (int x = 0; x < board[itemY].length; x++) {
                if (board[itemY][x] == 0) {
                    board[itemY][x] = 1;
                }
            }
            System.out.println("LINE_CLEAR item: Filled empty cells in line " + itemY + " to make it complete");
        }
    }
    
    @Override
    protected void doDeactivate() {
        // 즉시 효과이므로 비활성화할 것이 없음
    }
}