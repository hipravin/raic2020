package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Wait on current position until building is magically appeared at given position.
 * If nothing to repair - autoattack with path 0
 */
public class AutoRepairCommand extends Command {

    final int commandCreatedTick;
    final Position2d buildingCorner;
    final int workerEntityId;

    public AutoRepairCommand(Position2d buildingCorner, int workerEntityId, ParsedGameState pgs, StrategyParams strategyParams) {
        super(StrategyParams.MAX_VAL, Set.of(workerEntityId));//command is eternal until building isActive.
        this.commandCreatedTick = pgs.curTick();
        this.buildingCorner = buildingCorner;
        this.workerEntityId = workerEntityId;
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if(!pgs.getEntityIdToCell().containsKey(workerEntityId)) {
            return false;
        }

        return pgs.curTick() <= commandCreatedTick + strategyParams.autoRepairMaxWaitTicks
                || pgs.at(buildingCorner).test(c -> c.isMyBuilding());
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        return pgs.at(buildingCorner).test(c -> c.isMyBuilding() && c.getEntity().isActive());
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        if (pgs.at(buildingCorner).test(c -> c.isMyBuilding() && !c.getEntity().isActive())) {
            EntityAction action = new EntityAction();
            action.setRepairAction(new RepairAction(pgs.at(buildingCorner).getEntityId()));

            assignedActions.put(workerEntityId, new ValuedEntityAction(0.5, workerEntityId, action));
        } else {
            EntityAction autoAttack = new EntityAction();
            AttackAction attackAction = new AttackAction(null, new AutoAttack(0, new EntityType[]{EntityType.RESOURCE}));
            autoAttack.setAttackAction(attackAction);
            assignedActions.put(workerEntityId, new ValuedEntityAction(0.5, workerEntityId, autoAttack));
        }
    }

    @Override
    public String toString() {
        return "AutoRepairCommand{" +
                "commandCreatedTick=" + commandCreatedTick +
                ", buildingCorner=" + buildingCorner +
                ", workerEntityId=" + workerEntityId +
                '}';
    }
}
