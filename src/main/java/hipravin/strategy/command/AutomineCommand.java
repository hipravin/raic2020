package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2dUtil;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.AttackAction;
import model.AutoAttack;
import model.EntityAction;
import model.EntityType;

import java.util.Map;
import java.util.Set;

@Deprecated
public class AutomineCommand extends Command {
    final int entityId;

    public AutomineCommand(int entityId) {
        super(StrategyParams.MAX_VAL, Set.of(entityId));
        this.entityId = entityId;
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams) {
        return true;
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams) {
        return false;
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(2 * Position2dUtil.MAP_SIZE,
                new EntityType[]{EntityType.RESOURCE}));
        autoAttack.setAttackAction(attackAction);
        assignedActions.put(entityId, new ValuedEntityAction(0.5, entityId, autoAttack));

    }

    @Override
    public String toString() {
        return "AutomineCommand{" +
                "entityId=" + entityId +
                '}';
    }
}
