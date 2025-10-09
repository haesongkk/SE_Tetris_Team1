package tetris.scene.game.overlay;

import tetris.util.Animation;
import tetris.util.Theme;
import tetris.GameSettings;
import java.awt.*;
import javax.swing.*;

public class GOPanel extends Animation {


    GOPanel(String score, String lines, String time, String difficulty, boolean isHighScore) {
        super(
            null, Theme.GIANTS_INLINE, 
            Theme.Block('Z'), Theme.BG(), Theme.Block('O'),
            2, 0, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );


        setLayout(new BorderLayout());

        gameOver = new Animation(
            "GAME OVER!!",
            Theme.getFont(Theme.GIANTS_INLINE, 0.024f), 
            Theme.Block('Z'), Theme.BG(), Theme.BG(), 
            0, 0, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );
        int[] screenSize = GameSettings.getInstance().getResolutionSize();
        int width = (int)(screenSize[0] * 0.05f);
        int height = (int)(screenSize[1] * 0.03f);
        gameOver.setBorder(BorderFactory.createEmptyBorder(height, width, height, width));
        add(gameOver, BorderLayout.NORTH);

        scorePanel = new ScorePanel(score, lines, time, difficulty);
        add(scorePanel, BorderLayout.CENTER);

        goFooter = new GOFooter(isHighScore);
        add(goFooter, BorderLayout.SOUTH);

        badge = new Animation(
            "HIGH SCORE!",
            Theme.getFont(Theme.GIANTS_INLINE, 0.045f),
            Theme.LIGHT_GRAY, Theme.BG(), Theme.Block('O'),
            1, 0,
            SwingConstants.CENTER, SwingConstants.CENTER
        );

        hueBorder(4.5f, true);
        Animation.runLater(0, () -> gameOver.move(50, 0, 0, 0, 1.5f, 0.3f, false)); 
        Animation.runLater(0.3f, () -> scorePanel.startAnimations(2.5f));
        Animation.runLater(3.5f, () -> goFooter.startAnimations());

        badge.alpha = 0f;

        if(isHighScore) {
            Animation.runLater(3.8f, () -> badge.popOut(0.8f, 0.8f, 0.5f, 1.5f));
            Animation.runLater(4.2f, () -> badge.saturateBorder(2.5f, true));
        }


    }

    Animation gameOver;
    ScorePanel scorePanel;
    GOFooter goFooter;

    Animation badge;
    
    @Override
    public void release() {
        super.release();

        gameOver.release();
        gameOver = null;

        scorePanel.release();
        scorePanel = null;

        goFooter.release();
        goFooter = null;

        badge.release();
        badge = null;
    }


    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        
        final int headerY = gameOver.getY();
        final int headerH = gameOver.getHeight();
        final int posY = headerY + headerH;

        drawDottedLine(g, posY);
        drawBadge(g, posY);
    }

    void drawDottedLine(Graphics g, int posY) {
        Graphics2D g2 = (Graphics2D) g.create();

        final int width = getWidth();
        final int offset = (int)(width * 0.05f);
        float[] dashPattern = {7f, 5f}; 
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, dashPattern, 0f));
        g2.setColor(new Color(200, 200, 200, 100)); 
        g2.drawLine(offset, posY, width - offset, posY);

        g2.dispose();
    }

    void drawBadge(Graphics g, int posY) {
        //if(!isHighScore) return;
        if(badge.alpha == 0f) return;
        Graphics2D g2 = (Graphics2D) g.create();
        
        // 배지를 최상단에 그리기 위해 렌더링 힌트 설정
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


        final int width = getWidth();
        final int height = getHeight();

        final int badgeWidth = (int)(width * 0.25f * badge.scaleX);
        final int badgeHeight = (int)(height * 0.07f * badge.scaleY);
        
        final int badgeX = width / 2 - badgeWidth / 2;
        final int badgeY = posY - badgeHeight / 2;

        final int badgeRadius = (int)(badgeHeight * 0.5f);



        // 배지 배경
        g2.setColor(badge.getBackground());
        g2.fillRect(badgeX, badgeY, badgeWidth, badgeHeight);

        // 배지 텍스트
        g2.setColor(badge.getForeground());
        g2.setFont(Theme.getFont(Theme.GIANTS_BOLD, 0.009f));
        
        // 텍스트 중앙 정렬
        FontMetrics fm = g2.getFontMetrics();
        String text = badge.getText();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        int textX = badgeX + (badgeWidth - textWidth) / 2;
        int textY = badgeY + (badgeHeight - textHeight) / 2 + fm.getAscent();
        
        g2.drawString(text, textX, textY);
        
        // 배지 테두리
        g2.setColor(HSBtoColor(badge.borderHSB));
        g2.setStroke(new BasicStroke(badge.borderThickness));
        g2.drawRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, badgeRadius, badgeRadius);

        g2.dispose();
    }
    
}
