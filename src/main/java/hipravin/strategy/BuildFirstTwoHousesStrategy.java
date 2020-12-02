package hipravin.strategy;

import hipravin.alg.BruteForceUtil;
import hipravin.model.*;
import model.EntityType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hipravin.alg.BruteForceUtil.allCombinatonsOf;

public class BuildFirstTwoHousesStrategy implements SubStrategy {
    BeforeFirstHouseBuildOrder buildOrder;

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        if (currentParsedGameState.curTick() == 0) {
            buildOrder = findOptimalBeginning(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
        }


    }

    public BeforeFirstHouseBuildOrder findOptimalBeginning(
            GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
            StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Position2d firstWorker = currentParsedGameState.getMyWorkers().values().iterator().next().getPosition();


        MineralAndMinerPosition firstMiner = minerAndMineralClosest(firstWorker, gameHistoryState, currentParsedGameState, strategyParams, assignedActions);

        List<Position2d> firstMinerPositionsAfterFirstMined = Position2dUtil.upRightLeftDown(firstMiner.minerPosition)
                .filter(p -> currentParsedGameState.at(p)
                        .test(c -> (c.getPosition().equals(firstMiner.mineralPosition) || c.isEmpty())
                                && c.getLen1MineralsCount() > 0))
                .collect(Collectors.toList());

        List<BeforeFirstHouseBuildOrder.BuildMine> buildMines =
                bestBuildMines(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);


        Stream<int[]> buildMinesIndices = BruteForceUtil.allCombinatonsOf(buildMines.size(), 4);

        buildMinesIndices.map(ii -> {
            List<List<Position2d>> combinationsPositionsAtHouseBuild = new ArrayList<>();
            List<Position2d> positionsAtHouseBuild = Arrays.stream(ii)
                    .mapToObj(i -> buildMines.get(i).workerMinePosition).collect(Collectors.toList());

            if(!firstMinerPositionsAfterFirstMined.isEmpty()) {
                for (Position2d firstWorkerPosAtHouseBuild : firstMinerPositionsAfterFirstMined) {
                    List<Position2d> allPositionsAtHouseBuild = new ArrayList<>(positionsAtHouseBuild);
                    allPositionsAtHouseBuild.add(firstWorkerPosAtHouseBuild);
                    combinationsPositionsAtHouseBuild.add(allPositionsAtHouseBuild);
                }
            } else {
                combinationsPositionsAtHouseBuild.add(positionsAtHouseBuild);
            }
            checkIfTwoWorkersCanBUILD();//////

            return null;//convert to build order

        });




        //1. find nearest mineral to first worker.

        //2. find other minerals at same distance. Skip for now, choose random (first). Probability is too low, code is to complex.

        //3. For each mineral from step 2:

        //4. Lock mineral collected by first worker. Skip. too comlicated
        //5. Find best distinct positions to spawn workers (M positions >= 4)
        //6. randomly choose 4 out of M, remember seed
        //7. If first worker requires moving after mineral collection then iterate over all options:
        //8. Check if there is a house that can be built with 2 workers. If true -> stop

        //9. If no option to build house with two workers, repeat, but try to build 1 house.
        //10. If can't build event 1 house -> then choose option to send 3 workers to bottom or to left to clear space asap


        return null;

    }

    void checkIfTwoWorkersCanBUILD() {

    }

    public boolean isEmptyForHouseIgnoringUnitsAnd1Mineral(Position2d housePosition, Position2d mineralToIgnore, ParsedGameState pgs) {
        for (int i = 0; i < Position2dUtil.HOUSE_SIZE; i++) {
            for (int j = 0; j < Position2dUtil.HOUSE_SIZE; j++) {
                Position2d p = housePosition.shift(i, j);

                if(!mineralToIgnore.equals(p)
                        && !(pgs.at(p).test(c -> c.isUnit() || c.isEmpty()))) {
                    return false;
                }
            }

        }
        return true;
    }


    //    should return at least 4
    public List<BeforeFirstHouseBuildOrder.BuildMine> bestBuildMines(
            GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
            StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Set<Position2d> ccOuterEdge = pgs.findMyBuildings(EntityType.BUILDER_BASE).get(0)
                .getBuildingOuterEdgeWithoutCorners();

        int workersRequred = 4;

        List<Position2d> sorted = ccOuterEdge.stream()
                .sorted(Comparator.comparingInt(p -> pgs.at(p).getNearestMineralField().getPathLenEmptyCellsToThisCell()))
                .collect(Collectors.toList());
        if (sorted.size() >= workersRequred) {
            int minLen = pgs.at(sorted.get(0)).getNearestMineralField().getPathLenEmptyCellsToThisCell();

            long bestPositionsSameLenCount = sorted.stream().takeWhile(
                    p -> pgs.at(p).getNearestMineralField().getPathLenEmptyCellsToThisCell() == minLen)
                    .count();

            List<Position2d> bestPositions = sorted.subList(0, (int) Math.max(workersRequred, bestPositionsSameLenCount));

            List<MineralAndMinerPosition> mmps = bestPositions.stream()
                    .map(p -> minerAndMineralClosest(p, gameHistoryState, pgs, strategyParams, assignedActions))
                    .collect(Collectors.toList());

            List<BeforeFirstHouseBuildOrder.BuildMine> bestBuildMines = new ArrayList<>();

            for (int i = 0; i < bestPositions.size(); i++) {
                bestBuildMines.add(new BeforeFirstHouseBuildOrder.BuildMine(bestPositions.get(i),
                        mmps.get(i).minerPosition, mmps.get(i).mineralPosition));

            }
            return bestBuildMines;
        }


        return Collections.emptyList();


    }

    public MineralAndMinerPosition minerAndMineralClosest(
            Position2d workerPosition,
            GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
            StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        int toNearestMineral = pgs.at(workerPosition).getNearestMineralField().getPathLenEmptyCellsToThisCell();
        Map<Position2d, NearestEntity> toMinerals =
                GameStateParserDjkstra.shortWideSearch(pgs, Collections.emptySet(), Set.of(workerPosition), toNearestMineral);

        Position2d mineralPosition = toMinerals.values().stream()
                .filter(ne -> ne.getPathLenEmptyCellsToThisCell() == toNearestMineral)
                .filter(ne -> ne.getThisCell().isMineral())
                .map(ne -> ne.getThisCell().getPosition())
                .findAny().orElse(null);

        if (mineralPosition != null) {
            Position2d miningPos = toMinerals.values().stream()
                    .filter(ne -> ne.getPathLenEmptyCellsToThisCell() == toNearestMineral - 1)
                    .filter(ne -> ne.getThisCell().getPosition().lenShiftSum(mineralPosition) == 1) //neighbour
                    .map(ne -> ne.getThisCell().getPosition())
                    .findAny().orElse(null);

            if (miningPos != null) {
                return new MineralAndMinerPosition(miningPos, mineralPosition);
            }
        }

        return null; //algorithm error or non-reacheable
    }


    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        return currentParsedGameState.findMyBuildings(EntityType.HOUSE).size() < 2
                && currentParsedGameState.getPlayerView().getCurrentTick() < 100; //just in case

    }

    @Override
    public boolean isExclusivelyApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        return isApplicableAtThisTick(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }

    public static class MineralAndMinerPosition {
        public final Position2d minerPosition;
        public final Position2d mineralPosition;

        public MineralAndMinerPosition(Position2d minerPosition, Position2d mineralPosition) {
            this.minerPosition = minerPosition;
            this.mineralPosition = mineralPosition;
        }
    }

}
