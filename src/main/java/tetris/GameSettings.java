package tetris;

public class GameSettings {
    // 난이도 enum
    public enum Difficulty {
        EASY, NORMAL, HARD
    }
    
    private static GameSettings instance = new GameSettings();
    
    // 필수 설정값들
    private int displayMode = 0; // 0: 창모드, 1: 전체화면
    private int resolution = 2; // 0: 800x600, 1: 1024x768, 2: 1280x720, 3: 1920x1080
    private int colorBlindMode = 0; // 0: 일반 모드, 1: 적록색맹, 2: 청황색맹
    private Difficulty difficulty = Difficulty.NORMAL; // 기본 난이도
    
    // 게임 조작키 설정값들 (KeyEvent 상수값 사용)
    private int leftKey = 37;   // VK_LEFT (←)
    private int rightKey = 39;  // VK_RIGHT (→)
    private int rotateKey = 38; // VK_UP (↑)
    private int fallKey = 40;   // VK_DOWN (↓)
    private int dropKey = 32;   // VK_SPACE
    private int pauseKey = 80;  // VK_P
    private int holdKey = 16;   // VK_SHIFT
    private int exitKey = 81;   // VK_Q
    
    // 싱글톤 패턴 - 생성자를 private으로 설정
    private GameSettings() {}
    
    // 인스턴스 반환 메서드
    public static GameSettings getInstance() {
        return instance;
    }
    
    // 화면 모드 getter/setter
    public int getDisplayMode() {
        return displayMode;
    }
    
    public void setDisplayMode(int displayMode) {
        this.displayMode = Math.max(0, Math.min(1, displayMode));
    }
    
    // 해상도 getter/setter
    public int getResolution() {
        return resolution;
    }
    
    public void setResolution(int resolution) {
        this.resolution = Math.max(0, Math.min(3, resolution));
    }
    
    // 색맹 모드 getter/setter
    public int getColorBlindMode() {
        return colorBlindMode;
    }
    
    public void setColorBlindMode(int colorBlindMode) {
        this.colorBlindMode = Math.max(0, Math.min(2, colorBlindMode));
    }
    
