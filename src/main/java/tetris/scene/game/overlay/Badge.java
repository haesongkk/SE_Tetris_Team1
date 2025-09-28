package tetris.scene.game.overlay;

import java.awt.*;
import javax.swing.*;

import tetris.scene.scorescene.Theme;

// 작은 둥근 "HIGH SCORE!" 배지
class Badge extends JPanel {
    private float hue = 0f;            // 0~1 HSB hue
    private float alpha = 0f;          // 투명도 (0~1)
    private float scale = 0.8f;        // 크기 스케일
    private Timer timer;
    private Timer introTimer;

    Badge() {
        setOpaque(false);
        setBackground(new Color(30, 30, 30, 240)); // 어두운 배경으로 변경
        setPreferredSize(new Dimension(120, 28));
        setMinimumSize(new Dimension(100, 24));
        setMaximumSize(new Dimension(160, 32));
        setFont(Theme.GIANTS_BOLD.deriveFont(Font.PLAIN, 12f));
        setForeground(new Color(255, 255, 255)); // 밝은 흰색 글자로 변경
        setToolTipText("HIGH SCORE!");
    }



    public void startAnimation(float duration, float overshoot) {
        // duration: 초 단위
        // overshoot: 오버슛 강도 (0.0 ~ 2.0 정도 권장)
        int delay = 16; // 60fps
        final long durationNanos = (long)(duration * 1_000_000_000L);
        final long startTime = System.nanoTime();

        introTimer = new Timer(delay, e -> {
            long now = System.nanoTime();
            float t = Math.min(1f, (now - startTime) / (float) durationNanos);

            // easeOutBack: pop 애니메이션 (작게 → 오버슛 → 제자리)
            float s = overshoot; // overshoot 강도
            float tp = t - 1f;
            float backOut = tp * tp * ((s + 1f) * tp + s) + 1f;

            // alpha는 단순히 페이드인
            alpha = (float) Math.pow(t, 0.6);

            // scale: 0.8에서 시작해서 backOut 곡선에 따라 1.0에 도달
            scale = 0.8f + 0.2f * backOut;

            repaint();

            if (t >= 1f) {
                ((Timer) e.getSource()).stop();
                startHueAnimation(); // 등장 끝나면 색상순환 애니메이션 시작
            }
        });
        introTimer.start();
    }



    private void startHueAnimation() {
        int delay = 16; // 60fps
        timer = new Timer(delay, e -> {
            hue += 0.0035f;
            if (hue > 1f) hue -= 1f;
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (alpha < 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 🔹 잘림 방지
        g2.setClip(null);

        int w = getWidth(), h = getHeight();
        int arc = h;

        
        g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
        // 오버슛 대비: 스케일 적용
        g2.translate(w * 0.5, h * 0.5);
        g2.scale(scale, scale);
        g2.translate(-w * 0.5, -h * 0.5);

        // --- 기존 배지 그리기 ---
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        Color border = Color.getHSBColor(hue, 0.65f, 1.0f);
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(border);
        g2.drawRoundRect(1, 1, w - 2, h - 2, arc - 2, arc - 2);

        String text = "HIGH SCORE!";
        FontMetrics fm = g2.getFontMetrics(getFont());
        int tx = (w - fm.stringWidth(text)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setFont(getFont());
        g2.setColor(getForeground());
        g2.drawString(text, tx, ty);

        g2.dispose();
    }

}
