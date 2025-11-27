package tetris.scene.battle.effects;

import tetris.scene.battle.BattleItemContext;
import tetris.scene.battle.BattleItemEffect;
import tetris.scene.battle.Player;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 배틀 모드 전용 속도 증가 효과
 * 기존 SpeedUpEffect의 모든 문제점을 해결한 새로운 구현
 */
public class BattleSpeedUpEffect implements BattleItemEffect {
    private static final long EFFECT_DURATION = 5000; // 5초
    private static final double SPEED_UP_VALUE = 100.0; // 100ms로 설정
    
    private boolean isActive = false;
    private Timer effectTimer;
    private BattleItemContext context;
    private double originalSpeed;
    
    @Override
    public void activate(BattleItemContext context) {
        this.context = context;
        
        Player sourcePlayer = context.getSourcePlayer();
        Player targetPlayer = context.getTargetPlayer();
        
        System.out.println("🚀 SPEED UP: " + sourcePlayer.getDisplayName() + 
                          " activated speed up effect for " + targetPlayer.getDisplayName());
        
        // 현재 속도 저장
        originalSpeed = context.getGameInterface().getPlayerFallSpeed(targetPlayer);
        
        // 새 속도 적용
        context.getGameInterface().setPlayerFallSpeed(targetPlayer, SPEED_UP_VALUE);
        context.getGameInterface().setPlayerSpeedItemActive(targetPlayer, true);
        
        isActive = true;
        
        System.out.println("✅ Speed changed: " + originalSpeed + "ms → " + SPEED_UP_VALUE + "ms");
        
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
            
            // 원래 속도로 복원
            context.getGameInterface().setPlayerFallSpeed(targetPlayer, originalSpeed);
            context.getGameInterface().setPlayerSpeedItemActive(targetPlayer, false);
            
            System.out.println("🔄 Speed UP effect ended for " + targetPlayer.getDisplayName() + 
                             ": restored to " + originalSpeed + "ms");
            
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