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
        try { out.write(message + '\n'); out.flush(); } 
        catch (IOException ex) { }
     }

    public void release() {
        if(onDisconnect != null) {
            onDisconnect.run();
            onDisconnect = null;
        }
        send(RELEASE_MESSAGE);
        callbacks.clear();
        bRunning = false;
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
    private Runnable onDisconnect;
    private final String RELEASE_MESSAGE = "release";
    private long lastReceiveTime = -1;
    private final int TIMEOUT_MS = 5000;
    private final String PING_MESSAGE = "ping";
    private final String PONG_MESSAGE = "pong";
    private boolean bWaitingPong = false;
    private long lastPingTime = -1;


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

                try { if(!in.ready()) continue; } 
                catch (IOException e) { break; }
                
                String message = "";
                try { message = in.readLine(); } 
                catch (IOException ex) { break; }
                if(message == null) break;

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
            release();
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
