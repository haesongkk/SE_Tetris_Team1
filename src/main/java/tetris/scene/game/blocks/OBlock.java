package tetris.scene.game.blocks;

import tetris.ColorBlindHelper;
import tetris.GameSettings;
import java.awt.Color;

public class OBlock extends Block {

	public OBlock() {
		shape = new int[][] { 
			{1, 1}, 
			{1, 1}
		};
		// ColorBlindHelper를 사용하여 색맹 모드에 따른 색상 설정
		int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
		color = ColorBlindHelper.getBlockColor(2, colorBlindMode); // O블록은 타입 2
	}
	
	// 색상을 동적으로 업데이트하는 메서드
	@Override
	public Color getColor() {
		int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
		return ColorBlindHelper.getBlockColor(2, colorBlindMode);
	}
	
	@Override
	public void rotate() {
		// O-블록도 실제로 회전시킴 (폭탄 위치가 달라져야 하므로)
		super.rotate();
		System.out.println("O-Block rotated!");
	}
}
