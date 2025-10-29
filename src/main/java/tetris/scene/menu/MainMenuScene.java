package tetris.scene.menu;

import tetris.ColorBlindHelper;
import tetris.Game;
import tetris.GameSettings;
import tetris.util.Sound;
import tetris.util.Theme;
import tetris.scene.Scene;
import tetris.scene.game.GameScene;
import tetris.scene.game.ItemGameScene;
import tetris.scene.scorescene.ScoreScene;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class MainMenuScene extends Scene implements KeyListener {
    private final JFrame frame;
    private final GameSettings gameSettings;
    private JButton[] menuButtons;
    private int selectedButton = 0;
    
    private Sound bgm = null;
    
    // 색상 getter 메서드들
    private Color getBackgroundColor() {
        return Theme.MenuBG();
    }
    
    private Color getTitleColor() {
        return Theme.MenuTitle();
    }
    
    private Color getButtonColor() {
        return Theme.MenuButton();
    }
    
    private Color getSelectedButtonColor() {
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        if(colorBlindMode == 0) {
            return new Color(120, 120, 200); // 일반 모드 - 원래 색상 유지
        } else if(colorBlindMode == 1) {
            // 적록색맹 - 밝은 보라색 (더 진하게)
            return new Color(150, 100, 255);
        } else {
            // 청황색맹 - 밝은 주황색
            return new Color(255, 150, 80);
        }
    }
    
    private Color getTextColor() {
        return Color.WHITE;
    }

    // 메인 메뉴 씬 생성자
    public MainMenuScene(JFrame frame) {
        super(frame);
        this.frame = frame;
        this.gameSettings = GameSettings.getInstance();
        
        setupUI();
        setupKeyListener();

    }

    // UI 컴포넌트들을 초기화하고 배치하는 메서드
    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(getBackgroundColor());
        setFocusable(true);
        
        // 제목 패널
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // 메뉴 패널
        JPanel menuPanel = createMenuPanel();
        add(menuPanel, BorderLayout.CENTER);
        
        // 하단 정보 패널
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.SOUTH);
        
        frame.setContentPane(this);
        applyDisplaySettings();
        frame.revalidate();
        frame.repaint();
    }
    
    // 시작 화면 제목 패널 생성하는 메서드
    // 시작 화면 제목 패널 설정
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));

        // 해상도에 따른 폰트 크기 조정
        int[] resolution = gameSettings.getResolutionSize();
        int screenWidth = resolution[0];
        int titleFontSize = Math.max(48, screenWidth / 18);     // 최소 48px, 화면 너비에 비례
        int subtitleFontSize = Math.max(14, screenWidth / 60);  // 최소 14px, 화면 너비에 비례

        // 메인 제목
        JLabel titleLabel = new JLabel("TETRIS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, titleFontSize));
        titleLabel.setForeground(getTitleColor());
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 그림자 효과를 위한 백그라운드 제목
        JLabel shadowLabel = new JLabel("TETRIS");
        shadowLabel.setFont(new Font("Arial", Font.BOLD, titleFontSize));
        shadowLabel.setForeground(new Color(40, 40, 80));
        shadowLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 서브타이틀
        JLabel subtitleLabel = new JLabel("Team 1 Edition");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, subtitleFontSize));
        subtitleLabel.setForeground(getTextColor());
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(Box.createVerticalStrut(20));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(10));
        titlePanel.add(subtitleLabel);
        
        return titlePanel;
    }

    // 메뉴 버튼들을 생성하고 배치하는 메서드
    // 메뉴 버튼 패널 설정
    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        // 메뉴 버튼들
        String[] buttonTexts = {"Start Game", "Settings", "Score", "Exit"};
        menuButtons = new JButton[buttonTexts.length];
        
        // 해상도에 따른 버튼 간격 조정 (고정된 작은 간격)
        int buttonSpacing = 20; // 고정된 간격
        
        for (int i = 0; i < buttonTexts.length; i++) {
            menuButtons[i] = createMenuButton(buttonTexts[i], i);
            
            gbc.gridy = i;
            gbc.insets = new Insets(buttonSpacing / 2, 0, buttonSpacing / 2, 0);
            
            menuPanel.add(menuButtons[i], gbc);
        }
        
        updateButtonAppearance();
        
        return menuPanel;
    }

    // 개별 메뉴 버튼을 생성하고 스타일을 설정하는 메서드
    private JButton createMenuButton(String text, int index) {
        JButton button = new JButton(text);
        
        // 해상도에 따른 버튼 크기 조정 (더 합리적인 크기)
        int[] resolution = gameSettings.getResolutionSize();
        int screenWidth = resolution[0];
        int screenHeight = resolution[1];
        
        // 해상도에 따른 동적 크기 조정 (더 합리적인 크기)
        int buttonWidth = Math.max(200, Math.min(400, screenWidth / 3));  // 최소 200px, 최대 400px
        int buttonHeight = Math.max(50, Math.min(80, screenHeight / 12)); // 최소 50px, 최대 80px
        int fontSize = Math.max(18, Math.min(28, screenWidth / 30));      // 최소 18px, 최대 28px
        
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        button.setMinimumSize(new Dimension(buttonWidth, buttonHeight));
        button.setMaximumSize(new Dimension(buttonWidth, buttonHeight));

        button.setFocusable(false);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // 액션 리스너 추가
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleMenuSelection(index);
                MainMenuScene.this.requestFocusInWindow();
            }
        });
        
        return button;
    }

    // 하단에 조작키 안내 정보를 표시하는 패널 생성 메서드
    // 시작 메뉴 하단 조작키 안내
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // 해상도에 따른 폰트 크기 조정
        int[] resolution = gameSettings.getResolutionSize();
        int screenWidth = resolution[0];
        int infoFontSize = Math.max(14, screenWidth / 50);  // 최소 14px, 화면 너비에 비례
        
        JLabel infoLabel = new JLabel("↑↓ 키로 선택, Enter로 확인, ESC로 종료");
        infoLabel.setFont(new Font("Malgun Gothic", Font.BOLD, infoFontSize));
        infoLabel.setForeground(getTextColor());
        
        infoPanel.add(infoLabel);
        
        return infoPanel;
    }

    // 키보드 입력을 위한 리스너를 설정하는 메서드
    private void setupKeyListener() {
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
    }

    // 선택된 버튼의 외관을 업데이트하는 메서드
    private void updateButtonAppearance() {
        for (int i = 0; i < menuButtons.length; i++) {
            if (i == selectedButton) {
                menuButtons[i].setBackground(getSelectedButtonColor());
                menuButtons[i].setForeground(Color.WHITE);
            } else {
                menuButtons[i].setBackground(getButtonColor());
                menuButtons[i].setForeground(getTextColor());
            }
        }
        repaint();
    }

    // GameSettings에서 화면 크기와 모드를 적용하는 메서드
    private void applyDisplaySettings() {
        int[] size = gameSettings.getResolutionSize();
        int width = size[0];
        int height = size[1];
        
        // 전체화면/창모드 적용
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

    // 메뉴 선택 시 해당하는 동작을 수행하는 메서드
    private void handleMenuSelection(int index) {
        selectedButton = index;
        updateButtonAppearance();
        
        switch (index) {
            case 0: // 게임 시작
                startGame();
                break;
            case 1: // 설정
                showSettings();
                break;
            case 2: // 점수 기록
                showScores();
                break;
            case 3: // 종료
                quitGame();
                break;
            default:
            // do nothing
                break;
        }
    }

    // 게임을 시작하는 메서드 - 게임모드 선택 다이얼로그 표시
    private void startGame() {
        showGameModeDialog();
    }
    
    /**
     * 게임 모드 선택 다이얼로그를 표시합니다.
     */
    private void showGameModeDialog() {
        // 커스텀 다이얼로그 생성 (제목표시줄 없음)
        JDialog modeDialog = new JDialog(frame, true);
        modeDialog.setUndecorated(true); // 제목표시줄 제거
        modeDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        modeDialog.setResizable(false);
        
        // 해상도에 따른 다이얼로그 크기 조정
        int[] resolution = gameSettings.getResolutionSize();
        int screenWidth = resolution[0];
        int screenHeight = resolution[1];
        
        int dialogWidth = Math.max(300, Math.min(400, screenWidth / 2));
        int dialogHeight = Math.max(200, Math.min(300, screenHeight / 3));
        
        // 다이얼로그 크기 설정
        modeDialog.setSize(dialogWidth, dialogHeight);
        modeDialog.setLocationRelativeTo(frame);
        
        // 다이얼로그 내용 패널 설정
        JPanel dialogPanel = new JPanel();
        dialogPanel.setBackground(getBackgroundColor());
        dialogPanel.setLayout(new BorderLayout());
        dialogPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(getTitleColor(), 2), // 테두리 추가
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // 제목 라벨
        JLabel titleLabel = new JLabel("게임 모드 선택", SwingConstants.CENTER);
        int titleFontSize = Math.max(16, screenWidth / 50);
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, titleFontSize));
        titleLabel.setForeground(getTitleColor());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(3, 1, 0, 10));
        
        // Regular Mode 버튼
        JButton regularButton = createDialogButton("Regular Mode");
        regularButton.addActionListener(e -> {
            modeDialog.dispose();
            // 난이도 선택 다이얼로그 표시
            System.out.println("Starting Regular Mode game...");
            Game.setScene(new GameScene(frame, gameSettings.getDifficulty()));
        });
        
        // Item Mode 버튼 (활성화)
        JButton itemButton = createDialogButton("Item Mode");
        itemButton.addActionListener(e -> {
            modeDialog.dispose();
            // 해상도 설정을 유지하며 ItemGameScene으로 전환
            System.out.println("Starting Item Mode game...");
            Game.setScene(new ItemGameScene(frame));
        });
        itemButton.setToolTipText("폭탄 아이템과 함께하는 테트리스!");
        
        // 취소 버튼
        JButton cancelButton = new JButton("취소");
        cancelButton.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        cancelButton.setPreferredSize(new Dimension(250, 35));
        Color cancelColor = new Color(100, 50, 50);
        cancelButton.setBackground(cancelColor);
        cancelButton.setForeground(getTextColor());
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(true);
        cancelButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // 취소 버튼 전용 호버 효과 (빨간색 유지)
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(getSelectedButtonColor());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(cancelColor);
            }
        });
        
        cancelButton.addActionListener(e -> {
            System.out.println("Game start cancelled.");
            modeDialog.dispose();
        });
        
        buttonPanel.add(regularButton);
        buttonPanel.add(itemButton);
        buttonPanel.add(cancelButton);
        
        // 버튼 배열 (키보드 네비게이션용)
        JButton[] buttons = {regularButton, itemButton, cancelButton};
        final int[] currentIndex = {0}; // 현재 선택된 버튼 인덱스
        
        // 초기 포커스 설정
        buttons[0].setBackground(getSelectedButtonColor());
        
        // 설명 라벨
        JLabel descLabel = new JLabel("<html><center>Regular Mode: 클래식 테트리스<br>Item Mode: 준비중</center></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 11));
        descLabel.setForeground(Color.LIGHT_GRAY);
        
        // 컴포넌트 배치
        dialogPanel.add(titleLabel, BorderLayout.NORTH);
        dialogPanel.add(buttonPanel, BorderLayout.CENTER);
        dialogPanel.add(descLabel, BorderLayout.SOUTH);
        
        modeDialog.add(dialogPanel);
        
        // 키보드 네비게이션 추가
        modeDialog.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int keyCode = e.getKeyCode();
                
                if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
                    modeDialog.dispose();
                } else if (keyCode == java.awt.event.KeyEvent.VK_UP || keyCode == java.awt.event.KeyEvent.VK_LEFT) {
                    // 이전 버튼으로 이동
                    if (buttons[currentIndex[0]] == cancelButton) {
                        buttons[currentIndex[0]].setBackground(cancelColor);
                    } else {
                        buttons[currentIndex[0]].setBackground(getButtonColor());
                    }
                    currentIndex[0] = (currentIndex[0] - 1 + buttons.length) % buttons.length;
                    if (buttons[currentIndex[0]] == cancelButton) {
                        buttons[currentIndex[0]].setBackground(getSelectedButtonColor());
                    } else {
                        buttons[currentIndex[0]].setBackground(getSelectedButtonColor());
                    }
                } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN || keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
                    // 다음 버튼으로 이동
                    if (buttons[currentIndex[0]] == cancelButton) {
                        buttons[currentIndex[0]].setBackground(cancelColor);
                    } else {
                        buttons[currentIndex[0]].setBackground(getButtonColor());
                    }
                    currentIndex[0] = (currentIndex[0] + 1) % buttons.length;
                    if (buttons[currentIndex[0]] == cancelButton) {
                        buttons[currentIndex[0]].setBackground(getSelectedButtonColor());
                    } else {
                        buttons[currentIndex[0]].setBackground(getSelectedButtonColor());
                    }
                } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                    // 현재 선택된 버튼 클릭
                    buttons[currentIndex[0]].doClick();
                }
            }
        });
        
        // 다이얼로그가 포커스를 받을 수 있도록 설정
        modeDialog.setFocusable(true);
        
        // 다이얼로그 표시
        modeDialog.setVisible(true);
        modeDialog.requestFocus(); // 포커스 요청
    }
    
    /**
     * 다이얼로그용 버튼을 생성합니다.
     */
    private JButton createDialogButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(250, 35));
        button.setBackground(getButtonColor());
        button.setForeground(getTextColor());
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // 호버 효과 (활성화된 버튼만)
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(getSelectedButtonColor());
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(getButtonColor());
                }
            }
        });
        
        return button;
    }

    /**
     * 난이도 선택 다이얼로그를 표시합니다.
     * 10/13 해성: 난이도 선택을 setting 에서 받아오는 것으로 변경합니다.
     */
    // private void showDifficultyDialog() {
    //     // 커스텀 다이얼로그 생성
    //     JDialog difficultyDialog = new JDialog(frame, true);
    //     difficultyDialog.setUndecorated(true);
    //     difficultyDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    //     difficultyDialog.setResizable(false);
        
    //     // 해상도에 따른 다이얼로그 크기 조정
    //     int[] resolution = gameSettings.getResolutionSize();
    //     int screenWidth = resolution[0];
    //     int screenHeight = resolution[1];
        
    //     int dialogWidth = Math.max(300, Math.min(400, screenWidth / 2));
    //     int dialogHeight = Math.max(250, Math.min(350, screenHeight / 3));
        
    //     difficultyDialog.setSize(dialogWidth, dialogHeight);
    //     difficultyDialog.setLocationRelativeTo(frame);
        
    //     // 다이얼로그 내용 패널 설정
    //     JPanel dialogPanel = new JPanel();
    //     dialogPanel.setBackground(new Color(30, 30, 50));
    //     dialogPanel.setLayout(new BorderLayout());
    //     dialogPanel.setBorder(BorderFactory.createCompoundBorder(
    //         BorderFactory.createLineBorder(new Color(255, 255, 100), 2),
    //         BorderFactory.createEmptyBorder(15, 15, 15, 15)
    //     ));
        
    //     // 제목 라벨
    //     JLabel titleLabel = new JLabel("난이도 선택", SwingConstants.CENTER);
    //     int titleFontSize = Math.max(16, screenWidth / 50);
    //     titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, titleFontSize));
    //     titleLabel.setForeground(new Color(255, 255, 100));
    //     titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
    //     // 버튼 패널
    //     JPanel buttonPanel = new JPanel();
    //     buttonPanel.setOpaque(false);
    //     buttonPanel.setLayout(new GridLayout(4, 1, 0, 10));
        
    //     // Easy 버튼
    //     JButton easyButton = createDialogButton("Easy");
    //     easyButton.addActionListener(e -> {
            
    //     });
        
    //     // Normal 버튼
    //     JButton normalButton = createDialogButton("Normal");
    //     normalButton.addActionListener(e -> {
    //         gameSettings.setDifficulty(GameSettings.Difficulty.NORMAL);
    //         difficultyDialog.dispose();
    //         Game.setScene(new GameScene(frame, gameSettings.getDifficulty()));
    //     });
        
    //     // Hard 버튼
    //     JButton hardButton = createDialogButton("Hard");
    //     hardButton.addActionListener(e -> {
    //         gameSettings.setDifficulty(GameSettings.Difficulty.HARD);
    //         difficultyDialog.dispose();
    //         Game.setScene(new GameScene(frame, gameSettings.getDifficulty()));
    //     });
        
    //     // 취소 버튼
    //     JButton cancelButton = createDialogButton("취소");
    //     cancelButton.setBackground(new Color(100, 50, 50));
    //     cancelButton.addActionListener(e -> {
    //         difficultyDialog.dispose();
    //     });
        
    //     buttonPanel.add(easyButton);
    //     buttonPanel.add(normalButton);
    //     buttonPanel.add(hardButton);
    //     buttonPanel.add(cancelButton);
        
    //     // 설명 라벨
    //     JLabel descLabel = new JLabel("<html><center>Easy: 쉬운 난이도<br>Normal: 보통 난이도<br>Hard: 어려운 난이도</center></html>", SwingConstants.CENTER);
    //     descLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 11));
    //     descLabel.setForeground(Color.LIGHT_GRAY);
        
    //     // 컴포넌트 배치
    //     dialogPanel.add(titleLabel, BorderLayout.NORTH);
    //     dialogPanel.add(buttonPanel, BorderLayout.CENTER);
    //     dialogPanel.add(descLabel, BorderLayout.SOUTH);
        
    //     difficultyDialog.add(dialogPanel);
        
    //     // ESC 키로 다이얼로그 닫기
    //     difficultyDialog.addKeyListener(new java.awt.event.KeyAdapter() {
    //         @Override
    //         public void keyPressed(java.awt.event.KeyEvent e) {
    //             if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
    //                 difficultyDialog.dispose();
    //             }
    //         }
    //     });
        
    //     difficultyDialog.setFocusable(true);
    //     difficultyDialog.setVisible(true);
    //     difficultyDialog.requestFocus();
    // }

    // 설정 메뉴를 표시하는 메서드
    private void showSettings() {
        Game.setScene(new SettingsScene(frame));
    }

    // 점수 기록을 표시하는 메서드 (현재 미구현)
    private void showScores() {
        Game.setScene(new ScoreScene(frame));
        // JOptionPane.showMessageDialog(this, 
        //     "점수 기록 메뉴는 아직 구현되지 않았습니다.\n추후 업데이트 예정입니다.", 
        //     "점수 기록", 
        //     JOptionPane.INFORMATION_MESSAGE);
    }

    // 게임 종료 확인 다이얼로그를 표시하는 메서드
    private void quitGame() {
        JOptionPane pane = new JOptionPane(
            "정말로 게임을 종료하시겠습니까?",
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.YES_NO_OPTION
        );
        JDialog dialog = pane.createDialog(this, "종료 확인");
        dialog.setModal(true);

        JComponent target = dialog.getRootPane();
        InputMap im = target.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = target.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "focusPrev");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),   "focusPrev");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "focusNext");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),  "focusNext");

        am.put("focusPrev", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusPreviousComponent();
            }
        });
        am.put("focusNext", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "pressFocused");
        am.put("pressFocused", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (fo instanceof JButton) {
                    ((JButton) fo).doClick();
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        am.put("cancel", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                pane.setValue(JOptionPane.NO_OPTION);
                dialog.dispose();
            }
        });

        dialog.setVisible(true);

        Object val = pane.getValue();
        int choice = (val instanceof Integer) ? (Integer) val : JOptionPane.CLOSED_OPTION;

        
        // int choice = JOptionPane.showConfirmDialog(this, 
        //     "정말로 게임을 종료하시겠습니까?", 
        //     "종료 확인", 
        //     JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            Game.quit();
        }
    }

    // 키보드 키가 눌렸을 때 처리하는 메서드
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                selectedButton = (selectedButton - 1 + menuButtons.length) % menuButtons.length;
                updateButtonAppearance();
                break;
            case KeyEvent.VK_DOWN:
                selectedButton = (selectedButton + 1) % menuButtons.length;
                updateButtonAppearance();
                break;
            case KeyEvent.VK_ENTER:
                handleMenuSelection(selectedButton);
                break;
            case KeyEvent.VK_ESCAPE:
                quitGame();
                break;
        }
    }

    // 키보드 키가 떼어졌을 때 처리하는 메서드 (현재 미사용)
    @Override
    public void keyReleased(KeyEvent e) {
        // 필요시 구현
    }

    // 키보드 키가 타이핑되었을 때 처리하는 메서드 (현재 미사용)
    @Override
    public void keyTyped(KeyEvent e) {
        // 필요시 구현
    }

    // 테마 색상을 업데이트하는 메서드 (UI 재구성 없이 색상만 변경)
    private void updateThemeColors() {
        // 배경색 업데이트
        setBackground(getBackgroundColor());
        
        // 버튼 색상 업데이트
        updateButtonAppearance();
        
        // 화면 갱신
        repaint();
    }

    // 씬이 활성화될 때 호출되는 메서드
    @Override
    public void onEnter() {
        updateThemeColors(); // 색약 모드 변경사항 적용 (색상만)
        applyDisplaySettings();
        setFocusable(true);
        requestFocusInWindow();
        
        this.bgm = new Sound("bit-bit-loop-127680.mp3");
        this.bgm.play(true);
    }

    @Override
    public void onExit() {
        bgm.release();
    }
}