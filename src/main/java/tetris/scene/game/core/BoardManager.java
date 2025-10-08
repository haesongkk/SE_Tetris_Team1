package tetris.scene.game.core;

import tetris.scene.game.blocks.Block;
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
    
    public BoardManager() {
        initializeBoard();
    }
    
    /**
     * 게임 보드를 초기화합니다.
     */
    private void initializeBoard() {
        board = new int[GAME_HEIGHT][GAME_WIDTH];
        boardColors = new Color[GAME_HEIGHT][GAME_WIDTH];
        
        // 보드를 빈 상태로 초기화
        for (int i = 0; i < GAME_HEIGHT; i++) {
            for (int j = 0; j < GAME_WIDTH; j++) {
                board[i][j] = 0;
                boardColors[i][j] = null;
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
                        boardColors[boardY][boardX] = block.getColor();
                    }
                }
            }
        }
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
     * 완성된 모든 줄을 찾아서 제거하고 삭제된 줄 수를 반환합니다.
     * 여러 줄이 동시에 완성된 경우를 위한 개선된 버전
     */
    public int clearCompletedLines() {
        int linesClearedCount = 0;
        boolean[] linesToClear = new boolean[GAME_HEIGHT];
        
        // 1단계: 완성된 모든 줄 찾기
        for (int row = 0; row < GAME_HEIGHT; row++) {
            if (isLineFull(row)) {
                linesToClear[row] = true;
                linesClearedCount++;
                System.out.println("Line " + row + " is complete and will be cleared.");
            }
        }
        
        // 2단계: 완성된 줄들 제거 및 블록들 재배치
        if (linesClearedCount > 0) {
            int writeRow = GAME_HEIGHT - 1; // 새로 배치할 위치
            
            // 아래에서 위로 올라가면서 완성되지 않은 줄들만 복사
            for (int readRow = GAME_HEIGHT - 1; readRow >= 0; readRow--) {
                if (!linesToClear[readRow]) {
                    // 완성되지 않은 줄이면 아래쪽으로 이동
                    if (writeRow != readRow) {
                        System.out.println("Moving line " + readRow + " to line " + writeRow);
                    }
                    for (int col = 0; col < GAME_WIDTH; col++) {
                        board[writeRow][col] = board[readRow][col];
                        boardColors[writeRow][col] = boardColors[readRow][col];
                    }
                    writeRow--;
                } else {
                    System.out.println("Skipping completed line " + readRow);
                }
            }
            
            // 위쪽의 남은 줄들은 빈 줄로 설정
            while (writeRow >= 0) {
                for (int col = 0; col < GAME_WIDTH; col++) {
                    board[writeRow][col] = 0;
                    boardColors[writeRow][col] = null;
                }
                writeRow--;
            }
            
            System.out.println("Cleared " + linesClearedCount + " lines simultaneously!");
        }
        
        return linesClearedCount;
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
    public int getWidth() { return GAME_WIDTH; }
    public int getHeight() { return GAME_HEIGHT; }
    
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
}