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
    // 이미지 기반 색상: ORANGE, SKY BLUE, BLUISH GREEN, YELLOW, BLUE, VERMILION, REDDISH PURPLE
    private static final Color[] DEUTERANOPIA_COLORS = {
        new Color(230, 159, 0),   // ORANGE (Z 블록)
        new Color(86, 180, 233),  // SKY BLUE (L 블록)
        new Color(0, 158, 115),   // BLUISH GREEN (O 블록)
        new Color(240, 228, 66),  // YELLOW (S 블록)
        new Color(0, 114, 178),   // BLUE (I 블록)
        new Color(213, 94, 0),    // VERMILION (J 블록)
        new Color(204, 121, 167)  // REDDISH PURPLE (T 블록)
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
    
    /**
     * 색맹 모드에서 블록에 패턴을 그립니다.
     * 각 블록 타입마다 다른 패턴을 사용하여 색상 외에도 구분할 수 있게 합니다.
     * 
     * @param g2d Graphics2D 객체
     * @param blockType 블록 타입 (0-6)
     * @param x 블록의 x 좌표
     * @param y 블록의 y 좌표
     * @param size 블록의 크기
     * @param colorBlindMode 색맹 모드 (0: 패턴 없음, 1-2: 패턴 있음)
     * @param blockColor 블록의 색상 (패턴 색상을 자동으로 결정하기 위해 사용)
     */
    public static void drawBlockPattern(java.awt.Graphics2D g2d, int blockType, int x, int y, int size, int colorBlindMode, java.awt.Color blockColor) {
        // 일반 모드에서는 패턴을 그리지 않음
        if (colorBlindMode == 0) {
            return;
        }
        
        // 블록 색상의 밝기를 계산하여 패턴 색상 결정
        // 밝은 블록에는 어두운 패턴, 어두운 블록에는 밝은 패턴
        int brightness = (blockColor.getRed() + blockColor.getGreen() + blockColor.getBlue()) / 3;
        java.awt.Color patternColor;
        if (brightness > 150) {
            // 밝은 블록 - 검은색 패턴 사용 (약간 투명)
            patternColor = new java.awt.Color(0, 0, 0, 180);
        } else {
            // 어두운 블록 - 흰색 패턴 사용 (약간 투명)
            patternColor = new java.awt.Color(255, 255, 255, 180);
        }
        
        g2d.setColor(patternColor);
        g2d.setStroke(new java.awt.BasicStroke(1.5f));
        
        int padding = 2; // 여백
        int innerX = x + padding;
        int innerY = y + padding;
        int innerSize = size - padding * 2;
        
        switch (blockType % 7) {
            case 0: // Z 블록 - 대각선 줄무늬 (왼쪽 위에서 오른쪽 아래)
                for (int i = 0; i < innerSize; i += 4) {
                    g2d.drawLine(innerX + i, innerY, innerX, innerY + i);
                    if (i < innerSize) {
                        g2d.drawLine(innerX + innerSize, innerY + i, innerX + i, innerY + innerSize);
                    }
                }
                break;
                
            case 1: // L 블록 - 가로 줄무늬
                for (int i = 0; i < innerSize; i += 4) {
                    g2d.drawLine(innerX, innerY + i, innerX + innerSize, innerY + i);
                }
                break;
                
            case 2: // O 블록 - 점 패턴
                for (int i = 3; i < innerSize; i += 6) {
                    for (int j = 3; j < innerSize; j += 6) {
                        g2d.fillOval(innerX + i - 1, innerY + j - 1, 2, 2);
                    }
                }
                break;
                
            case 3: // S 블록 - 대각선 줄무늬 (오른쪽 위에서 왼쪽 아래)
                for (int i = 0; i < innerSize; i += 4) {
                    g2d.drawLine(innerX + innerSize - i, innerY, innerX + innerSize, innerY + i);
                    if (i < innerSize) {
                        g2d.drawLine(innerX, innerY + i, innerX + innerSize - i, innerY + innerSize);
                    }
                }
                break;
                
            case 4: // I 블록 - 세로 줄무늬
                for (int i = 0; i < innerSize; i += 4) {
                    g2d.drawLine(innerX + i, innerY, innerX + i, innerY + innerSize);
                }
                break;
                
            case 5: // J 블록 - 격자 패턴
                for (int i = 0; i < innerSize; i += 6) {
                    g2d.drawLine(innerX, innerY + i, innerX + innerSize, innerY + i);
                    g2d.drawLine(innerX + i, innerY, innerX + i, innerY + innerSize);
                }
                break;
                
            case 6: // T 블록 - X 패턴
                g2d.drawLine(innerX, innerY, innerX + innerSize, innerY + innerSize);
                g2d.drawLine(innerX + innerSize, innerY, innerX, innerY + innerSize);
                break;
        }
    }
    

   
}