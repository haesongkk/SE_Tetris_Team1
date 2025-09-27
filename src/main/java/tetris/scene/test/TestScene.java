package tetris.scene.test;

import tetris.Game;
import tetris.Global;
import tetris.scene.Scene;
import tetris.scene.game.GameScene;
import tetris.scene.game.overlay.GameOver;
import tetris.scene.scorescene.ScoreScene;

import javax.swing.*;
import java.awt.*;

public class TestScene extends Scene {
    private final JFrame m_frame;

    public TestScene(JFrame frame) {
        super(frame);

        this.m_frame = frame;
        frame.setSize(1080,720);
        frame.setLocationRelativeTo(null);


        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new GridLayout(0, 1, 8, 8));

        // 씬 전환 테스트
        JButton btnStart    = new JButton("Start Game");
        btnStart.addActionListener(e -> {
            Game.setScene(new GameScene(m_frame));
        });
        box.add(btnStart);


        JButton btnScore = new JButton("score board");
        btnScore.addActionListener(e -> {
            Game.setScene(new ScoreScene(m_frame));
        });
        box.add(btnScore);

        JButton btnOverlay = new JButton("overlay");
        btnOverlay.addActionListener(e -> {
            GameOver ol = new GameOver(frame, 100);
        });
        box.add(btnOverlay);

        
        // 프로그램 종료 테스트
        JButton btnQuit = new JButton("Quit");
        btnQuit.addActionListener(e -> {
            Game.quit();
        });
        box.add(btnQuit);

        int testGlobalScore = Global.GLOBAL;
        JTextArea scoreDisplay = new JTextArea("global var: " + testGlobalScore);
        scoreDisplay.setEditable(false);
        scoreDisplay.setOpaque(false);
        scoreDisplay.setForeground(Color.WHITE);
        scoreDisplay.setFont(new Font("Arial", Font.BOLD, 16));
        scoreDisplay.setHighlighter(null); // Disable text selection
        box.add(scoreDisplay);

        add(box);

        m_frame.setContentPane(this);
        m_frame.revalidate();
        m_frame.repaint();
    }
}

