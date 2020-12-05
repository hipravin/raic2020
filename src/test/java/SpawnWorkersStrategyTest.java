import hipravin.model.Position2d;
import hipravin.strategy.SpawnWorkersStrategy;
import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static hipravin.model.Position2d.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpawnWorkersStrategyTest {

    @Test
    void testSpawn() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 2, 52);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        SpawnWorkersStrategy spawnStrategy = new SpawnWorkersStrategy();

        Optional<Position2d> spawn = spawnStrategy.bestSpawnPos(
                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(), rstrategy.getStrategyParams());

        assertTrue(spawn.isPresent());

        assertEquals(of(10,5), spawn.get());
    }
}
