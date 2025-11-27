package tetris.scene.menu;


import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;

import tetris.Game;
import tetris.network.*;
import tetris.scene.battle.P2PBattleScene;


public class P2PRoomDialog extends BaseDG {

    private final String TEXT_WAIT_READY = "상대방을 기다리고 있습니다.";
    private final String TEXT_SELECT_MODE = "게임 모드를 선택하세요.";
    private final String TEXT_WAIT_SELECT = "상대방이 모드를 선택하는 중입니다...";

    private final String MODE_TEXTS[] = {
        "일반 모드",
        "아이템 모드",
        "시간 제한 모드"
    };
    private final String MODE_COMMANDS[] = {
        "normal",
        "item",
        "time_limit"
    };


    boolean readyFlag = false;
    boolean startFlag = false;
    int modeFlag = -1; 

    public P2PRoomDialog(JFrame frame, P2PBase p2p) {
        super(frame);

        JPanel container = new DGPanel();
        add(container);

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel();
        JPanel bottomPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        centerPanel.setOpaque(false);
        bottomPanel.setOpaque(false);
        container.add(topPanel, BorderLayout.NORTH);
        container.add(centerPanel, BorderLayout.CENTER);
        container.add(bottomPanel, BorderLayout.SOUTH);

        JLabel titleLabel = new DGTitle("P2P 대전 모드");
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JButton closeButton = new DGButton("닫기");
        bottomPanel.add(closeButton, BorderLayout.CENTER);
        closeButton.addActionListener(e -> onExit(p2p));

        p2p.setOnDisconnect(()-> onDisconnect(p2p));

        onWaitReady(centerPanel, p2p);

                
        super.setVisible(true);
        super.requestFocus();

    }

    private void onWaitReady(JPanel panel, P2PBase p2p) {
        panel.removeAll();
        panel.add(new DGDesc(TEXT_WAIT_READY));
        panel.revalidate();
        panel.repaint();

        final boolean isServer = (p2p instanceof P2PServer);
        sync(p2p, "ready", () -> {
            readyFlag = true;
            SwingUtilities.invokeLater(() -> {
                if(isServer) onSelectMode(panel, p2p);
                else onWaitSelect(panel, p2p);
            });
        });
    }

    private void onSelectMode(JPanel panel, P2PBase p2p) {
        panel.removeAll();
        panel.add(new DGDesc(TEXT_SELECT_MODE) {{
            setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.PLAIN, 12)); // 폰트 크기 절반으로 축소
        }});
        for(int i = 0; i < MODE_TEXTS.length; i++) {
            final int modeIndex = i;
            panel.add(new DGButton(MODE_TEXTS[i]) {{
                addActionListener(e -> {
                    modeFlag = modeIndex;
                    p2p.send("mode:" + Integer.toString(modeIndex));
                    onWaitStart(panel, p2p, modeIndex);
                });
            }});
        }
        panel.revalidate();
        panel.repaint();
    }

    private void onWaitSelect(JPanel panel, P2PBase p2p) {
        panel.removeAll();
        panel.add(new DGDesc(TEXT_WAIT_SELECT) {{
            setFont(new java.awt.Font("Malgun Gothic", java.awt.Font.PLAIN, 12)); // 폰트 크기 절반으로 축소
        }});
        panel.revalidate();
        panel.repaint();
        p2p.addCallback("mode:", (mode)-> {
            modeFlag = Integer.valueOf(mode);
            p2p.removeCallback("mode:");
            SwingUtilities.invokeLater(() -> {
                onWaitStart(panel, p2p, Integer.valueOf(mode));
            });
        });
    }

    private void onWaitStart(JPanel panel, P2PBase p2p, int selectedMode) {
        final JFrame FRAME = (JFrame)super.getOwner();
        panel.removeAll();
        panel.add(new DGDesc(MODE_TEXTS[selectedMode]));
        panel.add(new DGButton("게임 시작") {{
            addActionListener(e -> {
                sync(p2p, "start", () -> {
                    startFlag = true;
                    SwingUtilities.invokeLater(() -> {
                        Game.setScene(new P2PBattleScene(
                            FRAME, 
                            MODE_COMMANDS[selectedMode], 
                            p2p
                        ));

                        // 다이얼로그 닫기
                        P2PRoomDialog.this.dispose();
                    });
                });
            });
        }});
        panel.revalidate();
        panel.repaint();
    }

    private void onExit(P2PBase p2p) {
        System.out.println("P2PRoomDialog: onExit");
        p2p.setOnDisconnect(null);
        p2p.release();
        final JFrame FRAME = (JFrame)super.getOwner();
        this.dispose();
        Game.setScene(new MainMenuScene(FRAME));
    }

    private void onDisconnect(P2PBase p2p) {
        System.out.println("P2PRoomDialog: onDisconnect");
        JOptionPane.showMessageDialog(
            this,
            "상대방과의 연결이 끊어졌습니다.",
            "연결 끊김",
            JOptionPane.ERROR_MESSAGE
        );
        onExit(p2p);
    }

    public void sync(P2PBase p2p, String message, Runnable callback) {
        AtomicBoolean syncFlag = new AtomicBoolean(false);
        p2p.addCallback(message, (data) -> {
            syncFlag.set(true);
        });
        new Thread(() -> {
            do {
                try { Thread.sleep(100); } 
                catch (InterruptedException e) { }
                p2p.send(message);
            } while(!syncFlag.get());
            callback.run();
            p2p.removeCallback(message);
        }).start();

    }
}
   
