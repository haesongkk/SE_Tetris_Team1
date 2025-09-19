package tetris.framework;
// Scene, DefaultScene import
import tetris.scene.Scene;
import tetris.scene.DefaultScene;

// Game 클래스 정의 (싱글톤)
public class Game {
    // 현재 활성화된 Scene
    private Scene m_curScene = new DefaultScene();
    // Scene을 변경하는 메서드
    public void setScene(Scene scene) { 
        System.out.println("set scene"); // 디버그용 출력
        m_curScene.finalize();           // 이전 Scene 정리
        m_curScene = scene;              // Scene 교체
        m_curScene.initialize();         // 새 Scene 초기화
    }

    // 게임 전체 초기화 (현재 Scene 초기화)
    public void initialize() {
        m_curScene.initialize();
    }

    // 매 프레임마다 Scene의 update 호출
    public boolean update(double dt) {
        return m_curScene.update(dt);
    }

    // 게임 종료 시 호출 (Scene 정리)
    public void finalize() {
        System.out.println("game finalize");
        m_curScene.finalize();
    }

    // 싱글톤 패턴 구현
    private Game() { }
    static Game singleInstance = new Game(); // 유일 인스턴스
    public static Game get() { return singleInstance; } // 인스턴스 반환
    
}
