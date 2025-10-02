package tetris;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameTest {
    @Test
    void run_doesNotThrow() {
        assertDoesNotThrow(() -> Game.run());
    }
}