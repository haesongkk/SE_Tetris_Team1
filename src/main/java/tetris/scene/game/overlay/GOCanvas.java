package tetris.scene.game.overlay;
import tetris.util.Animation;

import tetris.util.Theme;
import javax.swing.*;
import java.awt.*;

public class GOCanvas extends Animation {

    GOCanvas() {
        super(
            null, Theme.GIANTS_INLINE, 
            Theme.HEADER_RED, Theme.BG, Theme.BADGE_YELLOW,
            2, 0, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        
        int badgeCenterY = (int)(h * 0.25);
        
        // 점선 스타일 설정
        float[] dashPattern = {7f, 5f}; // 5px 점선, 5px 간격
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, dashPattern, 0f));
        g2.setColor(new Color(200, 200, 200, 100)); // 반투명 회색 점선
        
        // 가로 점선 그리기 (좌우 여백 40px씩)
        g2.drawLine(20, badgeCenterY, w - 20, badgeCenterY);

        
        
        g2.dispose();

    }
}
