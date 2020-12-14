package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.alg.BruteForceUtil;
import hipravin.model.*;
import hipravin.strategy.command.*;
import model.EntityType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hipravin.strategy.StrategyParams.MAX_VAL;


/**
 * According to my perfect calculations we need to build first house with exact 3 workers. And 2 workers at desperate
 */
public class FinalGameStartStrategy implements SubStrategy {
    public  static BeforeFirstHouseBuildOrder buildOrder = null;

    public static boolean gameStartStrategyDone = false;

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        if (pgs.curTick() == 0) {
            buildOrder = tryToFind2Build1DiagRepair(gameHistoryState, pgs, strategyParams, assignedActions).orElse(null);

            if(buildOrder != null) {
                if(DebugOut.enabled) {
                    DebugOut.println(buildOrder.toString());
                }
                createFirstWorkerMineMove(gameHistoryState, pgs);
                createBuildWorkerCommands(gameHistoryState, pgs, strategyParams, assignedActions);
            } else {
                if(DebugOut.enabled) {
                    DebugOut.println("Unable to find start build");
                }
                gameStartStrategyDone = true;
            }
        }

        if(buildOrder != null
            && pgs.getPopulation().getPopulationUse() >= pgs.getPopulation().getActiveLimit()
              && pgs.getEstimatedResourceAfterTicks(2) >= pgs.getHouseCost()) {
            tryToBuildHouseAccordingPlan(gameHistoryState, pgs, strategyParams);

            gameStartStrategyDone = true;
        }
    }

    void tryToBuildHouseAccordingPlan(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                      StrategyParams strategyParams) {

        Position2d firstHouseWhereToBuild = buildOrder.firstHouseWhereToBuild;

        FreeSpace fs = pgs.calculateFreeSpace(pgs.at(firstHouseWhereToBuild), Position2dUtil.HOUSE_SIZE).orElse(null);
        if(fs != null && fs.isCompletelyFree()) {
            Set<Position2d> houseEdge = Position2dUtil.buildingOuterEdgeWithoutCorners(firstHouseWhereToBuild, Position2dUtil.HOUSE_SIZE);
            List<Position2d> len0Workers = houseEdge.stream().filter(he -> pgs.at(he).isMyWorker()).collect(Collectors.toList());

            if(len0Workers.size() >= 3) {
                build3Workers(firstHouseWhereToBuild, len0Workers.subList(0, 3), gameHistoryState, pgs, strategyParams);
            } else if(len0Workers.size() < 2) {
                //can't follow algorithm
                return;
            }
            //find repairer len 2
            Optional<List<Position2d>> repairer2 = canRepair2(firstHouseWhereToBuild, len0Workers, gameHistoryState, pgs, strategyParams);
            if(repairer2.isPresent()) {
                build2Workers1Repairer2(firstHouseWhereToBuild, len0Workers,
                        repairer2.get().get(0), repairer2.get().get(1),
                        gameHistoryState, pgs, strategyParams);
            }
        }
    }

    void build3Workers(Position2d firstHouseWhereToBuild, List<Position2d> workers, GameHistoryAndSharedState gameHistoryState,
                       ParsedGameState pgs, StrategyParams strategyParams) {
        Command brp1 = new BuildThenRepairCommand(firstHouseWhereToBuild, EntityType.HOUSE, pgs.at(workers.get(0)).getEntityId(), pgs, strategyParams);
        Command brp2 = new AutoRepairCommand(firstHouseWhereToBuild, pgs.at(workers.get(1)).getEntityId(), pgs, strategyParams);
        Command brp3 = new AutoRepairCommand(firstHouseWhereToBuild, pgs.at(workers.get(2)).getEntityId(), pgs, strategyParams);

        gameHistoryState.addOngoingCommand(brp1, true);
        gameHistoryState.addOngoingCommand(brp2, true);
        gameHistoryState.addOngoingCommand(brp3, true);
    }

    void build2Workers1Repairer2(Position2d firstHouseWhereToBuild, List<Position2d> len0Workers,
                                 Position2d len2Worker, Position2d len2WorkerMoveTo,
                                 GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {

        Command brp1 = new BuildThenRepairCommand(firstHouseWhereToBuild, EntityType.HOUSE, pgs.at(len0Workers.get(0)).getEntityId(), pgs, strategyParams);
        Command brp2 = new AutoRepairCommand(firstHouseWhereToBuild, pgs.at(len0Workers.get(1)).getEntityId(), pgs, strategyParams);
        Command moveThenAutoRepair = new MoveSingleCommand(pgs,  pgs.at(len2Worker).getEntityId(), len2WorkerMoveTo, MAX_VAL);
        Command arp3 = new AutoRepairCommand(firstHouseWhereToBuild, pgs.at(len2Worker).getEntityId(), pgs, strategyParams);
        //sometimes after repair worker just stays... try to send him back
//        Command moveBack = new MoveSingleCommand(pgs,  pgs.at(len2Worker).getEntityId(), len2Worker, MAX_VAL);

        CommandUtil.chainCommands(moveThenAutoRepair, arp3/**, moveBack**/);

        gameHistoryState.addOngoingCommand(brp1, true);
        gameHistoryState.addOngoingCommand(brp2, true);
        gameHistoryState.addOngoingCommand(moveThenAutoRepair, true);
    }

    Optional<List<Position2d>> canRepair2(Position2d firstHouseWhereToBuild, List<Position2d> builders,
                                    GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                    StrategyParams strategyParams) {
        Set<Position2d> houseEdgeWithoutBuilders = Position2dUtil.buildingOuterEdgeWithoutCorners(firstHouseWhereToBuild, Position2dUtil.HOUSE_SIZE);
        houseEdgeWithoutBuilders.removeIf(rp -> builders.contains(rp));

        for (Cell workerCell : pgs.getMyWorkers().values()) {
            Position2d workerPosition = workerCell.getPosition();
            if(builders.contains(workerPosition)) {
                continue;
            }

            Map<Position2d, NearestEntity> nearest = GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), Set.of(workerPosition), 2, true);

            for (Position2d outerEdgePosition : houseEdgeWithoutBuilders) {

                if(nearest.containsKey(outerEdgePosition)) {
                    return Optional.of(List.of(workerPosition, outerEdgePosition));
                }
            }
        }
        return Optional.empty();
    }

    void createFirstWorkerMineMove(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs) {
        MineralAndMinerPosition firstMiner = buildOrder.firstMiner;
        Position2d whereToMoveAfterMine = buildOrder.whereToMoveAfterFirstMineralBeingMined;
        int firstWorkerEntityId = pgs.getMyWorkers().keySet().iterator().next();//preconditions check that size ==1


        Command moveTo = new MoveSingleCommand(pgs, firstWorkerEntityId, firstMiner.minerPosition, MAX_VAL);
        Command mineExact = new MineExactMineral(firstWorkerEntityId, firstMiner.mineralPosition, 1);
        Command moveTo2 = new MoveSingleCommand(pgs, firstWorkerEntityId, whereToMoveAfterMine, MAX_VAL);
        //automine will be automated

        CommandUtil.chainCommands(moveTo, mineExact, moveTo2);

        gameHistoryState.addOngoingCommand(moveTo, false);
    }

    void createBuildWorkerCommands(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                   StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        if(buildOrder == null) {
            return;
        }

        Command prev = null;

        for (BeforeFirstHouseBuildOrder.BuildMine buildMine : buildOrder.workersWhereToBuild) {
            Command current = buildMineCommand(buildMine, pgs);

            if(prev == null) {
                gameHistoryState.addOngoingCommand(current, false);
            } else {
                CommandUtil.addNextCommands(prev, List.of(current));
            }

            prev = current;
        }
    }

    Command buildMineCommand(BeforeFirstHouseBuildOrder.BuildMine buildMine, ParsedGameState pgs) {

        Command buildWorker = new BuildWorkerCommand(buildMine.workerBuildPosition, pgs, MAX_VAL);
        Command mineFromExact = new MineFromExactPositionCommand(buildMine.workerBuildPosition, buildMine.workerMinePosition, buildMine.mineralToMinePosition);

        CommandUtil.chainCommands(buildWorker, mineFromExact);
        return buildWorker;
    }

    //loot at this! what the heck is going on? can this method be 1000 lines long?
    //but what can I do is time is limited and rules are changing so fast
    public Optional<BeforeFirstHouseBuildOrder> tryToFind2Build1DiagRepair(
            GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
            StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Position2d firstWorker = currentParsedGameState.getMyWorkers().values().iterator().next().getPosition();

        FinalGameStartStrategy.MineralAndMinerPosition firstMiner = minerAndMineralClosest(firstWorker, gameHistoryState, currentParsedGameState, strategyParams, assignedActions);

        if(firstMiner == null) {
            return Optional.empty();
        }

        List<Position2d> firstMinerPositionsAfterFirstMined = Position2dUtil.upRightLeftDown(firstMiner.minerPosition)
                .filter(p -> currentParsedGameState.at(p)
                        .test(c -> (c.getPosition().equals(firstMiner.mineralPosition) || c.isEmpty())
                                && c.getLen1MineralsCount() > 0))
                .collect(Collectors.toList());

        if(firstMinerPositionsAfterFirstMined.isEmpty()) {
            firstMinerPositionsAfterFirstMined = Position2dUtil.upRightLeftDown(firstMiner.mineralPosition)//
                    .filter(p -> currentParsedGameState.at(p)
                            .test(c -> (c.getPosition().equals(firstMiner.mineralPosition) || c.isEmpty())
                                    && c.getLen1MineralsCount() > 0))
                    .collect(Collectors.toList());
        }


        for (Position2d firstWorkerAfterMinePosition : firstMinerPositionsAfterFirstMined) {
            List<BeforeFirstHouseBuildOrder.BuildMine> buildMines =
                    bestBuildMines(firstMiner.mineralPosition, gameHistoryState, currentParsedGameState, strategyParams, assignedActions);

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

            if(buildMines.isEmpty()) {
                return Optional.empty();
            }

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
                                        && !strategyParams.firstHouseNonDesiredPositions().contains(foundHousePosition.get())
                                        && mineralMiningsAreDistinct(ii, buildMines)) {
                                    BeforeFirstHouseBuildOrder bo = new BeforeFirstHouseBuildOrder();
                                    bo.firstHouseWhereToBuild = foundHousePosition.get();
                                    bo.firstMineralToMine = firstMiner.mineralPosition;
                                    bo.whereToMoveAfterFirstMineralBeingMined = firstWorkerAfterMinePosition;
                                    bo.repairer3Len2Position = wr3;
                                    bo.workersWhereToBuild =
                                            positionsAtHouseBuild.subList(0, 4)
                                                    .stream().map(wmp -> buildMineMap.get(wmp)).collect(Collectors.toList());

                                    bo.firstMiner = firstMiner;

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

    public boolean mineralMiningsAreDistinct(int[] ii, List<BeforeFirstHouseBuildOrder.BuildMine> buildMines) {
        Set<Position2d> minePosition =
                Arrays.stream(ii)
                        .mapToObj(i -> buildMines.get(i).mineralToMinePosition).collect(Collectors.toSet());

        return minePosition.size() == ii.length;

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
            Position2d firstWorkerMineralToMinePosition,
            GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
            StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Set<Position2d> ccOuterEdge = new HashSet<>(pgs.findMyBuildings(EntityType.BUILDER_BASE).get(0)
                .getBuildingOuterEdgeWithoutCorners());

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

            List<FinalGameStartStrategy.MineralAndMinerPosition> mmps = bestPositions.stream()
                    .map(p -> minerAndMineralClosest(p, gameHistoryState, pgs, strategyParams, assignedActions))
                    .collect(Collectors.toList());

            List<BeforeFirstHouseBuildOrder.BuildMine> bestBuildMines = new ArrayList<>();

            for (int i = 0; i < bestPositions.size(); i++) {
                if(!mmps.get(i).mineralPosition.equals(firstWorkerMineralToMinePosition)) { //if mine same mineral tey both move and result is unpredicted
                    bestBuildMines.add(new BeforeFirstHouseBuildOrder.BuildMine(bestPositions.get(i),
                            mmps.get(i).minerPosition, mmps.get(i).mineralPosition));
                }

            }
            return bestBuildMines;
        }


        return Collections.emptyList();


    }

    public FinalGameStartStrategy.MineralAndMinerPosition minerAndMineralClosest(
            Position2d workerPosition,
            GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
            StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        if(pgs.at(workerPosition).getNearestMineralField() == null) {
            return null;
        }

        int toNearestMineral = pgs.at(workerPosition).getNearestMineralField().getPathLenEmptyCellsToThisCell();
        Map<Position2d, NearestEntity> toMinerals =
                GameStateParserDjkstra.shortWideSearch(pgs, Collections.emptySet(), Set.of(workerPosition), toNearestMineral, true);

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
                return new FinalGameStartStrategy.MineralAndMinerPosition(miningPos, mineralPosition);
            }
        }

        return null; //algorithm error or non-reacheable
    }

    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        if(currentParsedGameState.isRound1()) {
            return false;
        }

        if(gameStartStrategyDone) {
            return false;
        }

        boolean applicable = currentParsedGameState.findMyBuildings(EntityType.HOUSE).size() == 0
                && currentParsedGameState.findMyBuildings(EntityType.MELEE_BASE).size() == 0
                && currentParsedGameState.findMyBuildings(EntityType.RANGED_BASE).size() == 0
                && currentParsedGameState.findMyBuildings(EntityType.BUILDER_BASE).size() == 1
                && currentParsedGameState.getPlayerView().getCurrentTick() < 200; //just in case

        if(!applicable) {
            gameStartStrategyDone = true;
        }

        return applicable;
    }

    public static class MineralAndMinerPosition {
        public final Position2d minerPosition;
        public final Position2d mineralPosition;

        public MineralAndMinerPosition(Position2d minerPosition, Position2d mineralPosition) {
            this.minerPosition = minerPosition;
            this.mineralPosition = mineralPosition;
        }

        @Override
        public String toString() {
            return "MineralAndMinerPosition{" +
                    "minerPosition=" + minerPosition +
                    ", mineralPosition=" + mineralPosition +
                    '}';
        }
    }

}
