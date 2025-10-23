package tetris;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 모든 테스트 클래스에서 공통으로 사용하는 기본 테스트 클래스
 * 
 * 공통 기능:
 * - GUI 환경 설정 및 정리
 * - 테스트 프레임 관리
 * - 백그라운드 프로세스 정리
 * - 리플렉션 유틸리티 메서드
 * - 헤드리스 환경 체크
 */
public abstract class BaseTest {

    protected static JFrame testFrame;
    protected static Timer dialogCloser;
    
    /**
     * 모든 테스트 시작 전 공통 환경 설정
     */
    @BeforeAll
    static void setupBaseTestEnvironment() {
        System.out.println("=== 베이스 테스트 환경 설정 ===");

        // 헤드리스 환경 체크
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("⚠️ 헤드리스 환경: GUI 테스트 제한됨");
            return;
        }

        // 공통 테스트 프레임 생성
        if (testFrame == null) {
            testFrame = new JFrame("Test Framework");
            testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            testFrame.setSize(800, 600);
        }

        System.out.println("✅ 베이스 테스트 환경 설정 완료");
    }

    /**
     * 각 테스트 메서드 실행 전 공통 초기화
     */
    @BeforeEach
    void setupEachTest() {
        // 개별 테스트에서 필요한 경우 오버라이드
        if (isHeadlessEnvironment()) {
            System.out.println("헤드리스 환경에서 테스트 실행");
        }
    }

    /**
     * 모든 테스트 완료 후 공통 정리
     */
    @AfterAll
    static void cleanupBaseTestEnvironment() {
        System.out.println("=== 베이스 테스트 환경 정리 시작 ===");
        
        try {
            // 1. 다이얼로그 닫기 타이머 정리
            if (dialogCloser != null) {
                dialogCloser.stop();
                dialogCloser = null;
                System.out.println("🧹 다이얼로그 타이머 정리됨");
            }

            // 2. 테스트 프레임 정리
            if (testFrame != null) {
                testFrame.dispose();
                testFrame = null;
                System.out.println("🧹 테스트 프레임 정리됨");
            }

            // 3. Swing Timer 큐 정리
            cleanupSwingTimers();

            // 4. AWT EventQueue 정리
            cleanupEventQueue();

            // 5. 활성 GUI 스레드 정리
            cleanupActiveThreads();

            // 6. 강제 메모리 정리
            forceGarbageCollection();

        } catch (Exception e) {
            System.out.println("베이스 테스트 정리 중 오류 (무시): " + e.getMessage());
        }
        
        System.out.println("✅ 베이스 테스트 환경 정리 완료");
    }

    /**
     * 헤드리스 환경 여부 확인
     */
    protected static boolean isHeadlessEnvironment() {
        return GraphicsEnvironment.isHeadless();
    }

    /**
     * 테스트 프레임 반환 (지연 초기화)
     */
    protected static JFrame getTestFrame() {
        if (testFrame == null && !isHeadlessEnvironment()) {
            testFrame = new JFrame("Test Framework");
            testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            testFrame.setSize(800, 600);
        }
        return testFrame;
    }

    /**
     * 다이얼로그 자동 닫기 타이머 설정
     */
    protected static void setupDialogCloser(int delayMs, String testName) {
        if (isHeadlessEnvironment()) return;

        dialogCloser = new Timer(delayMs, e -> {
            try {
                Window[] windows = Window.getWindows();
                for (Window window : windows) {
                    if (window instanceof JDialog && window.isVisible()) {
                        System.out.println("🔄 " + testName + " 다이얼로그 자동 닫기");
                        window.dispose();
                    }
                }
            } catch (Exception ex) {
                System.out.println("다이얼로그 닫기 중 오류 (무시): " + ex.getMessage());
            }
        });
        dialogCloser.start();
        System.out.println("⏰ " + testName + " 다이얼로그 자동 닫기 타이머 시작됨");
    }

    /**
     * 리플렉션을 사용하여 필드 값 가져오기
     */
    protected static Object getFieldValue(Object obj, String fieldName) throws Exception {
        Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj instanceof Class ? null : obj);
    }

    /**
     * 리플렉션을 사용하여 메서드 호출
     */
    protected static Object invokeMethod(Object obj, String methodName, Object... args) throws Exception {
        Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }
        Method method = clazz.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(obj instanceof Class ? null : obj, args);
    }

    /**
     * 클래스 존재 여부 확인
     */
    protected static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Swing Timer 큐 정리
     */
    private static void cleanupSwingTimers() {
        try {
            javax.swing.Timer.setLogTimers(false);
            Field timersField = javax.swing.Timer.class.getDeclaredField("queue");
            timersField.setAccessible(true);
            Object timerQueue = timersField.get(null);
            if (timerQueue != null) {
                Method stopMethod = timerQueue.getClass().getDeclaredMethod("stop");
                stopMethod.setAccessible(true);
                stopMethod.invoke(timerQueue);
                System.out.println("🧹 Swing Timer 큐 정리됨");
            }
        } catch (Exception e) {
            // 리플렉션 실패는 무시
        }
    }

    /**
     * AWT EventQueue 정리
     */
    private static void cleanupEventQueue() {
        try {
            EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
            while (eventQueue.peekEvent() != null) {
                eventQueue.getNextEvent();
            }
            System.out.println("🧹 AWT EventQueue 정리됨");
        } catch (Exception e) {
            // 무시
        }
    }

    /**
     * 활성 GUI 스레드 정리
     */
    private static void cleanupActiveThreads() {
        try {
            ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
            ThreadGroup parentGroup;
            while ((parentGroup = rootGroup.getParent()) != null) {
                rootGroup = parentGroup;
            }

            Thread[] threads = new Thread[rootGroup.activeCount()];
            int count = rootGroup.enumerate(threads);

            int interruptedCount = 0;
            for (int i = 0; i < count; i++) {
                Thread thread = threads[i];
                if (thread != null && !thread.isDaemon() && thread != Thread.currentThread()) {
                    String threadName = thread.getName();
                    if (threadName.contains("AWT-EventQueue") || 
                        threadName.contains("TimerQueue") ||
                        threadName.contains("Swing-Timer")) {
                        thread.interrupt();
                        interruptedCount++;
                    }
                }
            }
            
            if (interruptedCount > 0) {
                System.out.println("🧹 " + interruptedCount + "개의 GUI 스레드 정리됨");
            }
        } catch (Exception e) {
            // 무시
        }
    }

    /**
     * 강제 가비지 컬렉션
     */
    private static void forceGarbageCollection() {
        try {
            System.runFinalization();
            System.gc();
            Thread.sleep(50);
            System.gc();
            System.out.println("🧹 가비지 컬렉션 완료");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 테스트 실행 시간 측정을 위한 유틸리티 클래스
     */
    protected static class TestTimer {
        private long startTime;
        
        public void start() {
            startTime = System.currentTimeMillis();
        }
        
        public long getElapsedMs() {
            return System.currentTimeMillis() - startTime;
        }
        
        public void printElapsed(String testName) {
            System.out.println("⏱️ " + testName + " 실행 시간: " + getElapsedMs() + "ms");
        }
    }
}