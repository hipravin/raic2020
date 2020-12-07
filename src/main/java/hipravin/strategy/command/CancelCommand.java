package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static hipravin.strategy.StrategyParams.MAX_VAL;

public class CancelCommand extends Command {
    protected CancelCommand() {
        super(MAX_VAL, Set.of());
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams) {
        return true;
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams) {
        return true;
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
    }
}
