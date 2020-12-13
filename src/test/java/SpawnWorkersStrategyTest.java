import hipravin.strategy.SpawnWorkersStrategy;
import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpawnWorkersStrategyTest {

    @Test
    void testFalseSurrounded() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 16, 70);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        SpawnWorkersStrategy spawnWorkersStrategy = new SpawnWorkersStrategy();

        assertFalse(spawnWorkersStrategy.detectRespIsSurroundedByMinerals(rstrategy.gameHistoryState, rstrategy.currentParsedGameState, rstrategy.strategyParams));
        assertFalse(spawnWorkersStrategy.detectRespIsDoubleSurroundedByMinerals(rstrategy.gameHistoryState, rstrategy.currentParsedGameState, rstrategy.strategyParams));


        System.out.println();
//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }

    @Test
    void testDoubleSurroundedFalse() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 16, 94);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        SpawnWorkersStrategy spawnWorkersStrategy = new SpawnWorkersStrategy();

        assertFalse(spawnWorkersStrategy.detectRespIsSurroundedByMinerals(rstrategy.gameHistoryState, rstrategy.currentParsedGameState, rstrategy.strategyParams));
        assertFalse(spawnWorkersStrategy.detectRespIsDoubleSurroundedByMinerals(rstrategy.gameHistoryState, rstrategy.currentParsedGameState, rstrategy.strategyParams));


        System.out.println();
//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }

    @Test
    void testDoubleSurrounded() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 16, 116);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        SpawnWorkersStrategy spawnWorkersStrategy = new SpawnWorkersStrategy();

        assertTrue(spawnWorkersStrategy.detectRespIsSurroundedByMinerals(rstrategy.gameHistoryState, rstrategy.currentParsedGameState, rstrategy.strategyParams));
        assertTrue(spawnWorkersStrategy.detectRespIsDoubleSurroundedByMinerals(rstrategy.gameHistoryState, rstrategy.currentParsedGameState, rstrategy.strategyParams));



        System.out.println();
//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }

    @Test
    void testCorrectSpawnNearestToCenter() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 15, 135);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }

   @Test
    void test() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 15, 70);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }



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


//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());//were sent to center
    }
}
