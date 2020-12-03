package hipravin.strategy;

import hipravin.alg.BruteForceUtil;
import hipravin.model.*;
import model.EntityType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * According to my perfect calculations we need to build first house with exact 3 workers. And 2 workers at desperate
 */
public class BuildFirstHouseFinalStrategy implements SubStrategy {

    BeforeFirstHouseBuildOrder buildOrder;

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        if (currentParsedGameState.curTick() == 0) {
            buildOrder = tryToFind2Build1DiagRepair(gameHistoryState, currentParsedGameState, strategyParams, assignedActions).orElse(null);


        }
    }

    void createBuildWorkerCommands(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                                   StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {


    }


    //loot at this! what the heck is going on? can this method be 1000 lines long?
    //but what can I do is time is limited and rules are changing so fast
    public Optional<BeforeFirstHouseBuildOrder> tryToFind2Build1DiagRepair(
            GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
            StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Position2d firstWorker = currentParsedGameState.getMyWorkers().values().iterator().next().getPosition();


        BuildFirstHouseFinalStrategy.MineralAndMinerPosition firstMiner = minerAndMineralClosest(firstWorker, gameHistoryState, currentParsedGameState, strategyParams, assignedActions);

        List<Position2d> firstMinerPositionsAfterFirstMined = Position2dUtil.upRightLeftDown(firstMiner.minerPosition)
                .filter(p -> currentParsedGameState.at(p)
                        .test(c -> (c.getPosition().equals(firstMiner.mineralPosition) || c.isEmpty())
                                && c.getLen1MineralsCount() > 0))
                .collect(Collectors.toList());


        for (Position2d firstWorkerAfterMinePosition : firstMinerPositionsAfterFirstMined) {
            List<BeforeFirstHouseBuildOrder.BuildMine> buildMines =
                    bestBuildMines(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);

            Map<Position2d, BeforeFirstHouseBuildOrder.BuildMine> buildMineMap = buildMines.stream()
                    .collect(Collectors.toMap(bm -> bm.workerMinePosition, Function.identity(), (buildMine, buildMine2) -> buildMine));

            buildMines.removeIf(bm -> bm.workerMinePosition.equals(firstWorkerAfterMinePosition));
            Set<Position2d> workerUniquePositions = buildMines.stream().map(bm -> bm.workerMinePosition).collect(Collectors.toSet());
            workerUniquePositions.add(firstWorkerAfterMinePosition);

            Map<Position2d, Set<Position2d>> buildHousePositionsLen0 = workerUniquePositions.stream()
                    .collect(Collectors.toMap(Function.identity(),
                            wp -> buildHousePositionsLen0(wp, currentParsedGameState, strategyParams, firstMiner.mineralPosition)));
            Map<Position2d, Set<Position2d>> buildHousePositionsLen2 = workerUniquePositions.stream()
                    .collect(Collectors.toMap(Function.identity(),
                            wp -> buildHousePositionsLen2(wp, currentParsedGameState, strategyParams, firstMiner.mineralPosition)));

            Map<Position2d, Set<Position2d>> blockedByThisWorker = workerUniquePositions.stream()
                    .collect(Collectors.toMap(Function.identity(),
                            wp -> Position2dUtil.housePositionsThatAreBlockedByWorker(wp)
                                    .filter(hp -> isEmptyForHouseIgnoringUnitsAnd1Mineral(hp, firstMiner.mineralPosition, currentParsedGameState))
                                    .collect(Collectors.toSet())));

            Stream<int[]> buildMinesIndices = BruteForceUtil.allCombinatonsOf(buildMines.size(), 4);

            long counter = 0;
            for (Iterator<int[]> iterator = buildMinesIndices.iterator();
                 iterator.hasNext() && counter < StrategyParams.MAX_COMBINATIONS_BF; counter++) {
                int[] ii = iterator.next();


                List<Position2d> positionsAtHouseBuild = Arrays.stream(ii)
                        .mapToObj(i -> buildMines.get(i).workerMinePosition).collect(Collectors.toList());

                positionsAtHouseBuild.add(firstWorkerAfterMinePosition);
                if (positionsAtHouseBuild.size() != 5) {
                    return Optional.empty();//alg error
                }
                if (new HashSet<>(positionsAtHouseBuild).size() != 5) {
                    continue;//conflicts in worker positions, don't try this combination
                }

                Set<Position2d> blockedByThis5Workers = null;

                for (int b1 = 0; b1 < positionsAtHouseBuild.size(); b1++) {
                    for (int b2 = b1 + 1; b2 < positionsAtHouseBuild.size(); b2++) {
                        Position2d w1 = positionsAtHouseBuild.get(b1);
                        Position2d w2 = positionsAtHouseBuild.get(b2);

                        if (Math.abs(w1.x - w2.x) > Position2dUtil.HOUSE_SIZE + 1 || Math.abs(w1.y - w2.y) > Position2dUtil.HOUSE_SIZE + 1) {
                            continue;
                        }

                        //check that both can build
                        Set<Position2d> intersection = twoSetsIntersection(
                                buildHousePositionsLen0.get(w1),
                                buildHousePositionsLen0.get(w2));

                        if (!intersection.isEmpty()) {
                            for (int b3 = 0; b3 < positionsAtHouseBuild.size(); b3++) {
                                Position2d wr3 = positionsAtHouseBuild.get(b3);
                                if (wr3.equals(w1) || wr3.equals(w2)) {
                                    continue;
                                }

                                Set<Position2d> w3CanRepairLen2 = buildHousePositionsLen2.get(wr3);
                                Optional<Position2d> foundHousePosition = twoSetFirstIntersection(intersection, w3CanRepairLen2);

                                if (blockedByThis5Workers == null && foundHousePosition.isPresent()) {
                                    blockedByThis5Workers = positionsAtHouseBuild.stream().flatMap(p ->
                                            blockedByThisWorker.get(p).stream()).collect(Collectors.toSet());
                                }

                                if (foundHousePosition.isPresent() && !blockedByThis5Workers.contains(foundHousePosition.get())
                                        && !strategyParams.firstHouseNonDesiredPositions().contains(foundHousePosition.get())) {
                                    BeforeFirstHouseBuildOrder bo = new BeforeFirstHouseBuildOrder();
                                    bo.firstHouseWhereToBuild = foundHousePosition.get();
                                    bo.firstMineralToMine = firstMiner.mineralPosition;
                                    bo.whereToMoveAfterFirstMineralBeingMined = firstWorkerAfterMinePosition;
                                    bo.repairer3Len2Position = wr3;
                                    bo.workersWhereToBuild =
                                            positionsAtHouseBuild.subList(0, 4)
                                                    .stream().map(wmp -> buildMineMap.get(wmp)).collect(Collectors.toList());

                                    return Optional.of(bo);
                                }
                            }
                        }
                    }

                }
            }
        }
        return Optional.empty();
    }

    //check minerals and this worker
    public static Set<Position2d> buildHousePositionsLen0(Position2d workerPosition, ParsedGameState pgs,
                                                          StrategyParams strategyParams, Position2d mineralFieldToIgnore) {
        return Position2dUtil.housePositionsThatWorkerCanBuildIfNoObstacle(workerPosition)
                .filter(hp -> isEmptyForHouseIgnoringUnitsAnd1Mineral(hp, mineralFieldToIgnore, pgs))
                .collect(Collectors.toSet());
    }

    public static Set<Position2d> buildHousePositionsLen2(Position2d workerPosition, ParsedGameState pgs,
                                                          StrategyParams strategyParams, Position2d mineralFieldToIgnore) {
        return Stream.of(workerPosition.diag1(), workerPosition.diag2(), workerPosition.diag3(), workerPosition.diag4())
                .filter(Position2dUtil::isPositionWithinMapBorder)
                .filter(wp -> isEmptyIgnoringUnitsAnd1Mineral(wp, mineralFieldToIgnore, pgs))
                .flatMap(Position2dUtil::housePositionsThatWorkerCanBuildIfNoObstacle)
                .filter(hp -> isEmptyForHouseIgnoringUnitsAnd1Mineral(hp, mineralFieldToIgnore, pgs))
                .collect(Collectors.toSet());
    }

    static Set<Position2d> twoSetsIntersection(Set<Position2d> set1, Set<Position2d> set2) {
        if (set1.size() < set2.size()) {
            return twoSetsIntersection(set2, set1);
        } else {
            Set<Position2d> intersection = new HashSet<>(set1);
            intersection.retainAll(set2);

            return intersection;
        }
    }

    static Optional<Position2d> twoSetFirstIntersection(Set<Position2d> set1, Set<Position2d> set2) {
        if (set1.size() < set2.size()) {
            return twoSetFirstIntersection(set2, set1);
        } else {
            for (Position2d p : set2) {
                if (set1.contains(p)) {
                    return Optional.of(p);
                }

            }
            return Optional.empty();
        }
    }

    public static boolean isEmptyForHouseIgnoringUnitsAnd1Mineral(Position2d housePosition, Position2d mineralToIgnore, ParsedGameState pgs) {
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

    public static boolean isEmptyIgnoringUnitsAnd1Mineral(Position2d p, Position2d mineralToIgnore, ParsedGameState pgs) {
        return mineralToIgnore.equals(p)
                || pgs.at(p).test(c -> c.isUnit() || c.isEmpty());
    }


    //    should return at least 4
    public List<BeforeFirstHouseBuildOrder.BuildMine> bestBuildMines(
            GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
            StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Set<Position2d> ccOuterEdge = pgs.findMyBuildings(EntityType.BUILDER_BASE).get(0)
                .getBuildingOuterEdgeWithoutCorners();

        int workersRequred = 4;

        Comparator<Position2d> byPathLenNearest = Comparator.comparingInt(p -> pgs.at(p).getNearestMineralField().getPathLenEmptyCellsToThisCell());
        Comparator<Position2d> byPathLenThenxy = byPathLenNearest.thenComparing(p -> p.x + p.y);

        List<Position2d> sorted = ccOuterEdge.stream()
                .sorted(byPathLenThenxy)
                .collect(Collectors.toList());
        if (sorted.size() >= workersRequred) {
            int minLen = pgs.at(sorted.get(0)).getNearestMineralField().getPathLenEmptyCellsToThisCell();

            long bestPositionsSameLenCount = sorted.stream().takeWhile(
                    p -> pgs.at(p).getNearestMineralField().getPathLenEmptyCellsToThisCell() <= minLen + 4) //faster house buuild is more important than this
                    .count();

            List<Position2d> bestPositions = sorted.subList(0, (int) Math.max(workersRequred, bestPositionsSameLenCount));

            List<BuildFirstHouseFinalStrategy.MineralAndMinerPosition> mmps = bestPositions.stream()
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

    public BuildFirstHouseFinalStrategy.MineralAndMinerPosition minerAndMineralClosest(
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
                return new BuildFirstHouseFinalStrategy.MineralAndMinerPosition(miningPos, mineralPosition);
            }
        }

        return null; //algorithm error or non-reacheable
    }

    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        return currentParsedGameState.findMyBuildings(EntityType.HOUSE).size() == 0
                && currentParsedGameState.findMyBuildings(EntityType.MELEE_BASE).size() == 0
                && currentParsedGameState.findMyBuildings(EntityType.RANGED_BASE).size() == 0
                && currentParsedGameState.findMyBuildings(EntityType.BUILDER_BASE).size() == 1
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
