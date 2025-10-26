package tetris.scene.game.blocks;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * 무게추 아이템 블록 클래스
 * 너비 4칸의 사다리꼴 모양으로, 바닥이나 블록에 닿으면 자동으로 아래로 떨어지면서
 * 무게추 밑의 모든 블록을 제거하는 아이템입니다.
 */
public class WeightItemBlock extends Block {
    private static BufferedImage weightImage; // 무게추 이미지
    private boolean isActivated = false; // 무게추가 활성화되었는지 여부
    private boolean canMoveHorizontally = true; // 좌우 이동 가능 여부
    private boolean isDestroying = false; // 무게추가 파괴 중인지 여부
    private boolean shouldDisappear = false; // 무게추가 사라져야 하는지 여부
    private int destroyTimer = 0; // 파괴 타이머 (점멸 효과용)
    private boolean isBlinking = false; // 점멸 상태
    private int fallTimer = 0; // 자동 낙하 타이머
    private static final int FALL_INTERVAL = 5; // 5프레임마다 한 칸씩 낙하 (더 빠르게 테스트)
    
    // 무게추 이미지 로드
    static {
        try {
            System.out.println("Attempting to load weight image from /weight.png");
            java.io.InputStream stream = WeightItemBlock.class.getResourceAsStream("/weight.png");
            if (stream != null) {
                weightImage = ImageIO.read(stream);
                System.out.println("✅ Weight image loaded successfully!");
            } else {
                System.out.println("❌ Weight image stream is null");
                weightImage = null;
            }
        } catch (IOException e) {
            System.out.println("❌ Weight image file error: " + e.getMessage());
            weightImage = null;
        } catch (Exception e) {
            System.out.println("❌ Unexpected error loading weight image: " + e.getMessage());
            weightImage = null;
        }
    }
    
    public WeightItemBlock() {
        // 사다리꼴 모양 정의 (너비 4칸)
        // 윗줄: 2칸, 아랫줄: 4칸
        shape = new int[][]{
            {0, 1, 1, 0}, // 윗줄 (중간 2칸)
            {1, 1, 1, 1}  // 아랫줄 (전체 4칸)
        };
        
        // 무게추 색상 (회색)
        color = Color.GRAY;
        
        System.out.println("WeightItemBlock created with trapezoid shape");
    }
    
    /**
     * 무게추가 바닥이나 블록에 닿았을 때 호출되는 메서드
     */
    public void activate() {
        if (!isActivated) {
            isActivated = true;
            canMoveHorizontally = false;
            System.out.println("WeightItemBlock activated! No more horizontal movement allowed.");
        }
    }
    
    /**
     * 무게추를 파괴 모드로 설정 (바닥에 닿았을 때)
     */
    public void startDestroying() {
        if (!isDestroying) {
            isDestroying = true;
            destroyTimer = 0;
            System.out.println("WeightItemBlock started destroying (blinking)!");
        }
    }
    
    /**
     * 파괴 타이머를 업데이트하고 점멸 효과를 처리
     * @return true if 무게추가 완전히 사라져야 함
     */
    public boolean updateDestroy() {
        if (!isDestroying) return false;
        
        destroyTimer++;
        
        // 점멸 효과 (5프레임마다 토글 - 더 빠른 점멸)
        if (destroyTimer % 5 == 0) {
            isBlinking = !isBlinking;
        }
        
        // 30프레임 후 사라짐 (약 0.5초 - 더 빠르게)
        if (destroyTimer >= 30) {
            shouldDisappear = true;
            System.out.println("WeightItemBlock disappeared after blinking!");
            return true;
        }
        
        return false;
    }
    
    /**
     * 무게추가 활성화되었는지 확인
     */
    public boolean isActivated() {
        return isActivated;
    }
    
    /**
     * 좌우 이동이 가능한지 확인
     */
    public boolean canMoveHorizontally() {
        return canMoveHorizontally;
    }
    
    /**
     * 무게추가 파괴 중인지 확인
     */
    public boolean isDestroying() {
        return isDestroying;
    }
    
    /**
     * 무게추가 사라져야 하는지 확인
     */
    public boolean shouldDisappear() {
        return shouldDisappear;
    }
    
    /**
     * 현재 점멸 중인지 확인
     */
    public boolean isBlinking() {
        return isBlinking;
    }
    
