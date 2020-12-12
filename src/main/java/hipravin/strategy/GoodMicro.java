package hipravin.strategy;

import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;

import java.util.*;

public class GoodMicro {

    List<MicroScript> microScripts = new ArrayList<>();

    public GoodMicro() {
//        microScripts.add();
    }

    /**
     * returns move 1 tick position for specific ranger, if empty then follow original strategy
     * Can return same position if hold/ hold = no action
     */
    public Optional<Position2d> tryPerformRangerBetterMicro(Position2d rangerPosition, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                            StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions,
                                                            Set<Position2d> reservedMoveToPositions) {

        List<Position2d> moveOptions = Position2dUtil.upRightLeftDownFiltered(rangerPosition,
                Arrays.asList(
                        p -> pgs.at(p).isEmpty())
        );

        for (MicroScript microScript : microScripts) {
            for (Position2d moveOption : moveOptions) {
                if(microScript.matches(rangerPosition, moveOption, pgs)) {
                    return Optional.of(moveOption);
                }
            }
        }

        return Optional.empty();
    }

    interface MicroScript {
        boolean matches(Position2d ranger, Position2d moveTo, ParsedGameState pgs);
    }

    static class SingleOpponent implements MicroScript {

        @Override
        public boolean matches(Position2d rangerPosition, Position2d moveTo, ParsedGameState pgs) {
            Cell ranger = pgs.at(rangerPosition);
            Cell target = pgs.at(moveTo);

            int rang5from = ranger.getAttackerCount(5);
            int rang5to = target.getAttackerCount(5);
            int rang6from = ranger.getAttackerCount(6);
            int rang6to = target.getAttackerCount(6);
            int rang7from = ranger.getAttackerCount(7);
            int rang7to = target.getAttackerCount(7);
            int rang8from = ranger.getAttackerCount(8);
            int rang8to = target.getAttackerCount(8);

            if(rang5from > 0) {
                return false;
            }


            return false;

        }
    }


}
