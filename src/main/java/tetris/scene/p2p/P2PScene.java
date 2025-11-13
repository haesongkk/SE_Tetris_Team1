package tetris.scene.p2p;

import javax.swing.JFrame;
import java.util.Timer;

import tetris.network.P2PBase;
import tetris.scene.Scene;
import tetris.scene.battle.BattleScene;

import com.google.gson.Gson;

/**
 * P2P 네트워크 대전 씬
 * BattleScene을 활용하여 로컬 대전과 동일한 기능을 제공하며,
 * 네트워크를 통해 원격 플레이어와 대전합니다.
 */
public class P2PScene extends Scene {

    private final P2PBase p2p;
    private final BattleScene battleScene;
    private Timer syncTimer;
    private Thread receiveThread;

    public P2PScene(JFrame frame, P2PBase p2p) {
        super(frame);
        this.p2p = p2p;
        
        // BattleScene 생성 (P2P 모드)
        this.battleScene = new BattleScene(frame, "P2P");
        
        // BattleScene을 컨텐츠로 설정
        setLayout(new java.awt.BorderLayout());
        add(battleScene, java.awt.BorderLayout.CENTER);
        
        frame.setContentPane(this);
        frame.revalidate();
        frame.repaint();
        
        // 네트워크 동기화 시작
        setupNetworkSync();
    }
    
    /**
     * 네트워크 동기화 설정
     * - 로컬 플레이어(1P)의 상태를 전송
     * - 원격 플레이어의 상태를 받아서 2P에 적용
     */
    private void setupNetworkSync() {
        // TODO: BattleScene의 1P 상태를 네트워크로 전송
        // TODO: 네트워크에서 받은 상태를 2P에 적용
        
        // 상태 전송 타이머 (100ms마다)
        syncTimer = new Timer();
        syncTimer.scheduleAtFixedRate(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    // TODO: 1P 게임 상태 전송
                    // String stateJson = serializePlayer1State();
                    // p2p.send(stateJson);
                }
            },
            0, 100
        );
        
        // 상태 수신 스레드
        receiveThread = new Thread(() -> {
            while (true) {
                String json = p2p.recieve();
                if (json != null) {
                    // TODO: 받은 상태를 2P에 적용
                    // applyToPlayer2(json);
                }
            }
        });
        receiveThread.start();
    }
    
    @Override
    public void onEnter() {
        battleScene.onEnter();
    }
    
    @Override
    public void onExit() {
        // 네트워크 동기화 중지
        if (syncTimer != null) {
            syncTimer.cancel();
        }
        if (receiveThread != null) {
            receiveThread.interrupt();
        }
        
        battleScene.onExit();
    }
}
