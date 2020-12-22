package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.model.*;
import hipravin.strategy.command.*;
import model.Entity;
import model.EntityType;

import java.util.*;
import java.util.stream.Collectors;

import static hipravin.model.Position2dUtil.buildingsHaveSpaceInBetween;
import static hipravin.strategy.StrategyParams.MAX_VAL;

public class BuildBarrackStrategy implements SubStrategy {
    public EntityType selectBarrackType() {
        return EntityType.RANGED_BASE;
    }

    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityType barrackType = selectBarrackType();

        Entity barrack = pgs.getMyBarrack(barrackType);

        gameHistoryState.ongoingBarrackBuildCommandCount();

        return barrack == null
                && pgs.getEstimatedResourceAfterTicks(strategyParams.barrackAheadBuildResourceTick) >= pgs.getBarrackCost(barrackType)
                && gameHistoryState.ongoingBarrackBuildCommandCount() < 1;
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        int distance = 15;

        GameStateParser.computeUniqueWorkersNearbyCenterMore(pgs, StrategyParams.BARRACK_WORKERS_NEARBY_MAX_PATH,
                StrategyParams.BARRACK_WORKERS_NEARBY_MAX_PATH_CENTER);

        int ct = pgs.curTick();
        boolean desperate = ct > strategyParams.desperateRangeBasePlacementTick;
        boolean agony = ct > strategyParams.agonyRangeBasePlacementTick;


