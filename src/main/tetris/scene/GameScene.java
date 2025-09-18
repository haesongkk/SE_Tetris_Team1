package tetris.scene;

import tetris.framework.Render;

public class GameScene implements Scene {
    @Override public void initialize(){ 
        Render.get().setClearColor(0, 0.f, 0, 1);
    }
    @Override public boolean update(double dt){
        Render.get().setColor(0, 1.f, 0, 1);
        Render.get().drawText(400,200, "다음씬", -1);
        return true;
    }
    @Override public void finalize(){ }
    
}
