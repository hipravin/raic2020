import hipravin.model.GameStateParser;
import hipravin.strategy.*;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import static hipravin.model.Position2d.of;
import static org.junit.jupiter.api.Assertions.*;

public class BuildHousesStrategyTest {

    @Test
    void testBlockWithUnitsLen() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 11, 224);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        assertTrue(BlockDetector.checkIfWorkerBlocksWayOut(of(19, 3),
                rstrategy.gameHistoryState, rstrategy.currentParsedGameState, rstrategy.strategyParams));

        System.out.println();


    }
    @Test
    void testBlockWithUnits() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 10, 107);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        assertTrue(BlockDetector.checkIfWorkerBlocksWayOut(of(5, 12),
                rstrategy.gameHistoryState, rstrategy.currentParsedGameState, rstrategy.strategyParams));

        assertEquals(of(5,14), AutomineFallbackStrategy.tryToUnblockTheWay(of(5,12), rstrategy.currentParsedGameState));

        System.out.println();


    }

    @Test
    void testBlockPartially2() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 10, 76);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        assertTrue(BlockDetector.checkIfHouseBlocksWayOut(of(5, 10),
                rstrategy.gameHistoryState, rstrategy.currentParsedGameState, rstrategy.strategyParams));


        System.out.println();


    }

    @Test
    void testBlockPartially() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 10, 74);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        assertTrue(BlockDetector.checkIfHouseBlocksWayOut(of(14, 5),
                rstrategy.gameHistoryState, rstrategy.currentParsedGameState, rstrategy.strategyParams));


        System.out.println();


    }

    @Test
    void testBlockDetector() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 9, 133);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        assertTrue(BlockDetector.checkIfHouseBlocksWayOut(of(12, 11),
                rstrategy.gameHistoryState, rstrategy.currentParsedGameState, rstrategy.strategyParams));

        assertFalse(BlockDetector.checkIfHouseBlocksWayOut(of(15, 15),
                rstrategy.gameHistoryState, rstrategy.currentParsedGameState, rstrategy.strategyParams));

        System.out.println();


    }
    @Test
    void testWorkerBlockDetector() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 9, 208);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        assertTrue(BlockDetector.checkIfWorkerBlocksWayOut(of(3, 18),
                rstrategy.gameHistoryState, rstrategy.currentParsedGameState, rstrategy.strategyParams));

        System.out.println();
    }


    @Test
    void testGood3w() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 9, 44);
        RootStrategy rstrategy = new RootStrategy();
        FinalGameStartStrategy.gameStartStrategyDone = true;
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        BuildHousesStrategy bhs = new BuildHousesStrategy();

        assertEquals(4, rstrategy.getGameHistoryState().getOngoingCommands().size());

    }

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
                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(), rstrategy.getStrategyParams(), false, false);
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
                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(), rstrategy.getStrategyParams(), false, false);
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
                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(), rstrategy.getStrategyParams(), true, false);
        assertTrue(built);
    }
}
