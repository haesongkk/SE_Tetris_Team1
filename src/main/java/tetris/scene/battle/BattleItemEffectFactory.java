package tetris.scene.battle;

import tetris.scene.game.items.ItemEffectType;
import tetris.scene.battle.effects.BattleSpeedUpEffect;
import tetris.scene.battle.effects.BattleSpeedDownEffect;
import tetris.scene.battle.effects.BattleVisionBlockEffect;

/**
 * 배틀 모드 전용 아이템 효과 팩토리
 * 기존 ItemEffectFactory와 분리하여 배틀 전용 효과 생성
 */
public class BattleItemEffectFactory {
    
    /**
     * 아이템 타입에 따른 배틀 전용 효과 생성
     */
    public static BattleItemEffect createBattleEffect(ItemEffectType type) {
        if (type == null) {
            return null;
        }
        
        switch (type) {
            case SPEED_UP:
                return new BattleSpeedUpEffect();
            case SPEED_DOWN:
                return new BattleSpeedDownEffect();
            case VISION_BLOCK:
                return new BattleVisionBlockEffect();
            case LINE_CLEAR:
                // LINE_CLEAR는 나중에 구현 예정
                return null;
            default:
                System.out.println("⚠️  No battle effect implementation for: " + type.getDisplayName());
                return null;
        }
    }
    
    /**
     * 해당 아이템 타입이 배틀 모드에서 지원되는지 확인
     */
    public static boolean isBattleSupported(ItemEffectType type) {
        switch (type) {
            case SPEED_UP:
            case SPEED_DOWN:
            case VISION_BLOCK:
                return true;
            case LINE_CLEAR:
                return false; // 아직 미구현
            default:
                return false;
        }
    }
}