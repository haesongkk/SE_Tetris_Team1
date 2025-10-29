package tetris.util;

import java.io.*;
import java.nio.file.*;

/**
 * 게임 데이터 파일의 경로를 관리하는 유틸리티 클래스
 * 
 * JAR/네이티브 실행 파일에서도 동작하도록 사용자 홈 디렉토리에 데이터를 저장합니다.
 * - Windows: %APPDATA%\Tetris Game\
 * - macOS: ~/Library/Application Support/Tetris Game/
 * - Linux: ~/.config/Tetris Game/
 */
public class DataPathManager {
    
    private static final String APP_NAME = "Tetris Game";
    private static final String DATA_DIR_NAME = "data";
    
    // 싱글톤 인스턴스
    private static DataPathManager instance;
    
    private final Path dataDirectory;
    
    private DataPathManager() {
        this.dataDirectory = initializeDataDirectory();
        ensureDataDirectoryExists();
        copyDefaultFilesIfNeeded();
    }
    
    /**
     * 싱글톤 인스턴스 반환
     */
    public static synchronized DataPathManager getInstance() {
        if (instance == null) {
            instance = new DataPathManager();
        }
        return instance;
    }
    
    /**
     * OS별 데이터 디렉토리 경로 결정
     */
    private Path initializeDataDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        
        Path baseDir;
        
        if (os.contains("win")) {
            // Windows: %APPDATA%\Tetris Game\data
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                baseDir = Paths.get(appData, APP_NAME);
            } else {
                baseDir = Paths.get(userHome, "AppData", "Roaming", APP_NAME);
            }
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/Tetris Game/data
            baseDir = Paths.get(userHome, "Library", "Application Support", APP_NAME);
        } else {
            // Linux/Unix: ~/.config/Tetris Game/data
            String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
            if (xdgConfigHome != null) {
                baseDir = Paths.get(xdgConfigHome, APP_NAME);
            } else {
                baseDir = Paths.get(userHome, ".config", APP_NAME);
            }
        }
        
        return baseDir.resolve(DATA_DIR_NAME);
    }
    
    /**
     * 데이터 디렉토리가 존재하지 않으면 생성
     */
    private void ensureDataDirectoryExists() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
                System.out.println("데이터 디렉토리 생성: " + dataDirectory.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("데이터 디렉토리 생성 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 기본 데이터 파일을 리소스에서 복사 (최초 실행 시)
     */
    private void copyDefaultFilesIfNeeded() {
        // settings.txt 복사
        copyDefaultFile("defaults/settings.txt", "settings.txt");
        
        // highscore 파일들 (빈 파일로 생성)
        createEmptyFileIfNotExists("highscore.txt");
        createEmptyFileIfNotExists("highscore_v2.txt");
    }
    
    /**
     * 리소스에서 기본 파일 복사
     */
    private void copyDefaultFile(String resourcePath, String targetFileName) {
        Path targetFile = dataDirectory.resolve(targetFileName);
        
        // 파일이 이미 존재하면 복사하지 않음 (사용자 데이터 보존)
        if (Files.exists(targetFile)) {
            return;
        }
        
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in != null) {
                Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("기본 파일 복사: " + targetFileName);
            } else {
                System.out.println("리소스 파일 없음 (무시): " + resourcePath);
                // 리소스가 없어도 빈 파일 생성
                createEmptyFileIfNotExists(targetFileName);
            }
        } catch (IOException e) {
            System.err.println("기본 파일 복사 실패: " + resourcePath);
            e.printStackTrace();
            // 복사 실패 시 빈 파일 생성
            createEmptyFileIfNotExists(targetFileName);
        }
    }
    
    /**
     * 빈 파일 생성 (존재하지 않을 경우)
     */
    private void createEmptyFileIfNotExists(String fileName) {
        Path file = dataDirectory.resolve(fileName);
        if (!Files.exists(file)) {
            try {
                Files.createFile(file);
                System.out.println("빈 파일 생성: " + fileName);
            } catch (IOException e) {
                System.err.println("파일 생성 실패: " + fileName);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 특정 데이터 파일의 전체 경로 반환
     */
    public File getDataFile(String fileName) {
        return dataDirectory.resolve(fileName).toFile();
    }
    
    /**
     * settings.txt 파일 경로
     */
    public File getSettingsFile() {
        return getDataFile("settings.txt");
    }
    
    /**
     * highscore.txt 파일 경로
     */
    public File getHighScoreFile() {
        return getDataFile("highscore.txt");
    }
    
    /**
     * highscore_v2.txt 파일 경로
     */
    public File getHighScoreV2File() {
        return getDataFile("highscore_v2.txt");
    }
    
    /**
     * 데이터 디렉토리 전체 경로 반환
     */
    public Path getDataDirectory() {
        return dataDirectory;
    }
    
    /**
     * 데이터 디렉토리 문자열 경로 반환
     */
    public String getDataDirectoryPath() {
        return dataDirectory.toAbsolutePath().toString();
    }
    
    /**
     * 모든 스코어 파일 초기화
     */
    public void clearAllScores() {
        try {
            Files.write(getHighScoreFile().toPath(), new byte[0]);
            Files.write(getHighScoreV2File().toPath(), new byte[0]);
            System.out.println("모든 스코어 파일 초기화 완료");
        } catch (IOException e) {
            System.err.println("스코어 파일 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 디버그 정보 출력
     */
    public void printDebugInfo() {
        System.out.println("=== 데이터 경로 정보 ===");
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("데이터 디렉토리: " + dataDirectory.toAbsolutePath());
        System.out.println("settings.txt: " + getSettingsFile().getAbsolutePath());
        System.out.println("highscore_v2.txt: " + getHighScoreV2File().getAbsolutePath());
        System.out.println("존재 여부:");
        System.out.println("  - 디렉토리: " + Files.exists(dataDirectory));
        System.out.println("  - settings.txt: " + getSettingsFile().exists());
        System.out.println("  - highscore_v2.txt: " + getHighScoreV2File().exists());
        System.out.println("====================");
    }
}
