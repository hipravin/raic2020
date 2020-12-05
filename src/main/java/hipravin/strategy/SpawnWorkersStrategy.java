package hipravin.strategy;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.command.BuildWorkerCommand;
import hipravin.strategy.command.Command;
import model.Entity;
import model.EntityType;

import java.util.*;
import java.util.stream.Collectors;

public class SpawnWorkersStrategy implements SubStrategy {

    boolean shouldSpawnMoreWorkers(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                   StrategyParams strategyParams) {
        return true; //TODO: research conditions
    }

    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                          StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Entity mycc = pgs.getMyCc();
        if (mycc != null) {
            boolean ccLocked = gameHistoryState.allOngoingCommandRelatedEntitiIds().anyMatch(id -> id.equals(mycc.getId()));

            return !ccLocked;
        }

        return false;
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        if (!shouldSpawnMoreWorkers(gameHistoryState, pgs, strategyParams)) {
            return;
        }

        Optional<Position2d> bestSpawn = bestSpawnPos(gameHistoryState, pgs, strategyParams);

        bestSpawn.ifPresent(sp -> {
            Command buildWorker = new BuildWorkerCommand(sp, pgs);
            gameHistoryState.addOngoingCommand(buildWorker, false);
        });
    }

    public Optional<Position2d> bestSpawnPos (
            GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
            StrategyParams strategyParams) {
        Set<Position2d> ccOuterEdge = pgs.findMyBuildings(EntityType.BUILDER_BASE).get(0)
                .getBuildingOuterEdgeWithoutCorners();

        ccOuterEdge.removeIf(p -> !pgs.at(p).isEmpty());

        if(ccOuterEdge.isEmpty()) {
            return Optional.empty();
        }

        if( (double)pgs.getMyWorkersAtMapCorner() / (pgs.getMineralsAtMapCorner() + 1.0) > strategyParams.mapCornerMiningRatio) {
            if (ccOuterEdge.stream().anyMatch(p -> !Position2dUtil.isMapMyCornerPosition(p))) {
                ccOuterEdge.removeIf(Position2dUtil::isMapMyCornerPosition);
            }
        }

        Comparator<Position2d> byPathLenNearest = Comparator.comparingInt(p -> pgs.at(p).getPathLenToNearestMineralOrInf());
        Comparator<Position2d> byPathLenThenxy = byPathLenNearest.thenComparing(p -> p.x + p.y, Comparator.reverseOrder());

        List<Position2d> sorted = ccOuterEdge.stream()
                .sorted(byPathLenThenxy)
                .collect(Collectors.toList());

        if(sorted.size() > 0) {
            return Optional.of(sorted.get(0));
        } else {
            //spawn randomly
            return spawnIfNoPathToMinerals(gameHistoryState, pgs, strategyParams);
        }
    }

    Optional<Position2d> spawnIfNoPathToMinerals(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                 StrategyParams strategyParams) {
        Set<Position2d> ccOuterEdge = pgs.findMyBuildings(EntityType.BUILDER_BASE).get(0)
                .getBuildingOuterEdgeWithoutCorners();

        ccOuterEdge.removeIf(p -> !pgs.at(p).isEmpty());
        Comparator<Position2d> topRight = Comparator.comparing(p -> p.x + p.y, Comparator.reverseOrder());

        List<Position2d> sorted = ccOuterEdge.stream()
                .sorted(topRight)
                .collect(Collectors.toList());
        if(sorted.isEmpty()) {
            return Optional.empty();
        } else if(sorted.size() == 1) {
            return Optional.of(sorted.get(0));
        } else {
            int idx = GameHistoryAndSharedState.random.nextInt() % 2;//first or second element
            return Optional.of(sorted.get(idx));
        }
    }
}
