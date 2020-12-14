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

public class BuildSwordmanCommand extends Command {

    final Position2d spawnLocation;
    final int meleeBaseEntityId;

    public BuildSwordmanCommand(Position2d spawnLocation, ParsedGameState pgs, int tickLimit) {
        super(pgs.curTick() + tickLimit, Set.of(pgs.getMySwordBase().getId()));
        this.spawnLocation = spawnLocation;
        this.meleeBaseEntityId = pgs.getMySwordBase().getId();
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        return pgs.getMySwordBase() != null && (pgs.at(spawnLocation).isEmpty() || pgs.at(spawnLocation).isMyUnit());
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        return pgs.at(spawnLocation).isMyUnit() && pgs.getMyUnitSpawned(EntityType.MELEE_UNIT).isPresent();
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction action = new EntityAction();
        BuildAction ba = new BuildAction(EntityType.MELEE_UNIT, spawnLocation.toVec2dInt());
        action.setBuildAction(ba);

        assignedActions.put(meleeBaseEntityId, new ValuedEntityAction(0.5, meleeBaseEntityId, action));
    }


    @Override
    public String toString() {
        return "BuildSwordmanCommand{" +
                "spawnLocation=" + spawnLocation +
                ", meleeBaseEntityId=" + meleeBaseEntityId +
                '}';
    }
}
