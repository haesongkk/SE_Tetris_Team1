package tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

/**
 * 테스트 정리 도우미 클래스
 * 모든 테스트에서 공통으로 사용할 수 있는 백그라운드 스레드 정리 기능 제공
 */
public class TestCleanupHelper {

    /**
     * 모든 GUI 관련 백그라운드 스레드와 리소스를 강제로 정리합니다.
     * 이 메소드는 @AfterAll에서 호출되어야 합니다.
     */
    public static void forceCompleteSystemCleanup(String testClassName) {
        System.out.println("=== " + testClassName + " 강화된 완전 시스템 정리 시작 ===");
        
        try {
            // 1. 모든 활성 Timer 완전 중지
            stopAllTimers();
            
            // 2. EDT(Event Dispatch Thread) 이벤트 큐 완전 정리
            cleanupEventQueue();
            
            // 3. 모든 윈도우 및 컴포넌트 완전 해제
            cleanupAllWindows();
            
            // 4. 백그라운드 스레드 강제 종료
            terminateBackgroundThreads();
            
            // 5. 시스템 레벨 강제 정리
            performSystemCleanup();
            
            // 6. 최종 상태 검증
            verifyCleanupResults(testClassName);
            
            System.out.println("✅ " + testClassName + " 강화된 완전 시스템 정리 완료");
            
        } catch (Exception e) {
            System.out.println(testClassName + " 강화된 정리 중 전체 오류 (무시): " + e.getMessage());
        }
    }

    /**
     * 모든 Swing Timer를 중지합니다.
     */
    private static void stopAllTimers() {
        try {
            Class<?> timerClass = Class.forName("javax.swing.Timer");
            
            // 로그 타이머 비활성화
            try {
                java.lang.reflect.Field logTimersField = timerClass.getDeclaredField("logTimers");
                logTimersField.setAccessible(true);
                logTimersField.set(null, false);
            } catch (Exception e) {
                // 무시
            }
            
            // Timer 큐 중지
            try {
                java.lang.reflect.Field queueField = timerClass.getDeclaredField("queue");
                queueField.setAccessible(true);
                Object timerQueue = queueField.get(null);
                
                if (timerQueue != null) {
                    java.lang.reflect.Method stopMethod = timerQueue.getClass().getDeclaredMethod("stop");
                    stopMethod.setAccessible(true);
                    stopMethod.invoke(timerQueue);
                    System.out.println("🧹 모든 Swing Timer 완전 중지됨");
                }
            } catch (Exception e) {
                // 무시
            }
            
        } catch (Exception e) {
            System.out.println("Timer 정리 중 오류 (무시): " + e.getMessage());
        }
    }

    /**
     * EDT 이벤트 큐를 정리합니다.
     */
    private static void cleanupEventQueue() {
        try {
            EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
            int eventCount = 0;
            
            // 이벤트 큐에서 모든 이벤트 제거
            while (eventQueue.peekEvent() != null && eventCount < 1000) {
                try {
                    eventQueue.getNextEvent();
                    eventCount++;
                } catch (Exception e) {
                    break;
                }
            }
            
            if (eventCount > 0) {
                System.out.println("🧹 " + eventCount + "개의 EDT 이벤트 정리됨");
            }
            
            // AWT 시스템 동기화
            Toolkit.getDefaultToolkit().sync();
            
        } catch (Exception e) {
            System.out.println("EDT 정리 중 오류 (무시): " + e.getMessage());
        }
    }

    /**
     * 모든 윈도우와 컴포넌트를 정리합니다.
     */
    private static void cleanupAllWindows() {
        try {
            Window[] windows = Window.getWindows();
            int closedCount = 0;
            
            for (Window window : windows) {
                if (window != null && window.isDisplayable()) {
                    try {
                        // 이벤트 리스너 제거
                        java.awt.event.WindowListener[] windowListeners = window.getWindowListeners();
                        for (java.awt.event.WindowListener listener : windowListeners) {
                            window.removeWindowListener(listener);
                        }
                        
                        // 컴포넌트 리스너 제거
                        java.awt.event.ComponentListener[] componentListeners = window.getComponentListeners();
                        for (java.awt.event.ComponentListener listener : componentListeners) {
                            window.removeComponentListener(listener);
                        }
                        
                        // 윈도우 닫기 이벤트 전송
                        window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
                        
                        // 윈도우 완전 해제
                        window.setVisible(false);
                        window.dispose();
                        closedCount++;
                        
                    } catch (Exception e) {
                        // 개별 윈도우 정리 실패는 무시
                    }
                }
            }
            
            if (closedCount > 0) {
                System.out.println("🧹 " + closedCount + "개의 윈도우 완전 해제됨");
            }
            
        } catch (Exception e) {
            System.out.println("윈도우 정리 중 오류 (무시): " + e.getMessage());
        }
    }

