package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.strategy.command.MoveTowardsCommand;

import java.util.*;
import java.util.stream.Collectors;

import static hipravin.model.Position2d.of;
import static hipravin.model.Position2dUtil.ENEMY_CORNER;

public class WorkerScoutStrategy implements SubStrategy {


    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                          StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        if (gameHistoryState.getMyRangBaseCompletedTick() == null) {
            return false;
        }

        return pgs.curTick() > gameHistoryState.getMyRangBaseCompletedTick() + strategyParams.workerScoutStartTickShiftAfterRangComplete
                && pgs.curTick() % strategyParams.workerScoutFrequency == 0
                && !pgs.getMyWorkers().isEmpty();
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        //fog edge closer to opponend but in def area

        sendOneWorkerTowardsFog(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }

    void sendOneWorkerTowardsFog(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                 StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Optional<Position2d> fog = findFogEdgeToScout(gameHistoryState, pgs, strategyParams);
        Optional<Position2d> worker = fog.flatMap(f -> findClosestWorkerForwardScout(f ,gameHistoryState, pgs, strategyParams));

        if(fog.isPresent() && worker.isPresent()) {

            DebugOut.println("Selected scout: " + worker.get() + " sent to " + fog.get());
            int workerId = pgs.at(worker.get()).getEntityId();

            MoveTowardsCommand moveToFog = new MoveTowardsCommand(pgs, workerId, fog.get(),
                    fog.get().lenShiftSum(worker.get()) + 3, 3);

            gameHistoryState.addOngoingCommand(moveToFog, false);
        }
    }

    Optional<Position2d> findFogEdgeToScout(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                            StrategyParams strategyParams) {
        Set<Position2d> fogEdges = new HashSet<>(pgs.getFogEdgePositionsSet());
        Position2d defAreaPosition = pgs.defAreaPosition().shift( -3, -3);

        Position2d barrackPosition = Optional.ofNullable(pgs.getMyRangerBase()).map(e -> of(e.getPosition())).orElse(ENEMY_CORNER);

        fogEdges.removeIf(fe -> fe.x > defAreaPosition.x && fe.y >= defAreaPosition.y);
        fogEdges.removeIf(fe -> fe.x > defAreaPosition.x && fe.y >= defAreaPosition.y);
        fogEdges.removeIf(fe -> fe.lenShiftSum(barrackPosition) < strategyParams.workerScoutToBarrackCloseMinRange);
        fogEdges.removeIf(fe -> fe.x < 10 || fe.y < 10);
        fogEdges.removeIf(fe -> pgs.at(fe).getAttackerCount(8) > 0
                ||  pgs.at(fe).getAttackerCount(7) > 0
                ||  pgs.at(fe).getAttackerCount(6) > 0
                ||  pgs.at(fe).getAttackerCount(5) > 0
        );

//        Optional<Position2d> closestToEnemy = fogEdges.stream().min(Comparator.comparingInt(fe -> ENEMY_CORNER.lenShiftSum(fe)));
//        Optional<Position2d> closestToMe = fogEdges.stream().min(Comparator.comparingInt(fe -> MY_CORNER.lenShiftSum(fe)));

        List<Position2d> fogEdgesList = new ArrayList<>(fogEdges);

        if(fogEdges.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(fogEdgesList.get(GameHistoryAndSharedState.random.nextInt(fogEdges.size())));
    }

    Optional<Position2d> findClosestWorkerForwardScout(Position2d fogEdge, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                       StrategyParams strategyParams) {

        Set<Integer> busyEntities = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();

        List<Position2d> notBusyWorkers = pgs.getMyWorkers()
                .values().stream().map(Cell::getEntity)
                .filter(e -> !busyEntities.contains(e.getId()))
                .map(e -> of(e.getPosition()))
                .collect(Collectors.toList());

        return notBusyWorkers.stream()
                .filter(w -> w.lenShiftSum(ENEMY_CORNER) > fogEdge.lenShiftSum(ENEMY_CORNER)) // scout only forward
                .filter(w -> w.lenShiftSum(fogEdge) < strategyParams.workerScoutMaxRange)
                .min(Comparator.comparingInt(w -> w.lenShiftSum(fogEdge)));
    }
}
