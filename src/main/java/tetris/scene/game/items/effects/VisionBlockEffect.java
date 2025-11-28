package tetris.scene.game.items.effects;

import tetris.scene.game.items.*;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * 시야 제한 아이템 효과
 * 4x20 크기의 시야 제한 영역을 생성합니다.
 */
public class VisionBlockEffect extends AbstractItemEffect {
    private static final long EFFECT_DURATION = 5000; // 5초간 시야 제한
    private Object gameScene;
    private int playerNumber = 0; // 배틀 모드에서 아이템을 발동한 플레이어 번호
    private int targetPlayerNumber = 0; // 배틀 모드에서 실제 효과를 받는 플레이어 번호
    // 원본 색상을 저장하는 맵 (위치키 -> 색상)
    private final Map<String, Color> originalColors = new HashMap<>();
    
    public VisionBlockEffect() {
        super(ItemEffectType.VISION_BLOCK, EFFECT_DURATION);
    }
    
    @Override
    protected void doActivate(ItemEffectContext context) {
        this.gameScene = context.getGameScene();
        this.playerNumber = context.getPlayerNumber(); // 플레이어 번호 저장
        
        if (gameScene == null) {
            System.out.println("Vision block effect: GameScene is null");
            return;
        }
        
        try {
            // 원본 색상 저장
            saveOriginalColor(context);
            
            // 시야 제한 효과 활성화
            // BattleScene인지 확인하여 플레이어별로 처리
            String gameSceneClass = gameScene.getClass().getSimpleName();
            
            if ("BattleScene".equals(gameSceneClass) && playerNumber > 0) {
                // 배틀 모드: 상대방에게 시야 제한 적용 (1P가 발동시 2P에게, 2P가 발동시 1P에게)
                this.targetPlayerNumber = (playerNumber == 1) ? 2 : 1;
                gameScene.getClass()
                    .getMethod("setVisionBlockActive", int.class, boolean.class)
                    .invoke(gameScene, targetPlayerNumber, true);
                System.out.println("Vision block effect activated by Player " + playerNumber + " → affecting Player " + targetPlayerNumber + " in BattleScene for " + 
                                 (EFFECT_DURATION / 1000) + " seconds");
            } else {
                // 일반 모드: 자신에게 시야 제한 적용 (기존 방식)
                this.targetPlayerNumber = 0; // 일반 모드에서는 플레이어 구분 없음
                gameScene.getClass()
                    .getMethod("setVisionBlockActive", boolean.class)
                    .invoke(gameScene, true);
                System.out.println("Vision block effect activated in " + gameSceneClass + " for " + 
                                 (EFFECT_DURATION / 1000) + " seconds");
            }
            
            // 아이템 셀을 일반 블록으로 변경
            convertItemCellToNormal(context);
                             
        } catch (Exception e) {
            System.out.println("Failed to apply vision block effect: " + e.getMessage());
            e.printStackTrace();
            // 효과 적용 실패 시 즉시 비활성화
            isActive = false;
        }
    }
    
