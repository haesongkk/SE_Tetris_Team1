package tetris.scene.game.items;

/**
 * 아이템 효과 타입 열거형
 */
public enum ItemEffectType {
    /**
     * 줄 삭제 아이템 (기존 폭탄 아이템을 대체)
     */
    LINE_CLEAR("줄 삭제", "L"),
    
    /**
     * 청소 아이템 - 3x3 영역 제거
     */
    CLEANUP("청소", "C"),
    
    /**
     * 낙하 속도 감소 아이템
     */
    SPEED_DOWN("속도 감소", "S"),
    
    /**
     * 낙하 속도 증가 아이템
     */
    SPEED_UP("속도 증가", "F"),
    
    /**
     * 시야 제한 아이템
     */
    VISION_BLOCK("시야 제한", "V");
    
    private final String displayName;
    private final String symbol;
    
    ItemEffectType(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getSymbol() {
        return symbol;
    }
}