package tetris.scene.game.overlay;
 
import javax.swing.*;
import javax.swing.text.AbstractDocument;

import tetris.Game;
import tetris.GameSettings;
import tetris.scene.game.GameScene;
import tetris.scene.scorescene.ScoreScene;
import tetris.util.Animation;
import tetris.util.EscapeHandler;
import tetris.util.HighScore;
import tetris.util.NoCommaFilter;
import tetris.util.RunLater;
import tetris.util.Sound;
import tetris.util.Theme;
import tetris.util.Theme.ColorType;

import java.awt.*;
import java.awt.event.*;


public class GameOver extends JPanel {
    final int ITEM_COUNT= 4;
    final int RETRY_COUNTDOWN = 5;
    
    final float TOP_ANIM_DURATION = 0.3f;
    final float ITEM_ANIM_DURATION = 0.2f;
    final float BOTTOM_ANIM_DURATION = 0.4f;

    final float[] CANVAS_RATIO = { 0.3f, 0.6f };

    final int SCORE;
    final int LINES;
    final int TIME;
    final String DIFFICULTY;
    final int RANK;

    final String[] ITEM_KEY_TEXT = {
        "Score", "Lines", "Time", "Mode"
    };

    final String BGM_FILE = "arcade-beat-323176.mp3";
    final String EFFECT_FILE = "gameboy-pluck-41265.mp3";
    final String TABLE_FILE = "./data/highscore_v2.txt";

    JFrame frame;
    Container mainContainer;
    
    Animation topContainer;
    JLabel titleLabel;

    JPanel centerContainer;
    Animation[] itemKeyContainers;
    JLabel[] itemKeyLabels;
    Animation[] itemValueContainers;
    JLabel[] itemValueLabels;

    Animation bottomContainer;
    JLabel assistLabel;
    JTextField nameField;
    JButton retryButton;

    RunLater start;
    RunLater[] itemOrder = new RunLater[4];
    RunLater[] itemValueOrder = new RunLater[4];
    RunLater end;
    RunLater[] countdown = new RunLater[RETRY_COUNTDOWN];

    boolean bRunEnd = false;

    Sound bgm = new Sound(BGM_FILE);
    Sound effect = new Sound(EFFECT_FILE);
    
    HighScore highScore = new HighScore(TABLE_FILE);

    EscapeHandler escHandler;
    KeyEventDispatcher enterDispatcher;

