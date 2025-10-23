package tetris;

import tetris.scene.game.core.ScoreManager;
import tetris.GameSettings;

/**
 * 블록 낙하 점수 변경 테스트
 */
public class BlockFallScoreChangeTest {
    
    public static void main(String[] args) {
        System.out.println("=== 블록 낙하 점수 변경 테스트 ===");
        
        // 각 난이도별로 테스트
        testDifficulty(GameSettings.Difficulty.EASY, "Easy");
        testDifficulty(GameSettings.Difficulty.NORMAL, "Normal");
        testDifficulty(GameSettings.Difficulty.HARD, "Hard");
        
        System.out.println("=== 테스트 완료 ===");
    }
    
    private static void testDifficulty(GameSettings.Difficulty difficulty, String difficultyName) {
        System.out.println("\n--- " + difficultyName + " 난이도 테스트 ---");
        
        ScoreManager scoreManager = new ScoreManager(difficulty);
        
        // 초기 점수 확인
        System.out.println("초기 점수: " + scoreManager.getScore());
        
        // 5번 낙하 테스트
        for (int i = 1; i <= 5; i++) {
            scoreManager.addBlockFallScore();
            System.out.println(i + "번째 낙하 후: " + scoreManager.getScore() + "점");
        }
        
        // 예상 점수와 비교
        int expectedScore = 5 * 10; // 5번 낙하 × 10점 = 50점
        if (scoreManager.getScore() == expectedScore) {
            System.out.println("✅ " + difficultyName + " 테스트 통과 - 예상 점수: " + expectedScore + "점");
        } else {
            System.out.println("❌ " + difficultyName + " 테스트 실패 - 예상: " + expectedScore + "점, 실제: " + scoreManager.getScore() + "점");
        }
    }
}