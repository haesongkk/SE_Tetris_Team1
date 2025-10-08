package tetris.scene.game.core;

import tetris.Game;
import tetris.GameSettings;
import tetris.scene.menu.MainMenuScene;

import javax.swing.JFrame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * 게임 입력 처리를 담당하는 클래스
 * 
 * 이 클래스는 키보드 입력을 받아 적절한 게임 액션으로 변환하고,
 * 게임 상태에 따라 입력을 필터링하는 역할을 담당합니다.
 * 
 * 주요 기능:
 * - 키보드 입력 이벤트 처리
 * - 게임 상태별 입력 제어 (PLAYING, PAUSED, GAME_OVER)
 * - 사용자 설정 키 매핑
 * - 게임 액션 명령 변환 및 실행
 * 
 * @author SE_Tetris_Team1
 */
public class InputHandler implements KeyListener {
    
    // 게임 액션 열거형
    public enum GameAction {
        MOVE_LEFT,
        MOVE_RIGHT,
        MOVE_DOWN,
        ROTATE,
        HARD_DROP,
        PAUSE,
        HOLD,
        EXIT_TO_MENU
    }
    
    // 입력 콜백 인터페이스
    public interface InputCallback {
        void onGameAction(GameAction action);
        boolean isGameOver();
        boolean isPaused();
        void repaintGame();
    }
    
    private final JFrame frame;
    private final InputCallback callback;
    private final GameSettings settings;
    
    /**
     * InputHandler 생성자
     * 
     * @param frame 게임 메인 프레임
     * @param callback 게임 액션 처리를 위한 콜백
     */
    public InputHandler(JFrame frame, InputCallback callback) {
        this.frame = frame;
        this.callback = callback;
        this.settings = GameSettings.getInstance();
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // 사용하지 않음
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        String keyName = GameSettings.getKeyName(keyCode);
        System.out.println("Key pressed: " + keyName + " (" + keyCode + ")");
        
        // ESC 키는 항상 처리 (메인 메뉴로 이동)
        if (keyCode == KeyEvent.VK_ESCAPE) {
            handleExitToMenu();
            return;
        }
        
        // 게임이 종료된 상태일 때는 ESC를 제외한 모든 키 입력 무시
        if (callback.isGameOver()) {
            return;
        }
        
        // 일시정지 키 처리 (게임이 진행 중일 때만)
        if (keyCode == settings.getPauseKey()) {
            handleGameAction(GameAction.PAUSE);
            return;
        }
        
        // 일시정지 상태일 때는 다른 키 입력 무시
        if (callback.isPaused()) {
            return;
        }
        
        // 사용자 설정 키에 따른 동작 처리
        GameAction action = mapKeyToAction(keyCode);
        if (action != null) {
            handleGameAction(action);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        // 사용하지 않음
    }
    
    /**
     * 키 코드를 게임 액션으로 매핑
     * 
     * @param keyCode 입력된 키 코드
     * @return 대응되는 게임 액션, 매핑되지 않으면 null
     */
    private GameAction mapKeyToAction(int keyCode) {
        if (keyCode == settings.getLeftKey()) {
            return GameAction.MOVE_LEFT;
        } else if (keyCode == settings.getRightKey()) {
            return GameAction.MOVE_RIGHT;
        } else if (keyCode == settings.getFallKey()) {
            return GameAction.MOVE_DOWN;
        } else if (keyCode == settings.getRotateKey()) {
            return GameAction.ROTATE;
        } else if (keyCode == settings.getDropKey()) {
            return GameAction.HARD_DROP;
        } else if (keyCode == settings.getHoldKey()) {
            return GameAction.HOLD;
        }
        
        return null; // 매핑되지 않은 키
    }
    
    /**
     * 게임 액션을 처리하고 화면을 갱신
     * 
     * @param action 실행할 게임 액션
     */
    private void handleGameAction(GameAction action) {
        callback.onGameAction(action);
        
        // 대부분의 액션 후에는 화면 갱신이 필요
        if (action != GameAction.PAUSE) {
            callback.repaintGame();
        }
    }
    
    /**
     * 메인 메뉴로 나가기 처리
     */
    private void handleExitToMenu() {
        Game.setScene(new MainMenuScene(frame));
    }
    
    /**
     * 디버그용: 현재 키 매핑 정보 출력 (사용자 친화적 키 이름 포함)
     */
    public void printKeyMappings() {
        System.out.println("=== Current Key Mappings ===");
        System.out.println("Left: " + GameSettings.getKeyName(settings.getLeftKey()) + " (" + settings.getLeftKey() + ")");
        System.out.println("Right: " + GameSettings.getKeyName(settings.getRightKey()) + " (" + settings.getRightKey() + ")");
        System.out.println("Down: " + GameSettings.getKeyName(settings.getFallKey()) + " (" + settings.getFallKey() + ")");
        System.out.println("Rotate: " + GameSettings.getKeyName(settings.getRotateKey()) + " (" + settings.getRotateKey() + ")");
        System.out.println("Hard Drop: " + GameSettings.getKeyName(settings.getDropKey()) + " (" + settings.getDropKey() + ")");
        System.out.println("Pause: " + GameSettings.getKeyName(settings.getPauseKey()) + " (" + settings.getPauseKey() + ")");
        System.out.println("Hold: " + GameSettings.getKeyName(settings.getHoldKey()) + " (" + settings.getHoldKey() + ")");
        System.out.println("===============================");
    }
    
    /**
     * 키 설정이 변경되었을 때 호출하여 설정을 새로 로드
     */
    public void refreshSettings() {
        // GameSettings는 싱글톤이므로 자동으로 최신 설정을 반영
        // 필요시 추가 로직 구현
        System.out.println("InputHandler: Key settings refreshed");
    }
}