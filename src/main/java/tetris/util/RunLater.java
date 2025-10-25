package tetris.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import java.awt.event.ActionListener;

public class RunLater {
    Timer timer;
    ActionListener listener;
    Runnable task;
    boolean bCanceled = false;
    boolean bFired = false;

    static final List<RunLater> counter = new CopyOnWriteArrayList<>();

    public RunLater(float delay, Runnable r) {
        this.task = r;
        final int delayMs = Math.max(0, Math.round(delay * 1000f));
        this.listener = e -> {
            if(bCanceled || bFired) return;
            r.run();
            bFired = true;
            release();
        };
        timer = new Timer(delayMs, listener);
        timer.setRepeats(false);
        timer.start();
        counter.add(this);
    }

    public void runNow() {
        if (bCanceled || bFired) return;
        task.run();
        bFired = true;
        release();
        
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

    public static void runNowAll(){
        for (RunLater r : counter) {
            r.runNow();
        }
    }

    public static void clear() {
        System.out.println("미해제 RunLater 객체 " + counter.size() + "개 해제");
        if (counter.isEmpty()) return;
        for (RunLater r : counter) {
            r.release();
        }
        counter.clear();
    }
}
