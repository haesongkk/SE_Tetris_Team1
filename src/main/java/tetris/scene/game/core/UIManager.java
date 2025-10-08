package tetris.scene.game.core;

import tetris.GameSettings;
import tetris.ColorBlindHelper;

import javax.swing.*;
import java.awt.*;

/**
 * 게임 UI 초기화 및 관리를 담당하는 클래스
 * - 컴포넌트 생성 및 배치
 * - 색상 테마 적용
 * - 레이아웃 설정
 * - 입력 시스템 연결
 */
public class UIManager {
    // 게임 영역 상수들
    private static final int GAME_HEIGHT = 20;
    private static final int GAME_WIDTH = 10;
    private static final int CELL_SIZE = 30;
    private static final int PREVIEW_SIZE = 4;
    private static final int PREVIEW_CELL_SIZE = 20;
    private static final int PREVIEW_MARGIN = 40;
    
    // UI 컴포넌트들
    private JPanel gamePanel;
    private JPanel wrapperPanel;
    
    /**
     * UI 시스템을 초기화합니다.
     */
    public void initializeUI(JPanel parentPanel, JFrame frame, InputHandler inputHandler) {
        // 기존 컴포넌트들 제거
        parentPanel.removeAll();
        
        // 기본 레이아웃 설정
        parentPanel.setLayout(new BorderLayout());
        
        // 색상 테마 적용
        applyColorTheme(parentPanel);
        
        // 게임 패널 생성 및 설정
        createGamePanel();
        
        // 래퍼 패널 생성 및 배치
        createWrapperPanel(parentPanel);
        
        // 입력 시스템 설정
        setupInputSystem(parentPanel, inputHandler);
        
        // 프레임 설정
        setupFrame(frame, parentPanel);
    }
    
    /**
     * 색상 테마를 적용합니다.
     */
    private void applyColorTheme(JPanel parentPanel) {
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        Color backgroundColor = ColorBlindHelper.getBackgroundColor(colorBlindMode);
        parentPanel.setBackground(backgroundColor);
    }
    
    /**
     * 게임 패널을 생성합니다.
     */
    private void createGamePanel() {
        // GamePanel은 GameScene의 내부 클래스이므로 여기서는 JPanel로 생성
        gamePanel = new JPanel();
        
        // 다음 블록 미리보기 공간을 포함한 크기로 조정
        Dimension panelSize = calculateGamePanelSize();
        gamePanel.setPreferredSize(panelSize);
        gamePanel.setBackground(Color.BLACK);
    }
    
    /**
     * 래퍼 패널을 생성하고 배치합니다.
     */
    private void createWrapperPanel(JPanel parentPanel) {
        // 게임 패널을 중앙에 배치하기 위한 래퍼 패널 생성
        wrapperPanel = new JPanel(new GridBagLayout());
        
        // 배경색 적용
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        Color backgroundColor = ColorBlindHelper.getBackgroundColor(colorBlindMode);
        wrapperPanel.setBackground(backgroundColor);
        
        wrapperPanel.add(gamePanel, new GridBagConstraints());
        parentPanel.add(wrapperPanel, BorderLayout.CENTER);
    }
    
    /**
     * 입력 시스템을 설정합니다.
     */
    private void setupInputSystem(JPanel parentPanel, InputHandler inputHandler) {
        // InputHandler를 키 리스너로 등록
        parentPanel.addKeyListener(inputHandler);
        parentPanel.setFocusable(true);
    }
    
    /**
     * 프레임을 설정합니다.
     */
    private void setupFrame(JFrame frame, JPanel parentPanel) {
        // 프레임에 이 Scene을 설정
        frame.setContentPane(parentPanel);
        frame.revalidate();
        frame.repaint();
        
        // 창을 화면 중앙에 위치시키기
        frame.setLocationRelativeTo(null);
    }
    
    /**
     * 게임 패널의 크기를 계산합니다.
     */
    private Dimension calculateGamePanelSize() {
        int previewWidth = PREVIEW_SIZE * PREVIEW_CELL_SIZE + PREVIEW_MARGIN;
        return new Dimension(
            (GAME_WIDTH + 2) * CELL_SIZE + previewWidth,
            (GAME_HEIGHT + 2) * CELL_SIZE
        );
    }
    
    /**
     * 포커스를 요청합니다.
     */
    public void requestFocus(JPanel parentPanel) {
        parentPanel.requestFocusInWindow();
    }
    
    /**
     * 게임 패널을 교체합니다 (GameScene의 내부 클래스 사용을 위해).
     */
    public void replaceGamePanel(JPanel newGamePanel) {
        if (wrapperPanel != null && gamePanel != null) {
            wrapperPanel.remove(gamePanel);
            
            // 새 패널 설정
            Dimension panelSize = calculateGamePanelSize();
            newGamePanel.setPreferredSize(panelSize);
            newGamePanel.setBackground(Color.BLACK);
            
            wrapperPanel.add(newGamePanel, new GridBagConstraints());
            this.gamePanel = newGamePanel;
            
            wrapperPanel.revalidate();
            wrapperPanel.repaint();
        }
    }
    
    /**
     * 현재 게임 패널을 반환합니다.
     */
    public JPanel getGamePanel() {
        return gamePanel;
    }
    
    /**
     * 래퍼 패널을 반환합니다.
     */
    public JPanel getWrapperPanel() {
        return wrapperPanel;
    }
    
    /**
     * UI 리소스를 정리합니다.
     */
    public void cleanup() {
        gamePanel = null;
        wrapperPanel = null;
    }
}