package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.command.Command;
import hipravin.strategy.command.MoveSingleCommand;
import model.Entity;

import java.util.*;
import java.util.stream.Collectors;

import static hipravin.model.Position2d.of;

public class PushMinersStrategy implements SubStrategy {

    static final Position2d basePoint = Position2dUtil.MY_CC.shift(2, 2);

    List<PushScript> pushMineralScripts = new ArrayList<>();

    int lastWorkerCount = -1;

    public PushMinersStrategy() {
        pushMineralScripts.add(new DeadEndMineralScript());
        pushMineralScripts.add(new Step1ToNextMineral());
        pushMineralScripts.add(new Step2JumpNextMineral());
    }

    boolean tryApplyPushThisTick(ParsedGameState currentParsedGameState, StrategyParams strategyParams) {
        return currentParsedGameState.getMyWorkers().size() > lastWorkerCount
                && lastWorkerCount > strategyParams.minWorkersToApplyPush
                && lastWorkerCount < strategyParams.maxWorkersToApplyPush
                ;
    }


    void tryPush1WorkerToGainMoreMineralFieldsAvailable(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                                                        StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Set<Integer> busyEntities = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();

        List<Entity> notBusyWorkers = currentParsedGameState.getMyWorkers()
                .values().stream().map(Cell::getEntity)
                .filter(e -> !busyEntities.contains(e.getId()))
                .collect(Collectors.toList());

        notBusyWorkers.sort(Comparator.comparingInt(w -> (int) of(w.getPosition()).lenShiftSum(basePoint)));//try to push closer workers first

        for (Entity notBusyWorker : notBusyWorkers) {
            for (PushScript script : pushMineralScripts) {
                if (script.tryApply(of(notBusyWorker.getPosition()), gameHistoryState, currentParsedGameState, strategyParams, assignedActions)) {
                    DebugOut.println("Push script " + script + " applied to worker" + of(notBusyWorker.getPosition()));
                    return;
                }
            }
        }
    }


    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        //if rangesr are started producing then better to stay compact and max income
        return strategyParams.useWorkerPush && (pgs.getMyRangerBase() == null || pgs.getMyRangerBase().isActive());
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        if (tryApplyPushThisTick(currentParsedGameState, strategyParams)) {
            tryPush1WorkerToGainMoreMineralFieldsAvailable(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
        }

        lastWorkerCount = currentParsedGameState.getMyWorkers().size();
    }


    static void applyMove(Position2d worker, Position2d target, int pathLen,
                          GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams,
                          Map<Integer, ValuedEntityAction> assignedActions) {
        Command move = new MoveSingleCommand(pgs, pgs.at(worker).getEntityId(), target, pathLen);
        gameHistoryState.addOngoingCommand(move, false);
    }

    static boolean inMap(Position2d p) {
        return Position2dUtil.isPositionWithinMapBorder(p);
    }

    static int countMineralsUpRightLeftDown(Position2d p, ParsedGameState pgs) {
        int count = 0;
        if (isMineral(p.shift(0, 1), pgs)) {
            count++;
        }
        if (isMineral(p.shift(1, 0), pgs)) {
            count++;
        }
        if (isMineral(p.shift(-1, 0), pgs)) {
            count++;
        }
        if (isMineral(p.shift(0, -1), pgs)) {
            count++;
        }

        return count;
    }

    static int countWorkersUpRightLeftDown(Position2d p, ParsedGameState pgs) {
        int count = 0;
        if (isWorker(p.shift(0, 1), pgs)) {
            count++;
        }
        if (isWorker(p.shift(1, 0), pgs)) {
            count++;
        }
        if (isWorker(p.shift(-1, 0), pgs)) {
            count++;
        }
        if (isWorker(p.shift(0, -1), pgs)) {
            count++;
        }

        return count;
    }

    public static boolean isEmpty(Position2d p, ParsedGameState pgs) {
        if (!Position2dUtil.isPositionWithinMapBorder(p)) {
            return false;
        }
        return pgs.at(p).isEmpty();
    }

    public static boolean isMineral(Position2d p, ParsedGameState pgs) {
        if (!Position2dUtil.isPositionWithinMapBorder(p)) {
            return false;
        }

        return pgs.at(p).isMineral();
    }

    public static boolean isBuilding(Position2d p, ParsedGameState pgs) {
        if (!Position2dUtil.isPositionWithinMapBorder(p)) {
            return false;
        }

        return pgs.at(p).isBuilding();
    }

