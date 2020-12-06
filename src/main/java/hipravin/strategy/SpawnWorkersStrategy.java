package hipravin.strategy;

import hipravin.model.*;
import hipravin.strategy.command.*;
import model.Entity;
import model.EntityType;

import java.util.*;
import java.util.stream.Collectors;

import static hipravin.strategy.StrategyParams.MAX_VAL;

public class SpawnWorkersStrategy implements SubStrategy {
    //to prevent multiple workers being sent to single mineral field
    LinkedHashSet<Position2d> lastMineralPositions = new LinkedHashSet<>();

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

        findBestSpawnPos(gameHistoryState, pgs, strategyParams);
    }

    public void findBestSpawnPos (
            GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
            StrategyParams strategyParams) {
        Set<Position2d> ccOuterEdge = pgs.findMyBuildings(EntityType.BUILDER_BASE).get(0)
                .getBuildingOuterEdgeWithoutCorners();

        ccOuterEdge.removeIf(p -> !pgs.at(p).isEmpty());

        if(ccOuterEdge.isEmpty()) {
            return;
        }

        if( (double)pgs.getMyWorkersAtMapCorner() / (pgs.getMineralsAtMapCorner() + 1.0) > strategyParams.mapCornerMiningRatio) {
            if (ccOuterEdge.stream().anyMatch(p -> !Position2dUtil.isMapMyCornerPosition(p))) {
                ccOuterEdge.removeIf(Position2dUtil::isMapMyCornerPosition);
            }
        }

        Map<Position2d, NearestEntity> nearestMinerals = GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), ccOuterEdge, MAX_VAL, false);
        nearestMinerals.entrySet().removeIf(e ->
                lastMineralPositions.contains(e.getKey()) || !pgs.at(e.getKey()).isMineralEdge());//not spawn to workers to same mf

        Comparator<NearestEntity> byPathLenNearestNe = Comparator.comparingInt(NearestEntity::getPathLenEmptyCellsToThisCell);

        Optional<NearestEntity> bestMineral = nearestMinerals.values().stream().min(byPathLenNearestNe);

        if(bestMineral.isPresent()) {
            addToLastSpawn(bestMineral.get().getThisCell().getPosition(), strategyParams);

            Command buildWorker = new BuildWorkerCommand(bestMineral.get().getSourceCell().getPosition(), pgs);
            Command mineExact = new MineFromExactPositionCommand(bestMineral.get().getSourceCell().getPosition(), null, bestMineral.get().getThisCell().getPosition());
            CommandUtil.chainCommands(buildWorker, mineExact);

            gameHistoryState.addOngoingCommand(buildWorker, false);
        } else {
            //spawn randomly
            //spawnIfNoPathToMinerals(gameHistoryState, pgs, strategyParams);
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

    void addToLastSpawn(Position2d lastMineralToMine, StrategyParams strategyParams) {
        while(lastMineralPositions.size() > strategyParams.maxSpawnToMineralsRememberCount) {
            lastMineralPositions.remove(lastMineralPositions.iterator().next());
        }

        lastMineralPositions.add(lastMineralToMine);
    }

    public LinkedHashSet<Position2d> getLastMineralPositions() {
        return lastMineralPositions;
    }
}
