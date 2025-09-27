package tetris.scene.game.overlay;

import java.awt.*;
import javax.swing.*;

import java.util.ArrayList;
import java.util.List;


public class GameOverBody extends JPanel {
    private List<EntryComp> compList = new ArrayList<>();
    private Timer animTimer;
    private int animIndex = 0;

    GameOverBody(int score, int lines, int time, String difficulty) {
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setLayout(new GridLayout(4,2,4,4));
        setOpaque(false);

        EntryComp scoreLabel = new EntryComp("score");
        EntryComp linesLabel = new EntryComp("lines");
        EntryComp timeLabel = new EntryComp("time");
        EntryComp difficultyLabel = new EntryComp("difficulty");
        EntryComp scoreValue = new EntryComp(Integer.toString(score));
        EntryComp linesValue = new EntryComp(Integer.toString(lines));
        EntryComp timeValue = new EntryComp(Integer.toString(time));
        EntryComp difficultyValue = new EntryComp(difficulty);

        add(scoreLabel);
        add(scoreValue);

        add(linesLabel);
        add(linesValue);

        add(timeLabel);
        add(timeValue);

        add(difficultyLabel);
        add(difficultyValue);

        compList.add(scoreLabel);
        compList.add(linesLabel);
        compList.add(timeLabel);
        compList.add(difficultyLabel);

        compList.add(scoreValue);
        compList.add(linesValue);
        compList.add(timeValue);
        compList.add(difficultyValue);

    }

    public void startAnimation(float duration, float overshoot) {
        int delay = (int)(duration * 1000);
        animTimer = new Timer(delay, e -> {
            if(animIndex >= compList.size()) {
                ((Timer)e.getSource()).stop();
                return;
            }
            compList.get(animIndex++).startAnimation(duration,overshoot);
        });
        animTimer.start();
    }
}

class EntryComp extends JLabel {
    float alpha = 0f;
    float offsetX = 0f;   // X축 이동 값
    long startTime = -1L;
    Timer animTimer;
    int delay = 16;

    EntryComp(String text) {
        setText(text);
    }

    void startAnimation(float duration, float overshoot) {
        final long durationNanos = (long)(duration * 1_000_000_000L);

        animTimer = new Timer(delay, e -> {
            long now = System.nanoTime();
            if (startTime < 0) startTime = now;
            float t = Math.min(1f, (now - startTime) / (float) durationNanos);

            // easeOutCubic
            float ease = 1 - (float)Math.pow(1 - t, 3);

            // 왼쪽에서 시작: -width → 0
            offsetX = -(1 - ease) * getWidth()
                    + overshoot * (1 - ease) * getWidth() * t;

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
