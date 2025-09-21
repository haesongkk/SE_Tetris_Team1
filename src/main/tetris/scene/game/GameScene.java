package tetris.scene.game;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import tetris.Game;
import tetris.scene.Scene;
import tetris.scene.game.blocks.*;
import tetris.scene.menu.MenuScene;

public class GameScene extends Scene {

    private JFrame m_frame;
    private static final int HEIGHT = 20;
    private static final int WIDTH  = 10;
    private static final char BORDER_CHAR = 'X';
    private static final int INIT_INTERVAL_MS = 1000;

    private JTextPane pane;
    private int[][] board;
    private SimpleAttributeSet styleSet;
    private Timer timer;
    private Block curr;
    private int x = 3; // Default Position
    private int y = 0;

    private boolean initialized = false;

    // ─────────────────────────────────────────────────────────────
    // Scene lifecycle
    // ─────────────────────────────────────────────────────────────

    public GameScene(JFrame frame) {
        super(frame);
        m_frame = frame;
        m_frame.setContentPane(this);
    }
    @Override
    public void onEnter() {
        if (!initialized) {
            initUI();
            initGameState();
            initialized = true;
        }
        requestFocusInWindow();
        if (timer != null && !timer.isRunning()) timer.start();
    }

    @Override
    public void onExit() {
        if (timer != null) timer.stop();
    }

    // ─────────────────────────────────────────────────────────────
    // Init
    // ─────────────────────────────────────────────────────────────
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        pane = new JTextPane();
        pane.setEditable(false);
        pane.setBackground(Color.BLACK);
        CompoundBorder border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 10),
                BorderFactory.createLineBorder(Color.DARK_GRAY, 5)
        );
        pane.setBorder(border);
        add(pane, BorderLayout.CENTER);

        styleSet = new SimpleAttributeSet();
        StyleConstants.setFontSize(styleSet, 18);
        StyleConstants.setFontFamily(styleSet, "Courier");
        StyleConstants.setBold(styleSet, true);
        StyleConstants.setForeground(styleSet, Color.WHITE);
        StyleConstants.setAlignment(styleSet, StyleConstants.ALIGN_CENTER);

        // 키 입력은 Scene(JPanel)에 직접 연결
        addKeyListener(new PlayerKeyListener());
        setFocusable(true);

        // 드롭 타이머
        timer = new Timer(INIT_INTERVAL_MS, e -> {
            moveDown();
            drawBoard();
        });
    }

    private void initGameState() {
        board = new int[HEIGHT][WIDTH];
        curr = getRandomBlock();
        placeBlock();
        drawBoard();
    }

    // ─────────────────────────────────────────────────────────────
    // Game logic (원본 Board 로직 그대로 이식/정리)
    // ─────────────────────────────────────────────────────────────
    private Block getRandomBlock() {
        Random rnd = new Random(System.currentTimeMillis());
        int block = rnd.nextInt(7); // 0~6
        switch (block) {
            case 0: return new IBlock();
            case 1: return new JBlock();
            case 2: return new LBlock();
            case 3: return new ZBlock();
            case 4: return new SBlock();
            case 5: return new TBlock();
            case 6: return new OBlock();
        }
        return new LBlock();
    }

    private void placeBlock() {
        StyledDocument doc = pane.getStyledDocument();
        SimpleAttributeSet styles = new SimpleAttributeSet();
        StyleConstants.setForeground(styles, curr.getColor());
        for (int j = 0; j < curr.height(); j++) {
            int rows = y + j == 0 ? 0 : y + j - 1;
            int offset = rows * (WIDTH + 3) + x + 1;
            doc.setCharacterAttributes(offset, curr.width(), styles, true);
            for (int i = 0; i < curr.width(); i++) {
                board[y + j][x + i] = curr.getShape(i, j);
            }
        }
    }

    private void eraseCurr() {
        for (int i = x; i < x + curr.width(); i++) {
            for (int j = y; j < y + curr.height(); j++) {
                board[j][i] = 0;
            }
        }
    }

    private void moveDown() {
        eraseCurr();
        if (y < HEIGHT - curr.height()) y++;
        else {
            placeBlock();
            curr = getRandomBlock();
            x = 3;
            y = 0;
        }
        placeBlock();
    }

    private void moveRight() {
        eraseCurr();
        if (x < WIDTH - curr.width()) x++;
        placeBlock();
    }

    private void moveLeft() {
        eraseCurr();
        if (x > 0) x--;
        placeBlock();
    }

    private void drawBoard() {
        StringBuilder sb = new StringBuilder();
        for (int t = 0; t < WIDTH + 2; t++) sb.append(BORDER_CHAR);
        sb.append("\n");
        for (int i = 0; i < board.length; i++) {
            sb.append(BORDER_CHAR);
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == 1) sb.append("O");
                else sb.append(" ");
            }
            sb.append(BORDER_CHAR).append("\n");
        }
        for (int t = 0; t < WIDTH + 2; t++) sb.append(BORDER_CHAR);
        pane.setText(sb.toString());

        StyledDocument doc = pane.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), styleSet, false);
        pane.setStyledDocument(doc);
    }

    @SuppressWarnings("unused")
    private void reset() {
        for (int i = 0; i < HEIGHT; i++)
            for (int j = 0; j < WIDTH; j++)
                board[i][j] = 0;
    }

    // ─────────────────────────────────────────────────────────────
    // Input
    // ─────────────────────────────────────────────────────────────
    private class PlayerKeyListener implements KeyListener {
        @Override public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DOWN:
                    moveDown();
                    drawBoard();
                    break;
                case KeyEvent.VK_RIGHT:
                    moveRight();
                    drawBoard();
                    break;
                case KeyEvent.VK_LEFT:
                    moveLeft();
                    drawBoard();
                    break;
                case KeyEvent.VK_UP:
                    eraseCurr();
                    curr.rotate();
                    drawBoard();
                    break;
                case KeyEvent.VK_ESCAPE:
                    Game.setScene(new MenuScene(m_frame));
                    break;
            }
        }
        @Override public void keyReleased(KeyEvent e) {}
    }
}
