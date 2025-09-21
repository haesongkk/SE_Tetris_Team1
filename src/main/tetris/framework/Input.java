package tetris.framework;
// HashMap, Map import
import java.util.HashMap;
import java.util.Map;

// 키 이벤트 관련 import
import java.awt.event.KeyEvent;
import java.awt.KeyboardFocusManager;

// Input 클래스: 키 입력 관리
public class Input{
    // 키 상태 열거형: NONE(안눌림), TAP(막 눌림), PUSH(누르고 있음), AWAY(막 뗌)
    public enum KeyState { NONE, TAP, PUSH, AWAY }
    // 키 종류 열거형
    public enum KeyType  { LEFT, RIGHT, UP, DOWN, SPACE, Z, ESC }

    // 특정 키의 상태 반환
    public KeyState getKeyState(KeyType key) {
        int code = KEYCODE.get(key); // KeyType을 KeyEvent 코드로 변환
        return m_keyState.get(code); // 해당 키의 상태 반환
    }

    // KeyType과 실제 KeyEvent 코드 매핑
    private final Map<KeyType, Integer> KEYCODE = Map.of(
        KeyType.LEFT,  KeyEvent.VK_LEFT,
        KeyType.RIGHT, KeyEvent.VK_RIGHT,
        KeyType.UP,    KeyEvent.VK_UP,
        KeyType.DOWN,  KeyEvent.VK_DOWN,
        KeyType.SPACE, KeyEvent.VK_SPACE,
        KeyType.Z,     KeyEvent.VK_Z,
        KeyType.ESC,   KeyEvent.VK_ESCAPE
    );
    // 이전 프레임의 키 상태
    private Map<Integer, Boolean> m_prevKeyDown = new HashMap<>();
    // 현재 프레임의 키 상태
    private Map<Integer, Boolean> m_curKeyDown = new HashMap<>();
    // 각 키의 KeyState
    private Map<Integer, KeyState> m_keyState = new HashMap<>();

    // 입력 시스템 초기화
    public void initialize() {
        for(Integer code: KEYCODE.values() ) {
            m_prevKeyDown.put(code,false); // 이전 상태 초기화
            m_curKeyDown.put(code,false);  // 현재 상태 초기화
            m_keyState.put(code, KeyState.NONE); // KeyState 초기화
        }
        // 키 이벤트 리스너 등록
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
        .addKeyEventDispatcher(e -> {
            int code = e.getKeyCode();
            switch (e.getID()) {
                case KeyEvent.KEY_PRESSED:
                    m_curKeyDown.put(code, true); // 키가 눌림
                    break;
                case KeyEvent.KEY_RELEASED: 
                    m_curKeyDown.put(code, false); // 키가 뗌
                    break;
            }
            return false; // 이벤트 계속 전달
        });
    }
    
    // 입력 상태 갱신 (프레임마다 호출)
    public boolean update(double dt) {
        for(Integer code: KEYCODE.values()) {
            boolean curDown = m_curKeyDown.get(code);   // 현재 상태
            boolean prevDown = m_prevKeyDown.get(code); // 이전 상태
            
            if(curDown && prevDown) m_keyState.put(code, KeyState.PUSH); // 계속 누름
            else if(curDown && !prevDown) m_keyState.put(code, KeyState.TAP); // 막 눌림
            else if(!curDown && prevDown) m_keyState.put(code, KeyState.AWAY); // 막 뗌
            else if(!curDown && !prevDown) m_keyState.put(code, KeyState.NONE); // 안눌림
            
            m_prevKeyDown.put(code, curDown); // 이전 상태 갱신
        }
        return true;
    }
    
    // 종료 시 호출 (현재 구현 없음)
    public void finalize() {
        
    }
    
    // 싱글톤 패턴 구현
    private Input() { }
    static Input singleInstance = new Input(); // 유일 인스턴스
    public static Input get() { return singleInstance; } // 인스턴스 반환
}
