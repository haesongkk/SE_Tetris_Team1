package tetris.util;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class EscapeHandler implements AWTEventListener, KeyEventDispatcher{
    Runnable escapeSignalCallback;

    public EscapeHandler(Runnable escapeSignalCallback){
        this.escapeSignalCallback = escapeSignalCallback;
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(this);
    }

    public void release() {
        System.out.println("escape handler release");
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .removeKeyEventDispatcher(this);
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (event instanceof MouseEvent me) {
            //System.out.println("MOUSE EVENT");

            int id = me.getID();
            Component src = me.getComponent();
            if (src instanceof JButton) return;
            if (src instanceof JTextField) return;
            
            if (id == MouseEvent.MOUSE_RELEASED) {
                System.out.println("MOUSE clicked");
                this.escapeSignalCallback.run();    
            }
            me.consume();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            escapeSignalCallback.run();
            return true;
        }
        return false;
    }
    
}
