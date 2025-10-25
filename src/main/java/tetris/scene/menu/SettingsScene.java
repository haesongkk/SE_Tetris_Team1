package tetris.scene.menu;

import tetris.Game;
import tetris.GameSettings;
import tetris.scene.Scene;
import tetris.util.Theme;

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
    private JComboBox<String> difficultyCombo;
    private JSlider volumeSlider;
    
    // Theme 기반 색상 메서드들 (색맹 모드에 따라 변경, 일반 모드는 원래 색상)
    private Color getBackgroundColor() { return Theme.MenuBG(); }
    private Color getTitleColor() { return Theme.MenuTitle(); }
    private Color getPanelColor() { return Theme.MenuPanel(); }
    private Color getTextColor() { return Theme.WHITE; }
    private Color getButtonColor() {
        return Theme.MenuButton();
    }
    
    private Color getSelectedButtonColor() {
        return new Color(120, 120, 200); // 선택된 버튼 색상 (고정)
    }

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
        setBackground(getBackgroundColor());
        setFocusable(true);
        
        add(createTitlePanel(), BorderLayout.NORTH);
        
        // 설정 패널을 커스텀 스크롤 패널로 감싸기
        JPanel settingsPanel = createSettingsPanel();
        JScrollPane scrollPane = createCustomScrollPane(settingsPanel);
        
        add(scrollPane, BorderLayout.CENTER);
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
        label.setForeground(getTitleColor());
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
        scoreResetButton.setBackground(new Color(120, 50, 50)); // 삭제 버튼은 빨간색 유지
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
        
        // 6. 난이도 설정
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(createLabel("난이도:"), gbc);
        
        gbc.gridx = 1;
        String[] difficulties = {"Easy", "Normal", "Hard"};
        difficultyCombo = new JComboBox<>(difficulties);
        difficultyCombo.setSelectedIndex(1); // Normal로 기본 설정
        styleComboBox(difficultyCombo);
        panel.add(difficultyCombo, gbc);

        // 7. 음량 설정
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(createLabel("음량:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        volumeSlider = new JSlider(0, 100, 20); // 초기 20%
        volumeSlider.setBackground(getBackgroundColor());
        panel.add(volumeSlider, gbc);
        
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
        resetButton.setBackground(new Color(100, 60, 60)); // 리셋 버튼은 빨간색 유지
        panel.add(resetButton);
        
        JButton applyButton = createStyledButton("적용");
        applyButton.addActionListener(e -> applySettings());
        applyButton.setBackground(new Color(60, 120, 60)); // 적용 버튼은 녹색 유지
        panel.add(applyButton);
        
        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(getTextColor());
        label.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        return label;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(160, 40));
        button.setBackground(getButtonColor());
        button.setForeground(getTextColor());
        button.setFocusable(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        return button;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(getPanelColor());
        comboBox.setForeground(getTextColor());
        comboBox.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
        comboBox.setFocusable(false);
    }
    
    private JButton createKeyButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(120, 35));
        button.setBackground(getButtonColor());
        button.setForeground(getTextColor());
        button.setFocusable(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // 호버 효과
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(getSelectedButtonColor());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(getButtonColor());
            }
        });
        
        return button;
    }

    private void setupKeyListener() {
        addKeyListener(this);
        setFocusable(true);
    }
    
    /**
     * 적용 버튼을 눌렀을 때 색맹 모드 변경사항을 UI에 적용합니다.
     */
    private void updateThemeColors() {
        // 메인 패널 색상 업데이트
        setBackground(getBackgroundColor());
        
        // 전체 UI 다시 구축
        removeAll();
        setupUI();
        
        // 현재 설정 값들 다시 로드
        loadCurrentSettings();
    }

    private void loadCurrentSettings() {
        if (displayModeCombo != null) displayModeCombo.setSelectedIndex(gameSettings.getDisplayMode());
        if (resolutionCombo != null) resolutionCombo.setSelectedIndex(gameSettings.getResolution());
        if (colorBlindModeCombo != null) colorBlindModeCombo.setSelectedIndex(gameSettings.getColorBlindMode());
        if (difficultyCombo != null) difficultyCombo.setSelectedIndex(gameSettings.getDifficultyIndex());
        if (volumeSlider != null) volumeSlider.setValue(gameSettings.getVolume());
    }

    private void saveCurrentSettings() {
        if (displayModeCombo != null) gameSettings.setDisplayMode(displayModeCombo.getSelectedIndex());
        if (resolutionCombo != null) gameSettings.setResolution(resolutionCombo.getSelectedIndex());
        if (colorBlindModeCombo != null) gameSettings.setColorBlindMode(colorBlindModeCombo.getSelectedIndex());
        if (difficultyCombo != null) gameSettings.setDifficultyIndex(difficultyCombo.getSelectedIndex());
        if (volumeSlider != null) gameSettings.setVolume(volumeSlider.getValue());
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
        updateThemeColors(); // 색맹 모드 적용 후 색상 업데이트
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
        JDialog keyDialog = new JDialog(frame, "조작키 설정", true);
        keyDialog.setSize(600, 800);
        keyDialog.setLocationRelativeTo(frame);
        keyDialog.setLayout(new BorderLayout());
        keyDialog.getContentPane().setBackground(getBackgroundColor());
        
        // 타이틀 패널
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(getBackgroundColor());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel titleLabel = new JLabel("조작키 설정", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 28));
        titleLabel.setForeground(getTitleColor());
        titlePanel.add(titleLabel);
        
        // 메인 패널 (스타일링 적용)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(getPanelColor());
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(20, 30, 20, 30),
            BorderFactory.createRaisedBevelBorder()
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.anchor = GridBagConstraints.WEST;
        
        GameSettings settings = GameSettings.getInstance();
        
        // 키 설정 버튼들
        JButton[] keyButtons = new JButton[7];
        String[] keyLabels = {"좌로 이동:", "우로 이동:", "회전:", "아래로 이동:", "한번에 떨어뜨리기:", "일시정지:", "홀드:"};
        int[] currentKeys = {
            settings.getLeftKey(), settings.getRightKey(), settings.getRotateKey(),
            settings.getFallKey(), settings.getDropKey(), settings.getPauseKey(), settings.getHoldKey()
        };
        
        for (int i = 0; i < keyLabels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.4;
            gbc.fill = GridBagConstraints.NONE;
            
            JLabel label = createLabel(keyLabels[i]);
            mainPanel.add(label, gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.6;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            keyButtons[i] = createKeyButton(GameSettings.getKeyName(currentKeys[i]));
            final int index = i;
            keyButtons[i].addActionListener(e -> changeKey(keyButtons[index], index));
            mainPanel.add(keyButtons[i], gbc);
        }
        
        // 설명 패널
        JPanel descPanel = new JPanel();
        descPanel.setBackground(getPanelColor());
        descPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JLabel descLabel = new JLabel("<html><center>키를 변경하려면 해당 버튼을 클릭하세요<br>ESC 키로 취소할 수 있습니다</center></html>");
        descLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        descLabel.setForeground(getTextColor());
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descPanel.add(descLabel);
        
        // 버튼 패널 (스타일링 적용)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(getBackgroundColor());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JButton resetButton = createStyledButton("기본값으로 초기화");
        resetButton.setBackground(new Color(100, 60, 60)); // 리셋 버튼은 빨간색 유지
        resetButton.addActionListener(e -> {
            settings.resetToDefaults();
            // 버튼 텍스트 업데이트
            for (int i = 0; i < keyButtons.length; i++) {
                int[] defaultKeys = {
                    settings.getLeftKey(), settings.getRightKey(), settings.getRotateKey(),
                    settings.getFallKey(), settings.getDropKey(), settings.getPauseKey(), settings.getHoldKey()
                };
                keyButtons[i].setText(GameSettings.getKeyName(defaultKeys[i]));
            }
        });
        
        JButton closeButton = createStyledButton("닫기");
        closeButton.setBackground(new Color(60, 120, 60)); // 확인 버튼은 녹색 유지
        closeButton.addActionListener(e -> keyDialog.dispose());
        
        buttonPanel.add(resetButton);
        buttonPanel.add(closeButton);
        
        // 메인 컨테이너 패널
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(getPanelColor());
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        contentPanel.add(descPanel, BorderLayout.SOUTH);
        
        keyDialog.add(titlePanel, BorderLayout.NORTH);
        keyDialog.add(contentPanel, BorderLayout.CENTER);
        keyDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        keyDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        keyDialog.setVisible(true);
    }
    
    private void changeKey(JButton button, int keyIndex) {
        // 키 입력 대화상자 (스타일링 적용)
        JDialog keyInputDialog = new JDialog(frame, "키 입력", true);
        keyInputDialog.setSize(400, 200);
        keyInputDialog.setLocationRelativeTo(frame);
        keyInputDialog.setLayout(new BorderLayout());
        keyInputDialog.getContentPane().setBackground(getBackgroundColor());
        
        // 타이틀 패널
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(getBackgroundColor());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        JLabel titleLabel = new JLabel("키 입력", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        titleLabel.setForeground(getTitleColor());
        titlePanel.add(titleLabel);
        
        // 메인 패널
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(getPanelColor());
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(20, 30, 20, 30),
            BorderFactory.createRaisedBevelBorder()
        ));
        
        JLabel instructionLabel = new JLabel("<html><center>사용할 키를 눌러주세요<br><br><font color='#CCCCCC'>ESC를 누르면 취소됩니다</font></center></html>");
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
        instructionLabel.setForeground(getTextColor());
        mainPanel.add(instructionLabel);
        
        keyInputDialog.add(titlePanel, BorderLayout.NORTH);
        keyInputDialog.add(mainPanel, BorderLayout.CENTER);
        
        keyInputDialog.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                
                if (keyCode == KeyEvent.VK_ESCAPE) {
                    keyInputDialog.dispose();
                    return;
                }
                
                // 키 설정 저장
                GameSettings settings = GameSettings.getInstance();
                switch (keyIndex) {
                    case 0: settings.setLeftKey(keyCode); break;
                    case 1: settings.setRightKey(keyCode); break;
                    case 2: settings.setRotateKey(keyCode); break;
                    case 3: settings.setFallKey(keyCode); break;
                    case 4: settings.setDropKey(keyCode); break;
                    case 5: settings.setPauseKey(keyCode); break;
                    case 6: settings.setHoldKey(keyCode); break;
                }
                
                // 버튼 텍스트 업데이트
                button.setText(GameSettings.getKeyName(keyCode));
                keyInputDialog.dispose();
            }
            
            @Override
            public void keyReleased(KeyEvent e) {}
            
            @Override
            public void keyTyped(KeyEvent e) {}
        });
        
        keyInputDialog.setFocusable(true);
        keyInputDialog.requestFocus();
        keyInputDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        keyInputDialog.setVisible(true);
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

    // 커스텀 스크롤바가 적용된 스크롤 패널 생성
    private JScrollPane createCustomScrollPane(JPanel contentPanel) {
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        
        // 기본 설정
        scrollPane.setBackground(getBackgroundColor());
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // 커스텀 스크롤바 UI 적용
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUI(new CustomScrollBarUI());
        verticalScrollBar.setPreferredSize(new Dimension(12, 0)); // 더 얇은 스크롤바
        
        return scrollPane;
    }
    
    // 커스텀 스크롤바 UI 클래스
    private static class CustomScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        
        @Override
        protected void configureScrollBarColors() {
            // 트랙은 설정창 배경과 같은 색
            this.trackColor = Theme.MenuBG();  // Theme에서 배경색 가져오기
            // 평상시 썸은 검은색
            this.thumbColor = new Color(0, 0, 0, 150);  // 반투명 검은색
            // 호버시 썸은 어두운 회색
            this.thumbHighlightColor = new Color(80, 80, 80, 200);  // 어두운 회색
            this.thumbLightShadowColor = new Color(0, 0, 0, 0);  // 투명
            this.thumbDarkShadowColor = new Color(0, 0, 0, 0);   // 투명
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 트랙을 설정창 배경색으로 칠함
            g2.setColor(trackColor);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            
            // 호버/드래그 효과에 따른 색상 변경
            if (isDragging || isThumbRollover()) {
                // 마우스 호버시: 어두운 회색
                g2.setColor(thumbHighlightColor);
            } else {
                // 평상시: 검은색 (반투명)
                g2.setColor(thumbColor);
            }
            
            // 더 얇고 둥근 썸 (여백을 더 주어서 얇게)
            g2.fillRoundRect(thumbBounds.x + 4, thumbBounds.y + 1, 
                           thumbBounds.width - 8, thumbBounds.height - 2, 4, 4);
        }
    }

    @Override
    public void onEnter() {
        requestFocusInWindow();
    }
}