package tetris.scene.game.blocks;

import java.awt.Color;
import tetris.ColorBlindHelper;
import tetris.GameSettings;

public class TBlock extends Block {
	
	private int rotationState = 0; // 0, 1, 2, 3 (0도, 90도, 180도, 270도)
	
	public TBlock() {
		// 초기 T블록 모양 (0도)
		shape = new int[][] { 
			{0, 1, 0},
			{1, 1, 1}
		};
		// ColorBlindHelper를 사용하여 색맹 모드에 따른 색상 설정
		int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
		color = ColorBlindHelper.getBlockColor(6, colorBlindMode); // T블록은 타입 6
	}
	
	// 색상을 동적으로 업데이트하는 메서드
	@Override
	public Color getColor() {
		int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
		return ColorBlindHelper.getBlockColor(6, colorBlindMode);
	}
	
	/**
	 * T블록의 중심점 기준 회전을 구현합니다.
	 * 중심점은 항상 (1,1) 위치의 블록입니다.
	 */
	@Override
	public void rotate() {
		rotationState = (rotationState + 1) % 4;
		
		switch (rotationState) {
			case 0: // 0도 - 기본 모양
				shape = new int[][] { 
					{0, 1, 0},
					{1, 1, 1}
				};
				break;
			case 1: // 90도 시계방향
				shape = new int[][] { 
					{0, 1, 0},
					{0, 1, 1},
					{0, 1, 0}
				};
				break;
			case 2: // 180도
				shape = new int[][] { 
					{0, 0, 0},
					{1, 1, 1},
					{0, 1, 0}
				};
				break;
			case 3: // 270도
				shape = new int[][] { 
					{0, 1, 0},
					{1, 1, 0},
					{0, 1, 0}
				};
				break;
		}
	}
	
	/**
	 * 현재 회전 상태를 반환합니다.
	 * @return 회전 상태 (0: 0도, 1: 90도, 2: 180도, 3: 270도)
	 */
	public int getRotationState() {
		return rotationState;
	}
}
