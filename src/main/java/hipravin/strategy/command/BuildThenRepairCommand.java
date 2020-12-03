package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.BuildAction;
import model.EntityAction;
import model.EntityType;

import java.util.Map;
import java.util.Set;

public class BuildThenRepairCommand extends Command {
    final Position2d cornerPosition;
    final EntityType buildingType;
    final int builderWorkerId;

    public BuildThenRepairCommand(Position2d cornerPosition, EntityType entityType, int builderWorkerId,  ParsedGameState pgs, StrategyParams strategyParams) {
        super(pgs.curTick() + strategyParams.buildCommandMaxWaitTicks, Set.of(builderWorkerId));
        this.cornerPosition = cornerPosition;
        this.buildingType = entityType;
        this.builderWorkerId = builderWorkerId;

        this.setNextCommand(new AutoRepairCommand(cornerPosition, builderWorkerId, pgs, strategyParams));
    }


    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if(!pgs.getEntityIdToCell().containsKey(builderWorkerId)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        return pgs.at(cornerPosition).test(c -> c.isMyBuilding());
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction entityAction = new EntityAction();
        BuildAction buildAction = new BuildAction(buildingType, cornerPosition.toVec2dInt());
        entityAction.setBuildAction(buildAction);
        assignedActions.put(builderWorkerId, new ValuedEntityAction(0.5, builderWorkerId, entityAction));
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
