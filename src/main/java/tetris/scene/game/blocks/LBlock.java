package tetris.scene.game.blocks;

import tetris.ColorBlindHelper;
import tetris.GameSettings;
import java.awt.Color;

public class LBlock extends Block {
	
	public LBlock() {
		shape = new int[][] { 
			{1, 1, 1},
			{1, 0, 0}
		};
		// ColorBlindHelper를 사용하여 색맹 모드에 따른 색상 설정
		int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
		color = ColorBlindHelper.getBlockColor(1, colorBlindMode); // L블록은 타입 1
	}
	
	// 색상을 동적으로 업데이트하는 메서드
	@Override
	public Color getColor() {
		int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
		return ColorBlindHelper.getBlockColor(1, colorBlindMode);
	}
}
