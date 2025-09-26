package tetris.scene.scorescene;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.Timer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class ScoreFooter extends JLabel {
    boolean bShow = false;
    int cnt = 0;

    ScoreFooter() {
        setOpaque(false);

        setFont(Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 45));
        setForeground(Theme.TEXT_GRAY);
        setText("P R E S S    E S C    T O    E X I T");
        setBorder(BorderFactory.createEmptyBorder(0,24,0,24));

        setHorizontalAlignment(SwingConstants.CENTER);
       
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (bShow) {
            super.paintComponent(g);
        }
    }

    public void startAnimation() {
        int delay = 400; // 밀리초
        Timer timer = new Timer(delay, e -> {
            cnt++;
            if(bShow && cnt==2) {
                bShow = !bShow;
                cnt =0;
            }
            if(!bShow && cnt == 1) {
                bShow = true;
                cnt = 0;
            }
            repaint();
        });
        timer.start();
    }
    
}
