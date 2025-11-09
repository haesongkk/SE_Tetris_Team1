package tetris.network;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import java.io.*;
import java.net.*;

public class EchoClient {
    final int PORT = 5000;

    KeyEventDispatcher keyEventDispatcher;

    Socket socket;
    BufferedReader in;
    BufferedWriter out;

    public EchoClient(String HOST) {
        try { socket = new Socket(HOST, PORT); }
        catch (IOException e) { return; }

        System.out.println("서버 연결됨");

        // 입출력 스트림 생성
        try {
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 키 이벤트 디스패처 등록
        keyEventDispatcher = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED){
                    int code = e.getKeyCode();
                    String keyString = KeyEvent.getKeyText(code);
                    System.out.println("키 입력 감지: " + code + " (" + keyString + ")");
                    sendToServer(String.valueOf(code));
                }
                
                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(keyEventDispatcher);
    }

    void sendToServer(String message) {
        try { out.write(message); }
        catch (IOException ex) { ex.printStackTrace(); }
    }
}
