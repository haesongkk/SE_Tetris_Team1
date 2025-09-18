package tetris.scene;

import java.util.Arrays;

import tetris.framework.Game;
import tetris.framework.Input;
import tetris.framework.Render;
import tetris.framework.Input.KeyState;
import tetris.framework.Input.KeyType;

public class DefaultScene implements Scene{
    double elapsed = 0;
    double downTimer = 0;

    
    char[][] m_block = {
        { 'O', 'O', ' '},
        { ' ', 'O', 'O'}
    };
    int[] m_blockPos = {5,0};
    char[][] m_board;


    @Override
    public void initialize() {
        // 창 사이즈 조절
        Render.get().resizeWindow(800,600);
        Render.get().setClearColor(0,0.1f,0.3f,1);
        setBoardSize(10,20);
    }

    @Override
    public boolean update(double dt) {
        elapsed += dt;
        downTimer += dt;
        if(downTimer >= 1) {
            downTimer = 0;
            m_blockPos[1]++;
        }
        
        if(Input.get().getKeyState(KeyType.LEFT) == KeyState.TAP) m_blockPos[0]--;
        if(Input.get().getKeyState(KeyType.RIGHT) == KeyState.TAP) m_blockPos[0]++;
        
        if(m_blockPos[0] > 7) m_blockPos[0] = 7;
        if(m_blockPos[0] < 0)  m_blockPos[0] = 0;
        if(m_blockPos[1] > 18) m_blockPos[1] = 18;
        if(m_blockPos[1] < 0)  m_blockPos[1] = 0;
        
        // 보드판 출력
        Render.get().setFont("Consolas", 30, 1);
        Render.get().setColor(0.5f,0.5f,0.5f,1);
        Render.get().drawText(300, 300, getBoardShape(), 20);
        
        // 사각형 출력
        Render.get().setStroke(5);
        Render.get().setColor(0.f,0.3f,0.f,1.f);
        Render.get().drawRect(600, 100, 100, 50, 15,15, false);
        
        // 텍스트 출력
        Render.get().setFont("SansSerif", 20, 1);
        Render.get().setColor(1,1,1,1.f);
        Render.get().drawText(600,100,String.format("%.3f", elapsed),-1);

        // 사각형 출력
        Render.get().setStroke(5);
        Render.get().setColor(0.3f,0.f,0.f,1.f);
        Render.get().drawRect(600, 500, 300, 50, 15,15, true);

        // 텍스트 출력
        Render.get().setFont("SansSerif", 20, 1);
        Render.get().setColor(1,1,1,1.f);
        Render.get().drawText(600,500, "ESC를 눌러 씬 전환하기",-1);

        if(Input.get().getKeyState(KeyType.ESC) == KeyState.AWAY) Game.get().setScene(new GameScene());
        return true;
    }
    
    @Override
    public void finalize() {
        System.out.println("default scene finalize"); // 왜 두 번 호출되지?
    }

    private void setBoardSize(int width, int height) {
        m_board = new char[height][width];
    }

    private String getBoardShape() {
        char[][] grid = new char[m_board.length][m_board[0].length];
        for (int i = 0; i < m_board.length; i++) {
            grid[i] = Arrays.copyOf(m_board[i], m_board[i].length);
        }
        for (int y = 0; y < m_block.length; y++) {
            for (int x = 0; x < m_block[y].length; x++) {
                char c = m_block[y][x];
                if(c== 'c') continue;
                int gridX = x + m_blockPos[0];
                int gridY = y + m_blockPos[1];
                grid[gridY][gridX] = c;
            }
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<grid[0].length+2; i++) {
            sb.append('X');
        }
        sb.append('\n');
        for (int r = 0; r < grid.length; r++) {
            sb.append('X').append(grid[r]).append('X').append('\n');
        }
        for (int i=0; i<grid[0].length+2; i++) {
            sb.append('X');
        }
        sb.append('\n');

        return sb.toString();
    }
    
}
