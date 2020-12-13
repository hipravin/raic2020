package hipravin.strategy.command;

import hipravin.DebugOut;
import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.*;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static hipravin.model.Position2d.of;
import static hipravin.strategy.StrategyParams.MAX_VAL;

public class RangerAttackHoldRetreatMicroCommand extends Command {
    Integer rangerEntityId;
    Position2d attackPosition;
    final Position2d retreatPosition;
    boolean workerHunter;

    public RangerAttackHoldRetreatMicroCommand(Integer rangerEntityId, Position2d attackPosition, Position2d retreatPosition) {
        this(rangerEntityId, attackPosition, retreatPosition, false);
    }

    public RangerAttackHoldRetreatMicroCommand(Integer rangerEntityId, Position2d attackPosition, Position2d retreatPosition, boolean workerHunter) {
        super(MAX_VAL, new HashSet<>());
        if (rangerEntityId != null) {
            this.getRelatedEntityIds().add(rangerEntityId);
        }
        this.rangerEntityId = rangerEntityId;
        this.attackPosition = attackPosition;
        this.retreatPosition = retreatPosition;
        this.workerHunter = workerHunter;
    }


    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if (rangerEntityId == null) {
            rangerEntityId = pgs.getMyUnitSpawned(EntityType.RANGED_UNIT).orElse(null);
            this.getRelatedEntityIds().add(rangerEntityId);
            if (rangerEntityId == null) {
                return false;
            }
        }

        return pgs.getEntityIdToCell().containsKey(rangerEntityId);
    }

    public int countEnemyRangersNearby(Position2d rangerPosition, int range,
                                       GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger counter = new AtomicInteger(0);
        Position2dUtil.iterAllPositionsInRangeExclusive(rangerPosition, range, p -> {
            if (pgs.at(p).isEnemyRanger()) {
                counter.incrementAndGet();
            }
        });

        return counter.get();
    }

    public int countMyRangersNearby(Position2d rangerPosition, int range,
                                    GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger counter = new AtomicInteger(0);
        Position2dUtil.iterAllPositionsInRangeExclusive(rangerPosition, range, p -> {
            if (pgs.at(p).isMyRanger()) {
                counter.incrementAndGet();
            }
        });

        return counter.get();
    }

    public int countRangersCloserToattackPoint(Position2d rangerPosition, int range,
                                               GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger counter = new AtomicInteger(0);
        Position2dUtil.iterAllPositionsInRangeExclusive(rangerPosition, range, p -> {
            if (pgs.at(p).isMyRanger() && rangerPosition.lenShiftSum(attackPosition) > p.lenShiftSum(attackPosition)) {
                counter.incrementAndGet();
            }
        });

        return counter.get();
    }


    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if (rangerEntityId == null) {
            rangerEntityId = pgs.getMyUnitSpawned(EntityType.RANGED_UNIT).orElse(null);
            this.getRelatedEntityIds().add(rangerEntityId);
            if (rangerEntityId == null) {
                return false;
            }
        }

        //decide what to do

        return false;
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Position2d rp = pgs.getEntityIdToCell().get(rangerEntityId).getPosition();
        if (rp.lenShiftSum(attackPosition) < 8) {
            DebugOut.println("Reach attackPosition: " + attackPosition);

            if (attackPosition.equals(strategyParams.attackPoints.get(0))) {
                attackPosition = Position2dUtil.randomMapPosition();
            } else {
                attackPosition = strategyParams.attackPoints.get(0);
            }
        }

        Cell rc = pgs.at(rp);


        if (rc.getAttackerCount(6) > 1 && rc.getPosition().lenShiftSum(retreatPosition) >= strategyParams.retreatStopRange) {
            DebugOut.println("Ranger retreat: " + rp);

            updateRetreat(gameHistoryState, pgs, strategyParams, assignedActions);
        } else if (rc.getAttackerCount(6) == 1
                        || rc.getAttackerCount(7) == 1) {
            DebugOut.println("Ranger hold: " + rp);

            updateHold(gameHistoryState, pgs, strategyParams, assignedActions);
        } else {
            updateAttack(gameHistoryState, pgs, strategyParams, assignedActions);
        }
    }

    public void updateAttack(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                             StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(Position2dUtil.RANGER_RANGE,
                strategyParams.rangerDefaultAttackTargets));

        autoAttack.setAttackAction(attackAction);

        autoAttack.setMoveAction(new MoveAction(attackPosition.toVec2dInt(), true, true));

        assignedActions.put(rangerEntityId, new ValuedEntityAction(0.5, rangerEntityId, autoAttack));
    }

    public void updateHold(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                           StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(0,
                strategyParams.rangerDefaultAttackTargets));
        autoAttack.setAttackAction(attackAction);

        assignedActions.put(rangerEntityId, new ValuedEntityAction(0.5, rangerEntityId, autoAttack));
    }

    public void updateRetreat(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                              StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(0,
                strategyParams.rangerDefaultAttackTargets));
        autoAttack.setAttackAction(attackAction);

        autoAttack.setMoveAction(new MoveAction(retreatPosition.toVec2dInt(), true, true));

        assignedActions.put(rangerEntityId, new ValuedEntityAction(0.5, rangerEntityId, autoAttack));
    }

    @Override
    public String toString() {
        return "RangerAttackHoldRetreatCommand{" +
                "rangerEntityId=" + rangerEntityId +
                ", attackPosition=" + attackPosition +
                ", retreatPosition=" + retreatPosition +
                '}';
    }
}
