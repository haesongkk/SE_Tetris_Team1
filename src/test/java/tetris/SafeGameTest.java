package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;

/**
 * 안전한 게임 테스트 클래스 - 타임아웃과 리소스 관리가 적용됨
 */
@DisplayName("타임아웃 제한된 안전한 게임 테스트")
public class SafeGameTest {

    @Test
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