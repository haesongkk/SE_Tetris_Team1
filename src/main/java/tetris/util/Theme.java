package tetris.util;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

import tetris.ColorBlindHelper;
import tetris.GameSettings;

import javax.swing.JFrame;

public final class Theme {
    private static JFrame currentFrame = null;
    
    public static void setCurrentFrame(JFrame frame) {
        currentFrame = frame;
    }
    public enum ColorType {
        RED, ORANGE, YELLOW, GREEN, CYAN, BLUE, PURPLE
    }
    private Theme(){}

    public static Color BG() {
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        if(colorBlindMode == 0) return new Color(0x00,0x00,0x00);
        return ColorBlindHelper.getBackgroundColor(colorBlindMode);
    }

    public static Color Border() {
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        return ColorBlindHelper.getBorderColor(colorBlindMode);
    }

    // 메뉴용 색상 메서드들 (색맹 모드에 따라 변경)
    public static Color MenuBG() {
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        if(colorBlindMode == 0) return new Color(20, 20, 40);  // 일반 모드 - 원래 색상
        return ColorBlindHelper.getBackgroundColor(colorBlindMode);
    }
    
    public static Color MenuTitle() {
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        if(colorBlindMode == 0) return new Color(255, 255, 100);  // 일반 모드 - 원래 색상
        return Block(ColorType.YELLOW);
    }
    
    public static Color MenuButton() {
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        if(colorBlindMode == 0) return new Color(70, 70, 120);  // 일반 모드 - 원래 색상
        return Border();
    }

    public static Color MenuButton(boolean hover) {
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        if(hover) {
            switch (colorBlindMode) {
                case 0:
                    return new Color(120, 120, 200);
                case 1:
                    return new Color(150, 100, 255);
                default:
                    return new Color(255, 150, 80);
            }
        } 
        else {
            if(colorBlindMode == 0) 
                return new Color(70, 70, 120);  
            else return Border();
        }
    }
    
    public static Color MenuPanel() {
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        if(colorBlindMode == 0) return new Color(40, 40, 70);  // 일반 모드 - 원래 색상
        // 색약 모드에서는 배경색보다 조금 밝은 색상 사용
        Color bg = ColorBlindHelper.getBackgroundColor(colorBlindMode);
        return new Color(
            Math.min(255, bg.getRed() + 30),
            Math.min(255, bg.getGreen() + 30),
            Math.min(255, bg.getBlue() + 30)
        );
    }

    public static Color getCustomeBlock(int i) {
        if(i==0) return new Color(0xFF,0x5B,0x6B);
        else if(i==1) return new Color(0xFF,0xA5,0x57);
        else if(i==2) return new Color(255, 225, 0); 
        else if(i==3) return new Color(0x43,0xD6,0x86);
        else if(i==4) return new Color(0x35,0xCF,0xF0);
        else if(i==5) return new Color(0x5A,0x8C,0xFF);
        else if(i==6) return new Color(0xA9,0x7B,0xE8);
        else return new Color(0xFF,0x5B,0x6B);
    }

    public static Color Block(char blockType) {
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        int blockId;
        if(blockType == 'Z') blockId = 0;
        else if(blockType == 'L') blockId = 1;
        else if(blockType == 'O') blockId = 2;
        else if(blockType == 'S') blockId = 3;
        else if(blockType == 'I') blockId = 4;
        else if(blockType == 'J') blockId = 5;
        else if(blockType == 'T') blockId = 6;
        else blockId = 0;

        return ColorBlindHelper.getBlockColor(blockId, colorBlindMode);
    }

    public static Color Block(ColorType blockType) {
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        int blockId;
        switch(blockType) {
            case RED:       blockId = 0; break;
            case ORANGE:    blockId = 1; break;
            case YELLOW:    blockId = 2; break;
            case GREEN:     blockId = 3; break;
            case CYAN:      blockId = 4; break;
            case BLUE:      blockId = 5; break;
            case PURPLE:    blockId = 6; break;
            default:        blockId = 0; break;
        }
        if(colorBlindMode == 0) return getCustomeBlock(blockId);
        return ColorBlindHelper.getBlockColor(blockId, colorBlindMode);
    }


    public static final Color LIGHT_GRAY  = Color.LIGHT_GRAY;
    public static final Color DARK_GRAY   = Color.DARK_GRAY;
    public static final Color WHITE   = Color.WHITE;
    public static final Color BLACK   = Color.BLACK;
    public static final Color GRAY   = Color.GRAY;


    public static final Font GIANTS_INLINE = loadFont("Giants-Inline.ttf");
    public static final Font GIANTS_BOLD = loadFont("Giants-Bold.ttf");
    public static final Font GIANTS_REGULAR = loadFont("Giants-Regular.ttf");

    public static Font GIANTS_INLINE(float sizeRatio) {
        float sizeRatioF = (float)sizeRatio / 100.0f;
        int[] screenSize = getActualScreenSize();
        return GIANTS_INLINE.deriveFont(sizeRatioF * screenSize[0]);
    }

    public static Font GIANTS_BOLD(float sizeRatio) {
        float sizeRatioF = (float)sizeRatio / 100.0f;
        int[] screenSize = getActualScreenSize();
        return GIANTS_BOLD.deriveFont(sizeRatioF * screenSize[0]);
    }

    public static Font GIANTS_REGULAR(float sizeRatio) {
        float sizeRatioF = (float)sizeRatio / 100.0f;
        int[] screenSize = getActualScreenSize();
        return GIANTS_REGULAR.deriveFont(sizeRatioF * screenSize[0]);
    }

    public static int getPixelWidth(float sizeRatio) {
        int[] screenSize = getActualScreenSize();
        return (int)(sizeRatio * screenSize[0]);
    }
    
    public static int getPixelHeight(float sizeRatio) {
        int[] screenSize = getActualScreenSize();
        return (int)(sizeRatio * screenSize[1]);
    }
    
    // 실제 창 크기 또는 설정된 해상도를 반환하는 메서드
    private static int[] getActualScreenSize() {
        if (currentFrame != null) {
            Dimension size = currentFrame.getSize();
            return new int[]{size.width, size.height};
        }
        // 폴백: GameSettings의 해상도 사용
        return GameSettings.getInstance().getResolutionSize();
    }

    static Font loadFont(String path) {
        try (InputStream in = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(path)) {
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FontFormatException | IOException ex) {
            System.out.println("Failed to load custom font:"+ path);
            return new Font("Dialog", Font.BOLD, 16);
        }
    }

}
