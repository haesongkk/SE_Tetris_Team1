package tetris.scene.scorescene;

import java.awt.*;
import javax.swing.*;

import tetris.util.Animation;
import tetris.util.Theme;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;


public class SSBody extends JPanel {
    private List<Animation> rowList = new ArrayList<>();
    private Timer animTimer;
    private int animIndex = 0;
    private int highlightRank = -1;

    private final String[] rankLabelContent = new String[] {
        "RANK", "NAME", "SCORE", "LINES", "TIME", "DIFFICULTY"
    };
    private final int[] rankLabelAlign = new int[] {
        SwingConstants.LEFT, SwingConstants.CENTER,
        SwingConstants.RIGHT, SwingConstants.CENTER,
        SwingConstants.CENTER, SwingConstants.CENTER
    };
    private final Color[] rowColors = new Color[] {
        Theme.STAND_BLUE, Theme.LIGHT_GREY, 
        Theme.LIGHT_GREY, Theme.S_GREEN, 
        Theme.T_PURPLE, Theme.L_ORANGE
    };
    
    SSBody() {
        this(-1);
    }
    
    SSBody(int highlightRank) {
        this.highlightRank = highlightRank;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


        Animation rankLabelRow = new Animation(
            null,
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f),
            Theme.HEADER_RED, Theme.BG, Theme.BG,
            1, 0,
            SwingConstants.LEFT, SwingConstants.CENTER
        );
        rankLabelRow.setLayout(new GridLayout(1,6,12,0));
        rankLabelRow.setAlignmentX(CENTER_ALIGNMENT);
        

        for (int i = 0; i < rankLabelContent.length; i++) {
            JLabel rankLabel = new JLabel(rankLabelContent[i]);
            rankLabel.setFont(Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f));
            rankLabel.setForeground(Theme.HEADER_RED);
            rankLabel.setAlignmentX(rankLabelAlign[i]);
            rankLabelRow.add(rankLabel);
            rankLabel.setPreferredSize(new Dimension(120, 40));
        }

        add(rankLabelRow);
        rowList.add(rankLabelRow);
        add(Box.createRigidArea(new Dimension(0, 12)));

        // ===== 데이터 로드/파싱/정렬 =====
        List<String> lines = loadFile("./resources/highscore.txt");
        for (String line : lines) {
            if (line == null) continue;
            String[] parts = line.split(",", -1);
            Animation animation = new Animation(
                null, 
                Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f), 
                Theme.HEADER_RED, Theme.BG, Theme.BG, 
                1, 20, 
                SwingConstants.LEFT, SwingConstants.CENTER
            );
            animation.setLayout(new GridLayout(1,6,12,0));
            animation.setAlignmentX(CENTER_ALIGNMENT);
            animation.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            for (int i = 0; i < parts.length; i++) {
                JLabel label = new JLabel(parts[i]);
                label.setFont(Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 14f));
                label.setForeground(rowColors[i]);
                label.setAlignmentX(rankLabelAlign[i]);
                label.setPreferredSize(new Dimension(120, 40));
                animation.add(label);
            }
            add(animation);
            rowList.add(animation);
        }
    }

    public void startAnimation() {
        animTimer = new Timer(100, e -> {
            if(animIndex >= rowList.size()) {
                ((Timer)e.getSource()).stop();
                return;
            }
            rowList.get(animIndex++).popOut(0.4f, 0.4f, 0.3f, 1.7f);
        });
        animTimer.start();

        if(highlightRank > 0) {
            Animation targetRow = rowList.get(highlightRank);
            Timer t = new Timer(1700, e -> {
                targetRow.hueBackground(3.5f, true);
                targetRow.hueBorder(3.f, true);
                ((Timer)e.getSource()).stop();
            });
            t.start();
        }
    }

    JLabel createJLabel(String text, Font font, Color color, int align) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(align);
        return label;
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

}
