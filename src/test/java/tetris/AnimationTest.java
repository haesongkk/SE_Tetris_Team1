package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import tetris.util.Animation;
import java.awt.*;
import javax.swing.SwingConstants;
import java.util.concurrent.TimeUnit;

/**
 * Animation 클래스의 개선된 기능들에 대한 JUnit 테스트
 * 새로운 애니메이션 효과들과 타이머 관리 시스템을 테스트합니다.
 */
@DisplayName("Animation 클래스 개선 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AnimationTest {

    private Animation animation;

    @BeforeEach
    @DisplayName("테스트 환경 설정")
    void setUp() {
        System.out.println("=== Animation 테스트 환경 설정 ===");
        
        // 헤드리스 환경에서는 Animation 테스트 건너뛰기
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("⚠️ 헤드리스 환경에서는 Animation 테스트를 건너뜁니다.");
            return;
        }
        
        try {
            // 기본 Animation 객체 생성
            // 10/25 Animation 생성자 변경
            animation = new Animation();
            System.out.println("✅ Animation 객체 생성 성공");
        } catch (Exception e) {
            System.out.println("⚠️ Animation 객체 생성 실패: " + e.getMessage());
            animation = null;
        }
        
        System.out.println("✅ Animation 테스트 환경 설정 완료");
    }

    @AfterEach
    @DisplayName("테스트 환경 정리")
    void tearDown() {
        if (animation != null) {
            animation.release();
            animation = null;
        }
        System.out.println("🧹 Animation 테스트 환경 정리 완료");
    }

    @AfterAll
    @DisplayName("전체 테스트 환경 정리")
    static void cleanup() {
        try {
            // TestCleanupHelper를 통한 시스템 정리
            TestCleanupHelper.forceCompleteSystemCleanup("AnimationTest");
            System.out.println("✅ Animation 전체 테스트 환경 정리 완료");
        } catch (Exception e) {
            System.out.println("Animation 테스트 정리 중 오류 (무시): " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. Animation 객체 생성 테스트")
    void testAnimationCreation() {
        System.out.println("=== 1. Animation 객체 생성 테스트 ===");

        // 헤드리스 환경 체크
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("⚠️ 헤드리스 환경에서는 Animation 생성 테스트를 건너뜁니다.");
            return;
        }
        
        // Animation 객체 확인
        assertNotNull(animation, "Animation 객체가 생성되어야 합니다.");
        
        // 초기 상태 확인
        assertEquals(0.0f, animation.alpha, 0.01f, "초기 alpha 값이 0이어야 합니다.");
        assertEquals(1.0f, animation.scaleX, 0.01f, "초기 scaleX 값이 1이어야 합니다.");
        assertEquals(1.0f, animation.scaleY, 0.01f, "초기 scaleY 값이 1이어야 합니다.");
        assertEquals(0.0f, animation.rotate, 0.01f, "초기 rotate 값이 0이어야 합니다.");
        assertEquals(0.0f, animation.offsetX, 0.01f, "초기 offsetX 값이 0이어야 합니다.");
        assertEquals(0.0f, animation.offsetY, 0.01f, "초기 offsetY 값이 0이어야 합니다.");
        assertFalse(animation.bVisible, "초기 상태에서는 보이지 않아야 합니다.");
        
        System.out.println("✅ Animation 객체 생성 테스트 통과");
    }

    @Test
    @Order(2)
    @DisplayName("2. popIn 애니메이션 효과 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testPopInAnimation() {
        System.out.println("=== 2. popIn 애니메이션 효과 테스트 ===");

        // 헤드리스 환경 체크
        if (GraphicsEnvironment.isHeadless() || animation == null) {
            System.out.println("⚠️ 헤드리스 환경이거나 Animation이 null이므로 테스트를 건너뜁니다.");
            return;
        }
        
        // popIn 애니메이션 시작
        // 10/25: popIn 함수 수정
        assertDoesNotThrow(() -> {
            animation.popIn(0.5f);
        }, "popIn 애니메이션이 예외를 발생시키지 않아야 합니다.");
        
        // 애니메이션 시작 후 상태 확인
        assertTrue(animation.bVisible, "popIn 시작 후 보이는 상태여야 합니다.");
        
        System.out.println("✅ popIn 애니메이션 효과 테스트 통과");
    }

    @Test
    @Order(3)
    @DisplayName("3. popOut 애니메이션 효과 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testPopOutAnimation() {
        System.out.println("=== 3. popOut 애니메이션 효과 테스트 ===");

        // 헤드리스 환경 체크
        if (GraphicsEnvironment.isHeadless() || animation == null) {
            System.out.println("⚠️ 헤드리스 환경이거나 Animation이 null이므로 테스트를 건너뜁니다.");
            return;
        }
        
        // popOut 애니메이션 시작
        // 10/25: popout 함수 수정
        assertDoesNotThrow(() -> {
            animation.popOut(0.5f);
        }, "popOut 애니메이션이 예외를 발생시키지 않아야 합니다.");
        
        // 애니메이션 시작 후 상태 확인
        assertTrue(animation.bVisible, "popOut 시작 후 보이는 상태여야 합니다.");
        
        System.out.println("✅ popOut 애니메이션 효과 테스트 통과");
    }

    @Test
    @Order(4)
    @DisplayName("4. blink 애니메이션 효과 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testBlinkAnimation() {
        System.out.println("=== 4. blink 애니메이션 효과 테스트 ===");

        // 헤드리스 환경 체크
        if (GraphicsEnvironment.isHeadless() || animation == null) {
            System.out.println("⚠️ 헤드리스 환경이거나 Animation이 null이므로 테스트를 건너뜁니다.");
            return;
        }
        
        // blink 애니메이션 시작
        assertDoesNotThrow(() -> {
            animation.blink(0.2f, 0.2f);
        }, "blink 애니메이션이 예외를 발생시키지 않아야 합니다.");
        
        // 애니메이션 상태 확인 (alpha는 1로 설정됨)
        assertEquals(1.0f, animation.alpha, 0.01f, "blink 시작 후 alpha가 1이어야 합니다.");
        assertEquals(1.0f, animation.scaleX, 0.01f, "blink 시작 후 scaleX가 1이어야 합니다.");
        assertEquals(1.0f, animation.scaleY, 0.01f, "blink 시작 후 scaleY가 1이어야 합니다.");
        
        System.out.println("✅ blink 애니메이션 효과 테스트 통과");
    }

    @Test
    @Order(5)
    @DisplayName("5. move 애니메이션 효과 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testMoveAnimation() {
        System.out.println("=== 5. move 애니메이션 효과 테스트 ===");

        // 헤드리스 환경 체크
        if (GraphicsEnvironment.isHeadless() || animation == null) {
            System.out.println("⚠️ 헤드리스 환경이거나 Animation이 null이므로 테스트를 건너뜁니다.");
            return;
        }
        
        // move 애니메이션 시작
        // 10/25: move 함수 수정
        assertDoesNotThrow(() -> {
            animation.move(0.5f,10, 20);
        }, "move 애니메이션이 예외를 발생시키지 않아야 합니다.");
        
        // 애니메이션 상태 확인
        assertEquals(1.0f, animation.alpha, 0.01f, "move 시작 후 alpha가 1이어야 합니다.");
        assertEquals(1.0f, animation.scaleX, 0.01f, "move 시작 후 scaleX가 1이어야 합니다.");
        assertEquals(1.0f, animation.scaleY, 0.01f, "move 시작 후 scaleY가 1이어야 합니다.");
        // bVisible은 타이머 내부에서 설정되므로 즉시 확인하지 않음
        
        System.out.println("✅ move 애니메이션 효과 테스트 통과");
    }

    @Test
    @Order(6)
    @DisplayName("6. hueBackground 애니메이션 효과 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testHueBackgroundAnimation() {
        System.out.println("=== 6. hueBackground 애니메이션 효과 테스트 ===");

        // 헤드리스 환경 체크
        if (GraphicsEnvironment.isHeadless() || animation == null) {
            System.out.println("⚠️ 헤드리스 환경이거나 Animation이 null이므로 테스트를 건너뜁니다.");
            return;
        }
        
        // hueBackground 애니메이션 시작
        assertDoesNotThrow(() -> {
            animation.hueBackground(1.0f, false);
        }, "hueBackground 애니메이션이 예외를 발생시키지 않아야 합니다.");
        
        // 애니메이션 상태 확인
        assertEquals(1.0f, animation.alpha, 0.01f, "hueBackground 시작 후 alpha가 1이어야 합니다.");
        assertTrue(animation.bVisible, "hueBackground 시작 후 보이는 상태여야 합니다.");
        assertEquals(1.0f, animation.scaleX, 0.01f, "hueBackground 시작 후 scaleX가 1이어야 합니다.");
        assertEquals(1.0f, animation.scaleY, 0.01f, "hueBackground 시작 후 scaleY가 1이어야 합니다.");
        
        System.out.println("✅ hueBackground 애니메이션 효과 테스트 통과");
    }

    @Test
    @Order(7)
    @DisplayName("7. hueBorder 애니메이션 효과 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testHueBorderAnimation() {
        System.out.println("=== 7. hueBorder 애니메이션 효과 테스트 ===");

        // 헤드리스 환경 체크
        if (GraphicsEnvironment.isHeadless() || animation == null) {
            System.out.println("⚠️ 헤드리스 환경이거나 Animation이 null이므로 테스트를 건너뜁니다.");
            return;
        }
        
        // hueBorder 애니메이션 시작
        assertDoesNotThrow(() -> {
            animation.hueBorder(1.0f, false);
        }, "hueBorder 애니메이션이 예외를 발생시키지 않아야 합니다.");
        
        // 애니메이션 상태 확인
        assertEquals(1.0f, animation.alpha, 0.01f, "hueBorder 시작 후 alpha가 1이어야 합니다.");
        assertTrue(animation.bVisible, "hueBorder 시작 후 보이는 상태여야 합니다.");
        assertEquals(1.0f, animation.scaleX, 0.01f, "hueBorder 시작 후 scaleX가 1이어야 합니다.");
        assertEquals(1.0f, animation.scaleY, 0.01f, "hueBorder 시작 후 scaleY가 1이어야 합니다.");
        
        System.out.println("✅ hueBorder 애니메이션 효과 테스트 통과");
    }

    @Test
    @Order(8)
    @DisplayName("8. saturateBorder 애니메이션 효과 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testSaturateBorderAnimation() {
        System.out.println("=== 8. saturateBorder 애니메이션 효과 테스트 ===");

        // 헤드리스 환경 체크
        if (GraphicsEnvironment.isHeadless() || animation == null) {
            System.out.println("⚠️ 헤드리스 환경이거나 Animation이 null이므로 테스트를 건너뜁니다.");
            return;
        }
        
        // saturateBorder 애니메이션 시작
        assertDoesNotThrow(() -> {
            animation.saturateBorder(1.0f, false);
        }, "saturateBorder 애니메이션이 예외를 발생시키지 않아야 합니다.");
        
        // 애니메이션 상태 확인
        assertEquals(1.0f, animation.alpha, 0.01f, "saturateBorder 시작 후 alpha가 1이어야 합니다.");
        assertTrue(animation.bVisible, "saturateBorder 시작 후 보이는 상태여야 합니다.");
        assertEquals(1.0f, animation.scaleX, 0.01f, "saturateBorder 시작 후 scaleX가 1이어야 합니다.");
        assertEquals(1.0f, animation.scaleY, 0.01f, "saturateBorder 시작 후 scaleY가 1이어야 합니다.");
        
        System.out.println("✅ saturateBorder 애니메이션 효과 테스트 통과");
    }

    @Test
    @Order(9)
    @DisplayName("9. Animation release 메서드 테스트")
    void testAnimationRelease() {
        System.out.println("=== 9. Animation release 메서드 테스트 ===");

        // 헤드리스 환경 체크
        if (GraphicsEnvironment.isHeadless() || animation == null) {
            System.out.println("⚠️ 헤드리스 환경이거나 Animation이 null이므로 테스트를 건너뜁니다.");
            return;
        }
        
        // 애니메이션 시작 후 release 테스트
        animation.blink(0.1f, 0.1f);
        
        // release 호출
        assertDoesNotThrow(() -> {
            animation.release();
        }, "release 메서드가 예외를 발생시키지 않아야 합니다.");
        
        System.out.println("✅ Animation release 메서드 테스트 통과");
    }

    @Test
    @Order(10)
    @DisplayName("10. Animation 다중 효과 동시 실행 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testMultipleAnimationEffects() {
        System.out.println("=== 10. Animation 다중 효과 동시 실행 테스트 ===");

        // 헤드리스 환경 체크
        if (GraphicsEnvironment.isHeadless() || animation == null) {
            System.out.println("⚠️ 헤드리스 환경이거나 Animation이 null이므로 테스트를 건너뜁니다.");
            return;
        }
        
        // 여러 애니메이션 효과 동시 실행
        assertDoesNotThrow(() -> {
            animation.blink(0.1f, 0.1f);
            // 약간의 지연 후 다른 효과 추가
            Thread.sleep(50);
            animation.hueBackground(0.5f, false);
            Thread.sleep(50);
            animation.hueBorder(0.5f, false);
        }, "다중 애니메이션 효과가 예외를 발생시키지 않아야 합니다.");
        
        System.out.println("✅ Animation 다중 효과 동시 실행 테스트 통과");
    }
}