package tetris.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Timer;

import java.awt.event.ActionListener;

public class RunLater {
    Timer timer;
    ActionListener listener;
    boolean bCanceled = false;

    static final List<RunLater> counter = new CopyOnWriteArrayList<>();

    public RunLater(float delay, Runnable r) {
        final int delayMs = Math.max(0, Math.round(delay * 1000f));
        this.listener = e -> {
            if(bCanceled) return;
            r.run();
            release();
        };
        timer = new Timer(delayMs, listener);
        timer.setRepeats(false);
        timer.start();
        counter.add(this);
    }

    public synchronized void release() {
        bCanceled = true;
        if (this.timer != null) {
            this.timer.stop();
            this.timer.removeActionListener(this.listener);
            this.listener = null;
            this.timer = null;
        }
        counter.remove(this);
    }

    public static void clear() {
        if (counter.isEmpty()) return;

        System.out.println("미해제 RunLater 객체: " + counter.size());
        for (RunLater r : counter) {
            r.release();
        }
        counter.clear();
    }
}
