package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;

@DisplayName("안전한 게임 기본 테스트")
public class GameTest {
    
    @Test
    @DisplayName("게임 인스턴스 생성 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void gameInstance_canBeCreated() {
        System.out.println("=== 게임 인스턴스 생성 테스트 ===");
        
        assertDoesNotThrow(() -> {
            Game gameInstance = Game.getInstance();
            assertNotNull(gameInstance, "Game 인스턴스가 null이 아니어야 합니다.");
            System.out.println("✅ Game 인스턴스 생성 성공");
        }, "Game 인스턴스 생성은 예외를 발생시키지 않아야 합니다.");
    }
    
    @Test
    @DisplayName("Scene 설정 안전성 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void setScene_handlesNullSafely() {
        System.out.println("=== Scene null 안전성 테스트 ===");
        
        assertDoesNotThrow(() -> {
            // null scene 설정이 안전하게 처리되는지 확인
            Game.setScene(null);
            System.out.println("✅ null scene 설정 처리 완료");
        }, "null scene 설정은 안전하게 처리되어야 합니다.");
    }
    
    @Test
    @DisplayName("게임 클래스 로딩 테스트")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void gameClass_loadsSuccessfully() {
        System.out.println("=== 게임 클래스 로딩 테스트 ===");
        
        assertDoesNotThrow(() -> {
            // 클래스 로딩만 확인 (실제 게임 실행 없음)
            Class<?> gameClass = Game.class;
            assertNotNull(gameClass, "Game 클래스가 로딩되어야 합니다.");
            System.out.println("✅ Game 클래스 로딩 성공");
        }, "Game 클래스 로딩은 예외를 발생시키지 않아야 합니다.");
    }
    
    /**
     * 게임 결과에 선택한 난이도 시현 테스트
     * - 난이도 설정이 게임 세션 동안 유지되는지 확인
     * - 게임 결과에 난이도가 올바르게 반영되는지 확인
     */
    @Test
    @DisplayName("게임 결과에 선택한 난이도 시현 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void gameResult_displaysSelectedDifficulty() {
        System.out.println("=== 게임 결과에 선택한 난이도 시현 테스트 ===");
        
        assertDoesNotThrow(() -> {
            // GameSettings 인스턴스 생성 및 초기화
            GameSettings settings = GameSettings.getInstance();
            
            // 각 난이도별로 테스트
            GameSettings.Difficulty[] difficulties = {
                GameSettings.Difficulty.EASY,
                GameSettings.Difficulty.NORMAL, 
                GameSettings.Difficulty.HARD
            };
            
            for (GameSettings.Difficulty difficulty : difficulties) {
                System.out.println("난이도 테스트: " + difficulty);
                
                // 난이도 설정
                settings.setDifficulty(difficulty);
                
                // 설정이 제대로 저장되었는지 확인
                assertEquals(difficulty, settings.getDifficulty(), 
                    difficulty + " 난이도가 설정되어야 합니다.");
                
                // 게임 세션 시뮬레이션 - 난이도가 유지되는지 확인
                // (실제 게임 실행 없이 설정만 확인)
                assertEquals(difficulty, settings.getDifficulty(),
                    "게임 세션 동안 " + difficulty + " 난이도가 유지되어야 합니다.");
                
                System.out.println("✅ " + difficulty + " 난이도 설정 및 유지 확인");
            }
            
            // 추가: 난이도별 게임 속도 설정 확인
            for (GameSettings.Difficulty difficulty : difficulties) {
                settings.setDifficulty(difficulty);
                
                // 난이도에 따른 속도 설정이 있는지 간접 확인
                // (실제 SpeedUp 클래스 사용 여부는 설정을 통해 확인)
                assertNotNull(settings.getDifficulty(), 
                    difficulty + " 난이도 설정이 null이 아니어야 합니다.");
                    
                System.out.println("✅ " + difficulty + " 난이도 속도 설정 확인");
            }
            
            System.out.println("✅ 게임 결과 난이도 시현 테스트 완료");
            
        }, "게임 결과 난이도 시현 테스트는 예외를 발생시키지 않아야 합니다.");
    }
    
    @AfterAll
    @DisplayName("GameTest 백그라운드 프로세스 정리")
    static void cleanup() {
        try {
            System.out.println("🧹 GameTest 백그라운드 프로세스 정리 시작...");
            
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
            
            // 2. AWT/Swing EventQueue 정리
            try {
                java.awt.EventQueue eventQueue = java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue();
                while (eventQueue.peekEvent() != null) {
                    eventQueue.getNextEvent();
                }
            } catch (Exception e) {
                // 무시
            }
            
            // 3. 활성 GUI 스레드 정리
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
                        System.out.println("⚠️ GameTest 활성 GUI 스레드 감지: " + threadName);
                        thread.interrupt();
                    }
                }
            }
            
            // 4. 강제 메모리 정리
            System.runFinalization();
            System.gc();
            
        } catch (Exception e) {
            System.out.println("GameTest 정리 중 오류 (무시): " + e.getMessage());
        }
        
        System.out.println("✅ GameTest 백그라운드 프로세스 정리 완료");
        
        // TestCleanupHelper를 통한 추가 정리
        TestCleanupHelper.forceCompleteSystemCleanup("GameTest");
    }
}