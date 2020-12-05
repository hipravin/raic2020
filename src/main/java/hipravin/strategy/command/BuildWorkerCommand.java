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

import static hipravin.strategy.StrategyParams.MAX_VAL;

public class BuildWorkerCommand extends Command {

    final Position2d spawnLocation;
    final int myCcEntityId;


    public BuildWorkerCommand(Position2d spawnLocation, ParsedGameState pgs) {
        super(MAX_VAL, Set.of(pgs.getMyCc().getId()));
        this.spawnLocation = spawnLocation;
        this.myCcEntityId = pgs.getMyCc().getId();
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        return pgs.getMyCc() != null && pgs.at(spawnLocation).isEmpty();
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

    @Override
    public String toString() {
        return "BuildWorkerCommand{" +
                "spawnLocation=" + spawnLocation +
                ", myCcEntityId=" + myCcEntityId +
                '}';
    }
}
