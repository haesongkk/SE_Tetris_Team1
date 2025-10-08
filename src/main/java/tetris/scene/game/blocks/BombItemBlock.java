package tetris.scene.game.blocks;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Random;

/**
 * 폭탄 아이템을 포함한 블록 클래스
 * 기존 블록의 셀 중 하나가 폭탄으로 변환됩니다.
 */
public class BombItemBlock extends Block {
    private Block originalBlock; // 원본 블록
    private int bombX, bombY; // 폭탄 위치
    private int bombCellIndex; // 원본 블록에서 폭탄 셀의 인덱스 (0,1,2,3...)
    private int[][] itemShape; // 아이템 정보 (0: 일반, 1: 폭탄)
    private static BufferedImage bombImage; // 폭탄 이미지
    
    // 정적 블록에서 폭탄 이미지 로드
    static {
        try {
            // 폭탄 이미지를 resources에서 로드 (없으면 null)
            System.out.println("Attempting to load bomb image from /bomb.png");
            java.io.InputStream stream = BombItemBlock.class.getResourceAsStream("/bomb.png");
            System.out.println("Image stream: " + stream);
            if (stream != null) {
                bombImage = ImageIO.read(stream);
                System.out.println("✅ Bomb image loaded successfully! Size: " + 
                    (bombImage != null ? bombImage.getWidth() + "x" + bombImage.getHeight() : "null"));
            } else {
                System.out.println("❌ Bomb image stream is null");
                bombImage = null;
            }
        } catch (IOException e) {
            System.out.println("❌ Bomb image file error: " + e.getMessage());
            bombImage = null;
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Bomb image not found in resources: " + e.getMessage());
            bombImage = null;
        } catch (Exception e) {
            System.out.println("❌ Unexpected error loading bomb image: " + e.getMessage());
            bombImage = null;
        }
    }
    
    public BombItemBlock(Block originalBlock) {
        this.originalBlock = originalBlock;
        this.color = originalBlock.getColor();
        
        // 원본 블록의 모양 복사
        copyOriginalShape();
        
        // 폭탄 위치 설정
        setBombPosition();
    }
    
