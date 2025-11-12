package tetris.network;

import java.io.*;
import java.net.*;

public class P2PClient extends P2PBase {
    public boolean connect(String HOST) {
        try { socket = new Socket(HOST, PORT); }
        catch (IOException e) { return false; }
        try {
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        networkThread = new Thread(()-> read());
        networkThread.start();
        return true;
    }

    void read() {
        try {
            String line;
            while ((line = in.readLine()) != null) { 
                System.out.println(line);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    void write(String message) {
        try { out.write(message + "\n"); out.flush(); }
        catch (IOException ex) { ex.printStackTrace(); }
    }
}
