package tetris.scene.game.items;

/**
 * 아이템 효과 실행에 필요한 컨텍스트 정보를 담는 클래스
 */
public class ItemEffectContext {
    private int[][] board;
    private int itemX;
    private int itemY;
    private Object gameScene;
    private Object blockManager;
    private Object boardManager;
    private Object scoreManager;
    
    public ItemEffectContext(int[][] board, int itemX, int itemY) {
        this.board = board;
        this.itemX = itemX;
        this.itemY = itemY;
    }
    
    public int[][] getBoard() {
        return board;
    }
    
    public void setBoard(int[][] board) {
        this.board = board;
    }
    
    public int getItemX() {
        return itemX;
    }
    
    public void setItemX(int itemX) {
        this.itemX = itemX;
    }
    
    public int getItemY() {
        return itemY;
    }
    
    public void setItemY(int itemY) {
        this.itemY = itemY;
    }
    
    public Object getGameScene() {
        return gameScene;
    }
    
    public void setGameScene(Object gameScene) {
        this.gameScene = gameScene;
    }
    
    public Object getBlockManager() {
        return blockManager;
    }
    
    public void setBlockManager(Object blockManager) {
        this.blockManager = blockManager;
    }
    
    public Object getBoardManager() {
        return boardManager;
    }
    
    public void setBoardManager(Object boardManager) {
        this.boardManager = boardManager;
    }
    
    public Object getScoreManager() {
        return scoreManager;
    }
    
    public void setScoreManager(Object scoreManager) {
        this.scoreManager = scoreManager;
    }
}