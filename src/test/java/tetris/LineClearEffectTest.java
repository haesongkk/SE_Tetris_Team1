package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import tetris.scene.game.items.effects.LineClearEffect;
import tetris.scene.game.items.ItemEffectContext;
import tetris.scene.game.items.ItemEffectType;
import java.awt.Color;
import java.util.concurrent.TimeUnit;

/**
 * LineClearEffect 클래스의 JUnit 테스트
 * LINE_CLEAR 아이템의 줄 채우기, 블링킹 통합, 정상 줄 삭제 시스템과의 통합을 테스트합니다.
 */
@DisplayName("LINE_CLEAR 아이템 효과 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LineClearEffectTest {

    private LineClearEffect lineClearEffect;
    private ItemEffectContext testContext;

    @BeforeEach
    @DisplayName("테스트 환경 설정")
    void setUp() {
        System.out.println("=== LineClearEffect 테스트 환경 설정 ===");
        
        lineClearEffect = new LineClearEffect();
        
        // 테스트용 보드 생성 (20x10)
        int[][] testBoard = new int[20][10];
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 10; x++) {
                testBoard[y][x] = 0;
            }
        }
        
        testContext = new ItemEffectContext(testBoard, 5, 10); // 보드 중앙 위치
        
        System.out.println("✅ LineClearEffect 테스트 환경 설정 완료");
    }

    @AfterEach
    @DisplayName("테스트 환경 정리")
    void tearDown() {
        if (lineClearEffect != null && lineClearEffect.isActive()) {
            lineClearEffect.deactivate();
        }
        System.out.println("🧹 LineClearEffect 테스트 환경 정리 완료");
    }

    @Test
    @Order(1)
    @DisplayName("1. LineClearEffect 생성 및 기본 속성 테스트")
    void testLineClearEffectCreation() {
        System.out.println("=== 1. LineClearEffect 생성 및 기본 속성 테스트 ===");

        // 생성 확인
        assertNotNull(lineClearEffect, "LineClearEffect가 정상적으로 생성되어야 합니다.");
        
        // 기본 속성 확인
        assertEquals(0, lineClearEffect.getDuration(), 
                     "LINE_CLEAR는 즉시 효과이므로 지속시간이 0이어야 합니다.");
        assertFalse(lineClearEffect.isActive(), 
                    "초기 상태에서는 비활성화되어 있어야 합니다.");
        
        System.out.println("✅ LineClearEffect 생성 및 기본 속성 테스트 통과");
    }

    @Test
    @Order(2)
    @DisplayName("2. 부분적으로 채워진 줄의 LINE_CLEAR 효과 테스트")
    void testLineClearEffectOnPartiallyFilledLine() {
        System.out.println("=== 2. 부분적으로 채워진 줄의 LINE_CLEAR 효과 테스트 ===");

        int targetY = 15;
        int[][] board = testContext.getBoard();
        
        // ItemEffectContext를 새로 생성하여 원하는 위치로 설정
        testContext = new ItemEffectContext(board, 5, targetY);
        
        // 테스트 줄을 부분적으로 채움 (3개 셀만 채움)
        board[targetY][2] = 1;
        board[targetY][5] = 1; // 아이템 위치
        board[targetY][8] = 1;
        
        // 초기 상태 확인
        int initialFilledCells = countFilledCellsInLine(board, targetY);
        assertEquals(3, initialFilledCells, "초기에 3개 셀이 채워져 있어야 합니다.");
        
        // LINE_CLEAR 효과 활성화
        lineClearEffect.activate(testContext);
        
        // 효과 후 줄이 완전히 채워졌는지 확인
        int finalFilledCells = countFilledCellsInLine(board, targetY);
        assertEquals(10, finalFilledCells, "LINE_CLEAR 효과 후 줄이 완전히 채워져야 합니다.");
        
        // 다른 줄은 영향받지 않았는지 확인
        for (int y = 0; y < 20; y++) {
            if (y != targetY) {
                assertEquals(0, countFilledCellsInLine(board, y), 
                           "다른 줄 " + y + "는 영향받지 않아야 합니다.");
            }
        }
        
        System.out.println("✅ 부분적으로 채워진 줄의 LINE_CLEAR 효과 테스트 통과");
    }

    @Test
    @Order(3)
    @DisplayName("3. 빈 줄의 LINE_CLEAR 효과 테스트")
    void testLineClearEffectOnEmptyLine() {
        System.out.println("=== 3. 빈 줄의 LINE_CLEAR 효과 테스트 ===");

        int targetY = 18;
        int[][] board = testContext.getBoard();
        testContext = new ItemEffectContext(board, 3, targetY);
        
        // 줄이 완전히 비어있는 상태 확인
        assertEquals(0, countFilledCellsInLine(board, targetY), 
                     "초기에 줄이 비어있어야 합니다.");
        
        // LINE_CLEAR 효과 활성화
        lineClearEffect.activate(testContext);
        
        // 효과 후 줄이 완전히 채워졌는지 확인
        assertEquals(10, countFilledCellsInLine(board, targetY), 
                     "빈 줄도 LINE_CLEAR 효과로 완전히 채워져야 합니다.");
        
        System.out.println("✅ 빈 줄의 LINE_CLEAR 효과 테스트 통과");
    }

    @Test
    @Order(4)
    @DisplayName("4. 이미 완성된 줄의 LINE_CLEAR 효과 테스트")
    void testLineClearEffectOnCompletedLine() {
        System.out.println("=== 4. 이미 완성된 줄의 LINE_CLEAR 효과 테스트 ===");

        int targetY = 12;
        int[][] board = testContext.getBoard();
        testContext = new ItemEffectContext(board, 4, targetY);
        
        // 줄을 완전히 채움
        for (int x = 0; x < 10; x++) {
            board[targetY][x] = 1;
        }
        
        // 초기 상태 확인
        assertEquals(10, countFilledCellsInLine(board, targetY), 
                     "초기에 줄이 완전히 채워져 있어야 합니다.");
        
        // LINE_CLEAR 효과 활성화
        lineClearEffect.activate(testContext);
        
        // 효과 후에도 여전히 완전히 채워져 있는지 확인
        assertEquals(10, countFilledCellsInLine(board, targetY), 
                     "이미 완성된 줄은 그대로 유지되어야 합니다.");
        
        System.out.println("✅ 이미 완성된 줄의 LINE_CLEAR 효과 테스트 통과");
    }

    @Test
    @Order(5)
    @DisplayName("5. 경계 위치에서의 LINE_CLEAR 효과 테스트")
    void testLineClearEffectOnBoundaryPositions() {
        System.out.println("=== 5. 경계 위치에서의 LINE_CLEAR 효과 테스트 ===");

        // 맨 위 줄 테스트
        int[][] topBoard = new int[20][10];
        ItemEffectContext topContext = new ItemEffectContext(topBoard, 0, 0);
        topBoard[0][0] = 1;
        topBoard[0][9] = 1;
        
        LineClearEffect topEffect = new LineClearEffect();
        topEffect.activate(topContext);
        
        assertEquals(10, countFilledCellsInLine(topBoard, 0), 
                     "맨 위 줄도 정상적으로 채워져야 합니다.");
        
        // 맨 아래 줄 테스트
        int[][] bottomBoard = new int[20][10];
        ItemEffectContext bottomContext = new ItemEffectContext(bottomBoard, 9, 19);
        bottomBoard[19][1] = 1;
        bottomBoard[19][5] = 1;
        
        LineClearEffect bottomEffect = new LineClearEffect();
        bottomEffect.activate(bottomContext);
        
        assertEquals(10, countFilledCellsInLine(bottomBoard, 19), 
                     "맨 아래 줄도 정상적으로 채워져야 합니다.");
        
        System.out.println("✅ 경계 위치에서의 LINE_CLEAR 효과 테스트 통과");
    }

    @Test
    @Order(6)
    @DisplayName("6. LINE_CLEAR 효과의 즉시 활성화/비활성화 테스트")
    void testLineClearEffectInstantActivation() {
        System.out.println("=== 6. LINE_CLEAR 효과의 즉시 활성화/비활성화 테스트 ===");

        // 활성화 전 상태
        assertFalse(lineClearEffect.isActive(), "활성화 전에는 비활성 상태여야 합니다.");
        
        // 효과 활성화
        lineClearEffect.activate(testContext);
        
        // LINE_CLEAR는 즉시 효과이므로 활성화 후에도 여전히 비활성 상태여야 함
        assertFalse(lineClearEffect.isActive(), 
                    "LINE_CLEAR는 즉시 효과이므로 활성화 후에도 비활성 상태여야 합니다.");
        
        // 비활성화 호출 (예외 발생하지 않아야 함)
        assertDoesNotThrow(() -> lineClearEffect.deactivate(), 
                          "이미 비활성 상태인 효과의 deactivate는 안전해야 합니다.");
        
        System.out.println("✅ LINE_CLEAR 효과의 즉시 활성화/비활성화 테스트 통과");
    }

    @Test
    @Order(7)
    @DisplayName("7. 잘못된 좌표에서의 LINE_CLEAR 효과 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testLineClearEffectWithInvalidCoordinates() {
        System.out.println("=== 7. 잘못된 좌표에서의 LINE_CLEAR 효과 테스트 ===");

        int[][] board = new int[20][10];
        
        // 음수 Y 좌표
        ItemEffectContext invalidContextNegative = new ItemEffectContext(board, 5, -1);
        LineClearEffect negativeEffect = new LineClearEffect();
        
        assertDoesNotThrow(() -> negativeEffect.activate(invalidContextNegative), 
                          "음수 Y 좌표에서도 예외가 발생하지 않아야 합니다.");
        
        // 범위를 벗어난 Y 좌표
        ItemEffectContext invalidContextOver = new ItemEffectContext(board, 5, 25);
        LineClearEffect overEffect = new LineClearEffect();
        
        assertDoesNotThrow(() -> overEffect.activate(invalidContextOver), 
                          "범위를 벗어난 Y 좌표에서도 예외가 발생하지 않아야 합니다.");
        
        // 원본 보드는 변경되지 않았는지 확인
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 10; x++) {
                assertEquals(0, board[y][x], 
                           "잘못된 좌표 테스트로 인해 원본 보드가 변경되지 않아야 합니다.");
            }
        }
        
        System.out.println("✅ 잘못된 좌표에서의 LINE_CLEAR 효과 테스트 통과");
    }

    /**
     * 지정된 줄에서 채워진 셀의 개수를 계산하는 헬퍼 메서드
     */
    private int countFilledCellsInLine(int[][] board, int lineY) {
        if (lineY < 0 || lineY >= board.length) {
            return 0;
        }
        
        int count = 0;
        for (int x = 0; x < board[lineY].length; x++) {
            if (board[lineY][x] == 1) {
                count++;
            }
        }
        return count;
    }
}