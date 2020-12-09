package hipravin.strategy.command;

import hipravin.DebugOut;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static hipravin.strategy.StrategyParams.MAX_VAL;

public class RangerAttackHoldRetreatCommand extends Command {
    Integer rangerEntityId;
    final Position2d attackPosition;
    final Position2d retreatPosition;

    public RangerAttackHoldRetreatCommand(Integer rangerEntityId, Position2d attackPosition, Position2d retreatPosition) {
        super(MAX_VAL, new HashSet<>());
        if (rangerEntityId != null) {
            this.getRelatedEntityIds().add(rangerEntityId);
        }
        this.rangerEntityId = rangerEntityId;
        this.attackPosition = attackPosition;
        this.retreatPosition = retreatPosition;
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
            if(pgs.at(p).isEnemyRanger()) {
                counter.incrementAndGet();
            }
        });

        return counter.get();
    }
    public int countMyRangersNearby(Position2d rangerPosition, int range,
                                    GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger counter = new AtomicInteger(0);
        Position2dUtil.iterAllPositionsInRangeExclusive(rangerPosition, range, p -> {
            if(pgs.at(p).isMyRanger()) {
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
        int enemyCount = countEnemyRangersNearby(rp, strategyParams.attackHoldEnemyRange, gameHistoryState, pgs, strategyParams);
        int myCount = countMyRangersNearby(rp, strategyParams.attackHoldMyRange, gameHistoryState, pgs, strategyParams);

        if(strategyParams.useAttackHoldOverCountTreshold
                && enemyCount * strategyParams.attackOverCountTreshhold > myCount) {
            DebugOut.println("Ranger hold: " + rp + ", my: " + myCount + ", enemy: " + enemyCount);
            updateHold(gameHistoryState, pgs, strategyParams, assignedActions);
        } else {
            updateAttack(gameHistoryState, pgs, strategyParams, assignedActions);
        }
    }

    public void updateAttack(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                             StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(Position2dUtil.MAP_SIZE,
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

    @Override
    public String toString() {
        return "RangerAttackHoldRetreatCommand{" +
                "rangerEntityId=" + rangerEntityId +
                ", attackPosition=" + attackPosition +
                ", retreatPosition=" + retreatPosition +
                '}';
    }
}
