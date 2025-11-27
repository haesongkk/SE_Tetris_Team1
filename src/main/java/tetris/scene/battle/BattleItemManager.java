package tetris.scene.battle;

import tetris.scene.game.items.ItemEffect;
import tetris.scene.game.items.ItemEffectFactory;
import tetris.scene.game.items.ItemEffectType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 배틀 모드 전용 아이템 효과 매니저
 * 플레이어간 아이템 효과의 정확한 적용을 보장
 */
public class BattleItemManager {
    private final BattleGameInterface gameInterface;
    private final Map<Player, Map<ItemEffectType, ItemEffect>> activeEffects;
    
    public BattleItemManager(BattleGameInterface gameInterface) {
        this.gameInterface = gameInterface;
        this.activeEffects = new ConcurrentHashMap<>();
        
        // 각 플레이어별 활성 효과 맵 초기화
        for (Player player : Player.values()) {
            activeEffects.put(player, new HashMap<>());
        }
    }
    
    /**
     * 아이템 효과를 적용
     * @param sourcePlayer 아이템을 사용한 플레이어
     * @param itemType 아이템 타입
     * @param x 아이템이 있던 x 좌표
     * @param y 아이템이 있던 y 좌표
     */
    public void applyItemEffect(Player sourcePlayer, ItemEffectType itemType, int x, int y) {
        if (!gameInterface.isGameActive()) {
            return;
        }
        
        // 배틀 모드에서는 상대방에게 효과 적용
        Player targetPlayer = sourcePlayer.getOpponent();
        
        System.out.println("🎯 Item Effect: " + sourcePlayer.getDisplayName() + 
                          " used " + itemType.getDisplayName() + 
                          " → affects " + targetPlayer.getDisplayName());
        
        // 기존 동일 타입 효과가 있으면 제거
        cancelItemEffect(targetPlayer, itemType);
        
        // 새 효과 생성 및 적용
        BattleItemEffect effect = BattleItemEffectFactory.createBattleEffect(itemType);
        if (effect != null) {
            // 배틀 전용 컨텍스트 생성
            BattleItemContext context = new BattleItemContext(
                sourcePlayer, targetPlayer, gameInterface, x, y
            );
            
            // 효과 활성화
            effect.activate(context);
            
            // 활성 효과 목록에 추가 (BattleItemEffect를 ItemEffect로 래핑)
            ItemEffectWrapper wrapper = new ItemEffectWrapper(effect, itemType);
            activeEffects.get(targetPlayer).put(itemType, wrapper);
            
            System.out.println("✅ Effect applied successfully");
        } else {
            System.out.println("⚠️  No battle effect available for " + itemType.getDisplayName() + 
                             ", using legacy system");
            // 배틀 효과가 없으면 기존 시스템 사용
            activateLegacyEffect(ItemEffectFactory.createEffect(itemType), 
                               new BattleItemContext(sourcePlayer, targetPlayer, gameInterface, x, y));
        }
    }
    
    /**
     * 특정 플레이어의 특정 아이템 효과 취소
     */
    public void cancelItemEffect(Player player, ItemEffectType itemType) {
        Map<ItemEffectType, ItemEffect> playerEffects = activeEffects.get(player);
        ItemEffect effect = playerEffects.remove(itemType);
        
        if (effect != null) {
            if (effect instanceof ItemEffectWrapper) {
                ItemEffectWrapper wrapper = (ItemEffectWrapper) effect;
                BattleItemEffect battleEffect = wrapper.getBattleEffect();
                if (battleEffect != null) {
                    battleEffect.deactivate();
                }
            } else {
                // 기존 효과 비활성화
                effect.deactivate();
            }
            
            System.out.println("🚫 Effect cancelled: " + itemType.getDisplayName() + 
                             " for " + player.getDisplayName());
        }
    }
    
    /**
     * 플레이어의 모든 활성 효과 취소
     */
    public void cancelAllEffects(Player player) {
        Map<ItemEffectType, ItemEffect> playerEffects = activeEffects.get(player);
        for (ItemEffectType type : playerEffects.keySet()) {
            cancelItemEffect(player, type);
        }
    }
    
    /**
     * 모든 플레이어의 모든 효과 취소 (게임 종료 시)
     */
    public void cancelAllEffects() {
        for (Player player : Player.values()) {
            cancelAllEffects(player);
        }
    }
    
    /**
     * 기존 ItemEffect를 배틀 모드에서 사용하기 위한 어댑터
     */
    private void activateLegacyEffect(ItemEffect effect, BattleItemContext context) {
        // 기존 ItemEffectContext로 변환하여 처리
        // 이 부분은 기존 효과들이 BattleItemEffect로 마이그레이션되면 제거 예정
        System.out.println("⚠️  Using legacy effect adapter for " + effect.getClass().getSimpleName());
    }
    
    /**
     * 기존 ItemEffect 비활성화
     */
    private void deactivateLegacyEffect(ItemEffect effect, Player player) {
        // 기존 효과 비활성화 로직
        System.out.println("⚠️  Deactivating legacy effect " + effect.getClass().getSimpleName());
    }
    
    /**
     * 현재 활성화된 효과 목록 반환
     */
    public Map<ItemEffectType, ItemEffect> getActiveEffects(Player player) {
        return new HashMap<>(activeEffects.get(player));
    }
    
    /**
     * 특정 효과가 활성화되어 있는지 확인
     */
    public boolean isEffectActive(Player player, ItemEffectType itemType) {
        return activeEffects.get(player).containsKey(itemType);
    }
}