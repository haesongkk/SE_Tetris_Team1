package tetris.scene.game.core;

import tetris.scene.game.blocks.*;
import java.util.Random;

/**
 * 아이템 모드에서 아이템 관련 로직을 관리하는 클래스
 */
public class ItemManager {
    private static final int LINES_FOR_ITEM = 2; // 아이템 등장을 위한 줄 삭제 수 (누적)
    
    private int totalLinesCleared = 0; // 총 삭제된 줄 수 (누적)
    private Random random;
    private boolean debugMode = false; // 디버그 모드: false면 2줄마다 폭탄 블록 생성
    
    public ItemManager() {
        this.random = new Random();
    }
    
    /**
     * 줄이 삭제되었을 때 호출되는 메서드
     * @param linesCleared 이번에 삭제된 줄 수
     */
    public void onLinesCleared(int linesCleared) {
        totalLinesCleared += linesCleared;
        System.out.println("Lines cleared this turn: " + linesCleared + ", Total lines cleared: " + totalLinesCleared);
    }
    
    /**
     * 아이템 블록을 생성해야 하는지 확인 (누적 2줄 이상일 때)
     * @return true if 아이템 블록을 생성해야 함
     */
    public boolean shouldCreateItemBlock() {
        if (debugMode) {
            System.out.println("Debug mode: Force creating item block!");
            return true; // 디버그 모드일 때는 무조건 아이템 블록 생성
        }
        return totalLinesCleared >= LINES_FOR_ITEM;
    }
    
    /**
     * 기본 블록을 아이템 블록으로 변환
     * @param originalBlock 원본 블록
     * @return 아이템이 포함된 블록
     */
    public Block createItemBlock(Block originalBlock) {
        if (originalBlock == null) return null;
        
        // 폭탄 아이템 블록 생성
        BombItemBlock itemBlock = new BombItemBlock(originalBlock);
        
        // 아이템 블록 생성 후 카운트 초기화
        int previousTotal = totalLinesCleared;
        totalLinesCleared = 0;
        System.out.println("Item block created! Lines counter reset to 0 (Total lines were: " + previousTotal + ")");
        
        return itemBlock;
    }
    
    /**
     * 아이템 카운터 리셋
     */
    public void reset() {
        totalLinesCleared = 0;
    }
    
    /**
     * 현재 삭제된 총 줄 수 반환
     */
    public int getTotalLinesCleared() {
        return totalLinesCleared;
    }
    
    /**
     * 다음 아이템까지 남은 줄 수
     */
    public int getLinesUntilNextItem() {
        return LINES_FOR_ITEM - totalLinesCleared;
    }
    
    /**
     * 디버그 모드 설정
     * @param enabled true면 무조건 폭탄 블록만 생성, false면 일반 모드
     */
    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
        System.out.println("Debug mode " + (enabled ? "enabled" : "disabled") + 
                         " - " + (enabled ? "Force bomb blocks only!" : "Normal item generation"));
    }
    
    /**
     * 현재 디버그 모드 상태 확인
     * @return true if 디버그 모드 활성화
     */
    public boolean isDebugMode() {
        return debugMode;
    }
}