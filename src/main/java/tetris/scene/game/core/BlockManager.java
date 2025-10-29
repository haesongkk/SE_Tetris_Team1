package tetris.scene.game.core;

import tetris.scene.game.blocks.*;
import tetris.scene.game.items.*;
import tetris.scene.game.items.ItemEffectType;
import tetris.util.SpeedUp;
import tetris.GameSettings;
import java.util.Random;

/**
 * 블록 관리를 담당하는 클래스
 * 현재/다음 블록의 생성, 이동, 회전 등의 블록 관련 로직을 처리합니다.
 */
public class BlockManager {
    
    // 블록 상태
    private Block currentBlock; // 현재 떨어지고 있는 블록
    private Block nextBlock; // 다음 블록
    private int x = 3; // 현재 블록의 x 위치
    private int y = 0; // 현재 블록의 y 위치
    private int lastBlockY = 0; // 마지막 블록 Y 위치
    
    // 게임 설정
    private final int gameWidth;
    private final int gameHeight;
    private final GameSettings.Difficulty difficulty;
    
    // 랜덤 생성기
    private final Random random;
    
    // 의존성
    private final BoardManager boardManager;
    private final BlockShake blockShake;
    private ItemManager itemManager; // 아이템 모드용 (옵션)
    private ScoreManager scoreManager; // 점수 관리자
    private Object gameScene; // GameScene 참조 (아이템 효과용)
    
    // 속도 증가 관리자
    private SpeedUp speedUp;
    
    // 게임 종료 상태
    private boolean isGameOver = false;
    
