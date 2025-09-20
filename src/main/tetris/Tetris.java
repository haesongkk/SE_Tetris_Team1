package tetris;

// 게임 프레임워크 import
import tetris.framework.Game;
import tetris.framework.Input;
import tetris.framework.Render;

// Tetris 클래스: 게임 실행 메인 클래스
public class Tetris {
    // 생성자: 게임 루프 및 초기화
    public Tetris() {
        long m_prevTime = System.nanoTime(); // 이전 프레임 시간
        Input.get().initialize();            // 입력 초기화
        Render.get().initialize();           // 렌더러 초기화
        Game.get().initialize();             // 게임 초기화

        // 메인 게임 루프
        while(true) {
            long now = System.nanoTime();
            double deltaTima = (now - m_prevTime) / 1_000_000_000.0; // 프레임 시간 계산
            m_prevTime = now;
            if(!Input.get().update(deltaTima)) break;  // 입력 갱신, false면 종료
            if(!Render.get().update(deltaTima)) break; // 렌더 갱신, false면 종료
            if(!Game.get().update(deltaTima)) break;   // 게임 갱신, false면 종료
        }

        // 종료 시 리소스 해제
        Input.get().finalize();
        Render.get().finalize();
        Game.get().finalize();
    }
}
