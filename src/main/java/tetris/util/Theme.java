package tetris.util;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

import tetris.ColorBlindHelper;
import tetris.GameSettings;

public final class Theme {
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


        if(colorBlindMode == 0) return getCustomeBlock(blockId);
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
        int[] screenSize = GameSettings.getInstance().getResolutionSize();
        return GIANTS_INLINE.deriveFont(sizeRatioF * screenSize[0]);
    }

    public static Font GIANTS_BOLD(float sizeRatio) {
        float sizeRatioF = (float)sizeRatio / 100.0f;
        int[] screenSize = GameSettings.getInstance().getResolutionSize();
        return GIANTS_BOLD.deriveFont(sizeRatioF * screenSize[0]);
    }

    public static Font GIANTS_REGULAR(float sizeRatio) {
        float sizeRatioF = (float)sizeRatio / 100.0f;
        int[] screenSize = GameSettings.getInstance().getResolutionSize();
        return GIANTS_REGULAR.deriveFont(sizeRatioF * screenSize[0]);
    }


    public static Font getFont(Font font, float sizeRatio) {
        int[] screenSize = GameSettings.getInstance().getResolutionSize();
        return font.deriveFont(sizeRatio * screenSize[0]);
    }

    public static int getPixelWidth(float sizeRatio) {
        int[] screenSize = GameSettings.getInstance().getResolutionSize();
        return (int)(sizeRatio * screenSize[0]);
    }
    public static int getPixelHeight(float sizeRatio) {
        int[] screenSize = GameSettings.getInstance().getResolutionSize();
        return (int)(sizeRatio * screenSize[1]);
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
