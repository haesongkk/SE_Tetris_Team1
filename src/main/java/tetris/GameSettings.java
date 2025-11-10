package tetris;

import tetris.util.DataPathManager;

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
    // ═══════════════════════════════════════════════════════════════
    // 싱글 플레이 모드 키 설정
    // ═══════════════════════════════════════════════════════════════
    private int leftKey = 37;   // VK_LEFT (←)
    private int rightKey = 39;  // VK_RIGHT (→)
    private int rotateKey = 38; // VK_UP (↑)
    private int fallKey = 40;   // VK_DOWN (↓)
    private int dropKey = 32;   // VK_SPACE
    private int pauseKey = 80;  // VK_P
    private int holdKey = 16;   // VK_SHIFT
    private int exitKey = 81;   // VK_Q
    
    // ═══════════════════════════════════════════════════════════════
    // 배틀 모드 - 1P 키 설정 (WASD + Space)
    // ═══════════════════════════════════════════════════════════════
    private int battleLeftKey1 = 65;   // VK_A
    private int battleRightKey1 = 68;  // VK_D
    private int battleRotateKey1 = 87; // VK_W
    private int battleFallKey1 = 83;   // VK_S
    private int battleDropKey1 = 32;   // VK_SPACE
    private int battlePauseKey1 = 80;  // VK_P
    private int battleHoldKey1 = 16;   // VK_SHIFT
    private int battleExitKey1 = 81;   // VK_Q
    
    // ═══════════════════════════════════════════════════════════════
    // 배틀 모드 - 2P 키 설정 (방향키 + Enter)
    // ═══════════════════════════════════════════════════════════════
    private int battleLeftKey2 = 37;   // VK_LEFT (←)
    private int battleRightKey2 = 39;  // VK_RIGHT (→)
    private int battleRotateKey2 = 38; // VK_UP (↑)
    private int battleFallKey2 = 40;   // VK_DOWN (↓)
    private int battleDropKey2 = 10;   // VK_ENTER
    private int battlePauseKey2 = 80;  // VK_P
    private int battleHoldKey2 = 16;   // VK_SHIFT
    private int battleExitKey2 = 81;   // VK_Q

    // 음량 조절
    private int volume = 20;
    private boolean isMuted = false;
    
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
    
    // 키 설정 getter 메서드들 (싱글 플레이 모드)
    public int getLeftKey() { return leftKey; }
    public int getRightKey() { return rightKey; }
    public int getRotateKey() { return rotateKey; }
    public int getFallKey() { return fallKey; }
    public int getDropKey() { return dropKey; }
    public int getPauseKey() { return pauseKey; }
    public int getHoldKey() { return holdKey; }
    public int getExitKey() { return exitKey; }
    
    // 배틀 모드 - 플레이어별 키 설정 가져오기
    // player가 0이면 싱글 플레이 모드 키 반환, 1 or 2면 배틀 모드 키 반환
    public int getLeftKey(int player) { 
        if (player == 0) return leftKey; // 싱글 플레이 모드
        return player == 1 ? battleLeftKey1 : battleLeftKey2; 
    }
    public int getRightKey(int player) { 
        if (player == 0) return rightKey; // 싱글 플레이 모드
        return player == 1 ? battleRightKey1 : battleRightKey2; 
    }
    public int getRotateKey(int player) { 
        if (player == 0) return rotateKey; // 싱글 플레이 모드
        return player == 1 ? battleRotateKey1 : battleRotateKey2; 
    }
    public int getFallKey(int player) { 
        if (player == 0) return fallKey; // 싱글 플레이 모드
        return player == 1 ? battleFallKey1 : battleFallKey2; 
    }
    public int getDropKey(int player) { 
        if (player == 0) return dropKey; // 싱글 플레이 모드
        return player == 1 ? battleDropKey1 : battleDropKey2; 
    }
    public int getPauseKey(int player) { 
        if (player == 0) return pauseKey; // 싱글 플레이 모드
        return player == 1 ? battlePauseKey1 : battlePauseKey2; 
    }
    public int getHoldKey(int player) { 
        if (player == 0) return holdKey; // 싱글 플레이 모드
        return player == 1 ? battleHoldKey1 : battleHoldKey2; 
    }
    public int getExitKey(int player) { 
        if (player == 0) return exitKey; // 싱글 플레이 모드
        return player == 1 ? battleExitKey1 : battleExitKey2; 
    }
    
    // 키 설정 setter 메서드들 (싱글 플레이 모드)
    public void setLeftKey(int keyCode) { this.leftKey = keyCode; }
    public void setRightKey(int keyCode) { this.rightKey = keyCode; }
    public void setRotateKey(int keyCode) { this.rotateKey = keyCode; }
    public void setFallKey(int keyCode) { this.fallKey = keyCode; }
    public void setDropKey(int keyCode) { this.dropKey = keyCode; }
    public void setPauseKey(int keyCode) { this.pauseKey = keyCode; }
    public void setHoldKey(int keyCode) { this.holdKey = keyCode; }
    
    // 배틀 모드 - 1P 키 설정 setter
    public void setBattleLeftKey1(int keyCode) { this.battleLeftKey1 = keyCode; }
    public void setBattleRightKey1(int keyCode) { this.battleRightKey1 = keyCode; }
    public void setBattleRotateKey1(int keyCode) { this.battleRotateKey1 = keyCode; }
    public void setBattleFallKey1(int keyCode) { this.battleFallKey1 = keyCode; }
    public void setBattleDropKey1(int keyCode) { this.battleDropKey1 = keyCode; }
    public void setBattlePauseKey1(int keyCode) { this.battlePauseKey1 = keyCode; }
    public void setBattleHoldKey1(int keyCode) { this.battleHoldKey1 = keyCode; }
    
    // 배틀 모드 - 2P 키 설정 setter
    public void setBattleLeftKey2(int keyCode) { this.battleLeftKey2 = keyCode; }
    public void setBattleRightKey2(int keyCode) { this.battleRightKey2 = keyCode; }
    public void setBattleRotateKey2(int keyCode) { this.battleRotateKey2 = keyCode; }
    public void setBattleFallKey2(int keyCode) { this.battleFallKey2 = keyCode; }
    public void setBattleDropKey2(int keyCode) { this.battleDropKey2 = keyCode; }
    public void setBattlePauseKey2(int keyCode) { this.battlePauseKey2 = keyCode; }
    public void setBattleHoldKey2(int keyCode) { this.battleHoldKey2 = keyCode; }
    
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

    public int getVolume() { return volume; }
    public void setVolume(int v) { volume = v; }
    String getVolumeString() { return Integer.toString(volume); }
    
    public boolean isMuted() { return isMuted; }
    public void setMuted(boolean muted) { isMuted = muted; }
    String getMutedString() { return Boolean.toString(isMuted); }
    
    // 모든 설정을 기본값으로 초기화하는 메서드
    public void resetToDefaults() {
        displayMode = 0;
        resolution = 2;
        colorBlindMode = 0;
        difficulty = Difficulty.NORMAL; // 난이도 기본값으로 초기화

        // 키 설정도 기본값으로 초기화
        // 싱글 플레이 모드
        leftKey = 37;   // VK_LEFT
        rightKey = 39;  // VK_RIGHT
        rotateKey = 38; // VK_UP
        fallKey = 40;   // VK_DOWN
        dropKey = 32;   // VK_SPACE
        pauseKey = 80;  // VK_P
        holdKey = 16;   // VK_SHIFT
        
        // 배틀 모드 - 1P (WASD + Space)
        battleLeftKey1 = 65;   // VK_A
        battleRightKey1 = 68;  // VK_D
        battleRotateKey1 = 87; // VK_W
        battleFallKey1 = 83;   // VK_S
        battleDropKey1 = 32;   // VK_SPACE
        battlePauseKey1 = 80;  // VK_P
        battleHoldKey1 = 16;   // VK_SHIFT
        
        // 배틀 모드 - 2P (방향키 + Enter)
        battleLeftKey2 = 37;   // VK_LEFT
        battleRightKey2 = 39;  // VK_RIGHT
        battleRotateKey2 = 38; // VK_UP
        battleFallKey2 = 40;   // VK_DOWN
        battleDropKey2 = 10;   // VK_ENTER
        battlePauseKey2 = 80;  // VK_P
        battleHoldKey2 = 16;   // VK_SHIFT

        volume = 20;
        isMuted = false;
    }
    
    // 스코어 보드 데이터를 초기화하는 메서드
    public void clearScoreBoard() {
        try {
            // DataPathManager를 통한 파일 경로 관리
            DataPathManager pathManager = DataPathManager.getInstance();
            pathManager.clearAllScores();
        } catch (Exception e) {
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
            "• 난이도: %s\n" +
            "• 음량: %s",
            getDisplayModeString(),
            getResolutionString(),
            getColorBlindModeString(),
            getDifficultyString(),
            getVolumeString()
        );
    }
    
    // 설정을 파일에 저장
    public void saveSettings() {
        try {
            DataPathManager pathManager = DataPathManager.getInstance();
            java.io.File settingsFile = pathManager.getSettingsFile();
            
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(settingsFile))) {
                writer.println("# Game Settings");
                writer.println("displayMode=" + displayMode);
                writer.println("resolution=" + resolution);
                writer.println("colorBlindMode=" + colorBlindMode);
                writer.println("difficulty=" + difficulty.ordinal());
                
                // 싱글 플레이 키 설정
                writer.println("leftKey=" + leftKey);
                writer.println("rightKey=" + rightKey);
                writer.println("rotateKey=" + rotateKey);
                writer.println("fallKey=" + fallKey);
                writer.println("dropKey=" + dropKey);
                writer.println("pauseKey=" + pauseKey);
                writer.println("holdKey=" + holdKey);
                writer.println("exitKey=" + exitKey);
                
                // 배틀 모드 1P 키 설정
                writer.println("battleLeftKey1=" + battleLeftKey1);
                writer.println("battleRightKey1=" + battleRightKey1);
                writer.println("battleRotateKey1=" + battleRotateKey1);
                writer.println("battleFallKey1=" + battleFallKey1);
                writer.println("battleDropKey1=" + battleDropKey1);
                writer.println("battlePauseKey1=" + battlePauseKey1);
                writer.println("battleHoldKey1=" + battleHoldKey1);
                writer.println("battleExitKey1=" + battleExitKey1);
                
                // 배틀 모드 2P 키 설정
                writer.println("battleLeftKey2=" + battleLeftKey2);
                writer.println("battleRightKey2=" + battleRightKey2);
                writer.println("battleRotateKey2=" + battleRotateKey2);
                writer.println("battleFallKey2=" + battleFallKey2);
                writer.println("battleDropKey2=" + battleDropKey2);
                writer.println("battlePauseKey2=" + battlePauseKey2);
                writer.println("battleHoldKey2=" + battleHoldKey2);
                writer.println("battleExitKey2=" + battleExitKey2);
                
                // 기타 설정
                writer.println("volume=" + volume);
                writer.println("isMuted=" + isMuted);
            }
            System.out.println("설정이 저장되었습니다: " + settingsFile.getAbsolutePath());
        } catch (java.io.IOException e) {
            System.err.println("설정 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 파일에서 설정 로드
    public void loadSettings() {
        try {
            DataPathManager pathManager = DataPathManager.getInstance();
            java.io.File settingsFile = pathManager.getSettingsFile();
            
            if (!settingsFile.exists()) {
                System.out.println("설정 파일이 없습니다. 기본 설정을 사용합니다.");
                return;
            }
            
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(settingsFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    // 주석이나 빈 줄 무시
                    if (line.startsWith("#") || line.isEmpty()) {
                        continue;
                    }
                    
                    // key=value 형식 파싱
                    String[] parts = line.split("=", 2);
                    if (parts.length != 2) continue;
                    
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    
                    try {
                        switch (key) {
                            case "displayMode":
                                displayMode = Integer.parseInt(value);
                                break;
                            case "resolution":
                                resolution = Integer.parseInt(value);
                                break;
                            case "colorBlindMode":
                                colorBlindMode = Integer.parseInt(value);
                                break;
                            case "difficulty":
                                int diffIndex = Integer.parseInt(value);
                                difficulty = Difficulty.values()[diffIndex];
                                break;
                            
                            // 싱글 플레이 키 설정
                            case "leftKey":
                                leftKey = Integer.parseInt(value);
                                break;
                            case "rightKey":
                                rightKey = Integer.parseInt(value);
                                break;
                            case "rotateKey":
                                rotateKey = Integer.parseInt(value);
                                break;
                            case "fallKey":
                                fallKey = Integer.parseInt(value);
                                break;
                            case "dropKey":
                                dropKey = Integer.parseInt(value);
                                break;
                            case "pauseKey":
                                pauseKey = Integer.parseInt(value);
                                break;
                            case "holdKey":
                                holdKey = Integer.parseInt(value);
                                break;
                            case "exitKey":
                                exitKey = Integer.parseInt(value);
                                break;
                            
                            // 배틀 모드 1P 키 설정
                            case "battleLeftKey1":
                                battleLeftKey1 = Integer.parseInt(value);
                                break;
                            case "battleRightKey1":
                                battleRightKey1 = Integer.parseInt(value);
                                break;
                            case "battleRotateKey1":
                                battleRotateKey1 = Integer.parseInt(value);
                                break;
                            case "battleFallKey1":
                                battleFallKey1 = Integer.parseInt(value);
                                break;
                            case "battleDropKey1":
                                battleDropKey1 = Integer.parseInt(value);
                                break;
                            case "battlePauseKey1":
                                battlePauseKey1 = Integer.parseInt(value);
                                break;
                            case "battleHoldKey1":
                                battleHoldKey1 = Integer.parseInt(value);
                                break;
                            case "battleExitKey1":
                                battleExitKey1 = Integer.parseInt(value);
                                break;
                            
                            // 배틀 모드 2P 키 설정
                            case "battleLeftKey2":
                                battleLeftKey2 = Integer.parseInt(value);
                                break;
                            case "battleRightKey2":
                                battleRightKey2 = Integer.parseInt(value);
                                break;
                            case "battleRotateKey2":
                                battleRotateKey2 = Integer.parseInt(value);
                                break;
                            case "battleFallKey2":
                                battleFallKey2 = Integer.parseInt(value);
                                break;
                            case "battleDropKey2":
                                battleDropKey2 = Integer.parseInt(value);
                                break;
                            case "battlePauseKey2":
                                battlePauseKey2 = Integer.parseInt(value);
                                break;
                            case "battleHoldKey2":
                                battleHoldKey2 = Integer.parseInt(value);
                                break;
                            case "battleExitKey2":
                                battleExitKey2 = Integer.parseInt(value);
                                break;
                            
                            // 기타 설정
                            case "volume":
                                volume = Integer.parseInt(value);
                                break;
                            case "isMuted":
                                isMuted = Boolean.parseBoolean(value);
                                break;
                        }
                    } catch (Exception e) {
                        System.err.println("설정 값 파싱 오류: " + key + "=" + value);
                    }
                }
            }
            System.out.println("설정이 로드되었습니다.");
        } catch (java.io.IOException e) {
            System.err.println("설정 로드 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}