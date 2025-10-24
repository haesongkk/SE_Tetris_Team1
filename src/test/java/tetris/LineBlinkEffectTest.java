package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import tetris.util.LineBlinkEffect;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeUnit;

/**
 * LineBlinkEffect 클래스의 JUnit 테스트
 * 줄 점멸 효과의 타이밍, 콜백 시스템, 상태 관리를 테스트합니다.
 */
@DisplayName("줄 점멸 효과 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LineBlinkEffectTest {

    private LineBlinkEffect blinkEffect;
    private AtomicBoolean onBlinkCompleteCalled;
    private AtomicBoolean onEffectUpdateCalled;
    private LineBlinkEffect.BlinkEffectCallback testCallback;

    @BeforeEach
    @DisplayName("테스트 환경 설정")
    void setUp() {
        System.out.println("=== LineBlinkEffect 테스트 환경 설정 ===");
        
        // 콜백 상태 초기화
        onBlinkCompleteCalled = new AtomicBoolean(false);
        onEffectUpdateCalled = new AtomicBoolean(false);
        
        // 테스트용 콜백 구현
        testCallback = new LineBlinkEffect.BlinkEffectCallback() {
            @Override
            public void onBlinkComplete() {
                onBlinkCompleteCalled.set(true);
                System.out.println("✅ onBlinkComplete 콜백 호출됨");
            }

            @Override
            public void onEffectUpdate() {
                onEffectUpdateCalled.set(true);
                System.out.println("📊 onEffectUpdate 콜백 호출됨");
            }
        };
        
        // LineBlinkEffect 인스턴스 생성
        blinkEffect = new LineBlinkEffect(testCallback);
        
        System.out.println("✅ LineBlinkEffect 테스트 환경 설정 완료");
    }

    @AfterEach
    @DisplayName("테스트 환경 정리")
    void tearDown() {
        if (blinkEffect != null) {
            blinkEffect.stop();
        }
        System.out.println("🧹 LineBlinkEffect 테스트 환경 정리 완료");
    }

    @Test
    @Order(1)
    @DisplayName("1. LineBlinkEffect 생성 및 초기 상태 테스트")
    void testLineBlinkEffectCreation() {
        System.out.println("=== 1. LineBlinkEffect 생성 및 초기 상태 테스트 ===");

        // 생성 확인
        assertNotNull(blinkEffect, "LineBlinkEffect가 정상적으로 생성되어야 합니다.");
        
        // 초기 상태 확인
        assertFalse(blinkEffect.isActive(), "초기 상태에서는 비활성화되어 있어야 합니다.");
        assertTrue(blinkEffect.getBlinkingLines().isEmpty(), "초기 상태에서는 점멸 중인 줄이 없어야 합니다.");
        
        System.out.println("✅ LineBlinkEffect 생성 및 초기 상태 테스트 통과");
    }

    @Test
    @Order(2)
    @DisplayName("2. 점멸 효과 시작 테스트")
    void testStartBlinkEffect() {
        System.out.println("=== 2. 점멸 효과 시작 테스트 ===");

        List<Integer> testLines = Arrays.asList(5, 10, 15);
        
        // 점멸 효과 시작
        blinkEffect.startBlinkEffect(testLines);
        
        // 상태 확인
        assertTrue(blinkEffect.isActive(), "점멸 효과가 활성화되어야 합니다.");
        assertEquals(testLines.size(), blinkEffect.getBlinkingLines().size(), 
                     "점멸 중인 줄 수가 일치해야 합니다.");
        
        // 각 줄이 점멸 중인지 확인
        for (Integer lineNumber : testLines) {
            assertTrue(blinkEffect.isLineBlinking(lineNumber), 
                      "줄 " + lineNumber + "이 점멸 중이어야 합니다.");
        }
        
        System.out.println("✅ 점멸 효과 시작 테스트 통과");
    }

    @Test
    @Order(3)
    @DisplayName("3. 점멸 효과 업데이트 및 콜백 테스트")
    void testBlinkEffectUpdate() throws InterruptedException {
        System.out.println("=== 3. 점멸 효과 업데이트 및 콜백 테스트 ===");

        List<Integer> testLines = Arrays.asList(7, 12);
        
        // 점멸 효과 시작
        blinkEffect.startBlinkEffect(testLines);
        
        // 업데이트 호출
        blinkEffect.update();
        
        // 업데이트 콜백이 호출되었는지 확인
        assertTrue(onEffectUpdateCalled.get(), 
                   "onEffectUpdate 콜백이 호출되어야 합니다.");
        
        // 여전히 활성화되어 있는지 확인 (짧은 시간)
        assertTrue(blinkEffect.isActive(), 
                   "짧은 시간 후에도 점멸 효과가 활성화되어 있어야 합니다.");
        
        System.out.println("✅ 점멸 효과 업데이트 및 콜백 테스트 통과");
    }

    @Test
    @Order(4)
    @DisplayName("4. 점멸 효과 완료 타이밍 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testBlinkEffectCompletion() throws InterruptedException {
        System.out.println("=== 4. 점멸 효과 완료 타이밍 테스트 ===");

        List<Integer> testLines = Arrays.asList(3, 8, 13);
        
        // 점멸 효과 시작
        blinkEffect.startBlinkEffect(testLines);
        
        // 효과 지속시간 (900ms) 동안 주기적으로 업데이트
        long startTime = System.currentTimeMillis();
        long maxDuration = 1200; // 여유를 두고 1.2초
        
        while (System.currentTimeMillis() - startTime < maxDuration) {
            blinkEffect.update();
            
            if (!blinkEffect.isActive()) {
                break; // 효과가 완료되면 루프 종료
            }
            
            Thread.sleep(50); // 50ms마다 업데이트
        }
        
        // 효과 완료 확인
        assertFalse(blinkEffect.isActive(), 
                    "점멸 효과가 지정된 시간 후에 완료되어야 합니다.");
        assertTrue(onBlinkCompleteCalled.get(), 
                   "onBlinkComplete 콜백이 호출되어야 합니다.");
        
        System.out.println("✅ 점멸 효과 완료 타이밍 테스트 통과");
    }

    @Test
    @Order(5)
    @DisplayName("5. 점멸 효과 중지 테스트")
    void testStopBlinkEffect() {
        System.out.println("=== 5. 점멸 효과 중지 테스트 ===");

        List<Integer> testLines = Arrays.asList(1, 19);
        
        // 점멸 효과 시작
        blinkEffect.startBlinkEffect(testLines);
        assertTrue(blinkEffect.isActive(), "점멸 효과가 시작되어야 합니다.");
        
        // 강제 중지
        blinkEffect.stop();
        
        // 중지 상태 확인
        assertFalse(blinkEffect.isActive(), "점멸 효과가 중지되어야 합니다.");
        assertTrue(blinkEffect.getBlinkingLines().isEmpty(), 
                   "점멸 중인 줄 리스트가 비어있어야 합니다.");
        
        // 점멸 상태 확인
        for (Integer lineNumber : testLines) {
            assertFalse(blinkEffect.isLineBlinking(lineNumber), 
                       "줄 " + lineNumber + "이 더 이상 점멸하지 않아야 합니다.");
        }
        
        System.out.println("✅ 점멸 효과 중지 테스트 통과");
    }

    @Test
    @Order(6)
    @DisplayName("6. 개별 줄 점멸 상태 테스트")
    void testIndividualLineBlinkingState() {
        System.out.println("=== 6. 개별 줄 점멸 상태 테스트 ===");

        List<Integer> blinkingLines = Arrays.asList(2, 6, 14);
        List<Integer> nonBlinkingLines = Arrays.asList(0, 4, 9, 18);
        
        // 점멸 효과 시작
        blinkEffect.startBlinkEffect(blinkingLines);
        
        // 점멸 중인 줄 확인
        for (Integer lineNumber : blinkingLines) {
            assertTrue(blinkEffect.isLineBlinking(lineNumber), 
                      "줄 " + lineNumber + "이 점멸 중이어야 합니다.");
        }
        
        // 점멸하지 않는 줄 확인
        for (Integer lineNumber : nonBlinkingLines) {
            assertFalse(blinkEffect.isLineBlinking(lineNumber), 
                       "줄 " + lineNumber + "이 점멸하지 않아야 합니다.");
        }
        
        System.out.println("✅ 개별 줄 점멸 상태 테스트 통과");
    }

    @Test
    @Order(7)
    @DisplayName("7. 빈 줄 리스트로 점멸 효과 시작 테스트")
    void testBlinkEffectWithEmptyLines() {
        System.out.println("=== 7. 빈 줄 리스트로 점멸 효과 시작 테스트 ===");

        // 빈 리스트로 점멸 효과 시작
        blinkEffect.startBlinkEffect(Arrays.asList());
        
        // 상태 확인
        assertTrue(blinkEffect.isActive(), "빈 리스트라도 효과는 활성화되어야 합니다.");
        assertTrue(blinkEffect.getBlinkingLines().isEmpty(), 
                   "점멸 중인 줄이 없어야 합니다.");
        
        System.out.println("✅ 빈 줄 리스트로 점멸 효과 시작 테스트 통과");
    }

    @Test
    @Order(8)
    @DisplayName("8. 콜백 없이 LineBlinkEffect 생성 테스트")
    void testLineBlinkEffectWithoutCallback() {
        System.out.println("=== 8. 콜백 없이 LineBlinkEffect 생성 테스트 ===");

        // null 콜백으로 생성
        LineBlinkEffect nullCallbackEffect = new LineBlinkEffect(null);
        
        assertNotNull(nullCallbackEffect, "null 콜백으로도 생성되어야 합니다.");
        assertFalse(nullCallbackEffect.isActive(), "초기 상태는 비활성화되어야 합니다.");
        
        // 점멸 효과 시작 및 업데이트 (예외 발생하지 않아야 함)
        assertDoesNotThrow(() -> {
            nullCallbackEffect.startBlinkEffect(Arrays.asList(5));
            nullCallbackEffect.update();
            nullCallbackEffect.stop();
        }, "null 콜백으로도 정상 동작해야 합니다.");
        
        System.out.println("✅ 콜백 없이 LineBlinkEffect 생성 테스트 통과");
    }

    @Test
    @Order(9)
    @DisplayName("9. 중복 줄 번호 처리 테스트")
    void testDuplicateLineNumbers() {
        System.out.println("=== 9. 중복 줄 번호 처리 테스트 ===");

        List<Integer> linesWithDuplicates = Arrays.asList(5, 10, 5, 15, 10, 5);
        
        // 중복이 있는 줄 리스트로 점멸 효과 시작
        blinkEffect.startBlinkEffect(linesWithDuplicates);
        
        // 실제 점멸 중인 줄 확인 (중복 제거되어야 함)
        List<Integer> blinkingLines = blinkEffect.getBlinkingLines();
        assertTrue(blinkingLines.contains(5), "줄 5가 점멸 중이어야 합니다.");
        assertTrue(blinkingLines.contains(10), "줄 10이 점멸 중이어야 합니다.");
        assertTrue(blinkingLines.contains(15), "줄 15가 점멸 중이어야 합니다.");
        
        System.out.println("점멸 중인 줄: " + blinkingLines);
        System.out.println("✅ 중복 줄 번호 처리 테스트 통과");
    }

    @Test
    @Order(10)
    @DisplayName("10. 연속 점멸 효과 시작 테스트")
    void testConsecutiveBlinkEffects() {
        System.out.println("=== 10. 연속 점멸 효과 시작 테스트 ===");

        // 첫 번째 점멸 효과
        blinkEffect.startBlinkEffect(Arrays.asList(1, 2, 3));
        assertTrue(blinkEffect.isActive(), "첫 번째 점멸 효과가 활성화되어야 합니다.");
        
        // 두 번째 점멸 효과 (이전 효과를 덮어씀)
        blinkEffect.startBlinkEffect(Arrays.asList(17, 18, 19));
        assertTrue(blinkEffect.isActive(), "두 번째 점멸 효과가 활성화되어야 합니다.");
        
        // 새로운 줄들이 점멸 중인지 확인
        assertTrue(blinkEffect.isLineBlinking(17), "줄 17이 점멸 중이어야 합니다.");
        assertTrue(blinkEffect.isLineBlinking(18), "줄 18이 점멸 중이어야 합니다.");
        assertTrue(blinkEffect.isLineBlinking(19), "줄 19가 점멸 중이어야 합니다.");
        
        // 이전 줄들은 더 이상 점멸하지 않아야 함
        assertFalse(blinkEffect.isLineBlinking(1), "줄 1이 더 이상 점멸하지 않아야 합니다.");
        assertFalse(blinkEffect.isLineBlinking(2), "줄 2가 더 이상 점멸하지 않아야 합니다.");
        assertFalse(blinkEffect.isLineBlinking(3), "줄 3이 더 이상 점멸하지 않아야 합니다.");
        
        System.out.println("✅ 연속 점멸 효과 시작 테스트 통과");
    }
}