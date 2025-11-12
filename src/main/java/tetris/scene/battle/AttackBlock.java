package tetris.scene.battle;

import java.awt.Color;

/**
 * 배틀모드에서 상대방에게 전송되는 공격(방해) 블록을 나타내는 클래스
 * 
 * 플레이어가 2줄 이상 삭제할 때 생성되며, 각 줄에서 가장 마지막에 들어온 블록을 제외한
 * 나머지 블록들의 패턴을 저장합니다.
 */
public class AttackBlock {
    private final int width;           // 보드 너비 (일반적으로 10)
    private final boolean[] pattern;   // 블록 패턴 (true = 블록 있음, false = 빈 공간)
    private final Color[] colors;      // 각 셀의 색상
    private final int[] blockTypes;    // 각 셀의 블록 타입 (색맹 모드 패턴용)
    
    /**
     * AttackBlock 생성자
     * 
     * @param width 보드 너비
     * @param pattern 블록 패턴 (true = 블록 있음, false = 빈 공간)
     * @param colors 각 셀의 색상
     * @param blockTypes 각 셀의 블록 타입
     */
    public AttackBlock(int width, boolean[] pattern, Color[] colors, int[] blockTypes) {
        this.width = width;
        this.pattern = pattern.clone();
        this.colors = colors.clone();
        this.blockTypes = blockTypes.clone();
    }
    
    /**
     * 보드 너비를 반환합니다.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * 지정된 위치에 블록이 있는지 확인합니다.
     * 
     * @param col 열 위치
     * @return 블록이 있으면 true, 없으면 false
     */
    public boolean hasBlockAt(int col) {
        if (col >= 0 && col < width) {
            return pattern[col];
        }
        return false;
    }
    
    /**
     * 지정된 위치의 블록 색상을 반환합니다.
     * 
     * @param col 열 위치
     * @return 해당 위치의 색상, 블록이 없으면 null
     */
    public Color getColorAt(int col) {
        if (col >= 0 && col < width && pattern[col]) {
            return colors[col];
        }
        return null;
    }
    
    /**
     * 지정된 위치의 블록 타입을 반환합니다.
     * 
     * @param col 열 위치
     * @return 해당 위치의 블록 타입, 블록이 없으면 -1
     */
    public int getBlockTypeAt(int col) {
        if (col >= 0 && col < width && pattern[col]) {
            return blockTypes[col];
        }
        return -1;
    }
    
    /**
     * 이 공격 블록이 가진 실제 블록 수를 반환합니다.
     */
    public int getBlockCount() {
        int count = 0;
        for (boolean hasBlock : pattern) {
            if (hasBlock) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 블록 패턴의 복사본을 반환합니다.
     */
    public boolean[] getPattern() {
        return pattern.clone();
    }
    
    /**
     * 색상 배열의 복사본을 반환합니다.
     */
    public Color[] getColors() {
        return colors.clone();
    }
    
    /**
     * 블록 타입 배열의 복사본을 반환합니다.
     */
    public int[] getBlockTypes() {
        return blockTypes.clone();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AttackBlock[width=").append(width).append(", blocks=");
        for (int i = 0; i < width; i++) {
            sb.append(pattern[i] ? "■" : "□");
        }
        sb.append("]");
        return sb.toString();
    }
}