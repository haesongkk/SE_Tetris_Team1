package tetris;

/**
 * 최소화된 테스트 정리 도우미 클래스
 * 무한 루프 위험을 완전히 제거
 */
public class TestCleanupHelper {
    
    /**
     * 최소한의 정리만 수행합니다.
     */
    public static void forceCompleteSystemCleanup(String testClassName) {
        try {
            // 간단한 가비지 컬렉션만 수행
            System.gc();
            
            // 최소한의 대기
            Thread.sleep(10);
            
        } catch (Exception e) {
            // 예외는 완전히 무시
        }
    }
}