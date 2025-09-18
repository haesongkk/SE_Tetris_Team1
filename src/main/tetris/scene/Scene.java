package tetris.scene;

public interface Scene {
    void initialize();
    boolean update(double dt);
    void finalize();
}
