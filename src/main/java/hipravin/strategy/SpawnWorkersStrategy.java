package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.model.*;
import hipravin.strategy.command.*;
import model.Entity;
import model.EntityType;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static hipravin.model.Position2d.of;
import static hipravin.strategy.StrategyParams.MAX_VAL;

public class SpawnWorkersStrategy implements SubStrategy {
    //to prevent multiple workers being sent to single mineral field
    LinkedHashSet<Position2d> lastMineralPositions = new LinkedHashSet<>();
    boolean workerAlreadySentToCenter = false;

    boolean shouldSpawnMoreWorkers(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                   StrategyParams strategyParams) {
        if (pgs.getEnemyArmy() != null) {
            Position2d vragUvorot = pgs.getDefendingAreaEnemies()
                    .stream()
                    .min(Comparator.comparingInt(e -> of(e.getPosition()).lenShiftSum(pgs.getMyCc().getPosition())))
                    .map(e -> of(e.getPosition()))
                    .orElse(null);


            if (vragUvorot != null
                    && vragUvorot.lenShiftSum(pgs.getMyCc().getPosition()) < strategyParams.dontSpawnWorkersVragUVorotPathLen) {
                return false;
            }
        }

        if (pgs.getMyRangerBase() != null && !pgs.getMyRangerBase().isActive()) {
            if (outOfMoney(pgs, gameHistoryState, strategyParams)
                    || outOfPopulation(pgs, strategyParams)) {
                return false;
            }
        }

        if (pgs.getMyWorkers().size() <= strategyParams.populationOfWorkersToIfExtraResources
                && haveExtraResources(pgs, gameHistoryState, strategyParams)
                && (pgs.getMyRangerBase() == null || !pgs.getMyRangerBase().isActive())) {
            return true;
        }

        if (pgs.getMyWorkers().size() >= strategyParams.populationOfWorkersToBuildBeforeRangers
                && pgs.getMyRangerBase() == null) {
            return false;
        }

        if (pgs.getMyWorkers().size() >= strategyParams.populationOfWorkersToBuildAfterRangers) {
            return false; //hold
        }

        return true; //TODO: research conditions
    }

    boolean detectEconomyIsStuck() {
        return false;
    }


    boolean haveExtraResources(ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState, StrategyParams strategyParams) {
        return pgs.getEstimatedResourceAfterTicks(1) >= gameHistoryAndSharedState.ongoingHouseBuildCommandCount() * pgs.getHouseCost()
                + pgs.getRangCost() + strategyParams.extraMoney
                ;
    }

    boolean outOfMoney(ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState, StrategyParams strategyParams) {
        return pgs.getEstimatedResourceAfterTicks(1) - gameHistoryAndSharedState.ongoingHouseBuildCommandCount() * pgs.getHouseCost()
                < strategyParams.outOfMoney;

    }

    boolean outOfPopulation(ParsedGameState pgs, StrategyParams strategyParams) {
        return pgs.getPopulation().getActiveLimit() - pgs.getPopulation().getPopulationUse() < strategyParams.outOfPopulation;
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

    public void findBestSpawnPos(
            GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
            StrategyParams strategyParams) {
        Set<Position2d> ccOuterEdge = new HashSet<>(pgs.findMyBuildings(EntityType.BUILDER_BASE).get(0)
                .getBuildingOuterEdgeWithoutCorners());

        ccOuterEdge.removeIf(p -> !pgs.at(p).isEmpty());

        if (ccOuterEdge.isEmpty()) {
            return;
        }

        if (ifSentToCenter(ccOuterEdge, gameHistoryState, pgs, strategyParams)) {
            workerAlreadySentToCenter = true;
            return;
        }

        if (!workerAlreadySentToCenter) {
            if (detectRespIsSurroundedByMinerals(gameHistoryState, pgs, strategyParams)) {
                sendWorkerToCenter(ccOuterEdge, gameHistoryState, pgs, strategyParams);

                workerAlreadySentToCenter = true;
            }
        }

//        if (ifSentToFog(ccOuterEdge, gameHistoryState, pgs, strategyParams)) { //don't like how it works
//            return;
//        }

        if ((double) pgs.getMyWorkersAtMapCorner() / (pgs.getMineralsAtMapCorner() + 1.0) > strategyParams.mapCornerMiningRatio) {
            if (ccOuterEdge.stream().anyMatch(p -> !Position2dUtil.isMapMyCornerPosition(p))) {
                ccOuterEdge.removeIf(Position2dUtil::isMapMyCornerPosition);
            }
        }

        Map<Position2d, NearestEntity> nearestMinerals = GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), ccOuterEdge, MAX_VAL, false);
        nearestMinerals.entrySet().removeIf(e ->
                lastMineralPositions.contains(e.getKey()) || !pgs.at(e.getKey()).isMineralEdge());//not spawn to workers to same mf

        //more econditions?
        Optional<NearestEntity> bestMineral = bestMineral(nearestMinerals, strategyParams);