    public static boolean isWorker(Position2d p, ParsedGameState pgs) {
        if (!Position2dUtil.isPositionWithinMapBorder(p)) {
            return false;
        }
        return pgs.at(p).isMyWorker();
    }

    interface PushScript {
        boolean tryApply(Position2d worker,
                         GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams,
                         Map<Integer, ValuedEntityAction> assignedActions);
    }

    static class Step2JumpNextMineral implements PushScript {

        @Override
        public boolean tryApply(Position2d worker, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
            Position2d p = worker.shift(2, 0);
            if (isFreeMoveToNextMineral2(worker, worker.shift(1, 0), p, pgs)) {
                applyMove(worker, p, 2, gameHistoryState, pgs, strategyParams, assignedActions);
                return true;
            }

            p = worker.shift(0, 2);
            if (isFreeMoveToNextMineral2(worker, worker.shift(0, 1), p, pgs)) {
                applyMove(worker, p, 2, gameHistoryState, pgs, strategyParams, assignedActions);
                return true;
            }

            p = worker.shift(-2, 0);
            if (isFreeMoveToNextMineral2(worker, worker.shift(-1, 0), p, pgs)) {
                applyMove(worker, p, 2, gameHistoryState, pgs, strategyParams, assignedActions);
                return true;
            }

            p = worker.shift(0, -2);
            if (isFreeMoveToNextMineral2(worker, worker.shift(0, -1), p, pgs)) {
                applyMove(worker, p, 2, gameHistoryState, pgs, strategyParams, assignedActions);
                return true;
            }
            return false;
        }

        boolean isFreeMoveToNextMineral2(Position2d worker, Position2d mid, Position2d to, ParsedGameState pgs) {
            return isEmpty(mid, pgs) && isEmpty(to, pgs) && countMineralsUpRightLeftDown(to, pgs) > 0
                    && to.lenShiftSum(basePoint) > worker.lenShiftSum(basePoint); //avoid conflicting moves
        }
    }


    static class Step1ToNextMineral implements PushScript {

        @Override
        public boolean tryApply(Position2d worker, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
            Position2d p = worker.shift(1, 0);
            if (isFreeMoveToNextMineral(worker, p, pgs)) {
                applyMove(worker, p, 1, gameHistoryState, pgs, strategyParams, assignedActions);
                return true;
            }

            p = worker.shift(0, 1);
            if (isFreeMoveToNextMineral(worker, p, pgs)) {
                applyMove(worker, p, 1, gameHistoryState, pgs, strategyParams, assignedActions);
                return true;
            }

            p = worker.shift(-1, 0);
            if (isFreeMoveToNextMineral(worker, p, pgs)) {
                applyMove(worker, p, 1, gameHistoryState, pgs, strategyParams, assignedActions);
                return true;
            }

            p = worker.shift(0, -1);
            if (isFreeMoveToNextMineral(worker, p, pgs)) {
                applyMove(worker, p, 1, gameHistoryState, pgs, strategyParams, assignedActions);
                return true;
            }
            return false;
        }

        boolean isFreeMoveToNextMineral(Position2d worker, Position2d p, ParsedGameState pgs) {
            return isEmpty(p, pgs) && countMineralsUpRightLeftDown(p, pgs) > 0
                    && countWorkersUpRightLeftDown(p, pgs) == 1
                    && p.lenShiftSum(basePoint) > worker.lenShiftSum(basePoint); //avoid conflicting moves
        }
    }

    static class DeadEndMineralScript implements PushScript {

        @Override
        public boolean tryApply(Position2d worker, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

            Position2d p = worker.shift(1, 0);
            if (isDeadEnd(p, pgs)) {
                applyMove(worker, p, 1, gameHistoryState, pgs, strategyParams, assignedActions);
                return true;
            }
            p = worker.shift(0, 1);
            if (isDeadEnd(p, pgs)) {
                applyMove(worker, p, 1, gameHistoryState, pgs, strategyParams, assignedActions);
                return true;
            }
            p = worker.shift(-1, 0);

            if (isDeadEnd(p, pgs)) {
                applyMove(worker, p, 1, gameHistoryState, pgs, strategyParams, assignedActions);
                return true;
            }
            p = worker.shift(0, -1);

            if (isDeadEnd(p, pgs)) {
                applyMove(worker, p, 1, gameHistoryState, pgs, strategyParams, assignedActions);
                return true;
            }

            return false;
        }

        boolean isDeadEnd(Position2d p, ParsedGameState pgs) {
            return isEmpty(p, pgs) && countMineralsUpRightLeftDown(p, pgs) == 3;
        }
    }
}
