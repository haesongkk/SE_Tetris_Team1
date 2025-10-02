package tetris.util;

import java.util.List;
import java.util.Objects;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Loader {
    public static List<String> loadFile(String path) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static void saveFile(String path, List<String> lines) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(lines, "lines");
    
        try {
            java.io.File target = new java.io.File(path);
            java.io.File parent = target.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs(); // 부모 디렉터리 없으면 생성
            }
    
            try (java.io.BufferedWriter bw = new java.io.BufferedWriter(
                    new java.io.OutputStreamWriter(
                            new java.io.FileOutputStream(target),
                            java.nio.charset.StandardCharsets.UTF_8))) {
    
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
