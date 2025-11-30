package tetris.scene.game.items.effects;

import tetris.scene.game.items.*;

/**
 * 낙하 속도 증가 아이템 효과
 * 5초간 블록 낙하 속도가 빨라집니다 (100ms).
 */
public class SpeedUpEffect extends AbstractItemEffect {
    private static final long EFFECT_DURATION = 5000; // 5초
    private Object gameScene;
    private double originalSpeed = 1.0;
    private int playerNumber = 0; // 배틀 모드에서 아이템을 발동한 플레이어 번호
    private int targetPlayerNumber = 0; // 배틀 모드에서 실제 효과를 받는 플레이어 번호
    
    public SpeedUpEffect() {
        super(ItemEffectType.SPEED_UP, EFFECT_DURATION);
    }
    
    @Override
    protected void doActivate(ItemEffectContext context) {
        this.gameScene = context.getGameScene();
        this.playerNumber = context.getPlayerNumber(); // 플레이어 번호 저장
        
        System.out.println("🔍 [SpeedUpEffect] doActivate called - playerNumber: " + playerNumber + ", gameScene: " + (gameScene != null ? gameScene.getClass().getSimpleName() : "null"));
        
        if (gameScene == null) {
            System.out.println("Speed up effect: GameScene is null");
            return;
        }
        
        try {
            // BattleScene인지 확인하여 플레이어별로 처리
            String gameSceneClass = gameScene.getClass().getSimpleName();
            boolean isBattleMode = gameSceneClass.equals("BattleScene") || gameSceneClass.equals("P2PBattleScene");
            
            System.out.println("🔍 [SpeedUpEffect] gameSceneClass: " + gameSceneClass + ", isBattleMode: " + isBattleMode);
            
            if (isBattleMode && playerNumber > 0) {
                // 배틀 모드: 상대방에게 속도 증가 적용 (1P가 발동시 2P에게, 2P가 발동시 1P에게)
                this.targetPlayerNumber = (playerNumber == 1) ? 2 : 1;
                
                // 원래 속도 저장
                Object fallSpeed = gameScene.getClass()
                    .getMethod("getFallSpeed", int.class)
                    .invoke(gameScene, targetPlayerNumber);
                
                if (fallSpeed instanceof Number) {
                    originalSpeed = ((Number) fallSpeed).doubleValue();
                }
                
                // ✅ 속도 증가: 300ms (빠르게, 체감 가능)
                double newSpeed = 300.0;
                gameScene.getClass()
                    .getMethod("setFallSpeed", int.class, double.class)
                    .invoke(gameScene, targetPlayerNumber, newSpeed);
                
                // 속도 아이템 활성화 상태 설정
                gameScene.getClass()
                    .getMethod("setSpeedItemActive", int.class, boolean.class)
                    .invoke(gameScene, targetPlayerNumber, true);
                
                System.out.println("⚡ Speed up effect activated by Player " + playerNumber + " → affecting Player " + targetPlayerNumber + " in " + gameSceneClass + ": " + originalSpeed + "ms -> " + newSpeed + "ms delay (매우 빠름) for " + (EFFECT_DURATION / 1000) + " seconds");
            } else {
                // 일반 모드: 자신에게 속도 증가 적용 (기존 방식)
                this.targetPlayerNumber = 0; // 일반 모드에서는 플레이어 구분 없음
                
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
                
                System.out.println("Speed up effect activated in " + gameSceneClass + ": " + originalSpeed + "ms -> " + newSpeed + "ms delay (매우 빠름) for " + (EFFECT_DURATION / 1000) + " seconds");
            }
                             
        } catch (Exception e) {
            System.out.println("Failed to apply speed up effect: " + e.getMessage());
            e.printStackTrace();
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
            // BattleScene인지 확인하여 플레이어별로 처리
            String gameSceneClass = gameScene.getClass().getSimpleName();
            boolean isBattleMode = gameSceneClass.equals("BattleScene") || gameSceneClass.equals("P2PBattleScene");
            
            if (isBattleMode && targetPlayerNumber > 0) {
                // 배틀 모드: 효과를 받았던 플레이어의 속도 복원
                gameScene.getClass()
                    .getMethod("setSpeedItemActive", int.class, boolean.class)
                    .invoke(gameScene, targetPlayerNumber, false);
                
                // 원래 속도로 복원
                gameScene.getClass()
                    .getMethod("setFallSpeed", int.class, double.class)
                    .invoke(gameScene, targetPlayerNumber, originalSpeed);
                
                System.out.println("Speed up effect ended for Player " + targetPlayerNumber + " in BattleScene (activated by Player " + playerNumber + "): restored to " + originalSpeed);
            } else {
                // 일반 모드: 기존 방식 사용
                gameScene.getClass()
                    .getMethod("setSpeedItemActive", boolean.class)
                    .invoke(gameScene, false);
                
                // 원래 속도로 복원
                gameScene.getClass()
                    .getMethod("setFallSpeed", double.class)
                    .invoke(gameScene, originalSpeed);
                
                System.out.println("Speed up effect ended in " + gameSceneClass + ": restored to " + originalSpeed);
            }
            
        } catch (Exception e) {
            System.out.println("Failed to restore original speed: " + e.getMessage());
        }
    }
}