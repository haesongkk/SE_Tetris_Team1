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
 * - 난이도별 점수 가중치 (Easy: 20% 감소, Hard: 20% 증가)
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

    // ==================== 점수 가중치 테스트 ====================

    /**
     * Easy 난이도에서 점수가 20% 감소하는지 테스트
     */
    @Test
    @DisplayName("Easy 난이도 점수 20% 감소 테스트")
    void testEasyDifficultyScoreReduction() {
        ScoreManager scoreManager = new ScoreManager(GameSettings.Difficulty.EASY);
        
        // 1줄 삭제 시 점수 (기본 1000점의 80% = 800점)
        scoreManager.addScore(1);
        assertEquals(800, scoreManager.getScore(), "Easy 난이도에서 1줄 삭제 시 800점이어야 함");
        
        // 블록 드롭 점수 (기본 100점의 80% = 80점)
        scoreManager.reset();
        scoreManager.addBlockDropScore();
        assertEquals(80, scoreManager.getScore(), "Easy 난이도에서 블록 드롭 시 80점이어야 함");
    }

    /**
     * Normal 난이도에서 기본 점수가 유지되는지 테스트
     */
    @Test
    @DisplayName("Normal 난이도 기본 점수 테스트")
    void testNormalDifficultyScore() {
        ScoreManager scoreManager = new ScoreManager(GameSettings.Difficulty.NORMAL);
        
        // 1줄 삭제 시 점수 (기본 1000점)
        scoreManager.addScore(1);
        assertEquals(1000, scoreManager.getScore(), "Normal 난이도에서 1줄 삭제 시 1000점이어야 함");
        
        // 블록 드롭 점수 (기본 100점)
        scoreManager.reset();
        scoreManager.addBlockDropScore();
        assertEquals(100, scoreManager.getScore(), "Normal 난이도에서 블록 드롭 시 100점이어야 함");
    }

    /**
     * Hard 난이도에서 점수가 20% 증가하는지 테스트
     */
    @Test
    @DisplayName("Hard 난이도 점수 20% 증가 테스트")
    void testHardDifficultyScoreIncrease() {
        ScoreManager scoreManager = new ScoreManager(GameSettings.Difficulty.HARD);
        
        // 1줄 삭제 시 점수 (기본 1000점의 120% = 1200점)
        scoreManager.addScore(1);
        assertEquals(1200, scoreManager.getScore(), "Hard 난이도에서 1줄 삭제 시 1200점이어야 함");
        
        // 블록 드롭 점수 (기본 100점의 120% = 120점)
        scoreManager.reset();
        scoreManager.addBlockDropScore();
        assertEquals(120, scoreManager.getScore(), "Hard 난이도에서 블록 드롭 시 120점이어야 함");
    }

    /**
     * 여러 줄 삭제 시 난이도별 점수 차이 테스트
     */
    @Test
    @DisplayName("여러 줄 삭제 시 난이도별 점수 테스트")
    void testMultiLineDifficultyScore() {
        ScoreManager easyManager = new ScoreManager(GameSettings.Difficulty.EASY);
        ScoreManager normalManager = new ScoreManager(GameSettings.Difficulty.NORMAL);
        ScoreManager hardManager = new ScoreManager(GameSettings.Difficulty.HARD);
        
        // 3줄 삭제 시
        easyManager.addScore(3);
        normalManager.addScore(3);
        hardManager.addScore(3);
        
        assertEquals(2400, easyManager.getScore(), "Easy: 3줄 삭제 시 2400점 (3000 * 0.8)");
        assertEquals(3000, normalManager.getScore(), "Normal: 3줄 삭제 시 3000점");
        assertEquals(3600, hardManager.getScore(), "Hard: 3줄 삭제 시 3600점 (3000 * 1.2)");
    }

    /**
     * 속도 배율과 난이도 배율이 동시에 적용되는지 테스트
     */
    @Test
    @DisplayName("속도 배율과 난이도 배율 동시 적용 테스트")
    void testSpeedAndDifficultyMultiplierCombination() {
        ScoreManager hardManager = new ScoreManager(GameSettings.Difficulty.HARD);
        
        // 속도 증가로 배율 1.2배 적용
        hardManager.onSpeedIncrease();
        
        // 1줄 삭제 시: 기본 1000 * 속도배율 1.2 * 난이도배율 1.2 = 1440점
        hardManager.addScore(1);
        assertEquals(1440, hardManager.getScore(), 
                    "Hard 난이도에서 속도 증가 후 1줄 삭제 시 1440점이어야 함 (1000 * 1.2 * 1.2)");
    }
}