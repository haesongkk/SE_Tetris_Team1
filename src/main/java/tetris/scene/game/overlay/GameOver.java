package tetris.scene.game.overlay;
 
import javax.swing.*;

import tetris.util.Theme;

import tetris.util.Animation;
import tetris.util.Loader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

public class GameOver extends JPanel {
    
    private List<Timer> animationTimers = new ArrayList<>();
    private JFrame frame;

    List<Animation> scoreEntryList = new ArrayList<>();
    String[] scoreEntryContent = {
        "SCORE", "LINES", "TIME", "DIFFICULTY"
    };
    Color[] scoreEntryColor = {
        Theme.I_CYAN, Theme.S_GREEN, Theme.T_PURPLE, Theme.L_ORANGE
    };
    Color scoreValueColor = Theme.SCORE_WHITE;
    Font scoreEntryFont = Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f);

    public GameOver(JFrame frame, int score, int lines, int time, String difficulty) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new GridBagLayout());
        
        // 배경 클릭 이벤트 흡수 (아래쪽 전달 방지)
        addMouseListener(new MouseAdapter() {});

        boolean isHighScore = isHighScore(score);
        System.out.println("isHighScore: " + isHighScore);
        
        GOCanvas popup = new GOCanvas();

        Animation gameOver = new Animation(
            "GAME OVER!!",
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 30f), 
            Theme.HEADER_RED, Theme.BG, Theme.BG, 
            0, 0, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );

        Animation scoreLabel = new Animation(
            "SCORE",
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f), 
            Theme.I_CYAN, Theme.BG, Theme.BG, 
            2, 0, 
            SwingConstants.LEFT, SwingConstants.CENTER
        );

        Animation linesLabel = new Animation(
            "LINES",
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f), 
            Theme.S_GREEN, Theme.BG, Theme.BG, 
            2, 0, 
            SwingConstants.LEFT, SwingConstants.CENTER
        );

        Animation timeLabel = new Animation(
            "TIME",
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f), 
            Theme.T_PURPLE, Theme.BG, Theme.BG, 
            2, 0, 
            SwingConstants.LEFT, SwingConstants.CENTER
        );

        Animation difficultyLabel = new Animation(
            "DIFFICULTY",
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f), 
            Theme.L_ORANGE, Theme.BG, Theme.BG, 
            2, 0, 
            SwingConstants.LEFT, SwingConstants.CENTER
        );

        Animation scoreValue = new Animation(
            Integer.toString(score),
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f), 
            Theme.SCORE_WHITE, Theme.BG, Theme.BG, 
            2, 0, 
            SwingConstants.LEFT, SwingConstants.CENTER
        );

        Animation linesValue = new Animation(
            Integer.toString(lines),
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f), 
            Theme.SCORE_WHITE, Theme.BG, Theme.BG, 
            2, 0, 
            SwingConstants.LEFT, SwingConstants.CENTER
        );  

        Animation timeValue = new Animation(
            Integer.toString(time),
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f), 
            Theme.SCORE_WHITE, Theme.BG, Theme.BG, 
            2, 0, 
            SwingConstants.LEFT, SwingConstants.CENTER
        );

        Animation difficultyValue = new Animation(
            difficulty,
            Theme.GIANTS_INLINE.deriveFont(Font.BOLD, 20f), 
            Theme.SCORE_WHITE, Theme.BG, Theme.BG, 
            2, 0, 
            SwingConstants.LEFT, SwingConstants.CENTER
        );


     

        popup.setLayout(new GridBagLayout());

        JPanel scoreEntry = new JPanel();
        scoreEntry.setOpaque(false);
        scoreEntry.setLayout(new GridLayout(4,2,4,4));
        scoreEntry.add(scoreLabel);
        scoreEntry.add(scoreValue);
        scoreEntry.add(linesLabel);
        scoreEntry.add(linesValue);
        scoreEntry.add(timeLabel);
        scoreEntry.add(timeValue);
        scoreEntry.add(difficultyLabel);
        scoreEntry.add(difficultyValue);





        GOFooter footer = new GOFooter(isHighScore);

        popup.add(gameOver, new GridBagConstraints(
            0, 0,               // gridx, gridy
            1, 2,               // gridwidth, gridheight
            1.0, 0.1,           // weightx, weighty
            GridBagConstraints.NORTH, // anchor
            GridBagConstraints.HORIZONTAL, // fill
            new Insets(28, 0, 12, 0), // insets (여백)
            0, 0                // ipadx, ipady
        ));

        // 배지 영역을 항상 추가 (하이스코어일 때는 배지, 아닐 때는 빈 공간)
        final Animation badge;
        if (isHighScore) {
            //badge= new Badge();
            badge = new Animation(
                "HIGH SCORE!", Theme.GIANTS_BOLD.deriveFont(Font.PLAIN, 12f), 
                new Color(255, 255, 255), 
                new Color(30, 30, 30, 240), 
                Theme.BADGE_YELLOW,
                2, 24, 
                SwingConstants.CENTER, SwingConstants.CENTER
            );
            badge.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
            popup.add(badge, new GridBagConstraints(
                0, 2,
                1, 1,
                1.0, 0.0,                      // 세로 공간 거의 안 먹음
                GridBagConstraints.CENTER,
                GridBagConstraints.NONE,       // 배지 자체 크기 유지
                new Insets(0, 12, 8, 12),      // header와 body 사이 여백
                0, 0
            ));
        } else {
            badge = null;
            // 하이스코어가 아닐 때는 투명한 빈 패널을 추가하여 공간 유지
            JPanel emptySpace = new JPanel();
            emptySpace.setOpaque(false);
            // Badge와 동일한 크기 설정
            emptySpace.setPreferredSize(new Dimension(120, 28));
            emptySpace.setMinimumSize(new Dimension(100, 24));
            emptySpace.setMaximumSize(new Dimension(160, 32));
            popup.add(emptySpace, new GridBagConstraints(
                0, 2,
                1, 1,
                1.0, 0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0, 12, 8, 12),
                0, 0
            ));
        }

        // body와 footer는 항상 동일한 위치에 배치
        popup.add(scoreEntry, new GridBagConstraints(
            0, 3,
            1, 2,
            1.0, 0.8,
            GridBagConstraints.CENTER,
            GridBagConstraints.BOTH,
            new Insets(0, 24, 0, 24),
            0, 0
        ));

        popup.add(footer, new GridBagConstraints(
            0, 5,
            1, 1,
            1.0, 0.1,
            GridBagConstraints.SOUTH,
            GridBagConstraints.HORIZONTAL,
            new Insets(12, 12, 24, 12),
            0, 0
        ));


        // ==== Popup을 GameOver에 중앙 배치 ====
        add(popup, new GridBagConstraints(
            0, 0,
            1, 1,
            1.0, 1.0,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0),
            0, 0
        ));

        JRootPane root = frame.getRootPane();
        root.setGlassPane(this);
        this.setVisible(true);
        this.requestFocusInWindow();

        popup.hueBorder(4.5f, true);

        runLater(0, () -> gameOver.move(50, 0, 0, 0, 1.5f, 0.3f, false)); 
        //runLater(0, () -> header.startAnimation(0.5f, 2.5f)); 
        runLater(500, () -> scoreLabel.move(-50, 0, 0, 0, 1.5f, 0.3f, false)); 
        runLater(800, () -> linesLabel.move(-50, 0, 0, 0, 1.5f, 0.3f, false)); 
        runLater(1100, () -> timeLabel.move(-50, 0, 0, 0, 1.5f, 0.3f, false)); 
        runLater(1400, () -> difficultyLabel.move(-50, 0, 0, 0, 1.5f, 0.3f, false)); 
        runLater(1700, () -> scoreValue.move(-50, 0, 0, 0, 1.5f, 0.3f, false)); 
        runLater(2000, () -> linesValue.move(-50, 0, 0, 0, 1.5f, 0.3f, false)); 
        runLater(2300, () -> timeValue.move(-50, 0, 0, 0, 1.5f, 0.3f, false)); 
        runLater(2600, () -> difficultyValue.move(-50, 0, 0, 0, 1.5f, 0.3f, false)); 
        
        if (isHighScore) {
            // 하이스코어일 때: 배지 애니메이션 + 입력창
            runLater(3500, () -> {
                if (badge != null) {
                    badge.popOut(0.8f, 0.8f, 0.3f, 1.3f);
                    //badge.startAnimation(0.5f, 1.3f);
                }
            }); 
            runLater(3850, () -> {
                if (badge != null) {
                    badge.hueBorder(3.5f, true);
                }
            });
            runLater(4200, () -> footer.startAnimation(0.5f, 1.3f));
            
            // 이름 입력 후 엔터키 처리
            JTextField nameField = footer.getNameField();
            if (nameField != null) {
                nameField.addActionListener(e -> {
                    String name = nameField.getText().trim();
                    if (!name.isEmpty()) {
                        
                        // 1. 하이스코어 파일 업데이트
                        updateHighScoreFile(name, score, lines, time, difficulty);
                        
                        // 2. ScoreScene으로 이동 (사용자 정보 전달)
                        goToScoreScene(frame, name, score);
                    }
                });
            }
        } else {
            // 하이스코어가 아닐 때: 카운트다운 시작
            runLater(3500, () -> footer.startAnimation(0.5f, 1.3f));
            runLater(4200, () -> footer.startCountdown());
        } 

    }
    

    void runLater(int delayMs, Runnable r) {
        Timer t = new Timer(delayMs, e -> { ((Timer)e.getSource()).stop(); r.run(); });
        t.setRepeats(false);
        animationTimers.add(t); // 타이머 저장
        t.start();
    }

    boolean isHighScore(int score) {
        List<String> lines = Loader.loadFile("highscore.txt");
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length < 3) continue;
            int highScore = Integer.parseInt(parts[2].trim());
            if (score > highScore) return true;
        }
        return false;
    }

   
    
    
    /** 하이스코어 파일 업데이트 (점수 순으로 정렬, 상위 10명만 유지) */
    void updateHighScoreFile(String name, int score, int lines, int time, String difficulty) {
        List<String> existingLines = Loader.loadFile("highscore.txt");
        
        // 시간을 mm:ss 형식으로 변환
        String timeFormatted = String.format("%02d:%02d", time / 60, time % 60);
        
        // 새로운 기록 추가 (올바른 형식: 랭킹,이름,점수,라인수,시간,난이도)
        String newRecord = String.format("0, %s, %d, %d, %s, %s", name, score, lines, timeFormatted, difficulty);
        existingLines.add(newRecord);
        
        // 점수 순으로 정렬 (내림차순)
        existingLines.sort((line1, line2) -> {
            try {
                String[] parts1 = line1.split(",", -1);
                String[] parts2 = line2.split(",", -1);
                if (parts1.length >= 3 && parts2.length >= 3) {
                    int score1 = Integer.parseInt(parts1[2].trim());
                    int score2 = Integer.parseInt(parts2[2].trim());
                    return Integer.compare(score2, score1); // 내림차순
                }
                return 0;
            } catch (Exception e) {
                return 0;
            }
        });
        
        // 상위 10명만 유지하고 랭킹 번호 재설정
        if (existingLines.size() > 10) {
            existingLines = existingLines.subList(0, 10);
        }
        
        // 랭킹 번호 재설정
        for (int i = 0; i < existingLines.size(); i++) {
            String[] parts = existingLines.get(i).split(",", -1);
            if (parts.length >= 6) {
                existingLines.set(i, String.format("%d, %s, %s, %s, %s, %s", 
                    i + 1, parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim(), parts[5].trim()));
            }
        }
        
        // 파일에 저장
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("./resources/highscore.txt"), StandardCharsets.UTF_8))) {
            for (String line : existingLines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /** ScoreScene으로 이동 */
    void goToScoreScene(JFrame frame, String userName, int userScore) {
        // 자원 정리: 타이머들 정지
        stopAllTimers();
        
        // GameOver 팝업 숨기기
        this.setVisible(false);
        
        // 새로 추가된 하이스코어의 순위 찾기
        int newRank = findNewHighScoreRank(userName, userScore);
        
        // ScoreScene으로 이동 (순위 정보 전달)
        tetris.Game.setScene(new tetris.scene.scorescene.ScoreScene(frame, newRank));
    }
    
    /** ScoreScene으로 직접 이동 (하이스코어가 아닌 경우) */
    void goToScoreSceneDirectly() {
        // 자원 정리: 타이머들 정지
        stopAllTimers();
        
        // GameOver 팝업 숨기기
        this.setVisible(false);
        
        // ScoreScene으로 이동 (하이라이트 없음)
        tetris.Game.setScene(new tetris.scene.scorescene.ScoreScene(frame));
    }

    void onRetry() {
        // 자원 정리: 타이머들 정지
        stopAllTimers();
        
        // GameOver 팝업 숨기기
        this.setVisible(false);

        // !!!!!!!! 진짜 GameScene으로 변경 !!!!!!!
        tetris.Game.setScene(new tetris.scene.game.GameScene(frame));
    }
    
    /** 새로 추가된 하이스코어의 순위 찾기 */
    private int findNewHighScoreRank(String userName, int userScore) {
        List<String> lines = Loader.loadFile("highscore.txt");
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split(",", -1);
            if (parts.length >= 3) {
                try {
                    String name = parts[1].trim();
                    int score = Integer.parseInt(parts[2].trim());
                    // 이름과 점수가 모두 일치하는 경우 (새로 추가된 기록)
                    if (name.equals(userName) && score == userScore) {
                        return i + 1; // 1부터 시작하는 순위
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return 1; // 기본값 (1등으로 가정)
    }
    
    /** 모든 타이머 정지 및 자원 정리 */
    void stopAllTimers() {
        // 애니메이션 타이머들 정지
        for (Timer timer : animationTimers) {
            if (timer != null && timer.isRunning()) {
                timer.stop();
            }
        }
        animationTimers.clear();
        
    }

    // ==== 반투명 배경 그리기 ====
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
