package tetris.scene.game.blocks;

import java.awt.Color;

public abstract class Block {
		
	protected int[][] shape;//
	protected Color color;
	
	public Block() {
		shape = new int[][]{ 
				{1, 1}, 
				{1, 1}
		};
		color = Color.YELLOW;
	}
	
	public int getShape(int x, int y) {
        if (y >= 0 && y < shape.length && x >= 0 && x < shape[y].length) {
            return shape[y][x];
        }
        return 0; // 범위를 벗어나면 0 반환
    }
	
	public Color getColor() {
		return color;
	}
	
	public void rotate() {
		// Rotate the block 90 deg. clockwise.
    int rows = shape.length;
    int cols = shape[0].length;
    
    // 새로운 회전된 배열 생성 (행과 열이 바뀜)
    int[][] rotated = new int[cols][rows];
    
    // 시계방향 90도 회전 공식: rotated[j][rows-1-i] = original[i][j]
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            rotated[j][rows - 1 - i] = shape[i][j];
        }
    }
    
    // 기존 shape를 회전된 배열로 교체
    shape = rotated;	
	}
	
	public int height() {
		// 실제 블록이 있는 줄까지만 계산
		for (int row = shape.length - 1; row >= 0; row--) {
			for (int col = 0; col < shape[row].length; col++) {
				if (shape[row][col] == 1) {
					return row + 1;
				}
			}
		}
		return shape.length; // 모든 줄이 비어있으면 원래 크기 반환
	}
	
	public int width() {
		if(shape.length > 0) {
			// 실제 블록이 있는 열까지만 계산
			int maxWidth = 0;
			for (int row = 0; row < shape.length; row++) {
				for (int col = shape[row].length - 1; col >= 0; col--) {
					if (shape[row][col] == 1) {
						maxWidth = Math.max(maxWidth, col + 1);
						break;
					}
				}
			}
			return maxWidth > 0 ? maxWidth : shape[0].length;
		}
		return 0;
	}
	
	/**
	 * 현재 위치에서 회전 가능한지 확인합니다.
	 * 
	 * @param x 현재 블록의 x 위치
	 * @param y 현재 블록의 y 위치
	 * @param board 게임 보드 상태 배열
	 * @param gameWidth 게임 보드 너비
	 * @param gameHeight 게임 보드 높이
	 * @return 회전 가능하면 true, 불가능하면 false
	 */
	public boolean canRotate(int x, int y, int[][] board, int gameWidth, int gameHeight) {
		// 임시로 회전해서 검사
		rotate();
		boolean canRotate = !hasCollision(x, y, board, gameWidth, gameHeight);
		rotateBack(); // 원상복구
		return canRotate;
	}
	
	/**
	 * 충돌 검사를 수행합니다.
	 */
	private boolean hasCollision(int x, int y, int[][] board, int gameWidth, int gameHeight) {
		// 1. 경계 체크
		if (x + width() > gameWidth || y + height() > gameHeight) {
			return true;
		}
		
		// 2. 기존 고정된 블록들과의 충돌 체크
		for (int blockRow = 0; blockRow < height(); blockRow++) {
			for (int blockCol = 0; blockCol < width(); blockCol++) {
				if (getShape(blockCol, blockRow) == 1) {
					int boardX = x + blockCol;
					int boardY = y + blockRow;
					
					// 보드 범위 체크
					if (boardX < 0 || boardX >= gameWidth || boardY < 0 || boardY >= gameHeight) {
						return true;
					}
					
					// 기존 블록과의 충돌 체크
					if (board[boardY][boardX] == 1) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * 회전을 원상복구합니다 (3번 더 회전하면 360도 = 원래 상태)
	 */
	private void rotateBack() {
		for (int i = 0; i < 3; i++) {
			rotate();
		}
	}
}