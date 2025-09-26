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
    
    ScoreBody() {
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
                return;
            }
            rowList.get(animIndex++).startAnimation();
        });
        animTimer.start();
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
    @Override Color[] getColors() {
        return new Color[] {
            Theme.STAND_BLUE, Theme.LIGHT_GREY, 
            Theme.LIGHT_GREY, Theme.S_GREEN, 
            Theme.T_PURPLE, Theme.L_ORANGE
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
        return new int[] { 14,14,14,14,14,14 };
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
}
