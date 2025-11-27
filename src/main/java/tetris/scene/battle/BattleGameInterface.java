package tetris.scene.battle;

/**
 * 배틀 모드 게임 시스템의 공통 인터페이스
 * 아이템 효과와 게임 상태 관리를 위한 통일된 API 제공
 */
public interface BattleGameInterface {
    
    // ========== 속도 관련 ==========
    /**
     * 플레이어의 낙하 속도를 설정
     */
    void setPlayerFallSpeed(Player player, double speed);
    
    /**
     * 플레이어의 현재 낙하 속도를 반환
     */
    double getPlayerFallSpeed(Player player);
    
    /**
     * 플레이어의 속도 아이템 활성화 상태를 설정
     */
    void setPlayerSpeedItemActive(Player player, boolean active);
    
    // ========== 시야 관련 ==========
    /**
     * 플레이어의 시야 차단 효과를 설정
     */
    void setPlayerVisionBlock(Player player, boolean active);
    
    /**
     * 플레이어의 시야 차단 상태를 반환
     */
    boolean isPlayerVisionBlocked(Player player);
    
    // ========== 게임 상태 관련 ==========
    /**
     * 게임이 진행 중인지 확인
     */
    boolean isGameActive();
    
    /**
     * 플레이어가 게임 오버 상태인지 확인
     */
    boolean isPlayerGameOver(Player player);
    
    /**
     * 게임 화면 다시 그리기
     */
    void repaintGame();
}