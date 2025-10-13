package tetris;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import tetris.scene.game.core.BlockManager;
import tetris.scene.game.core.BoardManager;
import tetris.scene.game.core.ScoreManager;
import tetris.scene.game.blocks.Block;
import tetris.scene.game.blocks.IBlock;
import tetris.util.SpeedUp;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 난이도 기능 테스트 클래스
 * - 난이도 설정
 * - 난이도별 I형 블럭 형성 확률
 * - 난이도별 속도 증가
 */
@DisplayName("난이도 기능 테스트")
public class DifficultyTest {

    private GameSettings gameSettings;
    private BoardManager boardManager;
    private ScoreManager scoreManager;

    @BeforeEach
    void setUp() {
        gameSettings = GameSettings.getInstance();
        boardManager = new BoardManager();
        scoreManager = new ScoreManager();
    }

    /**
     * 난이도 설정이 제대로 저장되는지 테스트
     */
    @Test
    void testDifficultySettings() {
        // Easy 설정
        gameSettings.setDifficulty(GameSettings.Difficulty.EASY);
        assertEquals(GameSettings.Difficulty.EASY, gameSettings.getDifficulty());

        // Normal 설정
        gameSettings.setDifficulty(GameSettings.Difficulty.NORMAL);
        assertEquals(GameSettings.Difficulty.NORMAL, gameSettings.getDifficulty());

        // Hard 설정
        gameSettings.setDifficulty(GameSettings.Difficulty.HARD);
        assertEquals(GameSettings.Difficulty.HARD, gameSettings.getDifficulty());
    }

    /**
     * Easy 난이도에서 I형 블럭이 20% 더 자주 생성되는지 테스트
     */
    @Test
    void testIBlockProbabilityEasy() throws Exception {
        BlockManager blockManager = new BlockManager(10, 20, boardManager, scoreManager, GameSettings.Difficulty.EASY);

        int totalBlocks = 10000;
        int iBlockCount = 0;

        for (int i = 0; i < totalBlocks; i++) {
            Block block = blockManager.getRandomBlockForTest();
            if (block instanceof IBlock) {
                iBlockCount++;
            }
        }

        double probability = (double) iBlockCount / totalBlocks;
        double expectedProbability = 1.0 / 7 * 1.2; // 기본 1/7의 120%

        // 약간의 오차 범위 허용 (통계적 변동성 고려)
        assertTrue(probability > expectedProbability * 0.9, "Easy 난이도에서 I형 블럭 확률이 예상보다 낮음: " + probability);
        assertTrue(probability < expectedProbability * 1.1, "Easy 난이도에서 I형 블럭 확률이 예상보다 높음: " + probability);
    }

    /**
     * Hard 난이도에서 I형 블럭이 20% 덜 생성되는지 테스트
     */
    @Test
    void testIBlockProbabilityHard() {
        BlockManager blockManager = new BlockManager(10, 20, boardManager, scoreManager, GameSettings.Difficulty.HARD);

        int totalBlocks = 10000;
        int iBlockCount = 0;

        for (int i = 0; i < totalBlocks; i++) {
            Block block = blockManager.getRandomBlockForTest();
            if (block instanceof IBlock) {
                iBlockCount++;
            }
        }

        double probability = (double) iBlockCount / totalBlocks;
        double expectedProbability = 1.0 / 7 * 0.8; // 기본 1/7의 80%

        // 약간의 오차 범위 허용 (통계적 변동성 고려)
        assertTrue(probability > expectedProbability * 0.9, "Hard 난이도에서 I형 블럭 확률이 예상보다 낮음: " + probability);
        assertTrue(probability < expectedProbability * 1.1, "Hard 난이도에서 I형 블럭 확률이 예상보다 높음: " + probability);
    }

    /**
     * Normal 난이도에서 I형 블럭이 기본 확률로 생성되는지 테스트
     */
    @Test
    void testIBlockProbabilityNormal() {
        BlockManager blockManager = new BlockManager(10, 20, boardManager, scoreManager, GameSettings.Difficulty.NORMAL);

        int totalBlocks = 10000;
        int iBlockCount = 0;

        for (int i = 0; i < totalBlocks; i++) {
            Block block = blockManager.getRandomBlockForTest();
            if (block instanceof IBlock) {
                iBlockCount++;
            }
        }

        double probability = (double) iBlockCount / totalBlocks;
        double expectedProbability = 1.0 / 7; // 기본 1/7

        // 약간의 오차 범위 허용 (통계적 변동성 고려)
        assertTrue(probability > expectedProbability * 0.9, "Normal 난이도에서 I형 블럭 확률이 예상보다 낮음: " + probability);
        assertTrue(probability < expectedProbability * 1.1, "Normal 난이도에서 I형 블럭 확률이 예상보다 높음: " + probability);
    }

    /**
     * Easy 난이도에서 속도 증가량이 20% 감소하는지 테스트
     */
    @Test
    void testSpeedIncreaseEasy() {
        SpeedUp speedUp = new SpeedUp(null, null, GameSettings.Difficulty.EASY);

        // SpeedUp의 intervalDecrease 필드에 접근하기 위해 리플렉션 사용
        try {
            java.lang.reflect.Field field = SpeedUp.class.getDeclaredField("intervalDecrease");
            field.setAccessible(true);
            int intervalDecrease = (int) field.get(speedUp);

            int expectedDecrease = (int) (200 * 0.8); // BASE_INTERVAL_DECREASE * 0.8
            assertEquals(expectedDecrease, intervalDecrease, "Easy 난이도에서 속도 증가량이 잘못 설정됨");
        } catch (Exception e) {
            fail("리플렉션으로 intervalDecrease 접근 실패: " + e.getMessage());
        }
    }

    /**
     * Hard 난이도에서 속도 증가량이 20% 증가하는지 테스트
     */
    @Test
    void testSpeedIncreaseHard() {
        SpeedUp speedUp = new SpeedUp(null, null, GameSettings.Difficulty.HARD);

        try {
            java.lang.reflect.Field field = SpeedUp.class.getDeclaredField("intervalDecrease");
            field.setAccessible(true);
            int intervalDecrease = (int) field.get(speedUp);

            int expectedDecrease = (int) (200 * 1.2); // BASE_INTERVAL_DECREASE * 1.2
            assertEquals(expectedDecrease, intervalDecrease, "Hard 난이도에서 속도 증가량이 잘못 설정됨");
        } catch (Exception e) {
            fail("리플렉션으로 intervalDecrease 접근 실패: " + e.getMessage());
        }
    }

    /**
     * Normal 난이도에서 속도 증가량이 기본값인지 테스트
     */
    @Test
    void testSpeedIncreaseNormal() {
        SpeedUp speedUp = new SpeedUp(null, null, GameSettings.Difficulty.NORMAL);

        try {
            java.lang.reflect.Field field = SpeedUp.class.getDeclaredField("intervalDecrease");
            field.setAccessible(true);
            int intervalDecrease = (int) field.get(speedUp);

            int expectedDecrease = 200; // BASE_INTERVAL_DECREASE
            assertEquals(expectedDecrease, intervalDecrease, "Normal 난이도에서 속도 증가량이 잘못 설정됨");
        } catch (Exception e) {
            fail("리플렉션으로 intervalDecrease 접근 실패: " + e.getMessage());
        }
    }
}