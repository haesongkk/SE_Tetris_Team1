package tetris;

import tetris.util.DataPathManager;

public class Tetris {
    public static void main(String[] args) {
        // 데이터 경로 초기화 (사용자 홈 디렉토리 기반)
        DataPathManager pathManager = DataPathManager.getInstance();
        pathManager.printDebugInfo();
        
        Game.run();
    }
}
