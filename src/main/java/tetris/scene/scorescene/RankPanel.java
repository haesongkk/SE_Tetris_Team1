package tetris.scene.scorescene;

import tetris.GameSettings;
import tetris.util.Animation;
import tetris.util.Theme;
import tetris.util.HighScore;

import java.util.List;
import java.awt.*;
import javax.swing.*;

public class RankPanel extends JPanel {
    RankPanel(int highlightRank, String mode) {
        HighScore highScore = new HighScore("./data/highscore_v2.txt");

        List<List<String>> rankData = highScore.get(mode);

        this.highlightRank = rankData.size() >= highlightRank ? highlightRank : -1;

        setOpaque(false);
        if(rankData.size() == 0) {
            noDataLabel = new Animation(
                "NO DATA",
                Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 30f),
                Theme.I_CYAN, Theme.BG, Theme.BG,
                1, 15,
                SwingConstants.CENTER, SwingConstants.CENTER
            );
            setLayout(new GridLayout(1,1));
            add(noDataLabel);
        } else {
            // 데이터 수와 무관하게 일정한 갯수의 행을 추가한다
            for(int i = 0; i < RANK_ROW_COUNT; i++) {
                rankRowList[i] = new RankRow();
                add(rankRowList[i]);
                for(int j = 0; j < COL_COUNT; j++) {
                    rankItemList[i][j] = new RankItem(i, j);
                    rankRowList[i].add(rankItemList[i][j]);
                }
            }
            for(int i = 0; i < rankData.size(); i++) {
                List<String> items = rankData.get(i);
                for(int j = 0; j < items.size(); j++) {
                    rankItemList[i+1][j].setText(items.get(j));
                }
            }
            setLayout(new GridLayout(11,1,0,12));
            setBorder(BorderFactory.createEmptyBorder(24,48,24,48));
        }
    }


    void setNextAnimation(Runnable nextAnimation) {
        this.nextAnimation = nextAnimation;
    }

    void startAnimations(float duration) {
        final float delay = duration / rankRowList.length;

        if(noDataLabel != null) {
            noDataLabel.popOut(0.6f, 0.6f, 0.3f, 1.4f);
            if(nextAnimation != null) {
                nextAnimation.run();
            }
            return;
        }

        animTimer = new Timer((int)(delay * 1000), e -> {
            rankRowList[animIndex++].popOut(0.6f, 0.6f, delay, 1.4f);
            if(animIndex == rankRowList.length) {
                animTimer.stop();
                if(nextAnimation != null) {
                    nextAnimation.run();
                }
                if(highlightRank > 0) {
                    rankRowList[highlightRank].hueBackground(3.5f, true);
                    rankRowList[highlightRank].hueBorder(3.5f, true);
                }
            }
        });
        animTimer.start();
    }


    final int RANK_ROW_COUNT = 11;
    final int COL_COUNT = 5;
    Runnable nextAnimation;
    int highlightRank = -1;
    Animation noDataLabel = null;
    RankRow[] rankRowList = new RankRow[RANK_ROW_COUNT];
    RankItem[][] rankItemList = new RankItem[RANK_ROW_COUNT][COL_COUNT];
    Timer animTimer;
    int animIndex = 0;

    void release() {
        animTimer.stop();
        animTimer = null;
        for(RankRow rankRow: rankRowList) {
            rankRow.release();
        }
    }
}

class RankRow extends Animation {
    RankRow() {
        super(null, Theme.GIANTS_INLINE, 
            Theme.BG, Theme.BG, Theme.BG, 
            1, 15, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );
        setLayout(new GridLayout(1,6,0,12));
    }
}

class RankItem extends JLabel {
    RankItem(int row, int col) {

        int[] screenSize = GameSettings.getInstance().getResolutionSize();

        float bigFontSize = screenSize[0] * 0.015f;
        float smallFontSize = screenSize[0] * 0.012f;

        Font font = row == 0 ? 
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, bigFontSize) : 
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, smallFontSize);

        Color color = row == 0 ? Theme.HEADER_RED : 
            col == 0 ? Theme.STAND_BLUE : 
            col == 1 ? Theme.LIGHT_GREY : 
            col == 2 ? Theme.L_ORANGE : 
            col == 3 ? Theme.T_PURPLE : 
            col == 4 ? Theme.S_GREEN : 
            Theme.LIGHT_GREY;

        int align = 
            col == 0 ? SwingConstants.CENTER : 
            col == 1 ? SwingConstants.CENTER : 
            col == 2 ? SwingConstants.RIGHT : 
            col == 3 ? SwingConstants.CENTER : 
            col == 4 ? SwingConstants.CENTER : 
            SwingConstants.CENTER;

        String text = row == 0 ? (
            col == 0 ? "RANK" : 
            col == 1 ? "NAME" : 
            col == 2 ? "SCORE" : 
            col == 3 ? "LINES" : 
            col == 4 ? "TIME" : 
            null ) : null;

        if(col == 2 ) {
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, (int)smallFontSize * 4));
        }

        setFont(font);
        setForeground(color);
        setHorizontalAlignment(align);
        setText(text);
    }

}
