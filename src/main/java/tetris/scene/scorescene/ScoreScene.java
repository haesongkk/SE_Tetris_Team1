package tetris.scene.scorescene;
import tetris.scene.Scene;

import tetris.Game;
import tetris.scene.menu.MainMenuScene;
import tetris.util.Animation;
import tetris.util.Theme;

import javax.swing.*;
import java.awt.*;

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
        this(frame, -1, "normal");
    }


    void startAnimations() {
        Animation.runLater(0, () -> titleLabel.popIn(0.8f, 0.8f, 0.3f, 2.f));
        Animation.runLater(0.3f, () -> rankPanel.startAnimations(2.5f));
        rankPanel.setNextAnimation(() -> Animation.runLater(0.5f, () -> exitLabel.blink(0.8f, 0.4f)));
    }



    Animation titleLabel;
    Animation exitLabel;
    RankPanel rankPanel;



    @Override public void onEnter() {
        startAnimations();
    }
    @Override public void onExit() {
        titleLabel.release();
        exitLabel.release();
        rankPanel.release();
        titleLabel = null;
        exitLabel = null;
        rankPanel = null;
    }
}

