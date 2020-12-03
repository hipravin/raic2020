package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.EntityAction;
import model.MoveAction;

import java.util.Map;
import java.util.Set;

public class MoveSingleCommand extends Command {
    final int entityId;
    final Position2d targetPosition;

    protected MoveSingleCommand(ParsedGameState pgs, int entityId, Position2d targetPosition, int expectedPathLen) {
        super(pgs.curTick() + expectedPathLen, Set.of(entityId));
        this.entityId = entityId;
        this.targetPosition = targetPosition;
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams) {
        return currentParsedGameState.getEntityIdToCell().containsKey(entityId);
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams) {
        return currentParsedGameState.getEntityIdToCell().get(entityId).getPosition().equals(targetPosition);
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                                      StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        EntityAction action = new EntityAction();
        MoveAction moveAction = new MoveAction(targetPosition.toVec2dInt(), true, true);

        action.setMoveAction(moveAction);
        assignedActions.put(entityId, new ValuedEntityAction(0.5, entityId, action));
    }
}