        boolean success =
                        !agony && tryToBuildBarrackShortDistance(3, distance, 10, gameHistoryState, pgs, strategyParams, true)
                        || !agony && tryToBuildBarrackShortDistance(3, distance, 10, gameHistoryState, pgs, strategyParams, false)
                        || !agony && tryToBuildBarrackShortDistance(3, distance, 15, gameHistoryState, pgs, strategyParams, true)
                        || !agony && tryToBuildBarrackShortDistance(3, distance, 15, gameHistoryState, pgs, strategyParams, false)
                        || tryToBuildBarrackShortDistance(5, distance, 20, gameHistoryState, pgs, strategyParams, true)
                        || tryToBuildBarrackShortDistance(5, distance, 20, gameHistoryState, pgs, strategyParams, false)
                        || desperate && tryToBuildBarrackShortDistance(5, distance, 30, gameHistoryState, pgs, strategyParams, true)
                        || desperate && tryToBuildBarrackShortDistance(5, distance, 30, gameHistoryState, pgs, strategyParams, false)
                        || agony && tryToBuildBarrackShortDistance(5, distance, 100, gameHistoryState, pgs, strategyParams, true)
                        || agony && tryToBuildBarrackShortDistance(5, distance, 100, gameHistoryState, pgs, strategyParams, false)
//                        || tryToBuildBarrackShortDistance(5, distance, 40, gameHistoryState, pgs, strategyParams, true)
//                        || tryToBuildBarrackShortDistance(5, distance, 40, gameHistoryState, pgs, strategyParams, false)
                ;

    }

    public boolean barrackPositionUnderAttack(Position2d barrackPosition, ParsedGameState pgs) {
        Set<Position2d> outerEdge = Position2dUtil.buildingOuterEdgeWithoutCorners(barrackPosition, Position2dUtil.RANGED_BASE_SIZE);

        for (Position2d edge : outerEdge) {
            if (pgs.at(edge).getAttackerCount(Position2dUtil.RANGER_RANGE) > 0) {
                return true;
            }
        }

        return false;
    }

    public boolean tryToBuildBarrackShortDistance(int minWorkersNear, int maxDistanceToWorker, int maxDistanceToDesiredBarrack,
                                                  GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                  StrategyParams strategyParams, boolean withNonDesiredAndSpacingFiltering) {
        DebugOut.println("Try to build barrack with workers: " + minWorkersNear + " with spacing: " + withNonDesiredAndSpacingFiltering);

        int size = Position2dUtil.RANGED_BASE_SIZE;

        Map<Position2d, FreeSpace> barrackOptions = pgs.myWorkersSquareCellsAsStream().stream()
                .filter(c -> pgs.calculateFreeSpace(c, size).map(FreeSpace::isCompletelyFree).orElse(false))
                .collect(Collectors.toMap(Cell::getPosition, c -> pgs.calculateFreeSpace(c, size).get()));

        barrackOptions.entrySet().removeIf(bo -> StrategyParams.DESIRED_BARRACK.lenShiftSum(bo.getKey()) > maxDistanceToDesiredBarrack);

        if (!pgs.getEnemyArmy().isEmpty()) {
            barrackOptions.entrySet().removeIf(boe -> barrackPositionUnderAttack(boe.getKey(), pgs));
        }

        if (withNonDesiredAndSpacingFiltering) {
            barrackOptions.entrySet().removeIf(e -> !doesntTouchOtherBuildings(e.getKey(), size, pgs));
            barrackOptions.entrySet().removeIf(e -> !dousntTouchMinerals(e.getKey(), size, pgs));
        }

        //barrackOptions -> [uniqueWorkerPosition -> NearestEntity]
        Map<Position2d, List<NearestEntity>> bpUniqueBestWorkers = new HashMap<>();
//        Set<Integer> busyWorkers = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();
        Set<Integer> busyWorkers = Set.of(); //build barracks is top ptiority? maybe build turrets is only more valued


        barrackOptions.forEach((bp, fs) -> {

            List<NearestEntity> workersNearEdge =
                    Position2dUtil.buildingOuterEdgeWithoutCorners(bp, size)
                            .stream().filter(edge -> pgs.at(edge).test(c -> c.isEmpty() || c.isMyWorker()))
                            .flatMap(edge -> workersAtDistanceLesThan(pgs.at(edge).getWorkersNearby(), maxDistanceToWorker).stream())
                            .collect(Collectors.toList()); //non-unique workers nearby

            workersNearEdge.removeIf(ne -> busyWorkers.contains(ne.getSourceCell().getEntityId()));//only not busy workers
            Set<Position2d> uniqueWorkerPositions = workersNearEdge.stream().map(ne -> ne.getSourceCell().getPosition())
                    .collect(Collectors.toSet());

            if (uniqueWorkerPositions.size() >= minWorkersNear) {
                Set<Position2d> workerUsed = new HashSet<>();
                Set<Position2d> edgePositionsUsed = new HashSet<>();
                Map<Position2d, NearestEntity> workerRepairPositions = new HashMap<>();

                int foundCount = 0;
                for (int distance = 0; distance <= maxDistanceToWorker; distance++) {
                    for (NearestEntity workerNe : workersNearEdge) {
                        if (workerNe.getPathLenEmptyCellsToThisCell() == distance
                                && !workerUsed.contains(workerNe.getSourceCell().getPosition())
                                && !edgePositionsUsed.contains(workerNe.getThisCell().getPosition())) {
                            workerUsed.add(workerNe.getSourceCell().getPosition());
                            edgePositionsUsed.add(workerNe.getThisCell().getPosition());

                            workerRepairPositions.put(workerNe.getThisCell().getPosition(), workerNe);

                            foundCount++;

                            if (foundCount >= minWorkersNear) {
                                break;
                            }
                        }
                    }
                    if (foundCount >= minWorkersNear) {
                        break;
                    }
                }
                //now we have exact workers

                if (foundCount >= minWorkersNear) {
                    bpUniqueBestWorkers.put(bp, new ArrayList<>(workerRepairPositions.values()));
                }
            }
        });


        //distance to map center
        Comparator<Position2d> toCenter = Comparator.comparingInt(p -> (int) StrategyParams.DESIRED_BARRACK.lenShiftSum(p));


        List<Position2d> acceptableBarrackPositions = new ArrayList<>(bpUniqueBestWorkers.keySet());
        acceptableBarrackPositions.sort(toCenter);

        if (!acceptableBarrackPositions.isEmpty() && acceptableBarrackPositions.get(0).lenShiftSum(StrategyParams.DESIRED_BARRACK) < maxDistanceToDesiredBarrack) {
            Position2d bp = acceptableBarrackPositions.get(0);
            createBuildAndRepairCommands(bp, bpUniqueBestWorkers.get(bp), gameHistoryState, pgs, strategyParams);
            return true;
        }

        return false;
    }

    void createBuildAndRepairCommands(Position2d barrackOption, List<NearestEntity> workers,
                                      GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                      StrategyParams strategyParams) {

        NearestEntity builder = workers.get(0);
        Command moveThenBuild = new MoveSingleCommand(pgs, builder.getSourceCell().getEntityId(),
                builder.getThisCell().getPosition(), MAX_VAL);
        Command bc = new BuildThenRepairCommand(barrackOption, selectBarrackType(), builder.getSourceCell().getEntityId(), pgs, strategyParams,
                strategyParams.buildBarrackMaxWaitTicks);
        CommandUtil.chainCommands(moveThenBuild, bc);

        gameHistoryState.addOngoingCommand(moveThenBuild, true);
        for (int i = 1; i < workers.size(); i++) {
            NearestEntity repairer = workers.get(i);
            int repairerEntityId = repairer.getSourceCell().getEntityId();

            Command moveThenAutoRepair = new MoveSingleCommand(pgs, repairerEntityId,
                    repairer.getThisCell().getPosition(), MAX_VAL);
            Command arp = new AutoRepairCommand(barrackOption, repairerEntityId, pgs, strategyParams);
            CommandUtil.chainCommands(moveThenAutoRepair, arp);

            gameHistoryState.addOngoingCommand(moveThenAutoRepair, true);
        }
    }

    static boolean doesntTouchOtherBuildings(Position2d corner, int size, ParsedGameState pgs) {
        return pgs.getAllMyBuildings()
                .stream().allMatch(b ->
                        buildingsHaveSpaceInBetween(corner, size,
                                b.getCornerCell().getPosition(), b.getCornerCell().getBuildingSize()));
    }

    static boolean dousntTouchMinerals(Position2d corner, int size, ParsedGameState pgs) {

        Set<Position2d> outerEdge = Position2dUtil.buildingOuterEdgeWithCorners(corner, size);
        return outerEdge.stream().noneMatch(c -> pgs.at(c).isMineral());
    }

    List<NearestEntity> workersAtDistanceLesThan(Map<Position2d, NearestEntity> nearbyWorkers, int maxDistance) {
        return nearbyWorkers.values().stream().filter(ne -> ne.getPathLenEmptyCellsToThisCell() <= maxDistance)
                .collect(Collectors.toList());
    }
}
