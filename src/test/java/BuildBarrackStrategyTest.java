import hipravin.model.GameStateParser;
import hipravin.strategy.BuildHousesStrategy;
import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuildBarrackStrategyTest {

    @Test
    void testNpe() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 7, 219);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        rstrategy.currentParsedGameState.allCellsAsStream().filter(c -> !c.isEmpty() && c.getEntity() == null)
                .forEach(c -> {
                    System.out.println(c.getPosition());
                });



//        assertEquals(1, rstrategy.getGameHistoryState().ongoingBarrackBuildCommandCount());
//        assertEquals(1, rstrategy.getGameHistoryState().getOngoingCommands().size());
    }

    @Test
    void testBuildCenter() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3, 7, 211);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);

        BuildHousesStrategy bhs = new BuildHousesStrategy();

//        assertEquals(1, rstrategy.getGameHistoryState().ongoingBarrackBuildCommandCount());
//        assertEquals(1, rstrategy.getGameHistoryState().getOngoingCommands().size());
    }

}