    /**
     * 원본 블록의 모양을 복사합니다.
     */
    private void copyOriginalShape() {
        int height = originalBlock.shape.length; // 실제 배열 크기 사용
        int width = originalBlock.shape[0].length; // 실제 배열 크기 사용
        
        shape = new int[height][width];
        itemShape = new int[height][width];
        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                shape[i][j] = originalBlock.getShape(j, i);
                itemShape[i][j] = 0; // 기본값: 일반 셀
            }
        }
    }
    
    /**
     * 블록의 셀 중 하나를 무작위로 폭탄으로 설정합니다.
     */
    private void setBombPosition() {
        Random random = new Random();
        
        // 블록이 있는 셀들의 위치를 찾습니다
        java.util.List<int[]> blockCells = new java.util.ArrayList<>();
        
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    blockCells.add(new int[]{i, j});
                }
            }
        }
        
        // 무작위로 하나의 셀을 폭탄으로 설정
        if (!blockCells.isEmpty()) {
            bombCellIndex = random.nextInt(blockCells.size()); // 셀 인덱스 저장
            int[] bombCell = blockCells.get(bombCellIndex);
            bombY = bombCell[0];
            bombX = bombCell[1];
            itemShape[bombY][bombX] = 1; // 폭탄 표시
            
            System.out.println("Bomb set at position: (" + bombX + ", " + bombY + ") with cell index: " + bombCellIndex);
        }
    }
    
    /**
     * 지정된 위치가 폭탄인지 확인합니다.
     */
    public boolean isBombCell(int x, int y) {
        if (y >= 0 && y < itemShape.length && x >= 0 && x < itemShape[y].length) {
            return itemShape[y][x] == 1;
        }
        return false;
    }
    
    /**
     * 폭탄의 색상을 반환합니다 (빨간색).
     */
    public Color getBombColor() {
        return Color.RED;
    }
    
    /**
     * 셀의 색상을 반환합니다 (폭탄이면 빨간색, 아니면 원본 색상).
     */
    public Color getCellColor(int x, int y) {
        if (isBombCell(x, y)) {
            return getBombColor();
        }
        return originalBlock.getColor();
    }
    
    /**
     * 폭탄 위치 정보를 반환합니다.
     */
    public int[] getBombPosition() {
        return new int[]{bombX, bombY};
    }
    
    /**
     * 이 블록이 아이템 블록인지 확인합니다.
     */
    public boolean hasItem() {
        return true;
    }
    
    /**
     * 폭탄 셀을 화면에 그립니다.
     * @param g2d Graphics2D 객체
     * @param x 그릴 x 위치
     * @param y 그릴 y 위치
     * @param cellSize 셀 크기
     */
    public void drawBombCell(Graphics2D g2d, int x, int y, int cellSize) {
        try {
            if (bombImage != null) {
                // 폭탄 이미지가 있으면 이미지를 그립니다
                g2d.drawImage(bombImage, x, y, cellSize, cellSize, null);
            } else {
                // 폭탄 이미지가 없으면 빨간색 원으로 그립니다
                g2d.setColor(Color.RED);
                g2d.fillOval(x + cellSize/4, y + cellSize/4, cellSize/2, cellSize/2);
                
                // 폭탄 테두리
                g2d.setColor(Color.DARK_GRAY);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(x + cellSize/4, y + cellSize/4, cellSize/2, cellSize/2);
                
                // 폭탄 심지 (작은 선)
                g2d.setColor(Color.ORANGE);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawLine(x + cellSize/2, y + cellSize/4, x + cellSize/2 + cellSize/8, y + cellSize/8);
            }
        } catch (Exception e) {
            // 에러가 발생하면 기본 빨간색 사각형으로 그립니다
            g2d.setColor(Color.RED);
            g2d.fillRect(x, y, cellSize, cellSize);
            g2d.setColor(Color.WHITE);
            g2d.drawString("💣", x + cellSize/4, y + cellSize*3/4);
        } finally {
            g2d.dispose();
        }
    }
    
    @Override
    public void rotate() {
        // 원본 블록 회전
        originalBlock.rotate();
        
        // 회전된 모양 복사
        copyOriginalShapeOnly();
        
        // 동일한 셀 인덱스의 새로운 위치로 폭탄 이동
        updateBombPositionAfterRotation();
    }
    
    /**
     * 회전 후 동일한 셀 인덱스 위치로 폭탄을 이동시킵니다.
     */
    private void updateBombPositionAfterRotation() {
        int oldBombX = bombX;
        int oldBombY = bombY;
        
        // 블록 타입별 특별 처리
        String blockType = originalBlock.getClass().getSimpleName();
        
        switch (blockType) {
            case "OBlock":
                // O블록: 2x2 사각형이므로 셀 인덱스 매핑이 단순함
                handleOBlockRotation(oldBombX, oldBombY);
                break;
                
            case "IBlock":
                // I블록: 특별한 회전 상태 처리
                handleIBlockRotation(oldBombX, oldBombY);
                break;
                
            default:
                // J, L, S, Z, T 블록: 기하학적 회전 공식 사용
                handleGeometricRotation(oldBombX, oldBombY);
                break;
        }
    }
    
    /**
     * 기하학적 회전 공식을 사용하여 폭탄 위치를 계산합니다.
     * Block.java의 rotate() 공식과 동일: rotated[j][rows-1-i] = original[i][j]
     */
    private void handleGeometricRotation(int oldBombX, int oldBombY) {
        // 회전 전 블록 크기 (원본 블록을 한 번 더 회전시켜서 이전 상태 확인)
        originalBlock.rotate(); originalBlock.rotate(); originalBlock.rotate(); // 3번 더 회전 = 원래 상태로 복귀
        int oldRows = originalBlock.shape.length; // 실제 배열 크기 사용
        originalBlock.rotate(); // 다시 현재 상태로
        
        // 기하학적 회전 공식 적용: (x,y) -> (oldRows-1-y, x)
        int newBombX = oldRows - 1 - oldBombY;
        int newBombY = oldBombX;
        
        setBombAtPosition(newBombX, newBombY, oldBombX, oldBombY);
    }
    
    /**
     * O블록의 회전 처리 (2x2 정사각형)
     */
    private void handleOBlockRotation(int oldBombX, int oldBombY) {
        // O블록은 2x2이므로 회전해도 상대적 위치가 유지됨
        // 하지만 실제로는 시계방향으로 위치가 바뀜: (0,0)->(0,1)->(1,1)->(1,0)
        int newBombX, newBombY;
        
        if (oldBombX == 0 && oldBombY == 0) {      // 좌상 -> 우상
            newBombX = 1; newBombY = 0;
        } else if (oldBombX == 1 && oldBombY == 0) { // 우상 -> 우하  
            newBombX = 1; newBombY = 1;
        } else if (oldBombX == 1 && oldBombY == 1) { // 우하 -> 좌하
            newBombX = 0; newBombY = 1;
        } else {                                   // 좌하 -> 좌상
            newBombX = 0; newBombY = 0;
        }
        
        setBombAtPosition(newBombX, newBombY, oldBombX, oldBombY);
    }
    
    /**
     * I블록의 회전 처리
     */
    private void handleIBlockRotation(int oldBombX, int oldBombY) {
        // I블록의 현재 회전 상태 확인
        if (originalBlock instanceof IBlock) {
            IBlock iBlock = (IBlock) originalBlock;
            int rotationState = iBlock.getRotationState();
            
            System.out.println("I-Block rotation state: " + rotationState + ", bombCellIndex: " + bombCellIndex);
            
            // 가로/세로 상태에 따라 인덱스 매핑
            if (rotationState % 2 == 0) { // 가로 상태 (1x4)
                bombCellIndex = Math.min(bombCellIndex, 3); // 0~3 범위 제한
                bombX = bombCellIndex;
                bombY = 0;
                System.out.println("I-Block horizontal: bombX=" + bombX + ", bombY=" + bombY);
            } else { // 세로 상태 (4x2, 두 번째 열 사용)
                bombCellIndex = Math.min(bombCellIndex, 3); // 0~3 범위 제한
                bombX = 1; // 두 번째 열 (I블록의 세로 형태에서 블록이 있는 열)
                bombY = bombCellIndex;
                System.out.println("I-Block vertical: bombX=" + bombX + ", bombY=" + bombY);
            }
            
            setBombAtPosition(bombX, bombY, oldBombX, oldBombY);
        }
    }

    
    /**
     * 폭탄을 새로운 위치에 설정하고 로그를 출력합니다.
     */
    private void setBombAtPosition(int newBombX, int newBombY, int oldBombX, int oldBombY) {
        // 범위 체크
        if (newBombY >= 0 && newBombY < itemShape.length && 
            newBombX >= 0 && newBombX < itemShape[newBombY].length) {
            
            bombX = newBombX;
            bombY = newBombY;
            
            // itemShape 재초기화
            clearItemShape();
            itemShape[bombY][bombX] = 1;
            
            System.out.println("Bomb rotated from (" + oldBombX + ", " + oldBombY + ") to (" + bombX + ", " + bombY + ") [index: " + bombCellIndex + "]");
        } else {
            System.out.println("Warning: Bomb position out of bounds after rotation!");
        }
    }
    
    /**
     * 원본 블록의 모양만 복사합니다 (itemShape는 건드리지 않음).
     */
    private void copyOriginalShapeOnly() {
        int height = originalBlock.shape.length; // 실제 배열 크기 사용
        int width = originalBlock.shape[0].length; // 실제 배열 크기 사용
        
        shape = new int[height][width];
        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                shape[i][j] = originalBlock.getShape(j, i);
            }
        }
        
        // itemShape 배열 크기 조정 (기존 데이터는 유지하되 크기만 맞춤)
        if (itemShape == null || itemShape.length != height || itemShape[0].length != width) {
            int[][] newItemShape = new int[height][width];
            itemShape = newItemShape;
        }
    }
    
    /**
     * itemShape 배열을 초기화합니다.
     */
    private void clearItemShape() {
        for (int i = 0; i < itemShape.length; i++) {
            for (int j = 0; j < itemShape[i].length; j++) {
                itemShape[i][j] = 0;
            }
        }
    }
    
    /**
     * 회전이 가능한지 확인합니다 (원본 블록을 기준으로).
     * @param board 게임 보드
     * @param x 현재 x 위치
     * @param y 현재 y 위치
     * @return 회전 가능 여부
     */
    public boolean canRotate(int[][] board, int x, int y) {
        // 원본 블록의 canRotate 메소드를 직접 사용
        return originalBlock.canRotate(x, y, board, board[0].length, board.length);
    }
    
    /**
     * 원본 블록을 반환합니다.
     * @return 원본 블록
     */
    public Block getOriginalBlock() {
        return originalBlock;
    }
}