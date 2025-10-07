package tetris.scene.game.overlay;

import java.awt.*;
import javax.swing.*;

import tetris.util.Animation;
import tetris.util.Theme;
import tetris.GameSettings;

public class ScorePanel extends JPanel {
    ScorePanel(String score, String lines, String time, String difficulty) {
        setOpaque(false);

        
        final int[] screenSize = GameSettings.getInstance().getResolutionSize();
        int width = (int)(screenSize[0] * 0.05f);
        int height = (int)(screenSize[1] * 0.03f);

        setLayout(new GridLayout(4,2,width/2,0));
        setBorder(BorderFactory.createEmptyBorder(height, width, height, width));

        
        final float labelSize = screenSize[0] * 0.015f;
        final float valueSize = screenSize[0] * 0.012f;
        final Font f = Theme.GIANTS_INLINE.deriveFont(Font.BOLD);


        final Font[] scoreItemFont = {
            f.deriveFont(labelSize), f.deriveFont(valueSize),
            f.deriveFont(labelSize), f.deriveFont(valueSize),
            f.deriveFont(labelSize), f.deriveFont(valueSize),
            f.deriveFont(labelSize), f.deriveFont(valueSize)
        };
        
        final Color[] scoreItemColor = {
            Theme.I_CYAN,   Theme.SCORE_WHITE,
            Theme.S_GREEN,  Theme.SCORE_WHITE,
            Theme.T_PURPLE, Theme.SCORE_WHITE,
            Theme.L_ORANGE, Theme.SCORE_WHITE
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
                scoreItemColor[i], Theme.BG, Theme.BG,
                0, 0,
                SwingConstants.LEFT, SwingConstants.CENTER
            );
            scoreItemList[i] = anim;
            add(anim);
        }
    }

    void startAnimations(float duration) {
        final float delay = duration / scoreItemList.length;
        final int[] animOrder = {0, 2, 4, 6, 1, 3, 5, 7};

        animTimer = new Timer((int)(delay * 1000), e -> {
            scoreItemList[animOrder[animIndex]].move(-50, 0, 0, 0, 1.5f, delay, false);
            animIndex++;
            if(animIndex == scoreItemList.length) {
                animTimer.stop();
            }
        });
        animTimer.start();
    }


    void free() {
        animTimer.stop();
        animTimer = null;
        for(Animation anim: scoreItemList) {
            //anim.deleteAnimTimer(anim.addAnimTimer());
        }
        scoreItemList = null;
    }

    int animIndex = 0;
    Timer animTimer;

    Animation[] scoreItemList = new Animation[8];

}
