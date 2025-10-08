package tetris.scene.game.core;

/**
 * 게임 상태 관리를 담당하는 클래스
 * 
 * 이 클래스는 테트리스 게임의 전반적인 상태(일시정지, 게임오버 등)와
 * 시간 추적을 관리하는 역할을 담당합니다.
 * 
 * 주요 기능:
 * - 게임 상태 관리 (PLAYING, PAUSED, GAME_OVER)
 * - 게임 시간 추적 (경과시간, 일시정지 시간)
 * - 상태 전환 로직
 * - 시간 포맷팅 및 계산
 * 
 * @author SE_Tetris_Team1
 */
public class GameStateManager {
    
    /**
     * 게임 상태 열거형
     */
    public enum GameState {
        PLAYING,    // 게임 진행 중
        PAUSED,     // 일시정지
        GAME_OVER   // 게임 종료
    }
    
    /**
     * 게임 상태 변경 콜백 인터페이스
     */
    public interface StateChangeCallback {
        void onStateChanged(GameState oldState, GameState newState);
        void onGameOver();
        void onPauseToggled(boolean isPaused);
    }
    
    // 게임 상태
    private GameState currentState;
    
    // 시간 추적 변수들
    private long gameStartTime;
    private long pausedTotalTime;
    private long pauseStartTime;
    
    // 콜백 인터페이스
    private StateChangeCallback callback;
    
    /**
     * GameStateManager 생성자
     * 
     * @param callback 상태 변경 시 호출될 콜백
     */
    public GameStateManager(StateChangeCallback callback) {
        this.callback = callback;
        reset();
    }
    
    /**
     * 게임 상태를 초기화합니다.
     */
    public void reset() {
        GameState oldState = currentState;
        currentState = GameState.PLAYING;
        
        // 시간 추적 초기화
        gameStartTime = System.currentTimeMillis();
        pausedTotalTime = 0;
        pauseStartTime = 0;
        
        if (callback != null && oldState != null) {
            callback.onStateChanged(oldState, currentState);
        }
        
        System.out.println("GameStateManager: Game state reset to PLAYING");
    }
    
    /**
     * 일시정지 상태를 토글합니다.
     */
    public void togglePause() {
        if (currentState == GameState.GAME_OVER) {
            return; // 게임 오버 상태에서는 일시정지 불가
        }
        
        GameState oldState = currentState;
        
        if (currentState == GameState.PLAYING) {
            // 일시정지 시작
            currentState = GameState.PAUSED;
            pauseStartTime = System.currentTimeMillis();
            System.out.println("GameStateManager: Game PAUSED");
        } else if (currentState == GameState.PAUSED) {
            // 일시정지 해제
            currentState = GameState.PLAYING;
            if (pauseStartTime > 0) {
                pausedTotalTime += System.currentTimeMillis() - pauseStartTime;
                pauseStartTime = 0;
            }
            System.out.println("GameStateManager: Game RESUMED");
        }
        
        if (callback != null) {
            callback.onStateChanged(oldState, currentState);
            callback.onPauseToggled(currentState == GameState.PAUSED);
        }
    }
    
    /**
     * 게임 오버 상태로 전환합니다.
     */
    public void triggerGameOver() {
        if (currentState == GameState.GAME_OVER) {
            return; // 이미 게임 오버 상태
        }
        
        GameState oldState = currentState;
        currentState = GameState.GAME_OVER;
        
        // 일시정지 중이었다면 일시정지 시간을 정리
        if (oldState == GameState.PAUSED && pauseStartTime > 0) {
            pausedTotalTime += System.currentTimeMillis() - pauseStartTime;
            pauseStartTime = 0;
        }
        
        System.out.println("GameStateManager: Game OVER");
        
        if (callback != null) {
            callback.onStateChanged(oldState, currentState);
            callback.onGameOver();
        }
    }
    
    /**
     * 현재 게임 상태를 반환합니다.
     * 
     * @return 현재 게임 상태
     */
    public GameState getCurrentState() {
        return currentState;
    }
    
    /**
     * 게임이 진행 중인지 확인합니다.
     * 
     * @return 게임이 진행 중이면 true
     */
    public boolean isPlaying() {
        return currentState == GameState.PLAYING;
    }
    
    /**
     * 게임이 일시정지 상태인지 확인합니다.
     * 
     * @return 일시정지 상태이면 true
     */
    public boolean isPaused() {
        return currentState == GameState.PAUSED;
    }
    
    /**
     * 게임이 종료된 상태인지 확인합니다.
     * 
     * @return 게임 종료 상태이면 true
     */
    public boolean isGameOver() {
        return currentState == GameState.GAME_OVER;
    }
    
    /**
     * 현재까지의 경과 시간을 초 단위로 반환합니다.
     * 
     * @return 경과 시간 (초)
     */
    public int getElapsedTimeInSeconds() {
        if (gameStartTime == 0) return 0;
        
        long currentTime = System.currentTimeMillis();
        long totalElapsed = currentTime - gameStartTime;
        
        // 현재 일시정지 상태인 경우
        if (currentState == GameState.PAUSED && pauseStartTime > 0) {
            totalElapsed -= (currentTime - pauseStartTime);
        }
        
        // 총 일시정지 시간 제외
        totalElapsed -= pausedTotalTime;
        
        return (int) (totalElapsed / 1000);
    }
    
    /**
     * 시간을 MM:SS 형식으로 포맷팅합니다.
     * 
     * @param seconds 초 단위 시간
     * @return MM:SS 형식의 문자열
     */
    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
    
    /**
     * 현재 시간을 포맷팅된 문자열로 반환합니다.
     * 
     * @return MM:SS 형식의 현재 경과 시간
     */
    public String getFormattedElapsedTime() {
        return formatTime(getElapsedTimeInSeconds());
    }
    
    /**
     * 게임 시작 시간을 반환합니다.
     * 
     * @return 게임 시작 시간 (밀리초)
     */
    public long getGameStartTime() {
        return gameStartTime;
    }
    
    /**
     * 총 일시정지 시간을 반환합니다.
     * 
     * @return 총 일시정지 시간 (밀리초)
     */
    public long getTotalPausedTime() {
        long total = pausedTotalTime;
        
        // 현재 일시정지 중이라면 현재 일시정지 시간도 포함
        if (currentState == GameState.PAUSED && pauseStartTime > 0) {
            total += System.currentTimeMillis() - pauseStartTime;
        }
        
        return total;
    }
    
    /**
     * 상태 변경 콜백을 설정합니다.
     * 
     * @param callback 새로운 콜백
     */
    public void setCallback(StateChangeCallback callback) {
        this.callback = callback;
    }
    
    /**
     * 디버그용: 현재 상태 정보를 출력합니다.
     */
    public void printStateInfo() {
        System.out.println("=== Game State Info ===");
        System.out.println("Current State: " + currentState);
        System.out.println("Elapsed Time: " + getFormattedElapsedTime());
        System.out.println("Total Paused Time: " + (getTotalPausedTime() / 1000) + "s");
        System.out.println("Is Playing: " + isPlaying());
        System.out.println("Is Paused: " + isPaused());
        System.out.println("Is Game Over: " + isGameOver());
        System.out.println("=======================");
    }
}