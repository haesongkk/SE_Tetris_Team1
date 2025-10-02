package tetris.scene.game.blocks;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 블록 흔들림 효과를 관리하는 유틸리티 클래스
 */
public class BlockShake {
    
    // 흔들림 설정 상수
    private static final int MAX_SHAKE_COUNT = 6; // 흔들림 횟수
    private static final int SHAKE_INTENSITY = 3; // 흔들림 강도 (픽셀)
    private static final int SHAKE_INTERVAL = 60; // 흔들림 간격 (밀리초)
    
    // 흔들림 상태 변수들
    private boolean isShaking = false;
    private int shakeOffsetX = 0;
    private int shakeOffsetY = 0;
    private Timer shakeTimer;
    private int shakeCount = 0;
    private ShakeCallback callback;
    
    /**
     * 흔들림 완료 시 호출되는 콜백 인터페이스
     */
    public interface ShakeCallback {
        void onShakeUpdate();
        void onShakeComplete();
    }
    
    /**
     * BlockShake 생성자
     * @param callback 흔들림 업데이트 및 완료 시 호출될 콜백
     */
    public BlockShake(ShakeCallback callback) {
        this.callback = callback;
    }
    
    /**
     * 흔들림 효과를 시작합니다.
     */
    public void startShake() {
        if (isShaking) return; // 이미 흔들리고 있으면 무시
        
        isShaking = true;
        shakeCount = 0;
        
        // 기존 흔들림 타이머가 있다면 정지
        stopShakeTimer();
        
        // 흔들림 타이머 생성
        shakeTimer = new Timer(SHAKE_INTERVAL, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateShake();
            }
        });
        
        shakeTimer.start();
    }
    
    /**
     * 흔들림 효과를 업데이트합니다.
     */
    private void updateShake() {
        if (!isShaking) return;
        
        // 흔들림 오프셋 계산 (좌우로 흔들기)
        if (shakeCount % 2 == 0) {
            shakeOffsetX = SHAKE_INTENSITY;
            shakeOffsetY = 0;
        } else {
            shakeOffsetX = -SHAKE_INTENSITY;
            shakeOffsetY = 0;
        }
        
        shakeCount++;
        
        // 화면 업데이트 콜백 호출
        if (callback != null) {
            callback.onShakeUpdate();
        }
        
        // 흔들림 종료 조건
        if (shakeCount >= MAX_SHAKE_COUNT) {
            stopShake();
        }
    }
    
    /**
     * 흔들림 효과를 중지합니다.
     */
    public void stopShake() {
        isShaking = false;
        shakeOffsetX = 0;
        shakeOffsetY = 0;
        shakeCount = 0;
        
        stopShakeTimer();
        
        // 완료 콜백 호출
        if (callback != null) {
            callback.onShakeComplete();
        }
    }
    
    /**
     * 흔들림 타이머를 정지합니다.
     */
    private void stopShakeTimer() {
        if (shakeTimer != null) {
            shakeTimer.stop();
            shakeTimer = null;
        }
    }
    
    /**
     * 현재 흔들림 상태를 반환합니다.
     * @return 흔들리고 있으면 true, 아니면 false
     */
    public boolean isShaking() {
        return isShaking;
    }
    
    /**
     * 현재 X축 흔들림 오프셋을 반환합니다.
     * @return X축 오프셋 (픽셀)
     */
    public int getShakeOffsetX() {
        return shakeOffsetX;
    }
    
    /**
     * 현재 Y축 흔들림 오프셋을 반환합니다.
     * @return Y축 오프셋 (픽셀)
     */
    public int getShakeOffsetY() {
        return shakeOffsetY;
    }
    
    /**
     * 리소스를 정리합니다. (메모리 누수 방지)
     */
    public void cleanup() {
        stopShake();
        callback = null;
    }
}