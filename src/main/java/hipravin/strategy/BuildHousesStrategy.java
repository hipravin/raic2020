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

public class BuildHousesStrategy implements SubStrategy {

    boolean shouldBuildHouse(ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState, StrategyParams strategyParams) {
        Entity rangBase = pgs.getMyRangerBase();

        if (pgs.getPopulation().getPotentialLimit() >= strategyParams.populationOfWorkersToBuildBeforeRangers
                && rangBase == null) {
            return false; //hold houses, collect resources for ranger baase
        }

        boolean willHaveResources = willHaveResourcesIn2Ticks(pgs, gameHistoryAndSharedState);//have money - > build house, the fastest strateg

        //TODO: check houses already in progress, other stuff
        boolean populationAheadRequired = populationAheadRequired(pgs, gameHistoryAndSharedState, strategyParams);

        return populationAheadRequired && willHaveResources;
    }

    boolean populationAheadRequired(ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState, StrategyParams strategyParams) {
        if(pgs.getMyBarrack(EntityType.RANGED_BASE) == null) {
            return pgs.getPopulation().getPotentialLimit() <
                    pgs.getPopulation().getPopulationUse() + strategyParams.getHousesAheadPopulationBeforeRangers(pgs.getPopulation().getPopulationUse());
        } else {
            return pgs.getPopulation().getPotentialLimit() <
                    pgs.getPopulation().getPopulationUse() + strategyParams.getHousesAheadPopulationWhenBuildingRangers(pgs.getPopulation().getPopulationUse());
        }
    }

    boolean willHaveResourcesIn2Ticks(ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState) {
        return pgs.getEstimatedResourceAfterTicks(2) >= pgs.getHouseCost() + gameHistoryAndSharedState.ongoingHouseBuildCommandCount() * pgs.getHouseCost()
                ;
    }

    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        if (!FinalGameStartStrategy.gameStartStrategyDone) {
            return false;
        }
        if (pgs.getActiveHouseCount() == 0
                && gameHistoryState.ongoingHouseBuildCommandCount() > 0) {
            return false;
        }


        return true;
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        if (!shouldBuildHouse(pgs, gameHistoryState, strategyParams)) {
            return;
        }

