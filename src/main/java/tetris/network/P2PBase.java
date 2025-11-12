package tetris.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;


public class P2PBase {

    final int PORT = 5000;
    public String HOST;

    Thread networkThread;
    Socket socket;
    BufferedReader in;
    BufferedWriter out;

    String state;
    char[][] boardTypes;
    char[][] itemTypes;
    char nextBlockType;
    int elapsedSeconds;
    float speedMultiplier;
    float difficultyMultiplier;
    int score;

    void run() { }

}