    public GameOver(JFrame frame, int score, int lines, int time, String difficulty){
        this.SCORE = score;
        this.LINES = lines;
        this.TIME = time;
        this.DIFFICULTY = difficulty.toLowerCase();

        this.RANK = this.highScore.add(DIFFICULTY, SCORE, LINES, TIME);
        
        this.frame = frame;

        this.escHandler = new EscapeHandler(this::onExitEvent);

        this.bgm.play(true);

        this.frame.getRootPane().setGlassPane(this);
        this.setVisible(true);

        mainContainer = new Container();
    
        topContainer = new Animation();
        titleLabel = new JLabel();

        centerContainer = new JPanel();
        itemKeyContainers = new Animation[]{
            new Animation(), new Animation(), new Animation(), new Animation()
        };
        itemKeyLabels = new JLabel[]{
        new JLabel(), new JLabel(), new JLabel(), new JLabel()
        };
        itemValueContainers = new Animation[]{
            new Animation(), new Animation(), new Animation(), new Animation()
        };
        itemValueLabels = new JLabel[]{
            new JLabel(), new JLabel(), new JLabel(), new JLabel()
        };

        bottomContainer = new Animation();
        assistLabel = new JLabel();
        nameField = new JTextField();
        retryButton = new JButton();

        setLayouts();
        setBackColors();
        setTexts();
        setFonts();
        setFrontColors();
        setAligns();
        setBorders();
        setButton();
        setField();
        
        // Theme 클래스에 현재 프레임 참조 설정 (폰트 크기 동적 계산용)
        if (frame != null) {
            tetris.util.Theme.setCurrentFrame(frame);
        }

        if(RANK >= 10) {
            enterDispatcher = new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent e) {
                    if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ENTER){
                        onButtonClick();
                        return true;
                    }
                    return false;
                }
            };
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(enterDispatcher);
        } else enterDispatcher = null;
        
        
        run();
    }

    void setLayouts() {
        setLayout(new GridLayout(1, 1));
        add(mainContainer);

        mainContainer.setLayout(new BorderLayout());
        mainContainer.add(topContainer, BorderLayout.NORTH);
        mainContainer.add(centerContainer, BorderLayout.CENTER);
        mainContainer.add(bottomContainer, BorderLayout.SOUTH);

        topContainer.setLayout(new GridLayout(1, 1));
        topContainer.add(titleLabel);

        centerContainer.setLayout(new GridLayout(ITEM_COUNT, 2));
        for(int i = 0; i < ITEM_COUNT; i++) {
            centerContainer.add(itemKeyContainers[i]);
            itemKeyContainers[i].setLayout(new GridLayout(1, 1));
            itemKeyContainers[i].add(itemKeyLabels[i]);

            centerContainer.add(itemValueContainers[i]);
            itemValueContainers[i].setLayout(new GridLayout(1, 1));
            itemValueContainers[i].add(itemValueLabels[i]);
        }

        bottomContainer.setLayout(new GridLayout(2, 1));
        bottomContainer.add(assistLabel);
        bottomContainer.add(RANK < 10 ? nameField : retryButton);

    }

    void setBackColors() {
        setOpaque(false);
        mainContainer.setBackground(Theme.BG());
        topContainer.setBackground(Theme.BG());
        centerContainer.setBackground(Theme.BG());
        bottomContainer.setBackground(Theme.BG());


        for(int i = 0; i < ITEM_COUNT; i++) {
            itemKeyContainers[i].setBackground(Theme.BG());
            itemValueContainers[i].setBackground(Theme.BG());
            itemKeyLabels[i].setBackground(Theme.BG());
            itemKeyLabels[i].setOpaque(true);
            itemValueLabels[i].setOpaque(true);
            itemValueLabels[i].setBackground(Theme.BG());
        }

    }

    void setTexts() {
        titleLabel.setText("GAME OVER!!");
        for(int i = 0; i < ITEM_COUNT; i++) {
            itemKeyLabels[i].setText(ITEM_KEY_TEXT[i]);
        }

        int m = TIME / 60;
        int s = TIME % 60;
        final String timeS = String.format("%02d:%02d", m, s);

        itemValueLabels[0].setText(Integer.toString(SCORE));
        itemValueLabels[1].setText(Integer.toString(LINES));
        itemValueLabels[2].setText(timeS);
        itemValueLabels[3].setText(DIFFICULTY);
        if(RANK < 10) {
            assistLabel.setText("ENTER YOUR NAME:");

        } else {
            assistLabel.setText(Integer.toString(RETRY_COUNTDOWN));
            retryButton.setText("RETRY?");

        }
    }

    void setFonts() {
        titleLabel.setFont(Theme.GIANTS_INLINE(2.4f));

        for(int i = 0; i < ITEM_COUNT; i++) {
            itemKeyLabels[i].setFont(Theme.GIANTS_INLINE(1.5f));
            itemValueLabels[i].setFont(Theme.GIANTS_BOLD(1.2f));
        }

        if(RANK < 10) {
            assistLabel.setFont(Theme.GIANTS_REGULAR(1.2f));
            nameField.setFont(Theme.GIANTS_REGULAR(1.6f));
            
        } else {
            assistLabel.setFont(Theme.GIANTS_REGULAR(1.6f));
            retryButton.setFont(Theme.GIANTS_BOLD(1.2f));
        }
    }

    void setFrontColors() {

        titleLabel.setForeground(Theme.Block('Z'));

        for(int i = 0; i < ITEM_COUNT; i++) {
            itemValueLabels[i].setForeground(Theme.LIGHT_GRAY);
        }
        itemKeyLabels[0].setForeground(Theme.Block('I'));
        itemKeyLabels[1].setForeground(Theme.Block('S'));
        itemKeyLabels[2].setForeground(Theme.Block('T'));
        itemKeyLabels[3].setForeground(Theme.Block('L'));


        if(RANK < 10) {
            assistLabel.setForeground(Theme.GRAY);
            nameField.setForeground(Theme.BLACK);
            
        } else {
            assistLabel.setForeground(Theme.Block(ColorType.YELLOW));
            retryButton.setForeground(Theme.GRAY);
        }
    }

    void setAligns() {
        
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i = 0; i < ITEM_COUNT; i++) {
            itemValueLabels[i].setHorizontalAlignment(SwingConstants.LEFT);
            itemKeyLabels[i].setHorizontalAlignment(SwingConstants.LEFT);
        }
        assistLabel.setHorizontalAlignment(
            RANK < 10 ? SwingConstants.LEFT : SwingConstants.CENTER
        );
     }

    void setBorders() { 

        mainContainer.setBorderColor(Theme.BG());
        topContainer.setBorderColor(Theme.BG());
        bottomContainer.setBorderColor(Theme.BG());

        // 실제 창 크기 사용 (동적 크기 조정)
        final int[] screenSize = getActualScreenSize();

        // 팝업 캔버스 스크린 비율 맞추기
        final float[] borderRatio = {1-CANVAS_RATIO[0], 1-CANVAS_RATIO[1]};
        final int borderHeight = (int)(screenSize[1] * borderRatio[1]/2);
        final int borderWidth = (int)(screenSize[0] * borderRatio[0]/2);
        setBorder(BorderFactory.createEmptyBorder(borderHeight, borderWidth, borderHeight, borderWidth));

        // 팝업 캔버스
        final int thickness = 2;
        mainContainer.setBorderThickness(thickness);

        final int marginTB = Theme.getPixelWidth(0.025f);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(marginTB, thickness + 1, marginTB, thickness + 1));

        final int paddingV = Theme.getPixelWidth(0.01f);
        final int centerMarginLeft = Theme.getPixelWidth(0.05f);
        final int bottomMarginLR = Theme.getPixelWidth(0.04f);

        topContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, paddingV, 0));
        centerContainer.setBorder(BorderFactory.createEmptyBorder(paddingV, centerMarginLeft, paddingV, 0));
        bottomContainer.setBorder(BorderFactory.createEmptyBorder(paddingV, bottomMarginLR, 0, bottomMarginLR));

    }
    
    // 실제 창 크기 또는 설정된 해상도를 반환하는 메서드
    private int[] getActualScreenSize() {
        if (frame != null) {
            Dimension size = frame.getSize();
            return new int[]{size.width, size.height};
        }
        // 폴백: GameSettings의 해상도 사용
        return GameSettings.getInstance().getResolutionSize();
    }

    void setButton() {
        retryButton.setFocusPainted(false);
        retryButton.setContentAreaFilled(false);

        retryButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                retryButton.setForeground(Theme.LIGHT_GRAY);
            }
                
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                retryButton.setForeground(Theme.GRAY);
            }
        });

        retryButton.addActionListener(e -> onButtonClick());

    }

    void setField() {
        nameField.addActionListener(e-> onEnterDown());
        ((AbstractDocument) nameField.getDocument())
        .setDocumentFilter(new NoCommaFilter());


    }

    void run() {
        float offset = 0.0f;
        start = new RunLater(
            offset,  () -> {
                topContainer.move(TOP_ANIM_DURATION, 50, 0);
                mainContainer.hueBorder(3.0f, true);
            }
        );
        offset += TOP_ANIM_DURATION;
        for (int i = 0; i < ITEM_COUNT; i++) {
            final Animation target = itemKeyContainers[i];
            itemOrder[i] = new RunLater(
                offset, () -> {
                    target.move(ITEM_ANIM_DURATION, -50, 0);
                    effect.play(false);
                }
            );
            offset += ITEM_ANIM_DURATION;
        }
        for (int i = 0; i < ITEM_COUNT; i++) {
            final Animation target = itemValueContainers[i];
            itemValueOrder[i] = new RunLater(
                offset, () -> {
                    target.move(ITEM_ANIM_DURATION, -50, 0);
                    effect.play(false);
                }
            );
            offset += ITEM_ANIM_DURATION;
        }
        end = new RunLater(
            offset,  () -> {
                bottomContainer.move(BOTTOM_ANIM_DURATION, 0, 100);
                this.bRunEnd = true;
                if(RANK < 10) {
                    mainContainer.setBadgeAnimation();
                    SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
                }

                
            }
        );

        if(RANK < 10) return;
        for(int i = 0; i< RETRY_COUNTDOWN; i++) {
            final int timeLeft = i;
            countdown[i] = new RunLater(
                offset + RETRY_COUNTDOWN - timeLeft, 
                () -> {
                    assistLabel.setText(Integer.toString(timeLeft));
                    if(timeLeft == 0) next();
                }
            );

        }
    }

    


    public void release() {
        if(enterDispatcher != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .removeKeyEventDispatcher(enterDispatcher);
            enterDispatcher = null;
        }
        
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

        if(this.retryButton != null) {
            for (ActionListener al : this.retryButton.getActionListeners()) {
                this.retryButton.removeActionListener(al);
            }
            this.retryButton = null;
        }
        if(this.nameField != null) {
            for (ActionListener al : this.nameField.getActionListeners()) {
                this.nameField.removeActionListener(al);
            }
            this.nameField = null;
        }
    }

    void next() {
        highScore.save();
        this.release();
        Game.setScene(new ScoreScene(frame, RANK + 1, DIFFICULTY));
    }

    public void onExitEvent() {
        System.out.println("Exit Event Callback");
        if(this.bRunEnd == false) {
            start.runNow();
            for(RunLater r: itemOrder) r.runNow();
            for(RunLater r: itemValueOrder) r.runNow();
            // i=0 호출하면 게임이 바로
            end.runNow();
            bRunEnd = true;
        } 
        else next();
    }

    public void onButtonClick() {
        highScore.save();
        release();
        Game.setScene(new GameScene(frame, tetris.GameSettings.getInstance().getDifficulty()));
    }

    public void onEnterDown() {
        final String name = nameField.getText().strip();
        if (!name.isEmpty()) {
            highScore.updateUserName(DIFFICULTY, RANK, name);
            next();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.SrcOver.derive(0.6f));
        g2.setColor(Theme.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

}

class Container extends Animation {
    final float[] LINE_PATTERN = {7f, 5f}; 
    float paddingH = 0.05f;

    Animation badge = null;
    final String BADGE_TEXT = "HIGH SCORE!";
    final int BADGE_THICKNESS = 1;
    
    void setBadgeAnimation() {
        badge = new Animation();
        badge.setBorderColor(Theme.Block('O'));
        badge.saturateBorder(2.5f, true);
    }
    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        int y = -1;
        LayoutManager lm = getLayout();
        if (lm instanceof BorderLayout) {
            BorderLayout bl = (BorderLayout) lm;
            Component north = bl.getLayoutComponent(BorderLayout.NORTH);
            if (north != null && north.isVisible()) {
                y = north.getY() + north.getHeight();
            }
        }

        if (y < 0) y = (int) (getHeight() * 0.2f);

        drawDottedLine(g, y);
        drawBadge(g, y);

    }

    void drawDottedLine(Graphics g, int posY) {
        Graphics2D g2 = (Graphics2D) g.create();

        final int width = getWidth();
        final int paddingH = (int)(width * this.paddingH);
        final float[] dashPattern = this.LINE_PATTERN;

        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, dashPattern, 0f));
        g2.setColor(Theme.DARK_GRAY); 
        g2.drawLine(paddingH, posY, width - paddingH, posY);

        g2.dispose();
    }

    void drawBadge(Graphics g, int posY) {
        //if(!isHighScore) return;
        if(badge == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


        final int width = getWidth();
        final int height = getHeight();

        final int badgeWidth = (int)(width * 0.25f);
        final int badgeHeight = (int)(height * 0.07f);
        
        final int badgeX = width / 2 - badgeWidth / 2;
        final int badgeY = posY - badgeHeight / 2;

        final int badgeRadius = (int)(badgeHeight * 0.5f);

        // 배지 배경
        g2.setColor(Theme.BG());
        g2.fillRect(badgeX, badgeY, badgeWidth, badgeHeight);

        // 배지 텍스트
        g2.setColor(Theme.WHITE);
        g2.setFont(Theme.getFont(Theme.GIANTS_BOLD, 0.009f));
        
        // 텍스트 중앙 정렬
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(BADGE_TEXT);
        int textHeight = fm.getHeight();
        
        int textX = badgeX + (badgeWidth - textWidth) / 2;
        int textY = badgeY + (badgeHeight - textHeight) / 2 + fm.getAscent();
        
        g2.drawString(BADGE_TEXT, textX, textY);
        
        // 배지 테두리
        g2.setColor(HSBtoColor(badge.borderHSB));
        g2.setStroke(new BasicStroke(BADGE_THICKNESS));
        g2.drawRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, badgeRadius, badgeRadius);

        g2.dispose();
    }
    
}


