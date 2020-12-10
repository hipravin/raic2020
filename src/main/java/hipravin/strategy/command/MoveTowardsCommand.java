package hipravin.strategy.command;

import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.*;

import java.util.Map;
import java.util.Set;

public class MoveTowardsCommand extends SingleEntityCommand {
    Position2d targetPosition;
    final int distanceCancelTreshhold;

    public MoveTowardsCommand(ParsedGameState pgs, int entityId, Position2d targetPosition, int expectedPathLen, int distanceCancelTreshhold) {
        super(pgs.curTick() + expectedPathLen, entityId);
        this.targetPosition = targetPosition;
        this.distanceCancelTreshhold = distanceCancelTreshhold;
    }

    public MoveTowardsCommand(ParsedGameState pgs, EntityType entityType, Position2d targetPosition, int expectedPathLen, int distanceCancelTreshhold) {
        super(pgs.curTick() + expectedPathLen, entityType);
        this.targetPosition = targetPosition;
        this.distanceCancelTreshhold = distanceCancelTreshhold;
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams) {
        tryResolveEntityId(currentParsedGameState);

        if(entityId == null) {
            return false;
        }

        Cell c = currentParsedGameState.getEntityIdToCell().get(entityId);
        if (c == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams) {
        tryResolveEntityId(currentParsedGameState);
        return currentParsedGameState.getEntityIdToCell().get(entityId).getPosition().lenShiftSum(targetPosition) < distanceCancelTreshhold;
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                                      StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        EntityAction action = new EntityAction();
        MoveAction moveAction = new MoveAction(targetPosition.toVec2dInt(), true, true);

        AttackAction attackAction = new AttackAction(null, new AutoAttack(1,
                new EntityType[]{EntityType.BUILDER_UNIT, EntityType.WALL}));
        action.setAttackAction(attackAction);

        action.setMoveAction(moveAction);
        assignedActions.put(entityId, new ValuedEntityAction(0.5, entityId, action));
    }

    @Override
    public String toString() {
        return "MoveTowardsCommand{" +
                "entityId=" + entityId +
                ", targetPosition=" + targetPosition +
                '}';
    }

    public void setTargetPosition(Position2d targetPosition) {
        this.targetPosition = targetPosition;
    }
}
