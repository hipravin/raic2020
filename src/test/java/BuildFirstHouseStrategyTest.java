import hipravin.model.Position2d;
import hipravin.strategy.BeforeFirstHouseBuildOrder;
import hipravin.strategy.BuildFirstHouseFinalStrategy;
import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static hipravin.model.Position2d.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildFirstHouseStrategyTest {
    RootStrategy strategy;

    @BeforeEach
    void setUp() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 3, 0);
        strategy = new RootStrategy();
        Action action = strategy.getAction(get0.getPlayerView(), null);

    }

    @Test
    void testFind2b1r() {

        BuildFirstHouseFinalStrategy bfs = new BuildFirstHouseFinalStrategy();

        System.out.println(LocalDateTime.now());
        Optional<BeforeFirstHouseBuildOrder> bo = bfs.tryToFind2Build1DiagRepair(
                strategy.getGameHistoryState(), strategy.getCurrentParsedGameState(),  strategy.getStrategyParams(), Collections.emptyMap());

        System.out.println(LocalDateTime.now());

        assertTrue(bo.isPresent());

        System.out.println(bo);

    }

    @Test
    void testFind2b1rProf() {

        BuildFirstHouseFinalStrategy bfs = new BuildFirstHouseFinalStrategy();

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
        BuildFirstHouseFinalStrategy bfs = new BuildFirstHouseFinalStrategy();

        Set<Position2d> len0Build = BuildFirstHouseFinalStrategy.buildHousePositionsLen0(
                of(3, 12), strategy.getCurrentParsedGameState(), strategy.getStrategyParams(), of(6, 13));

        assertEquals(8, len0Build.size());

        len0Build = BuildFirstHouseFinalStrategy.buildHousePositionsLen0(
                of(3, 12), strategy.getCurrentParsedGameState(), strategy.getStrategyParams(), of(7, 13));

        assertEquals(7, len0Build.size());


    }

    @Test
    void testFirstMapLen2() {
        BuildFirstHouseFinalStrategy bfs = new BuildFirstHouseFinalStrategy();

        Set<Position2d> len2Build = BuildFirstHouseFinalStrategy.buildHousePositionsLen2(
                of(2, 11), strategy.getCurrentParsedGameState(), strategy.getStrategyParams(), of(6, 13));

        System.out.println(len2Build);

        assertEquals(19, len2Build.size());

        len2Build = BuildFirstHouseFinalStrategy.buildHousePositionsLen2(
                of(2, 11), strategy.getCurrentParsedGameState(), strategy.getStrategyParams(), of(13, 13));

        assertEquals(18, len2Build.size());


    }


}