package tetris.scene.menu;
import tetris.Game;
import tetris.scene.Scene;
import tetris.scene.game.GameScene;

import javax.swing.JFrame;

import javax.swing.*;
import java.awt.*;

public class MenuScene extends Scene {
    private final JFrame m_frame;

    public MenuScene(JFrame frame) {
        super(frame);
        this.m_frame = frame;

        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new GridLayout(0, 1, 8, 8));

        JButton btnStart    = new JButton("Start Game");

        btnStart.addActionListener(e -> {
            Game.setScene(new GameScene(m_frame));
        });

        box.add(btnStart);

        add(box);

        m_frame.setContentPane(this);
        m_frame.revalidate();
        m_frame.repaint();
    }
}