    // 난이도 getter/setter
    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getDifficultyIndex() {
        switch (difficulty) {
            case EASY: return 0;
            case NORMAL: return 1;
            case HARD: return 2;
            default: return 3;
        }
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setDifficultyIndex(int index) {
        switch(index) {
            case 0: difficulty = Difficulty.EASY; break;
            case 1: difficulty = Difficulty.NORMAL; break;
            case 2: difficulty = Difficulty.HARD; break;
            default: difficulty = Difficulty.NORMAL; break;
        }
    }
    
    // 색맹 모드를 문자열로 반환하는 메서드
    public String getColorBlindModeString() {
        switch (colorBlindMode) {
            case 0: return "일반 모드";
            case 1: return "적록색맹 모드";
            case 2: return "청황색맹 모드";
            default: return "일반 모드";
        }
    }
    
    
    // 난이도를 문자열로 반환하는 메서드
    public String getDifficultyString() {
        switch (difficulty) {
            case EASY: return "Easy";
            case NORMAL: return "Normal";
            case HARD: return "Hard";
            default: return "Normal";
        }
    }
    
    // 해상도를 실제 크기로 변환하는 메서드
    public int[] getResolutionSize() {
        switch (resolution) {
            case 0: return new int[]{800, 600};
            case 1: return new int[]{1024, 768};
            case 2: return new int[]{1280, 720};
            case 3: return new int[]{1920, 1080};
            default: return new int[]{1280, 720};
        }
    }
    
    // 해상도를 문자열로 반환하는 메서드
    public String getResolutionString() {
        switch (resolution) {
            case 0: return "800x600 (4:3)";
            case 1: return "1024x768 (4:3)";
            case 2: return "1280x720 (16:9)";
            case 3: return "1920x1080 (16:9)";
            default: return "1280x720 (16:9)";
        }
    }
    
    // 화면 모드를 문자열로 반환하는 메서드
    public String getDisplayModeString() {
        return displayMode == 0 ? "창모드" : "전체화면";
    }
    
    // 키 설정 getter 메서드들
    public int getLeftKey() { return leftKey; }
    public int getRightKey() { return rightKey; }
    public int getRotateKey() { return rotateKey; }
    public int getFallKey() { return fallKey; }
    public int getDropKey() { return dropKey; }
    public int getPauseKey() { return pauseKey; }
    public int getHoldKey() { return holdKey; }
    public int getExitKey() { return exitKey; }
    
    // 키 설정 setter 메서드들
    public void setLeftKey(int keyCode) { this.leftKey = keyCode; }
    public void setRightKey(int keyCode) { this.rightKey = keyCode; }
    public void setRotateKey(int keyCode) { this.rotateKey = keyCode; }
    public void setFallKey(int keyCode) { this.fallKey = keyCode; }
    public void setDropKey(int keyCode) { this.dropKey = keyCode; }
    public void setPauseKey(int keyCode) { this.pauseKey = keyCode; }
    public void setHoldKey(int keyCode) { this.holdKey = keyCode; }
    
    // 키 코드를 문자열로 변환하는 메서드
    public static String getKeyName(int keyCode) {
        switch (keyCode) {
            // 화살표 키
            case 37: return "←";
            case 38: return "↑";
            case 39: return "→";
            case 40: return "↓";
            
            // 특수 키
            case 32: return "Space";
            case 10: return "Enter";
            case 27: return "Esc";
            case 8: return "Backspace";
            case 9: return "Tab";
            case 20: return "Caps Lock";
            case 127: return "Delete";
            
            // 수정자 키
            case 16: return "Shift";
            case 17: return "Ctrl";
            case 18: return "Alt";
            
            // 알파벳 키 (A-Z)
            case 65: return "A";
            case 66: return "B";
            case 67: return "C";
            case 68: return "D";
            case 69: return "E";
            case 70: return "F";
            case 71: return "G";
            case 72: return "H";
            case 73: return "I";
            case 74: return "J";
            case 75: return "K";
            case 76: return "L";
            case 77: return "M";
            case 78: return "N";
            case 79: return "O";
            case 80: return "P";
            case 81: return "Q";
            case 82: return "R";
            case 83: return "S";
            case 84: return "T";
            case 85: return "U";
            case 86: return "V";
            case 87: return "W";
            case 88: return "X";
            case 89: return "Y";
            case 90: return "Z";
            
            // 숫자 키 (0-9)
            case 48: return "0";
            case 49: return "1";
            case 50: return "2";
            case 51: return "3";
            case 52: return "4";
            case 53: return "5";
            case 54: return "6";
            case 55: return "7";
            case 56: return "8";
            case 57: return "9";
            
            // 펑션 키 (F1-F12)
            case 112: return "F1";
            case 113: return "F2";
            case 114: return "F3";
            case 115: return "F4";
            case 116: return "F5";
            case 117: return "F6";
            case 118: return "F7";
            case 119: return "F8";
            case 120: return "F9";
            case 121: return "F10";
            case 122: return "F11";
            case 123: return "F12";
            
            // 숫자 패드
            case 96: return "Num 0";
            case 97: return "Num 1";
            case 98: return "Num 2";
            case 99: return "Num 3";
            case 100: return "Num 4";
            case 101: return "Num 5";
            case 102: return "Num 6";
            case 103: return "Num 7";
            case 104: return "Num 8";
            case 105: return "Num 9";
            case 107: return "Num +";
            case 109: return "Num -";
            case 106: return "Num *";
            case 111: return "Num /";
            case 110: return "Num .";
            
            // 특수 문자 키
            case 192: return "`";      // 백틱
            case 45: return "-";       // 하이픈
            case 61: return "=";       // 등호
            case 91: return "[";       // 왼쪽 대괄호
            case 93: return "]";       // 오른쪽 대괄호
            case 92: return "\\";      // 백슬래시
            case 59: return ";";       // 세미콜론
            case 222: return "'";      // 어포스트로피
            case 44: return ",";       // 쉼표
            case 46: return ".";       // 마침표
            case 47: return "/";       // 슬래시
            
            // Home/End/Page Up/Down
            case 36: return "Home";
            case 35: return "End";
            case 33: return "Page Up";
            case 34: return "Page Down";
            case 155: return "Insert";
            
            // 기타 키
            case 524: return "Windows";
            case 525: return "Menu";
            case 19: return "Pause";
            case 145: return "Scroll Lock";
            case 144: return "Num Lock";
            
            default: 
                // 키 이름이 정의되지 않은 경우 키코드로 표시
                return "Key(" + keyCode + ")";
        }
    }
    
    // 모든 설정을 기본값으로 초기화하는 메서드
    public void resetToDefaults() {
        displayMode = 0;
        resolution = 2;
        colorBlindMode = 0;
        difficulty = Difficulty.NORMAL; // 난이도 기본값으로 초기화

        // 키 설정도 기본값으로 초기화
        leftKey = 37;   // VK_LEFT
        rightKey = 39;  // VK_RIGHT
        rotateKey = 38; // VK_UP
        fallKey = 40;   // VK_DOWN
        dropKey = 32;   // VK_SPACE
        pauseKey = 80;  // VK_P
        holdKey = 16;   // VK_SHIFT
    }
    
    // 스코어 보드 데이터를 초기화하는 메서드
    public void clearScoreBoard() {
        try {
            // highscore.txt 파일을 빈 파일로 만들기
            java.io.File scoreFile = new java.io.File("./data/highscore_v2.txt");
            if (scoreFile.exists()) {
                // 파일을 빈 내용으로 덮어쓰기
                try (java.io.FileWriter writer = new java.io.FileWriter(scoreFile, false)) {
                    writer.write(""); // 빈 내용으로 덮어쓰기
                }
            }
        } catch (java.io.IOException e) {
            System.err.println("스코어 보드 초기화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 현재 설정을 문자열로 반환하는 메서드 (디버그/확인용)
    public String getSettingsInfo() {
        return String.format(
            "설정 정보:\n" +
            "• 화면 모드: %s\n" +
            "• 해상도: %s\n" +
            "• 색맹 모드: %s\n" +
            "• 난이도: %s",
            getDisplayModeString(),
            getResolutionString(),
            getColorBlindModeString(),
            getDifficultyString()
        );
    }
}