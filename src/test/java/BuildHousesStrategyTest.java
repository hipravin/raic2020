import hipravin.model.GameStateParser;
import hipravin.strategy.BuildHousesStrategy;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuildHousesStrategyTest {

    @Test
    void testBuildEdgeConflct() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 2, 68);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        BuildHousesStrategy bhs = new BuildHousesStrategy();

        assertEquals(4, rstrategy.getGameHistoryState().getOngoingCommands().size());
//        assertEquals(1, rstrategy.getGameHistoryState().getOngoingCommands().size());
    }

    @Test
    void testBuild0() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 1, 68);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        BuildHousesStrategy bhs = new BuildHousesStrategy();

        GameStateParser.computeUniqueWorkersNearby(rstrategy.currentParsedGameState, StrategyParams.HOUSE_WORKERS_NEARBY_MAX_PATH);

//        boolean built = bhs.tryToBuildHouseShortDistance(3, 3,
//                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(), rstrategy.getStrategyParams(), false);
//        assertTrue(built);

        boolean built = bhs.tryToBuildHouseShortDistance(1, 3,
                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(), rstrategy.getStrategyParams(), false);
        assertTrue(built);

    }

    @Test
    void testBuild1() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 1, 68);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        BuildHousesStrategy bhs = new BuildHousesStrategy();
        GameStateParser.computeUniqueWorkersNearby(rstrategy.currentParsedGameState,  StrategyParams.HOUSE_WORKERS_NEARBY_MAX_PATH);
        boolean built = bhs.tryToBuildHouseShortDistance(2, 3,
                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(), rstrategy.getStrategyParams(), false);
        assertTrue(built);
    }

    @Test
    void testBuild2() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 1, 68);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        BuildHousesStrategy bhs = new BuildHousesStrategy();
        GameStateParser.computeUniqueWorkersNearby(rstrategy.currentParsedGameState,  StrategyParams.HOUSE_WORKERS_NEARBY_MAX_PATH);
        boolean built = bhs.tryToBuildHouseShortDistance(1, 3,
                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(), rstrategy.getStrategyParams(), true);
        assertTrue(built);
    }
}
