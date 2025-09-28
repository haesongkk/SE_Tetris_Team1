package tetris.scene.game.overlay;

import tetris.util.Theme;

import java.awt.*;
import javax.swing.*;

public class GOFooter extends JPanel {

    // ---- 애니 상태 ----
    private float alpha = 0f;       // 0~1
    private float offsetY = 0f;     // 아래쪽에서 위로 들어옴(+에서 0으로)
    private long startTime = -1L;
    private Timer animTimer;
    private final int frameDelay = 16; // ~60fps
    
    // ---- 깜빡임 애니메이션 상태 ----
    private boolean isBlinking = false;
    private boolean blinkVisible = false;
    private int blinkCount = 0;
    private Timer blinkTimer;
    
    // ---- 텍스트 필드 참조 ----
    private JTextField nameField;
    
    // ---- RETRY 버튼 관련 ----
    private JLabel countdownLabel;
    private JButton retryButton;
    private Timer countdownTimer;
    private int countdown = 5;

    GOFooter(boolean bHigh) {
        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5)); // 위아래 여백을 5px -> 2px로 줄임
        setOpaque(false);
        setLayout(new GridBagLayout()); // BorderLayout -> GridBagLayout으로 변경
        
        // Footer의 크기를 더 예쁘게 조정 (가로 폭 더 줄임)
        setPreferredSize(new Dimension(300, 100)); // 가로 350 -> 300으로 더 줄임
        setMinimumSize(new Dimension(300, 100));
        setMaximumSize(new Dimension(300, 100));

        if (bHigh) {
            nameField = new JTextField(6); // 컬럼 수를 더 줄임
            nameField.setFont(Theme.GIANTS_REGULAR.deriveFont(Font.PLAIN, 18f)); // 폰트 크기를 더 줄임
            nameField.setHorizontalAlignment(JTextField.CENTER);
            // 입력창 크기를 명시적으로 작게 설정
            nameField.setPreferredSize(new Dimension(100, 30)); // 가로 120px, 세로 20px로 고정
            nameField.setMinimumSize(new Dimension(100, 30));
            nameField.setMaximumSize(new Dimension(100, 30));

            JLabel title = new JLabel("ENTER YOUR NAME:", SwingConstants.LEFT);
            title.setFont(Theme.GIANTS_REGULAR.deriveFont(Font.BOLD, 14f));
            title.setForeground(Theme.TEXT_GRAY);

            // GridBagLayout으로 컴포넌트 추가
            add(title, new GridBagConstraints(
                0, 0, // gridx, gridy
                1, 1, // gridwidth, gridheight
                1.0, 0.0, // weightx, weighty
                GridBagConstraints.LINE_START, // anchor
                GridBagConstraints.NONE, // fill
                new Insets(0, 20, 5, 0), // insets (제목 아래 여백)
                0, 0 // ipadx, ipady
            ));
            
            add(nameField, new GridBagConstraints(
                0, 1, // gridx, gridy
                1, 1, // gridwidth, gridheight
                0.0, 0.0, // weightx, weighty (크기 고정)
                GridBagConstraints.CENTER, // anchor
                GridBagConstraints.HORIZONTAL, // fill (늘어나지 않음)
                new Insets(0, 20, 0, 20), // insets
                0, 0 // ipadx, ipady
            ));

        } else {
            // 카운트다운 라벨
            countdownLabel = new JLabel("5", SwingConstants.CENTER);
            countdownLabel.setFont(Theme.GIANTS_REGULAR.deriveFont(Font.BOLD, 24.f));
            countdownLabel.setForeground(Theme.O_YELLOW);
            
            // RETRY 버튼
            retryButton = new JButton("RETRY?") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int w = getWidth();
                    int h = getHeight();
                    
                    // 그라데이션 배경 (흰색 계열)
                    GradientPaint gradient;
                    if (getModel().isPressed()) {
                        gradient = new GradientPaint(0, 0, new Color(220, 220, 220), 0, h, new Color(180, 180, 180));
                    } else if (getModel().isRollover()) {
                        gradient = new GradientPaint(0, 0, new Color(250, 250, 250), 0, h, new Color(230, 230, 230));
                    } else {
                        gradient = new GradientPaint(0, 0, new Color(240, 240, 240), 0, h, new Color(200, 200, 200));
                    }
                    
                    g2.setPaint(gradient);
                    g2.fillRoundRect(0, 0, w, h, 15, 15);
                    
                    // 테두리
                    g2.setStroke(new BasicStroke(2f));
                    g2.setColor(new Color(120, 120, 120));
                    g2.drawRoundRect(1, 1, w-2, h-2, 15, 15);
                    
                    // 내부 그림자 효과
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRoundRect(2, 2, w-4, h/2, 15, 15);
                    
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            
            retryButton.setFont(Theme.GIANTS_BOLD.deriveFont(Font.ITALIC, 20.f));
            retryButton.setForeground(new Color(60, 60, 60));
            retryButton.setBorder(BorderFactory.createEmptyBorder(12, 10, 10, 10));
            retryButton.setPreferredSize(new Dimension(120, 80));
            retryButton.setMinimumSize(new Dimension(120, 80));
            retryButton.setMaximumSize(new Dimension(120, 80));
            retryButton.setFocusPainted(false);
            retryButton.setContentAreaFilled(false);
            retryButton.setOpaque(false);
            
            // 호버 효과
            retryButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    retryButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    retryButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });
            
            // 버튼 클릭 이벤트
            retryButton.addActionListener(e -> {
                onRetry();
                countdownTimer.stop();
            });
            
            add(countdownLabel, new GridBagConstraints(
                0, 0, // gridx, gridy
                1, 1, // gridwidth, gridheight
                1.0, 0.0, // weightx, weighty
                GridBagConstraints.CENTER, // anchor
                GridBagConstraints.NONE, // fill
                new Insets(8, 0, 4, 0), // insets
                0, 0 // ipadx, ipady
            ));
            
            add(retryButton, new GridBagConstraints(
                0, 1, // gridx, gridy
                1, 1, // gridwidth, gridheight
                1.0, 1.0, // weightx, weighty
                GridBagConstraints.CENTER, // anchor
                GridBagConstraints.VERTICAL, // fill (세로로만 늘어나게)
                new Insets(0, 0, 8, 0), // insets
                0, 0 // ipadx, ipady
            ));
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
        
        // 깜빡이는 상태일 때는 blinkVisible 상태에 따라 그리기
        if (isBlinking && !blinkVisible) return;
        
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
    
    /** ESC 문구 깜빡이는 애니메이션 시작 */
    public void startBlinkingAnimation() {
        isBlinking = true;
        blinkVisible = true;
        blinkCount = 0;
        alpha = 1f;
        int delay = 400; // 밀리초
        blinkTimer = new Timer(delay, e -> {
            blinkCount++;
            if (blinkVisible && blinkCount == 2) {
                blinkVisible = false;
                blinkCount = 0;
            } else if (!blinkVisible && blinkCount == 1) {
                blinkVisible = true;
                blinkCount = 0;
            }
            repaint();
        });
        blinkTimer.start();
    }
    
    /** 텍스트 필드 참조 반환 */
    public JTextField getNameField() {
        return nameField;
    }
    
    /** 카운트다운 시작 */
    public void startCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        countdown = 5;
        countdownLabel.setText(String.valueOf(countdown));
        
        countdownTimer = new Timer(1000, e -> {
            countdown--;
            if (countdown > 0) {
                countdownLabel.setText(String.valueOf(countdown));
            } else {
                countdownTimer.stop();
                goToScoreScene();
            }
        });
        countdownTimer.start();
    }
    
    /** ScoreScene으로 이동 */
    private void goToScoreScene() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        // GameOver 클래스의 goToScoreScene 메서드 호출을 위해 부모 컴포넌트를 통해 접근
        Container parent = this.getParent();
        while (parent != null && !(parent instanceof GameOver)) {
            parent = parent.getParent();
        }
        if (parent instanceof GameOver) {
            ((GameOver) parent).goToScoreSceneDirectly();
        }
    }

    void onRetry() {
        Container parent = this.getParent();
        while (parent != null && !(parent instanceof GameOver)) {
            parent = parent.getParent();
        }
        if (parent instanceof GameOver) {
            ((GameOver) parent).onRetry();
        }
    }


}
