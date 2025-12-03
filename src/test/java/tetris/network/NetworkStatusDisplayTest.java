package tetris.network;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

/**
 * NetworkStatusDisplay UI 컴포넌트 검증 자동화 테스트
 * 
 * 테스트 범위:
 * - PingLevel enum 동작 검증
 * - 지연 시간 업데이트 및 표시
 * - 연결 상태 변경 (연결 중, 연결됨, 연결 끊김)
 * - 경고 메시지 표시
 * - UI 컴포넌트 초기화 및 상태 변화
 */
@DisplayName("NetworkStatusDisplay UI 컴포넌트 테스트")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class NetworkStatusDisplayTest {

    private NetworkStatusDisplay statusDisplay;

    @BeforeEach
    @DisplayName("테스트 환경 초기화")
    void setUp() {
        System.out.println("--- 테스트 환경 초기화 ---");
        statusDisplay = new NetworkStatusDisplay();
    }

    @AfterEach
    @DisplayName("테스트 후 정리")
    void tearDown() {
        System.out.println("--- 테스트 후 정리 ---");
        if (statusDisplay != null) {
            statusDisplay = null;
        }
    }

    // ========== PingLevel Enum 테스트 ==========

    @Test
    @DisplayName("1-1. PingLevel - EXCELLENT (0-50ms)")
    void testPingLevelExcellent() {
        System.out.println("--- PingLevel EXCELLENT 테스트 ---");
        
        NetworkStatusDisplay.PingLevel level = NetworkStatusDisplay.PingLevel.fromLatency(0);
        assertEquals(NetworkStatusDisplay.PingLevel.EXCELLENT, level);
        assertEquals(0, level.getLevel());
        assertEquals("매우 좋음", level.getDescription());
        
        level = NetworkStatusDisplay.PingLevel.fromLatency(50);
        assertEquals(NetworkStatusDisplay.PingLevel.EXCELLENT, level);
        
        System.out.println("✓ EXCELLENT 레벨 검증 성공 (0-50ms)");
    }

    @Test
    @DisplayName("1-2. PingLevel - GOOD (51-100ms)")
    void testPingLevelGood() {
        System.out.println("--- PingLevel GOOD 테스트 ---");
        
        NetworkStatusDisplay.PingLevel level = NetworkStatusDisplay.PingLevel.fromLatency(51);
        assertEquals(NetworkStatusDisplay.PingLevel.GOOD, level);
        assertEquals(1, level.getLevel());
        assertEquals("좋음", level.getDescription());
        
        level = NetworkStatusDisplay.PingLevel.fromLatency(100);
        assertEquals(NetworkStatusDisplay.PingLevel.GOOD, level);
        
        System.out.println("✓ GOOD 레벨 검증 성공 (51-100ms)");
    }

    @Test
    @DisplayName("1-3. PingLevel - FAIR (101-150ms)")
    void testPingLevelFair() {
        System.out.println("--- PingLevel FAIR 테스트 ---");
        
        NetworkStatusDisplay.PingLevel level = NetworkStatusDisplay.PingLevel.fromLatency(101);
        assertEquals(NetworkStatusDisplay.PingLevel.FAIR, level);
        assertEquals(2, level.getLevel());
        assertEquals("보통", level.getDescription());
        
        level = NetworkStatusDisplay.PingLevel.fromLatency(150);
        assertEquals(NetworkStatusDisplay.PingLevel.FAIR, level);
        
        System.out.println("✓ FAIR 레벨 검증 성공 (101-150ms)");
    }

    @Test
    @DisplayName("1-4. PingLevel - POOR (151-200ms)")
    void testPingLevelPoor() {
        System.out.println("--- PingLevel POOR 테스트 ---");
        
        NetworkStatusDisplay.PingLevel level = NetworkStatusDisplay.PingLevel.fromLatency(151);
        assertEquals(NetworkStatusDisplay.PingLevel.POOR, level);
        assertEquals(3, level.getLevel());
        assertEquals("나쁨", level.getDescription());
        
        level = NetworkStatusDisplay.PingLevel.fromLatency(200);
        assertEquals(NetworkStatusDisplay.PingLevel.POOR, level);
        
        System.out.println("✓ POOR 레벨 검증 성공 (151-200ms)");
    }

    @Test
    @DisplayName("1-5. PingLevel - VERY_POOR (201ms+)")
    void testPingLevelVeryPoor() {
        System.out.println("--- PingLevel VERY_POOR 테스트 ---");
        
        NetworkStatusDisplay.PingLevel level = NetworkStatusDisplay.PingLevel.fromLatency(201);
        assertEquals(NetworkStatusDisplay.PingLevel.VERY_POOR, level);
        assertEquals(4, level.getLevel());
        assertEquals("매우 나쁨", level.getDescription());
        
        level = NetworkStatusDisplay.PingLevel.fromLatency(1000);
        assertEquals(NetworkStatusDisplay.PingLevel.VERY_POOR, level);
        
        System.out.println("✓ VERY_POOR 레벨 검증 성공 (201ms+)");
    }

    @Test
    @DisplayName("1-6. PingLevel 색상 검증")
    void testPingLevelColors() {
        System.out.println("--- PingLevel 색상 검증 ---");
        
        assertNotNull(NetworkStatusDisplay.PingLevel.EXCELLENT.getColor());
        assertNotNull(NetworkStatusDisplay.PingLevel.GOOD.getColor());
        assertNotNull(NetworkStatusDisplay.PingLevel.FAIR.getColor());
        assertNotNull(NetworkStatusDisplay.PingLevel.POOR.getColor());
        assertNotNull(NetworkStatusDisplay.PingLevel.VERY_POOR.getColor());
        
        // 각 레벨마다 다른 색상을 가져야 함
        assertNotEquals(
            NetworkStatusDisplay.PingLevel.EXCELLENT.getColor(),
            NetworkStatusDisplay.PingLevel.VERY_POOR.getColor()
        );
        
        System.out.println("✓ 모든 PingLevel에 색상이 올바르게 설정됨");
    }

    // ========== 초기화 테스트 ==========

    @Test
    @DisplayName("2-1. NetworkStatusDisplay 초기화")
    void testInitialization() {
        System.out.println("--- 초기화 테스트 ---");
        
        assertNotNull(statusDisplay);
        assertEquals(0, statusDisplay.getCurrentLatency());
        assertEquals(NetworkStatusDisplay.PingLevel.EXCELLENT, statusDisplay.getCurrentPingLevel());
        assertFalse(statusDisplay.isShowingWarning());
        
        System.out.println("✓ 초기 상태: latency=0ms, level=EXCELLENT, warning=false");
    }

    @Test
    @DisplayName("2-2. UI 컴포넌트 생성 확인")
    void testUIComponentsCreation() throws Exception {
        System.out.println("--- UI 컴포넌트 생성 확인 ---");
        
        // Reflection을 사용하여 private 필드 접근
        Field pingIconPanelField = NetworkStatusDisplay.class.getDeclaredField("pingIconPanel");
        pingIconPanelField.setAccessible(true);
        JPanel pingIconPanel = (JPanel) pingIconPanelField.get(statusDisplay);
        assertNotNull(pingIconPanel, "Ping 아이콘 패널이 생성되어야 함");
        
        Field latencyLabelField = NetworkStatusDisplay.class.getDeclaredField("latencyLabel");
        latencyLabelField.setAccessible(true);
        JLabel latencyLabel = (JLabel) latencyLabelField.get(statusDisplay);
        assertNotNull(latencyLabel, "지연 시간 레이블이 생성되어야 함");
        assertEquals("Ping: 0ms", latencyLabel.getText());
        
        Field statusLabelField = NetworkStatusDisplay.class.getDeclaredField("statusLabel");
        statusLabelField.setAccessible(true);
        JLabel statusLabel = (JLabel) statusLabelField.get(statusDisplay);
        assertNotNull(statusLabel, "상태 레이블이 생성되어야 함");
        
        System.out.println("✓ 모든 UI 컴포넌트가 정상적으로 생성됨");
    }

    // ========== 지연 시간 업데이트 테스트 ==========

    @Test
    @DisplayName("3-1. 낮은 지연 시간 업데이트 (30ms)")
    void testUpdateLatencyLow() throws Exception {
        System.out.println("--- 낮은 지연 시간 업데이트 테스트 ---");
        
        statusDisplay.updateLatency(30);
        
        assertEquals(30, statusDisplay.getCurrentLatency());
        assertEquals(NetworkStatusDisplay.PingLevel.EXCELLENT, statusDisplay.getCurrentPingLevel());
        assertFalse(statusDisplay.isShowingWarning());
        
        // 레이블 텍스트 확인
        Field latencyLabelField = NetworkStatusDisplay.class.getDeclaredField("latencyLabel");
        latencyLabelField.setAccessible(true);
        JLabel latencyLabel = (JLabel) latencyLabelField.get(statusDisplay);
        assertEquals("Ping: 30ms", latencyLabel.getText());
        
        System.out.println("✓ 30ms 지연 시간 업데이트 성공 (EXCELLENT)");
    }

    @Test
    @DisplayName("3-2. 중간 지연 시간 업데이트 (120ms)")
    void testUpdateLatencyMedium() throws Exception {
        System.out.println("--- 중간 지연 시간 업데이트 테스트 ---");
        
        statusDisplay.updateLatency(120);
        
        assertEquals(120, statusDisplay.getCurrentLatency());
        assertEquals(NetworkStatusDisplay.PingLevel.FAIR, statusDisplay.getCurrentPingLevel());
        assertFalse(statusDisplay.isShowingWarning());
        
        Field latencyLabelField = NetworkStatusDisplay.class.getDeclaredField("latencyLabel");
        latencyLabelField.setAccessible(true);
        JLabel latencyLabel = (JLabel) latencyLabelField.get(statusDisplay);
        assertEquals("Ping: 120ms", latencyLabel.getText());
        
        System.out.println("✓ 120ms 지연 시간 업데이트 성공 (FAIR)");
    }

    @Test
    @DisplayName("3-3. 높은 지연 시간 업데이트 - 경고 표시 (250ms)")
    void testUpdateLatencyHighWithWarning() throws Exception {
        System.out.println("--- 높은 지연 시간 업데이트 테스트 (경고) ---");
        
        statusDisplay.updateLatency(250);
        
        assertEquals(250, statusDisplay.getCurrentLatency());
        assertEquals(NetworkStatusDisplay.PingLevel.VERY_POOR, statusDisplay.getCurrentPingLevel());
        assertTrue(statusDisplay.isShowingWarning(), "200ms 초과 시 경고가 표시되어야 함");
        
        Field statusLabelField = NetworkStatusDisplay.class.getDeclaredField("statusLabel");
        statusLabelField.setAccessible(true);
        JLabel statusLabel = (JLabel) statusLabelField.get(statusDisplay);
        assertEquals("⚠ 전송 지연 발생!", statusLabel.getText());
        
        System.out.println("✓ 250ms 지연 시간 업데이트 성공 (경고 표시)");
    }

    @Test
    @DisplayName("3-4. 경계값 테스트 (200ms) - 경고 없음")
    void testUpdateLatencyBoundary200() {
        System.out.println("--- 경계값 테스트 (200ms) ---");
        
        statusDisplay.updateLatency(200);
        
        assertEquals(200, statusDisplay.getCurrentLatency());
        assertEquals(NetworkStatusDisplay.PingLevel.POOR, statusDisplay.getCurrentPingLevel());
        assertFalse(statusDisplay.isShowingWarning(), "200ms는 경고를 표시하지 않아야 함");
        
        System.out.println("✓ 200ms는 경고 없음 (POOR 레벨)");
    }

    @Test
    @DisplayName("3-5. 경계값 테스트 (201ms) - 경고 표시")
    void testUpdateLatencyBoundary201() {
        System.out.println("--- 경계값 테스트 (201ms) ---");
        
        statusDisplay.updateLatency(201);
        
        assertEquals(201, statusDisplay.getCurrentLatency());
        assertEquals(NetworkStatusDisplay.PingLevel.VERY_POOR, statusDisplay.getCurrentPingLevel());
        assertTrue(statusDisplay.isShowingWarning(), "201ms는 경고를 표시해야 함");
        
        System.out.println("✓ 201ms는 경고 표시됨 (VERY_POOR 레벨)");
    }

    @Test
    @DisplayName("3-6. 지연 시간 연속 업데이트")
    void testUpdateLatencyContinuous() {
        System.out.println("--- 지연 시간 연속 업데이트 테스트 ---");
        
        // 좋음 → 나쁨 → 매우 나쁨 → 좋음
        statusDisplay.updateLatency(50);
        assertEquals(NetworkStatusDisplay.PingLevel.EXCELLENT, statusDisplay.getCurrentPingLevel());
        
        statusDisplay.updateLatency(180);
        assertEquals(NetworkStatusDisplay.PingLevel.POOR, statusDisplay.getCurrentPingLevel());
        
        statusDisplay.updateLatency(300);
        assertEquals(NetworkStatusDisplay.PingLevel.VERY_POOR, statusDisplay.getCurrentPingLevel());
        assertTrue(statusDisplay.isShowingWarning());
        
        statusDisplay.updateLatency(80);
        assertEquals(NetworkStatusDisplay.PingLevel.GOOD, statusDisplay.getCurrentPingLevel());
        assertFalse(statusDisplay.isShowingWarning());
        
        System.out.println("✓ 연속 업데이트가 올바르게 동작함");
    }

    // ========== 상태 메시지 테스트 ==========

    @Test
    @DisplayName("4-1. 커스텀 상태 메시지 설정")
    void testSetStatusMessage() throws Exception {
        System.out.println("--- 커스텀 상태 메시지 테스트 ---");
        
        Color customColor = new Color(100, 150, 200);
        statusDisplay.setStatusMessage("테스트 메시지", customColor);
        
        Field statusLabelField = NetworkStatusDisplay.class.getDeclaredField("statusLabel");
        statusLabelField.setAccessible(true);
        JLabel statusLabel = (JLabel) statusLabelField.get(statusDisplay);
        
        assertEquals("테스트 메시지", statusLabel.getText());
        assertEquals(customColor, statusLabel.getForeground());
        
        System.out.println("✓ 커스텀 메시지가 올바르게 설정됨");
    }

    @Test
    @DisplayName("4-2. 연결 중 상태 표시")
    void testShowConnecting() throws Exception {
        System.out.println("--- 연결 중 상태 표시 테스트 ---");
        
        statusDisplay.showConnecting();
        
        Field statusLabelField = NetworkStatusDisplay.class.getDeclaredField("statusLabel");
        statusLabelField.setAccessible(true);
        JLabel statusLabel = (JLabel) statusLabelField.get(statusDisplay);
        
        assertEquals("연결 중...", statusLabel.getText());
        assertNotNull(statusLabel.getForeground());
        
        System.out.println("✓ 연결 중 상태가 올바르게 표시됨");
    }

    @Test
    @DisplayName("4-3. 연결됨 상태 표시")
    void testShowConnected() throws Exception {
        System.out.println("--- 연결됨 상태 표시 테스트 ---");
        
        statusDisplay.showConnected();
        
        Field statusLabelField = NetworkStatusDisplay.class.getDeclaredField("statusLabel");
        statusLabelField.setAccessible(true);
        JLabel statusLabel = (JLabel) statusLabelField.get(statusDisplay);
        
        assertEquals("연결됨", statusLabel.getText());
        assertNotNull(statusLabel.getForeground());
        
        System.out.println("✓ 연결됨 상태가 올바르게 표시됨");
    }

    @Test
    @DisplayName("4-4. 연결 끊김 상태 표시")
    void testShowDisconnected() throws Exception {
        System.out.println("--- 연결 끊김 상태 표시 테스트 ---");
        
        statusDisplay.showDisconnected();
        
        Field statusLabelField = NetworkStatusDisplay.class.getDeclaredField("statusLabel");
        statusLabelField.setAccessible(true);
        JLabel statusLabel = (JLabel) statusLabelField.get(statusDisplay);
        
        assertEquals("⚠ 연결 끊김!", statusLabel.getText());
        assertEquals(NetworkStatusDisplay.PingLevel.VERY_POOR, statusDisplay.getCurrentPingLevel());
        
        System.out.println("✓ 연결 끊김 상태가 올바르게 표시됨");
    }

    @Test
    @DisplayName("4-5. 상태 전환 시나리오")
    void testStatusTransitions() throws Exception {
        System.out.println("--- 상태 전환 시나리오 테스트 ---");
        
        Field statusLabelField = NetworkStatusDisplay.class.getDeclaredField("statusLabel");
        statusLabelField.setAccessible(true);
        JLabel statusLabel = (JLabel) statusLabelField.get(statusDisplay);
        
        // 연결 중 → 연결됨
        statusDisplay.showConnecting();
        assertEquals("연결 중...", statusLabel.getText());
        
        statusDisplay.showConnected();
        assertEquals("연결됨", statusLabel.getText());
        
        // 정상 동작 중 지연 발생
        statusDisplay.updateLatency(50);
        assertFalse(statusDisplay.isShowingWarning());
        
        statusDisplay.updateLatency(250);
        assertTrue(statusDisplay.isShowingWarning());
        assertEquals("⚠ 전송 지연 발생!", statusLabel.getText());
        
        // 연결 끊김
        statusDisplay.showDisconnected();
        assertEquals("⚠ 연결 끊김!", statusLabel.getText());
        
        System.out.println("✓ 상태 전환이 올바르게 동작함");
    }

    // ========== Getter 메서드 테스트 ==========

    @Test
    @DisplayName("5-1. getCurrentLatency() 메서드")
    void testGetCurrentLatency() {
        System.out.println("--- getCurrentLatency() 테스트 ---");
        
        assertEquals(0, statusDisplay.getCurrentLatency());
        
        statusDisplay.updateLatency(123);
        assertEquals(123, statusDisplay.getCurrentLatency());
        
        statusDisplay.updateLatency(456);
        assertEquals(456, statusDisplay.getCurrentLatency());
        
        System.out.println("✓ getCurrentLatency()가 올바른 값을 반환함");
    }

    @Test
    @DisplayName("5-2. getCurrentPingLevel() 메서드")
    void testGetCurrentPingLevel() {
        System.out.println("--- getCurrentPingLevel() 테스트 ---");
        
        assertEquals(NetworkStatusDisplay.PingLevel.EXCELLENT, statusDisplay.getCurrentPingLevel());
        
        statusDisplay.updateLatency(75);
        assertEquals(NetworkStatusDisplay.PingLevel.GOOD, statusDisplay.getCurrentPingLevel());
        
        statusDisplay.updateLatency(300);
        assertEquals(NetworkStatusDisplay.PingLevel.VERY_POOR, statusDisplay.getCurrentPingLevel());
        
        System.out.println("✓ getCurrentPingLevel()이 올바른 레벨을 반환함");
    }

    @Test
    @DisplayName("5-3. isShowingWarning() 메서드")
    void testIsShowingWarning() {
        System.out.println("--- isShowingWarning() 테스트 ---");
        
        assertFalse(statusDisplay.isShowingWarning());
        
        statusDisplay.updateLatency(150);
        assertFalse(statusDisplay.isShowingWarning());
        
        statusDisplay.updateLatency(250);
        assertTrue(statusDisplay.isShowingWarning());
        
        statusDisplay.updateLatency(100);
        assertFalse(statusDisplay.isShowingWarning());
        
        System.out.println("✓ isShowingWarning()이 올바른 상태를 반환함");
    }

    // ========== 극단값 테스트 ==========

    @Test
    @DisplayName("6-1. 극단값 - 0ms 지연")
    void testExtremeValueZero() {
        System.out.println("--- 극단값 테스트 (0ms) ---");
        
        statusDisplay.updateLatency(0);
        
        assertEquals(0, statusDisplay.getCurrentLatency());
        assertEquals(NetworkStatusDisplay.PingLevel.EXCELLENT, statusDisplay.getCurrentPingLevel());
        assertFalse(statusDisplay.isShowingWarning());
        
        System.out.println("✓ 0ms 지연 시간 처리 성공");
    }

    @Test
    @DisplayName("6-2. 극단값 - 매우 높은 지연 (5000ms)")
    void testExtremeValueVeryHigh() {
        System.out.println("--- 극단값 테스트 (5000ms) ---");
        
        statusDisplay.updateLatency(5000);
        
        assertEquals(5000, statusDisplay.getCurrentLatency());
        assertEquals(NetworkStatusDisplay.PingLevel.VERY_POOR, statusDisplay.getCurrentPingLevel());
        assertTrue(statusDisplay.isShowingWarning());
        
        System.out.println("✓ 5000ms 지연 시간 처리 성공");
    }

    @Test
    @DisplayName("6-3. 모든 경계값 테스트")
    void testAllBoundaryValues() {
        System.out.println("--- 모든 경계값 테스트 ---");
        
        // EXCELLENT/GOOD 경계 (50/51ms)
        statusDisplay.updateLatency(50);
        assertEquals(NetworkStatusDisplay.PingLevel.EXCELLENT, statusDisplay.getCurrentPingLevel());
        
        statusDisplay.updateLatency(51);
        assertEquals(NetworkStatusDisplay.PingLevel.GOOD, statusDisplay.getCurrentPingLevel());
        
        // GOOD/FAIR 경계 (100/101ms)
        statusDisplay.updateLatency(100);
        assertEquals(NetworkStatusDisplay.PingLevel.GOOD, statusDisplay.getCurrentPingLevel());
        
        statusDisplay.updateLatency(101);
        assertEquals(NetworkStatusDisplay.PingLevel.FAIR, statusDisplay.getCurrentPingLevel());
        
        // FAIR/POOR 경계 (150/151ms)
        statusDisplay.updateLatency(150);
        assertEquals(NetworkStatusDisplay.PingLevel.FAIR, statusDisplay.getCurrentPingLevel());
        
        statusDisplay.updateLatency(151);
        assertEquals(NetworkStatusDisplay.PingLevel.POOR, statusDisplay.getCurrentPingLevel());
        
        // POOR/VERY_POOR 경계 (200/201ms)
        statusDisplay.updateLatency(200);
        assertEquals(NetworkStatusDisplay.PingLevel.POOR, statusDisplay.getCurrentPingLevel());
        
        statusDisplay.updateLatency(201);
        assertEquals(NetworkStatusDisplay.PingLevel.VERY_POOR, statusDisplay.getCurrentPingLevel());
        
        System.out.println("✓ 모든 경계값이 올바르게 처리됨");
    }

    // ========== 통합 시나리오 테스트 ==========

    @Test
    @DisplayName("7-1. 실제 게임 시나리오 - 정상 플레이")
    void testRealGameScenarioNormal() throws Exception {
        System.out.println("--- 실제 게임 시나리오 (정상) ---");
        
        Field statusLabelField = NetworkStatusDisplay.class.getDeclaredField("statusLabel");
        statusLabelField.setAccessible(true);
        JLabel statusLabel = (JLabel) statusLabelField.get(statusDisplay);
        
        // 게임 시작 - 연결 중
        statusDisplay.showConnecting();
        assertEquals("연결 중...", statusLabel.getText());
        
        // 연결 성공
        statusDisplay.showConnected();
        assertEquals("연결됨", statusLabel.getText());
        
        // 안정적인 플레이 (낮은 지연)
        statusDisplay.updateLatency(30);
        assertEquals(NetworkStatusDisplay.PingLevel.EXCELLENT, statusDisplay.getCurrentPingLevel());
        
        statusDisplay.updateLatency(45);
        assertEquals(NetworkStatusDisplay.PingLevel.EXCELLENT, statusDisplay.getCurrentPingLevel());
        
        statusDisplay.updateLatency(35);
        assertEquals(NetworkStatusDisplay.PingLevel.EXCELLENT, statusDisplay.getCurrentPingLevel());
        
        assertFalse(statusDisplay.isShowingWarning());
        
        System.out.println("✓ 정상 게임 시나리오 완료");
    }

    @Test
    @DisplayName("7-2. 실제 게임 시나리오 - 네트워크 불안정")
    void testRealGameScenarioUnstable() throws Exception {
        System.out.println("--- 실제 게임 시나리오 (불안정) ---");
        
        Field statusLabelField = NetworkStatusDisplay.class.getDeclaredField("statusLabel");
        statusLabelField.setAccessible(true);
        JLabel statusLabel = (JLabel) statusLabelField.get(statusDisplay);
        
        // 게임 중 네트워크 품질 변화
        statusDisplay.updateLatency(40);
        assertFalse(statusDisplay.isShowingWarning());
        
        statusDisplay.updateLatency(120);
        assertFalse(statusDisplay.isShowingWarning());
        
        statusDisplay.updateLatency(250);
        assertTrue(statusDisplay.isShowingWarning());
        assertEquals("⚠ 전송 지연 발생!", statusLabel.getText());
        
        statusDisplay.updateLatency(180);
        assertFalse(statusDisplay.isShowingWarning());
        
        statusDisplay.updateLatency(60);
        assertFalse(statusDisplay.isShowingWarning());
        
        System.out.println("✓ 불안정 네트워크 시나리오 완료");
    }

    @Test
    @DisplayName("7-3. 실제 게임 시나리오 - 연결 끊김")
    void testRealGameScenarioDisconnect() throws Exception {
        System.out.println("--- 실제 게임 시나리오 (연결 끊김) ---");
        
        Field statusLabelField = NetworkStatusDisplay.class.getDeclaredField("statusLabel");
        statusLabelField.setAccessible(true);
        JLabel statusLabel = (JLabel) statusLabelField.get(statusDisplay);
        
        // 정상 플레이 중
        statusDisplay.showConnected();
        statusDisplay.updateLatency(50);
        
        // 갑자기 지연 증가
        statusDisplay.updateLatency(500);
        assertTrue(statusDisplay.isShowingWarning());
        
        // 연결 끊김
        statusDisplay.showDisconnected();
        assertEquals("⚠ 연결 끊김!", statusLabel.getText());
        assertEquals(NetworkStatusDisplay.PingLevel.VERY_POOR, statusDisplay.getCurrentPingLevel());
        
        System.out.println("✓ 연결 끊김 시나리오 완료");
    }
}
