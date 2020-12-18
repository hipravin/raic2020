package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.model.Building;
import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import model.EntityType;

import java.util.Comparator;
import java.util.Map;

import static hipravin.model.Position2d.of;

public class WorkerDefendingTurretsStrategy implements SubStrategy {
    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        return strategyParams.useWorkerDefendingTurrets
                && pgs.getMyRangerBase() != null
                && gameHistoryState.ongoingTurretBuildCommandCount() == 0
                && pgs.getMyRangers().size() > strategyParams.turretsMinRangers;
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        if(pgs.isRound1() || pgs.isRound2()) {
            Position2d enemy1 = of(0, 79);

            Cell closestYWorker = pgs.getMyWorkers().values().stream()
                    .min(Comparator.comparingInt(c -> c.getPosition().lenShiftSum(enemy1)))
                    .orElse(null);

            if (closestYWorker != null && !alreadyHaveTurret(closestYWorker.getPosition(), pgs, strategyParams)) {
                if (haveResources(pgs, gameHistoryState, strategyParams)) {
                    DebugOut.println("Turret requested: " + closestYWorker.getPosition());
                    gameHistoryState.turretRequests.add(closestYWorker.getPosition().shift(2, 0));
                }
            }

            Position2d enemy2 = of(79, 0);


            Cell closestXWorker = pgs.getMyWorkers().values().stream()
                    .min(Comparator.comparingInt(c -> c.getPosition().lenShiftSum(enemy2)))
                    .orElse(null);

            if (closestXWorker != null && !alreadyHaveTurret(closestXWorker.getPosition(), pgs, strategyParams)) {
                if (haveResources(pgs, gameHistoryState, strategyParams)) {
                    DebugOut.println("Turret requested: " + closestXWorker.getPosition());
                    gameHistoryState.turretRequests.add(closestXWorker.getPosition().shift(2, 0));
                }
            }


            return;
        }

        Position2d emenyCenterPos = of(70, 70);

        Cell closestYWorker = pgs.getMyWorkers().values().stream()
                .filter(c -> c.getPosition().getX() < c.getPosition().getY())
                .min(Comparator.comparingInt(c -> c.getPosition().lenShiftSum(emenyCenterPos)))
                .orElse(null);


        if (closestYWorker != null && !alreadyHaveTurret(closestYWorker.getPosition(), pgs, strategyParams)) {
            if (haveResources(pgs, gameHistoryState, strategyParams)) {
                DebugOut.println("Turret requested: " + closestYWorker.getPosition());
                gameHistoryState.turretRequests.add(closestYWorker.getPosition().shift(2, 0));
            }
        }

        Cell closestXWorker = pgs.getMyWorkers().values().stream()
                .filter(c -> c.getPosition().getX() > c.getPosition().getY())
                .min(Comparator.comparingInt(c -> c.getPosition().lenShiftSum(emenyCenterPos)))
                .orElse(null);

        if (closestXWorker != null && !alreadyHaveTurret(closestXWorker.getPosition(), pgs, strategyParams)) {
            if (haveResources(pgs, gameHistoryState, strategyParams)) {
                DebugOut.println("Turret requested: " + closestXWorker.getPosition());
                gameHistoryState.turretRequests.add(closestXWorker.getPosition().shift(0, 2));
            }
        }
    }

    boolean alreadyHaveTurret(Position2d pos, ParsedGameState pgs,
                              StrategyParams strategyParams) {
        Building closestMyTurret = pgs.getBuildingsByEntityId().values().stream()
                .filter(b -> b.getCornerCell().isMyEntity() && b.getCornerCell().getEntity().getEntityType() == EntityType.TURRET)
                .min(Comparator.comparingInt(b -> b.getCornerCell().getPosition().lenShiftSum(pos)))
                .orElse(null);

        return closestMyTurret != null
                && closestMyTurret.getCornerCell().getPosition().lenShiftSum(pos) <= strategyParams.turretsFrequency;

    }

    boolean haveResources(ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState,
                          StrategyParams strategyParams) {
        return pgs.getEstimatedResourceAfterTicks(0) -
                gameHistoryAndSharedState.ongoingTurretBuildCommandCount() * pgs.getTurretCost() > strategyParams.turretMinMinerals;
    }

}
