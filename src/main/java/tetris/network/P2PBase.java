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
        try {
            bRunning = false;
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
    private Consumer<Long> onLatencyUpdate; // 지연 시간 업데이트 콜백 추가
    private long lastReceivedTimestamp = 0; // 마지막 수신 시간

    protected void run() {
        bRunning = true;
        new Thread(() -> {
            while(bRunning) {
                String message = "";
                try { message = in.readLine(); } 
                catch (IOException ex) { break;  }
                if(message == null) { break; }

                // 메시지 수신 시 타임스탬프 업데이트
                long currentTime = System.currentTimeMillis();
                long latency = 0;
                
                if (lastReceivedTimestamp > 0) {
                    latency = currentTime - lastReceivedTimestamp;
                }
                lastReceivedTimestamp = currentTime;
                
                // 지연 시간 콜백 호출
                if (onLatencyUpdate != null && latency > 0) {
                    final long finalLatency = latency;
                    onLatencyUpdate.accept(finalLatency);
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
            System.out.println("release");
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

    /**
     * 지연 시간 업데이트 콜백을 설정합니다
     * @param onLatencyUpdate 지연 시간(ms)을 받는 콜백
     */
    public void setOnLatencyUpdate(Consumer<Long> onLatencyUpdate) {
        this.onLatencyUpdate = onLatencyUpdate;
    }

    /**
     * 현재 추정 지연 시간을 반환합니다
     * @return 마지막 수신 이후 경과 시간 (ms)
     */
    public long getEstimatedLatency() {
        if (lastReceivedTimestamp == 0) return 0;
        return System.currentTimeMillis() - lastReceivedTimestamp;
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
