package tetris.scene.battle.effects;

import tetris.scene.battle.BattleItemContext;
import tetris.scene.battle.BattleItemEffect;
import tetris.scene.battle.Player;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 배틀 모드 전용 시야 차단 효과
 */
public class BattleVisionBlockEffect implements BattleItemEffect {
    private static final long EFFECT_DURATION = 3000; // 3초
    
    private boolean isActive = false;
    private Timer effectTimer;
    private BattleItemContext context;
    
    @Override
    public void activate(BattleItemContext context) {
        this.context = context;
        
        Player sourcePlayer = context.getSourcePlayer();
        Player targetPlayer = context.getTargetPlayer();
        
        System.out.println("👁️ VISION BLOCK: " + sourcePlayer.getDisplayName() + 
                          " activated vision block effect for " + targetPlayer.getDisplayName());
        
        // 시야 차단 효과 적용
        context.getGameInterface().setPlayerVisionBlock(targetPlayer, true);
        
        isActive = true;
        
        System.out.println("✅ Vision blocked for " + targetPlayer.getDisplayName());
        
        // 자동 비활성화 타이머 설정
        effectTimer = new Timer();
        effectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                deactivate();
            }
        }, EFFECT_DURATION);
        
        // 화면 갱신
        context.getGameInterface().repaintGame();
    }
    
    @Override
    public void deactivate() {
        if (!isActive) {
            return;
        }
        
        isActive = false;
        
        if (effectTimer != null) {
            effectTimer.cancel();
            effectTimer = null;
        }
        
        if (context != null) {
            Player targetPlayer = context.getTargetPlayer();
            
            // 시야 차단 해제
            context.getGameInterface().setPlayerVisionBlock(targetPlayer, false);
            
            System.out.println("🔄 Vision block effect ended for " + targetPlayer.getDisplayName());
            
            // 화면 갱신
            context.getGameInterface().repaintGame();
        }
    }
    
    @Override
    public boolean isActive() {
        return isActive;
    }
    
    @Override
    public long getDuration() {
        return EFFECT_DURATION;
    }
}