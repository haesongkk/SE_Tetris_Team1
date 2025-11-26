package tetris.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


public class P2PBase {

    final int PORT = 5000;
    public String HOST;

    Socket socket = null;
    BufferedReader in = null;
    BufferedWriter out = null;

    public void send(String message) { 
        if (out == null) {
            System.err.println("P2P: 출력 스트림이 null입니다. 메시지 전송 실패: " + message);
            handleNetworkError(new IOException("출력 스트림이 null입니다"));
            return;
        }
        try { 
            out.write(message + '\n'); 
            out.flush(); 
        } catch (IOException ex) {
            System.err.println("P2P: 메시지 전송 실패 - " + ex.getMessage() + " (메시지: " + message + ")");
            handleNetworkError(ex);
        }
     }

    public void release() {
        // release() 호출 시에는 정상 종료이므로 오류 처리를 하지 않음
        bRunning = false;
        isHandlingError = true; // handleNetworkError가 호출되지 않도록 설정
        
        if(onDisconnect != null) {
            onDisconnect.run();
            onDisconnect = null;
        }
        
        // RELEASE_MESSAGE 전송 시도 (실패해도 무시)
        try {
            if (out != null) {
                out.write(RELEASE_MESSAGE + '\n');
                out.flush();
            }
        } catch (IOException e) {
            // release() 중에는 예외를 무시
        }
        
        callbacks.clear();
        try {
            if(in != null) in.close();
            if(out != null) out.close();
            if(socket != null) socket.close();
            System.out.println("p2p release success");

        } catch (IOException e) {
            System.out.println("p2p release failed");
        }
    }

    boolean bRunning = false;

    private Map<String, Consumer<String>> callbacks = new HashMap<>();
    protected Runnable onDisconnect;
    private final String RELEASE_MESSAGE = "release";
    private long lastReceiveTime = -1;
    private final int TIMEOUT_MS = 5000;
    private final String PING_MESSAGE = "ping";
    private final String PONG_MESSAGE = "pong";
    private boolean bWaitingPong = false;
    private long lastPingTime = -1;
    private boolean isHandlingError = false; // 중복 오류 처리 방지

    /**
     * 네트워크 오류 발생 시 처리
     */
    private void handleNetworkError(IOException e) {
        // 중복 오류 처리 방지
        if (isHandlingError || !bRunning) {
            return;
        }
        
        isHandlingError = true;
        bRunning = false;
        
        System.err.println("P2P: 네트워크 오류 발생 - " + e.getMessage());
        e.printStackTrace();
        
        // 사용자에게 알림 (onDisconnect 콜백 호출)
        if (onDisconnect != null) {
            try {
                onDisconnect.run();
            } catch (Exception ex) {
                System.err.println("P2P: onDisconnect 콜백 실행 중 오류: " + ex.getMessage());
            }
        }
    }

    protected void run() {
        bRunning = true;
        lastReceiveTime = System.currentTimeMillis();
        
        new Thread(() -> {
            while(bRunning) {
                // 타임아웃 검사
                long currentTime = System.currentTimeMillis();
                if(currentTime - lastReceiveTime > TIMEOUT_MS) {
                    if(bWaitingPong) {
                        if(currentTime - lastPingTime > TIMEOUT_MS) {
                            System.out.println("타임아웃 발생");
                            break;
                        } else { /* pong 대기 중 */}

                    } else {
                        send(PING_MESSAGE);
                        bWaitingPong = true;
                        lastPingTime = currentTime;
                    }
                }

                try { 
                    if (in == null) {
                        System.err.println("P2P: 입력 스트림이 null입니다");
                        handleNetworkError(new IOException("입력 스트림이 null입니다"));
                        break;
                    }
                    if(!in.ready()) continue; 
                } catch (IOException e) { 
                    System.err.println("P2P: 입력 스트림 확인 중 오류 - " + e.getMessage());
                    handleNetworkError(e);
                    break; 
                }
                
                String message = "";
                try { 
                    message = in.readLine(); 
                } catch (IOException ex) { 
                    System.err.println("P2P: 메시지 읽기 중 오류 - " + ex.getMessage());
                    handleNetworkError(ex);
                    break; 
                }
                if(message == null) {
                    System.out.println("P2P: 연결이 종료되었습니다 (null 메시지 수신)");
                    handleNetworkError(new IOException("연결이 종료되었습니다"));
                    break;
                }

                lastReceiveTime = System.currentTimeMillis();
                if(message.equals(RELEASE_MESSAGE)) { 
                    send(RELEASE_MESSAGE);
                    break; 
                } else if(message.equals(PING_MESSAGE)) {
                    send(PONG_MESSAGE);
                    continue;
                } else if(message.equals(PONG_MESSAGE)) {
                    bWaitingPong = false;
                    lastPingTime = -1;
                    continue;
                }

                for(String key : callbacks.keySet()) {
                    if(message.startsWith(key)) {
                        callbacks.get(key).accept(message.substring(key.length()));
                        break;
                    }
                }

            }
            // 정상 종료가 아닌 경우에만 release 호출 (이미 handleNetworkError에서 처리했을 수 있음)
            if (bRunning || !isHandlingError) {
                release();
            }
        }).start();
    }


    public void addCallback(String message, Consumer<String> callback) {
        callbacks.put(message, callback);
        for(String key : callbacks.keySet()) {
            System.out.println("key: " + key);
        }
    }

    public void removeCallback(String message) {
        callbacks.remove(message);
        for(String key : callbacks.keySet()) {
            System.out.println("key: " + key);
        }
    }

    public void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }



}
