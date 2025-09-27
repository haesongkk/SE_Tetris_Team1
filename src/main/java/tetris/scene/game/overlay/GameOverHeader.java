package tetris.scene.game.overlay;

import tetris.scene.scorescene.Theme;

import java.awt.*;
import javax.swing.*;


public class GameOverHeader extends JLabel {
    GameOverHeader() {
        setText("GAME OVER");
        setFont(Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f));
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    float alpha = 0f;
    float offsetX = 0f;   // X축 이동 값
    long startTime = -1L;
    Timer animTimer;
    int delay = 16;

    void startAnimation(float duration, float overshoot) {
        final long durationNanos = (long)(duration * 1_000_000_000L);

        animTimer = new Timer(delay, e -> {
            long now = System.nanoTime();
            if (startTime < 0) startTime = now;
            float t = Math.min(1f, (now - startTime) / (float) durationNanos);

            // easeOutCubic
            float ease = 1 - (float)Math.pow(1 - t, 3);

            // 오버슈트 적용: 목표지점(0)을 살짝 넘었다가 돌아오도록
            // overshoot = 0이면 일반 슬라이드 인
            // overshoot > 0이면 살짝 왼쪽으로 갔다가 0으로 복귀
            offsetX = (1 - ease) * getWidth() - overshoot * (1 - ease) * getWidth() * t;

            // 알파 페이드 인
            alpha = (float) Math.pow(t, 0.6);

            repaint();
            if (t >= 1f) ((Timer) e.getSource()).stop();
        });
        animTimer.start();
    }


    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // X축으로 이동 적용
        g2.translate(offsetX, 0);

        super.paintComponent(g2);
        g2.dispose();
    }
    
}
