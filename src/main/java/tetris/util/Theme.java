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
        return ColorBlindHelper.getBackgroundColor(colorBlindMode);
    }

    public static Color Border() {
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        return ColorBlindHelper.getBorderColor(colorBlindMode);
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
            ex.printStackTrace();
            System.out.println("Failed to load custom font.");
            return new Font("Dialog", Font.BOLD, 16);
        }
    }

}
