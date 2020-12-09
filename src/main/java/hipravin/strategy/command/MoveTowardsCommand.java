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

import static hipravin.strategy.StrategyParams.MAX_VAL;

public class MoveTowardsCommand extends Command {
    final int entityId;
    final Position2d targetPosition;

    public MoveTowardsCommand(ParsedGameState pgs, int entityId, Position2d targetPosition, int expectedPathLen) {
        super(pgs.curTick() + expectedPathLen, Set.of(entityId));
        this.entityId = entityId;
        this.targetPosition = targetPosition;
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams) {

        Cell c = currentParsedGameState.getEntityIdToCell().get(entityId);
        if (c == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams) {
        return currentParsedGameState.getEntityIdToCell().get(entityId).getPosition().lenShiftSum(targetPosition) < strategyParams.moveTowardsDistanceTreshold;
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
}
