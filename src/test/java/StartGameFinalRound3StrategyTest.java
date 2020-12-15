import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StartGameFinalRound3StrategyTest {
    RootStrategy strategy;

    @Test
    void testNpe() {


        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 17, 0);
        strategy = new RootStrategy();
        Action action = strategy.getAction(get0.getPlayerView(), null);

        System.out.println();
    }

    @Test
    void testFind2b1rProf() {
        long iterations = 5;
        Instant start = Instant.now();

        for (int i = 0; i < iterations; i++) {

            ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 3, 0);
            strategy = new RootStrategy();
            Action action = strategy.getAction(get0.getPlayerView(), null);
        }

        System.out.println(Duration.between(start, Instant.now()).dividedBy(iterations));
        assertTrue(Duration.between(start, Instant.now()).dividedBy(iterations).toMillis() < 500);
    }
}