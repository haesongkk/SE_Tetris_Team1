package tetris.framework;
import java.util.HashMap;
import java.util.Map;

import java.awt.event.KeyEvent;
import java.awt.KeyboardFocusManager;

public class Input{
    public enum KeyState { NONE, TAP, PUSH, AWAY }
    public enum KeyType  { LEFT, RIGHT, UP, DOWN, SPACE, Z, ESC }

    public KeyState getKeyState(KeyType key) {
        int code = KEYCODE.get(key);
        return m_keyState.get(code);
    }

    private final Map<KeyType, Integer> KEYCODE = Map.of(
        KeyType.LEFT,  KeyEvent.VK_LEFT,
        KeyType.RIGHT, KeyEvent.VK_RIGHT,
        KeyType.UP,    KeyEvent.VK_UP,
        KeyType.DOWN,  KeyEvent.VK_DOWN,
        KeyType.SPACE, KeyEvent.VK_SPACE,
        KeyType.Z,     KeyEvent.VK_Z,
        KeyType.ESC,   KeyEvent.VK_ESCAPE
    );
    private Map<Integer, Boolean> m_prevKeyDown = new HashMap<>();
    private Map<Integer, Boolean> m_curKeyDown = new HashMap<>();
    private Map<Integer, KeyState> m_keyState = new HashMap<>();

    
    public void initialize() {
        for(Integer code: KEYCODE.values() ) {
            m_prevKeyDown.put(code,false);
            m_curKeyDown.put(code,false);
            m_keyState.put(code, KeyState.NONE);
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
        .addKeyEventDispatcher(e -> {
            int code = e.getKeyCode();
            switch (e.getID()) {
                case KeyEvent.KEY_PRESSED:
                    m_curKeyDown.put(code, true);
                    break;
                case KeyEvent.KEY_RELEASED: 
                    m_curKeyDown.put(code, false);
                    break;
            }
            return false; 
        });
    }
    
    public boolean update(double dt) {
        for(Integer code: KEYCODE.values()) {
            boolean curDown = m_curKeyDown.get(code);
            boolean prevDown = m_prevKeyDown.get(code);
            
            if(curDown && prevDown) m_keyState.put(code, KeyState.PUSH);
            else if(curDown && !prevDown) m_keyState.put(code, KeyState.TAP);
            else if(!curDown && prevDown) m_keyState.put(code, KeyState.AWAY);
            else if(!curDown && !prevDown) m_keyState.put(code, KeyState.NONE);
            
            m_prevKeyDown.put(code, curDown);
        }
        return true;
    }
    
    public void finalize() {
        
    }
    
    // 싱글톤 패턴
    private Input() { }
    static Input singleInstance = new Input();
    public static Input get() { return singleInstance; }
}