    /**
     * BlockManager 생성자
     * 
     * @param gameWidth 게임 보드 너비
     * @param gameHeight 게임 보드 높이
     * @param boardManager 보드 관리자
     * @param scoreManager 점수 관리자
     * @param difficulty 난이도
     */
    public BlockManager(int gameWidth, int gameHeight, BoardManager boardManager, ScoreManager scoreManager, GameSettings.Difficulty difficulty) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.boardManager = boardManager;
        this.scoreManager = scoreManager;
        this.difficulty = difficulty;
        this.random = new Random(System.currentTimeMillis());
        this.blockShake = new BlockShake(new BlockShake.ShakeCallback() {
            @Override
            public void onShakeUpdate() {
                // 흔들림 업데이트 시 호출 (필요시 GameScene에서 화면 갱신)
            }
            
            @Override
            public void onShakeComplete() {
                // 흔들림 완료 시 호출
            }
        });
    }
    
    /**
     * 게임 시작 시 초기 블록을 생성합니다.
     */
    public void initializeBlocks() {
        currentBlock = getRandomBlock();
        nextBlock = getRandomBlock();
        x = 3;
        y = 0;
        lastBlockY = 0;
        isGameOver = false;
    }
    
    /**
     * 랜덤 블록을 생성합니다.
     * Fitness Proportionate Selection (Roulette Wheel Selection) 방식을 사용합니다.
     * 
     * @return 생성된 블록
     */
    private Block getRandomBlock() {
        // 난이도별 블록 적합도(가중치) 설정
        double[] blockWeights = getBlockWeights();
        
        // 전체 가중치 합계 계산
        double totalWeight = 0.0;
        for (double weight : blockWeights) {
            totalWeight += weight;
        }
        
        // Roulette Wheel Selection 실행
        double randomValue = random.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;
        
        Block newBlock = null;
        for (int i = 0; i < blockWeights.length; i++) {
            cumulativeWeight += blockWeights[i];
            if (randomValue <= cumulativeWeight) {
                newBlock = createBlockByIndex(i);
                break;
            }
        }
        
        // 예외 상황 처리 (마지막 블록으로 기본값 설정)
        if (newBlock == null) {
            newBlock = createBlockByIndex(6); // TBlock as default
        }
        
        // 블록 생성 수 증가 (SpeedUp 관리자 사용)
        if (speedUp != null) {
            speedUp.onBlockGenerated(isGameOver);
        }
        
        // 디버그 모드일 때는 무조건 폭탄 블록으로 변환
        if (itemManager != null && itemManager.shouldCreateItemBlock()) {
            System.out.println("Debug mode: Converting block to bomb block!");
            return itemManager.createItemBlock(newBlock);
        }
        
        return newBlock;
    }
    
    /**
     * 난이도별 블록 가중치를 반환합니다.
     * 인덱스: 0=I, 1=J, 2=L, 3=Z, 4=S, 5=T, 6=O
     * 
     * @return 블록별 가중치 배열
     */
    private double[] getBlockWeights() {
        double[] weights = new double[7];
        
        switch (difficulty) {
            case EASY:
                // Easy 모드: I블록 확률 20% 증가, 어려운 블록(S, Z) 확률 감소
                weights[0] = 1.2; // I블록 증가
                weights[1] = 1.0; // J블록
                weights[2] = 1.0; // L블록
                weights[3] = 0.9; // Z블록 감소
                weights[4] = 0.9; // S블록 감소
                weights[5] = 1.0; // T블록
                weights[6] = 1.0; // O블록
                break;
                
            case HARD:
                // Hard 모드: I블록 확률 20% 감소, 어려운 블록(S, Z) 확률 증가
                weights[0] = 0.8; // I블록 감소
                weights[1] = 1.0; // J블록
                weights[2] = 1.0; // L블록
                weights[3] = 1.1; // Z블록 증가
                weights[4] = 1.1; // S블록 증가
                weights[5] = 1.0; // T블록
                weights[6] = 1.0; // O블록
                break;
                
            default: // NORMAL
                // Normal 모드: 모든 블록 균등 확률
                for (int i = 0; i < weights.length; i++) {
                    weights[i] = 1.0;
                }
                break;
        }
        
        return weights;
    }
    
    /**
     * 인덱스에 따라 블록을 생성합니다.
     * 인덱스: 0=I, 1=J, 2=L, 3=Z, 4=S, 5=T, 6=O
     * 
     * @param index 블록 인덱스
     * @return 생성된 블록
     */
    private Block createBlockByIndex(int index) {
        switch (index) {
            case 0: return new IBlock();
            case 1: return new JBlock();
            case 2: return new LBlock();
            case 3: return new ZBlock();
            case 4: return new SBlock();
            case 5: return new TBlock();
            case 6: return new OBlock();
            default: return new TBlock(); // 기본값
        }
    }
    
    /**
     * 블록을 아래로 이동시킵니다.
     * 
     * @return 블록이 고정되었으면 true, 그냥 이동했으면 false
     */
    public boolean moveDown() {
        if (currentBlock == null || isGameOver) return false;
        
        if (canMoveDown()) {
            y++;
            // 블록이 1칸 떨어질 때마다 점수 획득 (자동/수동 무관)
            if (scoreManager != null) {
                scoreManager.addBlockFallScore();
            }
            return false; // 단순 이동
        } else {
            // 무게추 블록이 바닥이나 다른 블록에 닿았을 때 처리
            if (currentBlock instanceof WeightItemBlock) {
                WeightItemBlock weightBlock = (WeightItemBlock) currentBlock;
                if (!weightBlock.isActivated()) {
                    // 첫 번째로 블록에 닿은 경우 활성화
                    weightBlock.activate();
                    System.out.println("WeightItemBlock activated at position (" + x + ", " + y + ")");
                    return false; // 아직 고정하지 않음, 자동 낙하 시작
                } else {
                    // 이미 활성화된 상태에서는 자동 낙하만 처리하므로 moveDown에서는 아무것도 하지 않음
                    System.out.println("WeightItemBlock is already activated, automatic fall will be handled by updateWeightBlock()");
                    return false; // 자동 낙하는 updateWeightBlock()에서 처리
                }
            } else {
                // 일반 블록의 경우 기존 로직 유지
                placeBlockPermanently();
                return true; // 블록 고정됨
            }
        }
    }
    
    /**
     * 블록을 왼쪽으로 이동시킵니다.
     */
    public void moveLeft() {
        if (currentBlock == null || isGameOver) return;
        
        // 무게추 블록의 좌우 이동 제한 확인
        if (currentBlock instanceof WeightItemBlock) {
            WeightItemBlock weightBlock = (WeightItemBlock) currentBlock;
            if (!weightBlock.canMoveHorizontally()) {
                System.out.println("WeightItemBlock cannot move horizontally (activated)");
                return;
            }
        }
        
        if (canMoveLeft()) {
            x--;
        }
    }
    
    /**
     * 블록을 오른쪽으로 이동시킵니다.
     */
    public void moveRight() {
        if (currentBlock == null || isGameOver) return;
        
        // 무게추 블록의 좌우 이동 제한 확인
        if (currentBlock instanceof WeightItemBlock) {
            WeightItemBlock weightBlock = (WeightItemBlock) currentBlock;
            if (!weightBlock.canMoveHorizontally()) {
                System.out.println("WeightItemBlock cannot move horizontally (activated)");
                return;
            }
        }
        
        if (canMoveRight()) {
            x++;
        }
    }
    
    /**
     * 블록을 회전시킵니다.
     */
    public void rotateBlock() {
        if (currentBlock == null || isGameOver) return;
        
        if (canRotate()) {
            currentBlock.rotate();
        } else {
            // 회전할 수 없을 때 blockshake 시작
            blockShake.startShake();
            System.out.println("Cannot rotate block! Starting shake effect");
        }
    }
    
    /**
     * 하드 드롭을 실행합니다.
     * 
     * @return 블록이 고정되었으면 true
     */
    public boolean executeHardDrop() {
        if (currentBlock == null || isGameOver) return false;
        
        // 무게추 블록의 경우 특별 처리
        if (currentBlock instanceof WeightItemBlock) {
            WeightItemBlock weightBlock = (WeightItemBlock) currentBlock;
            
            // BlockHardDrop 클래스를 사용하여 하드 드롭 실행
            int newY = BlockHardDrop.executeHardDrop(currentBlock, x, y, 
                                                    boardManager.getBoard(), gameWidth, gameHeight);
            y = newY;
            
            // 무게추는 하드드롭 후 활성화되고 자동 낙하 시작
            if (!weightBlock.isActivated()) {
                weightBlock.activate();
                System.out.println("WeightItemBlock activated after hard drop at position (" + x + ", " + y + ")");
            }
            
            return false; // 무게추는 고정되지 않고 자동 낙하 시작
        } else {
            // 일반 블록의 경우 기존 로직 유지
            // BlockHardDrop 클래스를 사용하여 하드 드롭 실행
            int newY = BlockHardDrop.executeHardDrop(currentBlock, x, y, 
                                                    boardManager.getBoard(), gameWidth, gameHeight);
            y = newY;
            
            // 블록이 바닥에 닿았으므로 즉시 고정
            placeBlockPermanently();
            
            // 다음 블록 생성은 줄 삭제 검사 이후에 GameScene에서 호출
            
            return true;
        }
    }
    
    /**
     * 현재 블록을 보드에 영구적으로 고정합니다.
     */
    private void placeBlockPermanently() {
        System.out.println("Placing block permanently at x=" + x + ", y=" + y);
        
        // 아이템 블록인 경우 시각적 효과는 배치 전에 활성화 (속도, 시야 차단 등)
        if (currentBlock instanceof ItemBlock) {
            ItemBlock itemBlock = (ItemBlock) currentBlock;
            activateVisualItemEffects(itemBlock);
        }
        
        // BoardManager를 사용하여 블록을 영구적으로 보드에 고정
        boardManager.placeBlock(currentBlock, x, y);
        
        // 아이템 블록인 경우 보드 조작 효과는 배치 후에 활성화 (줄 삭제, 청소 등)
        if (currentBlock instanceof ItemBlock) {
            ItemBlock itemBlock = (ItemBlock) currentBlock;
            activateBoardManipulationEffects(itemBlock);
        }
        
        // 블록이 떨어질 때 점수 추가
        if (scoreManager != null) {
            scoreManager.addBlockDropScore();
        }
        
        // 게임 종료 조건 확인: BoardManager의 게임 오버 체크 사용
        if (boardManager.isGameOver()) {
            isGameOver = true;
            return;
        }
        
        // 마지막 블록 Y 위치 업데이트
        lastBlockY = y;
    }
    
    /**
     * 다음 블록을 현재 블록으로 만들고 새로운 다음 블록을 생성합니다.
     */
    public void generateNextBlock() {
        currentBlock = nextBlock;
        nextBlock = createNextBlock();
        x = 3;
        y = 0;
    }
    
    /**
     * 새로운 블록을 생성합니다. 아이템 모드에서는 아이템 블록을 생성할 수 있습니다.
     */
    private Block createNextBlock() {
        Block normalBlock = getRandomBlock();
        
        // 아이템 매니저가 있고 폭탄 블록을 생성해야 하는 경우
        if (itemManager != null && itemManager.shouldCreateItemBlock()) {
            System.out.println("Creating item block as next block! (Total lines: " + itemManager.getTotalLinesCleared() + ")");
            return itemManager.createItemBlock(normalBlock);
        }
        
        return normalBlock;
    }
    
    /**
     * 아이템 매니저를 설정합니다 (아이템 모드용).
     */
    public void setItemManager(ItemManager itemManager) {
        this.itemManager = itemManager;
    }
    
    /**
     * GameScene 참조를 설정합니다 (아이템 효과용).
     */
    public void setGameScene(Object gameScene) {
        this.gameScene = gameScene;
    }
    
    /**
     * 다음 블록을 즉시 아이템 블록으로 교체합니다.
     * 누적 2줄 이상 완성 시 즉시 폭탄 블록이 나와야 하는 경우에 사용합니다.
     */
    public void forceCreateItemBlock() {
        if (itemManager != null && itemManager.shouldCreateItemBlock()) {
            System.out.println("Force creating item block as next block! (Total lines: " + itemManager.getTotalLinesCleared() + ")");
            // 현재 미리보기에 표시된 nextBlock을 폭탄 블록으로 변환
            nextBlock = itemManager.createItemBlock(nextBlock);
        }
    }
    
    // 이동 가능성 체크 메서드들
    
    /**
     * 아래로 이동 가능한지 확인합니다.
     */
    public boolean canMoveDown() {
        return boardManager.canMoveDown(currentBlock, x, y);
    }
    
    /**
     * 왼쪽으로 이동 가능한지 확인합니다.
     */
    public boolean canMoveLeft() {
        return boardManager.canMoveLeft(currentBlock, x, y);
    }
    
    /**
     * 오른쪽으로 이동 가능한지 확인합니다.
     */
    public boolean canMoveRight() {
        return boardManager.canMoveRight(currentBlock, x, y);
    }
    
    /**
     * 회전 가능한지 확인합니다.
     */
    public boolean canRotate() {
        if (currentBlock == null) return false;
        
        // BombItemBlock의 경우 특별 처리
        if (currentBlock instanceof BombItemBlock) {
            return ((BombItemBlock) currentBlock).canRotate(boardManager.getBoard(), x, y);
        }
        
        // 일반 블록의 경우 Block.canRotate() 사용
        return currentBlock.canRotate(x, y, boardManager.getBoard(), gameWidth, gameHeight);
    }
    
    // Getter 메서드들
    
    /**
     * 현재 블록을 반환합니다.
     */
    public Block getCurrentBlock() {
        return currentBlock;
    }
    
    /**
     * 다음 블록을 반환합니다.
     */
    public Block getNextBlock() {
        return nextBlock;
    }
    
    /**
     * 현재 블록의 X 위치를 반환합니다.
     */
    public int getX() {
        return x;
    }
    
    /**
     * 현재 블록의 Y 위치를 반환합니다.
     */
    public int getY() {
        return y;
    }
    
    /**
     * 마지막 블록의 Y 위치를 반환합니다.
     */
    public int getLastBlockY() {
        return lastBlockY;
    }
    
    /**
     * 게임 종료 상태를 반환합니다.
     */
    public boolean isGameOver() {
        return isGameOver;
    }
    
    /**
     * 블록 흔들림 효과 객체를 반환합니다.
     */
    public BlockShake getBlockShake() {
        return blockShake;
    }
    
    /**
     * SpeedUp 관리자를 설정합니다.
     * 
     * @param speedUp SpeedUp 객체
     */
    public void setSpeedUp(SpeedUp speedUp) {
        this.speedUp = speedUp;
    }
    
    /**
     * 게임 종료 상태를 설정합니다.
     * 
     * @param gameOver 게임 종료 여부
     */
    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
    }
    
    /**
     * 리소스를 정리합니다.
     */
    public void cleanup() {
        if (blockShake != null) {
            blockShake.cleanup();
        }
    }
    
    /**
     * 무게추 블록의 업데이트를 처리합니다.
     * @return true if 무게추가 사라져서 다음 블록을 생성해야 함
     */
    public boolean updateWeightBlock() {
        if (currentBlock instanceof WeightItemBlock) {
            WeightItemBlock weightBlock = (WeightItemBlock) currentBlock;
            
            // 무게추가 활성화되었고 파괴 중이 아니라면 자동 낙하 처리
            if (weightBlock.isActivated() && !weightBlock.isDestroying()) {
                // 낙하 타이머 업데이트
                if (weightBlock.updateFall()) {
                    // 게임보드 바닥(y=19)에 도달했는지 확인 (무게추 높이 2 고려)
                    // 무게추의 맨 아래(y+1)가 게임보드 경계(19)를 넘지 않도록 함
                    if (y + 1 < 19) { // 무게추 맨 아래가 바닥(19)에 닿지 않음
                        // 한 칸 아래로 이동
                        y++;
                        System.out.println("WeightItemBlock moved down to y=" + y);
                        
                        // 현재 위치에서 아래의 모든 블록 제거
                        int clearedCount = weightBlock.clearBlocksBelow(
                            boardManager.getBoard(), 
                            boardManager.getBoardColors(), 
                            x, 
                            y,
                            scoreManager,
                            boardManager  // 아이템 셀 정보 삭제를 위해 BoardManager 추가
                        );
                        
                        if (clearedCount > 0) {
                            System.out.println("WeightItemBlock cleared " + clearedCount + " blocks below at y=" + y);
                        }
                        
                        return false; // 계속 떨어지는 중
                    } else {
                        // 게임보드 바닥에 도달했으면 파괴 모드로 전환
                        weightBlock.startDestroying();
                        System.out.println("WeightItemBlock reached game board bottom at y=" + y + ", starting destruction");
                        return false;
                    }
                }
            }
            
            // 파괴 중이라면 파괴 타이머 업데이트
            if (weightBlock.isDestroying()) {
                if (weightBlock.updateDestroy()) {
                    // 무게추가 완전히 사라짐
                    currentBlock = null;
                    System.out.println("WeightItemBlock completely destroyed, generating next block");
                    return true; // 다음 블록 생성 필요
                }
            }
        }
        
        return false;
    }
    
    /**
     * 현재 블록의 고스트 블록 Y 위치를 계산합니다.
     * 무게추의 경우 특별 처리를 합니다.
     * 
     * @return 고스트 블록의 Y 위치 (-1이면 고스트 블록 없음)
     */
    public int getGhostY() {
        if (currentBlock == null) return -1;
        
        // 무게추 블록의 경우 특별 처리
        if (currentBlock instanceof WeightItemBlock) {
            WeightItemBlock weightBlock = (WeightItemBlock) currentBlock;
            return weightBlock.calculateGhostY(boardManager.getBoard(), x, y);
        }
        
        // 일반 블록의 경우 직접 계산
        int ghostY = y;
        while (canMoveToPosition(ghostY + 1)) {
            ghostY++;
        }
        
        return ghostY;
    }
    
    /**
     * 지정된 Y 위치로 블록이 이동할 수 있는지 확인합니다.
     */
    private boolean canMoveToPosition(int newY) {
        if (currentBlock == null) return false;
        
        // BoardManager의 canMoveDown 메소드를 활용하여 임시 Y 위치 확인
        int originalY = y;
        y = newY - 1; // 목표 위치 -1로 설정
        boolean canMove = boardManager.canMoveDown(currentBlock, x, y);
        y = originalY; // 원래 위치로 복원
        
        return canMove;
    }
    
    /**
     * 시각적 아이템 효과를 활성화합니다 (블록 배치 전 실행).
     * SPEED_UP, SPEED_DOWN, VISION_BLOCK 등의 효과
     */
    private void activateVisualItemEffects(ItemBlock itemBlock) {
        if (itemBlock == null || itemManager == null) {
            return;
        }
        
        // 바닥 착지 시에만 처리하는 아이템 타입들 (시야 차단만)
        ItemEffectType itemType = itemBlock.getItemType();
        if (itemType == ItemEffectType.VISION_BLOCK) {
            
            System.out.println("🎯 Activating Visual ItemBlock with " + itemBlock.getItemType().getDisplayName() + " (before placement)");
            
            // 아이템 효과 생성
            ItemEffect effect = ItemEffectFactory.createEffect(itemBlock.getItemType());
            
            if (effect != null) {
                // ItemEffectContext 생성
                int[] itemPos = itemBlock.getItemPosition();
                int absoluteItemX = x + itemPos[0];
                int absoluteItemY = y + itemPos[1];
                
                ItemEffectContext context = new ItemEffectContext(
                    boardManager.getBoard(), 
                    absoluteItemX, 
                    absoluteItemY
                );
                
                // 필요한 컨텍스트 정보 설정
                context.setBlockManager(this);
                context.setBoardManager(boardManager);
                context.setScoreManager(scoreManager);
                context.setGameScene(gameScene);
                
                // 아이템 효과 활성화
                itemManager.activateItemEffect(effect, context);
                
                System.out.println("✅ Visual ItemBlock effect activated successfully!");
            } else {
                System.out.println("❌ Failed to create visual item effect for " + itemBlock.getItemType());
            }
        }
    }
    
    /**
     * 보드 조작 아이템 효과를 활성화합니다 (블록 배치 후 실행).
     * LINE_CLEAR, CLEANUP 등의 효과 (속도 아이템 제외)
     */
    private void activateBoardManipulationEffects(ItemBlock itemBlock) {
        if (itemBlock == null || itemManager == null) {
            return;
        }
        
        // 보드 조작 효과를 처리하는 아이템 타입들 (속도 아이템은 줄 삭제 시에만 활성화)
        ItemEffectType itemType = itemBlock.getItemType();
        if (itemType == ItemEffectType.LINE_CLEAR || 
            itemType == ItemEffectType.CLEANUP) {
            
            System.out.println("🎯 Activating Board Manipulation ItemBlock with " + itemBlock.getItemType().getDisplayName() + " (after placement)");
            
            // 아이템 효과 생성
            ItemEffect effect = ItemEffectFactory.createEffect(itemBlock.getItemType());
            
            if (effect != null) {
                // ItemEffectContext 생성 (배치 후 최신 보드 상태 반영)
                int[] itemPos = itemBlock.getItemPosition();
                int absoluteItemX = x + itemPos[0];
                int absoluteItemY = y + itemPos[1];
                
                ItemEffectContext context = new ItemEffectContext(
                    boardManager.getBoard(), 
                    absoluteItemX, 
                    absoluteItemY
                );
                
                // 필요한 컨텍스트 정보 설정
                context.setBlockManager(this);
                context.setBoardManager(boardManager);
                context.setScoreManager(scoreManager);
                context.setGameScene(gameScene);
                
                // 아이템 효과 활성화
                itemManager.activateItemEffect(effect, context);
                
                System.out.println("✅ Board Manipulation ItemBlock effect activated successfully!");
            } else {
                System.out.println("❌ Failed to create board manipulation item effect for " + itemBlock.getItemType());
            }
        }
    }
    
    /**
     * 테스트용 랜덤 블록 생성 메서드 (public)
     * 
     * @return 생성된 블록
     */
    public Block getRandomBlockForTest() {
        return getRandomBlock();
    }
}