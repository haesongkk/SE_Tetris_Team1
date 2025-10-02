package tetris.util;

import java.util.List;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Loader {
    public static List<String> loadFile(String path) {
        List<String> lines = new ArrayList<>();
    
        // 1) 클래스패스에서 먼저 시도 (src/main/resources 기준)
        //    예: path="/data/highscore.txt" 또는 "data/highscore.txt"
        String cp = path.startsWith("/") ? path.substring(1) : path;
        InputStream in = Thread.currentThread()
                               .getContextClassLoader()
                               .getResourceAsStream(cp);
    
        if (in != null) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) lines.add(line);
                return lines; // 성공했으면 반환
            } catch (IOException e) {
                e.printStackTrace();
                // 계속해서 파일 경로 폴백 시도
            }
        }
                // 2) 파일 경로로 폴백 (개발 중 상대/절대 경로 지원)
                try (BufferedReader br = java.nio.file.Files.newBufferedReader(
                    java.nio.file.Paths.get(path), StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) lines.add(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return lines;
        }

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