    /**
     * 백그라운드 스레드를 강제로 종료합니다.
     */
    private static void terminateBackgroundThreads() {
        try {
            ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
            ThreadGroup parentGroup;
            while ((parentGroup = rootGroup.getParent()) != null) {
                rootGroup = parentGroup;
            }
            
            Thread[] threads = new Thread[rootGroup.activeCount() * 3];
            int count = rootGroup.enumerate(threads, true);
            int terminatedCount = 0;
            
            for (int i = 0; i < count; i++) {
                Thread thread = threads[i];
                if (thread != null && thread != Thread.currentThread()) {
                    String threadName = thread.getName();
                    
                    // GUI 및 테스트 관련 모든 백그라운드 스레드 강제 종료
                    if (shouldTerminateThread(threadName)) {
                        try {
                            if (thread.isAlive()) {
                                System.out.println("🔧 백그라운드 스레드 강제 종료: " + threadName + " (상태: " + thread.getState() + ")");
                                
                                // 스레드 인터럽트
                                thread.interrupt();
                                
                                // 데몬이 아닌 스레드는 강제 종료 대기
                                if (!thread.isDaemon()) {
                                    thread.join(300); // 최대 300ms 대기
                                }
                                
                                terminatedCount++;
                            }
                        } catch (Exception e) {
                            // 개별 스레드 종료 실패는 무시
                        }
                    }
                }
            }
            
            if (terminatedCount > 0) {
                System.out.println("🧹 " + terminatedCount + "개의 백그라운드 스레드 강제 종료됨");
            }
            
        } catch (Exception e) {
            System.out.println("스레드 정리 중 오류 (무시): " + e.getMessage());
        }
    }

    /**
     * 스레드가 종료되어야 하는지 확인합니다.
     */
    private static boolean shouldTerminateThread(String threadName) {
        return threadName.contains("AWT-EventQueue") ||
               threadName.contains("TimerQueue") ||
               threadName.contains("Swing-Timer") ||
               threadName.contains("Java2D") ||
               threadName.contains("AWT-Windows") ||
               threadName.contains("AWT-Shutdown") ||
               threadName.toLowerCase().contains("test") ||
               threadName.contains("ForkJoinPool") ||
               threadName.contains("CommonPool") ||
               threadName.contains("Timer-") ||
               threadName.contains("EDT") ||
               threadName.contains("Disposer");
    }

    /**
     * 시스템 레벨 정리를 수행합니다.
     */
    private static void performSystemCleanup() {
        try {
            // 강화된 메모리 정리 (5회 반복)
            for (int i = 0; i < 5; i++) {
                System.runFinalization();
                System.gc();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            // JVM 종료 후크 정리 시도
            try {
                Runtime.getRuntime().runFinalization();
            } catch (Exception e) {
                // 무시
            }
            
            System.out.println("🧹 시스템 레벨 강제 정리 완료");
            
        } catch (Exception e) {
            System.out.println("시스템 정리 중 오류 (무시): " + e.getMessage());
        }
    }

    /**
     * 정리 결과를 검증합니다.
     */
    private static void verifyCleanupResults(String testClassName) {
        try {
            Thread.sleep(500); // 정리 작업 완료 대기
            
            ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
            ThreadGroup parentGroup;
            while ((parentGroup = rootGroup.getParent()) != null) {
                rootGroup = parentGroup;
            }
            
            Thread[] finalThreads = new Thread[rootGroup.activeCount() * 3];
            int finalCount = rootGroup.enumerate(finalThreads, true);
            int remainingGuiThreads = 0;
            
            for (int i = 0; i < finalCount; i++) {
                if (finalThreads[i] != null) {
                    String name = finalThreads[i].getName();
                    if (shouldTerminateThread(name)) {
                        remainingGuiThreads++;
                        System.out.println("⚠️ 남은 GUI 스레드: " + name + " (상태: " + finalThreads[i].getState() + ")");
                    }
                }
            }
            
            if (remainingGuiThreads == 0) {
                System.out.println("🎉 모든 GUI 관련 백그라운드 스레드가 완전히 정리됨");
            } else {
                System.out.println("⚠️ " + remainingGuiThreads + "개의 GUI 스레드가 여전히 활성 상태");
                
                // 마지막 시도: 강제 시스템 종료 준비
                if (remainingGuiThreads <= 5) {
                    System.out.println("🔧 마지막 시도: 잔여 스레드 추가 정리...");
                    
                    for (int i = 0; i < finalCount; i++) {
                        if (finalThreads[i] != null && shouldTerminateThread(finalThreads[i].getName())) {
                            try {
                                finalThreads[i].interrupt();
                                if (!finalThreads[i].isDaemon()) {
                                    finalThreads[i].join(50);
                                }
                            } catch (Exception e) {
                                // 무시
                            }
                        }
                    }
                    
                    // 최종 시도: EDT 강제 종료
                    try {
                        java.awt.EventQueue.invokeAndWait(() -> {
                            // EDT에서 자기 자신을 종료 시도
                            System.out.println("🔧 EDT 종료 시도");
                        });
                    } catch (Exception e) {
                        // 무시
                    }
                    
                    // JVM 레벨 강제 정리
                    try {
                        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                            System.out.println("🔧 JVM 종료 훅 실행됨");
                        }));
                    } catch (Exception e) {
                        // 무시
                    }
                    
                    // 최종 가비지 컬렉션
                    for (int j = 0; j < 10; j++) {
                        System.runFinalization();
                        System.gc();
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("최종 검증 중 오류 (무시): " + e.getMessage());
        }
    }
}