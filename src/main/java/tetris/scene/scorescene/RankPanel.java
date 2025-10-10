package tetris.scene.scorescene;

import tetris.GameSettings;
import tetris.util.Animation;
import tetris.util.Theme;
import tetris.util.Theme.ColorType;
import tetris.util.HighScore;
import tetris.util.RunLater;
import tetris.util.Sound;

import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;



public class RankPanel extends JPanel {
    RankPanel(int highlightRank, String mode) {
        setOpaque(false);
        setLayout(new BorderLayout());
        
        this.highScore = new HighScore("./data/highscore_v2.txt");

        this.highlightRank = highlightRank;
        int[] screenSize = GameSettings.getInstance().getResolutionSize();
        int gap = (int)(screenSize[0] * 0.02f);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridLayout(1,4,gap,0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, gap*4, 10, gap*30));
        
        add(panel, BorderLayout.NORTH);


        final String[] modeList = {"easy", "normal", "hard", "item"};
        for(int i = 0; i < modeList.length; i++) {
            buttonList[i] = new ModeBtn(modeList[i], this::reload);
            panel.add(buttonList[i]);
        }

        rankTable = new RankTable(highScore.get(mode).size());
        add(rankTable, BorderLayout.CENTER);
        load(mode);

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
    boolean reloadOk = false;



    void startAnimations(float duration, Runnable nextAnimation) {
        final float btnAnimDuration = 0.2f;
        final float btnAnimTotalDuration = btnAnimDuration * buttonList.length;

        for(int i=0; i<buttonList.length; i++) {
            ModeBtn button = buttonList[i];
            new RunLater(btnAnimDuration * i, () -> button.popOut(0.6f, 0.6f, btnAnimDuration, 1.4f));
        }
        new RunLater(btnAnimTotalDuration, () -> {
            rankTable.startAnimations(duration, highlightRank);
            reloadOk = true;

        });
        new RunLater(duration + 0.3f, () -> {
            if(nextAnimation != null) {
                nextAnimation.run();
            }
        });
        

    }

    void load(String mode) {
        List<List<String>> rankData = highScore.get(mode);
        for(ModeBtn button: buttonList) {
            if(button.getText().equals(mode)) {
                button.setForeground(Theme.WHITE);
                button.setBackground(Theme.LIGHT_GRAY);
            } else {
                button.setForeground(Theme.GRAY);
                button.setBackground(Theme.DARK_GRAY);
            }
        }
        rankTable.reload(rankData);
    }

    void reload(String mode) {
        if(!reloadOk) return;
        load(mode);
        rankTable.startAnimations(0f, -1);
    }
}

class ModeBtn extends Animation {
    ActionListener eventListener = null;
    ModeBtn(String mode, Consumer<String> clickCallback) {
        super(mode, Theme.getFont(Theme.GIANTS_INLINE, 0.015f), 
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

    List<RunLater> runLaters = new ArrayList<>();

    int validRowCount = 0;
    Sound sound = null;

    RankTable(int validRowCount) {
        setOpaque(false);
        setLayout(new GridLayout(11,1,0,12));
//         setBorder(BorderFactory.createCompoundBorder(
//     BorderFactory.createEmptyBorder(0, 48, 0, 48),                     // 바깥 패딩
//     BorderFactory.createCompoundBorder(
//         BorderFactory.createLineBorder(Theme.Border(), 2, true),         // 선
//         BorderFactory.createEmptyBorder(18, 0, 18, 0)                  // 선과 내용 사이 내부 패딩
//     )
// ));
        //setBorder(BorderFactory.createEmptyBorder(12,48,24,48));


        
        
        sound = new Sound("gameboy-pluck-41265.mp3");

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

        if(sound != null) {
            sound.release();
            sound = null;
        }
    }

    void startAnimations(float duration, int highlightRank) {
        final float delay = duration / rankRowList.length;
        runLaters.forEach(e -> e.release());
        runLaters.clear();
        for(int i = 0; i < this.validRowCount + 1; i++) {
            final float startTime = delay * i;
            final RankRow row = rankRowList[i];
            runLaters.add(new RunLater(startTime, () -> row.popOut(0.6f, 0.6f, delay, 1.4f)));
             runLaters.add(new RunLater(startTime, () -> sound.play(false)));
        }
        if(highlightRank > 0 && highlightRank < rankRowList.length) {
             runLaters.add(new RunLater(duration, () -> rankRowList[highlightRank].hueBackground(3.5f, true)));
             runLaters.add(new RunLater(duration, () -> rankRowList[highlightRank].hueBorder(3.5f, true)));
            
        }
        if(validRowCount == 0) {
             runLaters.add(new RunLater(delay, () -> rankRowList[RANK_ROW_COUNT / 2 + 1].popOut(0.6f, 0.6f, delay, 1.4f)));
             runLaters.add(new RunLater(delay, () -> sound.play(false)));
        }
    }


    void reload(List<List<String>> rankTableData) {
        removeAll();
        this.validRowCount = rankTableData.size();
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
            rankRowList[RANK_ROW_COUNT / 2 + 1].setText("아직 기록이 없습니다. 첫 기록을 세워주세요!");
        }
        revalidate();
        repaint();
    }

}

class RankRow extends Animation {
    RankRow() {
        super(null, Theme.getFont(Theme.GIANTS_REGULAR, 0.012f), 
            Theme.LIGHT_GRAY, Theme.BG(), Theme.BG(), 
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


        Font font = row == 0 ? 
            Theme.getFont(Theme.GIANTS_INLINE, 0.015f) : 
            Theme.getFont(Theme.GIANTS_INLINE, 0.012f);

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
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, Theme.getPixelWidth(0.05f)));
        }

        setFont(font);
        setForeground(color);
        setHorizontalAlignment(align);
        setText(text);
    }
}
