import hipravin.strategy.SpawnWorkersStrategy;
import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MagnetRepairStrategyTest {

    @Test
    void testMagnet() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 4, 217);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        System.out.println();

//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }
}
