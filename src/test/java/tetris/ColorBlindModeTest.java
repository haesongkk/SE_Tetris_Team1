package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import tetris.scene.menu.SettingsScene;
import tetris.scene.game.GameScene;
import tetris.scene.game.blocks.*;
import tetris.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 색각이상자 지원 기능 요구사항 테스트 클래스
 * 
 * 테스트 항목:
 * 1. 색맹 모드 설정 기능 존재 확인
 * 2. 적록색맹 모드 색상 팔레트 검증
 * 3. 청황색맹 모드 색상 팔레트 검증
 * 4. 블록별 색상 구분 가능성 테스트
 * 5. 색맹 모드별 배경색 및 테두리색 적절성 테스트
 * 6. 설정 저장 및 적용 기능 테스트
 * 7. 색상 대비 및 접근성 테스트
 */
@DisplayName("색각이상자 지원 기능 요구사항 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ColorBlindModeTest {

    private static JFrame testFrame;
    private static GameSettings gameSettings;
    private static Timer dialogCloser; // 다이얼로그 자동 닫기용 타이머

    @BeforeAll
    @DisplayName("테스트 환경 설정")
    static void setupTestEnvironment() {
        System.out.println("=== 색각이상자 지원 기능 테스트 환경 설정 ===");

        // 헤드리스 환경 체크
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("⚠️ 헤드리스 환경: GUI 테스트 제한됨");
            return;
        }

        try {
            // 다이얼로그 자동 닫기 타이머 설정
            setupDialogCloser();
            
            // 테스트용 프레임 생성
            testFrame = new JFrame("ColorBlind Mode Test");
            testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            testFrame.setSize(800, 600);

            // GameSettings 인스턴스 초기화
            gameSettings = GameSettings.getInstance();

            System.out.println("✅ 색각이상자 지원 기능 테스트 환경 설정 완료");
        } catch (Exception e) {
            System.err.println("❌ 테스트 환경 설정 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @AfterAll
    @DisplayName("테스트 환경 정리")
    static void tearDownTestEnvironment() {
        System.out.println("=== 색각이상자 지원 기능 테스트 환경 정리 ===");
        
        // 다이얼로그 자동 닫기 타이머 완전 정리
        cleanupDialogCloser();
        
        // 모든 열린 윈도우 정리
        cleanupAllWindows();
        
        if (testFrame != null) {
            testFrame.dispose();
            testFrame = null;
        }
        
        // 게임 설정 초기화
        if (gameSettings != null) {
            gameSettings.setColorBlindMode(0); // 일반 모드로 복원
        }
        
        System.out.println("✅ 테스트 환경 정리 완료");
        
        // 최종 강제 정리
        forceSystemCleanup();
    }

    @Test
    @Order(1)
    @DisplayName("1. 색맹 모드 설정 기능 존재 확인")
    void testColorBlindModeSettingExists() {
        System.out.println("=== 1. 색맹 모드 설정 기능 존재 확인 ===");

        assertDoesNotThrow(() -> {
            // GameSettings에 색맹 모드 관련 메서드 존재 확인
            Method getColorBlindModeMethod = GameSettings.class.getMethod("getColorBlindMode");
            assertNotNull(getColorBlindModeMethod, "getColorBlindMode 메서드가 존재해야 합니다.");

            Method setColorBlindModeMethod = GameSettings.class.getMethod("setColorBlindMode", int.class);
            assertNotNull(setColorBlindModeMethod, "setColorBlindMode 메서드가 존재해야 합니다.");

            // 기본값 확인 (일반 모드)
            int defaultMode = gameSettings.getColorBlindMode();
            assertTrue(defaultMode >= 0 && defaultMode <= 2, 
                "색맹 모드는 0(일반), 1(적록색맹), 2(청황색맹) 범위여야 합니다. 현재: " + defaultMode);

            System.out.println("현재 색맹 모드: " + defaultMode);
            System.out.println("✅ 색맹 모드 설정 기능 확인 완료");

        }, "색맹 모드 설정 기능 확인 중 예외가 발생해서는 안 됩니다.");

        System.out.println("✅ 색맹 모드 설정 기능 존재 확인 테스트 통과");
    }

    @Test
    @Order(2)
    @DisplayName("2. 적록색맹 모드 색상 팔레트 검증")
    void testDeuteranopiaColorPalette() {
        System.out.println("=== 2. 적록색맹 모드 색상 팔레트 검증 ===");

        assertDoesNotThrow(() -> {
            // 적록색맹 모드 설정
            gameSettings.setColorBlindMode(1);
            assertEquals(1, gameSettings.getColorBlindMode(), "적록색맹 모드가 설정되어야 합니다.");

            // 각 블록별 색상 확인
            for (int blockType = 0; blockType < 7; blockType++) {
                Color blockColor = ColorBlindHelper.getBlockColor(blockType, 1);
                assertNotNull(blockColor, "블록 " + blockType + "의 색상이 null이 아니어야 합니다.");

                // 적록색맹에게 피해야 할 색상 조합 확인 (빨강-초록 조합 없음)
                validateDeuteranopiaColor(blockColor, blockType);
            }

            // 배경색과 테두리색 확인
            Color backgroundColor = ColorBlindHelper.getBackgroundColor(1);
            Color borderColor = ColorBlindHelper.getBorderColor(1);
            
            assertNotNull(backgroundColor, "적록색맹 모드 배경색이 null이 아니어야 합니다.");
            assertNotNull(borderColor, "적록색맹 모드 테두리색이 null이 아니어야 합니다.");

            // 색상 대비 확인
            validateColorContrast(backgroundColor, borderColor);

            System.out.println("✅ 적록색맹 모드 색상 팔레트 검증 완료");

        }, "적록색맹 모드 색상 팔레트 검증 중 예외가 발생해서는 안 됩니다.");

        System.out.println("✅ 적록색맹 모드 색상 팔레트 검증 테스트 통과");
    }

    @Test
    @Order(3)
    @DisplayName("3. 청황색맹 모드 색상 팔레트 검증")
    void testTritanopiaColorPalette() {
        System.out.println("=== 3. 청황색맹 모드 색상 팔레트 검증 ===");

        assertDoesNotThrow(() -> {
            // 청황색맹 모드 설정
            gameSettings.setColorBlindMode(2);
            assertEquals(2, gameSettings.getColorBlindMode(), "청황색맹 모드가 설정되어야 합니다.");

            // 각 블록별 색상 확인
            for (int blockType = 0; blockType < 7; blockType++) {
                Color blockColor = ColorBlindHelper.getBlockColor(blockType, 2);
                assertNotNull(blockColor, "블록 " + blockType + "의 색상이 null이 아니어야 합니다.");

                // 청황색맹에게 피해야 할 색상 조합 확인 (파랑-녹색, 노랑-연두 조합 없음)
                validateTritanopiaColor(blockColor, blockType);
            }

            // 배경색과 테두리색 확인
            Color backgroundColor = ColorBlindHelper.getBackgroundColor(2);
            Color borderColor = ColorBlindHelper.getBorderColor(2);
            
            assertNotNull(backgroundColor, "청황색맹 모드 배경색이 null이 아니어야 합니다.");
            assertNotNull(borderColor, "청황색맹 모드 테두리색이 null이 아니어야 합니다.");

            // 색상 대비 확인
            validateColorContrast(backgroundColor, borderColor);

            System.out.println("✅ 청황색맹 모드 색상 팔레트 검증 완료");

        }, "청황색맹 모드 색상 팔레트 검증 중 예외가 발생해서는 안 됩니다.");

        System.out.println("✅ 청황색맹 모드 색상 팔레트 검증 테스트 통과");
    }

    @Test
    @Order(4)
    @DisplayName("4. 블록별 색상 구분 가능성 테스트")
    void testBlockColorDistinction() {
        System.out.println("=== 4. 블록별 색상 구분 가능성 테스트 ===");

        assertDoesNotThrow(() -> {
            for (int colorBlindMode = 0; colorBlindMode <= 2; colorBlindMode++) {
                gameSettings.setColorBlindMode(colorBlindMode);
                String modeName = getColorBlindModeName(colorBlindMode);
                
                System.out.println("색맹 모드 " + colorBlindMode + " (" + modeName + ") 테스트 중...");

                // 모든 블록 색상 수집
                Color[] blockColors = new Color[7];
                for (int i = 0; i < 7; i++) {
                    blockColors[i] = ColorBlindHelper.getBlockColor(i, colorBlindMode);
                }

                // 블록 간 색상 구분 가능성 확인
                validateBlockDistinction(blockColors, modeName);
            }

            System.out.println("✅ 모든 색맹 모드에서 블록 구분 가능성 확인 완료");

        }, "블록별 색상 구분 가능성 테스트 중 예외가 발생해서는 안 됩니다.");

        System.out.println("✅ 블록별 색상 구분 가능성 테스트 통과");
    }

    @Test
    @Order(5)
    @DisplayName("5. 색맹 모드별 테마 적용 테스트")
    void testThemeApplicationByColorBlindMode() {
        System.out.println("=== 5. 색맹 모드별 테마 적용 테스트 ===");

        assertDoesNotThrow(() -> {
            if (testFrame == null) {
                System.out.println("⚠️ 헤드리스 환경에서는 GUI 테스트를 건너뜁니다.");
                return;
            }

            for (int colorBlindMode = 0; colorBlindMode <= 2; colorBlindMode++) {
                gameSettings.setColorBlindMode(colorBlindMode);
                String modeName = getColorBlindModeName(colorBlindMode);
                
                System.out.println("색맹 모드 " + colorBlindMode + " (" + modeName + ") 테마 적용 테스트...");

                // Theme 클래스를 통한 색상 가져오기 테스트
                Color gameBackground = Theme.BG();
                Color gameBorder = Theme.Border();
                
                assertNotNull(gameBackground, modeName + " 모드 게임 배경색이 null이 아니어야 합니다.");
                assertNotNull(gameBorder, modeName + " 모드 게임 테두리색이 null이 아니어야 합니다.");

                // 블록 색상 테스트 (블록 타입별)
                char[] blockTypes = {'Z', 'L', 'O', 'S', 'I', 'J', 'T'};
                for (char blockType : blockTypes) {
                    Color blockColor = Theme.Block(blockType);
                    assertNotNull(blockColor, modeName + " 모드 블록 " + blockType + " 색상이 null이 아니어야 합니다.");
                }

                System.out.println(modeName + " 모드 테마 적용 확인 완료");
            }

        }, "색맹 모드별 테마 적용 테스트 중 예외가 발생해서는 안 됩니다.");

        System.out.println("✅ 색맹 모드별 테마 적용 테스트 통과");
    }

    @Test
    @Order(6)
    @DisplayName("6. 설정 메뉴에서 색맹 모드 선택 기능 테스트")
    void testColorBlindModeSettingsMenu() {
        System.out.println("=== 6. 설정 메뉴에서 색맹 모드 선택 기능 테스트 ===");

        assertDoesNotThrow(() -> {
            if (testFrame == null) {
                System.out.println("⚠️ 헤드리스 환경에서는 GUI 테스트를 건너뜁니다.");
                return;
            }

            // SettingsScene 생성 및 초기화
            SettingsScene settingsScene = new SettingsScene(testFrame);
            assertNotNull(settingsScene, "SettingsScene이 생성되어야 합니다.");

            // 색맹 모드 콤보박스 존재 확인
            Field colorBlindModeComboField = SettingsScene.class.getDeclaredField("colorBlindModeCombo");
            colorBlindModeComboField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            JComboBox<String> colorBlindModeCombo = (JComboBox<String>) colorBlindModeComboField.get(settingsScene);
            
            if (colorBlindModeCombo != null) {
                // 콤보박스 항목 확인
                assertTrue(colorBlindModeCombo.getItemCount() >= 3, 
                    "색맹 모드 콤보박스에 최소 3개 항목이 있어야 합니다 (일반, 적록색맹, 청황색맹)");

                // 각 모드 선택 테스트
                for (int i = 0; i < Math.min(3, colorBlindModeCombo.getItemCount()); i++) {
                    colorBlindModeCombo.setSelectedIndex(i);
                    assertEquals(i, colorBlindModeCombo.getSelectedIndex(), 
                        "색맹 모드 " + i + "가 선택되어야 합니다.");
                    
                    String selectedItem = (String) colorBlindModeCombo.getSelectedItem();
                    assertNotNull(selectedItem, "선택된 항목이 null이 아니어야 합니다.");
                    System.out.println("색맹 모드 " + i + ": " + selectedItem);
                }

                System.out.println("✅ 설정 메뉴 색맹 모드 선택 기능 확인 완료");
            } else {
                System.out.println("⚠️ 색맹 모드 콤보박스를 찾을 수 없습니다.");
            }

        }, "설정 메뉴 색맹 모드 선택 기능 테스트 중 예외가 발생해서는 안 됩니다.");

        System.out.println("✅ 설정 메뉴에서 색맹 모드 선택 기능 테스트 통과");
    }

    @Test
    @Order(7)
    @DisplayName("7. 색상 접근성 및 명도 대비 테스트")
    void testColorAccessibilityAndContrast() {
        System.out.println("=== 7. 색상 접근성 및 명도 대비 테스트 ===");

        assertDoesNotThrow(() -> {
            for (int colorBlindMode = 1; colorBlindMode <= 2; colorBlindMode++) {
                gameSettings.setColorBlindMode(colorBlindMode);
                String modeName = getColorBlindModeName(colorBlindMode);
                
                System.out.println(modeName + " 모드 접근성 테스트 중...");

                // 배경색과 각 블록 색상 간 대비 확인
                Color backgroundColor = ColorBlindHelper.getBackgroundColor(colorBlindMode);
                
                for (int blockType = 0; blockType < 7; blockType++) {
                    Color blockColor = ColorBlindHelper.getBlockColor(blockType, colorBlindMode);
                    
                    // 명도 대비 계산 및 검증
                    double contrastRatio = calculateContrastRatio(backgroundColor, blockColor);
                    
                    // WCAG 2.1 AA 기준 (4.5:1 이상)을 완화하여 테트리스 게임에 맞게 3.0:1 이상으로 설정
                    assertTrue(contrastRatio >= 3.0, 
                        modeName + " 모드에서 블록 " + blockType + "과 배경색의 대비비가 부족합니다. " +
                        "현재: " + String.format("%.2f", contrastRatio) + ":1 (최소 3.0:1 필요)");
                }

                // 권장 색상 사용 확인
                validateRecommendedColors(colorBlindMode);

                System.out.println(modeName + " 모드 접근성 확인 완료");
            }

        }, "색상 접근성 및 명도 대비 테스트 중 예외가 발생해서는 안 됩니다.");

        System.out.println("✅ 색상 접근성 및 명도 대비 테스트 통과");
    }

    // ==================== 헬퍼 메서드들 ====================

    /**
     * 적록색맹에게 적합한 색상인지 검증
     */
    private void validateDeuteranopiaColor(Color color, int blockType) {
        // 적록색맹에게 피해야 할 색상 범위 확인
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        
        // 빨강과 초록이 비슷한 수준이면 구분이 어려움
        boolean isProblematicRedGreen = Math.abs(red - green) < 50 && Math.max(red, green) > 100;
        
        assertFalse(isProblematicRedGreen, 
            "블록 " + blockType + "의 색상이 적록색맹에게 부적합합니다. " +
            "빨강(" + red + ")과 초록(" + green + ")이 너무 비슷합니다.");
            
        // 파란색, 노란색, 주황색 계열 사용 권장
        boolean isRecommendedColor = blue > 150 || // 파란색 계열
                                   (red > 200 && green > 200 && blue < 100) || // 노란색 계열
                                   (red > 200 && green > 100 && blue < 100); // 주황색 계열
        
        System.out.println("블록 " + blockType + " 색상: RGB(" + red + "," + green + "," + blue + ") - " +
                         (isRecommendedColor ? "권장 색상" : "기타 색상"));
    }

    /**
     * 청황색맹에게 적합한 색상인지 검증
     */
    private void validateTritanopiaColor(Color color, int blockType) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        
        // 파랑과 녹색이 비슷한 수준이면 구분이 어려움
        boolean isProblematicBlueGreen = Math.abs(blue - green) < 50 && Math.max(blue, green) > 100;
        
        assertFalse(isProblematicBlueGreen, 
            "블록 " + blockType + "의 색상이 청황색맹에게 부적합합니다. " +
            "파랑(" + blue + ")과 초록(" + green + ")이 너무 비슷합니다.");
            
        // 빨강, 초록, 보라 계열 사용 권장
        boolean isRecommendedColor = red > 150 || // 빨간색 계열
                                   green > 150 || // 초록색 계열
                                   (red > 100 && blue > 100 && green < 100); // 보라색 계열
        
        System.out.println("블록 " + blockType + " 색상: RGB(" + red + "," + green + "," + blue + ") - " +
                         (isRecommendedColor ? "권장 색상" : "기타 색상"));
    }

    /**
     * 색상 대비비 계산 (WCAG 2.1 기준)
     */
    private double calculateContrastRatio(Color background, Color foreground) {
        double bgLuminance = getRelativeLuminance(background);
        double fgLuminance = getRelativeLuminance(foreground);
        
        double lighter = Math.max(bgLuminance, fgLuminance);
        double darker = Math.min(bgLuminance, fgLuminance);
        
        return (lighter + 0.05) / (darker + 0.05);
    }

    /**
     * 상대 휘도 계산
     */
    private double getRelativeLuminance(Color color) {
        double[] rgb = {color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0};
        
        for (int i = 0; i < rgb.length; i++) {
            if (rgb[i] <= 0.03928) {
                rgb[i] = rgb[i] / 12.92;
            } else {
                rgb[i] = Math.pow((rgb[i] + 0.055) / 1.055, 2.4);
            }
        }
        
        return 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2];
    }

    /**
     * 두 색상 간 대비 확인
     */
    private void validateColorContrast(Color color1, Color color2) {
        double contrastRatio = calculateContrastRatio(color1, color2);
        assertTrue(contrastRatio >= 2.0, 
            "색상 대비가 부족합니다. 현재: " + String.format("%.2f", contrastRatio) + ":1");
    }

    /**
     * 블록 간 색상 구분 가능성 검증
     */
    private void validateBlockDistinction(Color[] blockColors, String modeName) {
        for (int i = 0; i < blockColors.length; i++) {
            for (int j = i + 1; j < blockColors.length; j++) {
                double contrastRatio = calculateContrastRatio(blockColors[i], blockColors[j]);
                
                assertTrue(contrastRatio >= 1.5, 
                    modeName + " 모드에서 블록 " + i + "과 블록 " + j + "의 구분이 어렵습니다. " +
                    "대비비: " + String.format("%.2f", contrastRatio) + ":1");
            }
        }
        System.out.println(modeName + " 모드 블록 간 구분 가능성 확인 완료");
    }

    /**
     * 권장 색상 사용 확인
     */
    private void validateRecommendedColors(int colorBlindMode) {
        String modeName = getColorBlindModeName(colorBlindMode);
        
        for (int blockType = 0; blockType < 7; blockType++) {
            Color color = ColorBlindHelper.getBlockColor(blockType, colorBlindMode);
            
            if (colorBlindMode == 1) { // 적록색맹
                // 파랑, 노랑, 주황, 청록 등 권장
                validateDeuteranopiaColor(color, blockType);
            } else if (colorBlindMode == 2) { // 청황색맹
                // 보라, 주황, 붉은색 등 권장
                validateTritanopiaColor(color, blockType);
            }
        }
        
        System.out.println(modeName + " 모드 권장 색상 사용 확인 완료");
    }

    /**
     * 색맹 모드명 반환
     */
    private String getColorBlindModeName(int mode) {
        switch (mode) {
            case 0: return "일반 모드";
            case 1: return "적록색맹 모드";
            case 2: return "청황색맹 모드";
            default: return "알 수 없는 모드";
        }
    }

    // ==================== 다이얼로그 자동 닫기 관련 메서드들 ====================

    /**
     * 모달 다이얼로그 자동 닫기 타이머를 설정합니다.
     */
    private static void setupDialogCloser() {
        dialogCloser = new Timer(300, e -> {
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                if (window instanceof JDialog) {
                    JDialog dialog = (JDialog) window;
                    if (dialog.isModal() && dialog.isVisible()) {
                        System.out.println("🔄 ColorBlindModeTest용 모달 다이얼로그 자동 닫기: " + dialog.getTitle());
                        
                        Component[] components = dialog.getContentPane().getComponents();
                        JButton firstButton = findFirstButton(components);
                        if (firstButton != null) {
                            firstButton.doClick();
                            System.out.println("✅ 첫 번째 버튼 클릭함: " + firstButton.getText());
                        } else {
                            dialog.dispose();
                            System.out.println("✅ 다이얼로그 강제 닫기 완료");
                        }
                    }
                }
            }
        });
        
        dialogCloser.setRepeats(true);
        dialogCloser.start();
        System.out.println("🔧 ColorBlindModeTest용 다이얼로그 자동 닫기 타이머 시작됨");
    }

    /**
     * 컴포넌트 배열에서 첫 번째 JButton을 재귀적으로 찾습니다.
     */
    private static JButton findFirstButton(Component[] components) {
        for (Component comp : components) {
            if (comp instanceof JButton) {
                return (JButton) comp;
            }
            if (comp instanceof Container) {
                Container container = (Container) comp;
                JButton button = findFirstButton(container.getComponents());
                if (button != null) {
                    return button;
                }
            }
        }
        return null;
    }

    /**
     * 다이얼로그 자동 닫기 타이머를 완전히 정리합니다.
     */
    private static void cleanupDialogCloser() {
        if (dialogCloser != null) {
            try {
                if (dialogCloser.isRunning()) {
                    dialogCloser.stop();
                    System.out.println("🔧 ColorBlindModeTest 다이얼로그 자동 닫기 타이머 중지됨");
                }
                
                java.awt.event.ActionListener[] listeners = dialogCloser.getActionListeners();
                for (java.awt.event.ActionListener listener : listeners) {
                    dialogCloser.removeActionListener(listener);
                }
                
                dialogCloser = null;
                System.out.println("✅ ColorBlindModeTest 다이얼로그 자동 닫기 타이머 완전 정리됨");
            } catch (Exception e) {
                System.out.println("ColorBlindModeTest 타이머 정리 중 오류 (무시): " + e.getMessage());
                dialogCloser = null;
            }
        }
        
        System.runFinalization();
        System.gc();
    }

    /**
     * 모든 열린 윈도우를 정리합니다.
     */
    private static void cleanupAllWindows() {
        try {
            Window[] windows = Window.getWindows();
            int closedCount = 0;
            
            for (Window window : windows) {
                if (window != null && window.isDisplayable()) {
                    if (window instanceof JDialog || window instanceof JFrame) {
                        clearWindowListeners(window);
                        window.setVisible(false);
                        window.dispose();
                        closedCount++;
                    }
                }
            }
            
            if (closedCount > 0) {
                System.out.println("🔧 ColorBlindModeTest에서 " + closedCount + "개의 윈도우 정리됨");
            }
            
            try {
                java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
                    new java.awt.event.WindowEvent(new JFrame(), java.awt.event.WindowEvent.WINDOW_CLOSING)
                );
            } catch (Exception e) {
                // 무시
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
        } catch (Exception e) {
            System.out.println("ColorBlindModeTest 윈도우 정리 중 오류 (무시): " + e.getMessage());
        }
    }

    /**
     * 윈도우의 모든 이벤트 리스너를 제거합니다.
     */
    private static void clearWindowListeners(Window window) {
        try {
            java.awt.event.WindowListener[] windowListeners = window.getWindowListeners();
            for (java.awt.event.WindowListener listener : windowListeners) {
                window.removeWindowListener(listener);
            }
            
            java.awt.event.ComponentListener[] componentListeners = window.getComponentListeners();
            for (java.awt.event.ComponentListener listener : componentListeners) {
                window.removeComponentListener(listener);
            }
            
            if (window instanceof Container) {
                Container container = (Container) window;
                java.awt.event.KeyListener[] keyListeners = container.getKeyListeners();
                for (java.awt.event.KeyListener listener : keyListeners) {
                    container.removeKeyListener(listener);
                }
            }
        } catch (Exception e) {
            // 무시
        }
    }

    /**
     * 시스템 레벨에서 강제 정리를 수행합니다.
     */
    private static void forceSystemCleanup() {
        try {
            System.out.println("🔧 ColorBlindModeTest 시스템 강제 정리 시작...");
            
            // 1. 모든 Timer 완전 중지
            try {
                javax.swing.Timer.setLogTimers(false);
                java.lang.reflect.Field timersField = javax.swing.Timer.class.getDeclaredField("queue");
                timersField.setAccessible(true);
                Object timerQueue = timersField.get(null);
                if (timerQueue != null) {
                    java.lang.reflect.Method stopMethod = timerQueue.getClass().getDeclaredMethod("stop");
                    stopMethod.setAccessible(true);
                    stopMethod.invoke(timerQueue);
                    System.out.println("🧹 Swing Timer 큐 완전 중지됨");
                }
            } catch (Exception e) {
                // Reflection 실패는 무시
            }
            
            // 2. EventQueue 정리
            try {
                java.awt.EventQueue eventQueue = java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue();
                while (eventQueue.peekEvent() != null) {
                    eventQueue.getNextEvent();
                }
            } catch (Exception e) {
                // 무시
            }
            
            // 3. 스레드 정리
            ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
            ThreadGroup parentGroup;
            while ((parentGroup = rootGroup.getParent()) != null) {
                rootGroup = parentGroup;
            }
            
            Thread[] threads = new Thread[rootGroup.activeCount()];
            int count = rootGroup.enumerate(threads);
            
            for (int i = 0; i < count; i++) {
                Thread thread = threads[i];
                if (thread != null && !thread.isDaemon() && thread != Thread.currentThread()) {
                    String threadName = thread.getName();
                    if (threadName.contains("AWT-EventQueue") || 
                        threadName.contains("TimerQueue") ||
                        threadName.contains("Swing-Timer")) {
                        System.out.println("⚠️ ColorBlindModeTest 활성 GUI 스레드 감지: " + threadName);
                        thread.interrupt();
                    }
                }
            }
            
            // 4. 메모리 정리
            System.runFinalization();
            System.gc();
            Thread.sleep(100);
            System.gc();
            
            System.out.println("✅ ColorBlindModeTest 시스템 강제 정리 완료");
            
        } catch (Exception e) {
            System.out.println("ColorBlindModeTest 시스템 정리 중 오류 (무시): " + e.getMessage());
        }
    }
}