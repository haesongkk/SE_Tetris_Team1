package tetris.scene.game.items;

import tetris.scene.game.items.effects.*;
import java.util.Random;

/**
 * 아이템 효과 생성을 담당하는 팩토리 클래스
 * 객체지향 디자인 패턴 중 Factory Pattern을 적용
 */
public class ItemEffectFactory {
    private static final Random random = new Random();
    
    /**
     * 랜덤한 아이템 효과를 생성합니다.
     * @return 생성된 아이템 효과
     */
    public static ItemEffect createRandomEffect() {
        ItemEffectType[] types = ItemEffectType.values();
        ItemEffectType randomType = types[random.nextInt(types.length)];
        return createEffect(randomType);
    }
    
    /**
     * 지정된 타입의 아이템 효과를 생성합니다.
     * @param type 아이템 효과 타입
     * @return 생성된 아이템 효과
     */
    public static ItemEffect createEffect(ItemEffectType type) {
        switch (type) {
            case LINE_CLEAR:
                return new LineClearEffect();
            case CLEANUP:
                return new CleanupEffect();
            case SPEED_DOWN:
                return new SpeedDownEffect();
            case SPEED_UP:
                return new SpeedUpEffect();
            case VISION_BLOCK:
                return new VisionBlockEffect();
            default:
                throw new IllegalArgumentException("Unknown item effect type: " + type);
        }
    }
    
    /**
     * 특정 확률에 따라 아이템 효과를 생성합니다.
     * @return 생성된 아이템 효과 (확률에 따라 다른 타입)
     */
    public static ItemEffect createWeightedRandomEffect() {
        int rand = random.nextInt(100);
        
        // 확률 분배 (총 100%)
        if (rand < 20) {
            return new LineClearEffect(); // 20% - 가장 기본적인 효과
        } else if (rand < 40) {
            return new CleanupEffect(); // 20% - 유용한 정리 효과
        } else if (rand < 60) {
            return new SpeedDownEffect(); // 20% - 도움이 되는 효과
        } else if (rand < 80) {
            return new SpeedUpEffect(); // 20% - 도전적인 효과
        } else {
            return new VisionBlockEffect(); // 20% - 가장 어려운 효과
        }
    }
}