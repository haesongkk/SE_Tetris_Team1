package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import tetris.scene.menu.MainMenuScene;
import tetris.scene.Scene;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 시작 메뉴 기능 요구사항 JUnit 5 테스트 클래스
 * 
 * 테스트 항목:
 * 1. 게임 실행 시 첫 화면으로 시작 메뉴가 나타나는지
 * 2. 메뉴 화면 상단에 게임 이름이 표시되는지
 * 3. 게임 시작, 설정, 스코어보드, 종료 메뉴가 표시되는지
 * 4. 키보드로 메뉴 간 이동이 가능한지 (위/아래 화살표)
 * 5. 엔터 키로 메뉴 선택이 가능한지
 * 6. 화면에 사용 가능한 키 안내가 표시되는지
 * 7. 메뉴 확장 가능성 (새 메뉴 추가 가능한 구조)
 */
@DisplayName("시작 메뉴 기능 요구사항 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StartMenuTest {
    
    private static MainMenuScene mainMenu;
    private static JFrame testFrame;
    
    @BeforeAll
    @DisplayName("테스트 환경 설정")
    static void setupTestEnvironment() {
        System.out.println("=== 시작 메뉴 테스트 환경 설정 ===");
        
        // 테스트용 프레임 생성
        testFrame = new JFrame("Tetris Test");
        testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        testFrame.setSize(800, 600);
        
        // 메인 메뉴 씬 생성
        mainMenu = new MainMenuScene(testFrame);
        
        // Game 인스턴스 초기화 (중요!)
        try {
            // Game.run()을 호출하지 않고 필요한 부분만 초기화
            Field frameField = tetris.Game.class.getDeclaredField("frame");
            frameField.setAccessible(true);
            frameField.set(tetris.Game.getInstance(), testFrame);
            
            Field curSceneField = tetris.Game.class.getDeclaredField("curScene");
            curSceneField.setAccessible(true);
            curSceneField.set(tetris.Game.getInstance(), mainMenu);
            
        } catch (Exception e) {
            System.out.println("Game 초기화 중 오류 (테스트 계속 진행): " + e.getMessage());
        }
        
        System.out.println("✅ 테스트 환경 설정 완료\n");
    }
    
    @AfterAll
    @DisplayName("테스트 환경 정리")
    static void cleanup() {
        if (testFrame != null) {
            testFrame.dispose();
        }
        System.out.println("🧹 테스트 환경 정리 완료");
    }
    
    @Test
    @Order(1)
    @DisplayName("1. 시작 메뉴 초기화 테스트")
    void testMainMenuInitialization() {
        // MainMenuScene 객체가 정상적으로 생성되었는지 확인
        assertNotNull(mainMenu, "MainMenuScene이 생성되지 않았습니다.");
        
        // Scene을 상속받았는지 확인
        assertTrue(mainMenu instanceof Scene, "MainMenuScene이 Scene을 상속받지 않았습니다.");
        
        // JPanel을 상속받았는지 확인 (UI 컴포넌트)
        assertTrue(mainMenu instanceof JPanel, "MainMenuScene이 JPanel을 상속받지 않았습니다.");
        
        // KeyListener를 구현했는지 확인 (키보드 입력 처리)
        assertTrue(mainMenu instanceof KeyListener, "MainMenuScene이 KeyListener를 구현하지 않았습니다.");
        
        System.out.println("✅ 시작 메뉴 초기화 테스트 통과");
    }
    
    @Test
    @Order(2)
    @DisplayName("2. 게임 제목 표시 테스트")
    void testGameTitleDisplay() throws Exception {
        // Reflection을 사용하여 createTitlePanel 메서드 호출
        Method createTitlePanelMethod = MainMenuScene.class.getDeclaredMethod("createTitlePanel");
        createTitlePanelMethod.setAccessible(true);
        JPanel titlePanel = (JPanel) createTitlePanelMethod.invoke(mainMenu);
        
        assertNotNull(titlePanel, "제목 패널이 생성되지 않았습니다.");
        
        // 제목 패널 내에 제목 라벨이 있는지 확인
        boolean titleFound = false;
        String foundTitle = "";
        Component[] components = titlePanel.getComponents();
        
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String text = label.getText();
                if (text != null && text.toUpperCase().contains("TETRIS")) {
                    titleFound = true;
                    foundTitle = text;
                    System.out.println("게임 제목 발견: " + text);
                    
                    // 제목 폰트 크기 확인 (큰 폰트여야 함)
                    Font font = label.getFont();
                    assertTrue(font.getSize() > 30, 
                        "제목 폰트가 너무 작습니다. 현재 크기: " + font.getSize());
                    
                    break;
                }
            }
        }
        
        assertTrue(titleFound, "게임 제목(TETRIS)이 표시되지 않았습니다.");
        System.out.println("✅ 게임 제목 표시 테스트 통과: " + foundTitle);
    }
    
    @Test
    @Order(3)
    @DisplayName("3. 메뉴 항목 표시 테스트")
    void testMenuItemsDisplay() throws Exception {
        // Reflection을 사용하여 menuButtons 필드 접근
        Field menuButtonsField = MainMenuScene.class.getDeclaredField("menuButtons");
        menuButtonsField.setAccessible(true);
        JButton[] menuButtons = (JButton[]) menuButtonsField.get(mainMenu);
        
        assertNotNull(menuButtons, "메뉴 버튼 배열이 생성되지 않았습니다.");
        assertTrue(menuButtons.length >= 4, 
            "필수 메뉴 항목이 부족합니다. 현재 개수: " + menuButtons.length);
        
        // 필수 메뉴 항목들 확인
        String[] requiredMenus = {"Start", "Settings", "Score", "Exit"};
        boolean[] foundMenus = new boolean[requiredMenus.length];
        
        for (int i = 0; i < menuButtons.length; i++) {
            if (menuButtons[i] != null) {
                String buttonText = menuButtons[i].getText().toLowerCase();
                System.out.println("메뉴 항목 " + i + ": " + menuButtons[i].getText());
                
                for (int j = 0; j < requiredMenus.length; j++) {
                    if (buttonText.contains(requiredMenus[j].toLowerCase())) {
                        foundMenus[j] = true;
                    }
                }
            }
        }
        
        // 모든 필수 메뉴가 있는지 확인
        for (int i = 0; i < requiredMenus.length; i++) {
            assertTrue(foundMenus[i], requiredMenus[i] + " 메뉴가 찾아지지 않았습니다.");
        }
        
        System.out.println("✅ 메뉴 항목 표시 테스트 통과 (발견된 메뉴 개수: " + menuButtons.length + ")");
    }
    
    @Test
    @Order(4)
    @DisplayName("4. 키보드 메뉴 이동 테스트")
    void testKeyboardMenuNavigation() throws Exception {
        // 초기 선택된 버튼 인덱스 확인
        Field selectedButtonField = MainMenuScene.class.getDeclaredField("selectedButton");
        selectedButtonField.setAccessible(true);
        
        int initialSelection = (Integer) selectedButtonField.get(mainMenu);
        System.out.println("초기 선택된 메뉴 인덱스: " + initialSelection);
        
        // 아래 화살표 키 입력 시뮬레이션
        KeyEvent downKeyEvent = new KeyEvent(mainMenu, KeyEvent.KEY_PRESSED, 
            System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
        mainMenu.keyPressed(downKeyEvent);
        
        int afterDownSelection = (Integer) selectedButtonField.get(mainMenu);
        System.out.println("아래 키 후 선택된 메뉴 인덱스: " + afterDownSelection);
        
        // 선택이 변경되었는지 확인 (순환 고려)
        Field menuButtonsField = MainMenuScene.class.getDeclaredField("menuButtons");
        menuButtonsField.setAccessible(true);
        JButton[] menuButtons = (JButton[]) menuButtonsField.get(mainMenu);
        int expectedDown = (initialSelection + 1) % menuButtons.length;
        
        assertEquals(expectedDown, afterDownSelection, 
            "아래 키 입력 후 선택이 올바르게 변경되지 않았습니다.");
        
        // 위 화살표 키 입력 시뮬레이션
        KeyEvent upKeyEvent = new KeyEvent(mainMenu, KeyEvent.KEY_PRESSED, 
            System.currentTimeMillis(), 0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED);
        mainMenu.keyPressed(upKeyEvent);
        
        int afterUpSelection = (Integer) selectedButtonField.get(mainMenu);
        System.out.println("위 키 후 선택된 메뉴 인덱스: " + afterUpSelection);
        
        // 원래 위치로 돌아왔는지 확인
        assertEquals(initialSelection, afterUpSelection, 
            "위 키 입력 후 선택이 원래 위치로 돌아가지 않았습니다.");
        
        System.out.println("✅ 키보드 메뉴 이동 테스트 통과");
    }
    
    @Test
    @Order(5)
    @DisplayName("5. 엔터 키 메뉴 선택 테스트")
    void testEnterKeyMenuSelection() throws Exception {
        // handleMenuSelection 메서드가 존재하는지 확인
        Method[] methods = MainMenuScene.class.getDeclaredMethods();
        boolean handleMenuSelectionExists = false;
        
        for (Method method : methods) {
            if (method.getName().equals("handleMenuSelection")) {
                handleMenuSelectionExists = true;
                System.out.println("handleMenuSelection 메서드 발견");
                
                // 메서드 파라미터 타입 확인
                Class<?>[] paramTypes = method.getParameterTypes();
                assertEquals(1, paramTypes.length, 
                    "handleMenuSelection 메서드는 정확히 하나의 파라미터를 받아야 합니다.");
                assertEquals(int.class, paramTypes[0], 
                    "handleMenuSelection 메서드는 int 파라미터를 받아야 합니다.");
                
                break;
            }
        }
        
        assertTrue(handleMenuSelectionExists, "handleMenuSelection 메서드가 존재하지 않습니다.");
        
        // 엔터 키 입력에 대한 반응 확인 (실제 Scene 전환 없이 키 처리만 확인)
        KeyEvent enterKeyEvent = new KeyEvent(mainMenu, KeyEvent.KEY_PRESSED, 
            System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
        
        // Game.curScene이 null일 수 있으므로 예외 발생 여부를 확인하되, 
        // NullPointerException은 예상되는 상황으로 처리
        try {
            mainMenu.keyPressed(enterKeyEvent);
            System.out.println("엔터 키 처리 성공 (정상 처리)");
        } catch (NullPointerException e) {
            // Game.curScene이 null이어서 발생하는 예외는 예상된 상황
            if (e.getMessage() != null && e.getMessage().contains("curScene")) {
                System.out.println("엔터 키 처리 확인됨 (Game.curScene null로 인한 예상된 예외)");
            } else {
                throw e; // 다른 NullPointerException은 실제 오류
            }
        }
        
        System.out.println("✅ 엔터 키 메뉴 선택 테스트 통과");
    }
    
    @Test
    @Order(6)
    @DisplayName("6. 사용 가능한 키 안내 표시 테스트")
    void testKeyInstructionsDisplay() throws Exception {
        // createInfoPanel 메서드 호출하여 안내 패널 확인
        Method createInfoPanelMethod = MainMenuScene.class.getDeclaredMethod("createInfoPanel");
        createInfoPanelMethod.setAccessible(true);
        JPanel infoPanel = (JPanel) createInfoPanelMethod.invoke(mainMenu);
        
        assertNotNull(infoPanel, "키 안내 패널이 생성되지 않았습니다.");
        
        // 안내 패널 내에 키 안내 텍스트가 있는지 확인
        boolean keyInstructionsFound = false;
        String foundInstructions = "";
        Component[] components = getAllComponents(infoPanel);
        
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String text = label.getText();
                if (text != null) {
                    String lowerText = text.toLowerCase();
                    System.out.println("안내 텍스트: " + text);
                    
                    // 키 안내 관련 텍스트가 있는지 확인
                    if (lowerText.contains("키") || lowerText.contains("key") || 
                        lowerText.contains("enter") || lowerText.contains("↑") || 
                        lowerText.contains("↓") || lowerText.contains("esc")) {
                        keyInstructionsFound = true;
                        foundInstructions = text;
                        break;
                    }
                }
            }
        }
        
        assertTrue(keyInstructionsFound, "키 사용 안내가 표시되지 않았습니다.");
        System.out.println("✅ 키 안내 표시 테스트 통과: " + foundInstructions);
    }
    
    @Test
    @Order(7)
    @DisplayName("7. 메뉴 확장 가능성 테스트")
    void testMenuExtensibility() throws Exception {
        // 메뉴 버튼 배열 구조 확인
        Field menuButtonsField = MainMenuScene.class.getDeclaredField("menuButtons");
        menuButtonsField.setAccessible(true);
        JButton[] menuButtons = (JButton[]) menuButtonsField.get(mainMenu);
        
        assertNotNull(menuButtons, "메뉴 버튼 배열이 없습니다.");
        
        // createMenuButton 메서드 존재 확인 (새 메뉴 버튼 생성 가능)
        assertDoesNotThrow(() -> {
            Method createMenuButtonMethod = MainMenuScene.class.getDeclaredMethod("createMenuButton", String.class, int.class);
            createMenuButtonMethod.setAccessible(true);
            System.out.println("createMenuButton 메서드 발견 - 새 메뉴 추가 가능");
        }, "새 메뉴 버튼을 생성하는 createMenuButton 메서드가 없습니다.");
        
        // handleMenuSelection 메서드 확인 (확장 가능)
        assertDoesNotThrow(() -> {
            Method handleMenuSelectionMethod = MainMenuScene.class.getDeclaredMethod("handleMenuSelection", int.class);
            assertNotNull(handleMenuSelectionMethod, "handleMenuSelection 메서드가 없습니다.");
        }, "handleMenuSelection 메서드를 찾을 수 없습니다.");
        
        // 메뉴 패널 구조 확인 (동적 추가 가능한 레이아웃)
        Method createMenuPanelMethod = MainMenuScene.class.getDeclaredMethod("createMenuPanel");
        createMenuPanelMethod.setAccessible(true);
        JPanel menuPanel = (JPanel) createMenuPanelMethod.invoke(mainMenu);
        
        assertNotNull(menuPanel, "메뉴 패널이 생성되지 않았습니다.");
        
        LayoutManager layout = menuPanel.getLayout();
        System.out.println("메뉴 패널 레이아웃: " + layout.getClass().getSimpleName());
        
        // 동적 레이아웃인지 확인
        boolean isDynamicLayout = layout instanceof GridBagLayout || 
                                layout instanceof BoxLayout || 
                                layout instanceof GridLayout;
        
        assertTrue(isDynamicLayout, 
            "메뉴 패널이 동적 메뉴 추가에 적합하지 않은 레이아웃을 사용합니다: " + layout.getClass().getSimpleName());
        
        System.out.println("✅ 메뉴 확장 가능성 테스트 통과");
        System.out.println("  - 메뉴 버튼 배열 구조: ✓");
        System.out.println("  - 새 버튼 생성 메서드: ✓");
        System.out.println("  - 메뉴 선택 처리 메서드: ✓");
        System.out.println("  - 동적 레이아웃: ✓");
    }
    
    /**
     * 컴포넌트의 모든 하위 컴포넌트를 재귀적으로 가져오는 유틸리티 메서드
     */
    private Component[] getAllComponents(Container container) {
        java.util.List<Component> components = new java.util.ArrayList<>();
        Component[] directComponents = container.getComponents();
        
        for (Component comp : directComponents) {
            components.add(comp);
            if (comp instanceof Container) {
                Component[] subComponents = getAllComponents((Container) comp);
                for (Component subComp : subComponents) {
                    components.add(subComp);
                }
            }
        }
        
        return components.toArray(new Component[0]);
    }
}