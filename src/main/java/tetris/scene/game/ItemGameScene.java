package tetris.scene.game;

import tetris.scene.game.core.*;
import tetris.scene.game.overlay.GameOver;
import javax.swing.*;
import java.awt.*;

/**
 * 아이템 모드 테트리스 게임 씬
 * 10줄마다 폭탄 아이템 블록이 등장합니다.
 */
public class ItemGameScene extends GameScene {
    private ItemManager itemManager;
    private boolean isItemLineClear = false; // 아이템으로 인한 줄 삭제인지 추적
    
    public ItemGameScene(JFrame frame) {
        super(frame, tetris.GameSettings.Difficulty.NORMAL);
        
        // 아이템 매니저 초기화
        itemManager = new ItemManager();
        
        // UI에 아이템 정보 표시 추가
        addItemModeIndicator();
        
        // BlockManager 설정을 나중에 수행 (초기화 완료 후)
        SwingUtilities.invokeLater(() -> {
            BlockManager blockManager = getBlockManager();
            if (blockManager != null) {
                blockManager.setItemManager(itemManager);
                System.out.println("Item mode initialized with BlockManager!");
            } else {
                System.err.println("Failed to get BlockManager for item mode");
            }
            
            // BoardManager에도 ItemManager 설정
            BoardManager boardManager = getBoardManager();
            if (boardManager != null) {
                boardManager.setItemManager(itemManager);
                System.out.println("Item mode initialized with BoardManager!");
            } else {
                System.err.println("Failed to get BoardManager for item mode");
            }
        });
    }
    
    /**
     * 아이템 모드 표시기를 UI에 추가합니다.
     */
    private void addItemModeIndicator() {
        // 아이템 모드임을 알리는 라벨 추가
        JLabel modeLabel = new JLabel("🎁 ITEM MODE - 2줄마다 폭탄 아이템!", SwingConstants.CENTER);
        modeLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        modeLabel.setForeground(Color.ORANGE);
        modeLabel.setOpaque(true);
        modeLabel.setBackground(new Color(0, 0, 0, 150));
        modeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // 게임 패널에 오버레이로 추가 (기존 컴포넌트 위에)
        setLayout(new OverlayLayout(this));
        
        // 상단에 표시
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setOpaque(false);
        topPanel.add(modeLabel);
        
        add(topPanel);
    }
    
    /**
     * BlockManager getter 메서드 (아이템 매니저 설정용)
     */
    @Override
    public BlockManager getBlockManager() {
        // GameScene의 protected 필드에 접근 (리플렉션 사용)
        try {
            java.lang.reflect.Field field = GameScene.class.getDeclaredField("blockManager");
            field.setAccessible(true);
            return (BlockManager) field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Cannot access blockManager: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * BoardManager getter 메서드 (아이템 매니저 설정용)
     */
    @Override
    public BoardManager getBoardManager() {
        // GameScene의 protected 필드에 접근 (리플렉션 사용)
        try {
            java.lang.reflect.Field field = GameScene.class.getDeclaredField("boardManager");
            field.setAccessible(true);
            return (BoardManager) field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Cannot access boardManager: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 줄 삭제 시 ItemManager에 알림
     */
    @Override
    protected void notifyLinesCleared(int linesCleared) {
        if (itemManager != null) {
            // LINE_CLEAR 아이템으로 인한 줄 삭제인지 확인
            if (isItemLineClear) {
                System.out.println("ItemGameScene: LINE_CLEAR item caused " + linesCleared + " lines to be cleared - NOT counting for item generation");
                isItemLineClear = false; // 플래그 리셋
            } else {
                System.out.println("ItemGameScene: Notifying ItemManager of " + linesCleared + " lines cleared (natural line clearing)");
                itemManager.onLinesCleared(linesCleared);
            }
        }
    }
    
    /**
     * LINE_CLEAR 아이템이 사용되었음을 표시합니다.
     * 이후 줄 삭제는 아이템으로 인한 것으로 간주되어 아이템 생성 카운트에서 제외됩니다.
     */
    public void markItemLineClear() {
        isItemLineClear = true;
        System.out.println("ItemGameScene: Marked next line clearing as item-caused");
    }
    
    /**
     * 게임 오버 시 Item 모드 결과를 "item" 카테고리에 저장하도록 오버라이드
     */
    @Override
    public void onGameOver() {
        System.out.println("ItemGameScene: Game Over triggered - saving to item category");
        
        // 마지막 블록 정보 저장 (게임 오버 후에도 화면에 보이도록)
        try {
            java.lang.reflect.Field blockManagerField = GameScene.class.getDeclaredField("blockManager");
            blockManagerField.setAccessible(true);
            BlockManager blockManager = (BlockManager) blockManagerField.get(this);
            
            if (blockManager.getCurrentBlock() != null) {
                java.lang.reflect.Field lastBlockField = GameScene.class.getDeclaredField("lastBlock");
                java.lang.reflect.Field lastBlockXField = GameScene.class.getDeclaredField("lastBlockX");
                java.lang.reflect.Field lastBlockYField = GameScene.class.getDeclaredField("lastBlockY");
                
                lastBlockField.setAccessible(true);
                lastBlockXField.setAccessible(true);
                lastBlockYField.setAccessible(true);
                
                lastBlockField.set(this, blockManager.getCurrentBlock());
                lastBlockXField.set(this, blockManager.getX());
                lastBlockYField.set(this, blockManager.getY());
            }
        } catch (Exception e) {
            System.err.println("Failed to access GameScene fields: " + e.getMessage());
        }
        
        // Item 모드용 게임 오버 오버레이 표시
        showItemModeGameOverOverlay();
    }
    
    /**
     * Item 모드용 게임 오버 오버레이를 표시합니다.
     */
    private void showItemModeGameOverOverlay() {
        try {
            // GameScene의 필드들에 접근
            java.lang.reflect.Field frameField = GameScene.class.getDeclaredField("m_frame");
            java.lang.reflect.Field scoreManagerField = GameScene.class.getDeclaredField("scoreManager");
            java.lang.reflect.Field gameStateManagerField = GameScene.class.getDeclaredField("gameStateManager");
            
            frameField.setAccessible(true);
            scoreManagerField.setAccessible(true);
            gameStateManagerField.setAccessible(true);
            
            JFrame frame = (JFrame) frameField.get(this);
            ScoreManager scoreManager = (ScoreManager) scoreManagerField.get(this);
            GameStateManager gameStateManager = (GameStateManager) gameStateManagerField.get(this);
            
            int currentScore = scoreManager.getScore();
            int currentLines = scoreManager.getLinesCleared();
            int currentTime = gameStateManager.getElapsedTimeInSeconds();
            
            // Item 모드용 GameOver 생성 ("item" 카테고리에 저장됨)
            GameOver gameOverOverlay = new GameOver(frame, currentScore, currentLines, currentTime, "item");
            
            // 게임 종료 화면을 현재 패널에 추가
            setLayout(new OverlayLayout(this));
            add(gameOverOverlay, 0); // 맨 앞에 추가
            
            revalidate();
            repaint();
            
            System.out.println("Item Mode Game Over! Score: " + currentScore + ", Lines: " + currentLines + ", Mode: item");
        } catch (Exception e) {
            System.err.println("Failed to show item mode game over overlay: " + e.getMessage());
            // fallback: 부모 클래스의 메소드 호출
            super.onGameOver();
        }
    }
}