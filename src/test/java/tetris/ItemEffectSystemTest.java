package tetris;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import tetris.scene.game.items.*;
import tetris.scene.game.items.effects.*;
import java.util.concurrent.TimeUnit;

/**
 * ItemEffect 시스템 통합 테스트
 * ItemEffectFactory, ItemEffectContext, 각종 아이템 효과들의 통합 테스트를 수행합니다.
 */
@DisplayName("ItemEffect 시스템 통합 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ItemEffectSystemTest {

    private ItemEffectContext testContext;
    private int[][] testBoard;

    @BeforeEach
    @DisplayName("테스트 환경 설정")
    void setUp() {
        System.out.println("=== ItemEffect 시스템 통합 테스트 환경 설정 ===");
        
        // 테스트용 보드 생성
        testBoard = new int[20][10];
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 10; x++) {
                testBoard[y][x] = 0;
            }
        }
        
        testContext = new ItemEffectContext(testBoard, 5, 10);
        
        System.out.println("✅ ItemEffect 시스템 통합 테스트 환경 설정 완료");
    }

    @AfterEach
    @DisplayName("테스트 환경 정리")
    void tearDown() {
        System.out.println("🧹 ItemEffect 시스템 통합 테스트 환경 정리 완료");
    }

    @Test
    @Order(1)
    @DisplayName("1. ItemEffectFactory 기본 생성 테스트")
    void testItemEffectFactoryCreation() {
        System.out.println("=== 1. ItemEffectFactory 기본 생성 테스트 ===");

        // 모든 아이템 타입에 대해 효과 생성 테스트
        for (ItemEffectType type : ItemEffectType.values()) {
            ItemEffect effect = ItemEffectFactory.createEffect(type);
            
            assertNotNull(effect, type.name() + " 효과가 생성되어야 합니다.");
            System.out.println("✅ " + type.name() + " 효과 생성 성공");
        }
        
        System.out.println("✅ ItemEffectFactory 기본 생성 테스트 통과");
    }

    @Test
    @Order(2)
    @DisplayName("2. LINE_CLEAR 효과 팩토리 생성 및 실행 테스트")
    void testLineClearEffectFromFactory() {
        System.out.println("=== 2. LINE_CLEAR 효과 팩토리 생성 및 실행 테스트 ===");

        // 팩토리를 통한 LINE_CLEAR 효과 생성
        ItemEffect lineClearEffect = ItemEffectFactory.createEffect(ItemEffectType.LINE_CLEAR);
        
        assertNotNull(lineClearEffect, "LINE_CLEAR 효과가 생성되어야 합니다.");
        assertTrue(lineClearEffect instanceof LineClearEffect, 
                   "생성된 효과가 LineClearEffect 인스턴스여야 합니다.");
        
        // 부분적으로 채워진 줄에 효과 적용
        int targetY = 15;
        testContext = new ItemEffectContext(testBoard, 5, targetY);
        testBoard[targetY][2] = 1;
        testBoard[targetY][5] = 1;
        testBoard[targetY][8] = 1;
        
        // 효과 활성화
        lineClearEffect.activate(testContext);
        
        // 줄이 완전히 채워졌는지 확인
        int filledCells = 0;
        for (int x = 0; x < 10; x++) {
            if (testBoard[targetY][x] == 1) {
                filledCells++;
            }
        }
        assertEquals(10, filledCells, "LINE_CLEAR 효과로 줄이 완전히 채워져야 합니다.");
        
        System.out.println("✅ LINE_CLEAR 효과 팩토리 생성 및 실행 테스트 통과");
    }

    @Test
    @Order(3)
    @DisplayName("3. CLEANUP 효과 팩토리 생성 및 실행 테스트")
    void testCleanupEffectFromFactory() {
        System.out.println("=== 3. CLEANUP 효과 팩토리 생성 및 실행 테스트 ===");

        // 팩토리를 통한 CLEANUP 효과 생성
        ItemEffect cleanupEffect = ItemEffectFactory.createEffect(ItemEffectType.CLEANUP);
        
        assertNotNull(cleanupEffect, "CLEANUP 효과가 생성되어야 합니다.");
        assertTrue(cleanupEffect instanceof CleanupEffect, 
                   "생성된 효과가 CleanupEffect 인스턴스여야 합니다.");
        
        // 3x3 영역에 블록 배치
        int centerX = 5, centerY = 10;
        testContext = new ItemEffectContext(testBoard, centerX, centerY);
        
        // 중심점 주변에 블록들 배치
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int x = centerX + dx;
                int y = centerY + dy;
                if (x >= 0 && x < 10 && y >= 0 && y < 20) {
                    testBoard[y][x] = 1;
                }
            }
        }
        
        // 효과 활성화
        cleanupEffect.activate(testContext);
        
        // 3x3 영역이 청소되었는지 확인
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int x = centerX + dx;
                int y = centerY + dy;
                if (x >= 0 && x < 10 && y >= 0 && y < 20) {
                    assertEquals(0, testBoard[y][x], 
                               "CLEANUP 효과로 (" + x + ", " + y + ") 위치가 청소되어야 합니다.");
                }
            }
        }
        
        System.out.println("✅ CLEANUP 효과 팩토리 생성 및 실행 테스트 통과");
    }

    @Test
    @Order(4)
    @DisplayName("4. SPEED_DOWN 효과 팩토리 생성 및 기본 속성 테스트")
    void testSpeedDownEffectFromFactory() {
        System.out.println("=== 4. SPEED_DOWN 효과 팩토리 생성 및 기본 속성 테스트 ===");

        // 팩토리를 통한 SPEED_DOWN 효과 생성
        ItemEffect speedDownEffect = ItemEffectFactory.createEffect(ItemEffectType.SPEED_DOWN);
        
        assertNotNull(speedDownEffect, "SPEED_DOWN 효과가 생성되어야 합니다.");
        assertTrue(speedDownEffect instanceof SpeedDownEffect, 
                   "생성된 효과가 SpeedDownEffect 인스턴스여야 합니다.");
        
        // 지속시간이 있는 효과인지 확인
        assertTrue(speedDownEffect.getDuration() > 0, 
                  "SPEED_DOWN 효과는 지속시간이 있어야 합니다.");
        
        // 초기 상태 확인
        assertFalse(speedDownEffect.isActive(), 
                   "초기 상태에서는 비활성화되어 있어야 합니다.");
        
        // 효과 활성화
        speedDownEffect.activate(testContext);
        
        // 활성화 상태 확인
        assertTrue(speedDownEffect.isActive(), 
                  "활성화 후에는 활성 상태여야 합니다.");
        
        System.out.println("✅ SPEED_DOWN 효과 팩토리 생성 및 기본 속성 테스트 통과");
    }

    @Test
    @Order(5)
    @DisplayName("5. SPEED_UP 효과 팩토리 생성 및 기본 속성 테스트")
    void testSpeedUpEffectFromFactory() {
        System.out.println("=== 5. SPEED_UP 효과 팩토리 생성 및 기본 속성 테스트 ===");

        // 팩토리를 통한 SPEED_UP 효과 생성
        ItemEffect speedUpEffect = ItemEffectFactory.createEffect(ItemEffectType.SPEED_UP);
        
        assertNotNull(speedUpEffect, "SPEED_UP 효과가 생성되어야 합니다.");
        assertTrue(speedUpEffect instanceof SpeedUpEffect, 
                   "생성된 효과가 SpeedUpEffect 인스턴스여야 합니다.");
        
        // 지속시간이 있는 효과인지 확인
        assertTrue(speedUpEffect.getDuration() > 0, 
                  "SPEED_UP 효과는 지속시간이 있어야 합니다.");
        
        // 초기 상태 확인
        assertFalse(speedUpEffect.isActive(), 
                   "초기 상태에서는 비활성화되어 있어야 합니다.");
        
        // 효과 활성화
        speedUpEffect.activate(testContext);
        
        // 활성화 상태 확인
        assertTrue(speedUpEffect.isActive(), 
                  "활성화 후에는 활성 상태여야 합니다.");
        
        System.out.println("✅ SPEED_UP 효과 팩토리 생성 및 기본 속성 테스트 통과");
    }

    @Test
    @Order(6)
    @DisplayName("6. VISION_BLOCK 효과 팩토리 생성 및 기본 속성 테스트")
    void testVisionBlockEffectFromFactory() {
        System.out.println("=== 6. VISION_BLOCK 효과 팩토리 생성 및 기본 속성 테스트 ===");

        // 팩토리를 통한 VISION_BLOCK 효과 생성
        ItemEffect visionBlockEffect = ItemEffectFactory.createEffect(ItemEffectType.VISION_BLOCK);
        
        assertNotNull(visionBlockEffect, "VISION_BLOCK 효과가 생성되어야 합니다.");
        assertTrue(visionBlockEffect instanceof VisionBlockEffect, 
                   "생성된 효과가 VisionBlockEffect 인스턴스여야 합니다.");
        
        // 지속시간이 있는 효과인지 확인
        assertTrue(visionBlockEffect.getDuration() > 0, 
                  "VISION_BLOCK 효과는 지속시간이 있어야 합니다.");
        
        // 초기 상태 확인
        assertFalse(visionBlockEffect.isActive(), 
                   "초기 상태에서는 비활성화되어 있어야 합니다.");
        
        // 효과 활성화
        visionBlockEffect.activate(testContext);
        
        // 활성화 상태 확인
        assertTrue(visionBlockEffect.isActive(), 
                  "활성화 후에는 활성 상태여야 합니다.");
        
        System.out.println("✅ VISION_BLOCK 효과 팩토리 생성 및 기본 속성 테스트 통과");
    }

    @Test
    @Order(7)
    @DisplayName("7. ItemEffectContext getter 메서드 테스트")
    void testItemEffectContextGetters() {
        System.out.println("=== 7. ItemEffectContext getter 메서드 테스트 ===");

        // ItemEffectContext 생성
        int itemX = 7, itemY = 12;
        ItemEffectContext context = new ItemEffectContext(testBoard, itemX, itemY);
        
        // getter 메서드 테스트
        assertEquals(itemX, context.getItemX(), "getItemX()가 올바른 값을 반환해야 합니다.");
        assertEquals(itemY, context.getItemY(), "getItemY()가 올바른 값을 반환해야 합니다.");
        assertSame(testBoard, context.getBoard(), "getBoard()가 동일한 보드 객체를 반환해야 합니다.");
        
        // 보드 크기 확인
        assertEquals(20, context.getBoard().length, "보드 높이가 20이어야 합니다.");
        assertEquals(10, context.getBoard()[0].length, "보드 너비가 10이어야 합니다.");
        
        System.out.println("✅ ItemEffectContext getter 메서드 테스트 통과");
    }

    @Test
    @Order(8)
    @DisplayName("8. 여러 효과 동시 활성화 테스트")
    void testMultipleEffectsActivation() {
        System.out.println("=== 8. 여러 효과 동시 활성화 테스트 ===");

        // 여러 효과 생성
        ItemEffect speedDownEffect = ItemEffectFactory.createEffect(ItemEffectType.SPEED_DOWN);
        ItemEffect speedUpEffect = ItemEffectFactory.createEffect(ItemEffectType.SPEED_UP);
        ItemEffect visionBlockEffect = ItemEffectFactory.createEffect(ItemEffectType.VISION_BLOCK);
        
        // 모든 효과가 초기에는 비활성화 상태
        assertFalse(speedDownEffect.isActive(), "SPEED_DOWN 효과가 초기에 비활성화되어야 합니다.");
        assertFalse(speedUpEffect.isActive(), "SPEED_UP 효과가 초기에 비활성화되어야 합니다.");
        assertFalse(visionBlockEffect.isActive(), "VISION_BLOCK 효과가 초기에 비활성화되어야 합니다.");
        
        // 모든 효과 활성화
        speedDownEffect.activate(testContext);
        speedUpEffect.activate(testContext);
        visionBlockEffect.activate(testContext);
        
        // 모든 효과가 활성화 상태인지 확인
        assertTrue(speedDownEffect.isActive(), "SPEED_DOWN 효과가 활성화되어야 합니다.");
        assertTrue(speedUpEffect.isActive(), "SPEED_UP 효과가 활성화되어야 합니다.");
        assertTrue(visionBlockEffect.isActive(), "VISION_BLOCK 효과가 활성화되어야 합니다.");
        
        System.out.println("✅ 여러 효과 동시 활성화 테스트 통과");
    }

    @Test
    @Order(9)
    @DisplayName("9. 즉시 효과 vs 지속 효과 구분 테스트")
    void testInstantVsPersistentEffects() {
        System.out.println("=== 9. 즉시 효과 vs 지속 효과 구분 테스트 ===");

        // 즉시 효과들
        ItemEffect lineClearEffect = ItemEffectFactory.createEffect(ItemEffectType.LINE_CLEAR);
        ItemEffect cleanupEffect = ItemEffectFactory.createEffect(ItemEffectType.CLEANUP);
        
        // 지속 효과들
        ItemEffect speedDownEffect = ItemEffectFactory.createEffect(ItemEffectType.SPEED_DOWN);
        ItemEffect speedUpEffect = ItemEffectFactory.createEffect(ItemEffectType.SPEED_UP);
        ItemEffect visionBlockEffect = ItemEffectFactory.createEffect(ItemEffectType.VISION_BLOCK);
        
        // 즉시 효과들의 지속시간 확인
        assertEquals(0, lineClearEffect.getDuration(), "LINE_CLEAR는 즉시 효과여야 합니다.");
        assertEquals(0, cleanupEffect.getDuration(), "CLEANUP은 즉시 효과여야 합니다.");
        
        // 지속 효과들의 지속시간 확인
        assertTrue(speedDownEffect.getDuration() > 0, "SPEED_DOWN은 지속 효과여야 합니다.");
        assertTrue(speedUpEffect.getDuration() > 0, "SPEED_UP은 지속 효과여야 합니다.");
        assertTrue(visionBlockEffect.getDuration() > 0, "VISION_BLOCK은 지속 효과여야 합니다.");
        
        // 즉시 효과 활성화 후 상태 확인
        lineClearEffect.activate(testContext);
        cleanupEffect.activate(testContext);
        
        assertFalse(lineClearEffect.isActive(), "즉시 효과는 활성화 후 바로 비활성화되어야 합니다.");
        assertFalse(cleanupEffect.isActive(), "즉시 효과는 활성화 후 바로 비활성화되어야 합니다.");
        
        // 지속 효과 활성화 후 상태 확인
        speedDownEffect.activate(testContext);
        speedUpEffect.activate(testContext);
        visionBlockEffect.activate(testContext);
        
        assertTrue(speedDownEffect.isActive(), "지속 효과는 활성화 후 활성 상태를 유지해야 합니다.");
        assertTrue(speedUpEffect.isActive(), "지속 효과는 활성화 후 활성 상태를 유지해야 합니다.");
        assertTrue(visionBlockEffect.isActive(), "지속 효과는 활성화 후 활성 상태를 유지해야 합니다.");
        
        System.out.println("✅ 즉시 효과 vs 지속 효과 구분 테스트 통과");
    }

    @Test
    @Order(10)
    @DisplayName("10. 잘못된 ItemEffectType 처리 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testInvalidItemEffectType() {
        System.out.println("=== 10. 잘못된 ItemEffectType 처리 테스트 ===");

        // null 타입으로 효과 생성 시도
        assertThrows(Exception.class, () -> {
            ItemEffectFactory.createEffect(null);
        }, "null 타입으로 효과 생성 시 예외가 발생해야 합니다.");
        
        System.out.println("✅ 잘못된 ItemEffectType 처리 테스트 통과");
    }

    @Test
    @Order(11)
    @DisplayName("11. ItemEffectContext setter 메서드 테스트")
    void testItemEffectContextSetters() {
        System.out.println("=== 11. ItemEffectContext setter 메서드 테스트 ===");

        ItemEffectContext context = new ItemEffectContext(testBoard, 3, 7);
        
        // Mock 객체들
        Object mockGameScene = new Object();
        Object mockBoardManager = new Object();
        Object mockBlockManager = new Object();
        Object mockScoreManager = new Object();
        
        // setter 메서드 테스트
        assertDoesNotThrow(() -> {
            context.setGameScene(mockGameScene);
            context.setBoardManager(mockBoardManager);
            context.setBlockManager(mockBlockManager);
            context.setScoreManager(mockScoreManager);
        }, "setter 메서드들이 예외를 발생시키지 않아야 합니다.");
        
        // getter로 확인
        assertSame(mockGameScene, context.getGameScene(), 
                  "setGameScene으로 설정한 객체가 getGameScene으로 반환되어야 합니다.");
        assertSame(mockBoardManager, context.getBoardManager(), 
                  "setBoardManager로 설정한 객체가 getBoardManager로 반환되어야 합니다.");
        assertSame(mockBlockManager, context.getBlockManager(), 
                  "setBlockManager로 설정한 객체가 getBlockManager로 반환되어야 합니다.");
        assertSame(mockScoreManager, context.getScoreManager(), 
                  "setScoreManager로 설정한 객체가 getScoreManager로 반환되어야 합니다.");
        
        System.out.println("✅ ItemEffectContext setter 메서드 테스트 통과");
    }
}