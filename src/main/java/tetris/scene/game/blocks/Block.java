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
		return shape.length;
	}
	
	public int width() {
		if(shape.length > 0)
			return shape[0].length;
		return 0;
	}
}