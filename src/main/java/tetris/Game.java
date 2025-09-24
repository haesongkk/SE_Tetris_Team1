package tetris;

import tetris.scene.*;
import tetris.scene.menu.MainMenuScene;

import javax.swing.JFrame;

public class Game {
    private Game() {}
    private static Game instance = new Game();
    public static Game getInstance() { return instance; }

    Scene curScene;
    JFrame frame;

    public static void run() {
        instance.frame = new JFrame("Tetris");
        instance.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        instance.frame.setVisible(true);

        instance.curScene = new MainMenuScene(instance.frame);
        instance.curScene.onEnter();
    }

    public static void setScene(Scene scene) {
        instance.curScene.onExit();
        instance.curScene = scene;
        instance.curScene.onEnter();
    }

    public static void quit() {
        instance.curScene.onExit();
        instance.frame.dispose();
    }
}
