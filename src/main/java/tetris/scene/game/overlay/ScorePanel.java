package tetris.scene.game.overlay;

import java.awt.*;
import javax.swing.*;

import tetris.util.Animation;
import tetris.util.RunLater;
import tetris.util.Sound;
import tetris.util.Theme;

public class ScorePanel extends JPanel {
    ScorePanel(String score, String lines, String time, String difficulty) {
        setOpaque(false);

        
        int width = Theme.getPixelWidth(0.05f);
        int height = Theme.getPixelHeight(0.03f);

        setLayout(new GridLayout(4,2,width/2,0));
        setBorder(BorderFactory.createEmptyBorder(height, width, height, width));

        
        final Font labelFont = Theme.getFont(Theme.GIANTS_INLINE, 0.015f);
        final Font valueFont = Theme.getFont(Theme.GIANTS_BOLD, 0.012f).deriveFont(3);


        final Font[] scoreItemFont = {
            labelFont, valueFont,
            labelFont, valueFont,
            labelFont, valueFont,
            labelFont, valueFont
        };
        
        final Color[] scoreItemColor = {
            Theme.Block('I'),   Theme.LIGHT_GRAY,
            Theme.Block('S'),   Theme.LIGHT_GRAY,
            Theme.Block('T'),   Theme.LIGHT_GRAY,
            Theme.Block('L'),   Theme.LIGHT_GRAY
        };

        final String[] scoreItemText = {
            "Score",       score,
            "Lines",       lines,
            "Time",        time,
            "Mode",        difficulty
        };

        for (int i = 0; i < 8; i++) {
            Animation anim = new Animation(
                scoreItemText[i], scoreItemFont[i],
                scoreItemColor[i], Theme.BG(), Theme.BG(),
                0, 0,
                SwingConstants.LEFT, SwingConstants.CENTER
            );
            scoreItemList[i] = anim;
            add(anim);
        }

        beep = new Sound("gameboy-pluck-41265.mp3");
    }

    void startAnimations(float duration) {
        final float delay = duration / scoreItemList.length;
        final int[] animOrder = {0, 2, 4, 6, 1, 3, 5, 7};

        animTimer = new Timer((int)(delay * 1000), e -> {
            new RunLater(0.1f, () -> beep.play(false));
            
            scoreItemList[animOrder[animIndex]].move(-50, 0, 0, 0, 1.5f, delay, false);
            animIndex++;

            if(animIndex == scoreItemList.length) {
                animTimer.stop();
            }
        });
        animTimer.start();
    }


    void release() {

        animTimer.stop();
        animTimer = null;
        for(Animation anim: scoreItemList) {
            anim.release();
        }
        scoreItemList = null;
        if(beep != null) {
            beep.release();
            beep = null;
        }
    }

    int animIndex = 0;

    Timer animTimer;
    Animation[] scoreItemList = new Animation[8];
    Sound beep = null;
}
