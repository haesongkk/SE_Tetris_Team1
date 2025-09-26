package tetris.scene.scorescene;
import tetris.scene.Scene;

import javax.swing.*;
import java.awt.*;

public class ScoreScene extends Scene {
    
    public ScoreScene(JFrame frame) {
        super(null);
        
        setBackground(Theme.BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(CENTER_ALIGNMENT);


        setBorder(BorderFactory.createEmptyBorder(12,24,12,24));

        add(Box.createRigidArea(new Dimension(0, 12)));
        ScoreHeader scoreHeader = new ScoreHeader();
        scoreHeader.setAlignmentX(CENTER_ALIGNMENT);
        add(scoreHeader);

        add(Box.createRigidArea(new Dimension(0, 12)));

        ScoreBody scoreBody = new ScoreBody();
        scoreBody.setAlignmentX(CENTER_ALIGNMENT);

        add(scoreBody);

        add(Box.createRigidArea(new Dimension(0, 12)));

        ScoreFooter footer = new ScoreFooter();
        footer.setAlignmentX(CENTER_ALIGNMENT);

        add(footer);

        add(Box.createRigidArea(new Dimension(0, 12)));

        Timer t1 = new Timer(0, e -> {
            scoreHeader.startAnimation();
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
            footer.startAnimation();
            revalidate(); repaint();
            ((Timer)e.getSource()).stop();
        });
        t3.setInitialDelay(2400);
        t3.start();


        // ESC → 종료(원하시면 Scene 전환 로직으로 바꾸세요)
        frame.getRootPane().registerKeyboardAction(
                e -> System.exit(0),
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
