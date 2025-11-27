package tetris.scene.battle;

/**
 * 배틀 모드 전용 아이템 효과 인터페이스
 * 기존 ItemEffect보다 명확한 배틀 모드 전용 API 제공
 */
public interface BattleItemEffect {
    
    /**
     * 효과 활성화
     * @param context 배틀 아이템 컨텍스트
     */
    void activate(BattleItemContext context);
    
    /**
     * 효과 비활성화
     */
    void deactivate();
    
    /**
     * 효과가 현재 활성화되어 있는지 확인
     */
    boolean isActive();
    
    /**
     * 효과 지속 시간 (밀리초), -1이면 수동 비활성화까지 지속
     */
    long getDuration();
}