package tetris.scene.game.overlay;
 
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameOver extends JPanel {
    private final double widthRatio = 0.3f;
    private final double heightRatio = 0.5f;

    private final JFrame frame;
    Timer timer1, timer2, timer3 ,timer4;


    // ==== 생성자 ====
    public GameOver(JFrame frame, int score) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new GridBagLayout());
        
        // 배경 클릭 이벤트 흡수 (아래쪽 전달 방지)
        addMouseListener(new MouseAdapter() {});
        
        PopUp popup = new PopUp();
        popup.setPreferredSize(new Dimension((int)(frame.getWidth() * widthRatio), (int)(frame.getHeight() * heightRatio)));
        popup.setBackground(Color.WHITE); 
        popup.setLayout(new GridBagLayout());
        
        GameOverHeader header = new GameOverHeader();
        Badge badge = new Badge();
        GameOverBody body = new GameOverBody(score, 11, 80, "normal");
        GameOverFooter footer = new GameOverFooter(true);


        popup.add(header, new GridBagConstraints(
            0, 0,               // gridx, gridy
            1, 2,               // gridwidth, gridheight
            1.0, 0.1,           // weightx, weighty
            GridBagConstraints.NORTH, // anchor
            GridBagConstraints.HORIZONTAL, // fill
            new Insets(24, 0, 12, 0), // insets (여백)
            0, 0                // ipadx, ipady
        ));

        // ★ 여기에 배지 추가
        popup.add(badge, new GridBagConstraints(
            0, 2,
            1, 1,
            1.0, 0.0,                      // 세로 공간 거의 안 먹음
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,       // 배지 자체 크기 유지
            new Insets(0, 12, 8, 12),      // header와 body 사이 여백
            0, 0
        ));

        popup.add(body, new GridBagConstraints(
            0, 3,
            1, 2,
            1.0, 0.8,
            GridBagConstraints.CENTER,
            GridBagConstraints.BOTH,
            new Insets(0, 24, 0, 24),
            0, 0
        ));

        popup.add(footer, new GridBagConstraints(
            0, 5,
            1, 1,
            1.0, 0.1,
            GridBagConstraints.SOUTH,
            GridBagConstraints.HORIZONTAL,
            new Insets(12, 12, 24, 12),
            0, 0
        ));


        // ==== Popup을 GameOver에 중앙 배치 ====
        add(popup, new GridBagConstraints(
            0, 0,
            1, 1,
            1.0, 1.0,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0),
            0, 0
        ));

        JRootPane root = frame.getRootPane();
        root.setGlassPane(this);
        this.setVisible(true);
        this.requestFocusInWindow();

        ;

        runLater(0, () -> header.startAnimation(0.5f, 2.5f)); 
        runLater(500, () -> body.startAnimation(0.3f, 2.5f)); 
        runLater(3500, () -> badge.startAnimation(0.6f, 1.3f)); 
        runLater(4200, () -> footer.startAnimation(0.5f, 1.3f)); 

    }

    void runLater(int delayMs, Runnable r) {
        Timer t = new Timer(delayMs, e -> { ((Timer)e.getSource()).stop(); r.run(); });
        t.setRepeats(false);
        t.start();
    }

    // ==== 반투명 배경 그리기 ====
    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.SrcOver.derive(0.6f));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}

class PopUp extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
        g2.dispose();
    }
}


