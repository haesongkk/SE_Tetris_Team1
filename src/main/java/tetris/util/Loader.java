package tetris.util;

import java.awt.Font;

public class Loader {

        public static Font loadFont(String path) {
            return loadFont(path, 16f, Font.BOLD);
        }
        
        public static Font loadFont(String path, float size, int style) {
            // path 예시: "/fonts/MyFont.ttf"  (src/main/resources/fonts/MyFont.ttf)
            String cp = path.startsWith("/") ? path.substring(1) : path;
        
            // 1) 클래스패스에서 먼저 시도
            try (java.io.InputStream in = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(cp)) {
                if (in != null) {
                    Font base = Font.createFont(Font.TRUETYPE_FONT, in);
                    tryRegisterFont(base); // 등록 시도(실패해도 무시)
                    return base.deriveFont(style, size);
                }
            } catch (java.awt.FontFormatException | java.io.IOException ignore) {
                // 폴백으로 진행
            }
        
            // 2) 파일 경로 폴백 (개발 중 절대/상대 경로 지원)
            try (java.io.InputStream in = java.nio.file.Files.newInputStream(java.nio.file.Paths.get(path))) {
                Font base = Font.createFont(Font.TRUETYPE_FONT, in);
                tryRegisterFont(base);
                return base.deriveFont(style, size);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Failed to load custom font. Fallback to Dialog.");
                return new Font("Dialog", style, Math.round(size));
            }
        }
        
        public static void tryRegisterFont(Font f) {
            try {
                java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(f);
            } catch (Throwable ignored) {
                // headless 등 환경에서 실패해도 렌더링에는 지장 없음
            }
        }
    
    
}
