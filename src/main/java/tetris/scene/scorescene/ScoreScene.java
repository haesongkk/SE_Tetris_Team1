package tetris.scene.scorescene;
import tetris.scene.Scene;

import tetris.Game;
import tetris.scene.menu.MainMenuScene;
import tetris.util.Animation;
import tetris.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ScoreScene extends Scene {
    public ScoreScene(JFrame frame, int highlightRank) {
        super(frame);
        setBackground(Theme.BG);

        rankData = loadFile("./resources/highscore.txt");
        userRank = highlightRank;

        createComponents();
        layoutComponents();
        startAnimations();

        frame.getRootPane().registerKeyboardAction(
                e -> Game.setScene(new MainMenuScene(frame)),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        frame.setContentPane(this);
        frame.revalidate();
        //frame.repaint();
    }

    public ScoreScene(JFrame frame) {
        this(frame, -1);
    }

    void createComponents() {
        titleLabel = new Animation(
            "HIGH SCORE", 
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 85), 
            Theme.TITLE_YELLOW, Theme.BG, Theme.BG, 
            0, 0, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );

        rankPanel = new JPanel();
        rankPanel.setOpaque(false);

        // 랭크 헤더
        rankRowList.add(new Animation(
                null, Theme.GIANTS_INLINE, 
                Theme.BG, Theme.BG, Theme.BG, 
                1, 0, 
                SwingConstants.CENTER, SwingConstants.CENTER
        ));

        // 랭크 헤더 라벨들
        List<JLabel> headerRow = new ArrayList<>();
        for(int i = 0; i < rankContent.length; i++) {
            headerRow.add(createJLabel(
                rankContent[i], 
                Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f), 
                Theme.HEADER_RED, 
                rankAlignH[i]
            ));
        }
        rankLabelList.add(headerRow);


        for(String line: rankData) {
            String[] parts = line.split(",", -1);

            // 랭크 행
            rankRowList.add(new Animation(
                null, Theme.GIANTS_INLINE, 
                Theme.BG, Theme.BG, Theme.BG, 
                1, 15, 
                SwingConstants.CENTER, SwingConstants.CENTER
            ));

            // 랭크 행 라벨들
            List<JLabel> rankLabelRow = new ArrayList<>();
            for(int i = 0; i < parts.length; i++) {
                rankLabelRow.add(createJLabel(
                    parts[i], 
                    Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 14f), 
                    rankColor[i], 
                    rankAlignH[i]
                ));
            }
            rankLabelList.add(rankLabelRow);
        }

        exitLabel = new Animation(
            "P R E S S    E S C    T O    E X I T", 
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 45), 
            Theme.TEXT_GRAY, Theme.BG, Theme.BG, 
            0, 0, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );
    }

    void layoutComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        add(titleLabel, BorderLayout.NORTH);

        rankPanel.setLayout(new GridLayout(11,1,0,12));
        rankPanel.setBorder(BorderFactory.createEmptyBorder(24,48,24,48));
        for(int i = 0; i < rankRowList.size(); i++) {
            rankRowList.get(i).setLayout(new GridLayout(1,6,0,12));
            for(JLabel rankLabel: rankLabelList.get(i)) {
                rankRowList.get(i).add(rankLabel);
            }
            rankPanel.add(rankRowList.get(i));
        }
        add(rankPanel, BorderLayout.CENTER);

        add(exitLabel, BorderLayout.SOUTH);

    }

    void startAnimations() {
        runLater(0, () -> titleLabel.popIn(0.8f, 0.8f, 0.3f, 2.f));
        for(Animation rankRow: rankRowList) {
            runLater(rankRowList.indexOf(rankRow) * 150 + 500, () -> rankRow.popOut(0.6f, 0.6f, 0.25f, 1.5f));
        }
        if(userRank > 0) {
            runLater(2400, () -> rankRowList.get(userRank).hueBackground(3.5f, true));
            runLater(2400, () -> rankRowList.get(userRank).hueBorder(3.5f, true));
        }
        runLater(3000, () -> exitLabel.blink(0.8f, 0.4f));
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

    JLabel createJLabel(String text, Font font, Color color, int align) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        //label.setAlignmentX(align);
        label.setHorizontalAlignment(align);
        return label;
    }

    void runLater(int delayMs, Runnable r) {
        Timer t = new Timer(delayMs, e -> { ((Timer)e.getSource()).stop(); r.run(); });
        t.setRepeats(false);
        animationTimers.add(t); 
        t.start();
    }

    Animation titleLabel;
    Animation exitLabel;
    JPanel rankPanel;
    List<Animation> rankRowList = new ArrayList<>();
    List<List<JLabel>> rankLabelList = new ArrayList<>();


    List<Timer> animationTimers = new ArrayList<>();
    List<String> rankData = new ArrayList<>();
    int userRank = -1;

    final String[] rankContent = {
        "RANK", "NAME", "SCORE", 
        "LINES", "TIME", "DIFFICULTY"
    };
    final int[] rankAlignH = {
        SwingConstants.CENTER, SwingConstants.CENTER,
        SwingConstants.RIGHT, SwingConstants.CENTER,
        SwingConstants.CENTER, SwingConstants.CENTER
    };
    final Color[] rankColor = {
        Theme.STAND_BLUE, Theme.LIGHT_GREY, 
        Theme.LIGHT_GREY, Theme.S_GREEN, 
        Theme.T_PURPLE, Theme.L_ORANGE
    };

    @Override public void onEnter() {}
    @Override public void onExit() {
        for(Timer t: animationTimers) {
            t.stop();
        }
        animationTimers.clear();
        rankRowList.clear();
        rankLabelList.clear();
        rankData.clear();
        userRank = -1;
        titleLabel = null;
        exitLabel = null;
        rankPanel = null;
    }
}
