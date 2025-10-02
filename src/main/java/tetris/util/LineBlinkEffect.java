package tetris.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 블록 줄이 완성되어 사라질 때 3번 점멸하는 연출을 담당하는 클래스
 */
public class LineBlinkEffect {
    
    /**
     * 점멸 연출 콜백 인터페이스
     */
    public interface BlinkEffectCallback {
        void onBlinkComplete();
        void onEffectUpdate();
    }
    
    private static final int EFFECT_DURATION = 900; // 연출 지속 시간 (ms) - 150ms * 6 = 900ms (3번 점멸)
    private static final int BLINK_CYCLE = 150; // 150ms마다 토글
    private static final int TOTAL_BLINKS = 3; // 총 점멸 횟수
    
    private List<Integer> blinkingLines; // 점멸하고 있는 줄들
    private long effectStartTime; // 연출 시작 시간
    private boolean isActive; // 연출 활성화 상태
    private BlinkEffectCallback callback; // 콜백
    
    public LineBlinkEffect(BlinkEffectCallback callback) {
        this.callback = callback;
        this.blinkingLines = new ArrayList<>();
        this.isActive = false;
    }
    
    /**
     * 줄 점멸 연출을 시작합니다.
     * @param lineNumbers 점멸할 줄 번호들
     */
    public void startBlinkEffect(List<Integer> lineNumbers) {
        this.blinkingLines = new ArrayList<>(lineNumbers);
        this.effectStartTime = System.currentTimeMillis();
        this.isActive = true;
        
        System.out.println("Line blink effect started for lines: " + lineNumbers);
    }
    
    /**
     * 연출을 업데이트합니다.
     */
    public void update() {
        if (!isActive) return;
        
        long elapsed = System.currentTimeMillis() - effectStartTime;
        
        // 콜백 호출
        if (callback != null) {
            callback.onEffectUpdate();
        }
        
        // 연출 완료 체크
        if (elapsed >= EFFECT_DURATION) {
            isActive = false;
            System.out.println("=== LINE BLINK EFFECT COMPLETED ===");
            if (callback != null) {
                System.out.println("Calling onBlinkComplete callback...");
                callback.onBlinkComplete();
            }
            System.out.println("Line blink effect completed");
        }
    }
    
    /**
     * 현재 줄이 깜빡임 상태인지 확인합니다 (점멸 상태 계산).
     * @return 깜빡임 상태면 true (희미하게), 일반 상태면 false (원래 색상)
     */
    private boolean shouldBlink() {
        if (!isActive) return false;
        
        long elapsed = System.currentTimeMillis() - effectStartTime;
        
        // 연출 완료 체크
        if (elapsed >= EFFECT_DURATION) {
            return false;
        }
        
        // 150ms마다 토글 - 3번 점멸: 희미함(0-150)→원래색(150-300)→희미함(300-450)→원래색(450-600)→희미함(600-750)→원래색(750-900)
        int cycle = (int)(elapsed / BLINK_CYCLE);
        boolean shouldBlink = (cycle % 2 == 0);
        
        // 3번만 점멸하도록 제한 (cycle 0,2,4만 점멸)
        if (cycle >= 6) {
            shouldBlink = false;
        }
        
        System.out.println("Elapsed: " + elapsed + "ms, Cycle: " + cycle + ", Blink: " + shouldBlink);
        
        return shouldBlink;
    }
    
    /**
     * 연출을 그립니다.
     * @param g2d Graphics2D 객체
     * @param cellSize 셀 크기
     * @param gameWidth 게임 보드 너비
     * @param board 게임 보드 배열
     * @param boardColors 게임 보드 색상 배열
     */
    public void draw(Graphics2D g2d, int cellSize, int gameWidth, int[][] board, Color[][] boardColors) {
        if (!isActive) return;
        
        boolean blinkState = shouldBlink();
        
        if (blinkState) {
            // 깜빡임 상태: 완성된 줄의 블록들을 희미한 색상으로 표시
            for (int lineNumber : blinkingLines) {
                for (int col = 0; col < gameWidth; col++) {
                    if (board[lineNumber][col] == 1) {
                        Color originalColor = boardColors[lineNumber][col];
                        if (originalColor != null) {
                            // 원래 색상을 30% 투명도로 희미하게 만들기
                            Color dimColor = new Color(
                                originalColor.getRed(),
                                originalColor.getGreen(),
                                originalColor.getBlue(),
                                80  // 30% 투명도로 더 희미하게
                            );
                            g2d.setColor(dimColor);
                            g2d.fillRect((col + 1) * cellSize + 1, (lineNumber + 1) * cellSize + 1, 
                                        cellSize - 2, cellSize - 2);
                            
                            // 희미한 테두리
                            Color dimBorderColor = new Color(0, 0, 0, 128);
                            g2d.setColor(dimBorderColor);
                            g2d.setStroke(new BasicStroke(1));
                            g2d.drawRect((col + 1) * cellSize + 1, (lineNumber + 1) * cellSize + 1, 
                                        cellSize - 2, cellSize - 2);
                        }
                    }
                }
            }
        }
        // blinkState가 false일 때는 원래 블록들이 그려지므로 별도 처리 불필요
    }
    
    /**
     * 특정 줄이 점멸 중인지 확인합니다.
     * @param lineNumber 확인할 줄 번호
     * @return 점멸 중이면 true
     */
    public boolean isLineBlinking(int lineNumber) {
        return isActive && blinkingLines.contains(lineNumber);
    }
    
    /**
     * 연출이 활성화되어 있는지 확인합니다.
     * @return 활성화 상태
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * 연출을 강제로 중단합니다.
     */
    public void stop() {
        isActive = false;
        blinkingLines.clear();
    }
    
    /**
     * 점멸 중인 줄들의 리스트를 반환합니다.
     * @return 점멸 중인 줄 번호 리스트
     */
    public List<Integer> getBlinkingLines() {
        return new ArrayList<>(blinkingLines);
    }
}