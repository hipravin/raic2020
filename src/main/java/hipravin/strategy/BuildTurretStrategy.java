package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.model.*;
import hipravin.strategy.command.*;
import model.Entity;
import model.EntityType;

import java.util.*;
import java.util.stream.Collectors;

import static hipravin.model.Position2d.of;
import static hipravin.model.Position2dUtil.buildingsHaveSpaceInBetween;
import static hipravin.strategy.StrategyParams.MAX_VAL;

public class BuildTurretStrategy implements SubStrategy {

    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        return true;
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        if(gameHistoryState.turretRequests.isEmpty()) {
            return;
        }

        int distance = 10;
        for (Position2d turretAttackingRequest : gameHistoryState.getTurretRequests()) {
            GameStateParser.computeUniqueWorkersNearbyInLimitedArea(pgs, StrategyParams.TURRET_WORKERS_NEARBY_MAX_PATH, turretAttackingRequest);
            boolean success =
                    tryToBuildTurretShortDistance(turretAttackingRequest, 8, distance, gameHistoryState, pgs, strategyParams, true)
                     || tryToBuildTurretShortDistance(turretAttackingRequest, 8, distance, gameHistoryState, pgs, strategyParams, false)
                     || tryToBuildTurretShortDistance(turretAttackingRequest, 4, distance, gameHistoryState, pgs, strategyParams, true)
                     || tryToBuildTurretShortDistance(turretAttackingRequest, 4, distance, gameHistoryState, pgs, strategyParams, false)
                     || tryToBuildTurretShortDistance(turretAttackingRequest, 1, distance, gameHistoryState, pgs, strategyParams, true)
                     || tryToBuildTurretShortDistance(turretAttackingRequest, 1, distance, gameHistoryState, pgs, strategyParams, false);
        }
    }

    public boolean tryToBuildTurretShortDistance(Position2d attackPosition, int minWorkersNear, int maxDistanceToWorker,
                                                  GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                  StrategyParams strategyParams, boolean withNonDesiredAndSpacingFiltering) {
        DebugOut.println("Try to build turret with workers: " + minWorkersNear + " with spacing: " + withNonDesiredAndSpacingFiltering);

        int size = Position2dUtil.TURRET_SIZE;

        Map<Position2d, FreeSpace> turretOptions = pgs.myWorkersSquareCellsAsStream().stream()
                .filter(c -> Position2dUtil.turretCanAttackPosition(c.getPosition(), attackPosition))
                .filter(c -> pgs.calculateFreeSpace(c, size).map(FreeSpace::isCompletelyFree).orElse(false))
                .collect(Collectors.toMap(Cell::getPosition, c -> pgs.calculateFreeSpace(c, size).get()));

        if (withNonDesiredAndSpacingFiltering) {
            turretOptions.entrySet().removeIf(e -> !doesntTouchOtherBuildings(e.getKey(), size, pgs));
            turretOptions.entrySet().removeIf(e -> !dousntTouchMinerals(e.getKey(), size, pgs));
        }

        //turretOptions -> [uniqueWorkerPosition -> NearestEntity]
        Map<Position2d, List<NearestEntity>> tpUniqueBestWorkers = new HashMap<>();
        Set<Integer> busyWorkers = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();
//        Set<Integer> busyWorkers = Set.of(); //build turret is top ptiority? maybe build better world is more


        turretOptions.forEach((bp, fs) -> {

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
                    tpUniqueBestWorkers.put(bp, new ArrayList<>(workerRepairPositions.values()));
                }
            }
        });


        //distance to map center
        Comparator<Position2d> toEnemy = Comparator.comparingInt(p -> (int) Position2dUtil.ENEMY_CORNER.lenShiftSum(p));


        List<Position2d> acceptableTurretPositions = new ArrayList<>(tpUniqueBestWorkers.keySet());
        acceptableTurretPositions.sort(toEnemy);

        if (!acceptableTurretPositions.isEmpty()) {
            Position2d bp = acceptableTurretPositions.get(0);
            createBuildAndRepairCommands(bp, tpUniqueBestWorkers.get(bp), gameHistoryState, pgs, strategyParams);
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
        Command bc = new BuildThenRepairCommand(barrackOption, EntityType.TURRET, builder.getSourceCell().getEntityId(), pgs, strategyParams,
                strategyParams.buildTurretMaxWaitTicks);
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
        return pgs.findAllMyBuildings()
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
