package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.model.*;
import hipravin.strategy.command.Command;
import hipravin.strategy.command.MoveTowardsCommand;

import java.util.*;
import java.util.stream.Collectors;

public class WayOutWorkersBlockingDetectingStrategy implements SubStrategy {
    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        return pgs.getPopulation().getPopulationUse() < strategyParams.wayOutBlockFindMaxPopulation
                && pgs.getPopulation().getPopulationUse() > strategyParams.wayOutBlockFindMinPopulation
                && (!pgs.getNewEntityIds().isEmpty() || !pgs.getWorkersMovedSinceLastTick().isEmpty());
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        findWayBlockers(gameHistoryState, pgs, strategyParams, assignedActions);
    }

    public boolean findWayBlockers(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                         StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {


        Set<Position2d> emptyFogEdge = pgs.allCellsAsStream().filter(c -> c.isFogEdge() && c.isEmpty())
                .map(Cell::getPosition)
                .collect(Collectors.toSet());

        Map<Position2d, NearestEntity> nes =
                GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), emptyFogEdge, strategyParams.wayOutFindPathLen, false, false, Set.of());

        Set<Position2d> workersFromFog = nes.values().stream()
                .filter(ne -> ne.getThisCell().isMyWorker())
                .map(c -> c.getThisCell().getPosition())
                .collect(Collectors.toSet());

        Map<Position2d, NearestEntity> nesIgnoreUnits =
                GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), emptyFogEdge, strategyParams.wayOutFindPathLen, false, true, Set.of());

        Set<Position2d> workersFromFogIgnoreUnits = nesIgnoreUnits.values().stream()
                .filter(ne -> ne.getThisCell().isMyWorker())
                .map(c -> c.getThisCell().getPosition())
                .collect(Collectors.toSet());

        if (workersFromFogIgnoreUnits.size() - workersFromFog.size() >= strategyParams.wayOutWorkerCountDiff) {
            workersFromFog.retainAll(workersFromFogIgnoreUnits);//now they all are candidates
            DebugOut.println("Way is blocked detected, possible blockers: " + workersFromFog);


            Set<Integer> busyEntities = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();
            List<Position2d> notBusyWorkers = pgs.getMyWorkers()
                    .values().stream().map(Cell::getEntity)
                    .filter(e -> !busyEntities.contains(e.getId()))
                    .map(e -> pgs.getEntityIdToCell().get(e.getId()).getPosition())
                    .collect(Collectors.toList());

            workersFromFog.retainAll(notBusyWorkers);

            List<Position2d> blockerCandidates = new ArrayList<>(workersFromFog);

            if (blockerCandidates.size() > strategyParams.wayOutWorkerMaxPullCount) {
                blockerCandidates.sort(Comparator.comparing(w -> nes.get(w).getPathLenEmptyCellsToThisCell(), Comparator.reverseOrder()));
                blockerCandidates = blockerCandidates.subList(0, strategyParams.wayOutWorkerMaxPullCount);
            }

            DebugOut.println("Way is blocked detected, move out : " + blockerCandidates);

            for (Position2d blockerCandidate : blockerCandidates) {
                createMoveTowardsNearestFog(blockerCandidate, nes, gameHistoryState, pgs, strategyParams);
            }
            return !blockerCandidates.isEmpty();
        }
        return false;
    }

    void createMoveTowardsNearestFog(Position2d worker, Map<Position2d, NearestEntity> nes,
                                     GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                     StrategyParams strategyParams) {

        Command moveTowards = new MoveTowardsCommand(pgs, pgs.at(worker).getEntityId(), nes.get(worker).getSourceCell().getPosition(),
                nes.get(worker).getPathLenEmptyCellsToThisCell(), 3);

        gameHistoryState.addOngoingCommand(moveTowards, false);
    }

}
