package tetris.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;


public class P2PBase {

    final int PORT = 5000;
    public String HOST;

    Socket socket = null;
    BufferedReader in = null;
    BufferedWriter out = null;

    public String receive() {
        try { return in.readLine(); } 
        catch (IOException e) { 
            System.out.println("수신 실패"); return null; 
        }
    }

    public void send(String message) {
        try { out.write(message + '\n'); out.flush(); } 
        catch (IOException ex) { 
            System.out.println("전송 실패"); 
        }
    }

    public void release() {
        try {
            if(in != null) in.close();
            if(out != null) out.close();
            if(socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("릴리즈 실패");
        }
    }

}
