package tetris;

import java.awt.Color;

/**
 * 색맹 지원을 위한 색상 변환 유틸리티 클래스
 * 적록색맹과 청황색맹을 위한 색상 팔레트를 제공합니다.
 */
public class ColorBlindHelper {
    
    // 일반 테트리스 블록 색상들
    private static final Color[] NORMAL_COLORS = {
        new Color(255, 0, 0),    // 빨간색 (Z 블록)
        new Color(255, 165, 0),  // 주황색 (L 블록)
        new Color(255, 255, 0),  // 노란색 (O 블록)
        new Color(0, 255, 0),    // 초록색 (S 블록)
        new Color(0, 255, 255),  // 시안색 (I 블록)
        new Color(0, 0, 255),    // 파란색 (J 블록)
        new Color(128, 0, 128)   // 보라색 (T 블록)
    };
    
    // 적록색맹을 위한 색상 팔레트 (파란색과 노란색 계열 중심)
    private static final Color[] DEUTERANOPIA_COLORS = {
        new Color(255, 255, 0),   // 밝은 노란색
        new Color(255, 200, 0),   // 황금색
        new Color(255, 150, 0),   // 주황색
        new Color(0, 150, 255),   // 하늘색
        new Color(0, 100, 255),   // 파란색
        new Color(100, 0, 200),   // 보라색
        new Color(200, 200, 200)  // 회색
    };
    
    // 청황색맹을 위한 색상 팔레트 (빨간색과 초록색 계열 중심)
    private static final Color[] TRITANOPIA_COLORS = {
        new Color(255, 0, 0),     // 빨간색
        new Color(255, 100, 100), // 밝은 빨간색
        new Color(0, 255, 0),     // 초록색
        new Color(100, 255, 100), // 밝은 초록색
        new Color(255, 0, 255),   // 마젠타
        new Color(150, 150, 150), // 회색
        new Color(50, 50, 50)     // 어두운 회색
    };
    
    /**
     * 색맹 모드에 따라 적절한 색상을 반환합니다.
     * 
     * @param blockType 블록 타입 (0-6)
     * @param colorBlindMode 색맹 모드 (0: 일반, 1: 적록색맹, 2: 청황색맹)
     * @return 적절한 색상
     */
    public static Color getBlockColor(int blockType, int colorBlindMode) {
        // 블록 타입 범위 검사
        if (blockType < 0 || blockType >= NORMAL_COLORS.length) {
            blockType = 0;
        }
        
        switch (colorBlindMode) {
            case 1: // 적록색맹 (Deuteranopia)
                return DEUTERANOPIA_COLORS[blockType];
            case 2: // 청황색맹 (Tritanopia)
                return TRITANOPIA_COLORS[blockType];
            case 0: // 일반 모드
            default:
                return NORMAL_COLORS[blockType];
        }
    }
    
    /**
     * 색맹 모드에 따라 게임 배경색을 반환합니다.
     */
    public static Color getBackgroundColor(int colorBlindMode) {
        switch (colorBlindMode) {
            case 1: // 적록색맹
                return new Color(30, 30, 50);  // 약간 파란 계열 배경
            case 2: // 청황색맹
                return new Color(50, 30, 30);  // 약간 빨간 계열 배경
            case 0: // 일반 모드
            default:
                return new Color(20, 20, 40);  // 기본 어두운 배경
        }
    }
    
    /**
     * 색맹 모드에 따라 테두리색을 반환합니다.
     */
    public static Color getBorderColor(int colorBlindMode) {
        switch (colorBlindMode) {
            case 1: // 적록색맹
                return new Color(255, 255, 255); // 흰색 테두리
            case 2: // 청황색맹
                return new Color(0, 0, 0);       // 검은색 테두리
            case 0: // 일반 모드
            default:
                return new Color(128, 128, 128); // 회색 테두리
        }
    }
    

   
}