        tryToBuildHouseShortDistance(gameHistoryState, pgs, strategyParams);
    }

    public void tryToBuildHouseShortDistance(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                             StrategyParams strategyParams) {

        int distance = 3;

        //permitted to build without spacing
        boolean pbws = pgs.getActiveHouseCount() <= strategyParams.maxHousesBeforeMandatorySpacing;

        GameStateParser.computeUniqueWorkersNearby(pgs, StrategyParams.HOUSE_WORKERS_NEARBY_MAX_PATH);

        boolean success =
                tryToBuildHouseShortDistance(3, distance, gameHistoryState, pgs, strategyParams, true)
                        || pbws && tryToBuildHouseShortDistance(3, distance, gameHistoryState, pgs, strategyParams, false)
                        || tryToBuildHouseShortDistance(2, distance, gameHistoryState, pgs, strategyParams, true)
                        || pbws && tryToBuildHouseShortDistance(2, distance, gameHistoryState, pgs, strategyParams, false)
                        || tryToBuildHouseShortDistance(2, distance, gameHistoryState, pgs, strategyParams, true)
                        || pbws && tryToBuildHouseShortDistance(2, distance, gameHistoryState, pgs, strategyParams, false)
                        || tryToBuildHouseShortDistance(1, distance + 2, gameHistoryState, pgs, strategyParams, true)
                        || pbws && tryToBuildHouseShortDistance(1, distance + 2, gameHistoryState, pgs, strategyParams, false);
    }

    public boolean tryToBuildHouseShortDistance(int workers, int maxDistanceToWorker, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                StrategyParams strategyParams, boolean withNonDesiredAndSpacingFiltering) {
        DebugOut.println("Try to build house with workers: " + workers + " with spacing: " + withNonDesiredAndSpacingFiltering);

        int size = Position2dUtil.HOUSE_SIZE;

        Map<Position2d, FreeSpace> houseOptions = pgs.myWorkersSquareCellsAsStream().stream()
                .filter(c -> pgs.calculateFreeSpace(c, size).map(FreeSpace::isCompletelyFree).orElse(false))
                .collect(Collectors.toMap(Cell::getPosition, c -> pgs.calculateFreeSpace(c, size).get()));

        houseOptions.entrySet().removeIf(ho ->
                ho.getKey().lenShiftSum(StrategyParams.DESIRED_BARRACK.shift(3,3)) < strategyParams.minHouseDistanceToCenter);//avoid house at center build by barrack builders


        if (withNonDesiredAndSpacingFiltering) {
            houseOptions.entrySet().removeIf(e -> strategyParams.houseNonDesiredPositions().contains(e.getKey()));
            houseOptions.entrySet().removeIf(e -> e.getKey().x + e.getKey().y > strategyParams.leftCornerSpacingDoesntMatterXPlusy
                    && !doesntTouchOtherBuildings(e.getKey(), size, pgs));
            houseOptions.entrySet().removeIf(e -> e.getKey().x + e.getKey().y > strategyParams.leftCornerSpacingDoesntMatterXPlusy
                    && !dousntTouchMinerals(e.getKey(), size, pgs));
        }

        //housePosition -> [uniqueWorkerPosition -> NearestEntity]
        Map<Position2d, List<NearestEntity>> hpUniqueBestWorkers = new HashMap<>();
        Set<Integer> busyWorkers = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();

        houseOptions.forEach((hp, fs) -> {

            List<NearestEntity> workersNearEdge =
                    Position2dUtil.buildingOuterEdgeWithoutCorners(hp, size)
                            .stream().filter(edge -> pgs.at(edge).test(c -> c.isEmpty() || c.isMyWorker()))
                            .flatMap(edge -> workersAtDistanceLesThan(pgs.at(edge).getWorkersNearby(), maxDistanceToWorker).stream())
                            .collect(Collectors.toList()); //non-unique workers nearby

            workersNearEdge.removeIf(ne -> busyWorkers.contains(ne.getSourceCell().getEntityId()));//only not busy workers
            Set<Position2d> uniqueWorkerPositions = workersNearEdge.stream().map(ne -> ne.getSourceCell().getPosition())
                    .collect(Collectors.toSet());

            if (uniqueWorkerPositions.size() >= workers) {
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

                            if (foundCount >= workers) {
                                break;
                            }
                        }
                    }
                    if (foundCount >= workers) {
                        break;
                    }
                }
                //now we have exact workers

                if (foundCount >= workers) {
                    hpUniqueBestWorkers.put(hp, new ArrayList<>(workerRepairPositions.values()));
                }
            }
        });

        Map<Position2d, Integer> sumPathLenMap = new HashMap<>();
        hpUniqueBestWorkers.forEach((hp, nes) -> {
            sumPathLenMap.put(hp, sumPathLen(nes));
        });

        Comparator<Position2d> sumPathLen = Comparator.comparingInt(sumPathLenMap::get);

        List<Position2d> acceptableHousePositions = new ArrayList<>(hpUniqueBestWorkers.keySet());
        acceptableHousePositions.sort(sumPathLen);

        if (!acceptableHousePositions.isEmpty()) {
            Position2d hp = acceptableHousePositions.get(0);
//            Position2d hp = selectBest(acceptableHousePositions, pgs, strategyParams);
            createBuildAndRepairCommands(hp, hpUniqueBestWorkers.get(hp), gameHistoryState, pgs, strategyParams);
            return true;
        }

        return false;
    }

    void createBuildAndRepairCommands(Position2d housePosition, List<NearestEntity> workers, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                      StrategyParams strategyParams) {

        NearestEntity builder = workers.get(0);
        Command moveThenBuild = new MoveSingleCommand(pgs, builder.getSourceCell().getEntityId(),
                builder.getThisCell().getPosition(), MAX_VAL);
        Command bc = new BuildThenRepairCommand(housePosition, EntityType.HOUSE, builder.getSourceCell().getEntityId(), pgs, strategyParams);
        CommandUtil.chainCommands(moveThenBuild, bc);

        gameHistoryState.addOngoingCommand(moveThenBuild, false);
        for (int i = 1; i < workers.size(); i++) {
            NearestEntity repairer = workers.get(i);
            int repairerEntityId = repairer.getSourceCell().getEntityId();

            Command moveThenAutoRepair = new MoveSingleCommand(pgs, repairerEntityId,
                    repairer.getThisCell().getPosition(), MAX_VAL);
            Command arp = new AutoRepairCommand(housePosition, repairerEntityId, pgs, strategyParams);
            CommandUtil.chainCommands(moveThenAutoRepair, arp);

            gameHistoryState.addOngoingCommand(moveThenAutoRepair, false);
        }
    }

    static int sumPathLen(List<NearestEntity> nes) {
        return nes.stream().mapToInt(NearestEntity::getPathLenEmptyCellsToThisCell).sum();
    }


    List<NearestEntity> workersAtDistanceLesThan(Map<Position2d, NearestEntity> nearbyWorkers, int maxDistance) {
        return nearbyWorkers.values().stream().filter(ne -> ne.getPathLenEmptyCellsToThisCell() <= maxDistance)
                .collect(Collectors.toList());
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
}
