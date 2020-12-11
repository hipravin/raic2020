import hipravin.strategy.PushMinersStrategy;
import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static hipravin.model.Position2d.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PushWorkersStrategyTest {
    @Test
    void testUnblockAngle() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 13, 150);

        Action action = strategy.getAction(get0.getPlayerView(), null);

        PushMinersStrategy.StepTurnHodorScript turnDiag = new PushMinersStrategy.StepTurnHodorScript();

        assertTrue(turnDiag.tryApply(of(15, 4), strategy.gameHistoryState, strategy.currentParsedGameState, strategy.strategyParams, new HashMap<>()));

        assertEquals(20, action.getEntityActions().size());
    }

}
