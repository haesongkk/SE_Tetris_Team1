package tetris.network;

import java.io.*;
import java.net.*;

public class P2PClient extends P2PBase {
    public boolean connect(String HOST) {
        // 소켓 연결 시도
        try { 
            socket = new Socket(HOST, PORT); 
            System.out.println("P2P 클라이언트: 서버에 연결됨 (" + HOST + ":" + PORT + ")");
        } catch (IOException e) { 
            System.err.println("P2P 클라이언트: 서버 연결 실패 (" + HOST + ":" + PORT + ") - " + e.getMessage());
            e.printStackTrace();
            return false; 
        }
        
        // 입출력 스트림 생성
        try {
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        } catch (IOException e) {
            System.err.println("P2P 클라이언트: 입출력 스트림 생성 실패 - " + e.getMessage());
            e.printStackTrace();
            // 스트림 생성 실패 시 리소스 정리
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ex) {
                System.err.println("P2P 클라이언트: 소켓 닫기 실패 - " + ex.getMessage());
            }
            return false;
        }
        
        super.run();
        return true;
    }
}
