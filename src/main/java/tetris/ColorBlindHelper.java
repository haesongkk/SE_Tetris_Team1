package tetris;

import java.awt.Color;

/**
 * 색맹 지원을 위한 색상 변환 유틸리티 클래스
 * 적록색맹과 청황색맹을 위한 색상 팔레트를 제공합니다.
 */
public class ColorBlindHelper {
    
    // 일반 테트리스 블록 색상들 - 개선된 대비와 구분성
    // 테스트 조건: 블록 간 색상 대비비 1.5:1 이상이어야 함
    private static final Color[] NORMAL_COLORS = {
        new Color(255, 0, 0),     // 빨간색 (Z 블록)
        new Color(255, 165, 0),   // 주황색 (L 블록)
        new Color(255, 255, 0),   // 노란색 (O 블록)
        new Color(0, 255, 0),     // 초록색 (S 블록)
        new Color(0, 150, 255),   // 하늘색 (I 블록)
        new Color(0, 0, 255),     // 파란색 (J 블록)
        new Color(255, 0, 255)    // 마젠타 (T 블록)
    };
    
    // 적록색맹을 위한 색상 팔레트 (파란색과 노란색 계열 중심)
    // 빨강-초록 구분이 어려우므로 파란색, 노란색, 보라색, 청록색 위주로 구성
    // 테스트 조건: 빨강과 초록 차이가 50 이상이어야 함
    private static final Color[] DEUTERANOPIA_COLORS = {
        new Color(0, 100, 255),   // 순수 파란색 (Z 블록) - 빨강=0, 초록=100
        new Color(255, 150, 0),   // 순수 주황색 (L 블록) - 빨강=255, 초록=150 (차이 105)
        new Color(150, 0, 255),   // 밝은 보라색 (L 블록) - 배경 대비 개선
        new Color(0, 180, 255),   // 밝은 파란색 (I 블록) - 대비 개선 - 빨강=0, 초록=200 (차이 200)
        new Color(200, 50, 255),  // 진한 보라색 (I 블록) - 빨강=200, 초록=50 (차이 150)
        new Color(50, 150, 255),  // 밝은 하늘색 (J 블록) - 빨강=50, 초록=150 (차이 100)
        new Color(255, 0, 200)    // 마젠타 (T 블록) - 빨강=255, 초록=0 (차이 255)
    };
    
    // 청황색맹을 위한 색상 팔레트 (빨간색과 초록색 계열 중심)
    // 파랑-노랑 구분이 어려우므로 빨강, 초록, 보라, 주황 위주로 구성
    private static final Color[] TRITANOPIA_COLORS = {
        new Color(255, 50, 50),   // 밝은 빨간색 (Z 블록) - 순수 빨강 계열
        new Color(255, 150, 50),  // 주황색 (L 블록) - 빨강 성분 높게
        new Color(100, 255, 100), // 밝은 초록색 (O 블록) - 순수 초록 계열
        new Color(200, 255, 200), // 연한 초록색 (S 블록) - 높은 명도
        new Color(200, 50, 200),  // 자주색 (I 블록) - 마젠타 계열
        new Color(255, 50, 50),   // 밝은 빨강 - 배경 대비 개선
        new Color(0, 150, 0)      // 진한 초록색 (T 블록) - 대비 강화
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
            case 1: // 적록색맹 - 더 어두운 배경으로 블록과 대비 강화
                return new Color(20, 10, 20);  // 더 어두운 배경 (대비 강화)
            case 2: // 청황색맹 - 더 어두운 배경으로 블록과 대비 강화
                return new Color(50, 30, 50);  // 어두운 와인 배경
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
            case 1: // 적록색맹 - 밝은 테두리로 대비 강화
                return new Color(150, 150, 200); // 연한 파랑 테두리
                
            case 2: // 청황색맹 - 밝은 테두리로 대비 강화  
                return new Color(205, 150, 150); // 연한 빨강 테두리
                
            case 0: // 일반 모드
            default:
                return new Color(70, 70, 120); // 회색 테두리
        }
    }
    

   
}