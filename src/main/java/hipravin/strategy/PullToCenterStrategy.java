package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.model.*;
import hipravin.strategy.command.Command;
import hipravin.strategy.command.MoveTowardsCommand;
import model.EntityType;

import java.util.*;
import java.util.stream.Collectors;

public class PullToCenterStrategy implements SubStrategy {

    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                          StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        if(pgs.isRound1() || pgs.isRound2()) {
            return false;
        }

        if(pgs.getMyCc() == null) {
            return false;
        }

        if(pgs.getMyRangerBase() != null || pgs.getMyRangers().size() > 0 || pgs.curTick() > 300) {
            return false;
        }

        return true;
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {


        if(gameHistoryState.getWorkerPullToCenterRequests() > 0) {
            pullToCenter(gameHistoryState, pgs, strategyParams);
        }
    }

    void pullToCenter(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                      StrategyParams strategyParams) {

        if(gameHistoryState.getWorkerPullToCenterRequests() <= 0) {
            return;
        }

        Building mycc = pgs.findMyBuildings(EntityType.BUILDER_BASE).get(0);

        Set<Position2d> outerEdge = new HashSet<>(mycc.getBuildingOuterEdgeWithoutCorners());
        outerEdge.removeIf(p -> pgs.at(p).isBuilding());

        Set<Integer> busyEntities = gameHistoryState.allOngoingRelatedEntitiIdsExceptMineExact();

        Map<Position2d, NearestEntity> nesForwardIgnoreUnits = GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), outerEdge,
                StrategyParams.PULL_TO_CENTER_WS_RANGE, false, true, Set.of());

        Position2d closestToCenter = nesForwardIgnoreUnits.keySet().stream()
                .min(Comparator.comparingInt(p -> p.lenShiftSum(StrategyParams.DESIRED_BARRACK)))
                .orElse(null);

        if(closestToCenter == null) {
            return;
        }

        Map<Position2d, NearestEntity> nesBackwardWithUnits = GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), Set.of(closestToCenter),
                StrategyParams.PULL_TO_CENTER_WS_RANGE, false, false, Set.of());

        List<NearestEntity> workers = nesBackwardWithUnits.values()
                .stream().filter(ne -> ne.getThisCell().isMyWorker())
                .collect(Collectors.toList());

        workers.removeIf(ne -> busyEntities.contains(ne.getThisCell().getEntityId()));

        workers.sort(Comparator.comparingInt(w -> w.getThisCell().getPosition().lenShiftSum(mycc.getCornerCell().getPosition().shift(2,2))));

        if(workers.isEmpty()) {
            DebugOut.println("Can't find workers to pull");

            return;
        }

        workers = workers.subList(0, Math.min(workers.size(), gameHistoryState.workerPullToCenterRequests));

        for (NearestEntity worker : workers) {
            createPullToCenterCommand(worker.getThisCell().getPosition(), gameHistoryState, pgs, strategyParams);
        }
        gameHistoryState.setWorkerPullToCenterRequests(gameHistoryState.workerPullToCenterRequests - workers.size());
    }

    void createPullToCenterCommand(Position2d workerPosition, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                   StrategyParams strategyParams) {
        DebugOut.println("Pull to center: " + workerPosition);

        Command command = new MoveTowardsCommand(pgs, pgs.at(workerPosition).getEntityId(), StrategyParams.DESIRED_BARRACK,
                Position2dUtil.MAP_SIZE, 5);

        gameHistoryState.addOngoingCommand(command, false);
    }
}
