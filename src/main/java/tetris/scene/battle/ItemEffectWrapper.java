package tetris.scene.battle;

import tetris.scene.game.items.ItemEffect;
import tetris.scene.game.items.ItemEffectContext;
import tetris.scene.game.items.ItemEffectType;

/**
 * BattleItemEffect를 기존 ItemEffect 인터페이스로 감싸는 래퍼 클래스
 * 기존 시스템과의 호환성을 유지하기 위해 사용
 */
public class ItemEffectWrapper implements ItemEffect {
    private final BattleItemEffect battleEffect;
    private ItemEffectType effectType;
    
    public ItemEffectWrapper(BattleItemEffect battleEffect) {
        this.battleEffect = battleEffect;
    }
    
    public ItemEffectWrapper(BattleItemEffect battleEffect, ItemEffectType effectType) {
        this.battleEffect = battleEffect;
        this.effectType = effectType;
    }
    
    @Override
    public void activate(ItemEffectContext context) {
        // 이 메서드는 호출되지 않을 예정 (BattleItemContext를 직접 사용)
        // 호환성을 위해서만 구현
        System.out.println("⚠️  ItemEffectWrapper.activate() called - this should not happen");
    }
    
    @Override
    public void deactivate() {
        if (battleEffect != null) {
            battleEffect.deactivate();
        }
    }
    
    @Override
    public boolean isActive() {
        return battleEffect != null && battleEffect.isActive();
    }
    
    @Override
    public long getDuration() {
        return battleEffect != null ? battleEffect.getDuration() : 0;
    }
    
    @Override
    public ItemEffectType getEffectType() {
        return effectType;
    }
    
    /**
     * 효과 타입을 설정합니다
     */
    public void setEffectType(ItemEffectType effectType) {
        this.effectType = effectType;
    }
    
    /**
     * 래핑된 BattleItemEffect를 반환
     */
    public BattleItemEffect getBattleEffect() {
        return battleEffect;
    }
}