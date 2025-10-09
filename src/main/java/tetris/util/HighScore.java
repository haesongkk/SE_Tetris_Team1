package tetris.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class HighScore extends HashMap<String, ArrayList<ScoreEntry>> {
    public HighScore(String path) {
        super();
        this.orgin = path;

        List<String> lines = new ArrayList<>();
        try{
            lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // 적절한 예외 처리 필요
            lines = new ArrayList<>();
        } catch (SecurityException e) {
            // 적절한 예외 처리 필요
            lines = new ArrayList<>();
        }

        final String matchInt = "\\s*\\d+\\s*";  // 정수 형식 매칭
        final String matchStr = "[^,]*"; // 문자열 형식 매칭
        final String matchEntry = "^" + matchStr + "," + matchInt + "," + matchInt + "," + matchInt + "$";

        String curKey = null;
        for(String line : lines){
            String trimmed = line.strip();
            if(trimmed.isEmpty()) {

            } else if(trimmed.startsWith("//")) {

            } else if(trimmed.startsWith("#")){
                curKey = line.substring(1).strip().toLowerCase();
                super.put(curKey, new ArrayList<ScoreEntry>());
            } else if(trimmed.matches(matchEntry)) {
                if(!super.containsKey(curKey)) throw new IllegalArgumentException("Mode is not set: " + line);
                super.get(curKey).add(new ScoreEntry(line));
            } else {

            }
        }

        for(String mode : super.keySet()){
            System.out.println(mode);
            for(ScoreEntry entry : super.get(mode)){
                System.out.println(entry.userName + " " + entry.score + " " + entry.removedLines + " " + entry.timeSeconds);
            }
        }
    }

    public int add(String mode, int score, int removedLines, int timeSeconds){
        // 1) 키 값 mode에 해당하는 순위 리스트 반환 (없다면 생성한다)
        ArrayList<ScoreEntry> list = this.computeIfAbsent(mode.toLowerCase(), k -> new ArrayList<ScoreEntry>());

        // 2) 새 점수 엔트리 생성
        ScoreEntry entry = new ScoreEntry(score, removedLines, timeSeconds);

        // 3) 새 점수가 삽입될 인덱스 계산
        int idx = Collections.binarySearch(list, entry);
        final int insertPos = idx >= 0 ? idx : -idx - 1;

        // 4) 새 점수 삽입
        list.add(insertPos, entry);

        // 5) 최대 개수 초과 시 마지막 점수 제거
        if(list.size() > maxCount) list.remove(maxCount);

        return insertPos;

    }

    public void updateUserName(String mode, int idx, String userName){
        // 1) 모드 존재 확인
        if(!super.containsKey(mode.toLowerCase())) throw new IllegalArgumentException("Unknown mode: " + mode);

        // 2) 인덱스 범위 확인
        ArrayList<ScoreEntry> list = super.get(mode.toLowerCase());
        if(idx >= list.size() || idx < 0) throw new IllegalArgumentException("Invalid index: " + idx);

        // 3) 이미 이름이 있는데 수정하는 상황은 허용하지 않음
        String prevUserName = list.get(idx).userName;
        if(prevUserName != null) throw new IllegalArgumentException("User name exists. Cannot overwrite: " + idx);

        list.get(idx).userName = userName;
    }

    public void save() {
        for(ArrayList<ScoreEntry> list : super.values()){
            Collections.sort(list);
        }
        List<String> output = new ArrayList<>();
        for(String mode : super.keySet()){
            ArrayList<ScoreEntry> list = super.get(mode);
            output.add("#" + mode);
            for(ScoreEntry entry : list){
                String entryLine = entry.userName + "," + entry.score + "," + entry.removedLines + "," + entry.timeSeconds;
                output.add(entryLine);
            }
        }
        try {
            Files.write(Paths.get(orgin), output, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            // 적절한 예외 처리 필요
        }
    }

    public List<List<String>> get(String mode){
        ArrayList<ScoreEntry> list = super.computeIfAbsent(mode.toLowerCase(), k -> new ArrayList<ScoreEntry>());
        List<List<String>> output = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            ScoreEntry scoreEntry = list.get(i);

            String strRank = String.valueOf(i + 1);
            String strUserName = scoreEntry.userName;
            String strScore = String.valueOf(scoreEntry.score);
            String strRemovedLines = String.valueOf(scoreEntry.removedLines);
            String strTime = String.format("%02d:%02d", scoreEntry.timeSeconds / 60, scoreEntry.timeSeconds % 60);

            output.add(Arrays.asList(strRank, strUserName, strScore, strRemovedLines, strTime));
        }

        return output;   
    }

    public void release() {
        for(ArrayList<ScoreEntry> list : super.values()){
            for(ScoreEntry entry : list){
                entry = null;
            }
            list = null;
        }
        super.clear();
    }

    final int maxCount = 10;
    String orgin = null;
}


class ScoreEntry implements Comparable<ScoreEntry>{
    String userName = null;
    int score;
    int removedLines;
    int timeSeconds;

    ScoreEntry(String userName, int score, int removedLines, int timeSeconds){
        this.userName = userName;
        this.score = score;
        this.removedLines = removedLines;
        this.timeSeconds = timeSeconds;
    }

    ScoreEntry(int score, int removedLines, int timeSeconds){
        this.score = score;
        this.removedLines = removedLines;
        this.timeSeconds = timeSeconds;
    }

    ScoreEntry(String line) {
        String[] parts = Arrays.stream(line.split(",", -1)).map(String::strip).toArray(String[]::new);

        this.userName = parts[0];
        this.score = Integer.parseInt(parts[1]);
        this.removedLines = Integer.parseInt(parts[2]);
        this.timeSeconds = Integer.parseInt(parts[3]);
    }

    @Override
    public int compareTo(ScoreEntry o) {
        // 1) score: 내림차순 (높을수록 앞)
        int c = Integer.compare(o.score, this.score);
        if (c != 0) return c;
    
        // 2) removedLines: 내림차순 (많을수록 앞)
        c = Integer.compare(o.removedLines, this.removedLines);
        if (c != 0) return c;
    
        // 3) timeSeconds: 오름차순 (짧을수록 앞..?)
        return Integer.compare(this.timeSeconds, o.timeSeconds);
    }
}
