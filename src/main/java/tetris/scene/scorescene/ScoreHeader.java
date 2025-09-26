package tetris.scene.scorescene;
import java.awt.*;
import javax.swing.*;

public class ScoreHeader extends JLabel{


    ScoreHeader() {
        setText("HIGH SCORE");
        setForeground(Theme.TITLE_YELLOW);
        setFont(Theme.GIANTS_INLINE.deriveFont(Font.ITALIC, 85));
        setBorder(BorderFactory.createEmptyBorder(0,100,0,100));

        setHorizontalAlignment(SwingConstants.CENTER);
        
        

    }
    public void startAnimation() {
        Timer animTimer = new Timer(delay, e -> {
            long now = System.nanoTime();
            if (startTime < 0) startTime = now;
            float t = Math.min(1f, (now - startTime) / (float) durationNanos);

            float overshoot = 1.25f; // 시작에서 얼마나 크게 둘지
            float ease = 1 - (float)Math.pow(1 - t, 5); // 감속 곡선 (easeOutCubic)

            // scale: 시작 1+overshoot → 끝 1.0
            scale = 1.0f + overshoot * (1 - ease);

            alpha = (float) Math.pow(t, 0.6);
            repaint();
            if (t >= 1f) ((Timer) e.getSource()).stop();
        });
        animTimer.start();
    }
    float alpha = 0f;
    float scale = 0.8f;
    long startTime = -1L;
    Timer animTimer;
    final long durationNanos = 320_000_000L;
    int delay = 16;



    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.translate(w * 0.5, h * 0.5);
        g2.scale(scale, scale);
        g2.translate(-w * 0.5, -h * 0.5);
        super.paintComponent(g2);
        g2.dispose();

    }

}
