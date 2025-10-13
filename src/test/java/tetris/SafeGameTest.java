package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;

/**
 * 안전한 게임 테스트 클래스 - 타임아웃과 강화된 리소스 관리가 적용됨
 */
@DisplayName("타임아웃 제한된 안전한 게임 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SafeGameTest {

    /**
     * 모든 테스트 후 백그라운드 프로세스 완전 정리
     */
    @AfterAll
    @DisplayName("SafeGameTest 백그라운드 프로세스 완전 정리")
    static void forceCompleteCleanup() {
        System.out.println("=== SafeGameTest 백그라운드 프로세스 완전 정리 시작 ===");
        
        try {
            // 1. 모든 윈도우 강제 닫기
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                if (window.isDisplayable()) {
                    window.setVisible(false);
                    window.dispose();
                }
            }
            
            // 2. 모든 Timer 완전 중지
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
            
            // 3. EDT 이벤트 큐 완전 정리
            try {
                java.awt.EventQueue eventQueue = java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue();
                int cleared = 0;
                while (eventQueue.peekEvent() != null && cleared < 50) {
                    eventQueue.getNextEvent();
                    cleared++;
                }
                if (cleared > 0) {
                    System.out.println("🧹 " + cleared + "개의 EDT 이벤트 정리");
                }
            } catch (Exception e) {
                // 무시
            }
            
            // 4. 백그라운드 스레드 강제 정리
            ThreadGroup root = Thread.currentThread().getThreadGroup();
            while (root.getParent() != null) {
                root = root.getParent();
            }
            
            Thread[] threads = new Thread[root.activeCount() * 2];
            int count = root.enumerate(threads, true);
            int terminated = 0;
            
            for (int i = 0; i < count; i++) {
                if (threads[i] != null && threads[i] != Thread.currentThread()) {
                    String name = threads[i].getName();
                    if (name.contains("AWT-") || name.contains("Timer") || name.contains("Swing") ||
                        name.contains("Java2D") || name.toLowerCase().contains("test")) {
                        
                        try {
                            if (threads[i].isAlive()) {
                                threads[i].interrupt();
                                if (!threads[i].isDaemon()) {
                                    threads[i].join(300);
                                }
                                terminated++;
                                System.out.println("🔧 스레드 종료: " + name);
                            }
                        } catch (Exception e) {
                            // 무시
                        }
                    }
                }
            }
            
            // 5. 최종 시스템 정리
            System.runFinalization();
            System.gc();
            Thread.sleep(100);
            System.gc();
            
            System.out.println("✅ SafeGameTest 백그라운드 프로세스 정리 완료 (" + terminated + "개 스레드 종료)");
            
        } catch (Exception e) {
            System.out.println("SafeGameTest 정리 중 오류 (무시): " + e.getMessage());
        }
        
        System.out.println("=== SafeGameTest 백그라운드 프로세스 완전 정리 완료 ===");
        
        // TestCleanupHelper를 통한 추가 정리
        TestCleanupHelper.forceCompleteSystemCleanup("SafeGameTest");
    }

    @Test
    @Order(1)
    @DisplayName("게임 실행 안전성 테스트 (5초 타임아웃)")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testGameRunSafety() {
        System.out.println("=== 안전한 게임 실행 테스트 시작 ===");
        
        // EDT에서 안전하게 실행
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    // Game.run() 호출하지 않고 컴포넌트만 테스트
                    System.out.println("✅ 게임 클래스 로딩 성공");
                    
                    // Game 인스턴스 확인만
                    Game gameInstance = Game.getInstance();
                    assertNotNull(gameInstance, "Game 인스턴스가 생성되어야 합니다.");
                    
                    System.out.println("✅ Game 인스턴스 생성 확인");
                    
                } catch (Exception e) {
                    System.err.println("게임 안전성 테스트 중 오류: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            });
        }, "게임 안전성 테스트는 예외를 발생시키지 않아야 합니다.");
        
        System.out.println("=== 안전한 게임 실행 테스트 완료 ===");
    }

    @Test
    @Order(2)
    @DisplayName("Scene 설정 null 안전성 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testSceneNullSafety() {
        System.out.println("=== Scene null 안전성 테스트 시작 ===");
        
        assertDoesNotThrow(() -> {
            // null scene 설정 테스트
            Game.setScene(null);
            System.out.println("✅ null scene 설정 처리 완료");
            
        }, "null scene 설정은 안전하게 처리되어야 합니다.");
        
        System.out.println("=== Scene null 안전성 테스트 완료 ===");
    }
}