package tetris.util;

import java.util.List;
import java.util.Objects;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class Loader {
    public static Path appDataFile(String relative) {
        // 사용자 홈 하위에 앱 전용 디렉터리
        Path dir = Paths.get(System.getProperty("user.home"), ".tetris");
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
        return dir.resolve(relative);
    }
    
    public static List<String> loadFile(String path) {
        List<String> lines = new ArrayList<>();
    
        // 0) 외부(쓰기 가능) 경로 우선
        Path external = appDataFile(path); // 예: "data/highscore.txt"
        if (Files.exists(external)) {
            try (BufferedReader br = Files.newBufferedReader(external, StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) lines.add(line);
                return lines;
            } catch (IOException e) {
                e.printStackTrace(); // 리소스 폴백 시도
            }
        }
    
        // 1) 클래스패스 폴백 (읽기 전용 기본값)
        String cp = path.startsWith("/") ? path.substring(1) : path;
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(cp);
        if (in != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) lines.add(line);
                // 외부 파일이 없었으면, 읽은 기본값을 외부 경로로 한 번 복사해 두면 좋음
                if (!Files.exists(external)) {
                    saveFile(path, lines); // 외부 경로에 저장
                }
                return lines;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        // 2) (선택) 개발 중 직접 지정한 경로도 지원하고 싶다면 마지막에 시도
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)) {
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
    
        // 항상 외부(쓰기 가능) 위치로 저장
        Path target = appDataFile(path); // 예: "~/.tetris/data/highscore.txt"
        try {
            Path parent = target.getParent();
            if (parent != null) Files.createDirectories(parent);
    
            // 같은 디렉터리에 임시 파일 생성 후 원자적 교체 시도
            Path tmp = Files.createTempFile(parent, "save-", ".tmp");
            try (BufferedWriter bw = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            }
    
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
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
