package tetris.scene;

import tetris.framework.Render;

// GameScene 클래스는 Scene 인터페이스를 구현
public class GameScene implements Scene {
    // 씬 초기화 메서드
    @Override public void initialize(){ 
        // 배경색을 검정색(0, 0.f, 0, 1)으로 설정
        Render.get().setClearColor(0, 0.f, 0, 1);
    }
    // 매 프레임마다 호출되는 업데이트 메서드
    @Override public boolean update(double dt){
        // 텍스트 색상을 초록색(0, 1.f, 0, 1)으로 설정
        Render.get().setColor(0, 1.f, 0, 1);
        // 화면 (400, 200) 위치에 "다음씬" 텍스트를 그림
        Render.get().drawText(400,200, "다음씬", -1);
        // true 반환 (씬이 계속 활성화됨)
        return true;
    }
    // 씬 종료 시 호출되는 메서드 (현재 구현 없음)
    @Override public void finalize(){ }
    // 클래스 끝
    
}
