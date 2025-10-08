package tetris.scene.scorescene;

import tetris.GameSettings;
import tetris.util.Animation;
import tetris.util.Theme;
import tetris.util.Theme.ColorType;
import tetris.util.HighScore;

import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;

import java.util.List;
import java.util.function.Consumer;



public class RankPanel extends JPanel {
    RankPanel(int highlightRank, String mode) {
        setOpaque(false);
        setLayout(new BorderLayout());
        
        this.highScore = new HighScore("./data/highscore_v2.txt");

        this.highlightRank = highlightRank;
        int[] screenSize = GameSettings.getInstance().getResolutionSize();
        int gap = (int)(screenSize[0] * 0.05f);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridLayout(1,4,gap,0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, gap, 0, gap));
        add(panel, BorderLayout.NORTH);


        final String[] modeList = {"normal", "hard", "easy", "item"};
        for(int i = 0; i < modeList.length; i++) {
            buttonList[i] = new ModeBtn(modeList[i], this::reload);
            panel.add(buttonList[i]);
        }

        rankTable = new RankTable();
        add(rankTable, BorderLayout.CENTER);
        reload(modeList[0]);

    }

    void release() {
        if(rankTable != null) {
            rankTable.release();
            rankTable = null;
        }
        if(highScore != null) {
            highScore.release();
            highScore = null;
        }
        for(ModeBtn button: buttonList) {
            button.release();
            button = null;
        }
        buttonList = null;
        removeAll();
    }

    
    int highlightRank = -1;
    float popOutDuration = 0.3f;

    HighScore highScore = null;
    RankTable rankTable = null;
    ModeBtn[] buttonList = new ModeBtn[4];



    void startAnimations(float duration, Runnable nextAnimation) {

        for(ModeBtn button: buttonList) {
            button.popOut(0.6f, 0.6f, 0.3f, 1.4f);
        }
        rankTable.startAnimations(duration, highlightRank);
        Animation.runLater(duration, () -> {
            if(nextAnimation != null) {
                nextAnimation.run();
            }
        });
    }

    void reload(String mode) {
        List<List<String>> rankData = highScore.get(mode);
        rankTable.reload(rankData);
        rankTable.startAnimations(0f, -1);
    }
}

class ModeBtn extends Animation {
    ActionListener eventListener = null;
    ModeBtn(String mode, Consumer<String> clickCallback) {
        super(mode, Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20), 
            Theme.WHITE, Theme.GRAY, Theme.DARK_GRAY, 1, 15, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


        eventListener = e -> clickCallback.accept(mode);
        addActionListener(eventListener);
    }


    @Override
    public void release() {
        super.release();
        removeActionListener(eventListener);
        eventListener = null;
    }
}

class RankTable extends JPanel {
    final int RANK_ROW_COUNT = 11;
    final int COL_COUNT = 5;
    RankRow[] rankRowList = new RankRow[RANK_ROW_COUNT];
    RankItem[][] rankItemList = new RankItem[RANK_ROW_COUNT][COL_COUNT];

    RankTable() {
        setOpaque(false);
        setLayout(new GridLayout(11,1,0,12));
        setBorder(BorderFactory.createEmptyBorder(24,48,24,48));

    }

    void release() {
        if (rankRowList != null) {
            for (RankRow row : rankRowList) {
                if (row != null) {
                    row.release();
                }
            }
            rankRowList = null;
        }
        
        rankItemList = null;
    }

    void startAnimations(float duration, int highlightRank) {
        final float delay = duration / rankRowList.length;
        for(int i = 0; i < rankRowList.length; i++) {
            final float startTime = delay * i;
            final RankRow row = rankRowList[i];
            Animation.runLater(startTime, () -> row.popOut(0.6f, 0.6f, delay, 1.4f));
        }
        if(highlightRank > 0) {
            Animation.runLater(duration, () -> rankRowList[highlightRank].hueBackground(3.5f, true));
            Animation.runLater(duration, () -> rankRowList[highlightRank].hueBorder(3.5f, true));
        }
    }


    void reload(List<List<String>> rankTableData) {
        removeAll();
        for(int i = 0; i < RANK_ROW_COUNT; i++) {
            rankRowList[i] = new RankRow();
            add(rankRowList[i]);
            for(int j = 0; j < COL_COUNT; j++) {
                rankItemList[i][j] = new RankItem(i, j);
                rankRowList[i].add(rankItemList[i][j]);
            }
        }
        for(int i = 0; i < rankTableData.size(); i++) {
            List<String> items = rankTableData.get(i);
            for(int j = 0; j < items.size(); j++) {
                rankItemList[i+1][j].setText(items.get(j));
            }
        }
        if(rankTableData.size() == 0) {
            rankItemList[5][2].setNoData();
        }
        revalidate();
        repaint();
    }

}

class RankRow extends Animation {
    RankRow() {
        super(null, Theme.GIANTS_INLINE, 
            Theme.BG(), Theme.BG(), Theme.BG(), 
            1, 15, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );
        setLayout(new GridLayout(1,5,0,12));
    }

    @Override
    public void release() {
        super.release();
        removeAll();
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

        Color color = row == 0 ? Theme.Block(ColorType.RED) : 
            col == 0 ? Theme.Block(ColorType.CYAN) : 
            col == 1 ? Theme.Border() : 
            col == 2 ? Theme.Block(ColorType.ORANGE) : 
            col == 3 ? Theme.Block(ColorType.PURPLE) : 
            col == 4 ? Theme.Block(ColorType.GREEN) : 
            Theme.LIGHT_GRAY;

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

    void setNoData() {
        setText("NO DATA");
        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(getFont().deriveFont(Font.BOLD, getFont().getSize() * 1.5f));
        setForeground(Theme.GRAY);
    }

}
