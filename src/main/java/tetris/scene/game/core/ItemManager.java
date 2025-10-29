package tetris.scene.game.core;

import tetris.scene.game.blocks.*;
import tetris.scene.game.items.*;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

/**
 * 아이템 모드에서 아이템 관련 로직을 관리하는 클래스
 * 객체지향적 설계를 통해 아이템 효과를 관리합니다.
 */
public class ItemManager {
    private static final int LINES_FOR_ITEM = 10; // 아이템 등장을 위한 줄 삭제 수 (누적) - 빠른 테스트를 위해 10 → 1줄로 변경
    
    private int totalLinesCleared = 0; // 총 삭제된 줄 수 (누적)
    private final Random random;
    private boolean debugMode = false; // 디버그 모드: false면 10줄마다 아이템 블록 생성
    private final List<ItemEffect> activeEffects; // 현재 활성화된 아이템 효과들
    
    public ItemManager() {
        this.random = new Random();
        this.activeEffects = new ArrayList<>();
    }
    
    /**
     * 줄이 삭제되었을 때 호출되는 메서드
     * @param linesCleared 이번에 삭제된 줄 수
     */
    public void onLinesCleared(int linesCleared) {
        totalLinesCleared += linesCleared;
        System.out.println("Lines cleared this turn: " + linesCleared + ", Total lines cleared: " + totalLinesCleared);
        
        // 비활성화된 효과들 정리
        cleanupInactiveEffects();
    }
    
    /**
     * 아이템 블록을 생성해야 하는지 확인 (누적 10줄 이상일 때)
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
     * 비활성화된 아이템 효과들을 정리합니다.
     */
    private void cleanupInactiveEffects() {
        activeEffects.removeIf(effect -> !effect.isActive());
    }
    
    /**
     * 기본 블록을 아이템 블록으로 변환
     * @param originalBlock 원본 블록
     * @return 아이템이 포함된 블록
     */
    public Block createItemBlock(Block originalBlock) {
        if (originalBlock == null) return null;
        
        Block itemBlock;
        
        if (debugMode) {
            // 디버그 모드일 때는 줄 삭제 아이템만 생성 (기존 폭탄 아이템 대체)
            itemBlock = new ItemBlock(originalBlock, ItemEffectType.LINE_CLEAR);
        } else {
            // 새로운 5가지 아이템 중 랜덤 선택
            ItemEffectType[] itemTypes = ItemEffectType.values();
            ItemEffectType randomType = itemTypes[random.nextInt(itemTypes.length)];
            
            // 20% 확률로 기존 무게추 아이템 유지, 80% 확률로 새 아이템들
            if (random.nextInt(100) < 20) {
                // 무게추 아이템 블록 생성 (기존 유지)
                itemBlock = new WeightItemBlock();
                System.out.println("Created WeightItemBlock (independent item)");
            } else {
                // 새로운 아이템 블록 생성
                itemBlock = new ItemBlock(originalBlock, randomType);
                System.out.println("Created ItemBlock with " + randomType.getDisplayName() + 
                                 " from " + originalBlock.getClass().getSimpleName());
            }
        }
        
        // 아이템 블록 생성 후 카운트 초기화
        int previousTotal = totalLinesCleared;
        totalLinesCleared = 0;
        System.out.println("Item block created! Lines counter reset to 0 (Total lines were: " + previousTotal + ")");
        
        return itemBlock;
    }
    
    /**
     * 아이템 효과를 활성화합니다.
     * @param effect 활성화할 아이템 효과
     * @param context 효과 실행 컨텍스트
     */
    public void activateItemEffect(ItemEffect effect, ItemEffectContext context) {
        if (effect == null) return;
        
        effect.activate(context);
        
        // 지속 시간이 있는 효과만 목록에 추가
        if (effect.getDuration() > 0) {
            activeEffects.add(effect);
        }
    }
    
    /**
     * 현재 활성화된 아이템 효과 목록을 반환합니다.
     * @return 활성화된 효과 목록
     */
    public List<ItemEffect> getActiveEffects() {
        cleanupInactiveEffects();
        return new ArrayList<>(activeEffects);
    }
    
    /**
     * 아이템 카운터 리셋
     */
    public void reset() {
        totalLinesCleared = 0;
        // 모든 활성 효과 비활성화
        for (ItemEffect effect : activeEffects) {
            effect.deactivate();
        }
        activeEffects.clear();
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
        return Math.max(0, LINES_FOR_ITEM - totalLinesCleared);
    }
    
    /**
     * 디버그 모드 설정
     * @param enabled true면 무조건 줄 삭제 아이템만 생성, false면 일반 모드
     */
    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
        System.out.println("Debug mode " + (enabled ? "enabled" : "disabled") + 
                         " - " + (enabled ? "Force line clear items only!" : "Random item generation"));
    }
    
    /**
     * 현재 디버그 모드 상태 확인
     * @return true if 디버그 모드 활성화
     */
    public boolean isDebugMode() {
        return debugMode;
    }
}