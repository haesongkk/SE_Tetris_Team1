package tetris.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Animation extends JLabel {
    final int delay = 16;

    public Animation(String text, Font font, Color foreground, Color background, Color border, int thickness, int radius, int hAlign, int vAlign) {
        borderRadius = radius;
        borderThickness = thickness;
        borderHSB = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), null);
        setOpaque(false);

        setText(text);
        setFont(font);

        setHorizontalAlignment(hAlign);
        setVerticalAlignment(vAlign);

        setForeground(foreground);
        setBackground(background);
        
        setBorder(new EmptyBorder(thickness, thickness, thickness, thickness));
    }

    float alpha = 0.f;

    float scaleX = 0.8f;
    float scaleY = 0.8f;

    float rotate = 0.f;

    float offsetX = 0.f;
    float offsetY = 0.f;

    boolean bVisible = false;
    
    long startTime = -1L;
    Timer animTimer;

    int borderRadius = 0;
    int borderThickness = 0;
    float[] borderHSB = new float[3];


    public void hueBorder(float duration, boolean bLoop) {
        alpha = 1f;
        bVisible = true;
        scaleX = 1f;
        scaleY = 1f;
        startTime = System.nanoTime();

        final long durationNanos = getDurationNanos(duration);
        animTimer = new Timer(delay, e -> {

            long elapsed = getElapsedNanos();
            float tp = getTimeProgress(elapsed, durationNanos);

            borderHSB[0] = tp % 1f;

            if(!bLoop && tp >= 1f) {
                ((Timer)e.getSource()).stop();
            }

            repaint();
        });
        animTimer.start();
    }

    public void pop(float startScaleX, float startScaleY, float duration, float overshoot) {
        alpha = 0f;
        bVisible = true;
        scaleX = startScaleX;
        scaleY = startScaleY;
        startTime = System.nanoTime();

        final long durationNanos = getDurationNanos(duration);

        animTimer = new Timer(delay, e -> {
            long now = System.nanoTime();
            float t = Math.min(1f, (now - startTime) / (float) durationNanos);

            float s = overshoot; // overshoot 강도
            float tp = t - 1f;
            float backOut = tp * tp * ((s + 1f) * tp + s) + 1f;


            alpha = (float) Math.pow(t, 0.6);
            scaleX = startScaleX + (1f - startScaleX) * backOut;
            scaleY = startScaleY + (1f - startScaleY) * backOut;

            repaint();

            if (t >= 1f) {
                ((Timer) e.getSource()).stop();
            }
        });
        animTimer.start();
    }

    public void move(int startX, int startY, int endX, int endY,
    float overshoot, float duration, boolean bLoop) {
        alpha   = 1f;
        scaleX  = 1f;
        scaleY  = 1f;
        final long  durationNanos = getDurationNanos(duration);
        startTime = System.nanoTime();

        animTimer = new Timer(delay, e -> {
            bVisible = true;

            long elapsed = getElapsedNanos();
            float t = getTimeProgress(elapsed, durationNanos);

            // 위치 보간
            offsetX = interpolate(startX, endX, overshoot, t % 1f);
            offsetY = interpolate(startY, endY, overshoot, t % 1f);

            repaint();

            if(!bLoop && t >= 1f) {
                ((Timer)e.getSource()).stop();
                offsetX = endX;
                offsetY = endY;
            }
        });
        animTimer.start();
    }

    /** 0..1로 클램프 */
    protected float clamp01(float t) {
        return t < 0f ? 0f : (t > 1f ? 1f : t);
    }

    /** Cubic ease-out */
    protected float easeOutCubic(float t) {
        t = clamp01(t);
        float u = 1f - t;
        return 1f - u*u*u;
    }

    /** Cubic ease-in */
    protected float easeInCubic(float t) {
        t = clamp01(t);
        return t*t*t;
    }


    protected float interpolate(float start, float end, float overshoot, float t) {
        t = clamp01(t);  // 0~1로 보정
        float eased;
    
        if (overshoot > 0f) {
            // Back Ease-Out (끝에서 살짝 넘어갔다가 돌아옴)
            float s = 1.70158f * overshoot; // 강도 조절
            float u = t - 1f;
            eased = 1f + (s + 1f) * u * u * u + s * u * u;
        } else {
            // 일반 Cubic Ease-Out
            eased = easeOutCubic(t);
        }
    
        return start + (end - start) * eased;
    }




    protected long getDurationNanos(float duration) {
        return (long)(duration * 1_000_000_000L);
    }

    protected long getElapsedNanos() {
        long now = System.nanoTime();
        return now - startTime;
    }

    protected float getTimeProgress(long cur, long total) {
        return (float)cur / (float)total;
    }

    protected Color HSBtoColor(float[] hsb) {
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    @Override
    public Dimension getPreferredSize() {
        LayoutManager lm = getLayout();
        if (lm != null) {
            Dimension d = lm.preferredLayoutSize(this);
            Insets in = getInsets();
            d.width  += in.left + in.right;
            d.height += in.top  + in.bottom;
            return d;
        }
        // JLabel 기본 크기만 쓰고, 강제 최소치 제거
        return super.getPreferredSize();
    }



    @Override
    public void paint(Graphics g) {
        if(!bVisible) return;

        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        g2.translate(offsetX, offsetY);

        g2.translate(w * 0.5, h * 0.5);
        g2.rotate(Math.toRadians(rotate));
        g2.scale(scaleX, scaleY);
        g2.translate(-w * 0.5, -h * 0.5);


        super.paint(g2);
        g2.dispose();
    }



    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getBackground());
        float o = borderThickness / 2f;
        int w = getWidth(), h = getHeight();
        g2.fillRoundRect(Math.round(o), Math.round(o),
                        Math.round(w - 1 - borderThickness),
                        Math.round(h - 1 - borderThickness),
                        borderRadius, borderRadius);
        g2.dispose();

        super.paintComponent(g); // 텍스트, 아이콘 등 그리기
    }

    @Override
    public void paintBorder(Graphics g) {
        if(!bVisible) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(borderThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(HSBtoColor(borderHSB));

        float o = borderThickness / 2f;
        int w = getWidth(), h = getHeight();
        g2.drawRoundRect(Math.round(o), Math.round(o),
                        Math.round(w - 1 - borderThickness),
                        Math.round(h - 1 - borderThickness),
                        borderRadius, borderRadius);
        g2.dispose();
    }



    
}