import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RootStrategyReplaySampleTickTest {
    @Test
    void testRound1Sample1Tick0() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(1,1,0);

        Action action = strategy.getAction(get0.getPlayerView(), null);

        assertEquals(0, action.getEntityActions().size());
    }
}
