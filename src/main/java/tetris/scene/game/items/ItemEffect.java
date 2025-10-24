package tetris.scene.game.items;

/**
 * 아이템 효과 인터페이스
 * 모든 아이템 효과는 이 인터페이스를 구현해야 합니다.
 */
public interface ItemEffect {
    
    /**
     * 아이템 효과를 활성화합니다.
     * @param context 아이템 효과 실행에 필요한 컨텍스트 정보
     */
    void activate(ItemEffectContext context);
    
    /**
     * 아이템 효과의 지속 시간을 반환합니다. (밀리초 단위)
     * @return 지속 시간 (0이면 즉시 효과)
     */
    long getDuration();
    
    /**
     * 아이템 효과가 현재 활성화되어 있는지 확인합니다.
     * @return true if 활성화됨
     */
    boolean isActive();
    
    /**
     * 아이템 효과를 비활성화합니다.
     */
    void deactivate();
    
    /**
     * 아이템 효과의 타입을 반환합니다.
     * @return 아이템 효과 타입
     */
    ItemEffectType getEffectType();
}