package tetris.scene.game.core;

import tetris.util.LineBlinkEffect;
import tetris.util.SpeedUp;
import tetris.GameSettings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 게임의 모든 타이머를 관리하는 클래스
 * - 블록 드롭 타이머 (게임 진행)
 * - 점멸 효과 타이머 (시각적 업데이트)
 */
public class TimerManager {
    private static final int INIT_INTERVAL_MS = 1000; // 블록 드롭 초기 속도 (밀리초)
    private static final int BLINK_INTERVAL_MS = 50; // 점멸 효과 업데이트 주기 (밀리초)
    
    // 타이머들
    private Timer dropTimer; // 블록 드롭 타이머
    private Timer blinkTimer; // 점멸 효과 전용 타이머
    
    // 의존성들
    private final GameStateManager gameStateManager;
    private final ScoreManager scoreManager;
    private final GameSettings.Difficulty difficulty;
    
    // 콜백 인터페이스들
    private TimerCallback timerCallback;
    private SpeedUp speedUp;
    private LineBlinkEffect lineBlinkEffect;
    
    /**
     * 타이머 콜백 인터페이스
     */
    public interface TimerCallback {
        void onDropTick(); // 블록 드롭 타이머 틱
        void onBlinkTick(); // 점멸 효과 타이머 틱
        void onLineDeletion(); // 줄 삭제 완료
    }
    
    public TimerManager(GameStateManager gameStateManager, ScoreManager scoreManager, GameSettings.Difficulty difficulty) {
        this.gameStateManager = gameStateManager;
        this.scoreManager = scoreManager;
        this.difficulty = difficulty;
        // speedUp is initialized in setupSpeedUp
    }
    
    /**
     * 타이머 시스템을 초기화합니다.
     */
    public void initialize(TimerCallback callback) {
        this.timerCallback = callback;
        createDropTimer();
        createBlinkTimer();
    }
    
    /**
     * 블록 드롭 타이머를 생성합니다.
     */
    private void createDropTimer() {
        if (dropTimer != null) dropTimer.stop();
        
        dropTimer = new Timer(INIT_INTERVAL_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 점멸 연출 중이 아니고, 일시정지나 게임 종료 상태가 아닐 때만 블록 이동
                boolean isBlinking = (lineBlinkEffect != null && lineBlinkEffect.isActive());
                if (gameStateManager.isPlaying() && !isBlinking) {
                    timerCallback.onDropTick();
                }
            }
        });
    }
    
    /**
     * 점멸 효과 타이머를 생성합니다.
     */
    private void createBlinkTimer() {
        if (blinkTimer != null) blinkTimer.stop();
        
        blinkTimer = new Timer(BLINK_INTERVAL_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 게임오버 상태가 아닐 때만 점멸 업데이트 및 화면 갱신
                if (!gameStateManager.isGameOver()) {
                    // 줄 점멸 연출 업데이트
                    if (lineBlinkEffect != null) {
                        lineBlinkEffect.update();
                    }
                    timerCallback.onBlinkTick();
                }
            }
        });
    }
    
    /**
     * SpeedUp 관리자를 설정합니다.
     */
    public void setupSpeedUp() {
        speedUp = new SpeedUp(dropTimer, new SpeedUp.SpeedIncreaseCallback() {
            @Override
            public void onSpeedIncrease() {
                // 속도가 증가할 때마다 점수 배율도 증가
                scoreManager.onSpeedIncrease();
            }
        }, difficulty);
    }
    
    /**
     * 줄 점멸 연출 관리자를 설정합니다.
     */
    public void setupLineBlinkEffect() {
        lineBlinkEffect = new LineBlinkEffect(new LineBlinkEffect.BlinkEffectCallback() {
            @Override
            public void onBlinkComplete() {
                // 연출이 끝나면 실제로 줄을 삭제
                timerCallback.onLineDeletion();
            }
            
            @Override
            public void onEffectUpdate() {
                // 연출 업데이트 시 화면 갱신
                timerCallback.onBlinkTick();
            }
        });
    }
    
    /**
     * 모든 타이머를 시작합니다.
     */
    public void startTimers() {
        if (dropTimer != null && !dropTimer.isRunning()) {
            dropTimer.start();
        }
        if (blinkTimer != null && !blinkTimer.isRunning()) {
            blinkTimer.start();
        }
    }
    
    /**
     * 모든 타이머를 정지합니다.
     */
    public void stopTimers() {
        if (dropTimer != null) {
            dropTimer.stop();
        }
        if (blinkTimer != null) {
            blinkTimer.stop();
        }
    }
    
    /**
     * 드롭 타이머만 정지합니다.
     */
    public void stopDropTimer() {
        if (dropTimer != null) {
            dropTimer.stop();
        }
    }
    
    /**
     * 점멸 타이머만 정지합니다.
     */
    public void stopBlinkTimer() {
        if (blinkTimer != null) {
            blinkTimer.stop();
        }
    }
    
    /**
     * 타이머 딜레이를 초기값으로 리셋합니다.
     */
    public void resetSpeed() {
        if (dropTimer != null) {
            dropTimer.setDelay(INIT_INTERVAL_MS);
        }
        if (speedUp != null) {
            speedUp.reset();
        }
    }
    
    /**
     * SpeedUp 관리자를 반환합니다.
     */
    public SpeedUp getSpeedUp() {
        return speedUp;
    }
    
    /**
     * LineBlinkEffect 관리자를 반환합니다.
     */
    public LineBlinkEffect getLineBlinkEffect() {
        return lineBlinkEffect;
    }
    
    /**
     * 드롭 타이머가 실행 중인지 확인합니다.
     */
    public boolean isDropTimerRunning() {
        return dropTimer != null && dropTimer.isRunning();
    }
    
    /**
     * 점멸 타이머가 실행 중인지 확인합니다.
     */
    public boolean isBlinkTimerRunning() {
        return blinkTimer != null && blinkTimer.isRunning();
    }
    
    /**
     * 리소스를 정리합니다.
     */
    public void cleanup() {
        stopTimers();
        if (speedUp != null) {
            speedUp.reset();
        }
        dropTimer = null;
        blinkTimer = null;
        speedUp = null;
        lineBlinkEffect = null;
    }
}