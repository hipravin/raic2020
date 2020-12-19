package hipravin.strategy;

import hipravin.model.*;
import hipravin.strategy.command.*;
import model.*;

import java.util.*;
import java.util.stream.Collectors;

import static hipravin.model.Position2d.of;


public class AutomineFallbackStrategy implements SubStrategy {
    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        autoAttackAllUnboundWorkers(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }

    public static Position2d tryToUnblockTheWay(Position2d cur, ParsedGameState pgs) {
        Position2d result = null;

        if (!Position2dUtil.isPositionWithinMapBorder(cur.shift(3, 3))) {
            return null;
        }

        if (cur.x > cur.y && cur.x > 10) {
            if (pgs.at(cur.shift(1, 0)).isEmpty() && pgs.at(cur.shift(2, 0)).isEmpty() && pgs.at(cur.shift(3, 0)).isEmpty()) {
                result = cur.shift(3, 0);
            } else if (pgs.at(cur.shift(1, 0)).isEmpty() && pgs.at(cur.shift(2, 0)).isEmpty()) {
                result = cur.shift(2, 0);
            } else if (pgs.at(cur.shift(1, 0)).isEmpty()) {
                result = cur.shift(1, 0);
            }
        } else if (cur.y > 10) {
            if (pgs.at(cur.shift(0, 1)).isEmpty() && pgs.at(cur.shift(0, 2)).isEmpty() && pgs.at(cur.shift(0, 3)).isEmpty()) {
                result = cur.shift(0, 3);
            } else if (pgs.at(cur.shift(0, 1)).isEmpty() && pgs.at(cur.shift(0, 2)).isEmpty()) {
                result = cur.shift(0, 2);
            } else if (pgs.at(cur.shift(0, 1)).isEmpty()) {
                result = cur.shift(0, 1);
            }
        }

        return result;
    }


    public void autoAttackAllUnboundWorkers(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                                            StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Set<Integer> busyEntities = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();

        List<Entity> notBusyWorkers = currentParsedGameState.getMyWorkers()
                .values().stream().map(Cell::getEntity)
                .filter(e -> !busyEntities.contains(e.getId()))
                .collect(Collectors.toList());

        currentParsedGameState.getMyWorkers().forEach((id, wc) -> {
            if (!handleRunAway(wc.getEntity(), gameHistoryState, currentParsedGameState, strategyParams, assignedActions)) {
                if (!busyEntities.contains(id)) {
                    if (!handleNoCloseMinerals(wc.getEntity(), gameHistoryState, currentParsedGameState, strategyParams, assignedActions)) {
                        justMine(wc.getEntity(), gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
                    }
                }
            }
        });


//
//        notBusyWorkers.forEach(w -> {
//            if (!handleRunAway(w, gameHistoryState, currentParsedGameState, strategyParams, assignedActions)
//                    && !handleNoCloseMinerals(w, gameHistoryState, currentParsedGameState, strategyParams, assignedActions)) {
//
//
//                justMine(w, gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
//            }
//        });

//        handleWayBlockers(busyEntities, gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }

    public boolean handleNoCloseMinerals(Entity w, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                         StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        NearestEntity nearestMineral = pgs.at(w.getPosition()).getNearestMineralField();
        if (nearestMineral != null && nearestMineral.getPathLenEmptyCellsToThisCell() <= Position2dUtil.WORKER_SIGHT_RANGE) {
            return false;
        }
        if (nearestMineral != null) {
            Command moveTo = new MoveTowardsCommand(pgs, w.getId(), nearestMineral.getSourceCell().getPosition(), nearestMineral.getPathLenEmptyCellsToThisCell() - 2, strategyParams.moveTowardsMineralsDistanceTreshold);

            gameHistoryState.addOngoingCommand(moveTo, false);

            return true;
        } else {
            Position2d to = strategyParams.sendToDesiredBarrackPosition;
            Command moveTo;
            if (of(w.getPosition()).lenShiftSum(to) < 15) {
                moveTo = new MoveTowardsCommand(pgs, w.getId(),
                        Position2dUtil.randomMapPosition(),
                        Position2dUtil.MAP_SIZE, strategyParams.moveTowardsMineralsDistanceTreshold);

            } else {
                //send to center
                moveTo = new MoveTowardsCommand(pgs, w.getId(), to, 10, strategyParams.moveTowardsMineralsDistanceTreshold);
            }

            moveTo.setConditionalReplacer(new CancelCommand(), new CommandPredicate() {
                @Override
                public boolean test(Command command, ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState, StrategyParams strategyParams) {
                    if (!pgs.getEntityIdToCell().containsKey(w.getId())) {
                        return false;
                    }

                    NearestEntity m = pgs.getEntityIdToCell().get(w.getId()).getNearestMineralField();
                    return m != null
                            && m.getPathLenEmptyCellsToThisCell() <= Position2dUtil.WORKER_SIGHT_RANGE;
                }
            });


            gameHistoryState.addOngoingCommand(moveTo, false);


            return true;
        }
    }

    public boolean handleRunAway(Entity w, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                 StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Entity rangBase = pgs.getMyRangerBase();


        if (!pgs.getEnemyArmy().isEmpty()) {
            if (pgs.at(w.getPosition()).test(
                    c -> c.getAttackerCount(8) > 0
                            || c.getAttackerCount(7) > 0
                            || c.getAttackerCount(6) > 0
                            || c.getAttackerCount(5) > 0
            )) {

                Position2d wp = of(w.getPosition());
                Position2d nearestEnemy = nearestEnemyUnitToPosition(of(w.getPosition()), pgs).orElse(null);

                if (nearestEnemy != null) {
                    Position2d runTo = Position2dUtil.runAwayDoubleDistance(wp, nearestEnemy);
                    Command retreatToRangBase = new MoveTowardsCommand(pgs, w.getId(),
                            runTo, 6, 5);
                    gameHistoryState.addOngoingCommand(retreatToRangBase, true);

                    return true;
                }


                if (rangBase != null) {
                    Command retreatToRangBase = new MoveTowardsCommand(pgs, w.getId(),
                            of(rangBase.getPosition()).shift(-5, -5), 10, 5);
                    gameHistoryState.addOngoingCommand(retreatToRangBase, true);

                    return true;
                }
            }
        }

        return false;
    }


    Optional<Position2d> nearestEnemyUnitToPosition(Position2d toPosition, ParsedGameState pgs) {
        return pgs.getEnemyArmy().keySet()
                .stream()
                .min(Comparator.comparingInt(p -> p.lenShiftSum(toPosition)));

    }

    public void justMine(Entity w, GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                         StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(Position2dUtil.WORKER_SIGHT_RANGE,
                new EntityType[]{EntityType.RESOURCE, EntityType.WALL, EntityType.BUILDER_UNIT}));
        autoAttack.setAttackAction(attackAction);
        assignedActions.put(w.getId(), new ValuedEntityAction(0.5, w.getId(), autoAttack));
    }
}
