import hipravin.strategy.SpawnWorkersStrategy;
import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpawnWorkersStrategyTest {

    @Test
    void testSpawn1() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 5, 71);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }

    @Test
    void testSpawn() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 2, 52);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        SpawnWorkersStrategy spawnStrategy = new SpawnWorkersStrategy();

        spawnStrategy.findBestSpawnPos(
                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(), rstrategy.getStrategyParams());
        spawnStrategy.findBestSpawnPos(
                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(), rstrategy.getStrategyParams());
        spawnStrategy.findBestSpawnPos(
                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(), rstrategy.getStrategyParams());


        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }
}
