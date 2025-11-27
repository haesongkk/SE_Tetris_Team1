package tetris.scene.battle;

/**
 * 배틀 모드의 플레이어를 나타내는 열거형
 * 플레이어 번호 시스템의 혼란을 해결하기 위해 도입
 */
public enum Player {
    PLAYER_1(0, 1, "Player 1"),
    PLAYER_2(1, 2, "Player 2");
    
    private final int internalId;  // BoardManager 등에서 사용하는 0-based ID
    private final int displayId;   // UI에서 표시하는 1-based ID
    private final String displayName; // UI 표시명
    
    Player(int internalId, int displayId, String displayName) {
        this.internalId = internalId;
        this.displayId = displayId;
        this.displayName = displayName;
    }
    
    public int getInternalId() {
        return internalId;
    }
    
    public int getDisplayId() {
        return displayId;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 상대방 플레이어를 반환
     */
    public Player getOpponent() {
        return this == PLAYER_1 ? PLAYER_2 : PLAYER_1;
    }
    
    /**
     * internal ID로부터 Player 찾기
     */
    public static Player fromInternalId(int internalId) {
        for (Player player : values()) {
            if (player.internalId == internalId) {
                return player;
            }
        }
        throw new IllegalArgumentException("Invalid internal ID: " + internalId);
    }
    
    /**
     * display ID로부터 Player 찾기
     */
    public static Player fromDisplayId(int displayId) {
        for (Player player : values()) {
            if (player.displayId == displayId) {
                return player;
            }
        }
        throw new IllegalArgumentException("Invalid display ID: " + displayId);
    }
}