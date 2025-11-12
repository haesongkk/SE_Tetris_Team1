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

    public String recieve() {
        try {
            return in.readLine();
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public void send(String message) {
        try { out.write(message + '\n'); out.flush(); } 
        catch (IOException ex) { ex.printStackTrace(); }
    }

}
