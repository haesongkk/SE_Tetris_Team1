package tetris.scene.game.blocks;

import java.awt.Color;
import tetris.ColorBlindHelper;
import tetris.GameSettings;

public class TBlock extends Block {
	
	public TBlock() {
		// T블록 초기 모양
		shape = new int[][] { 
			{0, 1, 0},
			{1, 1, 1},
			{0, 0, 0}
		};
		type = 6; // T 블록 타입
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
}
