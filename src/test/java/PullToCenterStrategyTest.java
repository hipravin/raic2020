import hipravin.strategy.PushMinersStrategy;
import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static hipravin.model.Position2d.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PullToCenterStrategyTest {
    @Test
    void testUnblockHouseAngle() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 15, 174);

        Action action = strategy.getAction(get0.getPlayerView(), null);

        PushMinersStrategy.UnblockAngle ua = new PushMinersStrategy.UnblockAngle();

        assertTrue(ua.tryApply(of(14, 15), strategy.gameHistoryState, strategy.currentParsedGameState, strategy.strategyParams, new HashMap<>()));

    }



}
