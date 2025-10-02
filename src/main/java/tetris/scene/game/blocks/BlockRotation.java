package tetris.scene.game.blocks;

/**
 * 블록 회전 기능을 담당하는 유틸리티 클래스
 * 블록의 회전 가능성 검사, 회전 상태 추정, 임시 회전 블록 생성 등의 기능을 제공합니다.
 */
public class BlockRotation {
    
    /**
     * 현재 블록이 주어진 위치에서 회전 가능한지 확인합니다.
     * 
     * @param curr 현재 블록
     * @param x 현재 블록의 x 위치
     * @param y 현재 블록의 y 위치
     * @param board 게임 보드 상태 배열
     * @param gameWidth 게임 보드 너비
     * @param gameHeight 게임 보드 높이
     * @return 회전 가능하면 true, 불가능하면 false
     */
    public static boolean canRotate(Block curr, int x, int y, int[][] board, int gameWidth, int gameHeight) {
        if (curr == null) return false;
        
        // 임시로 블록을 회전시켜서 충돌 검사
        Block tempBlock = createTempRotatedBlock(curr);
        if (tempBlock == null) return false;
        
        // 1. 경계 체크
        if (x + tempBlock.width() > gameWidth || y + tempBlock.height() > gameHeight) {
            return false;
        }
        
        // 2. 기존 고정된 블록들과의 충돌 체크
        for (int blockRow = 0; blockRow < tempBlock.height(); blockRow++) {
            for (int blockCol = 0; blockCol < tempBlock.width(); blockCol++) {
                if (tempBlock.getShape(blockCol, blockRow) == 1) {
                    int boardX = x + blockCol;
                    int boardY = y + blockRow;
                    
                    // 보드 범위 체크
                    if (boardX < 0 || boardX >= gameWidth || boardY < 0 || boardY >= gameHeight) {
                        return false;
                    }
                    
                    // 기존 블록과의 충돌 체크
                    if (board[boardY][boardX] == 1) {
                        System.out.println("Rotation blocked: collision at (" + boardX + ", " + boardY + ")");
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * 현재 블록을 임시로 회전시킨 복사본을 생성합니다.
     * 
     * @param curr 현재 블록
     * @return 회전된 블록의 복사본, 실패 시 null
     */
    public static Block createTempRotatedBlock(Block curr) {
        if (curr == null) return null;
        
        try {
            // 현재 블록의 복사본 생성
            Block tempBlock = null;
            
            // 블록 타입에 따라 새 인스턴스 생성
            if (curr instanceof IBlock) {
                tempBlock = new IBlock();
                // IBlock의 경우 현재 회전 상태를 직접 설정
                IBlock currentIBlock = (IBlock) curr;
                IBlock tempIBlock = (IBlock) tempBlock;
                int currentRotationState = currentIBlock.getRotationState();
                
                // 현재 상태로 맞춘 후 한 번 더 회전
                for (int i = 0; i < currentRotationState; i++) {
                    tempIBlock.rotate();
                }
                tempIBlock.rotate(); // 회전 후 상태 확인용
                
                return tempBlock;
            } else if (curr instanceof JBlock) {
                tempBlock = new JBlock();
            } else if (curr instanceof LBlock) {
                tempBlock = new LBlock();
            } else if (curr instanceof ZBlock) {
                tempBlock = new ZBlock();
            } else if (curr instanceof SBlock) {
                tempBlock = new SBlock();
            } else if (curr instanceof TBlock) {
                tempBlock = new TBlock();
            } else if (curr instanceof OBlock) {
                tempBlock = new OBlock();
            }
            
            if (tempBlock == null) return null;
            
            // 현재 블록과 같은 회전 상태로 맞추기 (IBlock이 아닌 경우)
            int currentRotations = getCurrentRotationCount(curr);
            for (int i = 0; i < currentRotations; i++) {
                tempBlock.rotate();
            }
            
            // 한 번 더 회전 (회전 후 상태 확인용)
            tempBlock.rotate();
            
            return tempBlock;
            
        } catch (Exception e) {
            System.err.println("Error creating temp rotated block: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 현재 블록의 회전 횟수를 추정합니다.
     * 
     * @param curr 현재 블록
     * @return 추정된 회전 횟수 (0: 0도, 1: 90도, 2: 180도, 3: 270도)
     */
    public static int getCurrentRotationCount(Block curr) {
        if (curr == null) return 0;
        
        // TBlock인 경우 직접 회전 상태를 반환
        if (curr instanceof TBlock) {
            return ((TBlock) curr).getRotationState();
        }
        
        // 다른 블록들은 기존 방식으로 추정
        Block originalBlock = createOriginalBlock(curr);
        
        if (originalBlock == null) return 0;
        
        // 현재 블록과 원본 블록의 크기를 비교하여 회전 상태 추정
        int currentWidth = curr.width();
        int currentHeight = curr.height();
        int originalWidth = originalBlock.width();
        int originalHeight = originalBlock.height();
        
        // 크기가 같으면 0도 또는 180도 회전
        if (currentWidth == originalWidth && currentHeight == originalHeight) {
            // 더 정확한 비교를 위해 실제 모양을 확인
            boolean sameShape = true;
            for (int i = 0; i < Math.min(currentWidth, originalWidth); i++) {
                for (int j = 0; j < Math.min(currentHeight, originalHeight); j++) {
                    if (curr.getShape(i, j) != originalBlock.getShape(i, j)) {
                        sameShape = false;
                        break;
                    }
                }
                if (!sameShape) break;
            }
            
            if (sameShape) {
                return 0; // 0도 회전
            } else {
                return 2; // 180도 회전
            }
        }
        // 크기가 바뀌었으면 90도 또는 270도 회전
        else if (currentWidth == originalHeight && currentHeight == originalWidth) {
            return 1; // 90도 회전 (또는 270도, 하지만 일단 1로 가정)
        }
        
        return 0; // 기본값
    }
    
    /**
     * 주어진 블록과 같은 타입의 원본 블록을 생성합니다.
     * 
     * @param block 기준 블록
     * @return 같은 타입의 새로운 블록 인스턴스
     */
    private static Block createOriginalBlock(Block block) {
        if (block instanceof IBlock) {
            return new IBlock();
        } else if (block instanceof JBlock) {
            return new JBlock();
        } else if (block instanceof LBlock) {
            return new LBlock();
        } else if (block instanceof ZBlock) {
            return new ZBlock();
        } else if (block instanceof SBlock) {
            return new SBlock();
        } else if (block instanceof TBlock) {
            return new TBlock();
        } else if (block instanceof OBlock) {
            return new OBlock();
        }
        
        return null;
    }
}