package tetris.scene.game.core;

import tetris.scene.game.blocks.*;
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
     * 
     * @return 생성된 블록
     */
    private Block getRandomBlock() {
        // 난이도에 따른 I형 블럭 확률 계산
        double iBlockProbability;
        switch (difficulty) {
            case EASY:
                iBlockProbability = 1.0 / 7 * 1.2; // 20% 증가
                break;
            case HARD:
                iBlockProbability = 1.0 / 7 * 0.8; // 20% 감소
                break;
            default:
                iBlockProbability = 1.0 / 7; // 기본 확률
        }
        
        Block newBlock;
        if (random.nextDouble() < iBlockProbability) {
            newBlock = new IBlock();
        } else {
            // IBlock 외 다른 블록들 중 랜덤 선택
            int block = random.nextInt(6) + 1; // 1~6 (IBlock 제외)
            switch (block) {
                case 1: newBlock = new JBlock(); break;
                case 2: newBlock = new LBlock(); break;
                case 3: newBlock = new ZBlock(); break;
                case 4: newBlock = new SBlock(); break;
                case 5: newBlock = new TBlock(); break;
                case 6: newBlock = new OBlock(); break;
                default: newBlock = new LBlock(); break;
            }
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
     * 블록을 아래로 이동시킵니다.
     * 
     * @return 블록이 고정되었으면 true, 그냥 이동했으면 false
     */
    public boolean moveDown() {
        if (currentBlock == null || isGameOver) return false;
        
        if (canMoveDown()) {
            y++;
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
        
        // BoardManager를 사용하여 블록을 영구적으로 보드에 고정
        boardManager.placeBlock(currentBlock, x, y);
        
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
                            y
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
     * 테스트용 랜덤 블록 생성 메서드 (public)
     * 
     * @return 생성된 블록
     */
    public Block getRandomBlockForTest() {
        return getRandomBlock();
    }
}