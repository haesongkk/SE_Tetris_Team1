package tetris.util;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.Player;
import java.io.InputStream;

// ONLY MP3
public class Sound {
    static int counter = 0;
    
    String origin = null; 
    volatile boolean running = false;
    volatile Thread thread = null;
    SoundDevice device = null;

    public Sound(String filePath) {
        this.origin = filePath;
        device = new SoundDevice();
    }

    public synchronized void play(boolean loop) {
        release();
        device = new SoundDevice();

        this.running = true;
        thread = new Thread(() -> {
            while (this.running) {

                // 1) 리소스 로드
                InputStream in = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(origin);
                        
                if (in == null) {
                    System.err.println("리소스 로드 실패: " + origin);
                    break;
                }

                // 2) player 객체 생성 및 재생
                Player player = null;
                try { 
                    player = new Player(in, device); 
                    while (this.running) 
                        if(!player.play(1)) break;
                } catch(JavaLayerException e) {
                    System.out.println("player 에러");
                    this.running = false;
                } finally {
                    if(player != null) {
                        try { player.close(); } 
                        catch (Exception ignored) {}
                        player = null;
                    }

                    if(in != null) {
                        try { in.close(); }
                        catch (Exception ignored) {}
                    }
                }
                if (!loop) break;
            }
        }, "mp3-play-thread");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void stop() {
        this.running = false;
    }

    public synchronized void release() {
        running = false;
        if(thread != null) {
            thread.interrupt();
            // 스레드 실제로 종료되기 전 참조 삭제 방지
            try { thread.join(500); } 
            catch (InterruptedException ignored) {}
            thread = null;
        }
        if(device != null){
            device.close();
            device = null;
        }

    }
}

class SoundDevice extends JavaSoundAudioDevice { 
    static private volatile float volume = 0.2f; 
    static public void setVolume(float v){ volume = Math.max(0f, Math.min(1f, v)); } 
    @Override 
    public void write(short[] s, int off, int len) throws JavaLayerException { 
        if (volume!=1f){ 
            for(int i=off, end=off+len;i<end;i++){ 
                int v = Math.round(s[i]*volume); 
                if(v>Short.MAX_VALUE) v=Short.MAX_VALUE; 
                else if(v<Short.MIN_VALUE) v=Short.MIN_VALUE; 
                s[i]=(short)v; 
            }
        } 
        super.write(s, off, len); 
    } 
}
