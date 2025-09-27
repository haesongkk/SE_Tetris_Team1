package tetris.scene.game.overlay;

import tetris.scene.scorescene.Theme;
import java.awt.*;
import javax.swing.*;

public class GameOverFooter extends JPanel {

    // ---- 애니 상태 ----
    private float alpha = 0f;       // 0~1
    private float offsetY = 0f;     // 아래쪽에서 위로 들어옴(+에서 0으로)
    private long startTime = -1L;
    private Timer animTimer;
    private final int frameDelay = 16; // ~60fps

    GameOverFooter(boolean bHigh) {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setOpaque(false);
        setLayout(new BorderLayout());

        if (bHigh) {
            JTextField nameField = new JTextField(12);
            nameField.setFont(Theme.GIANTS_INLINE.deriveFont(Font.PLAIN, 24f));
            nameField.setHorizontalAlignment(JTextField.CENTER);

            JLabel title = new JLabel("ENTER YOUR NAME:", SwingConstants.LEFT);
            title.setFont(Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 16f));
            title.setForeground(Theme.TEXT_GRAY);

            add(title, BorderLayout.NORTH);
            add(nameField, BorderLayout.CENTER);

        } else {
            JLabel escLabel = new JLabel("P R E S S   E S C   T O   E X I T", SwingConstants.CENTER);
            escLabel.setFont(Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 32f));
            escLabel.setForeground(Theme.TEXT_GRAY);
            add(escLabel, BorderLayout.CENTER);
        }
    }

    /** 아래에서 위로 슬라이드 + 페이드인 + 살짝 오버슈트 */
    public void startAnimation(float durationSec, float overshoot) {
        final long durationNanos = (long)(durationSec * 1_000_000_000L);
        startTime = -1L;

        // 시작 오프셋: 자신의 높이(없으면 대략 48px)만큼 아래
        int startOffset = Math.max(48, getPreferredSize().height / 6);
        offsetY = startOffset;
        alpha = 0f;

        animTimer = new Timer(frameDelay, e -> {
            long now = System.nanoTime();
            if (startTime < 0) startTime = now;
            float t = Math.min(1f, (now - startTime) / (float) durationNanos);

            // 완만 -> 오버슈트 순서로 조합 (부드럽게)
            float eased = easeOutCubic(t);
            float back  = easeOutBack(eased, overshoot); // 1을 살짝 넘었다가 복귀

            // 위치: 시작 offsetY -> 0
            offsetY = startOffset * (1f - back);

            // 페이드인
            alpha = (float)Math.pow(t, 0.8);

            repaint();
            if (t >= 1f) {
                offsetY = 0f; alpha = 1f;
                ((Timer)e.getSource()).stop();
            }
        });
        animTimer.start();
    }



    @Override
    public void paint(Graphics g) {
        if (alpha <= 0f) return; // 완전 투명일 때는 그리지 않음
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2.translate(0, offsetY);
        super.paint(g2);
        g2.dispose();
    }


    // 이징 함수들
    private static float easeOutCubic(float t) {
        return 1f - (float)Math.pow(1f - t, 3);
    }
    private static float easeOutBack(float t, float s) {
        // s: 오버슈트 강도(1.2~2.0 사이 추천, 높을수록 더 튐)
        float tp = t - 1f;
        return tp*tp*((s+1f)*tp + s) + 1f;
    }

    void close(String msg) {
        // TODO: 닉네임 제출/ESC 처리 등
    }
}
