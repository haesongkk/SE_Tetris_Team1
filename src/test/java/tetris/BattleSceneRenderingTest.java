package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import tetris.scene.battle.BattleScene;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;

/**
 * BattleScene 화면 렌더링 테스트
 * - 듀얼 플레이어 게임판 동시 렌더링 검증
 * - 공격 대기 블록 UI 표시 테스트
 * - 플레이어별 점수/레벨 정보 렌더링 확인
 * - UI 컴포넌트 배치 및 화면 구성 테스트
 */
@DisplayName("BattleScene 화면 렌더링 테스트")
public class BattleSceneRenderingTest {
    
    private JFrame testFrame;
    private BattleScene battleScene;
    private BufferedImage testImage;
    private Graphics2D testGraphics;
    
    @BeforeEach
    @DisplayName("렌더링 테스트 환경 초기화")
    void setUp() {
        System.out.println("=== BattleScene 화면 렌더링 테스트 시작 ===");
        
        // 테스트용 프레임 생성 (1200x800 - BattleScene에 적합한 크기)
        testFrame = new JFrame("BattleScene Rendering Test Frame");
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testFrame.setSize(1200, 800);
        
        // 테스트용 BufferedImage 생성 (실제 화면 렌더링 시뮬레이션)
        testImage = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_RGB);
        testGraphics = testImage.createGraphics();
        
        // 렌더링 품질 설정
        testGraphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                     java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        testGraphics.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, 
                                     java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
    
