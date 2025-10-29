package tetris.scene.game.blocks;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import tetris.scene.game.items.ItemEffectType;

/**
 * 새로운 아이템 시스템을 위한 블록 클래스
 * 기존 블록에 특정 아이템 효과를 추가한 블록입니다.
 * 
 * 지원되는 아이템 타입:
 * - LINE_CLEAR (L): 줄 삭제 아이템 - 전체 행 제거
 * - CLEANUP (C): 청소 아이템 - 3x3 영역 제거
 * - SPEED_DOWN (S): 속도 감소 아이템 - 5초간 50% 속도 감소
 * - SPEED_UP (F): 속도 증가 아이템 - 5초간 100% 속도 증가
 * - VISION_BLOCK (V): 시야 차단 아이템 - 8초간 4x20 영역 차단
 */
public class ItemBlock extends Block {
    // 아이템 이미지들
    private static BufferedImage broomImage;        // 청소 아이템 (CLEANUP)
    private static BufferedImage snailImage;        // 속도 감소 아이템 (SPEED_DOWN)
    private static BufferedImage runningImage;      // 속도 증가 아이템 (SPEED_UP)
    private static BufferedImage visionBlockImage;  // 시야 차단 아이템 (VISION_BLOCK)
    
    // 정적 블록에서 이미지 로드
    static {
        loadItemImages();
    }
    private final Block originalBlock; // 원본 블록
    private final ItemEffectType itemType; // 아이템 타입
    private int[][] itemShape; // 아이템 정보 (0: 일반, 1: 아이템) - 회전 시 크기 변경 가능하도록 final 제거
    private int itemX, itemY; // 아이템 위치 (회전 시 업데이트 가능하도록 final 제거)
    private int itemCellIndex; // 원본 블록에서 아이템 셀의 인덱스 (0,1,2,3...)
    
    /**
     * ItemBlock 생성자
     * @param originalBlock 원본 블록
     * @param itemType 아이템 효과 타입
     */
    public ItemBlock(Block originalBlock, ItemEffectType itemType) {
        this.originalBlock = originalBlock;
        this.itemType = itemType;
        
        // 원본 블록의 모양과 색상을 복사
        copyOriginalBlockProperties();
        
        // 아이템 모양 배열 초기화
        this.itemShape = new int[shape.length][shape[0].length];
        
        // 아이템 위치 설정 (원본 블록의 셀 중 하나를 아이템으로 설정)
        setRandomItemPosition();
        
        System.out.println("Created ItemBlock with " + itemType.getDisplayName() + 
                          " at position (" + itemX + "," + itemY + ")");
    }
    
    /**
     * 아이템 이미지들을 resources에서 로드합니다.
     */
    private static void loadItemImages() {
        try {
            // 청소 아이템 이미지 (broom.png)
            System.out.println("Loading cleanup item image from /broom.png");
            java.io.InputStream broomStream = ItemBlock.class.getResourceAsStream("/broom.png");
            if (broomStream != null) {
                broomImage = ImageIO.read(broomStream);
                System.out.println("✅ Cleanup (broom) image loaded successfully!");
            } else {
                System.out.println("❌ Cleanup (broom) image stream is null");
                broomImage = null;
            }
            
            // 속도 감소 아이템 이미지 (snail.png)
            System.out.println("Loading speed down item image from /snail.png");
            java.io.InputStream snailStream = ItemBlock.class.getResourceAsStream("/snail.png");
            if (snailStream != null) {
                snailImage = ImageIO.read(snailStream);
                System.out.println("✅ Speed down (snail) image loaded successfully!");
            } else {
                System.out.println("❌ Speed down (snail) image stream is null");
                snailImage = null;
            }
            
            // 속도 증가 아이템 이미지 (running.png)
            System.out.println("Loading speed up item image from /running.png");
            java.io.InputStream runningStream = ItemBlock.class.getResourceAsStream("/running.png");
            if (runningStream != null) {
                runningImage = ImageIO.read(runningStream);
                System.out.println("✅ Speed up (running) image loaded successfully!");
            } else {
                System.out.println("❌ Speed up (running) image stream is null");
                runningImage = null;
            }
            
            // 시야 차단 아이템 이미지 (visionblock.png)
            System.out.println("Loading vision block item image from /visionblock.png");
            java.io.InputStream visionStream = ItemBlock.class.getResourceAsStream("/visionblock.png");
            if (visionStream != null) {
                visionBlockImage = ImageIO.read(visionStream);
                System.out.println("✅ Vision block image loaded successfully!");
            } else {
                System.out.println("❌ Vision block image stream is null");
                visionBlockImage = null;
            }
            
        } catch (IOException e) {
            System.out.println("❌ Item image file error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Unexpected error loading item images: " + e.getMessage());
        }
    }
    
