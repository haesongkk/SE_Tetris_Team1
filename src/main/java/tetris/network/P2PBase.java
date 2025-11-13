package tetris.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;


public class P2PBase {

    final int PORT = 5000;
    public String HOST;

    Socket socket;
    BufferedReader in;
    BufferedWriter out;

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

}
