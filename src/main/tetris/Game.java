package tetris;

import javax.swing.JFrame;

import tetris.scene.*;
import tetris.scene.game.GameScene;

public class Game {
    private Game() {}
    private static Game instance = new Game();
    public static Game getInstance() { return instance; }

    Scene curScene;
    JFrame frame;

    public static void start() {
        instance.frame = new JFrame("Tetris");
        instance.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        instance.frame.setSize(480, 720);
        instance.frame.setLocationRelativeTo(null);
        instance.frame.setVisible(true);

        instance.curScene = new GameScene(instance.frame);
        instance.curScene.onEnter();
    }

    public static void setScene(Scene scene) {
        instance.curScene.onExit();
        instance.curScene = scene;
        instance.curScene.onEnter();
    }

    
}
