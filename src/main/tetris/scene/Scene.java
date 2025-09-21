package tetris.scene;

import javax.swing.*;

public abstract class Scene extends JPanel {
    public Scene(JFrame frame) {
        super();
    }
    public void onEnter() {}
    public void onExit() {}
}