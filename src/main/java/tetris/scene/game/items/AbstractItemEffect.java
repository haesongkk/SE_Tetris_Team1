package tetris.scene.game.items;

/**
 * 아이템 효과의 추상 기본 클래스
 * 공통 로직을 구현하고 하위 클래스에서 특화된 로직을 구현하도록 합니다.
 */
public abstract class AbstractItemEffect implements ItemEffect {
    protected boolean isActive = false;
    protected long startTime;
    protected final long duration;
    protected final ItemEffectType effectType;
    
    public AbstractItemEffect(ItemEffectType effectType, long duration) {
        this.effectType = effectType;
        this.duration = duration;
    }
    
    @Override
    public void activate(ItemEffectContext context) {
        if (isActive) {
            return; // 이미 활성화된 경우 무시
        }
        
        isActive = true;
        startTime = System.currentTimeMillis();
        
        System.out.println("Activating " + effectType.getDisplayName() + " effect");
        
        // 하위 클래스에서 구현할 실제 효과
        doActivate(context);
        
        // 지속 시간이 있는 경우 타이머 설정
        if (duration > 0) {
            scheduleDeactivation();
        } else {
            // 즉시 효과인 경우 바로 비활성화
            isActive = false;
        }
    }
    
    /**
     * 실제 아이템 효과를 수행하는 메서드 (하위 클래스에서 구현)
     * @param context 효과 실행 컨텍스트
     */
    protected abstract void doActivate(ItemEffectContext context);
    
    @Override
    public void deactivate() {
        if (!isActive) {
            return;
        }
        
        isActive = false;
        System.out.println("Deactivating " + effectType.getDisplayName() + " effect");
        
        // 하위 클래스에서 정리 작업이 필요한 경우 오버라이드
        doDeactivate();
    }
    
    /**
     * 효과 비활성화 시 추가 작업 (필요한 경우 하위 클래스에서 오버라이드)
     */
    protected void doDeactivate() {
        // 기본적으로는 아무것도 하지 않음
    }
    
    @Override
    public long getDuration() {
        return duration;
    }
    
    @Override
    public boolean isActive() {
        return isActive;
    }
    
    @Override
    public ItemEffectType getEffectType() {
        return effectType;
    }
    
    /**
     * 지속 시간 후 자동 비활성화를 위한 스케줄링
     */
    private void scheduleDeactivation() {
        // 간단한 타이머 구현 (실제로는 게임 엔진의 타이머 시스템 사용 권장)
        new Thread(() -> {
            try {
                Thread.sleep(duration);
                deactivate();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                deactivate();
            }
        }).start();
    }
    
    /**
     * 보드에서 유효한 좌표인지 확인
     */
    protected boolean isValidPosition(int[][] board, int x, int y) {
        return x >= 0 && x < board[0].length && y >= 0 && y < board.length;
    }
    
    /**
     * 보드의 특정 위치에 블록이 있는지 확인
     */
    protected boolean hasBlock(int[][] board, int x, int y) {
        return isValidPosition(board, x, y) && board[y][x] != 0;
    }
}