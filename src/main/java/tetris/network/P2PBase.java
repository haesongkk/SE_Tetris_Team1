package tetris.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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
        send(RELEASE_MESSAGE);
        onDisconnect = null;
        callbacks.clear();
        bRunning = false;
        try {
            if(in != null) in.close();
            if(out != null) out.close();
            if(socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("릴리즈 실패");
        }
    }

    boolean bRunning = false;

    private Map<String, Consumer<String>> callbacks = new HashMap<>();
    private Runnable onDisconnect;
    private final String RELEASE_MESSAGE = "release";

    protected void run() {
        bRunning = true;
        new Thread(() -> {
            while(bRunning) {
                String message = "";
                try { message = in.readLine(); } 
                catch (IOException ex) { break;  }
                if(message == null) { break; }
                if(message.equals(RELEASE_MESSAGE)) { 
                    send(RELEASE_MESSAGE);
                    break; 
                }

                for(String key : callbacks.keySet()) {
                    if(message.startsWith(key)) {
                        callbacks.get(key).accept(message.substring(key.length()));
                        break;
                    }
                }

            }
            if(onDisconnect != null) onDisconnect.run();
            release();
            System.out.println("p2p release");
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

    public void sync(String message, Runnable callback) {
        AtomicBoolean syncFlag = new AtomicBoolean(false);
        addCallback(message, (data) -> {
            syncFlag.set(true);
        });
        new Thread(() -> {
            do {
                try { Thread.sleep(100); } 
                catch (InterruptedException e) { }
                send(message);
            } while(!syncFlag.get());
            callback.run();
            removeCallback(message);
        }).start();

    }


}
