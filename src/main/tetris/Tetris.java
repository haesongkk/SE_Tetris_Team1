package tetris;

import tetris.framework.Game;
import tetris.framework.Input;
import tetris.framework.Render;

public class Tetris {
    public Tetris() {
        long m_prevTime = System.nanoTime();
        Input.get().initialize();
        Render.get().initialize();
        Game.get().initialize();

        while(true) {
            long now = System.nanoTime();
            double deltaTima = (now - m_prevTime) / 1_000_000_000.0;
            m_prevTime = now;
            if(!Input.get().update(deltaTima)) break;
            if(!Render.get().update(deltaTima)) break;
            if(!Game.get().update(deltaTima)) break;
        }

        Input.get().finalize();
        Render.get().finalize();
        Game.get().finalize();
    }
}