    /**
     * 아이템 위치의 원본 색상을 저장합니다.
     */
    private void saveOriginalColor(ItemEffectContext context) {
        try {
            Object boardManager = context.getBoardManager();
            int itemX = context.getItemX();
            int itemY = context.getItemY();
            
            if (boardManager != null) {
                // BoardManager의 getBoardColor를 사용해서 현재 색상을 가져옵니다
                Color currentColor = (Color) boardManager.getClass()
                    .getMethod("getBoardColor", int.class, int.class)
                    .invoke(boardManager, itemX, itemY);
                
                String posKey = itemX + "," + itemY;
                originalColors.put(posKey, currentColor);
                
                System.out.println("Saved original color at (" + itemX + "," + itemY + "): " + currentColor);
            }
        } catch (Exception e) {
            System.out.println("Failed to save original color: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDeactivate() {
        if (gameScene == null) {
            return;
        }
        
        try {
            // 시야 제한 효과 비활성화
            String gameSceneClass = gameScene.getClass().getSimpleName();
            
            if ("BattleScene".equals(gameSceneClass) && targetPlayerNumber > 0) {
                // 배틀 모드: 효과를 받았던 플레이어의 시야 제한 해제
                gameScene.getClass()
                    .getMethod("setVisionBlockActive", int.class, boolean.class)
                    .invoke(gameScene, targetPlayerNumber, false);
                System.out.println("Vision block effect ended for Player " + targetPlayerNumber + " in BattleScene (activated by Player " + playerNumber + ")");
            } else {
                // 일반 모드: 기존 방식 사용
                gameScene.getClass()
                    .getMethod("setVisionBlockActive", boolean.class)
                    .invoke(gameScene, false);
                System.out.println("Vision block effect ended in " + gameSceneClass);
            }
            
            // 원본 색상 정보 정리
            originalColors.clear();
            
        } catch (Exception e) {
            System.out.println("Failed to deactivate vision block effect: " + e.getMessage());
        }
    }
    
    /**
     * 아이템 셀을 일반 블록으로 변경합니다.
     */
    private void convertItemCellToNormal(ItemEffectContext context) {
        Object boardManager = context.getBoardManager();
        if (boardManager == null) {
            return;
        }
        
        int itemX = context.getItemX();
        int itemY = context.getItemY();
        
        try {
            // 원본 블록의 색상 정보 가져오기
            Color originalColor = getOriginalBlockColor(context);
            System.out.println("Original color retrieved: " + originalColor);
            
            // 1단계: 아이템 셀을 일반 셀로 변경 (아이템 정보 완전 제거)
            boardManager.getClass()
                .getMethod("setItemCell", int.class, int.class, boolean.class)
                .invoke(boardManager, itemX, itemY, false);
            
            // 2단계: 원본 블록 색상 복원
            if (originalColor != null) {
                boardManager.getClass()
                    .getMethod("setBoardColor", int.class, int.class, java.awt.Color.class)
                    .invoke(boardManager, itemX, itemY, originalColor);
                System.out.println("Board color set to: " + originalColor);
            } else {
                // 원본 색상을 찾을 수 없으면 기본 회색으로 설정
                Color defaultColor = new Color(128, 128, 128);
                boardManager.getClass()
                    .getMethod("setBoardColor", int.class, int.class, java.awt.Color.class)
                    .invoke(boardManager, itemX, itemY, defaultColor);
                System.out.println("Warning: Original color is null, using default gray color");
            }
            
            // 3단계: 강제로 아이템 블록 정보 제거 (확실한 제거)
            boardManager.getClass()
                .getMethod("clearItemBlockInfo", int.class, int.class)
                .invoke(boardManager, itemX, itemY);
            
            // 4단계: 제거 확인 및 강제 재설정
            Object itemInfo = boardManager.getClass()
                .getMethod("getItemBlockInfo", int.class, int.class)
                .invoke(boardManager, itemX, itemY);
            
            boolean isItemCell = (Boolean) boardManager.getClass()
                .getMethod("isItemCell", int.class, int.class)
                .invoke(boardManager, itemX, itemY);
            
            System.out.println("After conversion - isItemCell: " + isItemCell + ", itemInfo: " + (itemInfo != null));
            
            // 완전히 제거될 때까지 반복 시도
            int retryCount = 0;
            while ((itemInfo != null || isItemCell) && retryCount < 3) {
                retryCount++;
                System.out.println("Retry " + retryCount + ": Item info still exists, force clearing again.");
                
                // 다시 한번 강제 제거
                boardManager.getClass()
                    .getMethod("setItemCell", int.class, int.class, boolean.class)
                    .invoke(boardManager, itemX, itemY, false);
                boardManager.getClass()
                    .getMethod("clearItemBlockInfo", int.class, int.class)
                    .invoke(boardManager, itemX, itemY);
                
                // 다시 확인
                itemInfo = boardManager.getClass()
                    .getMethod("getItemBlockInfo", int.class, int.class)
                    .invoke(boardManager, itemX, itemY);
                isItemCell = (Boolean) boardManager.getClass()
                    .getMethod("isItemCell", int.class, int.class)
                    .invoke(boardManager, itemX, itemY);
            }
            
            if (itemInfo != null || isItemCell) {
                System.out.println("ERROR: Failed to completely remove item info after 3 retries!");
            } else {
                System.out.println("SUCCESS: Item info completely removed after " + retryCount + " retries");
            }
            
            System.out.println("Converted vision block item cell to normal with original color at (" + itemX + ", " + itemY + ")");
            
            // 5단계: 화면 즉시 갱신 요청 (GameScene repaint 호출)
            Object gameScene = context.getGameScene();
            if (gameScene != null) {
                try {
                    gameScene.getClass().getMethod("repaint").invoke(gameScene);
                    System.out.println("Requested screen refresh after item cell conversion");
                } catch (Exception repaintException) {
                    System.out.println("Failed to refresh screen: " + repaintException.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println("Failed to convert item cell to normal: " + e.getMessage());
        }
    }
    
    /**
     * 저장된 원본 블록의 색상을 가져옵니다.
     */
    private Color getOriginalBlockColor(ItemEffectContext context) {
        int itemX = context.getItemX();
        int itemY = context.getItemY();
        String posKey = itemX + "," + itemY;
        
        Color savedColor = originalColors.get(posKey);
        if (savedColor != null) {
            System.out.println("Retrieved saved original color at (" + itemX + "," + itemY + "): " + savedColor);
            return savedColor;
        }
        
        System.out.println("No saved original color found at (" + itemX + "," + itemY + ")");
        return null;
    }
}