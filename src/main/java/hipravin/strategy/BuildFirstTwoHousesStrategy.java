package hipravin.strategy;

import hipravin.alg.BruteForceUtil;
import hipravin.model.*;
import model.EntityType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildFirstTwoHousesStrategy implements SubStrategy {
    BeforeFirstHouseBuildOrder buildOrder;

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        if (currentParsedGameState.curTick() == 0) {
            buildOrder = findOptimalBeginning(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
        }


    }

    //extremely weak code, but no time :(
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
        Collections.shuffle(buildMines, new Random(0));//otherwise workers will spawn at one side

        //workerMinePos -> buildmine
        Map<Position2d, BeforeFirstHouseBuildOrder.BuildMine> buildMineMap = buildMines.stream()
                .collect(Collectors.toMap(bm -> bm.workerMinePosition, Function.identity(), (buildMine, buildMine2) -> buildMine));


        Stream<int[]> buildMinesIndices = BruteForceUtil.allCombinatonsOf(buildMines.size(), 4);

        long counter = 0;

        for (Iterator<int[]> iterator = buildMinesIndices.iterator(); iterator.hasNext(); counter++) {
            if (counter > StrategyParams.MAX_COMBINATIONS_BF) {
                break;
            }

            int[] ii = iterator.next();

            List<List<Position2d>> combinationsPositionsAtHouseBuild = new ArrayList<>();
            List<Position2d> positionsAtHouseBuild = Arrays.stream(ii)
                    .mapToObj(i -> buildMines.get(i).workerMinePosition).collect(Collectors.toList());

            if (!firstMinerPositionsAfterFirstMined.isEmpty()) {
                for (Position2d firstWorkerPosAtHouseBuild : firstMinerPositionsAfterFirstMined) {
                    if (!positionsAtHouseBuild.contains(firstWorkerPosAtHouseBuild)) {
                        List<Position2d> allPositionsAtHouseBuild = new ArrayList<>(positionsAtHouseBuild);
                        allPositionsAtHouseBuild.add(0, firstWorkerPosAtHouseBuild);
                        combinationsPositionsAtHouseBuild.add(allPositionsAtHouseBuild);
                    }
                }
            } else {
                combinationsPositionsAtHouseBuild.add(positionsAtHouseBuild);
            }

            for (List<Position2d> workerHouseBuildPositions : combinationsPositionsAtHouseBuild) {
                Optional<Position2d> housePosition =
                        checkIfTwoWorkerCanBuild(workerHouseBuildPositions, firstMiner.mineralPosition, currentParsedGameState, strategyParams);

                if (housePosition.isPresent()) {
                    BeforeFirstHouseBuildOrder bo = new BeforeFirstHouseBuildOrder();
                    bo.firstHouseWhereToBuild = housePosition.get();
                    bo.firstMineralToMine = firstMiner.mineralPosition;
                    bo.whereToMoveAfterFirstMineralBeingMined = (workerHouseBuildPositions.size() > 4) ? workerHouseBuildPositions.get(0) : null;

                    bo.workersWhereToBuild = lastFourElements(workerHouseBuildPositions)
                            .stream().map(wmp -> buildMineMap.get(wmp)).collect(Collectors.toList());

                    return bo;
                }
            }
        }

        return null;
    }

    static List<Position2d> lastFourElements(List<Position2d> workerHouseBuildPositions) {
        return workerHouseBuildPositions.size() > 4
                ? workerHouseBuildPositions.subList(1, workerHouseBuildPositions.size())
                : workerHouseBuildPositions;
    }

    Optional<Position2d> checkIfTwoWorkerCanBuild(List<Position2d> workers, Position2d firstMineralToMine, ParsedGameState pgs,
                                                  StrategyParams strategyParams) {

        Set<Position2d> options = Position2dUtil.housesThatCanBeBuildByTwoWorkers(workers);

        return options.stream().filter(hp ->
                isEmptyForHouseIgnoringUnitsAnd1Mineral(hp, firstMineralToMine, pgs)
                        && !strategyParams.firstHouseNonDesiredPositions().contains(hp))
                .findFirst();
    }

    public boolean isEmptyForHouseIgnoringUnitsAnd1Mineral(Position2d housePosition, Position2d mineralToIgnore, ParsedGameState pgs) {
        for (int i = 0; i < Position2dUtil.HOUSE_SIZE; i++) {
            for (int j = 0; j < Position2dUtil.HOUSE_SIZE; j++) {
                Position2d p = housePosition.shift(i, j);

                if (!mineralToIgnore.equals(p)
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
