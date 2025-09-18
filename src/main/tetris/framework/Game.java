package tetris.framework;
import tetris.scene.Scene;
import tetris.scene.DefaultScene;

public class Game {
    private Scene m_curScene = new DefaultScene();
    public void setScene(Scene scene) { 
        System.out.println("set scene");
        m_curScene.finalize();
        m_curScene = scene; 
        m_curScene.initialize();
    }

    public void initialize() {
        m_curScene.initialize();
    }

    public boolean update(double dt) {
        return m_curScene.update(dt);
    }

    public void finalize() {
        System.out.println("game finalize");
        m_curScene.finalize();
    }

    // 싱글톤 패턴
    private Game() { }
    static Game singleInstance = new Game();
    public static Game get() { return singleInstance; }
    
}
