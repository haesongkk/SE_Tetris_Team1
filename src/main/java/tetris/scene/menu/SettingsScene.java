package tetris.scene.menu;

import tetris.Game;
import tetris.GameSettings;
import tetris.scene.Scene;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SettingsScene extends Scene implements KeyListener {
    private final JFrame frame;
    private final GameSettings gameSettings;
    private JComboBox<String> displayModeCombo;
    private JComboBox<String> resolutionCombo;
    private JComboBox<String> colorBlindModeCombo;
    
    private final Color BACKGROUND_COLOR = new Color(20, 20, 40);
    private final Color TITLE_COLOR = new Color(255, 255, 100);
    private final Color PANEL_COLOR = new Color(40, 40, 70);
    private final Color TEXT_COLOR = Color.WHITE;
    private final Color BUTTON_COLOR = new Color(70, 70, 120);

    public SettingsScene(JFrame frame) {
        super(frame);
        this.frame = frame;
        this.gameSettings = GameSettings.getInstance();
        
        setupUI();
        loadCurrentSettings();
        setupKeyListener();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setFocusable(true);
        
        add(createTitlePanel(), BorderLayout.NORTH);
        add(createSettingsPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        frame.setContentPane(this);
        frame.revalidate();
        frame.repaint();
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        
        JLabel label = new JLabel("SETTINGS");
        label.setFont(new Font("Arial", Font.BOLD, 48));
        label.setForeground(TITLE_COLOR);
        label.setHorizontalAlignment(JLabel.CENTER);
        
        panel.add(label);
        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 1. 화면 모드 설정
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createLabel("화면 모드:"), gbc);
        
        gbc.gridx = 1;
        String[] displayModes = {"창모드", "전체화면"};
        displayModeCombo = new JComboBox<>(displayModes);
        styleComboBox(displayModeCombo);
        panel.add(displayModeCombo, gbc);
        
        // 2. 해상도 설정
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(createLabel("해상도:"), gbc);
        
        gbc.gridx = 1;
        String[] resolutions = {"800x600", "1024x768", "1280x720", "1920x1080"};
        resolutionCombo = new JComboBox<>(resolutions);
        resolutionCombo.setSelectedIndex(2);
        styleComboBox(resolutionCombo);
        panel.add(resolutionCombo, gbc);
        
        // 3. 게임 조작 키 설정 버튼
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton keySettingsButton = createStyledButton("게임 조작 키 설정");
        keySettingsButton.addActionListener(e -> showKeySettings());
        panel.add(keySettingsButton, gbc);
        
        // 4. 스코어 보드 기록 초기화 버튼
        gbc.gridy = 3;
        JButton scoreResetButton = createStyledButton("스코어 보드 기록 초기화");
        scoreResetButton.addActionListener(e -> clearScoreBoard());
        scoreResetButton.setBackground(new Color(120, 50, 50));
        panel.add(scoreResetButton, gbc);
        
        // 5. 색맹 모드 설정
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(createLabel("색맹 모드:"), gbc);
        
        gbc.gridx = 1;
        String[] colorBlindModes = {"일반 모드", "적록색맹 모드", "청황색맹 모드"};
        colorBlindModeCombo = new JComboBox<>(colorBlindModes);
        colorBlindModeCombo.setSelectedIndex(0);
        styleComboBox(colorBlindModeCombo);
        panel.add(colorBlindModeCombo, gbc);
        
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        
        JButton backButton = createStyledButton("뒤로가기");
        backButton.addActionListener(e -> goBack());
        panel.add(backButton);
        
        JButton resetButton = createStyledButton("설정 초기화");
        resetButton.addActionListener(e -> resetSettings());
        resetButton.setBackground(new Color(100, 60, 60));
        panel.add(resetButton);
        
        JButton applyButton = createStyledButton("적용");
        applyButton.addActionListener(e -> applySettings());
        applyButton.setBackground(new Color(60, 120, 60));
        panel.add(applyButton);
        
        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        return label;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(160, 40));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusable(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        return button;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(PANEL_COLOR);
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
        comboBox.setFocusable(false);
    }

    private void setupKeyListener() {
        addKeyListener(this);
        setFocusable(true);
    }

    private void loadCurrentSettings() {
        if (displayModeCombo != null) displayModeCombo.setSelectedIndex(gameSettings.getDisplayMode());
        if (resolutionCombo != null) resolutionCombo.setSelectedIndex(gameSettings.getResolution());
        if (colorBlindModeCombo != null) colorBlindModeCombo.setSelectedIndex(gameSettings.getColorBlindMode());
    }

    private void saveCurrentSettings() {
        if (displayModeCombo != null) gameSettings.setDisplayMode(displayModeCombo.getSelectedIndex());
        if (resolutionCombo != null) gameSettings.setResolution(resolutionCombo.getSelectedIndex());
        if (colorBlindModeCombo != null) gameSettings.setColorBlindMode(colorBlindModeCombo.getSelectedIndex());
    }

    private void goBack() {
        Game.setScene(new MainMenuScene(frame));
    }

    private void resetSettings() {
        int choice = JOptionPane.showConfirmDialog(this,
            "모든 설정을 초기값으로 되돌리시겠습니까?",
            "설정 초기화",
            JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            gameSettings.resetToDefaults();
            loadCurrentSettings();
            JOptionPane.showMessageDialog(this, "설정이 초기화되었습니다.");
        }
    }

    private void applySettings() {
        saveCurrentSettings();
        applyDisplaySettings();
        JOptionPane.showMessageDialog(this,
            gameSettings.getSettingsInfo(),
            "설정 적용",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void applyDisplaySettings() {
        int[] size = gameSettings.getResolutionSize();
        int width = size[0];
        int height = size[1];
        
        if (gameSettings.getDisplayMode() == 1) { // 전체화면
            frame.dispose();
            frame.setUndecorated(true);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        } else { // 창모드
            frame.dispose();
            frame.setUndecorated(false);
            frame.setExtendedState(JFrame.NORMAL);
            frame.setSize(width, height);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
        
        frame.revalidate();
        frame.repaint();
    }

    private void showKeySettings() {
        JOptionPane.showMessageDialog(this,
            "게임 조작 키 설정:\n\n" +
            "현재 기본 키 설정:\n" +
            "• 좌/우 이동: ← →\n" +
            "• 회전: ↑\n" +
            "• 빠른 낙하: ↓\n" +
            "• 즉시 낙하: Space\n" +
            "• 일시정지: P\n" +
            "• 홀드: Shift\n\n" +
            "키 커스터마이징 기능은 추후 업데이트 예정입니다.",
            "게임 조작 키 설정",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearScoreBoard() {
        int choice = JOptionPane.showConfirmDialog(this,
            "정말로 모든 점수 기록을 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다!",
            "스코어 보드 초기화",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            gameSettings.clearScoreBoard();
            JOptionPane.showMessageDialog(this,
                "스코어 보드가 초기화되었습니다.\n모든 점수 기록이 삭제되었습니다.",
                "초기화 완료",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                goBack();
                break;
            case KeyEvent.VK_ENTER:
                applySettings();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void onEnter() {
        requestFocusInWindow();
    }
}