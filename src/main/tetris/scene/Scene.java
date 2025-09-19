package tetris.scene;

// Scene 인터페이스: 씬의 기본 동작 정의
public interface Scene {
    // 씬 초기화
    void initialize();
    // 씬 갱신 (프레임마다 호출)
    boolean update(double dt);
    // 씬 종료 시 호출
    void finalize();
}