        if (bestMineral.isPresent()) {
            addToLastSpawn(bestMineral.get().getThisCell().getPosition(), strategyParams);
            Position2d spawnPosition = bestMineral.get().getSourceCell().getPosition();

            Command buildWorker = new BuildWorkerCommand(bestMineral.get().getSourceCell().getPosition(), pgs, 1);
            Command moveTowards = new MoveTowardsCommand(pgs, EntityType.BUILDER_UNIT, bestMineral.get().getThisCell().getPosition(),
                    bestMineral.get().getPathLenEmptyCellsToThisCell(), strategyParams.moveTowardsMineralsDistanceTreshold);
            CommandUtil.chainCommands(buildWorker, moveTowards);

            gameHistoryState.addOngoingCommand(buildWorker, false);
        } else {
            //spawn randomly and send to random fog
            Position2d pos = spawnIfNoPathToMinerals(gameHistoryState, pgs, strategyParams).orElse(null);
            if (pos != null) {
                Command buildWorker = new BuildWorkerCommand(pos, pgs, 1);
                gameHistoryState.addOngoingCommand(buildWorker, false);
            }
        }
    }

    int countWorkersOnResp(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                           StrategyParams strategyParams) {
        int count = 0;

        for (int x = 0; x < strategyParams.respSize; x++) {
            for (int y = 0; y < strategyParams.respSize; y++) {
                if (pgs.at(of(x, y)).isMyWorker()) {
                    count++;
                }
            }
        }
        return count;
    }

    boolean detectRespIsSurroundedByMinerals(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                             StrategyParams strategyParams) {
        Set<Position2d> ccouterEdge = new HashSet<>(pgs.getBuildingsByEntityId()
                .get(pgs.getMyCc().getId()).getBuildingOuterEdgeWithoutCorners());

        Map<Position2d, NearestEntity> nes =
                GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), ccouterEdge, StrategyParams.RESP_SURROUNDED_DETECT_RANGE,
                        false, true, Set.of());

        Optional<NearestEntity> farthest = nes.values().stream()
                .max(NearestEntity.comparedByPathLen);

        if (farthest.isPresent()) {
            boolean surrounded = nes.values().stream()
                    .filter(ne -> ne.getPathLenEmptyCellsToThisCell() == farthest.get().getPathLenEmptyCellsToThisCell())
                    .allMatch(ne -> (ne.getThisCell().isMineral() || ne.getThisCell().isMapEdge()));

            if(surrounded) {

                DebugOut.println("Resp is surrounded ");
                return true;
            }
        }
        DebugOut.println("Resp is not surrounded: " + farthest.map(NearestEntity::toString).orElse("no way"));

        return false;


    }

    boolean ifSentToCenter(Set<Position2d> ccOuterEdge, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                           StrategyParams strategyParams) {


        int workerNum = pgs.getMyWorkers().size();

        if (strategyParams.sendToCenter && strategyParams.sendToCenterWorkerNumbers.contains(workerNum)) {
            sendWorkerToCenter(ccOuterEdge, gameHistoryState, pgs, strategyParams);

            return true;
        }
        return false;
    }

    public Position2d sendToCenterSpawnPos(Set<Position2d> ccOuterEdge, Position2d centerPos,//center or barrack
                                           GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                           StrategyParams strategyParams) {

        Set<Position2d> ccOuter = new HashSet<>(ccOuterEdge);

        Map<Position2d, NearestEntity> nes =
                GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), ccOuter, StrategyParams.SEND_TO_CENTER_WS_RANGE, false, false, Set.of());

        NearestEntity closestToBarr = nes.values().stream().min(Comparator.comparingInt(ne -> ne.getThisCell().getPosition().lenShiftSum(centerPos)))
                .orElse(null);

        if(closestToBarr != null) {
            return closestToBarr.getSourceCell().getPosition();
        } else {
            return ccOuterEdge.stream().max(Comparator.comparingInt(p -> p.x + p.y)).orElse(null);
        }

    }

    public void sendWorkerToCenter(Set<Position2d> ccOuterEdge, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                   StrategyParams strategyParams) {
        Position2d centerOrBarrackPosition = fixedToCenterPosition();
        Position2d spawnPos = sendToCenterSpawnPos(ccOuterEdge, centerOrBarrackPosition, gameHistoryState, pgs, strategyParams);

        CommandPredicate barrackStartedToBuild = new CommandPredicate() {
            @Override
            public boolean test(Command command, ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState, StrategyParams strategyParams) {
                Entity rangBase = pgs.getMyBarrack(EntityType.RANGED_BASE);
                if (rangBase != null && !rangBase.isActive() && command instanceof MoveTowardsCommand) {
                    ((MoveTowardsCommand) command).setTargetPosition(of(rangBase.getPosition()).shift(2, 2));
                }

                return rangBase != null && rangBase.isActive();
            }
        };

        BiConsumer<Integer, Integer> onStart = (id, tick) -> {
            DebugOut.println("Sent to center (onstart) " + id + ", " + tick);
            gameHistoryState.sentToBarrackEntityIds.add(id);
            gameHistoryState.sentToBarrackTicks.put(id, tick);
        };
        BiConsumer<Integer, Integer> onDone = (id, tick) -> {
            DebugOut.println("Arrived to center (ondone) " + id + ", " + tick);

            gameHistoryState.arrivedToBarrackTicks.put(id, tick);
        };


        Command buildWorker = new BuildWorkerCommand(spawnPos, pgs, 1);
        SendNewWorkerToPositionCommand sendToCenter = new SendNewWorkerToPositionCommand(spawnPos, centerOrBarrackPosition, barrackStartedToBuild,
                strategyParams.moveTowardsBarracksDistanceTreshold, onDone, onStart);
        if (!gameHistoryState.sentToBarrackEntityIds.isEmpty()) {
            int followId = gameHistoryState.sentToBarrackEntityIds.get(gameHistoryState.sentToBarrackEntityIds.size() - 1);
            sendToCenter.setFollowEntityId(followId);
            DebugOut.println("Following " + followId);
        }


        CommandUtil.chainCommands(buildWorker, sendToCenter);

        gameHistoryState.addOngoingCommand(buildWorker, false);
    }

    public Position2d randomSendToCenterPosition() {
        Position2d basePosition = StrategyParams.DESIRED_BARRACK;
        List<Position2d> outerEdge = new ArrayList<>(Position2dUtil.buildingOuterEdgeWithoutCorners(basePosition, Position2dUtil.RANGED_BASE_SIZE));

        return outerEdge.get(GameHistoryAndSharedState.random.nextInt(outerEdge.size()));
    }

    public Position2d fixedToCenterPosition() {
        return StrategyParams.sendToDesiredBarrackPosition;
    }

    CommandPredicate closeToMineTargetPredicate = new CommandPredicate() {
        @Override
        public boolean test(Command command, ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState, StrategyParams strategyParams) {
            if (!(command instanceof MineExactMineral)) {
                return false;
            }
            MineExactMineral mem = (MineExactMineral) command;
            Optional<Position2d> minerCurrentPos = Optional.ofNullable(pgs.getMyWorkers().get(mem.getMinerId())).map(Cell::getPosition);
            if (minerCurrentPos.isPresent()
                    && minerCurrentPos.get().lenShiftSum(mem.getMineralToMine()) <= strategyParams.switchToAutoMineRange) {
                return true;
            }

            return false;
        }
    };

    Optional<NearestEntity> bestMineral(Map<Position2d, NearestEntity> nearestMinerals, StrategyParams strategyParams) {
        if (nearestMinerals.isEmpty()) {
            return Optional.empty();
        }

        Comparator<NearestEntity> byPathLenNearestNe = Comparator.comparingInt(NearestEntity::getPathLenEmptyCellsToThisCell);

        List<NearestEntity> nearestListSorted = new ArrayList<>(nearestMinerals.values());
        nearestListSorted.sort(byPathLenNearestNe);

        if (nearestListSorted.size() == 1) {
            return Optional.of(nearestListSorted.get(0));
        } else {
            if (strategyParams.ifRandom(strategyParams.bestMineralSpawnProb)) {
                return Optional.of(nearestListSorted.get(0));
            }
            if (strategyParams.ifRandom(strategyParams.worstMineralSpawnProb)) {
                return Optional.of(nearestListSorted.get(nearestListSorted.size() - 1));
            }

            int randIdx = GameHistoryAndSharedState.random.nextInt(nearestListSorted.size());
            return Optional.of(nearestListSorted.get(randIdx));
        }
    }

    Optional<Position2d> spawnIfNoPathToMinerals(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                 StrategyParams strategyParams) {
        Set<Position2d> ccOuterEdge = new HashSet<>(pgs.findMyBuildings(EntityType.BUILDER_BASE).get(0)
                .getBuildingOuterEdgeWithoutCorners());

        ccOuterEdge.removeIf(p -> !pgs.at(p).isEmpty());
        Comparator<Position2d> topRight = Comparator.comparing(p -> p.x + p.y, Comparator.reverseOrder());

        List<Position2d> sorted = ccOuterEdge.stream()
                .sorted(topRight)
                .collect(Collectors.toList());
        if (sorted.isEmpty()) {
            DebugOut.println("Can't find spawn location for worker");
            return Optional.empty();
        } else if (sorted.size() == 1) {
            return Optional.of(sorted.get(0));
        } else {
            int idx = GameHistoryAndSharedState.random.nextInt(2);
            return Optional.of(sorted.get(idx));
        }
    }

    void addToLastSpawn(Position2d lastMineralToMine, StrategyParams strategyParams) {
        while (lastMineralPositions.size() > strategyParams.maxSpawnToMineralsRememberCount) {
            lastMineralPositions.remove(lastMineralPositions.iterator().next());
        }

        lastMineralPositions.add(lastMineralToMine);
    }

    public LinkedHashSet<Position2d> getLastMineralPositions() {
        return lastMineralPositions;
    }
}
