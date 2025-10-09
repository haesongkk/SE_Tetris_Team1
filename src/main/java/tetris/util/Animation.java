package tetris.util;

import java.awt.*;
import javax.swing.*;

import java.util.List;
import java.util.ArrayList;

import javax.swing.border.EmptyBorder;

public class Animation extends JButton {
    final int delay = 16;

    public Animation(String text, Font font, Color foreground, Color background, Color border, int thickness, int radius, int hAlign, int vAlign) {
        setOpaque(false);  
        setContentAreaFilled(false);
        setFocusPainted(false);
        //setRolloverEnabled(false);
        //setBorderPainted(false);

        setText(text);
        setFont(font);
        
        setHorizontalAlignment(hAlign);
        setVerticalAlignment(vAlign);
        
        setForeground(foreground);
        setBackground(background);
        setBorderColor(border);
        
        setBorder(new EmptyBorder(thickness, thickness, thickness, thickness));


        borderRadius = radius;
        borderThickness = thickness;
        counter.add(this);
    }

    @Override
    public void setBackground(Color background) {
        super.setBackground(background);
        backgroundHSB = Color.RGBtoHSB(background.getRed(), background.getGreen(), background.getBlue(), null);
    }

    public void setBorderColor(Color border) {
        borderHSB = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), null);
    }


    public void release() {
        if(animTimers != null) {
            for(AnimTimer animTimer: animTimers) {
                animTimer.timer.stop();
                animTimer.timer = null;
            }
            animTimers.clear();
            animTimers = null;
        }
        counter.remove(this);
    }

    static List<Animation> counter = new ArrayList<>();


    public float alpha = 0.f;

    public float scaleX = 1.f;
    public float scaleY = 1.f;

    public float rotate = 0.f;

    public float offsetX = 0.f;
    public float offsetY = 0.f;

    public boolean bVisible = false;
    
    public int borderRadius = 0;
    public int borderThickness = 0;
    public float[] borderHSB = new float[3];
    public float[] backgroundHSB = new float[3];

    static List<Timer> runningTimers = new ArrayList<>();

    static public void runLater(float delay, Runnable r) {
        final int delayMs = (int)(delay * 1000);
        Timer t = new Timer(delayMs, e -> { 
            ((Timer)e.getSource()).stop(); 
            runningTimers.remove(e.getSource());
            r.run(); 
        });
        t.setRepeats(false);
        runningTimers.add(t); 
        t.start();

    }

    static public void reset() {
        for(Timer t: runningTimers) {
            t.stop();
        }
        runningTimers.clear();
    }



    class AnimTimer {
        Timer timer;
        long startTime = -1L;
        long getElapsedNanos() {
            return System.nanoTime() - startTime;
        }
    }

    List<AnimTimer> animTimers = new ArrayList<>();
    AnimTimer addAnimTimer() {
        AnimTimer animTimer = new AnimTimer();
        animTimers.add(animTimer);
        return animTimer;
    }
    void deleteAnimTimer(AnimTimer animTimer) {
        animTimer.timer.stop();
        animTimers.remove(animTimer);
    }


    public void hueBackground(float duration, boolean bLoop) {
        alpha = 1f;
        bVisible = true;
        scaleX = 1f;
        scaleY = 1f;

        backgroundHSB[1] = .5f;
        backgroundHSB[2] = .5f;

        final long durationNanos = secToNanos(duration);

        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();
        animTimer.timer = new Timer(delay, e -> {
            long elapsed = animTimer.getElapsedNanos();
            float tp = getTimeProgress(elapsed, durationNanos);

            backgroundHSB[0] = tp % 1f;

            if(!bLoop && tp >= 1f) {
                ((Timer)e.getSource()).stop();
            }

            repaint();
        });
        animTimer.timer.start();
    }


    public void hueBorder(float duration, boolean bLoop) {
        alpha = 1f;
        bVisible = true;
        scaleX = 1f;
        scaleY = 1f;

        borderHSB[1] = .8f;
        borderHSB[2] = .8f;

        final long durationNanos = secToNanos(duration);
        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();
        animTimer.timer = new Timer(delay, e -> {

            long elapsed = animTimer.getElapsedNanos();
            float tp = getTimeProgress(elapsed, durationNanos);

            borderHSB[0] = tp % 1f;

            if(!bLoop && tp >= 1f) {
                ((Timer)e.getSource()).stop();
            }

            repaint();
        });
        animTimer.timer.start();
    }

    public void saturateBorder(float duration, boolean bLoop) {
        alpha = 1f;
        bVisible = true;
        scaleX = 1f;
        scaleY = 1f;
    
        final long durationNanos = secToNanos(duration);
        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();
        animTimer.timer = new Timer(delay, e -> {
    
            long elapsed = animTimer.getElapsedNanos();
            float tp = getTimeProgress(elapsed, durationNanos);
    
            if (bLoop) {
                // 순환: 0 -> 1 -> 0 -> 1 반복
                borderHSB[1] = 0.5f + 0.5f * (float)Math.sin(tp * Math.PI * 2);
            } else {
                borderHSB[1] = tp;
                if (tp >= 1f) {
                    ((Timer)e.getSource()).stop();
                }
            }
    
            repaint();
        });
        animTimer.timer.start();
    }
    

    public void blink(float vis, float nonVis) {
        final long visNanos = (long)(vis * 1_000_000_000L);
        final long nonVisNanos = (long)(nonVis * 1000000000L);
        final long durationNanos = visNanos + nonVisNanos;
        alpha = 1f;
        scaleX = 1f;
        scaleY = 1f;
        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();
        animTimer.timer = new Timer(delay, e -> {
            long elapsed = animTimer.getElapsedNanos();
            long phase   = elapsed % durationNanos;
            bVisible = phase < visNanos;
            repaint();
        });
        animTimer.timer.start();
    }

    public void popIn(float startScaleX, float startScaleY, float duration, float overshoot) {
        alpha = 0f;
        bVisible = true;
        scaleX = startScaleX;
        scaleY = startScaleY;
        final long durationNanos = secToNanos(duration);

        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();
        animTimer.timer = new Timer(delay, e -> {
            long now = System.nanoTime();
            if (animTimer.startTime < 0) animTimer.startTime = now;
            float t = Math.min(1f, (now - animTimer.startTime) / (float) durationNanos);

            float ease = 1 - (float)Math.pow(1 - t, 5); // 감속 곡선 (easeOutCubic)

            // scale: 시작 1+overshoot → 끝 1.0
            scaleX = 1.0f + overshoot * (1 - ease);
            scaleY = 1.0f + overshoot * (1 - ease);

            alpha = (float) Math.pow(t, 0.6);
            repaint();
            if (t >= 1f) ((Timer) e.getSource()).stop();
        });
        animTimer.timer.start();
    }

    public void popOut(float startScaleX, float startScaleY, float duration, float overshoot) {
        alpha = 0f;
        bVisible = true;
        scaleX = startScaleX;
        scaleY = startScaleY;
        final long durationNanos = secToNanos(duration);

        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();


        animTimer.timer = new Timer(delay, e -> {
            long now = System.nanoTime();
            float t = Math.min(1f, (now - animTimer.startTime) / (float) durationNanos);

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
        animTimer.timer.start();
    }

    public void move(int startX, int startY, int endX, int endY, float overshoot, float duration, boolean bLoop) {
        alpha   = 1f;
        scaleX  = 1f;
        scaleY  = 1f;
        final long  durationNanos = secToNanos(duration);
        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();

        animTimer.timer = new Timer(delay, e -> {
            bVisible = true;

            long elapsed = animTimer.getElapsedNanos();
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
        animTimer.timer.start();
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

    protected float nanosToSec(long nanos) {
        return nanos / 1_000_000_000f;
    }

    protected long secToNanos(float sec) {
        return (long)(sec * 1_000_000_000L);
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
    public Dimension getMinimumSize() {
        LayoutManager lm = getLayout();
        if (lm != null) {
            Dimension d = lm.minimumLayoutSize(this);
            Insets in = getInsets();
            d.width  += in.left + in.right;
            d.height += in.top  + in.bottom;
            return d;
        }
        return super.getMinimumSize();
    }

    @Override
    public Dimension getMaximumSize() {
        LayoutManager lm = getLayout();
        if (lm != null) {
            Dimension d = lm.preferredLayoutSize(this); // 과하게 커지지 않게 preferred로 고정
            Insets in = getInsets();
            d.width  += in.left + in.right;
            d.height += in.top  + in.bottom;
            return d;
        }
        return super.getMaximumSize();
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
        g2.setColor(HSBtoColor(backgroundHSB));
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