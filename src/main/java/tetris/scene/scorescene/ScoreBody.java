package tetris.scene.scorescene;

import java.awt.*;
import javax.swing.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;


public class ScoreBody extends JPanel {
    private List<Row> rowList = new ArrayList<>();
    private Timer animTimer;
    private int animIndex = 0;
    private int highlightRank = -1;
    
    ScoreBody() {
        this(-1);
    }
    
    ScoreBody(int highlightRank) {
        this.highlightRank = highlightRank;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // ===== 헤더 행 (칼럼 제목) =====
       Row headerRow = new HeaderRow(new String[] {
           "RANK", "NAME", "SCORE", "LINES", "TIME", "DIFFICULTY"
       });
       //headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
       rowList.add(headerRow);
       add(headerRow);
       add(Box.createRigidArea(new Dimension(0, 12)));

        // ===== 데이터 로드/파싱/정렬 =====
        List<String> lines = loadFile("./resources/highscore.txt");
        for (String line : lines) {
            if (line == null) continue;
            String[] parts = line.split(",", -1);
            BodyRow comp = new BodyRow(parts);
            add(comp);
            rowList.add(comp);
        }
        
    }

    public void startAnimation() {
        animTimer = new Timer(110, e -> {
            if(animIndex >= rowList.size()) {
                ((Timer)e.getSource()).stop();
                // 모든 행 애니메이션 완료 후 하이스코어 강조 애니메이션 시작
                if (highlightRank > 0) {
                    startHighScoreHighlightAnimation();
                }
                return;
            }
            rowList.get(animIndex++).startAnimation();
        });
        animTimer.start();
    }
    
    /** 하이스코어 강조 애니메이션 시작 */
    private void startHighScoreHighlightAnimation() {
        // 해당 순위에 해당하는 행을 찾아서 강조
        if (highlightRank > 0 && highlightRank < rowList.size()) {
            Row targetRow = rowList.get(highlightRank); // highlightRank는 1부터 시작, rowList는 0부터 시작
            if (targetRow instanceof BodyRow) {
                ((BodyRow) targetRow).startHighScoreAnimation();
            }
        }
    }

    

    List<String> loadFile(String path) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    // JLabel createJLabel(Color color, Font font, int size, int style, int align, String text) {

    // }


}



abstract class Row extends JPanel {
    abstract Color[] getColors();
    abstract Font[] getFonts();
    abstract int[] getSizes();
    abstract int[] getStyle();
    int[] getAligns() {
        return new int[] {
            SwingConstants.CENTER,   SwingConstants.CENTER,
            SwingConstants.RIGHT,   SwingConstants.CENTER,
            SwingConstants.CENTER, SwingConstants.CENTER
        };
    }
    float[] getRatios(){
        return new float[] { 1,2,2,1,1,2 };
    } 

    
    

    float alpha = 0f;
    float scale = 0.8f;
    long startTime = -1L;
    Timer animTimer;
    final long durationNanos = 320_000_000L;
    int delay = 16;
     
   Row(String[] content) {
        setOpaque(false);
        setLayout(new GridLayout(1,6,12,0));


        for (int i = 0; i < content.length; i++) {
            JLabel lb = new JLabel(content[i], getAligns()[i]);
            lb.setFont(getFonts()[i].deriveFont(getStyle()[i], getSizes()[i]));
            lb.setForeground(getColors()[i]);

            if( i == 2 ) lb.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));

            add(lb);
        }
    }


    void startAnimation() {
        animTimer = new Timer(delay, e -> {
            long now = System.nanoTime();
            if (startTime < 0) startTime = now;
            float t = Math.min(1f, (now - startTime) / (float) durationNanos);

            float tp = t - 1f, s = 1.8f;
            float backOut = (tp*tp*((s+1f)*tp + s) + 1f);
            scale = 0.45f + 0.55f * backOut;
            //scale = 0.8f + 0.2f * backOut;
            alpha = (float) Math.pow(t, 0.6);
            repaint();
            if (t >= 1f) {
                ((Timer) e.getSource()).stop();
            }
        });
        animTimer.start();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.translate(w * 0.5, h * 0.5);
        g2.scale(scale, scale);
        g2.translate(-w * 0.5, -h * 0.5);
        super.paint(g2);
        g2.dispose();

    }
}

