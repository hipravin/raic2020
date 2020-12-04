package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BuildThenRepairCommand extends Command {
    final Position2d cornerPosition;
    final EntityType buildingType;
    final int builderWorkerId;

    public BuildThenRepairCommand(Position2d cornerPosition, EntityType entityType, int builderWorkerId, ParsedGameState pgs, StrategyParams strategyParams) {
        super(pgs.curTick() + strategyParams.buildCommandMaxWaitTicks, Set.of(builderWorkerId));
        this.cornerPosition = cornerPosition;
        this.buildingType = entityType;
        this.builderWorkerId = builderWorkerId;

        CommandUtil.addNextCommands(this,
                List.of(new AutoRepairCommand(cornerPosition, builderWorkerId, pgs, strategyParams)));
    }


    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if (!pgs.getEntityIdToCell().containsKey(builderWorkerId)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        return pgs.at(cornerPosition).test(c -> c.isMyBuilding());
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        if (buildingType == EntityType.HOUSE && pgs.getMyEstimatedResourceThisTick() < pgs.getHouseCost()) { //in order to spam build earlier and perform asap, but not skip mining
            EntityAction action = new EntityAction();
            AttackAction attackAction = new AttackAction(null, new AutoAttack(1, new EntityType[]{EntityType.RESOURCE}));
            action.setAttackAction(attackAction);
            assignedActions.put(builderWorkerId, new ValuedEntityAction(0.5, builderWorkerId, action));

        } else {

            EntityAction entityAction = new EntityAction();
            BuildAction buildAction = new BuildAction(buildingType, cornerPosition.toVec2dInt());
            entityAction.setBuildAction(buildAction);
            assignedActions.put(builderWorkerId, new ValuedEntityAction(0.5, builderWorkerId, entityAction));
        }
    }

    @Override
    public String toString() {
        return "BuildThenRepairCommand{" +
                "cornerPosition=" + cornerPosition +
                ", buildingType=" + buildingType +
                ", builderWorkerId=" + builderWorkerId +
                '}';
    }
}
