package tetris;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

import tetris.scene.game.core.ScoreManager;
import tetris.GameSettings;

/**
 * 블록 낙하 점수 변경 테스트
 */
@DisplayName("블록 낙하 점수 변경 테스트")
public class BlockFallScoreChangeTest {
    
    /**
     * 각 난이도별 블록 낙하 점수 테스트 (매개변수화된 테스트)
     */
    @ParameterizedTest
    @EnumSource(GameSettings.Difficulty.class)
    @DisplayName("난이도별 블록 낙하 점수 테스트 (10점 고정)")
    void testBlockFallScoreByDifficulty(GameSettings.Difficulty difficulty) {
        System.out.println("\n--- " + difficulty.name() + " 난이도 테스트 ---");
        
        ScoreManager scoreManager = new ScoreManager(difficulty);
        
        // 초기 점수 확인
        assertEquals(0, scoreManager.getScore(), "초기 점수는 0이어야 합니다.");
        System.out.println("초기 점수: " + scoreManager.getScore());
        
        // 5번 낙하 테스트
        for (int i = 1; i <= 5; i++) {
            scoreManager.addBlockFallScore();
            int expectedScore = i * 10; // 블록 낙하는 난이도와 무관하게 10점 고정
            assertEquals(expectedScore, scoreManager.getScore(), 
                i + "번째 낙하 후 점수가 " + expectedScore + "점이어야 합니다.");
            System.out.println(i + "번째 낙하 후: " + scoreManager.getScore() + "점");
        }
        
        // 최종 점수 확인
        int finalExpectedScore = 5 * 10; // 5번 낙하 × 10점 = 50점
        assertEquals(finalExpectedScore, scoreManager.getScore(), 
            "최종 점수가 " + finalExpectedScore + "점이어야 합니다.");
        
        System.out.println("✅ " + difficulty.name() + " 테스트 통과 - 예상 점수: " + finalExpectedScore + "점");
    }
    
    /**
     * Easy 난이도 블록 낙하 점수 테스트
     */
    @Test
    @DisplayName("Easy 난이도 블록 낙하 점수 테스트")
    void testEasyDifficultyBlockFallScore() {
        testDifficulty(GameSettings.Difficulty.EASY, "Easy");
    }
    
    /**
     * Normal 난이도 블록 낙하 점수 테스트
     */
    @Test
    @DisplayName("Normal 난이도 블록 낙하 점수 테스트")
    void testNormalDifficultyBlockFallScore() {
        testDifficulty(GameSettings.Difficulty.NORMAL, "Normal");
    }
    
    /**
     * Hard 난이도 블록 낙하 점수 테스트
     */
    @Test
    @DisplayName("Hard 난이도 블록 낙하 점수 테스트")
    void testHardDifficultyBlockFallScore() {
        testDifficulty(GameSettings.Difficulty.HARD, "Hard");
    }
    
    /**
     * 개별 난이도 테스트 헬퍼 메서드
     */
    private void testDifficulty(GameSettings.Difficulty difficulty, String difficultyName) {
        System.out.println("\n--- " + difficultyName + " 난이도 테스트 ---");
        
        ScoreManager scoreManager = new ScoreManager(difficulty);
        
        // 초기 점수 확인
        assertEquals(0, scoreManager.getScore(), "초기 점수는 0이어야 합니다.");
        System.out.println("초기 점수: " + scoreManager.getScore());
        
        // 5번 낙하 테스트
        for (int i = 1; i <= 5; i++) {
            scoreManager.addBlockFallScore();
            System.out.println(i + "번째 낙하 후: " + scoreManager.getScore() + "점");
        }
        
        // 예상 점수와 비교 (블록 낙하는 난이도 무관하게 10점)
        int expectedScore = 5 * 10; // 5번 낙하 × 10점 = 50점
        assertEquals(expectedScore, scoreManager.getScore(), 
            difficultyName + " 난이도에서 예상 점수와 일치해야 합니다.");
        
        System.out.println("✅ " + difficultyName + " 테스트 통과 - 예상 점수: " + expectedScore + "점");
    }
    
    /**
     * 블록 낙하 점수가 난이도에 영향받지 않는지 통합 테스트
     */
    @Test
    @DisplayName("블록 낙하 점수 난이도 무관 통합 테스트")
    void testBlockFallScoreIsIndependentOfDifficulty() {
        ScoreManager easyScoreManager = new ScoreManager(GameSettings.Difficulty.EASY);
        ScoreManager normalScoreManager = new ScoreManager(GameSettings.Difficulty.NORMAL);
        ScoreManager hardScoreManager = new ScoreManager(GameSettings.Difficulty.HARD);
        
        // 각 난이도에서 동일한 횟수로 블록 낙하
        int fallCount = 3;
        for (int i = 0; i < fallCount; i++) {
            easyScoreManager.addBlockFallScore();
            normalScoreManager.addBlockFallScore();
            hardScoreManager.addBlockFallScore();
        }
        
        // 모든 난이도에서 동일한 점수여야 함
        int expectedScore = fallCount * 10;
        assertEquals(expectedScore, easyScoreManager.getScore(), "Easy 난이도 점수가 예상과 다릅니다.");
        assertEquals(expectedScore, normalScoreManager.getScore(), "Normal 난이도 점수가 예상과 다릅니다.");
        assertEquals(expectedScore, hardScoreManager.getScore(), "Hard 난이도 점수가 예상과 다릅니다.");
        
        // 모든 난이도에서 점수가 동일한지 확인
        assertEquals(easyScoreManager.getScore(), normalScoreManager.getScore(), 
            "Easy와 Normal 난이도의 블록 낙하 점수가 동일해야 합니다.");
        assertEquals(normalScoreManager.getScore(), hardScoreManager.getScore(), 
            "Normal과 Hard 난이도의 블록 낙하 점수가 동일해야 합니다.");
        
        System.out.println("✅ 블록 낙하 점수는 난이도에 무관하게 " + expectedScore + "점으로 동일합니다.");
    }
}