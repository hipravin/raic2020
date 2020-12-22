package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.*;

import java.util.Map;
import java.util.Set;


public class RangerRepairCommand extends Command {

    final int workerEntityId;
    final int rangerId;

    public RangerRepairCommand(Integer rangerId, int workerEntityId, ParsedGameState pgs, StrategyParams strategyParams) {
        super(pgs.curTick() + 6, Set.of(workerEntityId));//command is eternal until building isActive.
        this.rangerId = rangerId;
        this.workerEntityId = workerEntityId;
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if (!pgs.getEntityIdToCell().containsKey(workerEntityId)) {
            return false;
        }
        if (!pgs.getEntityIdToCell().containsKey(rangerId)) {
            return false;
        }
        if (pgs.getEntityIdToCell().get(workerEntityId).getPosition()
                .lenShiftSum(pgs.getEntityIdToCell().get(rangerId).getPosition()) != 1) {
            return false;
        }


        return true;

    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        return (pgs.getEntityIdToCell().get(rangerId).getHealthLeft() >= 6);
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction action = new EntityAction();
        action.setRepairAction(new RepairAction(rangerId));

        assignedActions.put(workerEntityId, new ValuedEntityAction(0.5, workerEntityId, action));
    }

    @Override
    public String toString() {
        return "RangerRepairCommand{" +
                "workerEntityId=" + workerEntityId +
                ", rangerId=" + rangerId +
                '}';
    }
}
