package tetris.scene.scorescene;
import tetris.scene.Scene;

import tetris.Game;
import tetris.scene.menu.MainMenuScene;
import tetris.util.*;
import tetris.util.Theme.ColorType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ScoreScene extends Scene {
    final int ROW_COUNT = 11;
    final int COL_COUNT = 5;

    final float HEADER_ANIM_DURATION = 0.4f;
    final float BUTTON_ANIM_DURATION = 0.2f;
    final float ROW_ANIM_DURATION = 0.2f;
    final float FOOTER_ANIM_BLINK_ON_DURATION = 0.6f;
    final float FOOTER_ANIM_BLINK_OFF_DURATION = 0.4f;

    final String HEADER_TEXT = "HIGH SCORE";
    final String FOOTER_TEXT = "P R E S S    E S C    T O    E X I T";
    final String[] BUTTON_TEXT = {"EASY", "NORMAL", "HARD", "ITEM"};
    final String[] TABLE_HEADER_TEXTS = {"RANK", "NAME", "SCORE", "LINES", "TIME"};

    final String BGM_FILE = "8-bit-game-music-122259.mp3";
    final String EFFECT_FILE = "gameboy-pluck-41265.mp3";
    final String TABLE_FILE = "./data/highscore_v2.txt";

    final String NO_RECORD_TEXT = "아직 기록이 없습니다. 첫 기록을 세워주세요!";

    JFrame frame;
    String mode;
    int highlightRank;

    Sound bgm;
    Sound effect;
    
    HighScore highScore;

    EscapeHandler escHandler;

    boolean bAnimationEnd = false;
    
    JPanel bodyPanel;
    JPanel buttonPanel;
    JPanel tablePanel;
    
    Animation headerAnim;
    Animation footerAnim;
    Animation[] buttonAnims = new Animation[BUTTON_TEXT.length];
    Animation[] rowAnims = new Animation[ROW_COUNT];
    
    JLabel headerLabel;
    JButton[] buttons = new JButton[BUTTON_TEXT.length];
    JLabel[][] cells = new JLabel[ROW_COUNT][COL_COUNT];
    JLabel footerLabel;
    JLabel noDataMessage;
    
    
    RunLater headerRun;
    RunLater footerRun;
    RunLater[] buttonRuns = new RunLater[BUTTON_TEXT.length];
    RunLater[] rowRuns = new RunLater[ROW_COUNT];

    Animation highlightedRow;
    


    public ScoreScene(JFrame frame) {
        this(frame, -1, "EASY");
    }

    public ScoreScene(JFrame frame, int highlightRank, String mode) {
        super(frame);
        this.frame = frame;
        if(highlightRank < 0) this.highlightRank = -1;
        else if(highlightRank > 10) this.highlightRank = -1;
        else this.highlightRank = highlightRank;
        this.mode = mode;

        this.highScore = new HighScore(TABLE_FILE);

        this.bgm = new Sound(BGM_FILE);
        this.effect = new Sound(EFFECT_FILE);

        this.escHandler = new EscapeHandler(this::onExitEvent);

        this.bodyPanel = new JPanel();
        this.buttonPanel = new JPanel();
        this.tablePanel = new JPanel();

        this.headerLabel = new JLabel();
        for(int i = 0; i < this.BUTTON_TEXT.length; i++) {
            this.buttons[i] = new JButton();
        }
        for(int i = 0; i < ROW_COUNT; i++)
             for(int j = 0; j < COL_COUNT; j++)
                 this.cells[i][j] = new JLabel();
        this.footerLabel = new JLabel();

        this.noDataMessage = new JLabel();
        
        this.headerAnim = new Animation();
        this.footerAnim = new Animation();
        for(int i = 0; i < ROW_COUNT; i++) {
            this.rowAnims[i] = new Animation();
        }
        for(int i = 0; i < BUTTON_TEXT.length; i++) {
            this.buttonAnims[i] = new Animation();
        }

        if(this.highlightRank > 0) {
            this.highlightedRow = this.rowAnims[this.highlightRank];
        } else {
            this.highlightedRow = null;
        }

        

        for(int i = 0; i < BUTTON_TEXT.length; i++) {
            final JButton target = this.buttons[i];
            this.buttonRuns[i] = new RunLater(
                i * 0.4f, () -> target.setVisible(true));
        }

        setLayout();
        setBackground();
        setText();
        setFont();
        setColor();
        setAlign();
        setBorder();
        setButtonEvents();
        this.onButtonClick(this.mode);


        setAnimRuns(false);
        this.bgm.play(true);


        this.frame.setContentPane(this);
        super.revalidate();



    }

    @Override
    public void onEnter() {

    }
    @Override
    public void onExit() { }

    void release() {
        if(this.escHandler != null) {
            this.escHandler.release();
            this.escHandler = null;
        }

        if(this.bgm != null) {
            this.bgm.release();
            this.bgm = null;
        }
        if(this.effect != null) {
            this.effect.release();
            this.effect = null;
        }

        Sound.clear();
        RunLater.clear();
        Animation.clear();

        for(JButton btn : this.buttons)
            for (ActionListener al : btn.getActionListeners()) 
                btn.removeActionListener(al);
        
    }


    void setLayout() {
        setLayout(new BorderLayout());

        add(this.headerAnim, BorderLayout.NORTH);
        add(this.bodyPanel, BorderLayout.CENTER);
        add(this.footerAnim, BorderLayout.SOUTH);

        this.headerAnim.add(this.headerLabel);

        this.bodyPanel.setLayout(new BorderLayout());
        this.bodyPanel.add(this.buttonPanel, BorderLayout.NORTH);
        this.bodyPanel.add(this.tablePanel, BorderLayout.CENTER);

        this.buttonPanel.setLayout(new GridLayout(1,4));
        for(int i = 0; i < this.buttons.length; i++) {
            this.buttonPanel.add(this.buttonAnims[i]);
            this.buttonAnims[i].setLayout(new GridLayout(1, 1));
            this.buttonAnims[i].add(this.buttons[i]);
        }

        this.tablePanel.setLayout(new GridLayout(11,1));
        for(Animation row : this.rowAnims)
            this.tablePanel.add(row);

        for(Animation row : this.rowAnims)
            row.setLayout(new GridLayout(1,5));

        for(int i = 0; i < cells.length; i++)
            for(int j = 0; j < cells[i].length; j++)
                this.rowAnims[i].add(this.cells[i][j]);

        this.footerAnim.add(this.footerLabel);

        int centerRow = ROW_COUNT / 2 + 1;
        this.rowAnims[centerRow].removeAll();
        this.rowAnims[centerRow].setLayout(new CardLayout());


        JPanel gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        gridPanel.setLayout(new GridLayout(1,COL_COUNT));
        for(int j = 0; j < COL_COUNT; j++)
            gridPanel.add(this.cells[centerRow][j]);

        JPanel messagePanel = new JPanel();
        messagePanel.setOpaque(false);
        messagePanel.setLayout(new GridLayout(1,1));
        messagePanel.add(this.noDataMessage);

        this.rowAnims[centerRow].add(gridPanel, "GRID");
        this.rowAnims[centerRow].add(messagePanel, "MESSAGE");
            


    }

    void setBackground() {
        setBackground(Theme.BG());

        this.bodyPanel.setBackground(Theme.BG());
        this.buttonPanel.setBackground(Theme.BG());
        this.tablePanel.setBackground(Theme.BG());

        this.headerLabel.setBackground(Theme.BG());
        for(JButton btn : this.buttons) btn.setBackground(Theme.BG());
        for(JLabel[] cs : this.cells)
            for(JLabel c : cs) c.setBackground(Theme.BG());
        this.footerLabel.setBackground(Theme.BG());
        
        this.headerAnim.setBackground(Theme.BG());
        this.footerAnim.setBackground(Theme.BG());
        for(Animation a: rowAnims) a.setBackground(Theme.BG());
        for(Animation a: buttonAnims) a.setBackground(Theme.BG());
    }



    void setText() {
        this.headerLabel.setText(HEADER_TEXT);
        this.footerLabel.setText(FOOTER_TEXT);
        this.noDataMessage.setText(NO_RECORD_TEXT);
        for(int i = 0; i < BUTTON_TEXT.length; i++) {
            this.buttons[i].setText(BUTTON_TEXT[i]);
        }
        for(int i = 0; i < COL_COUNT; i++) {
            this.cells[0][i].setText(TABLE_HEADER_TEXTS[i]);
        }

        for(int i = 1; i < ROW_COUNT; i++) 
            for(int j = 0; j < COL_COUNT; j++) 
                this.cells[i][j].setText("");
            
        
        List<List<String>> scores = highScore.get(mode);
        for(int i = 0; i < scores.size(); i++) {
            List<String> items = scores.get(i);
            for(int j = 0; j < items.size(); j++) {
                this.cells[i+1][j].setText(items.get(j));
            }
        }
        CardLayout cl = (CardLayout)(this.rowAnims[ROW_COUNT / 2 + 1].getLayout());
        if(scores.size() == 0) {
            cl.show(this.rowAnims[ROW_COUNT / 2 + 1], "MESSAGE");
        } else cl.show(this.rowAnims[ROW_COUNT / 2 + 1], "GRID");

    }

    void setFont() {
        this.headerLabel.setFont(Theme.GIANTS_INLINE(7.0f));
        this.footerLabel.setFont(Theme.GIANTS_INLINE(4f));
        this.noDataMessage.setFont(Theme.GIANTS_INLINE(1.5f));
        for(int i = 0; i < BUTTON_TEXT.length; i++) {
            this.buttons[i].setFont(Theme.GIANTS_INLINE(1.5f));
        }
        for(int i = 0; i < COL_COUNT; i++) {
            this.cells[0][i].setFont(Theme.GIANTS_INLINE(1.5f));
        }

        for(int i = 1; i < ROW_COUNT; i++) {
            for(int j = 0; j < COL_COUNT; j++) {
                this.cells[i][j].setFont(Theme.GIANTS_INLINE(1.2f));
            }
        }
    }

    void setColor() {
        this.headerLabel.setForeground(Theme.Block(ColorType.YELLOW));
        this.footerLabel.setForeground(Theme.GRAY);
        this.noDataMessage.setForeground(Theme.LIGHT_GRAY);
        for(int i = 0; i < BUTTON_TEXT.length; i++) {
            this.buttons[i].setForeground(Theme.GRAY);
        }
        for(int i = 0; i < COL_COUNT; i++) {
            this.cells[0][i].setForeground(Theme.Block(ColorType.RED));
        }
        for(int i = 1; i< ROW_COUNT; i++) {
            this.cells[i][0].setForeground(Theme.Block(ColorType.CYAN));
            this.cells[i][1].setForeground(Theme.LIGHT_GRAY);
            this.cells[i][2].setForeground(Theme.Block(ColorType.ORANGE));
            this.cells[i][3].setForeground(Theme.Block(ColorType.PURPLE));
            this.cells[i][4].setForeground(Theme.Block(ColorType.GREEN));
        }
    }

    void setAlign() {
        this.headerLabel.setAlignmentX(CENTER_ALIGNMENT);
        this.footerLabel.setAlignmentX(CENTER_ALIGNMENT);
        this.noDataMessage.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i = 0; i < BUTTON_TEXT.length; i++) {
            this.buttons[i].setAlignmentX(CENTER_ALIGNMENT);
        }
        for(int i = 0; i < ROW_COUNT; i++)
            for(int j = 0; j < COL_COUNT; j++)
                this.cells[i][j].setHorizontalAlignment(SwingConstants.CENTER);

    }

    void setBorder() {

        this.headerAnim.setBorderColor(Theme.BG());
        this.footerAnim.setBorderColor(Theme.BG());
        for(Animation a: rowAnims) a.setBorderColor(Theme.BG());
        for(Animation a: buttonAnims) a.setBorderColor(Theme.BG());

        // 버튼 패널 좌우 여백
        int padding = Theme.getPixelWidth(0.25f);
        this.buttonPanel.setBorder(BorderFactory.createEmptyBorder(
            0,padding,0,padding
        ));

        // 버튼 패딩
        int buttonPadding = Theme.getPixelWidth(0.01f);
        for(Animation btnAnim: this.buttonAnims) {
            btnAnim.setBorder(BorderFactory.createEmptyBorder(
                0, buttonPadding, 0, buttonPadding
            ));
        }

        // 랭크 패널 상하좌우 여백
        int tablePaddingV = Theme.getPixelWidth(0.02f);
        int tablePaddingH = Theme.getPixelWidth(0.1f);
        this.tablePanel.setBorder(BorderFactory.createEmptyBorder(
            tablePaddingV, tablePaddingH, tablePaddingV, tablePaddingH
        ));

        // 전체 상하 여백
        int marginTB = Theme.getPixelWidth(0.01f);
        this.setBorder(BorderFactory.createEmptyBorder(
            marginTB, 0, marginTB, 0
        ));

        // 행 테두리 두께 및 반지름 설정
        for(Animation rowAnim: this.rowAnims) {
            int thickness = Theme.getPixelWidth(0.001f);
            int radius = Theme.getPixelWidth(0.005f);
            rowAnim.setBorderThickness(thickness);
            rowAnim.setBorderRadius(radius);
        }
    }

    void setButtonEvents() {
        for(int i = 0; i < BUTTON_TEXT.length; i++) {
            final String targetMode = BUTTON_TEXT[i];
            this.buttons[i].setContentAreaFilled(false);
            this.buttons[i].setFocusPainted(false);
            this.buttons[i].addActionListener(e -> onButtonClick(targetMode));
        }

    }

    void setAnimRuns(boolean bReload) {
        if(!bReload) {
            this.headerAnim.setVisible(false);
            this.headerRun = new RunLater(
                0, () -> {
                    headerAnim.popIn(HEADER_ANIM_DURATION);
                }
            );
            
            for(int i = 0; i < BUTTON_TEXT.length; i++) {
                final Animation target = this.buttonAnims[i];
                target.setVisible(false);
                this.buttonRuns[i] = new RunLater(
                    i * BUTTON_ANIM_DURATION + HEADER_ANIM_DURATION,
                    () -> target.popOut(BUTTON_ANIM_DURATION)
                );
            }
        }

        for(int i = 0; i < ROW_COUNT; i++) {
            if(rowRuns[i] != null){
                rowRuns[i].release();
                rowRuns[i] = null;
            }
        }
        if(highlightedRow != null) highlightedRow.stop();

        float rowAnimStartAt = bReload ? 0f :
            HEADER_ANIM_DURATION + BUTTON_TEXT.length * BUTTON_ANIM_DURATION;
        int validRowCount = highScore.get(mode).size();
        for(int i = 0; i < validRowCount + 1; i++) {
            final Animation target = this.rowAnims[i];
            target.setVisible(false);
            this.rowRuns[i] = new RunLater(
                i * ROW_ANIM_DURATION + rowAnimStartAt, () -> {
                    target.popOut(ROW_ANIM_DURATION);
                    this.effect.play(false);
                }
            );
        }
        if(validRowCount == 0) {
            final Animation target = this.rowAnims[ROW_COUNT / 2 + 1];
            target.setVisible(false);
            this.rowRuns[ROW_COUNT / 2 + 1] = new RunLater(
                ROW_ANIM_DURATION + rowAnimStartAt, () -> {
                    target.popOut(ROW_ANIM_DURATION);
                    this.effect.play(false);
                }
            );
        }
        if(!bReload) {
            float footerAnimStartAt = rowAnimStartAt + (validRowCount+1) * ROW_ANIM_DURATION;
            this.footerAnim.setVisible(false);
            this.footerRun = new RunLater(
                footerAnimStartAt, () -> {
                    footerAnim.blink(
                        FOOTER_ANIM_BLINK_ON_DURATION,
                        FOOTER_ANIM_BLINK_OFF_DURATION
                    );
                    this.bAnimationEnd = true;
                    if(this.highlightRank > validRowCount) return;
                    if(this.highlightedRow == null) return;
                    this.highlightedRow.hueBackground(3.5f, true);
                    this.highlightedRow.hueBorder(3.5f, true);

                }
            );
        
        } 
    }


    void onButtonClick(String _mode) {
        this.mode = _mode;
        
        int thick = Theme.getPixelWidth(0.002f);
        for(JButton b: this.buttons) {
            if(b.getText().toLowerCase().equals(_mode.toLowerCase())) {
                b.setForeground(Theme.WHITE);
                b.setBorder(BorderFactory.createLineBorder(
                    Theme.WHITE, thick, true
                ));
            } else {
                b.setForeground(Theme.GRAY);
                b.setBorder(BorderFactory.createLineBorder(
                    Theme.GRAY, thick, true
                ));
            }
        }
        
        setText();
        setBackground();
        setBorder(); // 테두리가 안 지워져서
        setAnimRuns(true);
    }

    void onExitEvent() {
        if(bAnimationEnd == false) {
            RunLater.runNowAll();
            bAnimationEnd = true;
        }
        else {
            release();
            Game.setScene(new MainMenuScene(frame));
        }
    }
}


