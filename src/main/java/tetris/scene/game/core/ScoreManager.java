package tetris.scene.game.core;

import java.awt.*;

/**
 * 테트리스 게임의 점수 계산 및 표시를 담당하는 클래스
 */
public class ScoreManager {
    private static final int POINTS_PER_LINE = 1000; // 기본 줄 삭제당 점수
    private static final double SPEED_MULTIPLIER_INCREASE = 0.2; // 속도 증가 시 점수 배율 증가 (20%)
    private static final double MAX_SPEED_MULTIPLIER = 1.6; // 최대 점수 배율 (160%)
    
    private int score; // 현재 점수
    private int linesCleared; // 삭제된 줄 수
    private double speedMultiplier; // 속도에 따른 점수 배율 (1.0 = 100%, 1.2 = 120%)
    
    public ScoreManager() {
        reset();
    }
    
    /**
     * 점수를 초기화합니다.
     */
    public void reset() {
        score = 0;
        linesCleared = 0;
        speedMultiplier = 1.0; // 기본 배율 100%
    }
    
    /**
     * 줄을 삭제했을 때 점수를 추가합니다.
     * @param linesClearedCount 삭제된 줄의 수
     */
    public void addScore(int linesClearedCount) {
        if (linesClearedCount > 0) {
            linesCleared += linesClearedCount;
            
            // 속도 배율을 적용한 점수 계산 (반올림 처리로 정확한 점수 계산)
            int baseScore = linesClearedCount * POINTS_PER_LINE;
            int bonusScore = (int) Math.round(baseScore * speedMultiplier);
            score += bonusScore;
            
            System.out.println("Cleared " + linesClearedCount + " lines! Base: " + baseScore + 
                             ", Multiplier: " + String.format("%.1f", speedMultiplier) + 
                             "x, Final: " + bonusScore + ", Total score: " + score);
        }
    }
    
    /**
     * 현재 점수를 반환합니다.
     * @return 현재 점수
     */
    public int getScore() {
        return score;
    }
    
    /**
     * 삭제된 총 줄 수를 반환합니다.
     * @return 삭제된 줄 수
     */
    public int getLinesCleared() {
        return linesCleared;
    }
    
    /**
     * 게임 속도가 증가했을 때 점수 배율을 증가시킵니다.
     */
    public void onSpeedIncrease() {
        if (speedMultiplier < MAX_SPEED_MULTIPLIER) {
            speedMultiplier += SPEED_MULTIPLIER_INCREASE;
            // 부동소수점 연산 오차를 방지하기 위해 반올림 처리
            speedMultiplier = Math.round(speedMultiplier * 10.0) / 10.0;
            
            // 최댓값 강제 제한
            if (speedMultiplier > MAX_SPEED_MULTIPLIER) {
                speedMultiplier = MAX_SPEED_MULTIPLIER;
            }
            System.out.println("Speed increased! Score multiplier is now: " + String.format("%.1f", speedMultiplier) + "x");
        } else {
            System.out.println("Speed increased! Score multiplier is already at maximum: " + String.format("%.1f", speedMultiplier) + "x");
        }
    }
    
    /**
     * 현재 점수 배율을 반환합니다.
     * @return 현재 점수 배율
     */
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }
    
    /**
     * 줄당 점수를 반환합니다.
     * @return 줄당 점수
     */
    public int getPointsPerLine() {
        return POINTS_PER_LINE;
    }
    
    /**
     * 점수판을 그립니다.
     * @param g2d Graphics2D 객체
     * @param scoreBoardX 점수판 x 좌표
     * @param scoreBoardY 점수판 y 좌표
     * @param scoreBoardWidth 점수판 너비
     * @param scoreBoardHeight 점수판 높이
     */
    public void drawScoreBoard(Graphics2D g2d, int scoreBoardX, int scoreBoardY, 
                              int scoreBoardWidth, int scoreBoardHeight) {
        // 점수판 배경
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(scoreBoardX, scoreBoardY, scoreBoardWidth, scoreBoardHeight);

        // 점수판 테두리
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(scoreBoardX, scoreBoardY, scoreBoardWidth, scoreBoardHeight);

        // 점수 정보 표시
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();

        // "SCORE" 라벨
        String scoreLabel = "SCORE";
        int labelWidth = fm.stringWidth(scoreLabel);
        g2d.drawString(scoreLabel, scoreBoardX + (scoreBoardWidth - labelWidth) / 2, scoreBoardY + 20);

        // 현재 점수
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        fm = g2d.getFontMetrics();
        String scoreText = String.format("%,d", score);
        int scoreWidth = fm.stringWidth(scoreText);
        g2d.drawString(scoreText, scoreBoardX + (scoreBoardWidth - scoreWidth) / 2, scoreBoardY + 45);
    }
}