    /**
     * 원본 블록의 속성을 복사합니다.
     */
    private void copyOriginalBlockProperties() {
        // 원본 블록의 모양 복사
        int height = originalBlock.shape.length; // 실제 배열 크기 사용
        int width = originalBlock.shape[0].length; // 실제 배열 크기 사용
        this.shape = new int[height][width];
        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                this.shape[i][j] = originalBlock.getShape(j, i);
            }
        }
        
        // 원본 블록의 색상 복사
        this.color = originalBlock.getColor();
    }
    
    /**
     * 원본 블록의 셀 중 하나를 랜덤하게 아이템 위치로 설정합니다.
     */
    private void setRandomItemPosition() {
        java.util.Random random = new java.util.Random();
        
        // 블록이 있는 셀들의 위치를 찾습니다
        java.util.List<int[]> blockCells = new java.util.ArrayList<>();
        
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    blockCells.add(new int[]{i, j}); // y, x 순서
                }
            }
        }
        
        // 무작위로 하나의 셀을 아이템으로 설정
        if (!blockCells.isEmpty()) {
            itemCellIndex = random.nextInt(blockCells.size()); // 셀 인덱스 저장
            int[] itemCell = blockCells.get(itemCellIndex);
            itemY = itemCell[0];
            itemX = itemCell[1];
            itemShape[itemY][itemX] = 1; // 아이템 표시
            
            System.out.println("Item set at position: (" + itemX + ", " + itemY + ") with cell index: " + itemCellIndex);
        }
    }
    
    /**
     * 지정된 위치가 아이템인지 확인합니다.
     * @param x x 좌표
     * @param y y 좌표
     * @return 아이템이면 true
     */
    public boolean isItemCell(int x, int y) {
        if (y >= 0 && y < itemShape.length && x >= 0 && x < itemShape[y].length) {
            return itemShape[y][x] == 1;
        }
        return false;
    }
    
    /**
     * 아이템 타입을 반환합니다.
     * @return ItemEffectType
     */
    public ItemEffectType getItemType() {
        return itemType;
    }
    
    /**
     * 원본 블록을 반환합니다.
     * @return 원본 Block 객체
     */
    public Block getOriginalBlock() {
        return originalBlock;
    }
    
    /**
     * 아이템 위치를 반환합니다.
     * @return [x, y] 위치 배열
     */
    public int[] getItemPosition() {
        return new int[]{itemX, itemY};
    }
    
    /**
     * 셀의 색상을 반환합니다 (아이템이면 아이템 색상, 아니면 원본 색상).
     * @param x x 좌표
     * @param y y 좌표
     * @return 색상
     */
    public Color getCellColor(int x, int y) {
        if (isItemCell(x, y)) {
            return getItemColor();
        }
        return originalBlock.getColor();
    }
    
    /**
     * 아이템 타입에 따른 아이템 색상을 반환합니다.
     * @return 아이템 색상
     */
    public Color getItemColor() {
        switch (itemType) {
            case LINE_CLEAR:
                return Color.RED; // 빨간색 - 줄 삭제
            case CLEANUP:
                return Color.GREEN; // 녹색 - 청소
            case SPEED_DOWN:
                return Color.BLUE; // 파란색 - 속도 감소
            case SPEED_UP:
                return Color.YELLOW; // 노란색 - 속도 증가
            case VISION_BLOCK:
                return originalBlock.getColor(); // 원본 블록 색상 - 시야 차단
            default:
                return Color.WHITE; // 기본값
        }
    }
    
    /**
     * 아이템 타입에 따른 아이템 이미지를 반환합니다.
     * @return 아이템 이미지 (없으면 null)
     */
    public BufferedImage getItemImage() {
        switch (itemType) {
            case LINE_CLEAR:
                return null; // LINE_CLEAR는 기존 폭탄 이미지나 색상으로 표시
            case CLEANUP:
                return broomImage; // 빗자루 이미지
            case SPEED_DOWN:
                return snailImage; // 달팽이 이미지
            case SPEED_UP:
                return runningImage; // 달리기 이미지
            case VISION_BLOCK:
                return visionBlockImage; // 시야 차단 이미지
            default:
                return null;
        }
    }
    
    /**
     * 아이템 셀을 화면에 그립니다.
     * @param g2d Graphics2D 객체
     * @param x 그릴 x 위치 (픽셀)
     * @param y 그릴 y 위치 (픽셀)
     * @param cellSize 셀 크기 (픽셀)
     */
    public void drawItemCell(Graphics2D g2d, int x, int y, int cellSize) {
        BufferedImage itemImage = getItemImage();
        
        if (itemImage != null) {
            // 이미지가 있으면 흰색 배경 + 이미지로 그리기
            g2d.setColor(Color.WHITE);
            g2d.fillRect(x, y, cellSize, cellSize);
            g2d.drawImage(itemImage, x, y, cellSize, cellSize, null);
        } else {
            // 이미지가 없으면 색상으로 그리기 (LINE_CLEAR의 경우)
            // 검은색 바탕
            g2d.setColor(Color.BLACK);
            g2d.fillRect(x, y, cellSize, cellSize);
            
            // 아이템 심볼 텍스트 추가 (흰색 글자, 5pt 큰 폰트)
            g2d.setColor(Color.WHITE);
            int fontSize = cellSize / 3 + 5; // 기본 크기보다 5pt 키움
            g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
            FontMetrics fm = g2d.getFontMetrics();
            String symbol = getItemSymbol();
            int textX = x + (cellSize - fm.stringWidth(symbol)) / 2;
            int textY = y + (cellSize + fm.getAscent()) / 2;
            g2d.drawString(symbol, textX, textY);
        }
    }
    
    /**
     * 이 블록이 아이템 블록인지 확인합니다.
     * @return 항상 true
     */
    public boolean hasItem() {
        return true;
    }
    
    /**
     * 아이템 블록인지 확인합니다.
     * @return 항상 true
     */
    public boolean isItemBlock() {
        return true;
    }
    
    /**
     * 블록 회전 시 아이템 위치도 함께 회전시킵니다.
     */
    @Override
    public void rotate() {
        // 원본 블록 회전
        originalBlock.rotate();
        
        // 회전된 모양 복사
        copyOriginalShapeOnly();
        
        // 동일한 셀 인덱스의 새로운 위치로 아이템 이동
        updateItemPositionAfterRotation();
    }
    
    /**
     * 회전 후 동일한 셀 인덱스 위치로 아이템을 이동시킵니다.
     */
    private void updateItemPositionAfterRotation() {
        int oldItemX = itemX;
        int oldItemY = itemY;
        
        // 블록 타입별 특별 처리
        String blockType = originalBlock.getClass().getSimpleName();
        
        switch (blockType) {
            case "OBlock":
                // O블록: 2x2 사각형이므로 셀 인덱스 매핑이 단순함
                handleOBlockRotation(oldItemX, oldItemY);
                break;
                
            case "IBlock":
                // I블록: 특별한 회전 상태 처리
                handleIBlockRotation(oldItemX, oldItemY);
                break;
                
            default:
                // J, L, S, Z, T 블록: 기하학적 회전 공식 사용
                handleGeometricRotation(oldItemX, oldItemY);
                break;
        }
    }
    
    /**
     * 기하학적 회전 공식을 사용하여 아이템 위치를 계산합니다.
     * Block.java의 rotate() 공식과 동일: rotated[j][rows-1-i] = original[i][j]
     */
    private void handleGeometricRotation(int oldItemX, int oldItemY) {
        // 회전 전 블록 크기 (원본 블록을 한 번 더 회전시켜서 이전 상태 확인)
        originalBlock.rotate(); originalBlock.rotate(); originalBlock.rotate(); // 3번 더 회전 = 원래 상태로 복귀
        int oldRows = originalBlock.shape.length; // 실제 배열 크기 사용
        originalBlock.rotate(); // 다시 현재 상태로
        
        // 기하학적 회전 공식 적용: (x,y) -> (oldRows-1-y, x)
        int newItemX = oldRows - 1 - oldItemY;
        int newItemY = oldItemX;
        
        setItemAtPosition(newItemX, newItemY, oldItemX, oldItemY);
    }
    
    /**
     * O블록의 회전 처리 (2x2 정사각형)
     */
    private void handleOBlockRotation(int oldItemX, int oldItemY) {
        // O블록은 2x2이므로 회전해도 상대적 위치가 유지됨
        // 하지만 실제로는 시계방향으로 위치가 바뀜: (0,0)->(0,1)->(1,1)->(1,0)
        int newItemX, newItemY;
        
        if (oldItemX == 0 && oldItemY == 0) {      // 좌상 -> 우상
            newItemX = 1; newItemY = 0;
        } else if (oldItemX == 1 && oldItemY == 0) { // 우상 -> 우하  
            newItemX = 1; newItemY = 1;
        } else if (oldItemX == 1 && oldItemY == 1) { // 우하 -> 좌하
            newItemX = 0; newItemY = 1;
        } else {                                   // 좌하 -> 좌상
            newItemX = 0; newItemY = 0;
        }
        
        setItemAtPosition(newItemX, newItemY, oldItemX, oldItemY);
    }
    
    /**
     * I블록의 회전 처리
     */
    private void handleIBlockRotation(int oldItemX, int oldItemY) {
        // I블록이 일반 블록의 회전 공식을 사용하므로 기하학적 회전 적용
        handleGeometricRotation(oldItemX, oldItemY);
    }
    
    /**
     * 아이템을 새로운 위치에 설정하고 로그를 출력합니다.
     */
    private void setItemAtPosition(int newItemX, int newItemY, int oldItemX, int oldItemY) {
        // 범위 체크
        if (newItemY >= 0 && newItemY < itemShape.length && 
            newItemX >= 0 && newItemX < itemShape[newItemY].length) {
            
            itemX = newItemX;
            itemY = newItemY;
            
            // itemShape 재초기화
            clearItemShape();
            itemShape[itemY][itemX] = 1;
            
            System.out.println("Item rotated from (" + oldItemX + ", " + oldItemY + ") to (" + itemX + ", " + itemY + ") [index: " + itemCellIndex + "]");
        } else {
            System.out.println("Warning: Item position out of bounds after rotation!");
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
     * 아이템 타입의 심볼을 반환합니다.
     * 이미지가 있는 아이템의 경우 빈 문자열을 반환합니다.
     * @return 아이템 심볼 문자열 (이미지가 있으면 빈 문자열)
     */
    public String getItemSymbol() {
        // 이미지가 있는 아이템들은 심볼을 사용하지 않음
        if (getItemImage() != null) {
            return ""; // 이미지가 있으면 심볼 없음
        }
        // 이미지가 없는 아이템(LINE_CLEAR)만 심볼 사용
        return itemType.getSymbol();
    }
    
    /**
     * 아이템 타입의 표시 이름을 반환합니다.
     * @return 아이템 표시 이름
     */
    public String getItemDisplayName() {
        return itemType.getDisplayName();
    }
}