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

public class BuildWorkerCommand extends Command {

    final Position2d spawnLocation;
    final int myCcEntityId;


    public BuildWorkerCommand(Position2d spawnLocation, ParsedGameState pgs) {
        super(Integer.MAX_VALUE, Set.of(pgs.getMyCc().getId()));
        this.spawnLocation = spawnLocation;
        this.myCcEntityId = pgs.getMyCc().getId();
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        return pgs.getMyCc() != null;
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        return pgs.at(spawnLocation).isMyWorker();
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction action = new EntityAction();
        BuildAction ba = new BuildAction(EntityType.BUILDER_UNIT, spawnLocation.toVec2dInt());
        action.setBuildAction(ba);

        assignedActions.put(myCcEntityId, new ValuedEntityAction(0.5, myCcEntityId, action));
    }
}
