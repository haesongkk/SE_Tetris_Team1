package tetris.scene.battle;

/**
 * 배틀 모드 전용 아이템 효과 컨텍스트
 * 기존 ItemEffectContext보다 명확하고 타입 안전한 정보 제공
 */
public class BattleItemContext {
    private final Player sourcePlayer;  // 아이템을 사용한 플레이어
    private final Player targetPlayer;  // 효과가 적용될 플레이어
    private final BattleGameInterface gameInterface;
    private final int itemX;
    private final int itemY;
    
    public BattleItemContext(Player sourcePlayer, Player targetPlayer, 
                           BattleGameInterface gameInterface, int itemX, int itemY) {
        this.sourcePlayer = sourcePlayer;
        this.targetPlayer = targetPlayer;
        this.gameInterface = gameInterface;
        this.itemX = itemX;
        this.itemY = itemY;
    }
    
    public Player getSourcePlayer() {
        return sourcePlayer;
    }
    
    public Player getTargetPlayer() {
        return targetPlayer;
    }
    
    public BattleGameInterface getGameInterface() {
        return gameInterface;
    }
    
    public int getItemX() {
        return itemX;
    }
    
    public int getItemY() {
        return itemY;
    }
}