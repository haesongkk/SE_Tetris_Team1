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
    
    /**
     * 네트워크 타임아웃 및 ping 간격 설정 (밀리초)
     *
     * - TIMEOUT_MS: ping 보낸 후 pong이 오지 않았을 때 "끊겼다"고 판단하는 기준
     * - PING_INTERVAL_MS: 주기적으로 ping을 보내는 간격 (RTT 측정 + 끊김 감지용)
     */
    private static final int TIMEOUT_MS = 5000;      // 5초 이상 pong 없으면 끊김
    private static final int PING_INTERVAL_MS = 500; // 0.5초마다 ping 전송

    // ping/pong 메시지 포맷: ping:<id>, pong:<id>
    private final String PING_PREFIX = "ping:"; 
    private final String PONG_PREFIX = "pong:"; 

    private boolean bWaitingPong = false;
    private long lastPingTime = -1;
    private long pingSeq = 0;                          // ping 시퀀스 ID
    private final Map<Long, Long> pingSentTimeMap = new HashMap<>(); // pingID → 보낸 시각(ms)

    // RTT 측정 결과
    private long lastRttMs = -1;   // 마지막 ping의 RTT(ms)
    private long avgRttMs  = -1;   // 간단한 이동 평균 RTT(ms)

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
        
        new Thread(() -> {
            while (bRunning) {
                long currentTime = System.currentTimeMillis();

                // 1) 주기적인 ping 전송 (RTT 측정 + 끊김 감지용)
                if (!bWaitingPong && (lastPingTime < 0 || currentTime - lastPingTime >= PING_INTERVAL_MS)) {
                    long id = ++pingSeq;
                    pingSentTimeMap.put(id, currentTime);
                    send(PING_PREFIX + id);
                    bWaitingPong = true;
                    lastPingTime = currentTime;
                    // System.out.println("P2P: send ping:" + id);
                }

                // 2) pong 타임아웃 체크
                if (bWaitingPong && currentTime - lastPingTime > TIMEOUT_MS) {
                    System.out.println("타임아웃 발생: ping/pong " + TIMEOUT_MS + "ms 초과");
                    handleNetworkError(new IOException("ping/pong 타임아웃"));
                    break;
                }

                // 3) 입력 스트림 준비 확인
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
                
                String message = null;
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

                // 5) 제어 메시지 처리
                if(message.equals(RELEASE_MESSAGE)) { 
                    send(RELEASE_MESSAGE);
                    break; 
                } else if(message.startsWith(PING_PREFIX)) {
                    // 상대가 보낸 ping:<id> → 그대로 pong:<id>로 돌려줌
                    String idPart = message.substring(PING_PREFIX.length());
                    send(PONG_PREFIX + idPart);
                    continue;
                } else if(message.startsWith(PONG_PREFIX)) {
                    // 내가 보낸 ping:<id>에 대한 응답
                    String idPart = message.substring(PONG_PREFIX.length());
                    try {
                        long id = Long.parseLong(idPart.trim());
                        Long sentTime = pingSentTimeMap.remove(id);
                        if (sentTime != null) {
                            long rtt = currentTime - sentTime;
                            lastRttMs = rtt;
                            if (avgRttMs < 0) avgRttMs = rtt;
                            else avgRttMs = (avgRttMs * 3 + rtt) / 4;
                        }
                    } catch (NumberFormatException ignore) { }
                    bWaitingPong = false;
                    lastPingTime = currentTime;
                    continue;
                }

                // 6) 일반 메시지 콜백 처리
                for(String key : callbacks.keySet()) {
                    if(message.startsWith(key)) {
                        callbacks.get(key).accept(message.substring(key.length()));
                        break;
                    }
                }
            }

            // 정상 종료가 아닌 경우에만 release 호출
            // (IO 오류 등으로 handleNetworkError가 이미 처리한 경우는 건드리지 않음)
            if (bRunning || !isHandlingError) {
                release();
            }
        }).start();
    }


    public void addCallback(String message, Consumer<String> callback) {
        callbacks.put(message, callback);
    }

    public void removeCallback(String message) {
        callbacks.remove(message);
    }

    public void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    /**
     * 마지막 ping에 대한 RTT(ms).
     * 아직 ping/pong이 한 번도 오가지 않았다면 -1을 반환합니다.
     */
    public long getLastRttMs() {
        return lastRttMs;
    }

    /**
     * 최근 여러 번의 RTT를 반영한 이동 평균 값(ms).
     * 아직 측정되지 않았다면 -1을 반환합니다.
     */
    public long getAvgRttMs() {
        return avgRttMs;
    }
}
