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

    public BuildThenRepairCommand(Position2d cornerPosition, EntityType entityType, int builderWorkerId, ParsedGameState pgs, StrategyParams strategyParams,
                                  int builtCommanMaxWaitTicks) {
        super(pgs.curTick() + builtCommanMaxWaitTicks, Set.of(builderWorkerId));
        this.cornerPosition = cornerPosition;
        this.buildingType = entityType;
        this.builderWorkerId = builderWorkerId;

        CommandUtil.addNextCommands(this,
                List.of(new AutoRepairCommand(cornerPosition, builderWorkerId, pgs, strategyParams)));
    }

    public BuildThenRepairCommand(Position2d cornerPosition, EntityType entityType, int builderWorkerId, ParsedGameState pgs, StrategyParams strategyParams) {
        this(cornerPosition, entityType, builderWorkerId, pgs, strategyParams, strategyParams.buildCommandMaxWaitTicks);
    }


    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if (!pgs.getEntityIdToCell().containsKey(builderWorkerId)) {
            return false;
        }
        if ((buildingType == EntityType.MELEE_BASE || buildingType == EntityType.RANGED_BASE)
                && pgs.getMyBarrack(buildingType) != null) {
            return false;
        }

        if(pgs.calculateFreeSpace(pgs.at(cornerPosition),
                pgs.getPlayerView().getEntityProperties().get(buildingType).getSize())
                .map(fs -> fs.isFreeButContainOurUnits()).orElse(false)) {
            return false; //if building can't be placed - don;t try, find other position, don't wait
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

    public Position2d getCornerPosition() {
        return cornerPosition;
    }

    public EntityType getBuildingType() {
        return buildingType;
    }

    public int getBuilderWorkerId() {
        return builderWorkerId;
    }
}
