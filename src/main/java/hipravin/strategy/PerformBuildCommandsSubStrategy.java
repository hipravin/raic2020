package hipravin.strategy;


import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.strategy.command.BuildingBuildCommand;
import hipravin.strategy.command.RepairCommand;
import model.BuildAction;
import model.EntityAction;
import model.MoveAction;
import model.RepairAction;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates move, build, repair sequence for single or multiple worker building build actions.
 * <p>
 * If for some reason build can't be performed, try to repeat for a few ticks, then DROP the commmand
 */
public class PerformBuildCommandsSubStrategy implements SubStrategy {


    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Set<Integer> buildersUsedEntityIds = new HashSet<>();//check for conflicts, use first build commands with top priority

        gameHistoryState.getOngoingBuildCommands().forEach(bc -> {
            performBuildCommand(bc, buildersUsedEntityIds, gameHistoryState,
                    currentParsedGameState, strategyParams, assignedActions);
        });
    }

    void performBuildCommand(BuildingBuildCommand command, Set<Integer> buildUsedEntityIds,
                             GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                             StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        if (buildUsedEntityIds.contains(command.getBuilderEntityId())) {
            return;
        }
        buildUsedEntityIds.add(command.getBuilderEntityId());

        Cell builder = currentParsedGameState.getMyWorkers().get(command.getBuilderEntityId());

        if (builder != null) {

            if (!builder.getPosition().equals(command.getBuilderBuildPosition())) {
                move(builder.getEntityId(), command.getBuilderBuildPosition(), assignedActions);
            } else {//can build
                if (!currentParsedGameState.at(command.getBuildingCornerPosition()).isMyBuilding()) {
                    build(command, assignedActions);
                }
            }

            if (currentParsedGameState.at(command.getBuildingCornerPosition())
                    .test(c -> c.isMyBuilding() && !c.getEntity().isActive())) {
                command.getRepairCommands().forEach(rc -> {
                    Cell repairer = currentParsedGameState.getMyWorkers().get(rc.getRepairerEntityId());
                    if (repairer.getPosition().equals(rc.getRepairPosition())
                          && rc.getRepairStartTick() <= currentParsedGameState.curTick()) {
                        repair(command, rc, currentParsedGameState.at(command.getBuildingCornerPosition()).getEntityId(), assignedActions);
                    }
                });
            }
            //move repairers
            command.getRepairCommands().forEach(rc -> {
                Cell repairer = currentParsedGameState.getMyWorkers().get(rc.getRepairerEntityId());
                if (repairer != null
                        && !repairer.getPosition().equals(rc.getRepairPosition())
                        && rc.getMoveToRepairPositionTick().map(t -> t <= currentParsedGameState.curTick()).orElse(false)) {
                    move(repairer.getEntityId(), rc.getRepairPosition(), assignedActions);
                }
            });

        }
    }

    void move(int entityId, Position2d to, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction action = new EntityAction();
        MoveAction moveAction = new MoveAction(to.toVec2dInt(), true, false);

        action.setMoveAction(moveAction);
        assignedActions.put(entityId, new ValuedEntityAction(0.5, entityId, action));
    }

    void build(BuildingBuildCommand bc, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction entityAction = new EntityAction();
        BuildAction buildAction = new BuildAction(bc.getBuildingType(), bc.getBuildingCornerPosition().toVec2dInt());
        entityAction.setBuildAction(buildAction);
        assignedActions.put(bc.getBuilderEntityId(), new ValuedEntityAction(0.5, bc.getBuilderEntityId(), entityAction));
    }

    void repair(BuildingBuildCommand bc, RepairCommand rc, int buildingId, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction action = new EntityAction();
        action.setRepairAction(new RepairAction(buildingId));

        assignedActions.put(rc.getRepairerEntityId(), new ValuedEntityAction(0.5, rc.getRepairerEntityId(), action));
        assignedActions.put(rc.getRepairerEntityId(), new ValuedEntityAction(0.5, rc.getRepairerEntityId(), action));
    }

}
