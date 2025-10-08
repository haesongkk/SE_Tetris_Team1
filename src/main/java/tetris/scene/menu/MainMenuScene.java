package tetris.scene.menu;

import tetris.Game;
import tetris.GameSettings;
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
    
    // 색상 정의
    private final Color BACKGROUND_COLOR = new Color(20, 20, 40);
    private final Color TITLE_COLOR = new Color(255, 255, 100);
    private final Color BUTTON_COLOR = new Color(70, 70, 120);
    private final Color SELECTED_BUTTON_COLOR = new Color(120, 120, 200);
    private final Color TEXT_COLOR = Color.WHITE;

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
        setBackground(BACKGROUND_COLOR);
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

        // 메인 제목
        JLabel titleLabel = new JLabel("TETRIS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 72));
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 그림자 효과를 위한 백그라운드 제목
        JLabel shadowLabel = new JLabel("TETRIS");
        shadowLabel.setFont(new Font("Arial", Font.BOLD, 72));
        shadowLabel.setForeground(new Color(40, 40, 80));
        shadowLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 서브타이틀
        JLabel subtitleLabel = new JLabel("Team 1 Edition");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        subtitleLabel.setForeground(TEXT_COLOR);
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
        gbc.insets = new Insets(10, 0, 10, 0);
        
        // 메뉴 버튼들
        String[] buttonTexts = {"Start Game", "Settings", "Score", "Exit"};
        menuButtons = new JButton[buttonTexts.length];
        
        for (int i = 0; i < buttonTexts.length; i++) {
            menuButtons[i] = createMenuButton(buttonTexts[i], i);
            gbc.gridy = i;
            menuPanel.add(menuButtons[i], gbc);
        }
        
        updateButtonAppearance();
        
        return menuPanel;
    }

    // 개별 메뉴 버튼을 생성하고 스타일을 설정하는 메서드
    private JButton createMenuButton(String text, int index) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setPreferredSize(new Dimension(250, 60));

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
        
        JLabel infoLabel = new JLabel("↑↓ 키로 선택, Enter로 확인, ESC로 종료");
        infoLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        infoLabel.setForeground(TEXT_COLOR);
        
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
                menuButtons[i].setBackground(SELECTED_BUTTON_COLOR);
                menuButtons[i].setForeground(Color.WHITE);
            } else {
                menuButtons[i].setBackground(BUTTON_COLOR);
                menuButtons[i].setForeground(TEXT_COLOR);
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
        
        // 다이얼로그 크기 설정 (작은 크기)
        modeDialog.setSize(350, 250);
        modeDialog.setLocationRelativeTo(frame);
        
        // 다이얼로그 내용 패널 설정
        JPanel dialogPanel = new JPanel();
        dialogPanel.setBackground(new Color(30, 30, 50));
        dialogPanel.setLayout(new BorderLayout());
        dialogPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 100), 2), // 테두리 추가
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // 제목 라벨
        JLabel titleLabel = new JLabel("게임 모드 선택", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        titleLabel.setForeground(new Color(255, 255, 100));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(3, 1, 0, 10));
        
        // Regular Mode 버튼
        JButton regularButton = createDialogButton("🎮 Regular Mode");
        regularButton.addActionListener(e -> {
            modeDialog.dispose();
            Game.setScene(new GameScene(frame));
        });
        
        // Item Mode 버튼 (활성화)
        JButton itemButton = createDialogButton("🎁 Item Mode");
        itemButton.addActionListener(e -> {
            modeDialog.dispose();
            Game.setScene(new ItemGameScene(frame));
        });
        itemButton.setToolTipText("폭탄 아이템과 함께하는 테트리스!");
        
        // 취소 버튼
        JButton cancelButton = createDialogButton("❌ 취소");
        cancelButton.setBackground(new Color(100, 50, 50));
        cancelButton.addActionListener(e -> modeDialog.dispose());
        
        buttonPanel.add(regularButton);
        buttonPanel.add(itemButton);
        buttonPanel.add(cancelButton);
        
        // 설명 라벨
        JLabel descLabel = new JLabel("<html><center>Regular Mode: 클래식 테트리스<br>Item Mode: 준비중</center></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 11));
        descLabel.setForeground(Color.LIGHT_GRAY);
        
        // 컴포넌트 배치
        dialogPanel.add(titleLabel, BorderLayout.NORTH);
        dialogPanel.add(buttonPanel, BorderLayout.CENTER);
        dialogPanel.add(descLabel, BorderLayout.SOUTH);
        
        modeDialog.add(dialogPanel);
        
        // ESC 키로 다이얼로그 닫기 기능 추가
        modeDialog.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    modeDialog.dispose();
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
        button.setBackground(new Color(70, 70, 120));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // 호버 효과 (활성화된 버튼만)
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(120, 120, 200));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(70, 70, 120));
                }
            }
        });
        
        return button;
    }

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
        int choice = JOptionPane.showConfirmDialog(this, 
            "정말로 게임을 종료하시겠습니까?", 
            "종료 확인", 
            JOptionPane.YES_NO_OPTION);
        
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

    // 씬이 활성화될 때 호출되는 메서드
    @Override
    public void onEnter() {
        applyDisplaySettings();
        requestFocusInWindow();
    }
}