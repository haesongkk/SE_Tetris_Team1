package tetris.network;

import java.io.*;
import java.net.*;

public class P2PServer extends P2PBase {

    ServerSocket serverSocket;
    public Runnable onConnect;

    public P2PServer() {
        // 호스트 주소 얻기
        try { HOST = InetAddress.getLocalHost().getHostAddress(); }
        catch (UnknownHostException e) { throw new RuntimeException(e);}

        // 서버 소켓 생성
        try { serverSocket = new ServerSocket(PORT); }
        catch (IOException e) { e.printStackTrace(); }
        System.out.println("서버 시작: " + HOST);

        Thread waitThread = new Thread(()-> waitForClient());
        waitThread.start();
    }

    void waitForClient() {

        // 클라이언트 접속 대기
        try { socket = serverSocket.accept(); }
        catch (IOException e) { e.printStackTrace(); }
        System.out.println("클라이언트 접속: " + socket.getInetAddress());

        // 입출력 스트림 생성
        try {
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 접속 콜백 호출
        if (onConnect != null) onConnect.run();

    }
}