class HeaderRow extends Row {
    @Override Color[] getColors() {
        return new Color[] {
            Theme.HEADER_RED, Theme.HEADER_RED, 
            Theme.HEADER_RED, Theme.HEADER_RED, 
            Theme.HEADER_RED, Theme.HEADER_RED
        };
    }
    @Override Font[] getFonts() {
        return new Font[] {
            Theme.GIANTS_INLINE, Theme.GIANTS_INLINE,
            Theme.GIANTS_INLINE, Theme.GIANTS_INLINE, 
            Theme.GIANTS_INLINE, Theme.GIANTS_INLINE
        };
    }
    @Override int[] getSizes() {
        return new int[] { 20,20,20,20,20,20 };
    }
    @Override int[] getStyle() {
        return new int[] {
            Font.BOLD, Font.BOLD,
            Font.BOLD, Font.BOLD,
            Font.BOLD, Font.BOLD
        };
    }
    
    HeaderRow(String[] content) {
        super(content);
    }
}

class BodyRow extends Row {
    // 하이스코어 강조 애니메이션 상태
    private boolean isHighScoreHighlight = false;
    private Timer highlightTimer;
    private float pulseScale = 1.0f;
    private float hue = 0f;
    private float glowAlpha = 0f;
    
    @Override Color[] getColors() {
        if (isHighScoreHighlight) {
            // 하이스코어 강조 시 색상순환 색상 사용
            Color highlightColor = Color.getHSBColor(hue, 0.8f, 1.0f);
            return new Color[] {
                highlightColor, highlightColor,
                highlightColor, highlightColor,
                highlightColor, highlightColor
            };
        } else {
            return new Color[] {
                Theme.STAND_BLUE, Theme.LIGHT_GREY, 
                Theme.LIGHT_GREY, Theme.S_GREEN, 
                Theme.T_PURPLE, Theme.L_ORANGE
            };
        }
    }
    @Override Font[] getFonts() {
        return new Font[] {
            Theme.GIANTS_INLINE, Theme.GIANTS_INLINE,
            Theme.GIANTS_INLINE, Theme.GIANTS_INLINE, 
            Theme.GIANTS_INLINE, Theme.GIANTS_INLINE
        };
    }
    @Override int[] getSizes() {
        if (isHighScoreHighlight) {
            // 펄스 효과를 위한 크기 조정
            int baseSize = 14;
            int pulseSize = (int)(baseSize * pulseScale);
            return new int[] { pulseSize, pulseSize, pulseSize, pulseSize, pulseSize, pulseSize };
        } else {
            return new int[] { 14,14,14,14,14,14 };
        }
    }
    @Override int[] getStyle() {
        return new int[] {
            Font.BOLD, Font.BOLD,
            Font.BOLD, Font.BOLD,
            Font.BOLD, Font.BOLD
        };
    }
    
    BodyRow(String[] content) {
        super(content);
    }
    
    /** 하이스코어 강조 애니메이션 시작 */
    public void startHighScoreAnimation() {
        isHighScoreHighlight = true;
        
        // 펄스 + 색상순환 + 글로우 애니메이션
        highlightTimer = new Timer(50, e -> {
            // 펄스 효과 (1.0 ~ 1.1 사이에서 부드럽게 변화)
            pulseScale = 1.0f + (float)(Math.sin(System.currentTimeMillis() * 0.005) * 0.1);
            
            // 색상순환 효과
            hue += 0.02f;
            if (hue > 1f) hue -= 1f;
            
            // 글로우 효과
            glowAlpha = (float)(Math.sin(System.currentTimeMillis() * 0.003) * 0.3 + 0.3);
            
            repaint();
        });
        highlightTimer.start();
    }
    
    /** 하이스코어 강조 애니메이션 정지 */
    private void stopHighScoreAnimation() {
        isHighScoreHighlight = false;
        if (highlightTimer != null) {
            highlightTimer.stop();
        }
        pulseScale = 1.0f;
        hue = 0f;
        glowAlpha = 0f;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if (isHighScoreHighlight) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 글로우 효과 (배경 하이라이트)
            g2.setComposite(AlphaComposite.SrcOver.derive(glowAlpha));
            g2.setColor(Color.getHSBColor(hue, 0.3f, 1.0f));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            
            // 색상순환 테두리
            g2.setComposite(AlphaComposite.SrcOver.derive(0.8f));
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(Color.getHSBColor(hue, 0.8f, 1.0f));
            g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
            
            g2.dispose();
        }
        
        super.paintComponent(g);
    }
}
