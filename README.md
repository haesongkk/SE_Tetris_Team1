# SE_Tetris_Team1

테트리스 게임 프로젝트 - 아이템 시스템, 난이도 설정, 색맹 모드 지원

## 🎮 주요 기능
- **아이템 시스템**: 빗자루, 폭탄, 투명화, 달팽이 등 다양한 아이템
- **난이도 모드**: EASY, NORMAL, HARD (블록 생성 확률 차별화)
- **색맹 모드**: 블록 색상 구분이 어려운 사용자를 위한 접근성 지원
- **Fitness Proportionate Selection**: 난이도별 블록 생성 확률 조정
- **사용자 데이터 관리**: 설정과 하이스코어를 사용자 홈 디렉토리에 안전하게 저장

## 📦 빌드 및 배포

### 1. JAR 파일 빌드
```powershell
.\gradlew jar
```
생성 위치: `build/libs/SE_Tetris_Team1.jar`

### 2. 게임 실행
```powershell
java -jar build/libs/SE_Tetris_Team1.jar
```

### 3. 네이티브 실행 파일 생성 (Windows)
```powershell
# 아이콘 포함 실행 파일 빌드
.\gradlew jpackageExe
```
생성 위치: `dist/Tetris Game/Tetris Game.exe`

**특징:**
- JRE 17 번들 포함 (Java 설치 불필요)
- 아이콘이 적용된 Windows 실행 파일
- 독립 실행 가능한 배포 패키지

### 4. 테스트 실행
```powershell
# 전체 테스트 (138개 + 데이터 경로 관리 테스트)
.\gradlew test

# Jacoco 코드 커버리지 리포트 생성
.\gradlew jacocoTestReport
```
리포트 위치: `build/reports/jacoco/test/html/index.html`

## 💾 데이터 파일 관리

### 자동 경로 관리
게임 데이터(설정, 하이스코어)는 OS별로 표준 위치에 자동 저장됩니다:

**Windows:**
```
%APPDATA%\Tetris Game\data\
C:\Users\{사용자명}\AppData\Roaming\Tetris Game\data\
```

**macOS:**
```
~/Library/Application Support/Tetris Game/data/
```

**Linux:**
```
~/.config/Tetris Game/data/
```

### 저장되는 파일
- **settings.txt**: 게임 설정 (해상도, 난이도, 키 설정, 음량 등)
- **highscore_v2.txt**: 하이스코어 기록 (난이도별 분리)
- **highscore.txt**: 레거시 스코어 파일

### 데이터 위치 확인
```powershell
# Windows에서 데이터 폴더 열기
explorer "$env:APPDATA\Tetris Game\data"

# 파일 목록 확인
Get-ChildItem "$env:APPDATA\Tetris Game\data"
```

## � 배포 패키지 테스트

상세한 테스트 절차는 [`DEPLOYMENT_TEST_GUIDE.md`](DEPLOYMENT_TEST_GUIDE.md)를 참조하세요.

**빠른 테스트:**
```powershell
# 1. 실행 파일 빌드
.\gradlew jpackageExe

# 2. 실행 파일 실행
cd "dist\Tetris Game"
.\Tetris` Game.exe

# 3. 데이터 파일 확인
Get-ChildItem "$env:APPDATA\Tetris Game\data"
```

## �🛠️ 개발 환경
- **Java**: JDK 17
- **빌드 도구**: Gradle 9.1.0
- **테스트 프레임워크**: JUnit Jupiter 5.10.2
- **품질 검사**: SpotBugs, Jacoco
- **배포**: jpackage (JEP 392)

## 📁 프로젝트 구조
```
src/main/java/tetris/
  ├── Game.java               # 게임 엔진 코어
  ├── BlockManager.java       # 블록 생성 및 FPS 알고리즘
  ├── ScoreManager.java       # 점수 시스템
  ├── ItemManager.java        # 아이템 시스템
  ├── ColorBlindHelper.java   # 색맹 모드 지원
  └── util/
      └── DataPathManager.java # 데이터 파일 경로 관리

src/main/resources/
  └── defaults/
      └── settings.txt        # 기본 설정 파일

dist/                           # 배포 패키지 출력 디렉토리
  └── Tetris Game/
      ├── Tetris Game.exe     # Windows 실행 파일
      ├── app/                # 게임 JAR
      └── runtime/            # 번들된 JRE 17
```

## 🎨 리소스 출처

[8-bit-game-music-122259.mp3](https://pixabay.com/ko/music/%EB%B9%84%EB%94%94%EC%98%A4-%EA%B2%8C%EC%9E%84-8-bit-game-music-122259/)

[gameboy-pluck-41265.mp3](https://pixabay.com/ko/sound-effects/gameboy-pluck-41265/)

[Giants-Bold.ttf 외 2개](https://www.giantsclub.com/html/?pcode=1007)

[arcade-beat-323176.mp3](https://pixabay.com/ko/music/%EB%B9%84%EB%94%94%EC%98%A4-%EA%B2%8C%EC%9E%84-arcade-beat-323176/)

## 📝 추가 문서

- **배포 테스트 가이드**: [`DEPLOYMENT_TEST_GUIDE.md`](DEPLOYMENT_TEST_GUIDE.md)
- **테스트 자동화 가이드**: [`TEST_AUTOMATION_GUIDE.md`](TEST_AUTOMATION_GUIDE.md)

