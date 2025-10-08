package tetris.scene.game.core;

import tetris.scene.game.blocks.*;
import tetris.util.SpeedUp;
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
    
    // 의존성
    private final BoardManager boardManager;
    private final BlockShake blockShake;
    private ItemManager itemManager; // 아이템 모드용 (옵션)
    
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
     */
    public BlockManager(int gameWidth, int gameHeight, BoardManager boardManager) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.boardManager = boardManager;
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
        Random rnd = new Random(System.currentTimeMillis());
        int block = rnd.nextInt(7); // 0~6 랜덤 블록
        
        // 블록 생성 수 증가 (SpeedUp 관리자 사용)
        if (speedUp != null) {
            speedUp.onBlockGenerated(isGameOver);
        }
        
        Block newBlock;
        switch (block) {
            case 0: newBlock = new IBlock(); break;
            case 1: newBlock = new JBlock(); break;
            case 2: newBlock = new LBlock(); break;
            case 3: newBlock = new ZBlock(); break;
            case 4: newBlock = new SBlock(); break;
            case 5: newBlock = new TBlock(); break;
            case 6: newBlock = new OBlock(); break;
            default: newBlock = new LBlock(); break;
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
            // 블록이 바닥에 닿았을 때 보드에 고정
            placeBlockPermanently();
            
            // 다음 블록 생성은 줄 삭제 검사 이후에 GameScene에서 호출
            return true; // 블록 고정됨
        }
    }
    
    /**
     * 블록을 왼쪽으로 이동시킵니다.
     */
    public void moveLeft() {
        if (currentBlock == null || isGameOver) return;
        
        if (canMoveLeft()) {
            x--;
        }
    }
    
    /**
     * 블록을 오른쪽으로 이동시킵니다.
     */
    public void moveRight() {
        if (currentBlock == null || isGameOver) return;
        
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
        
        // BlockHardDrop 클래스를 사용하여 하드 드롭 실행
        int newY = BlockHardDrop.executeHardDrop(currentBlock, x, y, 
                                                boardManager.getBoard(), gameWidth, gameHeight);
        y = newY;
        
        // 블록이 바닥에 닿았으므로 즉시 고정
        placeBlockPermanently();
        
        // 다음 블록 생성은 줄 삭제 검사 이후에 GameScene에서 호출
        
        return true;
    }
    
    /**
     * 현재 블록을 보드에 영구적으로 고정합니다.
     */
    private void placeBlockPermanently() {
        System.out.println("Placing block permanently at x=" + x + ", y=" + y);
        
        // BoardManager를 사용하여 블록을 영구적으로 보드에 고정
        boardManager.placeBlock(currentBlock, x, y);
        
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
}