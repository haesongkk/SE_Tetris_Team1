package tetris.scene.game.items.effects;

import tetris.scene.game.items.*;

/**
 * 낙하 속도 증가 아이템 효과
 * 5초간 블록 낙하 속도가 매우 빨라집니다 (100ms).
 */
public class SpeedUpEffect extends AbstractItemEffect {
    private static final long EFFECT_DURATION = 5000; // 5초
    private Object gameScene;
    private double originalSpeed = 1.0;
    
    public SpeedUpEffect() {
        super(ItemEffectType.SPEED_UP, EFFECT_DURATION);
    }
    
    @Override
    protected void doActivate(ItemEffectContext context) {
        this.gameScene = context.getGameScene();
        
        if (gameScene == null) {
            System.out.println("Speed up effect: GameScene is null");
            return;
        }
        
        try {
            // 속도 아이템 활성화 상태 설정
            gameScene.getClass()
                .getMethod("setSpeedItemActive", boolean.class)
                .invoke(gameScene, true);
            
            // 현재 속도 저장
            Object fallSpeed = gameScene.getClass()
                .getMethod("getFallSpeed")
                .invoke(gameScene);
            
            if (fallSpeed instanceof Number) {
                originalSpeed = ((Number) fallSpeed).doubleValue();
            }
            
            // 속도를 매우 빠르게 설정 (100ms)
            double newSpeed = 100.0;
            gameScene.getClass()
                .getMethod("setFallSpeed", double.class)
                .invoke(gameScene, newSpeed);
            
            System.out.println("Speed up effect: " + originalSpeed + "ms -> " + newSpeed + "ms delay (매우 빠름)" + 
                             " for " + (EFFECT_DURATION / 1000) + " seconds");
                             
        } catch (Exception e) {
            System.out.println("Failed to apply speed up effect: " + e.getMessage());
            // 효과 적용 실패 시 즉시 비활성화
            isActive = false;
        }
    }
    
    @Override
    protected void doDeactivate() {
        if (gameScene == null) {
            return;
        }
        
        try {
            // 속도 아이템 활성화 상태 해제
            gameScene.getClass()
                .getMethod("setSpeedItemActive", boolean.class)
                .invoke(gameScene, false);
            
            // 원래 속도로 복원
            gameScene.getClass()
                .getMethod("setFallSpeed", double.class)
                .invoke(gameScene, originalSpeed);
            
            System.out.println("Speed up effect ended: restored to " + originalSpeed);
            
        } catch (Exception e) {
            System.out.println("Failed to restore original speed: " + e.getMessage());
        }
    }
}