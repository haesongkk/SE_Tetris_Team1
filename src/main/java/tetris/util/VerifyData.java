package tetris.util;

import java.util.List;

public class VerifyData {

    public static boolean verifyHighScore(List<String> highscore) {

        final String tagtoken = "#";
        final String[] tag = { 
            tagtoken + "noraml", tagtoken + "hard", 
            tagtoken + "easy",   tagtoken + "item"
        };

        // 데이터 유효성 체크 (정수형이나 시간 형식 잘 맞는지)
        // split 결과 개수 맞는지
        // 모드 태그 중복 체크
        // 점수가ㅏ 10 개 이하
        // 점수 오름차순 체크
        // 데이터가 없는건 상관 없음
        // 형식을 맞추었다면 true 반환


        // 하나라도 잘못되면 false 반환
        
        return true;
    }
    
}
