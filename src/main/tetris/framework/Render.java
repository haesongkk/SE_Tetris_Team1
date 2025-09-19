package tetris.framework;

// GUI 관련 import
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

// Render 클래스: 화면 그리기 담당 (싱글톤)
public class Render {
    // 기본 창 크기
    final int WIDTH = 800;
    final int HEIGHT = 600;
    // 창 제목
    final String TITLE = "Tetris";
    // 화면 클리어 색상
    Color CLEAR = Color.BLACK;

    // Swing 컴포넌트 및 그래픽스
    JFrame m_frame;
    Canvas m_canvas;
    BufferStrategy m_buffer;
    Graphics2D m_graphics;

    // 창 크기 조정
    public void resizeWindow(int width, int height) {
        m_canvas.setPreferredSize(new Dimension(width, height)); // 캔버스 크기 설정
        m_frame.pack(); // 프레임 크기 맞춤
        m_frame.setLocationRelativeTo(null); // 화면 중앙 배치
        
        m_canvas.createBufferStrategy(3); // 더블 버퍼링
        m_buffer = m_canvas.getBufferStrategy();
        m_graphics.dispose(); // 이전 그래픽스 해제
        m_graphics = (Graphics2D)m_buffer.getDrawGraphics(); // 새 그래픽스 획득
    }

    // 화면 클리어 색상 설정
    public void setClearColor(float r, float g, float b, float a) {
        CLEAR = new Color(r,g,b,a);
    }

    // 그리기 색상 설정
    public void setColor(float r, float g, float b, float a) {
        m_graphics.setColor(new Color(r,g,b,a));
    }

    // 폰트 설정
    public void setFont(String name/*"Arial"*/, int size, int style /*0,1,2,3*/) {
        m_graphics.setFont(new Font(name, style, size));
    }

    // 선 두께 설정
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

    // 사각형(둥근 모서리) 그리기
    public void drawRect(int x, int y, int width, int height, int radiusX, int radiusY, boolean bFill) {
        int left = x - width/2; // 중심 기준 좌상단 x
        int top = y - height/2; // 중심 기준 좌상단 y
        if(bFill) m_graphics.fillRoundRect(left, top, width, height, radiusX*2, radiusY*2); // 채우기
        else m_graphics.drawRoundRect(left, top, width, height, radiusX*2, radiusY*2); // 테두리만
        
    }

    // 텍스트 그리기 (여러 줄 지원)
    public void drawText(int x, int y, String text, int lineHeight) {
        FontMetrics fm = m_graphics.getFontMetrics(); // 폰트 정보
        String[] lines = text.split("\n", -1); // 줄 단위 분리
        

        if(lineHeight<0) lineHeight = fm.getHeight(); // 줄 높이 자동 계산
        final int blockHeight = lineHeight * lines.length; // 전체 블록 높이
        final int blockTop = y - blockHeight / 2 + fm.getAscent(); // 첫 줄 y좌표

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            int w = fm.stringWidth(line); // 줄 너비
            int lineX = x - w / 2; // 가운데 정렬 x좌표
            int lineY = blockTop + i * lineHeight; // 줄별 y좌표

            m_graphics.drawString(line, lineX, lineY); // 텍스트 출력
        }
    }




    // 렌더러 초기화 (윈도우, 캔버스, 버퍼 등 생성)
    public void initialize() {
        // 윈도우 생성
        m_frame = new JFrame();
        m_frame.setTitle(TITLE);
        m_frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // 스크린(캔버스) 생성
        m_canvas = new Canvas();
        m_canvas.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        m_canvas.setIgnoreRepaint(true); 

        // 윈도우에 캔버스 추가
        m_frame.add(m_canvas);
        m_frame.pack();
        m_frame.setLocationRelativeTo(null);
        m_frame.setVisible(true);

        // 더블 버퍼링을 위한 백버퍼 생성
        m_canvas.createBufferStrategy(3);
        m_buffer = m_canvas.getBufferStrategy();
        m_graphics = (Graphics2D)m_buffer.getDrawGraphics();
    }

    // 매 프레임마다 호출 (버퍼 스왑 및 화면 클리어)
    public boolean update(double dt) {
        // 화면 출력 (백버퍼 스왑)
        m_buffer.show(); 

        // 기존 그래픽스 해제 후 새로 획득
        m_graphics.dispose();
        m_graphics = (Graphics2D)m_buffer.getDrawGraphics();

        // 백버퍼 비우기 (클리어 색상으로 전체 칠하기)
        m_graphics.setColor(CLEAR);
        m_graphics.fillRect(0, 0, WIDTH, HEIGHT);

        return true;

    }

    // 종료 시 리소스 해제
    public void finalize() {
        m_graphics.dispose();
        m_buffer.dispose();
        m_frame.dispose();

        m_buffer = null;
        m_canvas = null;
        m_frame = null;
        m_graphics = null;
    }


    // 싱글톤 패턴 구현
    private Render() { }
    static Render singleInstance = new Render(); // 유일 인스턴스
    public static Render get() { return singleInstance; } // 인스턴스 반환
}
