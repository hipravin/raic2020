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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class MoveTowardsCommand extends SingleEntityCommand {
    Position2d targetPosition;
    Integer followEntityId;
    Map<Integer, Position2d> lastPositions = new HashMap<>();


    final int distanceCancelTreshhold;

    BiConsumer<Integer, Integer> oncompleteIdTickConsumer;
    BiConsumer<Integer, Integer> onStartIdTickConsumer;

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

        if (entityId == null) {
            return false;
        }

        if (onStartIdTickConsumer != null) {
            onStartIdTickConsumer.accept(entityId, currentParsedGameState.curTick());
            onStartIdTickConsumer = null;
        }

        Cell c = currentParsedGameState.getEntityIdToCell().get(entityId);
        if (c == null) {
            return false;
        }

        Position2d currentPosition = currentParsedGameState.getEntityIdToCell().get(entityId).getPosition();

        lastPositions.put(currentParsedGameState.curTick(), currentPosition);
        lastPositions.entrySet().removeIf(e -> e.getKey().equals(currentParsedGameState.curTick() - StrategyParams.MOVE_TOWARDS_MAX_HIST));

        if(isStuck(currentPosition, gameHistoryState, currentParsedGameState, strategyParams)) {
            return false;
        }

        return true;
    }

    boolean isStuck(Position2d currentPosition, GameHistoryAndSharedState gameHistoryState,
                    ParsedGameState pgs, StrategyParams strategyParams) {

        if(lastPositions.size() < 3) {
            return false;
        }
        int curTick = pgs.curTick();

        Position2d pr = lastPositions.get(curTick - 1);
        Position2d prpr = lastPositions.get(curTick - 2);

        if(pr == null || prpr == null) {
            return false;
        }

        if(currentPosition.equals(prpr) && !currentPosition.equals(pr)) {
            DebugOut.println("Move stuck: " + currentPosition);

            if(gameHistoryState.getSentToBarrackEntityIds().contains(entityId)) {
                DebugOut.println("Requesting pull to center");
                gameHistoryState.incrementWorkerPullToCenterRequests();
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams) {
        tryResolveEntityId(currentParsedGameState);
        boolean completed = currentParsedGameState.getEntityIdToCell().get(entityId).getPosition().lenShiftSum(targetPosition) < distanceCancelTreshhold;

        if (completed) {
            if (oncompleteIdTickConsumer != null) {
                oncompleteIdTickConsumer.accept(entityId, currentParsedGameState.curTick());
            }
        }

        return completed;
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                                      StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        EntityAction action = new EntityAction();
        Position2d toPosition = targetPosition;

        Position2d ccPosition = Position2dUtil.MY_CC.shift(5,5);
        Position2d currentPosition = currentParsedGameState.getEntityIdToCell().get(entityId).getPosition();

        //turn of follow because they don't then break minerals and we don;t have time to clear path
        //try again?

        if(strategyParams.useWorkerFollow) {

            Position2d followEntityIdPosition = Optional.ofNullable(currentParsedGameState.getEntityIdToCell()
                    .get(followEntityId)).map(Cell::getPosition).orElse(null);

            if (followEntityId != null && currentParsedGameState.getEntityIdToCell().containsKey(followEntityId)
                    && gameHistoryState.allOMoveTowardsCommadsRelatedIds().contains(followEntityId)
                    && followEntityIdPosition != null
//                    && ccPosition.lenShiftSum(currentPosition) > strategyParams.useWorkerFollowMinRange
                    && targetPosition.lenShiftSum(currentPosition) > targetPosition.lenShiftSum(followEntityIdPosition)) {

                if(followLooksRight(followEntityIdPosition, gameHistoryState, currentParsedGameState, strategyParams)) {
                    toPosition = followEntityIdPosition;
                } else {
                    //follow unit
                    toPosition = targetPosition;
                    followEntityId = null;
                }
            }
        }

        MoveAction moveAction = new MoveAction(toPosition.toVec2dInt(), true, true);

        AttackAction attackAction = new AttackAction(null, new AutoAttack(1,
                new EntityType[]{EntityType.BUILDER_UNIT, EntityType.WALL}));
        action.setAttackAction(attackAction);

        action.setMoveAction(moveAction);
        assignedActions.put(entityId, new ValuedEntityAction(0.5, entityId, action));
    }

    public boolean followLooksRight(Position2d followEntityIdPosition, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                    StrategyParams strategyParams) {
        Position2d oldFollowPosition = pgs.getWorkersMovedSinceLastTickReversed().get(followEntityIdPosition);
        Position2d myCurrPosition = pgs.getEntityIdToCell().get(entityId).getPosition();

        if(oldFollowPosition == null || myCurrPosition == null || followEntityIdPosition == null) {
            return false;
        }
        if(followEntityIdPosition.lenShiftSum(targetPosition) >= oldFollowPosition.lenShiftSum(targetPosition)
           || followEntityIdPosition.lenShiftSum(targetPosition) >= myCurrPosition.lenShiftSum(targetPosition)) {
            DebugOut.println("Cancel follow: " + myCurrPosition + " -> " + followEntityIdPosition);

            return  false;
        }

        return true;
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

    public BiConsumer<Integer, Integer> getOncompleteIdTickConsumer() {
        return oncompleteIdTickConsumer;
    }

    public void setOncompleteIdTickConsumer(BiConsumer<Integer, Integer> oncompleteIdTickConsumer) {
        this.oncompleteIdTickConsumer = oncompleteIdTickConsumer;
    }

    public BiConsumer<Integer, Integer> getOnStartIdTickConsumer() {
        return onStartIdTickConsumer;
    }

    public void setOnStartIdTickConsumer(BiConsumer<Integer, Integer> onStartIdTickConsumer) {
        this.onStartIdTickConsumer = onStartIdTickConsumer;
    }

    public void setFollowEntityId(Integer followEntityId) {
        this.followEntityId = followEntityId;
    }
}
