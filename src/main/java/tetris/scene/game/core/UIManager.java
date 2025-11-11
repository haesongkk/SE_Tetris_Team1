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
    private static final int PREVIEW_SIZE = 4;
    private static final int PREVIEW_MARGIN = 40;
    
    // 동적 크기 변수들
    private int cellSize;
    private int previewCellSize;
    
    // UI 컴포넌트들
    private JPanel gamePanel;
    private JPanel wrapperPanel;
    private JFrame frame; // 실제 창 크기 참조용
    
    /**
     * UI 시스템을 초기화합니다.
     */
    public void initializeUI(JPanel parentPanel, JFrame frame, InputHandler inputHandler) {
        // 프레임 참조 저장
        this.frame = frame;
        
        // 기존 컴포넌트들 제거
        parentPanel.removeAll();
        
        // 기본 레이아웃 설정
        parentPanel.setLayout(new BorderLayout());
        
        // 색상 테마 적용
        applyColorTheme(parentPanel);
        
        // 래퍼 패널 먼저 생성 (크기 계산에 필요)
        wrapperPanel = new JPanel(new GridBagLayout());
        int colorBlindMode = GameSettings.getInstance().getColorBlindMode();
        Color backgroundColor = ColorBlindHelper.getBackgroundColor(colorBlindMode);
        wrapperPanel.setBackground(backgroundColor);
        parentPanel.add(wrapperPanel, BorderLayout.CENTER);
        
        // 해상도에 따른 동적 크기 계산 (wrapperPanel 생성 후)
        calculateDynamicSizes();
        
        // 게임 패널 생성 및 설정
        createGamePanel();
        
        // 게임 패널을 래퍼에 추가
        wrapperPanel.add(gamePanel, new GridBagConstraints());
        
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
     * P2PScene에서 사용하기 위해 public으로 변경합니다.
     */
    public Dimension calculateGamePanelSize() {
        int previewWidth = PREVIEW_SIZE * previewCellSize + PREVIEW_MARGIN;
        return new Dimension(
            (GAME_WIDTH + 2) * cellSize + previewWidth,
            (GAME_HEIGHT + 2) * cellSize
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
    
    /**
     * 해상도에 따른 동적 크기를 계산합니다.
     * 실제 창 크기를 기준으로 보드가 창에 맞도록 셀 크기를 계산합니다.
     */
    private void calculateDynamicSizes() {
        int screenWidth, screenHeight;
        
        // wrapperPanel의 실제 크기를 사용 (최대화 시 실제 사용 가능한 공간)
        if (wrapperPanel != null && wrapperPanel.getWidth() > 0 && wrapperPanel.getHeight() > 0) {
            // wrapperPanel이 이미 렌더링되었으면 실제 크기 사용
            screenWidth = wrapperPanel.getWidth();
            screenHeight = wrapperPanel.getHeight();
            System.out.println("Using wrapperPanel size: " + screenWidth + "x" + screenHeight);
        } else if (frame != null) {
            // wrapperPanel이 아직 렌더링 안 됐으면 프레임 크기에서 추정
            Dimension frameSize = frame.getSize();
            Insets insets = frame.getInsets();
            screenWidth = frameSize.width - insets.left - insets.right;
            screenHeight = frameSize.height - insets.top - insets.bottom;
            System.out.println("Using frame size: " + screenWidth + "x" + screenHeight);
        } else {
            // 프레임 참조가 없으면 설정된 해상도 사용 (폴백)
            int[] resolution = GameSettings.getInstance().getResolutionSize();
            screenWidth = resolution[0];
            screenHeight = resolution[1];
            System.out.println("Using resolution setting: " + screenWidth + "x" + screenHeight);
        }
        
        // 세로 기준으로 셀 크기 계산 (보드 높이 + 경계 = 22셀)
        // 약간의 여유 공간을 남겨둠 (95% 사용)
        int availableHeight = (int) (screenHeight * 0.95);
        int calculatedCellSize = availableHeight / (GAME_HEIGHT + 2); // 20 + 2(경계) = 22
        
        // 최대 셀 크기 제한 (1920x1080 최대화 시에도 안전하게)
        int maxCellSize = 35;
        cellSize = Math.min(maxCellSize, Math.max(15, calculatedCellSize));
        
        // 가로 크기도 체크해서 화면을 벗어나지 않도록 조정 (95% 사용)
        int availableWidth = (int) (screenWidth * 0.95);
        int previewWidth = PREVIEW_SIZE * (cellSize * 2 / 3) + PREVIEW_MARGIN;
        int requiredWidth = (GAME_WIDTH + 2) * cellSize + previewWidth;
        if (requiredWidth > availableWidth) {
            // 가로가 넘치면 cellSize를 줄임
            cellSize = (availableWidth - PREVIEW_MARGIN) / (GAME_WIDTH + 2 + PREVIEW_SIZE * 2 / 3);
            cellSize = Math.min(maxCellSize, Math.max(15, cellSize));
        }
        
        // 미리보기 셀 크기는 메인 셀의 2/3 정도
        previewCellSize = Math.max(10, cellSize * 2 / 3);
        
        System.out.println("Final cell size: " + cellSize + ", Preview cell size: " + previewCellSize);
    }
    
    /**
     * 창 크기 변경 시 게임 보드 크기를 다시 계산합니다.
     * 창모드에서 창을 확장했을 때 호출됩니다.
     */
    public void recalculateSizes() {
        // wrapperPanel이 리사이즈된 후에 크기를 계산하도록 지연
        if (wrapperPanel != null) {
            wrapperPanel.revalidate();
            SwingUtilities.invokeLater(() -> {
                calculateDynamicSizes();
                
                // 게임 패널 크기 업데이트
                if (gamePanel != null) {
                    Dimension newSize = calculateGamePanelSize();
                    gamePanel.setPreferredSize(newSize);
                    gamePanel.setSize(newSize);
                    gamePanel.revalidate();
                    gamePanel.repaint();
                }
                
                if (wrapperPanel != null) {
                    wrapperPanel.repaint();
                }
            });
        } else {
            // wrapperPanel이 없으면 바로 계산
            calculateDynamicSizes();
            if (gamePanel != null) {
                Dimension newSize = calculateGamePanelSize();
                gamePanel.setPreferredSize(newSize);
                gamePanel.setSize(newSize);
            }
        }
    }
    
    /**
     * 동적 셀 크기를 반환합니다.
     */
    public int getCellSize() {
        return cellSize;
    }
    
    /**
     * 동적 미리보기 셀 크기를 반환합니다.
     */
    public int getPreviewCellSize() {
        return previewCellSize;
    }
}