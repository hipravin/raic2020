import hipravin.strategy.PushMinersStrategy;
import hipravin.strategy.TestServerUtil;
import hipravin.strategy.WayOutWorkersBlockingDetectingStrategy;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static hipravin.model.Position2d.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WayOUtBlockersDetectorStrategyTest {
//    @Test
//    void testFalseBlock() {
//        RootStrategy strategy = new RootStrategy();
//        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 17, 200);
//
//        Action action = strategy.getAction(get0.getPlayerView(), null);
//        WayOutWorkersBlockingDetectingStrategy wstrategy = new WayOutWorkersBlockingDetectingStrategy();
//
//        assertTrue(wstrategy.findWayBlockers(strategy.gameHistoryState, strategy.currentParsedGameState, strategy.strategyParams, new HashMap<>()));
//    }

    @Test
    void testDetect3() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 17, 97);

        Action action = strategy.getAction(get0.getPlayerView(), null);
        WayOutWorkersBlockingDetectingStrategy wstrategy = new WayOutWorkersBlockingDetectingStrategy();

        assertTrue(wstrategy.findWayBlockers(strategy.gameHistoryState, strategy.currentParsedGameState, strategy.strategyParams, new HashMap<>()));
    }

    @Test
    void testDetect2() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 17, 113);

        Action action = strategy.getAction(get0.getPlayerView(), null);
        WayOutWorkersBlockingDetectingStrategy wstrategy = new WayOutWorkersBlockingDetectingStrategy();

        assertTrue(wstrategy.findWayBlockers(strategy.gameHistoryState, strategy.currentParsedGameState, strategy.strategyParams, new HashMap<>()));
    }

    @Test
    void testDetect() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 15, 297);

        Action action = strategy.getAction(get0.getPlayerView(), null);
        WayOutWorkersBlockingDetectingStrategy wstrategy = new WayOutWorkersBlockingDetectingStrategy();

        assertTrue(wstrategy.findWayBlockers(strategy.gameHistoryState, strategy.currentParsedGameState, strategy.strategyParams, new HashMap<>()));
    }
}
