# SE_Tetris_Team1

## 프로젝트 개요
Java Swing을 이용한 테트리스 게임 구현 프로젝트입니다.

## 팀 정보
- **팀명**: Team1
- **프로젝트명**: SE_Tetris_Team1

## 개발 환경
- **언어**: Java 17
- **GUI 프레임워크**: Java Swing
- **빌드 도구**: Gradle
- **테스트 프레임워크**: JUnit 5
- **코드 커버리지**: JaCoCo
- **IDE**: Visual Studio Code

## 프로젝트 구조
```
src/
├── main/java/tetris/
│   ├── Game.java                    # 메인 게임 관리 클래스 (싱글톤)
│   ├── Tetris.java                  # 프로그램 진입점
│   ├── Global.java                  # 전역 설정
│   ├── util/
│   │   ├── Animation.java           # 애니메이션 유틸리티
│   │   └── Theme.java               # 테마 관리
│   └── scene/                       # 게임 씬 관리
│       ├── Scene.java               # 씬 인터페이스
│       ├── test/TestScene.java      # 테스트 씬
│       ├── game/                    # 게임 플레이 씬
│       │   ├── GameScene.java
│       │   ├── blocks/              # 테트리스 블록들
│       │   │   ├── Block.java       # 블록 기본 클래스
│       │   │   ├── IBlock.java      # I형 블록
│       │   │   ├── OBlock.java      # O형 블록
│       │   │   ├── TBlock.java      # T형 블록
│       │   │   ├── SBlock.java      # S형 블록
│       │   │   ├── ZBlock.java      # Z형 블록
│       │   │   ├── JBlock.java      # J형 블록
│       │   │   └── LBlock.java      # L형 블록
│       │   └── overlay/             # 게임 오버레이
│       └── scorescene/              # 점수 화면
└── test/java/tetris/
    └── GameTest.java                # 단위 테스트
```

## 주요 기능

### 1. 게임 시스템
- **싱글톤 패턴**: Game 클래스를 통한 중앙 집중식 게임 관리
- **씬 관리 시스템**: 다양한 게임 상태(테스트, 게임, 점수) 간 전환
- **Java Swing GUI**: 크로스 플랫폼 지원

### 2. 테트리스 블록 시스템
- **7가지 테트리스 블록**: I, O, T, S, Z, J, L 블록 구현
- **블록 상속 구조**: Block 기본 클래스를 상속받는 각 블록 타입
- **색상 시스템**: 각 블록별 고유 색상

### 3. 씬 관리
- **인터페이스 기반**: Scene 인터페이스를 통한 일관된 씬 관리
- **생명주기 관리**: onEnter(), onExit() 메서드를 통한 씬 전환 처리

## 빌드 및 실행

### 전제 조건
- Java 17 이상
- Gradle (gradlew 포함)

### 빌드 명령어
```bash
# 프로젝트 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 게임 실행
./gradlew run
# 또는
./gradlew runGame

# JAR 파일 생성
./gradlew jar
```

### IDE에서 실행
1. VS Code에서 프로젝트 열기
2. Java Extension Pack 설치
3. `src/main/java/tetris/Tetris.java` 실행

## 테스트 및 품질 관리

### 단위 테스트
- **JUnit 5** 사용
- **Mockito** 활용한 모킹
- 현재 구현된 테스트:
  - `GameTest.java`: Game 클래스의 싱글톤 패턴 및 씬 전환 테스트

### 코드 커버리지
- **JaCoCo** 를 통한 코드 커버리지 측정
- **최소 70% 커버리지** 요구사항
- HTML, XML 리포트 생성

```bash
# 커버리지 리포트 생성
./gradlew test jacocoTestReport

# 커버리지 검증
./gradlew jacocoTestCoverageVerification
```

커버리지 리포트 위치: `build/reports/jacoco/test/html/index.html`

## CI/CD

### GitHub Actions
- **자동 빌드**: 모든 브랜치 푸시 시 빌드 실행
- **테스트 자동화**: 빌드 시 자동 테스트 실행
- **의존성 관리**: Dependabot을 통한 의존성 업데이트

## 프로젝트 설정

### Gradle 설정 (`build.gradle`)
- Java 17 호환성
- UTF-8 인코딩 (한글 주석 지원)
- JUnit 5 테스트 환경
- JaCoCo 코드 커버리지
- 실행 가능한 JAR 생성

### VS Code 설정
- Java 17 런타임 설정
- Launch 설정으로 쉬운 디버깅

## 아키텍처 특징

### 디자인 패턴
1. **싱글톤 패턴**: Game 클래스
2. **팩토리 패턴**: 블록 생성 시스템
3. **전략 패턴**: Scene 인터페이스 활용

### 객체지향 설계
- **상속**: Block 클래스를 상속받는 구체적인 블록들
- **다형성**: Scene 인터페이스를 통한 다양한 씬 구현
- **캡슐화**: 각 클래스의 책임 분리

## 향후 개발 계획
- [ ] 게임 로직 완성 (블록 이동, 회전, 라인 클리어)
- [ ] 점수 시스템 구현
- [ ] 사운드 효과 추가
- [ ] 설정 메뉴 구현
- [ ] 추가 테스트 케이스 작성

## 라이선스
이 프로젝트는 교육 목적으로 제작되었습니다.

## 기여자
Team1 구성원들

---
**프로젝트 상태**: 개발 중
**최종 업데이트**: 2025년 9월 29일