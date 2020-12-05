import hipravin.strategy.BeforeFirstHouseBuildOrder;
import hipravin.strategy.FinalGameStartStrategy;
import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmptyMineralsTest {

    @Test
    void testNpeBug() {

        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 7, 0);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);


        FinalGameStartStrategy bfs = new FinalGameStartStrategy();

        Optional<BeforeFirstHouseBuildOrder> bo = bfs.tryToFind2Build1DiagRepair(
                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(),  rstrategy.getStrategyParams(), Collections.emptyMap());

        assertFalse(bo.isPresent());

        System.out.println(bo);

    }}
