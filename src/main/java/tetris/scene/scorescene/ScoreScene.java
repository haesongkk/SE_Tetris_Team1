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

public class ScoreScene extends Scene {
    public ScoreScene(JFrame frame, int highlightRank, String mode) {
        super(frame);
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        // 제목 라벨
        titleLabel = new Animation(
            "HIGH SCORE", 
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 85), 
            Theme.TITLE_YELLOW, Theme.BG, Theme.BG, 
            0, 0, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );
        add(titleLabel, BorderLayout.NORTH);
        
        // 랭크 패널
        rankPanel = new RankPanel(highlightRank, mode);
        add(rankPanel, BorderLayout.CENTER);

        // 종료 라벨
        exitLabel = new Animation(
            "P R E S S    E S C    T O    E X I T", 
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 45), 
            Theme.TEXT_GRAY, Theme.BG, Theme.BG, 
            0, 0, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );
        add(exitLabel, BorderLayout.SOUTH);


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
        this(frame, -1, "Normal");
    }


    void startAnimations() {
        runLater(0, () -> titleLabel.popIn(0.8f, 0.8f, 0.3f, 2.f));
        runLater(0.3f, () -> rankPanel.startAnimations(2.5f));
        rankPanel.setNextAnimation(() -> runLater(0.5f, () -> exitLabel.blink(0.8f, 0.4f)));
    }


    void runLater(float delay, Runnable r) {
        final int delayMs = (int)(delay * 1000);
        Timer t = new Timer(delayMs, e -> { ((Timer)e.getSource()).stop(); r.run(); });
        t.setRepeats(false);
        animationTimers.add(t); 
        t.start();
    }

    Animation titleLabel;
    Animation exitLabel;
    RankPanel rankPanel;


    List<Timer> animationTimers = new ArrayList<>();

    @Override public void onEnter() {}
    @Override public void onExit() {
        for(Timer t: animationTimers) {
            t.stop();
        }
        animationTimers.clear();
        animationTimers = null;
        titleLabel = null;
        exitLabel = null;
        rankPanel = null;
    }
}

