package tetris.scene.battle;

import javax.swing.*;
import java.awt.*;

/**
 * P2P 네트워크 상태를 표시하는 UI 컴포넌트
 * 
 * 기능:
 * - 5단계 Ping 바 표시 (0-100ms: 5개, 101-150ms: 4개, 151-200ms: 3개, 201-300ms: 2개, 300ms+: 1개)
 * - 지연시간 숫자 표시 (밀리초 단위)
 * - 색상 코드: 초록(정상), 노랑(주의), 주황(경고), 빨강(위험), 회색(심각)
 */
public class NetworkStatusDisplay extends JPanel {
    
    private long currentLatency = 0; // 현재 지연시간 (ms)
    
    // Ping 레벨 정의
    private enum PingLevel {
        EXCELLENT(0, 100, Color.GREEN, 5),      // 0-100ms: 초록, 5개 바
        GOOD(101, 150, Color.YELLOW, 4),        // 101-150ms: 노랑, 4개 바
        FAIR(151, 200, Color.ORANGE, 3),        // 151-200ms: 주황, 3개 바
        POOR(201, 300, Color.RED, 2),           // 201-300ms: 빨강, 2개 바
        CRITICAL(301, Long.MAX_VALUE, Color.GRAY, 1); // 300ms+: 회색, 1개 바
        
        final long minLatency;
        final long maxLatency;
        final Color color;
        final int bars;
        
        PingLevel(long min, long max, Color color, int bars) {
            this.minLatency = min;
            this.maxLatency = max;
            this.color = color;
            this.bars = bars;
        }
        
        static PingLevel fromLatency(long latency) {
            for (PingLevel level : values()) {
                if (latency >= level.minLatency && latency <= level.maxLatency) {
                    return level;
                }
            }
            return CRITICAL;
        }
    }
    
    public NetworkStatusDisplay() {
        setOpaque(false); // 투명 배경
        setPreferredSize(new Dimension(250, 60));
    }
    
    /**
     * 지연시간 업데이트
     * @param latencyMs 지연시간 (밀리초)
     */
    public void updateLatency(long latencyMs) {
        this.currentLatency = latencyMs;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Ping 아이콘 및 바 그리기
        drawPingIcon(g2d, 10, 10);
        
        // 지연시간 텍스트 그리기
        PingLevel level = PingLevel.fromLatency(currentLatency);
        g2d.setColor(level.color);
        g2d.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        String latencyText = currentLatency + "ms";
        g2d.drawString(latencyText, 80, 30);
        
        // 상태 메시지 표시
        String statusText;
        if (currentLatency <= 100) {
            statusText = "정상";
        } else if (currentLatency <= 150) {
            statusText = "주의";
        } else if (currentLatency <= 200) {
            statusText = "경고";
        } else if (currentLatency <= 300) {
            statusText = "랙 발생!";
        } else {
            statusText = "연결 불안정";
        }
        
        g2d.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        g2d.drawString(statusText, 150, 30);
    }
    
    /**
     * Ping 아이콘 (5개 바 형태) 그리기
     */
    private void drawPingIcon(Graphics2D g2d, int x, int y) {
        PingLevel level = PingLevel.fromLatency(currentLatency);
        
        int barWidth = 8;
        int barGap = 4;
        int baseHeight = 10;
        
        for (int i = 0; i < 5; i++) {
            int barHeight = baseHeight + (i * 8);
            int barX = x + (i * (barWidth + barGap));
            int barY = y + (40 - barHeight);
            
            if (i < level.bars) {
                // 활성 바: 레벨 색상
                g2d.setColor(level.color);
                g2d.fillRect(barX, barY, barWidth, barHeight);
            } else {
                // 비활성 바: 회색 테두리만
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawRect(barX, barY, barWidth, barHeight);
            }
        }
    }
}
