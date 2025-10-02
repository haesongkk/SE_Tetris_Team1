package tetris.scene.game.overlay;

import java.awt.*;

/**
 * 테트리스 게임의 점수 계산 및 표시를 담당하는 클래스
 */
public class ScoreManager {
    private static final int POINTS_PER_LINE = 1000; // 줄 삭제당 점수
    
    private int score; // 현재 점수
    private int linesCleared; // 삭제된 줄 수
    
    public ScoreManager() {
        reset();
    }
    
    /**
     * 점수를 초기화합니다.
     */
    public void reset() {
        score = 0;
        linesCleared = 0;
    }
    
    /**
     * 줄을 삭제했을 때 점수를 추가합니다.
     * @param linesClearedCount 삭제된 줄의 수
     */
    public void addScore(int linesClearedCount) {
        if (linesClearedCount > 0) {
            linesCleared += linesClearedCount;
            score += linesClearedCount * POINTS_PER_LINE;
            
            System.out.println("Cleared " + linesClearedCount + " lines! Total score: " + score);
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

        // 삭제된 줄 수
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        fm = g2d.getFontMetrics();
        String linesLabel = "LINES";
        int linesLabelWidth = fm.stringWidth(linesLabel);
        g2d.drawString(linesLabel, scoreBoardX + (scoreBoardWidth - linesLabelWidth) / 2, scoreBoardY + 70);

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        fm = g2d.getFontMetrics();
        String linesText = String.valueOf(linesCleared);
        int linesWidth = fm.stringWidth(linesText);
        g2d.drawString(linesText, scoreBoardX + (scoreBoardWidth - linesWidth) / 2, scoreBoardY + 90);

        // 줄당 점수 정보
        g2d.setFont(new Font("Arial", Font.PLAIN, 8));
        fm = g2d.getFontMetrics();
        String pointsInfo = POINTS_PER_LINE + " pts/line";
        int pointsWidth = fm.stringWidth(pointsInfo);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString(pointsInfo, scoreBoardX + (scoreBoardWidth - pointsWidth) / 2, scoreBoardY + 110);
    }
}
