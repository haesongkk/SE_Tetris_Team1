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
        int[][] board = context.getBoard();
        int itemY = context.getItemY();
        
        if (itemY < 0 || itemY >= board.length) {
            System.out.println("Line clear effect: Invalid Y position " + itemY);
            return;
        }
        
        // 해당 줄을 완전히 삭제 (모든 셀을 0으로 설정)
        for (int x = 0; x < board[itemY].length; x++) {
            board[itemY][x] = 0;
        }
        
        // 삭제된 줄 위의 모든 줄들을 한 칸씩 아래로 이동
        for (int y = itemY; y > 0; y--) {
            for (int x = 0; x < board[y].length; x++) {
                board[y][x] = board[y - 1][x];
            }
        }
        
        // 맨 위 줄은 비워둠
        for (int x = 0; x < board[0].length; x++) {
            board[0][x] = 0;
        }
        
        System.out.println("Line clear effect activated at row " + itemY);
        
        // 점수 추가 (줄 삭제 1줄로 계산)
        if (context.getScoreManager() != null) {
            try {
                // 리플렉션을 사용하여 addScore 메서드 호출
                context.getScoreManager().getClass()
                    .getMethod("addScore", int.class)
                    .invoke(context.getScoreManager(), 1);
                System.out.println("Added score for line clear item");
            } catch (Exception e) {
                System.out.println("Failed to add score for line clear: " + e.getMessage());
            }
        }
    }
}