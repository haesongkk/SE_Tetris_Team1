package tetris.scene.game.core;

import tetris.scene.game.blocks.Block;
import tetris.scene.game.blocks.ItemBlock;
import tetris.scene.game.items.ItemEffect;
import tetris.scene.game.items.ItemEffectContext;
import tetris.scene.game.items.ItemEffectFactory;
import tetris.scene.game.items.ItemEffectType;
import java.awt.Color;

/**
 * 테트리스 게임 보드 관리를 담당하는 클래스
 * 보드 상태, 줄 완성 확인, 줄 삭제 등의 로직을 처리합니다.
 */
public class BoardManager {
    private static final int GAME_HEIGHT = 20;
    private static final int GAME_WIDTH = 10;
    
    private int[][] board; // 게임 보드 상태 (0: 빈칸, 1: 블록 있음)
    private Color[][] boardColors; // 각 셀의 색상 정보
    private int[][] boardTypes; // 각 셀의 블록 타입 정보 (패턴 그리기용)
    private boolean[][] bombCells; // 폭탄 셀 정보 (아이템 모드용)
    private boolean[][] itemCells; // 아이템 셀 정보 (ItemBlock용)
    private ItemBlock[][] itemBlockInfo; // 아이템 블록 정보 저장 (이미지 렌더링용)
    private ItemManager itemManager; // 아이템 모드 관리자 (null이면 일반 모드)
    private Object gameScene; // GameScene 참조 (아이템 효과용)
    private Object blockManager; // BlockManager 참조 (아이템 효과용)
    private int playerNumber = 0; // 배틀 모드에서 플레이어 번호 (0=Player1, 1=Player2)
    
    public BoardManager() {
        initializeBoard();
    }
    
    /**
     * 게임 보드를 초기화합니다.
     */
    private void initializeBoard() {
        board = new int[GAME_HEIGHT][GAME_WIDTH];
        boardColors = new Color[GAME_HEIGHT][GAME_WIDTH];
        boardTypes = new int[GAME_HEIGHT][GAME_WIDTH];
        bombCells = new boolean[GAME_HEIGHT][GAME_WIDTH];
        itemCells = new boolean[GAME_HEIGHT][GAME_WIDTH];
        itemBlockInfo = new ItemBlock[GAME_HEIGHT][GAME_WIDTH];
        
        // 보드를 빈 상태로 초기화
        for (int i = 0; i < GAME_HEIGHT; i++) {
            for (int j = 0; j < GAME_WIDTH; j++) {
                board[i][j] = 0;
                boardColors[i][j] = null;
                boardTypes[i][j] = -1; // -1은 빈 셀을 의미
                bombCells[i][j] = false;
                itemCells[i][j] = false;
                itemBlockInfo[i][j] = null;
            }
        }
    }
    
    /**
     * 보드를 재설정합니다.
     */
    public void reset() {
        initializeBoard();
    }
    
