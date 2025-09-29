package tetris.scene.game.blocks;

import tetris.ColorBlindHelper;
import tetris.GameSettings;
import java.awt.Color;

public class IBlock extends Block {
	
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
	
}
