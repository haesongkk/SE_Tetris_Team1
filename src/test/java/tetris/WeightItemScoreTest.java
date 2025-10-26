package tetris;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import tetris.scene.game.core.ScoreManager;
import tetris.scene.game.blocks.WeightItemBlock;
import tetris.GameSettings;
import java.awt.Color;

/**
 * 무게추 아이템 점수 기능 테스트 클래스
 * 무게추가 셀을 지울 때마다 50점 추가 (난이도별 차등 적용)
 */
@DisplayName("무게추 아이템 점수 테스트")
public class WeightItemScoreTest {

    private WeightItemBlock weightBlock;
    private int[][] testBoard;
    private Color[][] testBoardColors;

    @BeforeEach
    void setUp() {
        weightBlock = new WeightItemBlock();
        
        // 10x20 테스트 보드 생성
        testBoard = new int[20][10];
        testBoardColors = new Color[20][10];
        
        // 테스트용 블록들을 보드 하단에 배치
        for (int row = 17; row < 20; row++) {
            for (int col = 2; col < 8; col++) {
                testBoard[row][col] = 1;
                testBoardColors[row][col] = Color.BLUE;
            }
        }
    }

    @Test
    @DisplayName("Easy 난이도에서 무게추 셀 제거 점수 테스트 (40점)")
    void testWeightItemScoreEasy() {
        ScoreManager scoreManager = new ScoreManager(GameSettings.Difficulty.EASY);
        
        // 무게추가 블록들을 제거 (무게추 위치: x=4, y=16)
        int clearedCount = weightBlock.clearBlocksBelow(testBoard, testBoardColors, 4, 16, scoreManager, null);
        
        // 제거된 셀 개수 확인 (무게추 범위 내의 셀들)
        assertTrue(clearedCount > 0, "무게추가 블록을 제거해야 함");
        
        // 점수 확인: 제거된 셀 수 × 40점 (50 × 0.8)
        int expectedScore = clearedCount * 40;
        assertEquals(expectedScore, scoreManager.getScore(), 
                    "Easy 난이도에서 무게추 셀 제거 시 셀당 40점이어야 함 (50 × 0.8)");
    }

    @Test
    @DisplayName("Normal 난이도에서 무게추 셀 제거 점수 테스트 (50점)")
    void testWeightItemScoreNormal() {
        ScoreManager scoreManager = new ScoreManager(GameSettings.Difficulty.NORMAL);
        
        int clearedCount = weightBlock.clearBlocksBelow(testBoard, testBoardColors, 4, 16, scoreManager, null);
        
        assertTrue(clearedCount > 0, "무게추가 블록을 제거해야 함");
        
        // 점수 확인: 제거된 셀 수 × 50점 (기본)
        int expectedScore = clearedCount * 50;
        assertEquals(expectedScore, scoreManager.getScore(), 
                    "Normal 난이도에서 무게추 셀 제거 시 셀당 50점이어야 함");
    }

    @Test
    @DisplayName("Hard 난이도에서 무게추 셀 제거 점수 테스트 (60점)")
    void testWeightItemScoreHard() {
        ScoreManager scoreManager = new ScoreManager(GameSettings.Difficulty.HARD);
        
        int clearedCount = weightBlock.clearBlocksBelow(testBoard, testBoardColors, 4, 16, scoreManager, null);
        
        assertTrue(clearedCount > 0, "무게추가 블록을 제거해야 함");
        
        // 점수 확인: 제거된 셀 수 × 60점 (50 × 1.2)
        int expectedScore = clearedCount * 60;
        assertEquals(expectedScore, scoreManager.getScore(), 
                    "Hard 난이도에서 무게추 셀 제거 시 셀당 60점이어야 함 (50 × 1.2)");
    }

    @Test
    @DisplayName("무게추 개별 셀 점수 추가 메서드 테스트")
    void testWeightItemCellScoreMethod() {
        ScoreManager easyManager = new ScoreManager(GameSettings.Difficulty.EASY);
        ScoreManager normalManager = new ScoreManager(GameSettings.Difficulty.NORMAL);
        ScoreManager hardManager = new ScoreManager(GameSettings.Difficulty.HARD);
        
        // 각 난이도별로 셀 점수 추가
        easyManager.addWeightItemCellScore();
        normalManager.addWeightItemCellScore();
        hardManager.addWeightItemCellScore();
        
        assertEquals(40, easyManager.getScore(), "Easy: 셀당 40점 (50 × 0.8)");
        assertEquals(50, normalManager.getScore(), "Normal: 셀당 50점");
        assertEquals(60, hardManager.getScore(), "Hard: 셀당 60점 (50 × 1.2)");
    }

    @Test
    @DisplayName("무게추와 다른 점수 시스템 조합 테스트")
    void testWeightItemWithOtherScores() {
        ScoreManager scoreManager = new ScoreManager(GameSettings.Difficulty.HARD);
        
        // 줄 삭제 점수 (1000 × 1.2 = 1200점)
        scoreManager.addScore(1);
        assertEquals(1200, scoreManager.getScore(), "Hard 난이도 줄 삭제: 1200점");
        
        // 무게추 셀 점수 (50 × 1.2 = 60점)
        scoreManager.addWeightItemCellScore();
        assertEquals(1260, scoreManager.getScore(), "줄 삭제 + 무게추 셀: 1260점");
        
        // 블록 드롭 점수 (100 × 1.2 = 120점)
        scoreManager.addBlockDropScore();
        assertEquals(1380, scoreManager.getScore(), "전체 합계: 1380점");
    }

    @Test
    @DisplayName("빈 보드에서 무게추 사용 시 점수 변화 없음 테스트")
    void testWeightItemOnEmptyBoard() {
        ScoreManager scoreManager = new ScoreManager(GameSettings.Difficulty.NORMAL);
        
        // 빈 보드 생성
        int[][] emptyBoard = new int[20][10];
        Color[][] emptyBoardColors = new Color[20][10];
        
        int clearedCount = weightBlock.clearBlocksBelow(emptyBoard, emptyBoardColors, 4, 16, scoreManager, null);
        
        assertEquals(0, clearedCount, "빈 보드에서는 제거된 셀이 없어야 함");
        assertEquals(0, scoreManager.getScore(), "빈 보드에서는 점수 증가가 없어야 함");
    }
}