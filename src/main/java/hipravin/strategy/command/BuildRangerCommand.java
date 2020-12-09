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

public class BuildRangerCommand extends Command {

    final Position2d spawnLocation;
    final int rangerBaseEntityId;

    public BuildRangerCommand(Position2d spawnLocation, ParsedGameState pgs, int tickLimit) {
        super(pgs.curTick() + tickLimit, Set.of(pgs.getMyRangerBase().getId()));
        this.spawnLocation = spawnLocation;
        this.rangerBaseEntityId = pgs.getMyRangerBase().getId();
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        return pgs.getMyRangerBase() != null && (pgs.at(spawnLocation).isEmpty() || pgs.at(spawnLocation).isMyRanger());
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        return pgs.at(spawnLocation).isMyRanger() && pgs.getMyUnitSpawned(EntityType.RANGED_UNIT).isPresent();
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction action = new EntityAction();
        BuildAction ba = new BuildAction(EntityType.RANGED_UNIT, spawnLocation.toVec2dInt());
        action.setBuildAction(ba);

        assignedActions.put(rangerBaseEntityId, new ValuedEntityAction(0.5, rangerBaseEntityId, action));
    }

    @Override
    public String toString() {
        return "BuildRangerCommand{" +
                "spawnLocation=" + spawnLocation +
                ", rangerBaseEntityId=" + rangerBaseEntityId +
                '}';
    }
}
