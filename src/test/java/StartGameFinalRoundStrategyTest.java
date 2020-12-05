import hipravin.model.Position2d;
import hipravin.strategy.BeforeFirstHouseBuildOrder;
import hipravin.strategy.FinalGameStartStrategy;
import hipravin.strategy.TestServerUtil;
import hipravin.strategy.command.BuildWorkerCommand;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static hipravin.model.Position2d.of;
import static org.junit.jupiter.api.Assertions.*;

class StartGameFinalRoundStrategyTest {
    RootStrategy strategy;

    @BeforeEach
    void setUp() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 3, 0);
        strategy = new RootStrategy();
        Action action = strategy.getAction(get0.getPlayerView(), null);

    }

    @Test
    void testFindBoSeparateMineral() {

        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 4, 0);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);


        FinalGameStartStrategy bfs = new FinalGameStartStrategy();

        Optional<BeforeFirstHouseBuildOrder> bo = bfs.tryToFind2Build1DiagRepair(
                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(),  rstrategy.getStrategyParams(), Collections.emptyMap());

        assertTrue(bo.isPresent());

        System.out.println(bo);

    }

    @Test
    void testFindBoSeparateMineral2() {

        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 6, 0);
        RootStrategy rstrategy = new RootStrategy();
        Action action = rstrategy.getAction(get0.getPlayerView(), null);


        FinalGameStartStrategy bfs = new FinalGameStartStrategy();

        Optional<BeforeFirstHouseBuildOrder> bo = bfs.tryToFind2Build1DiagRepair(
                rstrategy.getGameHistoryState(), rstrategy.getCurrentParsedGameState(),  rstrategy.getStrategyParams(), Collections.emptyMap());

        assertTrue(bo.isPresent());

        System.out.println(bo);

    }

    @Test
    void testBuilOrderToCommands() {

        FinalGameStartStrategy bfs = new FinalGameStartStrategy();

//        bfs.decide(strategy.gameHistoryState, strategy.currentParsedGameState, strategy.strategyParams, new HashMap<>());

        assertEquals(2, strategy.gameHistoryState.getOngoingCommands().size());
        BuildWorkerCommand buildChain = (BuildWorkerCommand)strategy.gameHistoryState.getOngoingCommands().get(1);

        System.out.println(buildChain);




    }


    @Test
    void testFind2b1r() {

        FinalGameStartStrategy bfs = new FinalGameStartStrategy();

        Optional<BeforeFirstHouseBuildOrder> bo = bfs.tryToFind2Build1DiagRepair(
                strategy.getGameHistoryState(), strategy.getCurrentParsedGameState(),  strategy.getStrategyParams(), Collections.emptyMap());

        assertTrue(bo.isPresent());

        System.out.println(bo);

    }

    @Test
    void testFind2b1rProf() {

        FinalGameStartStrategy bfs = new FinalGameStartStrategy();

        System.out.println(LocalDateTime.now());
        long iterations = 50;
        Instant start = Instant.now();

        for (int i = 0; i < iterations; i++) {

            Optional<BeforeFirstHouseBuildOrder> bo = bfs.tryToFind2Build1DiagRepair(
                    strategy.getGameHistoryState(), strategy.getCurrentParsedGameState(),  strategy.getStrategyParams(), Collections.emptyMap());

        }

        System.out.println(Duration.between(start, Instant.now()).dividedBy(iterations));


    }

    @Test
    void testFirstMapLen0() {
        FinalGameStartStrategy bfs = new FinalGameStartStrategy();

        Set<Position2d> len0Build = FinalGameStartStrategy.buildHousePositionsLen0(
                of(3, 12), strategy.getCurrentParsedGameState(), strategy.getStrategyParams(), of(6, 13));

        assertEquals(8, len0Build.size());

        len0Build = FinalGameStartStrategy.buildHousePositionsLen0(
                of(3, 12), strategy.getCurrentParsedGameState(), strategy.getStrategyParams(), of(7, 13));

        assertEquals(7, len0Build.size());


    }

    @Test
    void testFirstMapLen2() {
        FinalGameStartStrategy bfs = new FinalGameStartStrategy();

        Set<Position2d> len2Build = FinalGameStartStrategy.buildHousePositionsLen2(
                of(2, 11), strategy.getCurrentParsedGameState(), strategy.getStrategyParams(), of(6, 13));

        System.out.println(len2Build);

        assertEquals(19, len2Build.size());

        len2Build = FinalGameStartStrategy.buildHousePositionsLen2(
                of(2, 11), strategy.getCurrentParsedGameState(), strategy.getStrategyParams(), of(13, 13));

        assertEquals(18, len2Build.size());


    }


}