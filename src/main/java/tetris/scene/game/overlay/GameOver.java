package tetris.scene.game.overlay;
 
import javax.swing.*;

import tetris.Game;
import tetris.GameSettings;
import tetris.scene.game.GameScene;
import tetris.scene.scorescene.ScoreScene;
import tetris.util.HighScore;
import tetris.util.RunLater;
import tetris.util.Sound;
import tetris.util.Theme;

import java.awt.*;
import java.awt.event.*;

public class GameOver extends JPanel {


    public GameOver(JFrame frame, int score, int lines, int time, String difficulty) {
        this.highScore = new HighScore("./data/highscore_v2.txt");
        this.frame = frame;

        this.difficulty = difficulty.toLowerCase();
        this.score = score;
        this.lines = lines;
        this.time = time;

        this.rankIndex = highScore.add(difficulty, score, lines, time);
        System.out.println("rank: " + rankIndex);

        setOpaque(false);
        
        addMouseListener(new MouseAdapter() {});

        // 캔버스 사이즈 맞추기
        final int[] screenSize = GameSettings.getInstance().getResolutionSize();
        final float[] borderRatio = {1-canvasRatio[0], 1-canvasRatio[1]};
        final int borderHeight = (int)(screenSize[1] * borderRatio[1]/2);
        final int borderWidth = (int)(screenSize[0] * borderRatio[0]/2);

        setLayout(new GridLayout(1, 1));
        setBorder(BorderFactory.createEmptyBorder(borderHeight, borderWidth, borderHeight, borderWidth));
        
        popup = new GOPanel(
            Integer.toString(score), 
            Integer.toString(lines), 
            formatTime(time), 
            difficulty, 
            rankIndex >= 0 && rankIndex < 10
        );
        add(popup);

        bgm = new Sound("arcade-beat-323176.mp3");
        bgm.play(true);

        frame.getRootPane().setGlassPane(this);
        this.setVisible(true);
        this.requestFocusInWindow();
    }

    void onRetry() {
        this.release();
        Game.setScene(new GameScene(frame));
    }

    void onNext(String name) {
        if(rankIndex >= 0 && rankIndex < 10) {
            highScore.updateUserName(difficulty, rankIndex, name);
            highScore.save();
        }
        this.release();
        Game.setScene(new ScoreScene(frame, rankIndex + 1, difficulty));
    }



    
    String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
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

    void release() {
        //frame.getRootPane().setGlassPane(null);

        highScore.release();
        highScore = null;

        popup.release();
        popup = null;

        if(bgm != null) {
            bgm.release();
            bgm = null;
        }

        RunLater.clear();
    }


    JFrame frame;
    GOPanel popup;
    HighScore highScore;
    Sound bgm = null;

    int score;
    int lines;
    int time;
    String difficulty;
    int rankIndex;

    final float[] canvasRatio = { 0.3f, 0.6f };
}