    /**
     * 지정된 위치에 블록을 배치할 수 있는지 확인합니다.
     */
    public boolean canPlaceBlock(Block block, int x, int y) {
        if (block == null) return false;
        
        for (int j = 0; j < block.height(); j++) {
            for (int i = 0; i < block.width(); i++) {
                if (block.getShape(i, j) == 1) {
                    int newX = x + i;
                    int newY = y + j;
                    
                    // 경계 검사
                    if (newX < 0 || newX >= GAME_WIDTH || newY >= GAME_HEIGHT) {
                        return false;
                    }
                    
                    // 이미 블록이 있는 위치인지 확인
                    if (newY >= 0 && board[newY][newX] == 1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * 블록을 보드에 영구적으로 배치합니다.
     */
    public void placeBlock(Block block, int x, int y) {
        if (block == null) return;
        
        for (int j = 0; j < block.height(); j++) {
            for (int i = 0; i < block.width(); i++) {
                if (block.getShape(i, j) == 1) {
                    int boardX = x + i;
                    int boardY = y + j;
                    
                    if (boardY >= 0 && boardY < GAME_HEIGHT && 
                        boardX >= 0 && boardX < GAME_WIDTH) {
                        board[boardY][boardX] = 1;
                        
                        // 블록 타입 저장 (패턴 그리기용)
                        boardTypes[boardY][boardX] = block.getType();
                        
                        // 아이템 블록인 경우 특별 처리
                        if (block instanceof ItemBlock) {
                            ItemBlock itemBlock = (ItemBlock) block;
                            boardColors[boardY][boardX] = itemBlock.getCellColor(i, j);
                            // 아이템 셀인 경우 아이템 정보 저장 (렌더링용)
                            if (itemBlock.isItemCell(i, j)) {
                                itemCells[boardY][boardX] = true;
                                itemBlockInfo[boardY][boardX] = itemBlock;
                                System.out.println("ItemBlock cell placed at board position: (" + boardX + ", " + boardY + ") with " + itemBlock.getItemType().getDisplayName());
                            }
                        } else {
                            boardColors[boardY][boardX] = block.getColor();
                        }
                    }
                }
            }
        }
        
        // 폭탄 폭발 처리는 줄 삭제 검사 이후에 별도로 처리
    }
    
    /**
     * 특정 줄이 완전히 채워져 있는지 확인합니다.
     */
    public boolean isLineFull(int row) {
        if (row < 0 || row >= GAME_HEIGHT) return false;
        
        for (int col = 0; col < GAME_WIDTH; col++) {
            if (board[row][col] == 0) {
                return false; // 빈 칸이 하나라도 있으면 완성되지 않음
            }
        }
        return true; // 모든 칸이 채워져 있음
    }
    
    /**
     * 폭탄 폭발을 처리합니다 (줄 삭제 검사 이후 호출용).
     */
    public void processBombExplosions() {
        handleBombExplosion();
    }
    
    /**
     * 폭탄이 있는 줄들을 찾아서 반환합니다 (점멸 효과용).
     * @return 폭탄이 있는 줄 번호들의 리스트
     */
    public java.util.List<Integer> getBombLines() {
        java.util.List<Integer> bombLines = new java.util.ArrayList<>();
        
        // 폭탄이 있는 모든 줄 찾기
        for (int row = 0; row < GAME_HEIGHT; row++) {
            for (int col = 0; col < GAME_WIDTH; col++) {
                if (bombCells[row][col]) {
                    bombLines.add(row);
                    break; // 해당 줄에서 폭탄을 찾았으면 다음 줄로
                }
            }
        }
        
        return bombLines;
    }
    
    /**
     * 폭탄 폭발을 처리합니다. 폭탄이 있는 모든 줄을 삭제합니다.
     */
    private void handleBombExplosion() {
        java.util.Set<Integer> linesToExplode = new java.util.HashSet<>();
        
        // 폭탄이 있는 모든 줄 찾기
        for (int row = 0; row < GAME_HEIGHT; row++) {
            for (int col = 0; col < GAME_WIDTH; col++) {
                if (bombCells[row][col]) {
                    linesToExplode.add(row);
                    System.out.println("Bomb explosion detected at line: " + row);
                }
            }
        }
        
        // 폭탄이 있는 줄들 삭제
        if (!linesToExplode.isEmpty()) {
            explodeLines(linesToExplode);
        }
    }
    
    /**
     * 지정된 줄들을 폭발로 인해 삭제합니다.
     */
    private void explodeLines(java.util.Set<Integer> linesToExplode) {
        int linesExploded = linesToExplode.size();
        System.out.println("Exploding " + linesExploded + " lines due to bomb(s)!");
        
        // 삭제될 줄들을 표시
        boolean[] lineToDelete = new boolean[GAME_HEIGHT];
        for (int line : linesToExplode) {
            lineToDelete[line] = true;
        }
        
        // 줄 삭제 및 재배치 (clearCompletedLines와 유사한 로직)
        int writeRow = GAME_HEIGHT - 1;
        for (int readRow = GAME_HEIGHT - 1; readRow >= 0; readRow--) {
            if (!lineToDelete[readRow]) {
                // 삭제되지 않은 줄이면 아래쪽으로 이동
                if (writeRow != readRow) {
                    for (int col = 0; col < GAME_WIDTH; col++) {
                        board[writeRow][col] = board[readRow][col];
                        boardColors[writeRow][col] = boardColors[readRow][col];
                        boardTypes[writeRow][col] = boardTypes[readRow][col];
                        bombCells[writeRow][col] = bombCells[readRow][col];
                    }
                }
                writeRow--;
            }
        }
        
        // 위쪽의 남은 줄들은 빈 줄로 설정
        while (writeRow >= 0) {
            for (int col = 0; col < GAME_WIDTH; col++) {
                board[writeRow][col] = 0;
                boardColors[writeRow][col] = null;
                boardTypes[writeRow][col] = -1;
                bombCells[writeRow][col] = false;
            }
            writeRow--;
        }
    }
    
    
    /**
     * 완성된 줄과 폭탄이 있는 줄을 구분해서 삭제하고 각각의 개수를 반환합니다.
     * LINE_CLEAR 아이템으로 인한 줄 삭제도 포함하여 처리합니다.
     * @return int[2] - [0]: 완성된 줄 수, [1]: 폭탄으로 삭제된 줄 수
     */
    public int[] clearCompletedAndBombLinesSeparately() {
        boolean[] linesToClear = new boolean[GAME_HEIGHT];
        int completedLines = 0;
        int bombLines = 0;
        
        // 1단계: 완성된 줄 찾기 (LINE_CLEAR 아이템이 있는 줄도 포함)
        for (int row = 0; row < GAME_HEIGHT; row++) {
            if (isLineFull(row)) {
                // LINE_CLEAR 아이템이 있는 줄인지 확인
                boolean hasLineClearItem = false;
                for (int col = 0; col < GAME_WIDTH; col++) {
                    if (itemCells[row][col] && itemBlockInfo[row][col] != null) {
                        Object itemType = itemBlockInfo[row][col].getItemType();
                        if (itemType != null && "LINE_CLEAR".equals(itemType.toString())) {
                            hasLineClearItem = true;
                            break;
                        }
                    }
                }
                
                // 완성된 줄이면 LINE_CLEAR 아이템 여부와 관계없이 삭제 대상에 추가
                linesToClear[row] = true;
                completedLines++;
                if (hasLineClearItem) {
                    System.out.println("Line " + row + " is complete with LINE_CLEAR item and will be cleared.");
                } else {
                    System.out.println("Line " + row + " is complete and will be cleared.");
                }
            }
        }
        
        // 2단계: 폭탄이 있는 줄 찾기 (완성된 줄과 중복되지 않은 것만)
        for (int row = 0; row < GAME_HEIGHT; row++) {
            for (int col = 0; col < GAME_WIDTH; col++) {
                if (bombCells[row][col] && !linesToClear[row]) {
                    linesToClear[row] = true;
                    bombLines++;
                    System.out.println("Line " + row + " has bomb and will be cleared.");
                    break; // 해당 줄에서 폭탄을 찾았으면 다음 줄로
                }
            }
        }
        
        // 3단계: 선택된 줄들 제거 및 블록들 재배치
        int totalLinesCleared = completedLines + bombLines;
        if (totalLinesCleared > 0) {
            clearLines(linesToClear);
            System.out.println("Cleared " + completedLines + " completed lines and " + bombLines + " bomb lines!");
        }
        
        return new int[]{completedLines, bombLines};
    }
    
    /**
     * 지정된 줄들을 삭제하고 블록들을 재배치합니다.
     */
    private void clearLines(boolean[] linesToClear) {
        // 1단계: 삭제될 줄의 아이템 효과들을 먼저 활성화
        activateItemEffectsInClearedLines(linesToClear);
        
        int writeRow = GAME_HEIGHT - 1; // 새로 배치할 위치
        
        // 아래에서 위로 올라가면서 삭제되지 않은 줄들만 복사
        for (int readRow = GAME_HEIGHT - 1; readRow >= 0; readRow--) {
            if (!linesToClear[readRow]) {
                // 삭제되지 않은 줄이면 아래쪽으로 이동
                if (writeRow != readRow) {
                    System.out.println("Moving line " + readRow + " to line " + writeRow);
                }
                for (int col = 0; col < GAME_WIDTH; col++) {
                    board[writeRow][col] = board[readRow][col];
                    boardColors[writeRow][col] = boardColors[readRow][col];
                    boardTypes[writeRow][col] = boardTypes[readRow][col];
                    bombCells[writeRow][col] = bombCells[readRow][col];
                    itemCells[writeRow][col] = itemCells[readRow][col];
                    itemBlockInfo[writeRow][col] = itemBlockInfo[readRow][col];
                }
                writeRow--;
            } else {
                System.out.println("Skipping deleted line " + readRow);
            }
        }
        
        // 위쪽의 남은 줄들은 빈 줄로 설정
        while (writeRow >= 0) {
            for (int col = 0; col < GAME_WIDTH; col++) {
                board[writeRow][col] = 0;
                boardColors[writeRow][col] = null;
                boardTypes[writeRow][col] = -1;
                bombCells[writeRow][col] = false;
                itemCells[writeRow][col] = false;
                itemBlockInfo[writeRow][col] = null;
            }
            writeRow--;
        }
    }
    
    /**
     * 게임 오버 상태인지 확인합니다 (맨 위 줄에 블록이 있는지).
     */
    public boolean isGameOver() {
        for (int col = 0; col < GAME_WIDTH; col++) {
            if (board[0][col] == 1) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 블록이 아래로 이동할 수 있는지 확인합니다.
     */
    public boolean canMoveDown(Block block, int x, int y) {
        if (block == null) return false;
        if (y + block.height() >= GAME_HEIGHT) return false;
        
        for (int j = 0; j < block.height(); j++) {
            for (int i = 0; i < block.width(); i++) {
                if (block.getShape(i, j) == 1) {
                    int newY = y + j + 1;
                    int newX = x + i;
                    if (newY >= GAME_HEIGHT || (newY >= 0 && board[newY][newX] == 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * 블록이 왼쪽으로 이동할 수 있는지 확인합니다.
     */
    public boolean canMoveLeft(Block block, int x, int y) {
        if (block == null) return false;
        
        for (int j = 0; j < block.height(); j++) {
            for (int i = 0; i < block.width(); i++) {
                if (block.getShape(i, j) == 1) {
                    int newX = x + i - 1;
                    int newY = y + j;
                    if (newX < 0 || (newY >= 0 && newY < GAME_HEIGHT && board[newY][newX] == 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * 블록이 오른쪽으로 이동할 수 있는지 확인합니다.
     */
    public boolean canMoveRight(Block block, int x, int y) {
        if (block == null) return false;
        
        for (int j = 0; j < block.height(); j++) {
            for (int i = 0; i < block.width(); i++) {
                if (block.getShape(i, j) == 1) {
                    int newX = x + i + 1;
                    int newY = y + j;
                    if (newX >= GAME_WIDTH || (newY >= 0 && newY < GAME_HEIGHT && board[newY][newX] == 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    // Getter 메서드들
    public int[][] getBoard() { return board; }
    public Color[][] getBoardColors() { return boardColors; }
    public int[][] getBoardTypes() { return boardTypes; }
    public boolean[][] getBombCells() { return bombCells; }
    public int getWidth() { return GAME_WIDTH; }
    public int getHeight() { return GAME_HEIGHT; }

    // Setter 메서드들
    public void setBoardTypes(int[][] boardTypes) {
        this.boardTypes = boardTypes;
    }
    public void setBoard(int[][] board) {
        this.board = board;
    }
    public void setBombCells(boolean[][] bombCells) {
        this.bombCells = bombCells;
    }
    public void setItemCells(boolean[][] itemCells) {
        this.itemCells = itemCells;
    }
    public void setItemBlockInfo(ItemBlock[][] itemBlockInfo) {
        this.itemBlockInfo = itemBlockInfo;
    }
    public void setBoardColors(Color[][] boardColors) {
        this.boardColors = boardColors;
    }
    
    /**
     * 특정 위치가 폭탄 셀인지 확인합니다.
     */
    public boolean isBombCell(int x, int y) {
        if (y >= 0 && y < GAME_HEIGHT && x >= 0 && x < GAME_WIDTH) {
            return bombCells[y][x];
        }
        return false;
    }
    
    /**
     * 디버깅용 보드 상태 출력
     */
    public void printBoard() {
        System.out.println("Current board state:");
        for (int i = 0; i < GAME_HEIGHT; i++) {
            for (int j = 0; j < GAME_WIDTH; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("---");
    }
    
    /**
     * 아이템 매니저를 설정합니다 (아이템 모드용)
     */
    public void setItemManager(ItemManager itemManager) {
        this.itemManager = itemManager;
        System.out.println("ItemManager set in BoardManager: " + (itemManager != null));
    }
    
    /**
     * GameScene 참조를 설정합니다 (아이템 효과용)
     */
    public void setGameScene(Object gameScene) {
        this.gameScene = gameScene;
        System.out.println("GameScene set in BoardManager: " + (gameScene != null));
    }
    
    /**
     * BlockManager 참조를 설정합니다 (아이템 효과용)
     */
    public void setBlockManager(Object blockManager) {
        this.blockManager = blockManager;
        System.out.println("BlockManager set in BoardManager: " + (blockManager != null));
    }
    
    /**
     * 지정된 위치가 아이템 셀인지 확인합니다.
     */
    public boolean isItemCell(int x, int y) {
        if (y >= 0 && y < GAME_HEIGHT && x >= 0 && x < GAME_WIDTH) {
            return itemCells[y][x];
        }
        return false;
    }
    
    /**
     * 지정된 위치의 아이템 블록 정보를 반환합니다.
     */
    public ItemBlock getItemBlockInfo(int x, int y) {
        if (y >= 0 && y < GAME_HEIGHT && x >= 0 && x < GAME_WIDTH) {
            return itemBlockInfo[y][x];
        }
        return null;
    }
    
    /**
     * 아이템 셀 배열을 반환합니다.
     */
    public boolean[][] getItemCells() {
        return itemCells;
    }
    
    /**
     * 삭제될 줄들의 아이템 효과들을 먼저 활성화합니다.
     */
    private void activateItemEffectsInClearedLines(boolean[] linesToClear) {
        if (itemManager == null) return;
        
        for (int row = 0; row < GAME_HEIGHT; row++) {
            if (linesToClear[row]) {
                // 해당 줄의 모든 아이템 셀 확인
                for (int col = 0; col < GAME_WIDTH; col++) {
                    if (itemCells[row][col] && itemBlockInfo[row][col] != null) {
                        ItemBlock itemBlock = itemBlockInfo[row][col];
                        // 줄 삭제 시에는 LINE_CLEAR와 SPEED 아이템들 활성화
                        // (VISION_BLOCK, CLEANUP 아이템들은 바닥 착지 시에만 활성화)
                        if (itemBlock.getItemType() == ItemEffectType.LINE_CLEAR ||
                            itemBlock.getItemType() == ItemEffectType.SPEED_UP ||
                            itemBlock.getItemType() == ItemEffectType.SPEED_DOWN ||
                            itemBlock.getItemType() == ItemEffectType.VISION_BLOCK) {
                            System.out.println("🎯 Activating " + itemBlock.getItemType().getDisplayName() + " item effect in cleared line at (" + col + "," + row + ")");
                            
                            // BattleScene인지 확인하여 새로운 시스템 사용
                            if (gameScene != null && gameScene.getClass().getSimpleName().equals("BattleScene")) {
                                handleBattleModeItemActivation(itemBlock, col, row);
                            } else {
                                // 기존 시스템 사용 (레귤러 모드)
                                handleRegularModeItemActivation(itemBlock, col, row);
                            }
                        } else {
                            System.out.println("⏭️ Skipping " + itemBlock.getItemType().getDisplayName() + 
                                             " item in cleared line (only activates on landing)");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 특정 줄을 강제로 삭제합니다 (LINE_CLEAR 아이템용)
     * @param lineIndex 삭제할 줄의 인덱스
     */
    public void forceClearLine(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= GAME_HEIGHT) {
            System.out.println("Invalid line index for force clear: " + lineIndex);
            return;
        }
        
        System.out.println("Force clearing line " + lineIndex + " with blink effect");
        
        // 해당 줄을 완성된 줄로 표시하여 블링킹 효과 적용
        java.util.List<Integer> linesToBlink = new java.util.ArrayList<>();
        linesToBlink.add(lineIndex);
        
        // GameScene에 블링킹 시작을 알림
        notifyLineBlinkStart(linesToBlink);
    }
    
    /**
     * 줄 완성 체크를 강제로 실행합니다 (LINE_CLEAR 아이템용)
     * 해당 줄을 완성된 것으로 만들어서 블링킹 효과와 함께 처리되도록 합니다.
     */
    public void triggerLineCheck() {
        // GameScene이나 다른 컴포넌트에서 줄 체크 로직을 실행하도록 알림
        // 실제로는 GameScene의 checkLines() 메서드를 호출해야 함
        System.out.println("Triggering line check for blink effect integration");
        
        // 현재는 직접 줄 체크를 수행 (추후 GameScene 연동으로 개선)
        checkAndHandleCompletedLines();
    }
    
    /**
     * 완성된 줄을 체크하고 처리합니다 (내부용)
     */
    private void checkAndHandleCompletedLines() {
        java.util.List<Integer> completedLines = new java.util.ArrayList<>();
        
        // 완성된 줄 찾기
        for (int row = 0; row < GAME_HEIGHT; row++) {
            if (isLineFull(row)) {
                completedLines.add(row);
                System.out.println("Found completed line: " + row);
            }
        }
        
        // 완성된 줄이 있으면 블링킹 효과 시작
        if (!completedLines.isEmpty()) {
            notifyLineBlinkStart(completedLines);
        }
    }
    
    /**
     * GameScene에 줄 블링킹 시작을 알립니다.
     */
    private void notifyLineBlinkStart(java.util.List<Integer> lines) {
        // GameScene 참조가 필요하지만, 지금은 간단히 즉시 삭제로 처리
        // 실제로는 GameScene의 블링킹 시스템을 사용해야 함
        System.out.println("Starting blink effect for lines: " + lines);
        
        // 임시: 블링킹 없이 즉시 삭제 (추후 개선 필요)
        for (int lineIndex : lines) {
            forceClearLineImmediate(lineIndex);
        }
    }
    
    /**
     * 즉시 줄을 삭제합니다 (내부 사용)
     */
    private void forceClearLineImmediate(int lineIndex) {
        System.out.println("Force clearing line " + lineIndex);
        
        // 해당 줄 위의 모든 줄들을 한 칸씩 아래로 이동
        for (int row = lineIndex; row > 0; row--) {
            for (int col = 0; col < GAME_WIDTH; col++) {
                board[row][col] = board[row - 1][col];
                boardColors[row][col] = boardColors[row - 1][col];
                boardTypes[row][col] = boardTypes[row - 1][col];
                bombCells[row][col] = bombCells[row - 1][col];
                itemCells[row][col] = itemCells[row - 1][col];
                itemBlockInfo[row][col] = itemBlockInfo[row - 1][col];
            }
        }
        
        // 맨 위 줄은 비워둠
        for (int col = 0; col < GAME_WIDTH; col++) {
            board[0][col] = 0;
            boardColors[0][col] = null;
            boardTypes[0][col] = -1;
            bombCells[0][col] = false;
            itemCells[0][col] = false;
            itemBlockInfo[0][col] = null;
        }
        
        System.out.println("Force clear completed for line " + lineIndex);
    }
    
    /**
     * 특정 위치의 아이템 셀 상태를 설정합니다.
     * @param x x 좌표
     * @param y y 좌표
     * @param isItem 아이템 셀 여부
     */
    public void setItemCell(int x, int y, boolean isItem) {
        if (y >= 0 && y < GAME_HEIGHT && x >= 0 && x < GAME_WIDTH) {
            itemCells[y][x] = isItem;
            if (!isItem) {
                itemBlockInfo[y][x] = null; // 아이템 정보도 제거
            }
            System.out.println("Set item cell at (" + x + ", " + y + ") to " + isItem);
        } else {
            System.out.println("Invalid coordinates for setItemCell: (" + x + ", " + y + ")");
        }
    }
    
    /**
     * 특정 위치의 블록 색상을 설정합니다.
     * @param x x 좌표
     * @param y y 좌표
     * @param color 설정할 색상
     */
    public void setBoardColor(int x, int y, Color color) {
        if (y >= 0 && y < GAME_HEIGHT && x >= 0 && x < GAME_WIDTH) {
            boardColors[y][x] = color;
            System.out.println("Set board color at (" + x + ", " + y + ") to " + color);
        } else {
            System.out.println("Invalid coordinates for setBoardColor: (" + x + ", " + y + ")");
        }
    }
    
    /**
     * 특정 위치의 아이템 블록 정보를 강제로 제거합니다.
     * @param x x 좌표
     * @param y y 좌표
     */
    public void clearItemBlockInfo(int x, int y) {
        if (y >= 0 && y < GAME_HEIGHT && x >= 0 && x < GAME_WIDTH) {
            itemBlockInfo[y][x] = null;
            itemCells[y][x] = false;
            System.out.println("Cleared item block info at (" + x + ", " + y + ")");
        } else {
            System.out.println("Invalid coordinates for clearItemBlockInfo: (" + x + ", " + y + ")");
        }
    }
    
    /**
     * 특정 위치의 블록 색상을 반환합니다.
     * @param x x 좌표
     * @param y y 좌표
     * @return 해당 위치의 색상, 유효하지 않은 좌표면 null
     */
    public Color getBoardColor(int x, int y) {
        if (y >= 0 && y < GAME_HEIGHT && x >= 0 && x < GAME_WIDTH) {
            return boardColors[y][x];
        }
        return null;
    }
    
    /**
     * 지정된 영역에서 삭제된 블록들로 인해 위에 있는 블록들을 아래로 내립니다.
     * 각 열별로 중력을 적용하여 빈 공간을 채웁니다.
     * 
     * @param minX 영향받는 영역의 최소 X 좌표
     * @param maxX 영향받는 영역의 최대 X 좌표  
     * @param minY 영향받는 영역의 최소 Y 좌표
     * @param maxY 영향받는 영역의 최대 Y 좌표
     */
    public void compactColumns(int minX, int maxX, int minY, int maxY) {
        System.out.println("Compacting columns in area: (" + minX + "," + minY + ") to (" + maxX + "," + maxY + ")");
        
        // 각 열별로 중력 적용
        for (int col = minX; col <= maxX; col++) {
            if (col < 0 || col >= GAME_WIDTH) continue;
            
            int writeRow = maxY; // 아래부터 채워나갈 위치
            
            // 아래에서 위로 올라가면서 빈 공간이 아닌 블록들만 아래로 이동
            for (int readRow = maxY; readRow >= 0; readRow--) {
                if (board[readRow][col] != 0) {
                    // 블록이 있으면 writeRow 위치로 이동
                    if (writeRow != readRow) {
                        System.out.println("Moving block from (" + col + "," + readRow + ") to (" + col + "," + writeRow + ")");
                        
                        // 블록 데이터 이동
                        board[writeRow][col] = board[readRow][col];
                        boardColors[writeRow][col] = boardColors[readRow][col];
                        boardTypes[writeRow][col] = boardTypes[readRow][col];
                        bombCells[writeRow][col] = bombCells[readRow][col];
                        itemCells[writeRow][col] = itemCells[readRow][col];
                        itemBlockInfo[writeRow][col] = itemBlockInfo[readRow][col];
                        
                        // 원래 위치는 비우기
                        board[readRow][col] = 0;
                        boardColors[readRow][col] = null;
                        boardTypes[readRow][col] = -1;
                        bombCells[readRow][col] = false;
                        itemCells[readRow][col] = false;
                        itemBlockInfo[readRow][col] = null;
                    }
                    writeRow--; // 다음에 채울 위치로 이동
                }
            }
            
            // 나머지 위쪽 칸들은 비우기
            for (int row = writeRow; row >= 0; row--) {
                if (board[row][col] != 0) {
                    board[row][col] = 0;
                    boardColors[row][col] = null;
                    boardTypes[row][col] = -1;
                    bombCells[row][col] = false;
                    itemCells[row][col] = false;
                    itemBlockInfo[row][col] = null;
                }
            }
        }
        
        System.out.println("Column compaction completed");
    }
    
    /**
     * 배틀 모드에서 아이템 효과를 활성화합니다
     */
    private void handleBattleModeItemActivation(ItemBlock itemBlock, int col, int row) {
        try {
            // BattleScene에서 BattleItemManager 가져오기
            Object battleItemManager = gameScene.getClass()
                .getMethod("getBattleItemManager")
                .invoke(gameScene);
                
            if (battleItemManager != null) {
                // 현재 플레이어 확인
                int currentPlayerNum = determinePlayerNumber(); // 0-based
                Object sourcePlayer = null;
                
                // Player 열거형의 fromInternalId 메서드 사용
                Class<?> playerClass = Class.forName("tetris.scene.battle.Player");
                Object[] playerValues = (Object[]) playerClass.getMethod("values").invoke(null);
                
                for (Object player : playerValues) {
                    int internalId = (Integer) player.getClass().getMethod("getInternalId").invoke(player);
                    if (internalId == currentPlayerNum) {
                        sourcePlayer = player;
                        break;
                    }
                }
                
                if (sourcePlayer != null) {
                    // BattleItemManager의 applyItemEffect 호출
                    battleItemManager.getClass()
                        .getMethod("applyItemEffect", playerClass, 
                                  Class.forName("tetris.scene.game.items.ItemEffectType"), int.class, int.class)
                        .invoke(battleItemManager, sourcePlayer, itemBlock.getItemType(), col, row);
                        
                    System.out.println("✅ Battle mode item effect applied successfully");
                } else {
                    System.err.println("❌ Could not determine source player for battle item effect");
                }
            } else {
                System.err.println("❌ BattleItemManager not found in BattleScene");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to apply battle mode item effect: " + e.getMessage());
            e.printStackTrace();
            
            // 실패 시 기존 시스템으로 폴백
            handleRegularModeItemActivation(itemBlock, col, row);
        }
    }
    
    /**
     * 레귤러 모드에서 아이템 효과를 활성화합니다 (기존 방식)
     */
    private void handleRegularModeItemActivation(ItemBlock itemBlock, int col, int row) {
        ItemEffect effect = ItemEffectFactory.createEffect(itemBlock.getItemType());
        if (effect != null) {
            ItemEffectContext context = new ItemEffectContext(
                getBoard(), col, row
            );
            // 필요한 컨텍스트 정보 설정
            context.setBoardManager(this);
            context.setBlockManager(blockManager);
            context.setGameScene(gameScene);
            
            itemManager.activateItemEffect(effect, context);
        }
    }
    
    /**
     * 현재 플레이어 번호를 반환합니다
     */
    private int determinePlayerNumber() {
        return this.playerNumber;
    }
    
    /**
     * 배틀모드에서 이 BoardManager의 플레이어 번호를 설정합니다
     * @param playerNumber 플레이어 번호 (0=Player1, 1=Player2)
     */
    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
        System.out.println("PlayerNumber set in BoardManager: " + (playerNumber + 1));
    }

}