    /**
     * 무게추의 자동 낙하를 업데이트합니다.
     * @return true if 한 칸 아래로 떨어져야 함
     */
    public boolean updateFall() {
        if (!isActivated || isDestroying) {
            return false;
        }
        
        fallTimer++;
        System.out.println("WeightItemBlock updateFall() called: fallTimer=" + fallTimer + "/" + FALL_INTERVAL);
        
        if (fallTimer >= FALL_INTERVAL) {
            fallTimer = 0;
            System.out.println("WeightItemBlock ready to fall one step down");
            return true; // 한 칸 아래로 떨어져야 함
        }
        
        return false;
    }
    
    /**
     * 낙하 타이머를 리셋합니다.
     */
    public void resetFallTimer() {
        fallTimer = 0;
    }
    
    /**
     * 무게추 셀을 화면에 그립니다.
     * @param g2d Graphics2D 객체
     * @param x 그릴 x 위치
     * @param y 그릴 y 위치
     * @param cellSize 셀 크기
     */
    public void drawWeightCell(Graphics2D g2d, int x, int y, int cellSize) {
        try {
            // 점멸 중이고 현재 점멸 상태라면 그리지 않음
            if (isDestroying && isBlinking) {
                return;
            }
            
            if (weightImage != null) {
                // 무게추 이미지가 있으면 이미지를 그립니다
                g2d.drawImage(weightImage, x, y, cellSize, cellSize, null);
            } else {
                // 무게추 이미지가 없으면 회색 사다리꼴로 그립니다
                drawTrapezoidShape(g2d, x, y, cellSize);
            }
            
            // 활성화된 상태면 빨간 테두리 추가
            if (isActivated) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(x, y, cellSize, cellSize);
            }
            
            // 파괴 중이면 특별한 효과 추가 (테두리 깜빡임)
            if (isDestroying && !isBlinking) {
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(4));
                g2d.drawRect(x - 1, y - 1, cellSize + 2, cellSize + 2);
            }
            
        } catch (Exception e) {
            // 에러가 발생하면 기본 회색 사각형으로 그립니다
            if (!isDestroying || !isBlinking) {
                g2d.setColor(Color.GRAY);
                g2d.fillRect(x, y, cellSize, cellSize);
                g2d.setColor(Color.WHITE);
                g2d.drawString("⚖", x + cellSize/4, y + cellSize*3/4);
            }
        }
    }
    
    /**
     * 사다리꼴 모양을 그립니다.
     */
    private void drawTrapezoidShape(Graphics2D g2d, int x, int y, int cellSize) {
        // 무게추의 메탈릭한 느낌을 위한 그라데이션
        GradientPaint gradient = new GradientPaint(
            x, y, Color.LIGHT_GRAY,
            x, y + cellSize, Color.DARK_GRAY
        );
        g2d.setPaint(gradient);
        g2d.fillRect(x, y, cellSize, cellSize);
        
        // 무게추 테두리
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, cellSize, cellSize);
        
        // 무게추 무늬 (가로선들)
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        for (int i = 1; i < 4; i++) {
            int lineY = y + (cellSize * i / 4);
            g2d.drawLine(x + 2, lineY, x + cellSize - 2, lineY);
        }
    }
    
    /**
     * 무게추 블록은 회전할 수 없습니다.
     */
    public void rotate() {
        // 무게추는 회전하지 않음 (Override 제거)
        System.out.println("WeightItemBlock cannot be rotated!");
    }
    
    /**
     * 무게추 블록은 회전할 수 없습니다.
     */
    public boolean canRotate(int[][] board, int x, int y) {
        return false; // 무게추는 회전 불가
    }
    
    /**
     * 이 블록이 아이템 블록인지 확인합니다.
     */
    public boolean hasItem() {
        return true;
    }
    
    /**
     * 무게추 블록인지 확인합니다.
     */
    public boolean isWeightBlock() {
        return true;
    }
    
    /**
     * 무게추의 색상을 반환합니다.
     */
    public Color getWeightColor() {
        return color;
    }
    
    /**
     * 무게추가 지정된 열의 모든 블록을 제거해야 하는 범위를 반환합니다.
     * @param boardX 무게추의 보드상 X 위치
     * @param boardY 무게추의 보드상 Y 위치
     * @return 제거할 열들의 배열 [시작열, 끝열]
     */
    public int[] getDestructionRange(int boardX, int boardY) {
        // 무게추는 4칸 너비이므로 현재 위치부터 +3칸까지 모든 열을 제거
        return new int[]{boardX, boardX + 3};
    }
    
    /**
     * 무게추 아래의 모든 블록을 제거합니다.
     * @param board 게임 보드
     * @param boardColors 게임 보드 색상
     * @param weightX 무게추의 X 위치
     * @param weightY 무게추의 Y 위치
     * @param scoreManager 점수 관리자 (셀 제거 시 점수 추가용)
     * @param boardManager 보드 매니저 (아이템 셀 정보 삭제용)
     * @return 제거된 블록의 개수
     */
    public int clearBlocksBelow(int[][] board, Color[][] boardColors, int weightX, int weightY, 
                               tetris.scene.game.core.ScoreManager scoreManager, 
                               tetris.scene.game.core.BoardManager boardManager) {
        int clearedCount = 0;
        int[] range = getDestructionRange(weightX, weightY);
        int startCol = Math.max(0, range[0]);
        int endCol = Math.min(board[0].length - 1, range[1]);
        
        System.out.println("WeightItemBlock clearing blocks in columns " + startCol + " to " + endCol + 
                          " below position (" + weightX + ", " + weightY + ")");
        
        // 무게추가 차지하는 두 줄의 블록들을 모두 제거 (무게추가 지나간 자리)
        for (int row = weightY; row <= weightY + 1; row++) {
            if (row < board.length) {
                // 해당 행의 무게추 범위 열들에서 블록 제거
                for (int col = startCol; col <= endCol; col++) {
                    if (board[row][col] == 1) {
                        board[row][col] = 0;
                        boardColors[row][col] = null;
                        clearedCount++;
                        
                        // 🔧 아이템 셀 정보도 함께 삭제 (버그 수정)
                        if (boardManager != null) {
                            boardManager.clearItemBlockInfo(col, row);
                            System.out.println("Cleared item info at (" + col + ", " + row + ")");
                        }
                        
                        // 셀을 지울 때마다 50점 추가 (난이도 배율 적용)
                        if (scoreManager != null) {
                            scoreManager.addWeightItemCellScore();
                        }
                    }
                }
                System.out.println("WeightItemBlock cleared blocks from row " + row);
            }
        }
        
        // 무게추는 블록을 파괴하므로 중력 효과를 적용하지 않음
        // (고정된 블록들은 그 자리에 그대로 남아있어야 함)
        
        System.out.println("WeightItemBlock cleared " + clearedCount + " blocks");
        return clearedCount;
    }
    
    /**
     * 무게추의 고스트 블록 위치를 계산합니다.
     * 무게추가 활성화되지 않은 상태에서만 고스트 블록을 표시합니다.
     * 
     * @param board 게임 보드
     * @param currentX 현재 X 위치
     * @param currentY 현재 Y 위치
     * @return 고스트 블록의 Y 위치 (활성화된 상태면 -1 반환)
     */
    public int calculateGhostY(int[][] board, int currentX, int currentY) {
        // 이미 활성화된 무게추는 고스트 블록을 표시하지 않음
        if (isActivated) {
            System.out.println("WeightItemBlock: Ghost disabled (activated)");
            return -1;
        }
        
        System.out.println("WeightItemBlock: Calculating ghost position from (" + currentX + ", " + currentY + ")");
        
        // 무게추가 떨어질 수 있는 최대 Y 위치 계산
        int ghostY = currentY;
        
        // 한 칸씩 아래로 내려가면서 충돌 검사
        while (ghostY + 1 < board.length) {
            // 무게추의 다음 위치에서 충돌 검사
            boolean collision = false;
            
            // 무게추 shape의 각 블록에 대해 충돌 검사
            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[row].length; col++) {
                    if (shape[row][col] == 1) {
                        int checkX = currentX + col;
                        int checkY = ghostY + 1 + row; // 한 칸 아래 위치
                        
                        // 보드 경계 검사
                        if (checkX < 0 || checkX >= board[0].length || 
                            checkY >= board.length) {
                            collision = true;
                            break;
                        }
                        
                        // 기존 블록과의 충돌 검사
                        if (checkY >= 0 && board[checkY][checkX] == 1) {
                            collision = true;
                            break;
                        }
                    }
                }
                if (collision) break;
            }
            
            if (collision) {
                break; // 충돌하면 현재 위치가 최종 위치
            }
            
            ghostY++; // 한 칸 더 아래로
        }
        
        System.out.println("WeightItemBlock: Ghost position calculated as y=" + ghostY);
        return ghostY;
    }
}