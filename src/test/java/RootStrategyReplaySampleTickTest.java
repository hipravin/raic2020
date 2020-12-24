import hipravin.strategy.TestServerUtil;
import model.Action;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RootStrategyReplaySampleTickTest {
    @Test
    void test62moveWhat() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3,19,297);

        Action action = strategy.getAction(get0.getPlayerView(), null);


        System.out.println(action.getEntityActions().size());

        //standing?
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4565, attackPosition=(7,15), retreatPosition=(17,12)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4556, attackPosition=(8,21), retreatPosition=(18,15)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4580, attackPosition=(9,20), retreatPosition=(19,13)}

    }

    @Test
    void test72hold() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3,19,259);

        Action action = strategy.getAction(get0.getPlayerView(), null);


        System.out.println(action.getEntityActions().size());

        //standing?
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4565, attackPosition=(7,15), retreatPosition=(17,12)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4556, attackPosition=(8,21), retreatPosition=(18,15)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4580, attackPosition=(9,20), retreatPosition=(19,13)}

    }


    @Test
    void test61hold() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3,19,238);

        Action action = strategy.getAction(get0.getPlayerView(), null);

        assertEquals(0, strategy.currentParsedGameState.at(37,38).getAttackerCount(5));
        assertEquals(1, strategy.currentParsedGameState.at(37,38).getAttackerCount(6));
        assertEquals(1, strategy.currentParsedGameState.at(37,38).getAttackerCount(7));

        System.out.println(action.getEntityActions().size());

        //standing?
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4565, attackPosition=(7,15), retreatPosition=(17,12)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4556, attackPosition=(8,21), retreatPosition=(18,15)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4580, attackPosition=(9,20), retreatPosition=(19,13)}

    }


    @Test
    void tesheal() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3,19,371);

        Action action = strategy.getAction(get0.getPlayerView(), null);

        System.out.println(action.getEntityActions().size());

        //standing?
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4565, attackPosition=(7,15), retreatPosition=(17,12)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4556, attackPosition=(8,21), retreatPosition=(18,15)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4580, attackPosition=(9,20), retreatPosition=(19,13)}

    }

    @Test
    void tesCountTurret6() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3,19,890);

        Action action = strategy.getAction(get0.getPlayerView(), null);

        System.out.println(action.getEntityActions().size());

        //standing?
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4565, attackPosition=(7,15), retreatPosition=(17,12)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4556, attackPosition=(8,21), retreatPosition=(18,15)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4580, attackPosition=(9,20), retreatPosition=(19,13)}

    }

    @Test
    void tesCountWnemyWorkres() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3,19,596);

        Action action = strategy.getAction(get0.getPlayerView(), null);

        System.out.println(action.getEntityActions().size());

        //standing?
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4565, attackPosition=(7,15), retreatPosition=(17,12)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4556, attackPosition=(8,21), retreatPosition=(18,15)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4580, attackPosition=(9,20), retreatPosition=(19,13)}

    }

    @Test
    void testStuckRang() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3,19,905);

        Action action = strategy.getAction(get0.getPlayerView(), null);

        System.out.println(action.getEntityActions().size());

        //standing?
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4565, attackPosition=(7,15), retreatPosition=(17,12)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4556, attackPosition=(8,21), retreatPosition=(18,15)}
//        Ongonig:RangerAttackHoldRetreatCommand{rangerEntityId=4580, attackPosition=(9,20), retreatPosition=(19,13)}

    }

    @Test
    void testNoMineralsVisible() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3,12,0);

        Action action = strategy.getAction(get0.getPlayerView(), null);

        assertEquals(2 , action.getEntityActions().size());
    }

    @Test
    void testUnableToFingStuck() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3,7,0);

        Action action = strategy.getAction(get0.getPlayerView(), null);

        assertEquals(2 , action.getEntityActions().size());
    }

    @Test
    void testRound1Sample1Tick0() {
        RootStrategy strategy = new RootStrategy();
        ServerMessage.GetAction get0 = TestServerUtil.readGet(3,6,0);

        Action action = strategy.getAction(get0.getPlayerView(), null);

        assertEquals(2, action.getEntityActions().size());
    }
//
//    @Test
//    void testRound1Sample2Tick52() {
//        RootStrategy strategy = new RootStrategy();
//        ServerMessage.GetAction get0 = TestServerUtil.readGet(1,2,52);
//
//        Action action = strategy.getAction(get0.getPlayerView(), null);
//
//        assertEquals(1, strategy.gameHistoryState.getOngoingBuildCommands().size());
//    }
}
