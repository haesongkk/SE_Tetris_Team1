package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import tetris.scene.game.core.BoardManager;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * BoardManager 클래스의 새로 추가된 메서드들에 대한 JUnit 테스트
 * clearCompletedAndBombLinesSeparately, forceClearLine, triggerLineCheck 등의 기능을 테스트합니다.
 */
@DisplayName("BoardManager 새 메서드 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BoardManagerNewMethodsTest {

    private BoardManager boardManager;

    @BeforeEach
    @DisplayName("테스트 환경 설정")
    void setUp() {
        System.out.println("=== BoardManager 새 메서드 테스트 환경 설정 ===");
        
        boardManager = new BoardManager();
        
        System.out.println("✅ BoardManager 새 메서드 테스트 환경 설정 완료");
    }

    @AfterEach
    @DisplayName("테스트 환경 정리")
    void tearDown() {
        if (boardManager != null) {
            boardManager.reset();
        }
        System.out.println("🧹 BoardManager 새 메서드 테스트 환경 정리 완료");
    }

    @Test
    @Order(1)
    @DisplayName("1. clearCompletedAndBombLinesSeparately 메서드 기본 테스트")
    void testClearCompletedAndBombLinesSeparately() {
        System.out.println("=== 1. clearCompletedAndBombLinesSeparately 메서드 기본 테스트 ===");

        // 완성된 줄 생성 (줄 18)
        fillLine(18);
        
        // 완성된 줄과 폭탄 줄을 구분해서 삭제
        int[] result = boardManager.clearCompletedAndBombLinesSeparately();
        
        // 결과 검증
        assertNotNull(result, "결과 배열이 null이 아니어야 합니다.");
        assertEquals(2, result.length, "결과 배열은 길이가 2여야 합니다. [완성된 줄 수, 폭탄 줄 수]");
        assertEquals(1, result[0], "완성된 줄이 1개 삭제되어야 합니다.");
        assertEquals(0, result[1], "폭탄 줄은 0개여야 합니다.");
        
        // 해당 줄이 삭제되었는지 확인
        assertFalse(isLineFull(18), "줄 18이 삭제되어야 합니다.");
        
        System.out.println("✅ clearCompletedAndBombLinesSeparately 메서드 기본 테스트 통과");
    }

    @Test
    @Order(2)
    @DisplayName("2. 폭탄이 있는 줄 삭제 테스트")
    void testBombLinesClear() {
        System.out.println("=== 2. 폭탄이 있는 줄 삭제 테스트 ===");

        // 폭탄 셀이 있는 줄 생성 (줄 15)
        setBombCell(5, 15, true);
        setCell(5, 15, 1); // 폭탄 셀에 블록도 배치
        setCell(3, 15, 1); // 다른 블록들도 배치
        setCell(7, 15, 1);
        
        // 폭탄 줄 확인
        List<Integer> bombLines = boardManager.getBombLines();
        assertTrue(bombLines.contains(15), "줄 15가 폭탄 줄로 인식되어야 합니다.");
        
        // 완성된 줄과 폭탄 줄을 구분해서 삭제
        int[] result = boardManager.clearCompletedAndBombLinesSeparately();
        
        // 결과 검증
        assertEquals(0, result[0], "완성된 줄은 0개여야 합니다.");
        assertEquals(1, result[1], "폭탄 줄이 1개 삭제되어야 합니다.");
        
        System.out.println("✅ 폭탄이 있는 줄 삭제 테스트 통과");
    }

    @Test
    @Order(3)
    @DisplayName("3. 완성된 줄과 폭탄 줄이 모두 있는 경우 테스트")
    void testMixedCompletedAndBombLines() {
        System.out.println("=== 3. 완성된 줄과 폭탄 줄이 모두 있는 경우 테스트 ===");

        // 완성된 줄 생성 (줄 19)
        fillLine(19);
        
        // 폭탄이 있는 부분적으로 채워진 줄 생성 (줄 16)
        setBombCell(2, 16, true);
        setCell(2, 16, 1);
        setCell(4, 16, 1);
        setCell(6, 16, 1);
        
        // 완성된 줄과 폭탄 줄을 구분해서 삭제
        int[] result = boardManager.clearCompletedAndBombLinesSeparately();
        
        // 결과 검증
        assertEquals(1, result[0], "완성된 줄이 1개 삭제되어야 합니다.");
        assertEquals(1, result[1], "폭탄 줄이 1개 삭제되어야 합니다.");
        
        // 두 줄 모두 삭제되었는지 확인
        assertFalse(isLineFull(19), "완성된 줄 19가 삭제되어야 합니다.");
        assertFalse(boardManager.isBombCell(2, 16), "폭탄 줄 16이 삭제되어야 합니다.");
        
        System.out.println("✅ 완성된 줄과 폭탄 줄이 모두 있는 경우 테스트 통과");
    }

    @Test
    @Order(4)
    @DisplayName("4. forceClearLine 메서드 테스트")
    void testForceClearLine() {
        System.out.println("=== 4. forceClearLine 메서드 테스트 ===");

        // 부분적으로 채워진 줄 생성 (줄 10)
        setCell(1, 10, 1);
        setCell(3, 10, 1);
        setCell(5, 10, 1);
        setCell(7, 10, 1);
        setCell(9, 10, 1);
        
        // 해당 줄이 완성되지 않았음을 확인
        assertFalse(boardManager.isLineFull(10), "줄 10이 완성되지 않은 상태여야 합니다.");
        
        // 강제로 줄 삭제
        boardManager.forceClearLine(10);
        
        // 줄이 삭제되었는지 확인 (위의 줄들이 아래로 이동)
        // 현재 줄 10이 비어있어야 함
        for (int x = 0; x < 10; x++) {
            assertEquals(0, boardManager.getBoard()[10][x], 
                        "강제 삭제 후 줄 10의 x=" + x + " 위치가 비어있어야 합니다.");
        }
        
        System.out.println("✅ forceClearLine 메서드 테스트 통과");
    }

    @Test
    @Order(5)
    @DisplayName("5. 잘못된 인덱스로 forceClearLine 호출 테스트")
    void testForceClearLineInvalidIndex() {
        System.out.println("=== 5. 잘못된 인덱스로 forceClearLine 호출 테스트 ===");

        // 음수 인덱스
        assertDoesNotThrow(() -> boardManager.forceClearLine(-1), 
                          "음수 인덱스로 forceClearLine 호출이 예외를 발생시키지 않아야 합니다.");
        
        // 범위를 벗어난 인덱스
        assertDoesNotThrow(() -> boardManager.forceClearLine(25), 
                          "범위를 벗어난 인덱스로 forceClearLine 호출이 예외를 발생시키지 않아야 합니다.");
        
        // 경계값 테스트
        assertDoesNotThrow(() -> boardManager.forceClearLine(0), 
                          "경계값 0으로 forceClearLine 호출이 예외를 발생시키지 않아야 합니다.");
        assertDoesNotThrow(() -> boardManager.forceClearLine(19), 
                          "경계값 19로 forceClearLine 호출이 예외를 발생시키지 않아야 합니다.");
        
        System.out.println("✅ 잘못된 인덱스로 forceClearLine 호출 테스트 통과");
    }

    @Test
    @Order(6)
    @DisplayName("6. triggerLineCheck 메서드 테스트")
    void testTriggerLineCheck() {
        System.out.println("=== 6. triggerLineCheck 메서드 테스트 ===");

        // 완성된 줄 생성 (줄 17)
        fillLine(17);
        
        // triggerLineCheck 호출
        assertDoesNotThrow(() -> boardManager.triggerLineCheck(), 
                          "triggerLineCheck 호출이 예외를 발생시키지 않아야 합니다.");
        
        System.out.println("✅ triggerLineCheck 메서드 테스트 통과");
    }

    @Test
    @Order(7)
    @DisplayName("7. getBombLines 메서드 테스트")
    void testGetBombLines() {
        System.out.println("=== 7. getBombLines 메서드 테스트 ===");

        // 여러 줄에 폭탄 셀 배치
        setBombCell(3, 5, true);
        setBombCell(7, 10, true);
        setBombCell(2, 15, true);
        
        // 폭탄이 있는 줄들 확인
        List<Integer> bombLines = boardManager.getBombLines();
        
        assertNotNull(bombLines, "폭탄 줄 리스트가 null이 아니어야 합니다.");
        assertTrue(bombLines.contains(5), "줄 5가 폭탄 줄 리스트에 포함되어야 합니다.");
        assertTrue(bombLines.contains(10), "줄 10이 폭탄 줄 리스트에 포함되어야 합니다.");
        assertTrue(bombLines.contains(15), "줄 15가 폭탄 줄 리스트에 포함되어야 합니다.");
        assertEquals(3, bombLines.size(), "폭탄 줄이 정확히 3개여야 합니다.");
        
        System.out.println("✅ getBombLines 메서드 테스트 통과");
    }

    @Test
    @Order(8)
    @DisplayName("8. 폭탄 셀이 없는 경우 getBombLines 테스트")
    void testGetBombLinesEmpty() {
        System.out.println("=== 8. 폭탄 셀이 없는 경우 getBombLines 테스트 ===");

        // 폭탄 셀이 없는 상태에서 확인
        List<Integer> bombLines = boardManager.getBombLines();
        
        assertNotNull(bombLines, "폭탄 줄 리스트가 null이 아니어야 합니다.");
        assertTrue(bombLines.isEmpty(), "폭탄 셀이 없으면 빈 리스트를 반환해야 합니다.");
        
        System.out.println("✅ 폭탄 셀이 없는 경우 getBombLines 테스트 통과");
    }

    @Test
    @Order(9)
    @DisplayName("9. 여러 줄 동시 삭제 후 블록 재배치 테스트")
    void testMultipleLinesClearAndRearrangement() {
        System.out.println("=== 9. 여러 줄 동시 삭제 후 블록 재배치 테스트 ===");

        // 상위 줄에 블록 배치 (줄 5)
        setCell(0, 5, 1);
        setCell(1, 5, 1);
        setCell(2, 5, 1);
        
        // 하위에 완성된 줄들 생성 (줄 18, 19)
        fillLine(18);
        fillLine(19);
        
        // 삭제 전 상위 블록 위치 확인
        assertEquals(1, boardManager.getBoard()[5][0], "삭제 전 상위 블록이 있어야 합니다.");
        
        // 완성된 줄들 삭제
        int[] result = boardManager.clearCompletedAndBombLinesSeparately();
        assertEquals(2, result[0], "2개의 완성된 줄이 삭제되어야 합니다.");
        
        // 상위 블록들이 아래로 이동했는지 확인
        assertEquals(1, boardManager.getBoard()[7][0], "상위 블록이 2칸 아래로 이동해야 합니다.");
        assertEquals(1, boardManager.getBoard()[7][1], "상위 블록이 2칸 아래로 이동해야 합니다.");
        assertEquals(1, boardManager.getBoard()[7][2], "상위 블록이 2칸 아래로 이동해야 합니다.");
        
        // 원래 위치는 비어있어야 함
        assertEquals(0, boardManager.getBoard()[5][0], "원래 위치는 비어있어야 합니다.");
        
        System.out.println("✅ 여러 줄 동시 삭제 후 블록 재배치 테스트 통과");
    }

    @Test
    @Order(10)
    @DisplayName("10. processBombExplosions 메서드 독립 실행 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testProcessBombExplosions() {
        System.out.println("=== 10. processBombExplosions 메서드 독립 실행 테스트 ===");

        // 폭탄 셀이 있는 줄 생성
        setBombCell(4, 12, true);
        setCell(4, 12, 1);
        setCell(2, 12, 1);
        setCell(8, 12, 1);
        
        // 폭탄 폭발 처리 실행
        assertDoesNotThrow(() -> boardManager.processBombExplosions(), 
                          "processBombExplosions 호출이 예외를 발생시키지 않아야 합니다.");
        
        // 폭탄이 있던 줄이 제거되었는지 확인
        assertFalse(boardManager.isBombCell(4, 12), 
                   "폭탄 셀이 제거되어야 합니다.");
        
        System.out.println("✅ processBombExplosions 메서드 독립 실행 테스트 통과");
    }

    // 헬퍼 메서드들

    /**
     * 지정된 줄을 완전히 채우는 헬퍼 메서드
     */
    private void fillLine(int lineY) {
        for (int x = 0; x < 10; x++) {
            setCell(x, lineY, 1);
        }
    }

    /**
     * 지정된 위치에 블록을 설정하는 헬퍼 메서드
     */
    private void setCell(int x, int y, int value) {
        if (y >= 0 && y < 20 && x >= 0 && x < 10) {
            boardManager.getBoard()[y][x] = value;
            if (value == 1) {
                boardManager.getBoardColors()[y][x] = Color.BLUE; // 기본 색상
            } else {
                boardManager.getBoardColors()[y][x] = null;
            }
        }
    }

    /**
     * 지정된 위치의 폭탄 셀 상태를 설정하는 헬퍼 메서드
     */
    private void setBombCell(int x, int y, boolean isBomb) {
        if (y >= 0 && y < 20 && x >= 0 && x < 10) {
            boardManager.getBombCells()[y][x] = isBomb;
        }
    }

    /**
     * 지정된 줄이 완전히 채워져 있는지 확인하는 헬퍼 메서드
     */
    private boolean isLineFull(int lineY) {
        return boardManager.isLineFull(lineY);
    }
}