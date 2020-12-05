import hipravin.strategy.BeforeFirstHouseBuildOrder;
import hipravin.strategy.deprecated.BuildFirstTwoHousesStrategy;
import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static hipravin.model.Position2d.of;
import static org.junit.jupiter.api.Assertions.*;

@Deprecated
class BuildFirstTwoHousesStrategyTest {
    RootStrategy strategy;

    @BeforeEach
    void setUp() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 3, 0);
        strategy = new RootStrategy();
        Action action = strategy.getAction(get0.getPlayerView(), null);

    }

//    @Test
//    void testFirstHouseBo() {
//        System.out.println(LocalDateTime.now());
//        BuildFirstTwoHousesStrategy bft = new BuildFirstTwoHousesStrategy();
//
//        BeforeFirstHouseBuildOrder beg = bft.findOptimalBeginning(strategy.getGameHistoryState(), strategy.getCurrentParsedGameState(),
//                strategy.getStrategyParams(), new HashMap<>());
//
//
//        System.out.println(LocalDateTime.now());
//        assertNotNull(beg);
//        System.out.println(beg);
//
//
//    }

    @Test
    void testIsEmptyForHouse() {
        BuildFirstTwoHousesStrategy bft = new BuildFirstTwoHousesStrategy();

        assertTrue(
                bft.isEmptyForHouseIgnoringUnitsAnd1Mineral(
                        of(2, 2), of(6, 13), strategy.getCurrentParsedGameState()));
        assertTrue(
                bft.isEmptyForHouseIgnoringUnitsAnd1Mineral(
                        of(4, 11), of(6, 13), strategy.getCurrentParsedGameState()));
        assertFalse(
                bft.isEmptyForHouseIgnoringUnitsAnd1Mineral(
                        of(3, 3), of(6, 13), strategy.getCurrentParsedGameState()));
        assertFalse(
                bft.isEmptyForHouseIgnoringUnitsAnd1Mineral(
                        of(2, 1), of(6, 13), strategy.getCurrentParsedGameState()));

    }

    @Test
    void testBestBuildMines() {
        BuildFirstTwoHousesStrategy bft = new BuildFirstTwoHousesStrategy();

        List<BeforeFirstHouseBuildOrder.BuildMine> buildMines =
                bft.bestBuildMines(strategy.getGameHistoryState(), strategy.getCurrentParsedGameState(),
                        strategy.getStrategyParams(), new HashMap<>());

        assertEquals(19, buildMines.size());
    }

    @Test
    void testMiningAndMiner1() {
        BuildFirstTwoHousesStrategy bft = new BuildFirstTwoHousesStrategy();

        BuildFirstTwoHousesStrategy.MineralAndMinerPosition mmp =
                bft.minerAndMineralClosest(of(4, 4), strategy.getGameHistoryState(), strategy.getCurrentParsedGameState(),
                        strategy.getStrategyParams(), new HashMap<>());


        assertNotNull(mmp);
        assertEquals(of(4, 2), mmp.minerPosition);
        assertEquals(of(4, 1), mmp.mineralPosition);
    }

    @Test
    void testMiningAndMiner2() {
        BuildFirstTwoHousesStrategy bft = new BuildFirstTwoHousesStrategy();

        BuildFirstTwoHousesStrategy.MineralAndMinerPosition mmp =
                bft.minerAndMineralClosest(of(10, 7), strategy.getGameHistoryState(), strategy.getCurrentParsedGameState(),
                        strategy.getStrategyParams(), new HashMap<>());


        assertNotNull(mmp);
        assertEquals(of(12, 7), mmp.minerPosition);
        assertEquals(of(13, 7), mmp.mineralPosition);
    }

}