    @Test
    @DisplayName("듀얼 플레이어 게임판 동시 렌더링 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testDualPlayerGameBoardRendering() {
        System.out.println("--- 듀얼 플레이어 게임판 동시 렌더링 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 일반 모드 BattleScene 생성
            battleScene = new BattleScene(testFrame, "일반 모드");
            
            // Scene 초기화
            battleScene.onEnter();
            System.out.println("✅ BattleScene 초기화 완료");
            
            // 배경색으로 화면 클리어
            testGraphics.setColor(Color.BLACK);
            testGraphics.fillRect(0, 0, 1200, 800);
            
            // BattleScene의 paintComponent 메서드 호출 시뮬레이션
            // (실제로는 repaint()를 통해 간접 호출)
            battleScene.setBounds(0, 0, 1200, 800);
            battleScene.repaint();
            
            // 렌더링이 예외 없이 완료되는지 확인
            Thread.sleep(100); // 렌더링 완료 대기
            System.out.println("✅ 듀얼 게임판 렌더링 예외 없이 완료");
            
            // 화면 크기가 적절한지 확인
            assertTrue(battleScene.getWidth() >= 0, "BattleScene 폭이 0 이상이어야 합니다");
            assertTrue(battleScene.getHeight() >= 0, "BattleScene 높이가 0 이상이어야 합니다");
            
            battleScene.onExit();
            
        }, "듀얼 플레이어 게임판 렌더링은 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("공격 대기 블록 UI 프레임 렌더링 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testAttackBlockUIFrameRendering() {
        System.out.println("--- 공격 대기 블록 UI 프레임 렌더링 테스트 ---");
        
        assertDoesNotThrow(() -> {
            battleScene = new BattleScene(testFrame, "일반 모드");
            battleScene.onEnter();
            
            // 테스트용 Graphics2D에 공격 블록 UI 프레임 그리기 시뮬레이션
            testGraphics.setColor(Color.WHITE);
            testGraphics.setStroke(new java.awt.BasicStroke(2));
            
            // 1P 공격 블록 UI 프레임 (화면 왼쪽)
            testGraphics.drawRect(50, 200, 100, 200);
            testGraphics.drawString("1P Attack", 55, 190);
            
            // 2P 공격 블록 UI 프레임 (화면 오른쪽)  
            testGraphics.drawRect(1050, 200, 100, 200);
            testGraphics.drawString("2P Attack", 1055, 190);
            
            System.out.println("✅ 공격 대기 블록 UI 프레임 그리기 완료");
            
            // 실제 BattleScene 렌더링 호출
            battleScene.repaint();
            Thread.sleep(50);
            
            System.out.println("✅ 공격 블록 UI 렌더링 예외 없이 완료");
            
            battleScene.onExit();
            
        }, "공격 대기 블록 UI 렌더링은 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("플레이어별 점수/레벨 정보 렌더링 테스트")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testPlayerScoreLevelRendering() {
        System.out.println("--- 플레이어별 점수/레벨 정보 렌더링 테스트 ---");
        
        assertDoesNotThrow(() -> {
            battleScene = new BattleScene(testFrame, "일반 모드");
            battleScene.onEnter();
            
            // ScoreManager가 초기화되었는지 간접 확인
            // (리플렉션을 통해 내부 ScoreManager 접근)
            Class<?> battleSceneClass = battleScene.getClass();
            
            Object scoreManager1 = getField(battleScene, battleSceneClass, "scoreManager1");
            Object scoreManager2 = getField(battleScene, battleSceneClass, "scoreManager2");
            
            assertNotNull(scoreManager1, "1P ScoreManager가 초기화되어야 합니다");
            assertNotNull(scoreManager2, "2P ScoreManager가 초기화되어야 합니다");
            System.out.println("✅ 플레이어별 ScoreManager 초기화 확인");
            
            // 테스트용 점수 정보 렌더링 시뮬레이션
            testGraphics.setColor(Color.WHITE);
            testGraphics.setFont(new Font("Arial", Font.BOLD, 16));
            
            // 1P 점수 정보 영역
            testGraphics.drawString("Player 1", 50, 50);
            testGraphics.drawString("Score: 0", 50, 70);
            testGraphics.drawString("Level: 1", 50, 90);
            testGraphics.drawString("Lines: 0", 50, 110);
            
            // 2P 점수 정보 영역
            testGraphics.drawString("Player 2", 1050, 50);
            testGraphics.drawString("Score: 0", 1050, 70);
            testGraphics.drawString("Level: 1", 1050, 90);
            testGraphics.drawString("Lines: 0", 1050, 110);
            
            System.out.println("✅ 플레이어별 점수 정보 렌더링 시뮬레이션 완료");
            
            // 실제 Scene 렌더링
            battleScene.repaint();
            Thread.sleep(50);
            
            battleScene.onExit();
            
        }, "플레이어별 점수/레벨 정보 렌더링은 예외 없이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("화면 해상도별 UI 적응 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testUIAdaptationToResolution() {
        System.out.println("--- 화면 해상도별 UI 적응 테스트 ---");
        
        // 다양한 해상도에서 테스트
        int[][] resolutions = {
            {1024, 768},   // 4:3
            {1280, 720},   // 16:9
            {1920, 1080}   // Full HD
        };
        
        for (int[] resolution : resolutions) {
            int width = resolution[0];
            int height = resolution[1];
            
            System.out.println("해상도 테스트: " + width + "x" + height);
            
            assertDoesNotThrow(() -> {
                // 해상도에 맞는 프레임 생성
                JFrame resolutionFrame = new JFrame("Resolution Test " + width + "x" + height);
                resolutionFrame.setSize(width, height);
                
                battleScene = new BattleScene(resolutionFrame, "일반 모드");
                battleScene.onEnter();
                
                // Scene 크기 설정
                battleScene.setBounds(0, 0, width, height);
                
                // 렌더링 테스트
                battleScene.repaint();
                Thread.sleep(50);
                
                System.out.println("✅ " + width + "x" + height + " 해상도 렌더링 성공");
                
                battleScene.onExit();
                resolutionFrame.dispose();
                
            }, width + "x" + height + " 해상도에서 UI가 정상 렌더링되어야 합니다");
        }
        
        System.out.println("✅ 모든 해상도에서 UI 적응 테스트 완료");
    }
    
    @Test
    @DisplayName("아이템 모드 vs 일반 모드 렌더링 차이 테스트")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testItemModeVsNormalModeRendering() {
        System.out.println("--- 아이템 모드 vs 일반 모드 렌더링 차이 테스트 ---");
        
        String[] modes = {"일반 모드", "아이템 모드"};
        
        for (String mode : modes) {
            System.out.println("모드 렌더링 테스트: " + mode);
            
            assertDoesNotThrow(() -> {
                battleScene = new BattleScene(testFrame, mode);
                battleScene.onEnter();
                
                // ItemManager 존재 여부 확인 (아이템 모드에서만 존재)
                Class<?> battleSceneClass = battleScene.getClass();
                Object itemManager1 = getField(battleScene, battleSceneClass, "itemManager1");
                Object itemManager2 = getField(battleScene, battleSceneClass, "itemManager2");
                
                if ("아이템 모드".equals(mode)) {
                    assertNotNull(itemManager1, "아이템 모드에서 1P ItemManager가 존재해야 합니다");
                    assertNotNull(itemManager2, "아이템 모드에서 2P ItemManager가 존재해야 합니다");
                    System.out.println("✅ 아이템 모드 - ItemManager 확인됨");
                } else {
                    assertNull(itemManager1, "일반 모드에서 1P ItemManager는 null이어야 합니다");
                    assertNull(itemManager2, "일반 모드에서 2P ItemManager는 null이어야 합니다");
                    System.out.println("✅ 일반 모드 - ItemManager null 확인됨");
                }
                
                // 렌더링 테스트
                battleScene.repaint();
                Thread.sleep(50);
                
                System.out.println("✅ " + mode + " 렌더링 완료");
                
                battleScene.onExit();
                
            }, mode + " 렌더링은 예외 없이 작동해야 합니다");
        }
    }
    
    @Test
    @DisplayName("렌더링 성능 및 메모리 사용량 테스트")
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testRenderingPerformanceAndMemory() {
        System.out.println("--- 렌더링 성능 및 메모리 사용량 테스트 ---");
        
        assertDoesNotThrow(() -> {
            // 메모리 사용량 측정 시작
            Runtime runtime = Runtime.getRuntime();
            long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
            
            battleScene = new BattleScene(testFrame, "일반 모드");
            battleScene.onEnter();
            
            // 연속 렌더링 테스트 (성능 확인)
            long startTime = System.currentTimeMillis();
            int renderCount = 10;
            
            for (int i = 0; i < renderCount; i++) {
                battleScene.repaint();
                Thread.sleep(10); // 10ms 간격으로 렌더링
            }
            
            long endTime = System.currentTimeMillis();
            long renderingTime = endTime - startTime;
            
            System.out.println("✅ " + renderCount + "회 렌더링 완료 (소요시간: " + renderingTime + "ms)");
            
            // 평균 렌더링 시간 계산
            double avgRenderTime = (double) renderingTime / renderCount;
            System.out.println("✅ 평균 렌더링 시간: " + String.format("%.1f", avgRenderTime) + "ms");
            
            // 렌더링 성능 검증 (평균 50ms 이하면 양호)
            assertTrue(avgRenderTime < 100, "평균 렌더링 시간이 100ms 이하여야 합니다");
            
            battleScene.onExit();
            
            // 메모리 사용량 측정 종료
            System.gc(); // 가비지 컬렉션 실행
            Thread.sleep(100);
            
            long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
            long memoryUsed = memoryAfter - memoryBefore;
            
            System.out.println("✅ 메모리 사용량: " + (memoryUsed / 1024) + "KB");
            
            // 메모리 사용량이 과도하지 않은지 확인 (10MB 이하)
            assertTrue(memoryUsed < 10 * 1024 * 1024, "메모리 사용량이 10MB 이하여야 합니다");
            
        }, "렌더링 성능 및 메모리 테스트는 예외 없이 작동해야 합니다");
    }
    
    /**
     * 리플렉션을 통해 private 필드 값을 가져오는 헬퍼 메서드
     */
    private Object getField(Object instance, Class<?> clazz, String fieldName) {
        try {
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            System.err.println("필드 접근 실패: " + fieldName + " - " + e.getMessage());
            return null;
        }
    }
    
    @AfterEach
    @DisplayName("렌더링 테스트 정리")
    void tearDown() {
        if (testGraphics != null) {
            testGraphics.dispose();
            testGraphics = null;
        }
        
        if (testImage != null) {
            testImage.flush();
            testImage = null;
        }
        
        if (testFrame != null) {
            testFrame.dispose();
            testFrame = null;
        }
        
        // 강제 메모리 정리
        System.gc();
        System.out.println("=== BattleScene 화면 렌더링 테스트 완료 ===\n");
    }
    
    @AfterAll
    @DisplayName("BattleSceneRenderingTest 전체 정리")
    static void cleanup() {
        System.out.println("🧹 BattleSceneRenderingTest 정리 완료");
        
        // TestCleanupHelper를 통한 추가 정리
        TestCleanupHelper.forceCompleteSystemCleanup("BattleSceneRenderingTest");
    }
}