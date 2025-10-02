package tetris.scene.game.blocks;

import java.awt.Color;
import tetris.ColorBlindHelper;
import tetris.GameSettings;

public class IBlock extends Block {
	
	private int rotationState = 0; // 0: 가로, 1: 세로(오른쪽), 2: 가로, 3: 세로(오른쪽)
	
	public IBlock() {
		shape = new int[][] { 
			{1, 1, 1, 1}
		};
		// ColorBlindHelper를 사용하여 색맹 모드에 따른 색상 설정
		int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
		color = ColorBlindHelper.getBlockColor(4, colorBlindMode); // I블록은 타입 4
	}
	
	// 색상을 동적으로 업데이트하는 메서드
	@Override
	public Color getColor() {
		int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
		return ColorBlindHelper.getBlockColor(4, colorBlindMode);
	}
	
	@Override
	public void rotate() {
		rotationState = (rotationState + 1) % 4;
		
		switch (rotationState) {
			case 0: // 가로
			case 2: // 가로 (180도 회전)
				shape = new int[][] {
					{1, 1, 1, 1}
				};
				break;
			case 1: // 세로 (한 칸 오른쪽)
			case 3: // 세로 (한 칸 오른쪽, 180도 회전)
				shape = new int[][] {
					{0, 1},
					{0, 1},
					{0, 1},
					{0, 1}
				};
				break;
		}
	}
	
	/**
	 * 현재 회전 상태를 반환합니다.
	 * @return 회전 상태 (0~3)
	 */
	public int getRotationState() {
		return rotationState;
	}
	
}
