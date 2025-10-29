package tetris.util;

import javax.swing.Timer;
import tetris.GameSettings;

/**
 * 게임 속도 조정을 위한 유틸리티 클래스
 * 블록 생성 수와 삭제된 줄 수를 기반으로 게임 속도를 동적으로 증가시킵니다.
 */
public class SpeedUp {
    
    /**
     * 속도 증가 시 호출되는 콜백 인터페이스
     */
    public interface SpeedIncreaseCallback {
        void onSpeedIncrease();
    }
    
    // 상수들
    private static final int BLOCKS_THRESHOLD = 5;     // 속도 증가를 위한 블록 생성 임계값
    private static final int LINES_THRESHOLD = 1;      // 속도 증가를 위한 줄 삭제 임계값
    private static final int BASE_INTERVAL_DECREASE = 200;   // 기본 속도 증가 시 감소할 딜레이 시간 (ms)
    private static final int MIN_INTERVAL = 400;        // 최소 딜레이 시간 (최대 속도)
    
    private final GameSettings.Difficulty difficulty; // 난이도
    private final int intervalDecrease; // 난이도에 따른 속도 증가량
    
    // 추적 변수들
    private int blocksGenerated;     // 생성된 블록 수
    private int totalLinesCleared;   // 삭제된 총 줄 수
    private int currentInterval;     // 현재 타이머 딜레이
    
    private Timer timer; // 게임 타이머 참조 (나중에 설정)
    private SpeedIncreaseCallback callback; // 속도 증가 콜백
    private Object gameScene; // GameScene 참조 (속도 아이템 상태 확인용)
    
    /**
     * SpeedUp 객체를 생성합니다.
     * @param timer 게임 타이머
     * @param callback 속도 증가 시 호출될 콜백
     * @param difficulty 난이도
     * @param gameScene GameScene 인스턴스 (속도 아이템 상태 확인용)
     */
    public SpeedUp(Timer timer, SpeedIncreaseCallback callback, GameSettings.Difficulty difficulty, Object gameScene) {
        this.timer = timer;
        this.callback = callback;
        this.difficulty = difficulty;
        this.gameScene = gameScene;
        
        // 난이도에 따른 속도 증가량 설정
        switch (difficulty) {
            case EASY:
                this.intervalDecrease = (int) (BASE_INTERVAL_DECREASE * 0.8); // 20% 덜 증가
                break;
            case HARD:
                this.intervalDecrease = (int) (BASE_INTERVAL_DECREASE * 1.2); // 20% 더 증가
                break;
            case NORMAL:
            default:
                this.intervalDecrease = BASE_INTERVAL_DECREASE;
                break;
        }
        
        this.blocksGenerated = 0;
        this.totalLinesCleared = 0;
        this.currentInterval = 1000; // 실제 타이머 딜레이로 초기화
    }
    
    /**
     * 타이머를 설정합니다.
     */
    public void setTimer(Timer timer) {
        this.timer = timer;
    }
    
    /**
     * 콜백을 설정합니다.
     */
    public void setCallback(SpeedIncreaseCallback callback) {
        this.callback = callback;
    }
    
    /**
     * 모든 카운터와 설정을 초기화합니다.
     */
    public void reset() {
        blocksGenerated = 0;
        totalLinesCleared = 0;
        currentInterval = timer.getDelay();
    }
    
    /**
     * 블록이 생성될 때 호출됩니다.
     * 게임이 진행 중일 때만 카운팅합니다.
     * @param isGameOver 게임 오버 상태
     */
    public void onBlockGenerated(boolean isGameOver) {
        if (!isGameOver) {
            blocksGenerated++;
            checkSpeedIncrease();
        }
    }
    
    /**
     * 줄이 삭제될 때 호출됩니다.
     * @param linesCleared 이번에 삭제된 줄 수
     */
    public void onLinesCleared(int linesCleared) {
        if (linesCleared > 0) {
            totalLinesCleared += linesCleared;
            checkSpeedIncrease();
        }
    }
    
    /**
     * 속도 증가 조건을 확인하고 필요시 속도를 증가시킵니다.
     */
    private void checkSpeedIncrease() {
        // 속도 아이템이 활성화된 경우 자동 속도 증가 방지
        if (isSpeedItemActive()) {
            System.out.println("Speed item is active, skipping automatic speed increase");
            return;
        }
        
        if (blocksGenerated >= BLOCKS_THRESHOLD || totalLinesCleared >= LINES_THRESHOLD) {
            // 현재 딜레이를 감소시켜 속도 증가
            currentInterval = Math.max(MIN_INTERVAL, currentInterval - intervalDecrease);
            
            // 타이머의 딜레이 업데이트
            timer.setDelay(currentInterval);
            
            System.out.println("Speed increased! New interval: " + currentInterval + "ms");
            System.out.println("Blocks generated: " + blocksGenerated + ", Lines cleared: " + totalLinesCleared);
            
            // 속도 증가 콜백 호출
            if (callback != null) {
                callback.onSpeedIncrease();
            }
            
            // 카운터 초기화
            blocksGenerated = 0;
            totalLinesCleared = 0;
        }
    }
    
    /**
     * 속도 아이템이 활성화되어 있는지 확인합니다.
     */
    private boolean isSpeedItemActive() {
        if (gameScene != null) {
            try {
                return (Boolean) gameScene.getClass().getMethod("isSpeedItemActive").invoke(gameScene);
            } catch (Exception e) {
                System.out.println("Failed to check speed item status: " + e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    /**
     * 현재 생성된 블록 수를 반환합니다.
     * @return 생성된 블록 수
     */
    public int getBlocksGenerated() {
        return blocksGenerated;
    }
    
    /**
     * 현재 삭제된 총 줄 수를 반환합니다.
     * @return 삭제된 총 줄 수
     */
    public int getTotalLinesCleared() {
        return totalLinesCleared;
    }
    
    /**
     * 현재 타이머 간격을 반환합니다.
     * @return 현재 타이머 간격 (ms)
     */
    public int getCurrentInterval() {
        return currentInterval;
    }
    
    /**
     * 블록 생성 임계값을 반환합니다.
     * @return 블록 생성 임계값
     */
    public static int getBlocksThreshold() {
        return BLOCKS_THRESHOLD;
    }
    
    /**
     * 줄 삭제 임계값을 반환합니다.
     * @return 줄 삭제 임계값
     */
    public static int getLinesThreshold() {
        return LINES_THRESHOLD;
    }
}