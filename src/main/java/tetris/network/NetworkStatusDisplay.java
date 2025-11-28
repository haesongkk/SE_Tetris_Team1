package tetris.network;

import tetris.util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * P2P 네트워크 상태를 표시하는 UI 컴포넌트
 * - Ping 상태 아이콘 (5단계)
 * - 네트워크 지연/연결 끊김 경고 메시지
 */
public class NetworkStatusDisplay extends JPanel {
    
    // Ping 상태 레벨 (0: 매우 좋음 ~ 4: 매우 나쁨)
    public enum PingLevel {
        EXCELLENT(0, "매우 좋음", new Color(34, 197, 94)),      // 0-50ms
        GOOD(1, "좋음", new Color(132, 204, 22)),                // 51-100ms
        FAIR(2, "보통", new Color(234, 179, 8)),                 // 101-150ms
        POOR(3, "나쁨", new Color(249, 115, 22)),                // 151-200ms
        VERY_POOR(4, "매우 나쁨", new Color(239, 68, 68));       // 201ms+
        
        private final int level;
        private final String description;
        private final Color color;
        
        PingLevel(int level, String description, Color color) {
            this.level = level;
            this.description = description;
            this.color = color;
        }
        
        public int getLevel() { return level; }
        public String getDescription() { return description; }
        public Color getColor() { return color; }
        
        public static PingLevel fromLatency(long latencyMs) {
            if (latencyMs <= 50) return EXCELLENT;
            else if (latencyMs <= 100) return GOOD;
            else if (latencyMs <= 150) return FAIR;
            else if (latencyMs <= 200) return POOR;
            else return VERY_POOR;
        }
    }
    
    private PingLevel currentPingLevel = PingLevel.EXCELLENT;
    private long currentLatency = 0;
    private String statusMessage = "";
    private boolean showWarning = false;
    
    private JPanel pingIconPanel;
    private JLabel statusLabel;
    private JLabel latencyLabel;
    
    public NetworkStatusDisplay() {
        setLayout(new BorderLayout(10, 5));
        setOpaque(false);
        
        // Ping 아이콘 패널 (신호 막대 형태)
        pingIconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPingIcon((Graphics2D) g);
            }
        };
        pingIconPanel.setOpaque(false);
        pingIconPanel.setPreferredSize(new Dimension(60, 30));
        
        // 상태 텍스트 패널
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        latencyLabel = new JLabel("Ping: 0ms");
        latencyLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        latencyLabel.setForeground(Color.WHITE);
        
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(255, 255, 255, 200));
        
        textPanel.add(latencyLabel);
        textPanel.add(statusLabel);
        
        add(pingIconPanel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
    }
    
    /**
     * Ping 상태 아이콘을 그립니다 (신호 막대 5개)
     */
    private void drawPingIcon(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int barWidth = 8;
        int barSpacing = 3;
        int maxHeight = 30;
        
        Color activeColor = currentPingLevel.getColor();
        Color inactiveColor = new Color(100, 100, 100, 100);
        
        // 5개의 막대를 그림 (레벨에 따라 활성화된 막대 수 변경)
        int activeBars = 5 - currentPingLevel.getLevel(); // EXCELLENT=5개, VERY_POOR=1개
        
        for (int i = 0; i < 5; i++) {
            int barHeight = (int) (maxHeight * (i + 1) / 5.0);
            int x = i * (barWidth + barSpacing);
            int y = maxHeight - barHeight;
            
            g2d.setColor(i < activeBars ? activeColor : inactiveColor);
            g2d.fillRoundRect(x, y, barWidth, barHeight, 3, 3);
            
            // 테두리
            if (i < activeBars) {
                g2d.setColor(activeColor.darker());
                g2d.drawRoundRect(x, y, barWidth, barHeight, 3, 3);
            }
        }
    }
    
    /**
     * 네트워크 지연 상태를 업데이트합니다
     * @param latencyMs 지연 시간 (밀리초)
     */
    public void updateLatency(long latencyMs) {
        this.currentLatency = latencyMs;
        this.currentPingLevel = PingLevel.fromLatency(latencyMs);
        
        latencyLabel.setText("Ping: " + latencyMs + "ms");
        latencyLabel.setForeground(currentPingLevel.getColor());
        
        // 200ms 초과 시 경고 표시
        if (latencyMs > 200) {
            showWarning = true;
            statusLabel.setText("⚠ 전송 지연 발생!");
            statusLabel.setForeground(new Color(239, 68, 68));
        } else {
            showWarning = false;
            statusLabel.setText(currentPingLevel.getDescription());
            statusLabel.setForeground(new Color(255, 255, 255, 200));
        }
        
        repaint();
    }
    
    /**
     * 커스텀 상태 메시지를 표시합니다
     */
    public void setStatusMessage(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
        repaint();
    }
    
    /**
     * 연결 중 상태를 표시합니다
     */
    public void showConnecting() {
        statusLabel.setText("연결 중...");
        statusLabel.setForeground(new Color(234, 179, 8));
        repaint();
    }
    
    /**
     * 연결됨 상태를 표시합니다
     */
    public void showConnected() {
        statusLabel.setText("연결됨");
        statusLabel.setForeground(new Color(34, 197, 94));
        repaint();
    }
    
    /**
     * 연결 끊김 상태를 표시합니다
     */
    public void showDisconnected() {
        statusLabel.setText("⚠ 연결 끊김!");
        statusLabel.setForeground(new Color(239, 68, 68));
        currentPingLevel = PingLevel.VERY_POOR;
        repaint();
    }
    
    public long getCurrentLatency() {
        return currentLatency;
    }
    
    public PingLevel getCurrentPingLevel() {
        return currentPingLevel;
    }
    
    public boolean isShowingWarning() {
        return showWarning;
    }
}
