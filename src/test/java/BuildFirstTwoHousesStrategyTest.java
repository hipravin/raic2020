import hipravin.strategy.BeforeFirstHouseBuildOrder;
import hipravin.strategy.BuildFirstTwoHousesStrategy;
import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static hipravin.model.Position2d.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BuildFirstTwoHousesStrategyTest {
    RootStrategy strategy;

    @BeforeEach
    void setUp() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 3, 0);
        strategy = new RootStrategy();
        Action action = strategy.getAction(get0.getPlayerView(), null);

    }

    @Test
    void testBestBuildMines() {
        BuildFirstTwoHousesStrategy bft = (BuildFirstTwoHousesStrategy) strategy.getSubStrategies().get(0);

        List<BeforeFirstHouseBuildOrder.BuildMine> buildMines =
                bft.bestBuildMines(strategy.getGameHistoryState(), strategy.getCurrentParsedGameState(),
                strategy.getStrategyParams(), new HashMap<>());

        assertEquals(10, buildMines.size());




    }

    @Test
    void testMiningAndMiner1() {
        BuildFirstTwoHousesStrategy bft = (BuildFirstTwoHousesStrategy) strategy.getSubStrategies().get(0);

        BuildFirstTwoHousesStrategy.MineralAndMinerPosition mmp =
                bft.minerAndMineralClosest(of(4,4), strategy.getGameHistoryState(), strategy.getCurrentParsedGameState(),
                strategy.getStrategyParams(), new HashMap<>());


        assertNotNull(mmp);
        assertEquals(of(4,2), mmp.minerPosition);
        assertEquals(of(4,1), mmp.mineralPosition);
    }

    @Test
    void testMiningAndMiner2() {
        BuildFirstTwoHousesStrategy bft = (BuildFirstTwoHousesStrategy) strategy.getSubStrategies().get(0);

        BuildFirstTwoHousesStrategy.MineralAndMinerPosition mmp =
                bft.minerAndMineralClosest(of(10,7), strategy.getGameHistoryState(), strategy.getCurrentParsedGameState(),
                strategy.getStrategyParams(), new HashMap<>());


        assertNotNull(mmp);
        assertEquals(of(12,7), mmp.minerPosition);
        assertEquals(of(13,7), mmp.mineralPosition);
    }

}