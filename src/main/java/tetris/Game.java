package tetris;

import javax.swing.JFrame;
import tetris.scene.*;
import tetris.scene.menu.MainMenuScene;

public class Game {
    private Game() {}
    private static Game instance = new Game();
    public static Game getInstance() { return instance; }

    Scene curScene;
    JFrame frame;

    public static void run() {
        // 설정 파일 로드
        GameSettings.getInstance().loadSettings();
        
        instance.frame = new JFrame("Tetris");                                  // 게임 창 생성
        instance.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);                // 창 닫기 시 프로그램 종료
        instance.frame.setVisible(true);                                            // 창 표시

        instance.curScene = new MainMenuScene(instance.frame);
        instance.curScene.onEnter();
    }

    public static void setScene(Scene scene) {
        if (instance.curScene != null) {               // null 체크 추가
            instance.curScene.onExit();                 // 현재 씬 종료 처리    
        }
        instance.curScene = scene;                      // 씬 교체     
        if (instance.curScene != null) {               // null 체크 추가
            instance.curScene.onEnter();                // 새 씬 진입 처리
        }
    }

    public static void quit() {
        instance.curScene.onExit();               // 현재 씬 종료 처리      
        instance.frame.dispose();                 // 창 닫기
        System.exit(0);
        
    }
}
