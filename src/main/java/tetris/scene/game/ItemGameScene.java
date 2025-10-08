package tetris.scene.game;

import tetris.scene.game.core.*;
import javax.swing.*;
import java.awt.*;

/**
 * 아이템 모드 테트리스 게임 씬
 * 10줄마다 폭탄 아이템 블록이 등장합니다.
 */
public class ItemGameScene extends GameScene {
    private ItemManager itemManager;
    
    public ItemGameScene(JFrame frame) {
        super(frame);
        
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
    private BlockManager getBlockManager() {
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
    private BoardManager getBoardManager() {
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
            System.out.println("ItemGameScene: Notifying ItemManager of " + linesCleared + " lines cleared");
            itemManager.onLinesCleared(linesCleared);
        }
    }
    
    /**
     * 필요시 다음 블록을 즉시 폭탄 블록으로 생성
     * @param linesCleared 이번에 삭제된 줄 수 (사용하지 않음 - 누적으로 판단)
     */
    @Override
    protected void forceCreateItemBlockIfNeeded(int linesCleared) {
        BlockManager blockManager = getBlockManager();
        if (blockManager != null) {
            System.out.println("ItemGameScene: Checking if item block should be created (Lines this turn: " + linesCleared + ")");
            blockManager.forceCreateItemBlock();
        }
    }
}