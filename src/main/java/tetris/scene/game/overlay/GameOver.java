package tetris.scene.game.overlay;
 
import javax.swing.*;

import tetris.util.Theme;
import tetris.util.VerifyData;
import tetris.Game;
import tetris.GameSettings;
import tetris.scene.game.GameScene;
import tetris.scene.scorescene.ScoreScene;
import tetris.util.Animation;
import tetris.util.Loader;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GameOver extends JPanel {
    JFrame frame;
    GOPanel popup;

    int score;
    int lines;
    int time;
    String difficulty;
    int rank;

    final float[] canvasRatio = { 0.3f, 0.6f };

    public GameOver(JFrame frame, int score, int lines, int time, String difficulty) {
        this.frame = frame;
        this.score = score;
        this.lines = lines;
        this.time = time;
        this.difficulty = difficulty;
        this.rank = getRank(score, difficulty);
        System.out.println("rank: " + rank);

        setOpaque(false);
        
        addMouseListener(new MouseAdapter() {});

        // 캔버스 사이즈 맞추기
        final int[] screenSize = GameSettings.getInstance().getResolutionSize();
        final float[] borderRatio = {1-canvasRatio[0], 1-canvasRatio[1]};
        final int borderHeight = (int)(screenSize[1] * borderRatio[1]/2);
        final int borderWidth = (int)(screenSize[0] * borderRatio[0]/2);

        setLayout(new GridLayout(1, 1));
        setBorder(BorderFactory.createEmptyBorder(borderHeight, borderWidth, borderHeight, borderWidth));
        
        popup = new GOPanel(
            Integer.toString(score), 
            Integer.toString(lines), 
            formatTime(time), 
            difficulty, 
            rank > 0
        );
        add(popup);

        frame.getRootPane().setGlassPane(this);
        this.setVisible(true);
        this.requestFocusInWindow();
    }

    void onRetry() {
        Game.setScene(new GameScene(frame));
        popup.free();
        free();
    }

    void onNext(String name) {
        if(rank > 0) updateHighScore(rank, name, Integer.toString(score), Integer.toString(lines), formatTime(time), difficulty);
        Game.setScene(new ScoreScene(frame, rank, difficulty));
        popup.free();
        free();
    }


    void free() {
        //frame.getRootPane().setGlassPane(null);
        popup.free();
        popup = null;
        frame = null;

    }

    
    String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    void updateHighScore(int rank, String name, String score, String lines, String time, String mode) {
        final String path = "./data/highscore_v2.txt";
        List<String> file = Loader.loadFile(path);
        if(!VerifyData.verifyHighScore(file)) {
            file = new ArrayList<String>();
            Loader.saveFile(path, file);
        }

        final String tag = "#" + mode;
        final String entry = rank + "," + name + "," + score + "," + lines + "," + time;

        
        int tagIdx = file.indexOf(tag);
         // 1) 섹션이 없으면: 태그 + 첫 레코드 추가
        if (tagIdx == -1) {
            file.add(tag);
            file.add(entry);
            Loader.saveFile(path, file);
            return;
        }

        // 2) 섹션 경계 찾기
        final int sectionStart = tagIdx + 1; // 첫 기록 라인
        int nextTagIdx = -1;
        for (int i = sectionStart; i < file.size(); i++) {
            String ln = file.get(i);
            if (ln != null && ln.strip().startsWith("#")) {
                nextTagIdx = i; // 다음 섹션의 태그 라인
                break;
            }
        }
        final int sectionEnd = (nextTagIdx == -1) ? file.size() : nextTagIdx; // [start, end)
         // 3) 기존 레코드만(빈 줄 제외) 수집
        List<String[]> records = new ArrayList<>();
        for (int i = sectionStart; i < sectionEnd; i++) {
            String ln = file.get(i);
            if (ln == null) continue;
            String trimmed = ln.strip();
            if (trimmed.isEmpty()) continue;

            String[] parts = trimmed.split(",", -1);
            if (parts.length < 5) continue; // 형식 불량 방어
            records.add(parts);
        }
        // 4) 삽입 위치(0-base) 계산: rank는 1-base 가정 → 경계 보정
        int insertPos = Math.max(0, Math.min(rank - 1, records.size()));

        // 새 레코드 파싱 형태로 만들어 끼우기
        String[] newParts = entry.split(",", -1);
        if (newParts.length < 5) {
            // 안전을 위해 최소 길이 보정
            newParts = new String[]{ String.valueOf(rank), name, score, lines, time };
        }
        records.add(insertPos, newParts);

        // 5) 상위 10개만 유지
        if (records.size() > 10) {
            records = new ArrayList<>(records.subList(0, 10));
        }

        // 6) 랭크 번호를 1..N으로 다시 매겨서 문자열로 재조립
        java.util.List<String> rebuilt = new java.util.ArrayList<>(records.size());
        for (int i = 0; i < records.size(); i++) {
            String[] p = records.get(i);
            // p[0] = rank 갱신
            p[0] = Integer.toString(i + 1);
            rebuilt.add(String.join(",", p));
        }

        // 7) 섹션 내용 교체
        file.subList(sectionStart, sectionEnd).clear();
        file.addAll(sectionStart, rebuilt);

        Loader.saveFile(path, file);
    }

    int getRank(int score, String mode) {
        final String path = "./data/highscore_v2.txt";
        List<String> lines = Loader.loadFile(path);
        if(!VerifyData.verifyHighScore(lines)) {
            List<String> initialHighScore = new ArrayList<String>();
            Loader.saveFile(path, initialHighScore);
            lines = initialHighScore;
        }

        final String tagtoken = "#";
        final String tag = tagtoken + mode;

        boolean foundTag = false;
        int rank = 0;

        for(String line : lines) {
            String trimmed = line.strip();

            if(!foundTag) {
                if(trimmed.equals(tag)) foundTag = true;
                continue;
            }

            if(trimmed.startsWith(tagtoken)) break;

            if(!trimmed.isEmpty()) {
                String[] parts = trimmed.split(",", -1);
                rank = Integer.parseInt(parts[0]);
                if(Integer.parseInt(parts[2].trim()) < score) return rank;
            }
        }

        
        if(!foundTag) return 1;
        if(++rank <= 10) return rank;
        else return -1;

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.SrcOver.derive(0.6f));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}


