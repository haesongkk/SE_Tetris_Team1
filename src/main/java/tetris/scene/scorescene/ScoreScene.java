package tetris.scene.scorescene;
import tetris.scene.Scene;

import tetris.Game;
import tetris.scene.menu.MainMenuScene;
import tetris.util.Animation;
import tetris.util.RunLater;
import tetris.util.Sound;
import tetris.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static java.awt.Toolkit.getDefaultToolkit;

public class ScoreScene extends Scene {
    public ScoreScene(JFrame frame, int highlightRank, String mode) {
        super(frame);
        setBackground(Theme.BG());
        setLayout(new BorderLayout());
        this.frame = frame;

        // 제목 라벨
        titleLabel = new Animation(
            "HIGH SCORE", 
            Theme.getFont(Theme.GIANTS_INLINE, 0.07f), 
            Theme.Block('O'), Theme.BG(), Theme.BG(), 
            0, 0, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );
        titleLabel.setBorder(BorderFactory.createEmptyBorder(24, 0, 12, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // 랭크 패널
        rankPanel = new RankPanel(highlightRank, mode);
        add(rankPanel, BorderLayout.CENTER);

        // 종료 라벨
        exitLabel = new Animation(
            "P R E S S    E S C    T O    E X I T", 
            Theme.getFont(Theme.GIANTS_INLINE, 0.04f), 
            Theme.GRAY, Theme.BG(), Theme.BG(), 
            0, 0, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );
        exitLabel.setBorder(BorderFactory.createEmptyBorder(12, 0, 24, 0));
        add(exitLabel, BorderLayout.SOUTH);



        frame.getRootPane().registerKeyboardAction(
                e -> Game.setScene(new MainMenuScene(frame)),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        frame.setContentPane(this);
        frame.revalidate();
        //frame.repaint();
    }

    public ScoreScene(JFrame frame) {
        this(frame, -1, "Easy");
    }


    void startAnimations() {
        new RunLater(0, () -> titleLabel.popIn(0.8f, 0.8f, 0.3f, 2.f));
        new RunLater(
            0.3f, 
            () -> rankPanel.startAnimations(
                2.5f, 
                () -> new RunLater(0.5f, () -> exitLabel.blink(0.8f, 0.4f))
            )
        );
    }



    Animation titleLabel;
    Animation exitLabel;
    RankPanel rankPanel;

    Sound sound = null;
    JFrame frame;
    AWTEventListener mouseEventListener;

    @Override public void onEnter() {
        startAnimations();

        sound = new Sound("8-bit-game-music-122259.mp3");
        sound.play(true);
        
        // 전역 마우스 이벤트 리스너 등록 - 버튼 외의 클릭 차단
        mouseEventListener = new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseEvent) {
                    MouseEvent mouseEvent = (MouseEvent) event;
                    Component source = mouseEvent.getComponent();
                    
                    // 버튼 클릭은 허용 (Animation 클래스는 JButton을 상속)
                    if (source instanceof ModeBtn) {
                        return; // 버튼 이벤트는 정상 전달
                    }
                    
                    // 버튼이 아닌 곳 클릭 차단
                    if (mouseEvent.getID() == MouseEvent.MOUSE_RELEASED) {
                        Game.setScene(new MainMenuScene(frame));
                    }
                }
            }
        };
        
        // MOUSE_EVENT_MASK로 마우스 이벤트만 감지
        getDefaultToolkit().addAWTEventListener(mouseEventListener, AWTEvent.MOUSE_EVENT_MASK);
    }
    @Override public void onExit() {
        // 마우스 이벤트 리스너 제거
        if (mouseEventListener != null) {
            getDefaultToolkit().removeAWTEventListener(mouseEventListener);
            mouseEventListener = null;
        }
        
        if(titleLabel != null) {
            titleLabel.release();
            titleLabel = null;
        }

        if(exitLabel != null) {
            exitLabel.release();
            exitLabel = null;
        }

        if(rankPanel != null) {
            rankPanel.release();
            rankPanel = null;
        }

        if(sound != null) {
            sound.release();
            sound = null;
        }

        RunLater.clear();
    }
}

