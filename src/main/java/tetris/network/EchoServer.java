package tetris.network;

import java.io.*;
import java.net.*;

public class EchoServer {
    final int PORT = 5000;
    public final String HOST;

    Thread serverThread;
    ServerSocket serverSocket;
    Socket clientSocket;

    BufferedReader in;
    BufferedWriter out;


    public EchoServer() {
        // 호스트 주소 얻기
        try { HOST = InetAddress.getLocalHost().getHostAddress(); }
        catch (UnknownHostException e) { throw new RuntimeException(e);}
        
        // 서버 스레드 시작
        serverThread = new Thread(()-> runServer());
        serverThread.start();
    }

    void runServer() {
        // 서버 소켓 생성
        try { serverSocket = new ServerSocket(PORT); }
        catch (IOException e) { e.printStackTrace(); }
        
        System.out.println("서버 시작: " + HOST);

        // 클라이언트 접속 대기
        try { clientSocket = serverSocket.accept(); }
        catch (IOException e) { e.printStackTrace(); }

        System.out.println("클라이언트 접속: " + clientSocket.getInetAddress());

        // 입출력 스트림 생성
        try {
            in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(
                new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 클라이언트로부터 메시지 수신
        try {
            String line;
            while ((line = in.readLine()) != null) {   // \n 올 때까지 블로킹
                System.out.println("클라이언트 메세지: " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
