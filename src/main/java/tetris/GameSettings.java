package tetris;

public class GameSettings {
    private static GameSettings instance = new GameSettings();
    
    // 필수 설정값들
    private int displayMode = 0; // 0: 창모드, 1: 전체화면
    private int resolution = 2; // 0: 800x600, 1: 1024x768, 2: 1280x720, 3: 1920x1080
    private int colorBlindMode = 0; // 0: 일반 모드, 1: 적록색맹, 2: 청황색맹
    
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
    
    // 색맹 모드를 문자열로 반환하는 메서드
    public String getColorBlindModeString() {
        switch (colorBlindMode) {
            case 0: return "일반 모드";
            case 1: return "적록색맹 모드";
            case 2: return "청황색맹 모드";
            default: return "일반 모드";
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
    
    // 모든 설정을 기본값으로 초기화하는 메서드
    public void resetToDefaults() {
        displayMode = 0;
        resolution = 2;
        colorBlindMode = 0;
    }
    
    // 스코어 보드 데이터를 초기화하는 메서드
    public void clearScoreBoard() {
        // 실제 구현에서는 점수 파일이나 데이터베이스 초기화
        // 현재는 메시지만 표시
    }
    
    // 현재 설정을 문자열로 반환하는 메서드 (디버그/확인용)
    public String getSettingsInfo() {
        return String.format(
            "설정 정보:\n" +
            "• 화면 모드: %s\n" +
            "• 해상도: %s\n" +
            "• 색맹 모드: %s",
            getDisplayModeString(),
            getResolutionString(),
            getColorBlindModeString()
        );
    }
}