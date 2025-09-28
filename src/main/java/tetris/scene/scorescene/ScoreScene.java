package tetris.scene.scorescene;
import tetris.Game;
import tetris.scene.Scene;
import tetris.scene.test.TestScene;
import tetris.util.Animation;
import tetris.util.Theme;

import javax.swing.*;
import java.awt.*;

public class ScoreScene extends Scene {
    
    public ScoreScene(JFrame frame) {
        this(frame, -1);
    }
    
    public ScoreScene(JFrame frame, int highlightRank) {
        super(null);
        
        setBackground(Theme.BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(CENTER_ALIGNMENT);


        setBorder(BorderFactory.createEmptyBorder(12,24,12,24));

        add(Box.createRigidArea(new Dimension(0, 12)));

        Animation highscore = new Animation(
            "HIGH SCORE", 
            Theme.GIANTS_INLINE.deriveFont(Font.ITALIC, 85), 
            Theme.TITLE_YELLOW, Theme.BG, Theme.BG, 
            0, 0, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );
        highscore.setAlignmentX(CENTER_ALIGNMENT);
        highscore.setBorder(BorderFactory.createEmptyBorder(0,100,0,100));
        add(highscore);

        add(Box.createRigidArea(new Dimension(0, 12)));


        ScoreBody scoreBody = new ScoreBody(highlightRank);
        scoreBody.setAlignmentX(CENTER_ALIGNMENT);

        add(scoreBody);

        add(Box.createRigidArea(new Dimension(0, 12)));


        Animation exitLabel = new Animation(
            "P R E S S    E S C    T O    E X I T", 
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 45), 
            Theme.TEXT_GRAY, Theme.BG, Theme.BG, 
            0, 0, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );
        //exitLabel.setBorder(BorderFactory.createEmptyBorder(0,24,0,24));
        exitLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(exitLabel);

        // ScoreFooter footer = new ScoreFooter();
        // footer.setAlignmentX(CENTER_ALIGNMENT);
        // add(footer);

        add(Box.createRigidArea(new Dimension(0, 12)));

        Timer t1 = new Timer(0, e -> {
            highscore.popIn(0.8f, 0.8f, 0.3f, 2.f);
            add(Box.createRigidArea(new Dimension(0, 12)));

            revalidate(); repaint();
            ((Timer)e.getSource()).stop();
        });
        t1.setInitialDelay(0);
        t1.start();


        Timer t2 = new Timer(0, e -> {
            scoreBody.startAnimation();
            revalidate(); repaint();
            ((Timer)e.getSource()).stop();
        });
        t2.setInitialDelay(450);
        t2.start();


        Timer t3 = new Timer(0, e -> {
            //footer.startAnimation();
            exitLabel.blink(0.8f, 0.4f);
            revalidate(); repaint();
            ((Timer)e.getSource()).stop();
        });
        t3.setInitialDelay(2400);
        t3.start();


        // !!!!!! TestScene 말고 시작 메뉴 씬으로 변경 !!!!!!!!
        frame.getRootPane().registerKeyboardAction(
                e -> Game.setScene(new TestScene(frame)),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        frame.setContentPane(this);
        frame.revalidate();
        frame.repaint();
    }

    @Override public void onEnter() {}
    @Override public void onExit() {}
}
