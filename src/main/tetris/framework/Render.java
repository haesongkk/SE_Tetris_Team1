package tetris.framework;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import java.awt.Canvas;
import java.awt.image.BufferStrategy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class Render {
    final int WIDTH = 800;
    final int HEIGHT = 600;
    final String TITLE = "Tetris";
    Color CLEAR = Color.BLACK;

    JFrame m_frame;
    Canvas m_canvas;
    BufferStrategy m_buffer;
    Graphics2D m_graphics;

    public void resizeWindow(int width, int height) {
        m_canvas.setPreferredSize(new Dimension(width, height));
        m_frame.pack();
        m_frame.setLocationRelativeTo(null);
        
        m_canvas.createBufferStrategy(3);
        m_buffer = m_canvas.getBufferStrategy();
        m_graphics.dispose();
        m_graphics = (Graphics2D)m_buffer.getDrawGraphics();
    }

    public void setClearColor(float r, float g, float b, float a) {
        CLEAR = new Color(r,g,b,a);
    }

    public void setColor(float r, float g, float b, float a) {
        m_graphics.setColor(new Color(r,g,b,a));
    }

    public void setFont(String name/*"Arial"*/, int size, int style /*0,1,2,3*/) {
        m_graphics.setFont(new Font(name, style, size));
    }

    // 필요하다면 추가 기능 구현
    public void setStroke(int border) {
        m_graphics.setStroke(new BasicStroke(
            border
            //BasicStroke.CAP_ROUND,      // 끝 모양 (BUTT, ROUND, SQUARE)
            //BasicStroke.JOIN_BEVEL,     // 모서리 모양 (MITER, ROUND, BEVEL)
            //10.0f,                      // miter limit (MITER 일 때만)
            //new float[]{10.0f, 5.0f},   // 점선 패턴 (on, off 길이 반복)
            //0.0f 
        ));
    }

    public void drawRect(int x, int y, int width, int height, int radiusX, int radiusY, boolean bFill) {
        int left = x - width/2;
        int top = y - height/2;
        if(bFill) m_graphics.fillRoundRect(left, top, width, height, radiusX*2, radiusY*2);
        else m_graphics.drawRoundRect(left, top, width, height, radiusX*2, radiusY*2);
        
    }

   public void drawText(int x, int y, String text, int lineHeight) {
        FontMetrics fm = m_graphics.getFontMetrics();
        String[] lines = text.split("\n", -1);
        

        if(lineHeight<0) lineHeight = fm.getHeight();
        final int blockHeight = lineHeight * lines.length;
        final int blockTop = y - blockHeight / 2 + fm.getAscent();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            int w = fm.stringWidth(line);
            int lineX = x - w / 2;
            int lineY = blockTop + i * lineHeight;

            m_graphics.drawString(line, lineX, lineY);
        }
    }




    public void initialize() {
        // 윈도우 생성
        m_frame = new JFrame();
        m_frame.setTitle(TITLE);
        m_frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // 스크린 생성
        m_canvas = new Canvas();
        m_canvas.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        m_canvas.setIgnoreRepaint(true); 

        // 윈도우에 스크린 붙이기
        m_frame.add(m_canvas);
        m_frame.pack();
        m_frame.setLocationRelativeTo(null);
        m_frame.setVisible(true);

        // 더블 버퍼링을 위한 백버퍼 생성
        m_canvas.createBufferStrategy(3);
        m_buffer = m_canvas.getBufferStrategy();
        m_graphics = (Graphics2D)m_buffer.getDrawGraphics();
    }

    public boolean update(double dt) {
        // 화면 출력 (백버퍼 스왑)
        m_buffer.show(); 

        // 기존 참조하던 그래픽스 인스턴스 해제하고 현재 그릴 버퍼의 그래픽스를 받아오기
        m_graphics.dispose();
        m_graphics = (Graphics2D)m_buffer.getDrawGraphics();

        // 백버퍼 비우기 (기본 색으로 칠하기)
        m_graphics.setColor(CLEAR);
        m_graphics.fillRect(0, 0, WIDTH, HEIGHT);

        return true;

    }

    public void finalize() {
        m_graphics.dispose();
        m_buffer.dispose();
        m_frame.dispose();

        m_buffer = null;
        m_canvas = null;
        m_frame = null;
        m_graphics = null;
    }


    private Render() { }
    static Render singleInstance = new Render();
    public static Render get() { return singleInstance; }
}
