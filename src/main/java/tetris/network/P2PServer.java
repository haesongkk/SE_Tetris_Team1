package tetris.network;

import java.io.*;
import java.net.*;
import javax.swing.SwingUtilities;

public class P2PServer extends P2PBase {

    ServerSocket serverSocket;
    public Runnable onConnect;

    public P2PServer() {
        // 호스트 주소 얻기
        try { 
            HOST = InetAddress.getLocalHost().getHostAddress(); 
        } catch (UnknownHostException e) { 
            System.err.println("P2P 서버: 호스트 주소를 얻을 수 없습니다 - " + e.getMessage());
            e.printStackTrace();
            return; 
        }

        // 서버 소켓 생성
        try { 
            serverSocket = new ServerSocket(PORT); 
        } catch (IOException e) { 
            System.err.println("P2P 서버: 서버 소켓 생성 실패 (포트: " + PORT + ") - " + e.getMessage());
            e.printStackTrace();
            return; 
        }
        System.out.println("서버 시작: " + HOST);
        System.out.println("서버 포트: " + PORT);

        Thread waitThread = new Thread(()-> waitForClient());
        waitThread.start();
    }

    void waitForClient() {
        // 클라이언트 접속 대기
        try { 
            socket = serverSocket.accept(); 
        } catch (IOException e) { 
            System.err.println("P2P 서버: 클라이언트 접속 대기 중 오류 - " + e.getMessage());
            e.printStackTrace();
            // 서버 소켓이 닫혔거나 오류 발생 시 종료
            if (onDisconnect != null) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        onDisconnect.run();
                    } catch (Exception ex) {
                        System.err.println("P2P 서버: onDisconnect 콜백 실행 중 오류: " + ex.getMessage());
                    }
                });
            }
            return;
        }
        
        if (socket == null) {
            System.err.println("P2P 서버: 소켓이 null입니다");
            return;
        }
        
        System.out.println("클라이언트 접속: " + socket.getInetAddress());

        // 입출력 스트림 생성
        try {
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        } catch (IOException e) {
            System.err.println("P2P 서버: 입출력 스트림 생성 실패 - " + e.getMessage());
            e.printStackTrace();
            // 스트림 생성 실패 시 리소스 정리
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ex) {
                System.err.println("P2P 서버: 소켓 닫기 실패 - " + ex.getMessage());
            }
            if (onDisconnect != null) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        onDisconnect.run();
                    } catch (Exception ex) {
                        System.err.println("P2P 서버: onDisconnect 콜백 실행 중 오류: " + ex.getMessage());
                    }
                });
            }
            return;
        }
        
        // 접속 콜백 호출 (Swing UI 변경은 EDT에서 실행)
        if (onConnect != null) {
            SwingUtilities.invokeLater(onConnect);
        }
        super.run();
    }

    @Override
    public void release() {
        super.release();
        try {
            if(serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("P2P 서버: 서버 소켓 종료됨");
            }
        } catch (IOException e) {
            System.err.println("P2P 서버: 서버 소켓 닫기 실패 - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
