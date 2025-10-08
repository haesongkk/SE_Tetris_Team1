package tetris.scene;

import javax.swing.*;

/**
 * 테트리스 게임의 화면 전환 시스템을 구현하는 추상 기본 클래스
 * 
 * 이 클래스는 게임의 모든 화면(메인 메뉴, 게임 화면, 설정 화면 등)이 
 * 상속받아야 하는 공통 기반 클래스입니다.
 * 
 * 주요 기능:
 * - JPanel을 상속받아 UI 컴포넌트 기능 제공
 * - 화면 생명주기 메서드 정의 (onEnter, onExit)
 * - 모든 화면이 동일한 인터페이스를 따르도록 강제
 * - Game.java의 setScene() 메서드와 연동하여 화면 전환 관리
 * 
 * 사용되는 디자인 패턴:
 * - Template Method Pattern: 생명주기 메서드 틀 제공
 * - State Pattern: 게임 상태(화면)별 행동 정의
 * 
 */
public abstract class Scene extends JPanel {
    
    /**
     * Scene 생성자
     * @param frame 게임 메인 프레임
     */
    public Scene(JFrame frame) {
        super();
    }
    
    /**
     * 화면 진입 시 호출되는 생명주기 메서드
     * 하위 클래스에서 오버라이드하여 화면 초기화 로직 구현
     */
    public void onEnter() {}
    
    /**
     * 화면 종료 시 호출되는 생명주기 메서드
     * 하위 클래스에서 오버라이드하여 화면 정리 로직 구현
     */
    public void onExit() {}
}