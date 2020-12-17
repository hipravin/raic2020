import hipravin.strategy.SpawnWorkersStrategy;
import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MagnetRepairStrategyTest {

    @Test
    void testMagnetComplications() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 18, 195);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        System.out.println();

//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }

    @Test
    void testMagnetTudaSuda4NoRepairNoFogSend() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 11, 135);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        System.out.println();

//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }
    @Test
    void testMagnetTudaSuda3() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 11, 84);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        System.out.println();

//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }

    @Test
    void testMagnetTudaSuda2() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 8, 238);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        System.out.println();

//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }

    @Test
    void testMagnetTudaSuda() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 8, 225);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        System.out.println();

//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }

    @Test
    void testMagnet0() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 5, 88);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        System.out.println();

//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }

    @Test
    void testMagnet() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 4, 217);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        System.out.println();

//        assertEquals(3, spawnStrategy.getLastMineralPositions().size());
    }
}
