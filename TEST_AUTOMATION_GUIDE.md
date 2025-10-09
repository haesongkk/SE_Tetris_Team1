## 🚀 **SE_Tetris_Team1 GitHub Actions 테스트 자동화 설정 완료**

이 가이드는 **SE_Tetris_Team1** 프로젝트의 테스트 코드를 GitHub의 commit/merge 시 자동으로 실행되도록 설정하는 방법을 설명합니다.

### 📁 **추가된 파일들**

#### 1. **GitHub Actions 워크플로우**
**파일**: `.github/workflows/test-automation.yml`

**기능**:
- ✅ **자동 테스트 실행**: `main`, `develop` 브랜치에 push/PR 시 자동 실행
- ✅ **JUnit 5 테스트**: 표준 JUnit 테스트 자동 실행
- ✅ **커스텀 테스트**: `BasicTest.java`, `GameScreenTest.java` 실행
- ✅ **테스트 리포트**: HTML 및 XML 형태로 결과 제공
- ✅ **아티팩트 저장**: JAR 파일 및 테스트 로그 자동 저장

#### 2. **JUnit 5 표준 테스트**
**파일**: `src/test/java/tetris/BasicJUnitTest.java`

**기능**:
- ✅ **표준 JUnit 5 어노테이션**: `@Test`, `@DisplayName` 등 사용
- ✅ **CI 환경 대응**: 헤드리스 모드 자동 감지
- ✅ **상세한 테스트 결과**: 각 테스트 케이스별 상세 정보 제공

#### 3. **Gradle 빌드 설정 개선**
**파일**: `build.gradle`

**추가된 기능**:
- ✅ **JaCoCo 테스트 커버리지**: 코드 커버리지 측정
- ✅ **테스트 리포트**: HTML/XML 형태의 상세 리포트
- ✅ **CI 환경 대응**: GitHub Actions 최적화

---

### 🎯 **사용 방법**

#### 1. **자동 실행 조건**
다음 상황에서 테스트가 **자동으로 실행**됩니다:

```bash
# main 또는 develop 브랜치에 push할 때
git push origin main
git push origin develop

# Pull Request 생성/업데이트할 때
git checkout -b feature/new-feature
git push origin feature/new-feature
# → GitHub에서 PR 생성 시 자동 실행
```

#### 2. **로컬에서 동일한 테스트 실행**
```bash
# JUnit 5 테스트 실행
./gradlew test

# 커스텀 테스트 실행
java -cp "build/classes/java/main:build/classes/java/test" tetris.BasicTest

# 테스트 커버리지 리포트 생성
./gradlew jacocoTestReport
```

#### 3. **GitHub에서 결과 확인**
1. **GitHub Repository** → **Actions** 탭 이동
2. **최근 워크플로우 실행** 클릭
3. **테스트 결과 확인**:
   - ✅ 성공한 테스트
   - ❌ 실패한 테스트
   - 📊 테스트 커버리지
   - 📄 상세 로그

---

### 📊 **테스트 결과 리포트**

#### **HTML 리포트 위치**
```
build/reports/tests/test/index.html    # JUnit 테스트 결과
build/reports/jacoco/test/html/index.html    # 커버리지 리포트
```

#### **GitHub Actions 아티팩트**
- 🗂️ **tetris-jar**: 컴파일된 JAR 파일
- 📋 **test-logs**: 테스트 실패 시 상세 로그

---

### 🔧 **커스터마이징**

#### **브랜치 변경**
워크플로우가 실행될 브랜치를 변경하려면:

```yaml
# .github/workflows/test-automation.yml
on:
  push:
    branches: [ "main", "develop", "your-branch" ]  # 여기에 브랜치 추가
  pull_request:
    branches: [ "main", "develop", "your-branch" ]
```

#### **테스트 시간 제한 변경**
```yaml
# .github/workflows/test-automation.yml
- name: Run JUnit Tests
  run: ./gradlew test
  timeout-minutes: 15  # 15분으로 변경
```

#### **테스트 실패 시 동작 변경**
```yaml
# 테스트 실패해도 계속 진행
continue-on-error: true

# 테스트 실패하면 워크플로우 중단
continue-on-error: false
```

---

### 🎉 **검증된 테스트 항목**

#### **BasicTest.java / BasicJUnitTest.java**
1. ✅ **시작 메뉴에서 게임 시작** 선택 시 테트리스 게임 시작
2. ✅ **20줄, 10칸의 보드(board)** 존재
3. ✅ **총 7가지의 테트로미노(블럭)**가 무작위로 등장
4. ✅ **블럭을 쌓아 각 행을 채우면** 해당 행이 삭제됨

#### **GameScreenTest.java**
1. ✅ **블럭이 쌓이는 보드(board)** - 20줄 × 10칸
2. ✅ **다음 블럭을 확인할 수 있는 부분**
3. ✅ **점수를 확인할 수 있는 부분**
4. ✅ **실시간으로 바뀌는 점수를 표시**

---

### 🚨 **주의사항**

1. **헤드리스 모드**: CI 환경에서는 GUI가 없으므로 헤드리스 모드로 실행됩니다.
2. **테스트 시간**: GUI 테스트는 시간이 오래 걸릴 수 있습니다.
3. **브랜치 보호**: `main` 브랜치에 테스트 통과를 필수로 설정할 수 있습니다.

---

### 📞 **문제 해결**

#### **테스트 실패 시**
1. **로컬에서 먼저 테스트**: `./gradlew test`
2. **로그 확인**: GitHub Actions의 상세 로그 확인
3. **아티팩트 다운로드**: 실패 로그 다운로드하여 분석

#### **헤드리스 모드 문제**
```java
// 테스트 코드에서 GUI 사용 시
if (!GraphicsEnvironment.isHeadless()) {
    // GUI 사용 코드
} else {
    // 헤드리스 모드 대체 코드
}
```

이제 **모든 commit과 PR에서 테스트가 자동으로 실행**되어 코드 품질을 보장할 수 있습니다! 🎯