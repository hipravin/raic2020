package hipravin.strategy;

import hipravin.DebugOut;
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

    boolean detectEconomyIsStuck() {
        return false;
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

        //more econditions?
        Optional<NearestEntity> bestMineral = bestMineral(nearestMinerals, strategyParams);

        if(bestMineral.isPresent()) {
            addToLastSpawn(bestMineral.get().getThisCell().getPosition(), strategyParams);

            Command buildWorker = new BuildWorkerCommand(bestMineral.get().getSourceCell().getPosition(), pgs);
            Command mineExact = new MineFromExactPositionCommand(bestMineral.get().getSourceCell().getPosition(),
                    null, bestMineral.get().getThisCell().getPosition(), closeToMineTargetPredicate);
            CommandUtil.chainCommands(buildWorker, mineExact);

            gameHistoryState.addOngoingCommand(buildWorker, false);
        } else {
            //spawn randomly and send to random fog
            spawnIfNoPathToMinerals(gameHistoryState, pgs, strategyParams);
        }
    }

    CommandPredicate closeToMineTargetPredicate = new CommandPredicate() {
        @Override
        public boolean test(Command command, ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState, StrategyParams strategyParams) {
            if(!(command instanceof MineExactMineral)) {
                return false;
            }
            MineExactMineral mem = (MineExactMineral) command;
            Optional<Position2d> minerCurrentPos = Optional.ofNullable(pgs.getMyWorkers().get(mem.getMinerId())).map(Cell::getPosition);
            if(minerCurrentPos.isPresent()
                    && minerCurrentPos.get().lenShiftSum(mem.getMineralToMine()) <= strategyParams.switchToAutoMineRange) {
                return true;
            }

            return false;
        }
    };

    Optional<NearestEntity> bestMineral(Map<Position2d, NearestEntity> nearestMinerals, StrategyParams strategyParams) {
        if(nearestMinerals.isEmpty()) {
            return Optional.empty();
        }

        Comparator<NearestEntity> byPathLenNearestNe = Comparator.comparingInt(NearestEntity::getPathLenEmptyCellsToThisCell);

        List<NearestEntity> nearestListSorted = new ArrayList<>(nearestMinerals.values());
        nearestListSorted.sort(byPathLenNearestNe);

        if(nearestListSorted.size() == 1) {
            return Optional.of(nearestListSorted.get(0));
        } else {
            if(strategyParams.ifRandom(strategyParams.bestMineralSpawnProb)) {
                return Optional.of(nearestListSorted.get(0));
            }
            if(strategyParams.ifRandom(strategyParams.worstMineralSpawnProb)) {
                return Optional.of(nearestListSorted.get(nearestListSorted.size() -1));
            }

            int randIdx = GameHistoryAndSharedState.random.nextInt(nearestListSorted.size());
            return Optional.of(nearestListSorted.get(randIdx));
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
            DebugOut.println("Can't find spawn location for worker");
            return Optional.empty();
        } else if(sorted.size() == 1) {
            return Optional.of(sorted.get(0));
        } else {
            int idx = Math.abs(GameHistoryAndSharedState.random.nextInt() % 2);//first or second element
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
