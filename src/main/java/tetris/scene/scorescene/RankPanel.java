package tetris.scene.scorescene;

import tetris.util.Loader;
import tetris.GameSettings;
import tetris.util.Animation;
import tetris.util.Theme;
import tetris.util.VerifyData;

import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import javax.swing.*;

public class RankPanel extends JPanel {
    RankPanel(int highlightRank, String mode) {

        List<String> rankData = Loader.loadFile("./data/highscore_v2.txt");

        if(!VerifyData.verifyHighScore(rankData)){
            System.out.println("rank data is invalid, so it is saved as 0");
            rankData = new ArrayList<String>();
            Loader.saveFile("./data/highscore_v2.txt", rankData);
        }  

        List<String> filtered = new ArrayList<String>();
        boolean tagFound = false;
        for(String line: rankData) {
            String trimmed = line.trim();
            if(trimmed.isEmpty()) continue;
            
            if(!tagFound) {
                if(trimmed.equals("#" + mode)) tagFound = true;
                continue;
            }
            if(trimmed.startsWith("#")) break;

            if(!trimmed.isEmpty()) filtered.add(line);

        }


        this.highlightRank = filtered.size() > highlightRank ? highlightRank : -1;

        setOpaque(false);
        if(filtered.size() == 0) {
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
            for(int i = 0; i < RANK_ROW_COUNT; i++) {
                rankRowList[i] = new RankRow();
                add(rankRowList[i]);
                for(int j = 0; j < COL_COUNT; j++) {
                    rankItemList[i][j] = new RankItem(i, j);
                    rankRowList[i].add(rankItemList[i][j]);
                }
            }
            for(int i = 0; i < filtered.size(); i++) {
                String[] parts = filtered.get(i).split(",", -1);
                if(parts.length > 6) {
                    System.out.println("Rank data is invalid, so it is skipped");
                    continue;
                }
                for(int j = 0; j < parts.length; j++) {
                    rankItemList[i+1][j].setText(parts[j]);
                }
            }
            setLayout(new GridLayout(11,1,0,12));
            setBorder(BorderFactory.createEmptyBorder(24,48,24,48));
        }
    }

    boolean verifyRankData(List<String> rankData) {
        if(rankData.size() > 10) {
            System.out.println("rank data size is greater than 10");
            return false;
        }

        for(String line: rankData) {
            String[] parts = line.split(",", -1);
            if(parts.length != 6) {
                System.out.println("line length is not 6");
                return false;
            }
            try {
                int rank = Integer.parseInt(parts[0].trim());
                if(rank < 1 || rank > 10) {
                    System.out.println("rank is not between 1 and 10");
                    return false;
                }
                
                int score = Integer.parseInt(parts[2].trim());
                if(score < 0) {
                    System.out.println("score is less than 0");
                    return false;
                }
                
                int lines = Integer.parseInt(parts[3].trim());
                if(lines < 0) {
                    System.out.println("lines is less than 0");
                    return false;
                }
                
                String[] time = parts[4].split(":", -1);
                if(time.length != 2) {
                    System.out.println("time is not in the format of mm:ss");
                    return false;
                }

                int min = Integer.parseInt(time[0].trim());
                int sec = Integer.parseInt(time[1].trim());
                if(min < 0 || sec < 0) {
                    System.out.println("min or sec is less than 0");
                    return false;
                }
                
            } catch (NumberFormatException e) {
                System.out.println("number format exception");
                return false;
            }
        }
        return true;
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
    final int COL_COUNT = 6;
    Runnable nextAnimation;
    int highlightRank = -1;
    Animation noDataLabel = null;
    RankRow[] rankRowList = new RankRow[RANK_ROW_COUNT];
    RankItem[][] rankItemList = new RankItem[RANK_ROW_COUNT][COL_COUNT];
    Timer animTimer;
    int animIndex = 0;
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
            col == 2 ? Theme.LIGHT_GREY : 
            col == 3 ? Theme.S_GREEN : 
            col == 4 ? Theme.T_PURPLE : 
            col == 5 ? Theme.L_ORANGE : 
            Theme.LIGHT_GREY;

        int align = 
            col == 0 ? SwingConstants.CENTER : 
            col == 1 ? SwingConstants.CENTER : 
            col == 2 ? SwingConstants.RIGHT : 
            col == 3 ? SwingConstants.CENTER : 
            col == 4 ? SwingConstants.CENTER : 
            col == 5 ? SwingConstants.CENTER : 
            SwingConstants.CENTER;

        String text = row == 0 ? (
            col == 0 ? "RANK" : 
            col == 1 ? "NAME" : 
            col == 2 ? "SCORE" : 
            col == 3 ? "LINES" : 
            col == 4 ? "TIME" : 
            col == 5 ? "DIFFICULTY" : 
            null ) : null;

        if(col == 2 ) {
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, (int)smallFontSize * 3));
        }

        setFont(font);
        setForeground(color);
        setHorizontalAlignment(align);
        setText(text);
    }

}
