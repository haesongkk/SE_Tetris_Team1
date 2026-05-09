# SE_Tetris_Team1

`SE_Tetris_Team1`은 소프트웨어공학 팀 프로젝트로 개발한 Java 기반 테트리스 게임입니다.

기본 테트리스 플레이를 중심으로 싱글 플레이, 아이템 모드, 로컬 2인 배틀 모드, P2P 네트워크 배틀 모드, 난이도 설정, 색맹 모드, 사용자 설정 저장, 하이스코어 관리 기능을 구현했습니다.

## 실행 화면

[![](https://img.youtube.com/vi/SyxkUnvwehw/maxresdefault.jpg)](https://youtu.be/SyxkUnvwehw)

> 위 이미지를 클릭하면 플레이 영상을 확인할 수 있습니다.

## 주요 기능

### 기본 테트리스 플레이

* 블록 이동, 회전, 빠른 낙하, 하드 드롭 기능을 지원합니다.
* 완성된 줄은 제거되며, 제거한 줄 수에 따라 점수가 증가합니다.
* 다음 블록 미리보기와 현재 점수 표시를 제공합니다.
* 게임 오버 시 결과를 확인하고 점수를 저장할 수 있습니다.

### 난이도 선택

* `EASY`, `NORMAL`, `HARD` 난이도를 지원합니다.
* 난이도에 따라 게임 진행 속도와 블록 생성 방식이 달라집니다.
* 사용자는 자신의 숙련도에 맞는 난이도를 선택할 수 있습니다.

### 아이템 모드

* 일반 테트리스에 아이템 효과를 추가한 모드입니다.
* 줄 삭제, 청소, 속도 감소, 속도 증가, 시야 제한 등의 아이템 효과를 제공합니다.
* 아이템 블록을 통해 일반 모드보다 변수가 많은 플레이를 할 수 있습니다.

### 2인 배틀 모드

* 하나의 화면에서 두 명의 플레이어가 동시에 테트리스를 플레이할 수 있습니다.
* 1P와 2P는 각각 독립된 보드, 점수, 블록 상태를 가집니다.
* 일정 줄 이상을 제거하면 상대방에게 방해 블록을 보낼 수 있습니다.
* 아이템 배틀 모드와 시간 제한 모드도 지원합니다.

### P2P 네트워크 배틀

* 서버/클라이언트 방식의 P2P 연결을 통해 네트워크 배틀을 진행할 수 있습니다.
* 방 생성 및 참가 방식으로 상대 플레이어와 연결합니다.
* 네트워크 상태 표시 기능을 통해 연결 상태를 확인할 수 있습니다.

### 색맹 모드

* 일반 모드 외에 적록색맹 모드와 청황색맹 모드를 지원합니다.
* 색상 팔레트를 변경하고, 블록별 패턴을 함께 표시하여 색상만으로 구분하기 어려운 상황을 보완합니다.

### 사용자 설정 저장

* 해상도, 화면 모드, 색맹 모드, 난이도, 키 설정, 음량 등의 설정을 저장합니다.
* 설정 파일은 사용자 환경에 맞는 OS별 표준 경로에 저장됩니다.
* 게임을 다시 실행해도 이전 설정을 유지할 수 있습니다.

### 하이스코어 관리

* 게임 결과를 하이스코어 파일에 저장합니다.
* 난이도별 점수 기록을 관리할 수 있습니다.
* 설정 메뉴에서 스코어보드를 초기화할 수 있습니다.

## 기술 스택

* Language: Java 17
* GUI: Java Swing / AWT
* Build Tool: Gradle
* Test Framework: JUnit Jupiter
* Code Coverage: Jacoco
* Static Analysis: SpotBugs
* Code Style: Checkstyle
* Audio Library: JLayer
* Data Format: Text File, Gson
* Packaging: jpackage

## 프로젝트 구조

```text
SE_Tetris_Team1/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── data/
│   ├── settings.txt
│   ├── highscore.txt
│   └── highscore_v2.txt
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tetris/
│   │   │       ├── Tetris.java
│   │   │       ├── Game.java
│   │   │       ├── GameSettings.java
│   │   │       ├── ColorBlindHelper.java
│   │   │       ├── network/
│   │   │       │   ├── P2PBase.java
│   │   │       │   ├── P2PServer.java
│   │   │       │   ├── P2PClient.java
│   │   │       │   └── NetworkStatusDisplay.java
│   │   │       ├── scene/
│   │   │       │   ├── Scene.java
│   │   │       │   ├── menu/
│   │   │       │   │   ├── MainMenuScene.java
│   │   │       │   │   ├── SettingsScene.java
│   │   │       │   │   └── P2PRoomDialog.java
│   │   │       │   ├── game/
│   │   │       │   │   ├── GameScene.java
│   │   │       │   │   ├── ItemGameScene.java
│   │   │       │   │   ├── blocks/
│   │   │       │   │   ├── core/
│   │   │       │   │   ├── items/
│   │   │       │   │   └── overlay/
│   │   │       │   ├── battle/
│   │   │       │   │   ├── BattleScene.java
│   │   │       │   │   ├── P2PBattleScene.java
│   │   │       │   │   ├── AttackBlock.java
│   │   │       │   │   └── NetworkStatusDisplay.java
│   │   │       │   └── scorescene/
│   │   │       └── util/
│   │   │           ├── DataPathManager.java
│   │   │           ├── HighScore.java
│   │   │           ├── Sound.java
│   │   │           ├── Theme.java
│   │   │           ├── Animation.java
│   │   │           ├── LineBlinkEffect.java
│   │   │           └── RunLater.java
│   │   └── resources/
│   │       ├── defaults/
│   │       ├── Tetris.ico
│   │       ├── *.mp3
│   │       ├── *.ttf
│   │       └── item image resources
│   └── test/
│       └── java/
│           └── tetris/
│               ├── basic tests
│               ├── battle tests
│               ├── board tests
│               ├── block tests
│               └── network tests
├── config/
│   └── checkstyle/
├── gradle/
│   └── wrapper/
├── TEST_AUTOMATION_GUIDE.md
└── NETWORK_ERROR_HANDLING_SUMMARY.md
```

## 실행 방법

### 1. 요구 환경

* JDK 17 이상
* Gradle Wrapper 사용 가능 환경
* Windows 기준 PowerShell 또는 CMD

### 2. 리포지토리 클론

```bash
git clone https://github.com/haesongkk/SE_Tetris_Team1.git
cd SE_Tetris_Team1
```

### 3. JAR 빌드

Windows PowerShell 기준:

```bash
.\gradlew jar
```

빌드 결과물은 다음 경로에 생성됩니다.

```text
build/libs/SE_Tetris_Team1.jar
```

### 4. JAR 실행

```bash
java -jar build/libs/SE_Tetris_Team1.jar
```

### 5. Windows 실행 파일 생성

```bash
.\gradlew jpackageExe
```

생성 위치는 다음과 같습니다.

```text
dist/Tetris Game/Tetris Game.exe
```

`jpackage`로 생성된 실행 파일은 JRE를 포함한 독립 실행 패키지 형태로 배포할 수 있습니다.

### 6. 테스트 실행

전체 테스트 실행:

```bash
.\gradlew test
```

테스트와 커버리지 리포트 생성:

```bash
.\gradlew jacocoTestReport
```

또는:

```bash
.\gradlew testWithCoverage
```

커버리지 리포트는 다음 경로에서 확인할 수 있습니다.

```text
build/reports/jacoco/test/html/index.html
```

## 리소스 출처

[8-bit-game-music-122259.mp3](https://pixabay.com/ko/music/%EB%B9%84%EB%94%94%EC%98%A4-%EA%B2%8C%EC%9E%84-8-bit-game-music-122259/)

[gameboy-pluck-41265.mp3](https://pixabay.com/ko/sound-effects/gameboy-pluck-41265/)

[Giants-Bold.ttf 외 2개](https://www.giantsclub.com/html/?pcode=1007)

[arcade-beat-323176.mp3](https://pixabay.com/ko/music/%EB%B9%84%EB%94%94%EC%98%A4-%EA%B2%8C%EC%9E%84-arcade-beat-323176/)

[the-return-of-the-8-bit-era-301292.mp3](https://pixabay.com/music/video-games-the-return-of-the-8-bit-era-301292/)
