package tetris.util;

import java.awt.*;
import java.io.File;

public final class Theme {
    private Theme(){}

    public static final Color BG             = new Color(0x00,0x00,0x00);
    public static final Color TITLE_YELLOW   = new Color(200, 200, 30); 
    public static final Color HEADER_RED = new Color(255, 60, 60);

    public static final Color LIGHT_GREY  = new Color(200,200,200);
    
    // Tetromino-ish
    public static final Color I_CYAN         = new Color(0x35,0xCF,0xF0);
    public static final Color O_YELLOW       = new Color(0xF2,0xD2,0x4A);
    public static final Color T_PURPLE       = new Color(0xA9,0x7B,0xE8);
    public static final Color S_GREEN        = new Color(0x43,0xD6,0x86);
    public static final Color Z_RED          = new Color(0xFF,0x5B,0x6B);
    public static final Color J_BLUE         = new Color(0x5A,0x8C,0xFF);
    public static final Color L_ORANGE       = new Color(0xFF,0xA5,0x57);


    


    public static final Color ROW_BG         = new Color(24, 24, 24);
    public static final Color ROW_HILIGHT    = new Color(58, 58, 58, 120);
    public static final Color ROW_SHADOW     = new Color(8, 8, 8, 160);

    public static final Color STAND_BLUE     = new Color(170, 190, 255);
    public static final Color TEXT_WHITE     = new Color(235, 235, 235);
    public static final Color TEXT_GRAY     = new Color(100,100,100);
    public static final Color TEXT_LIGHTGRAY     = new Color(150,150,150);
    public static final Color SCORE_WHITE    = new Color(245, 245, 245);

    public static final Color BADGE_YELLOW   = new Color(255, 223, 128);
    public static final Color DIVIDER        = new Color(36, 36, 36);

    public static final Font GIANTS_INLINE = Loader.loadFont("Giants-Inline.ttf");
    public static final Font GIANTS_BOLD = Loader.loadFont("Giants-Bold.ttf");
    public static final Font GIANTS_REGULAR = Loader.loadFont("Giants-Regular.ttf");
    

}
