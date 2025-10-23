package tetris.scene.game.items.effects;

import tetris.scene.game.items.*;

/**
 * 시야 제한 아이템 효과
 * 4x20 크기의 시야 제한 영역을 생성합니다.
 */
public class VisionBlockEffect extends AbstractItemEffect {
    private static final long EFFECT_DURATION = 5000; // 5초간 시야 제한
    private Object gameScene;
    
    public VisionBlockEffect() {
        super(ItemEffectType.VISION_BLOCK, EFFECT_DURATION);
    }
    
    @Override
    protected void doActivate(ItemEffectContext context) {
        this.gameScene = context.getGameScene();
        
        if (gameScene == null) {
            System.out.println("Vision block effect: GameScene is null");
            return;
        }
        
        try {
            // 시야 제한 효과 활성화
            // 게임 보드의 중앙 4칸(인덱스 3-6)을 가림
            gameScene.getClass()
                .getMethod("setVisionBlockActive", boolean.class)
                .invoke(gameScene, true);
            
            System.out.println("Vision block effect activated for " + 
                             (EFFECT_DURATION / 1000) + " seconds");
                             
        } catch (Exception e) {
            System.out.println("Failed to apply vision block effect: " + e.getMessage());
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
            // 시야 제한 효과 비활성화
            gameScene.getClass()
                .getMethod("setVisionBlockActive", boolean.class)
                .invoke(gameScene, false);
            
            System.out.println("Vision block effect ended");
            
        } catch (Exception e) {
            System.out.println("Failed to deactivate vision block effect: " + e.getMessage());
        }
    }
}