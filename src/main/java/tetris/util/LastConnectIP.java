package tetris.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LastConnectIP {
    private static final String FILE_NAME = "last_connect_ip.txt";
    private static final String DEFAULT_IP = "192.168.1.100";

    public static String load () {
        File file = getFile();
        if(file.length() == 0) return DEFAULT_IP;

        try { return getLine(file); }
        catch (IOException e) { return DEFAULT_IP; }
    }

    public static void save (String ip) {
        File file = getFile();
        try { saveLine(file, ip); }
        catch (IOException e) { System.err.println("Failed to save last connect IP: " + e.getMessage()); }
    }

    private static File getFile() {
        return DataPathManager.getInstance()
            .getOrCreateDataFile(FILE_NAME);
    }

    private static String getLine(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        br.close();
        return line;
    }

    private static void saveLine(File file, String line) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
        bw.write(line);
        bw.close();
    }
    
}
