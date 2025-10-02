package tetris.scene.game.blocks;

/**
 * 블록 하드 드롭 기능을 담당하는 유틸리티 클래스
 * 블록을 즉시 바닥까지 떨어뜨리는 기능을 제공합니다.
 */
public class BlockHardDrop {
    
    /**
     * 하드 드롭을 실행합니다. 현재 블록을 가능한 한 아래까지 즉시 떨어뜨립니다.
     * 
     * @param curr 현재 블록
     * @param x 현재 블록의 x 위치 (참조로 전달될 수 없으므로 반환값으로 처리)
     * @param y 현재 블록의 y 위치 (참조로 전달될 수 없으므로 반환값으로 처리)
     * @param board 게임 보드 상태 배열
     * @param gameWidth 게임 보드 너비
     * @param gameHeight 게임 보드 높이
     * @return 하드 드롭 후의 최종 y 위치
     */
    public static int executeHardDrop(Block curr, int x, int y, int[][] board, int gameWidth, int gameHeight) {
        if (curr == null) return y;
        
        int finalY = y;
        
        // 블록이 더 이상 아래로 갈 수 없을 때까지 y 좌표를 증가
        while (canMoveDown(curr, x, finalY, board, gameWidth, gameHeight)) {
            finalY++;
        }
        
        System.out.println("Hard drop executed! Block moved from y=" + y + " to y=" + finalY);
        return finalY;
    }
    
    /**
     * 블록이 아래로 이동할 수 있는지 확인합니다.
     * 
     * @param curr 현재 블록
     * @param x 현재 블록의 x 위치
     * @param y 현재 블록의 y 위치
     * @param board 게임 보드 상태 배열
     * @param gameWidth 게임 보드 너비
     * @param gameHeight 게임 보드 높이
     * @return 아래로 이동 가능하면 true, 불가능하면 false
     */
    private static boolean canMoveDown(Block curr, int x, int y, int[][] board, int gameWidth, int gameHeight) {
        if (y + curr.height() >= gameHeight) return false;
        
        for (int j = 0; j < curr.height(); j++) {
            for (int i = 0; i < curr.width(); i++) {
                if (curr.getShape(i, j) == 1) {
                    int newY = y + j + 1;
                    int newX = x + i;
                    if (newY >= gameHeight || (newY >= 0 && newX >= 0 && newX < gameWidth && board[newY][newX] == 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * 하드 드롭 결과를 나타내는 내부 클래스
     */
    public static class HardDropResult {
        public final int finalY;
        public final boolean blockPlaced;
        
        public HardDropResult(int finalY, boolean blockPlaced) {
            this.finalY = finalY;
            this.blockPlaced = blockPlaced;
        }
    }
    
    /**
     * 하드 드롭을 실행하고 결과 정보를 반환합니다.
     * 
     * @param curr 현재 블록
     * @param x 현재 블록의 x 위치
     * @param y 현재 블록의 y 위치
     * @param board 게임 보드 상태 배열
     * @param gameWidth 게임 보드 너비
     * @param gameHeight 게임 보드 높이
     * @return 하드 드롭 결과 (최종 위치, 블록 배치 여부)
     */
    public static HardDropResult executeHardDropWithResult(Block curr, int x, int y, int[][] board, int gameWidth, int gameHeight) {
        if (curr == null) return new HardDropResult(y, false);
        
        int finalY = executeHardDrop(curr, x, y, board, gameWidth, gameHeight);
        boolean blockPlaced = (finalY != y); // 위치가 변경되었으면 블록이 이동된 것
        
        return new HardDropResult(finalY, blockPlaced);
    }
}