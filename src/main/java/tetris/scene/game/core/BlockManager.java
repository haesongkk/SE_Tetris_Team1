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
        int block = rnd.nextInt(7); // 0~6
        
        // 블록 생성 수 증가 (SpeedUp 관리자 사용)
        if (speedUp != null) {
            speedUp.onBlockGenerated(isGameOver);
        }
        
        switch (block) {
            case 0: return new IBlock();
            case 1: return new JBlock();
            case 2: return new LBlock();
            case 3: return new ZBlock();
            case 4: return new SBlock();
            case 5: return new TBlock();
            case 6: return new OBlock();
        }
        return new LBlock();
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
            
            // 게임이 종료되지 않은 경우에만 다음 블록 생성
            if (!isGameOver) {
                generateNextBlock();
            }
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
        
        // 게임이 종료되지 않은 경우에만 다음 블록 생성
        if (!isGameOver) {
            generateNextBlock();
        }
        
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
    private void generateNextBlock() {
        currentBlock = nextBlock;
        nextBlock = getRandomBlock();
        x = 3;
        y = 0;
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
        return BlockRotation.canRotate(currentBlock, x, y, boardManager.getBoard(), gameWidth, gameHeight);
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