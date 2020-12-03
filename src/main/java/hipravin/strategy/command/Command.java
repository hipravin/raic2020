package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Command {
    private final Set<Integer> relatedEntityIds = new HashSet<>();
    private final int expiryTick;//if currentTick >= expiryTick then command is removed at start of processing

    private Command nextCommand;

    protected Command(int expiryTick, Collection<Integer> relatedEntityIds) {
        this.expiryTick = expiryTick;
    }


    public abstract boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                                    StrategyParams strategyParams);

    public abstract boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                     StrategyParams strategyParams);

    public abstract void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                               StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions);


    /**
     * To be checked after
     * @return
     */
    public boolean isStale(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                           StrategyParams strategyParams) {
        return currentParsedGameState.curTick() >= expiryTick;
    }

    public Set<Integer> getRelatedEntityIds() {
        return relatedEntityIds;
    }


    public int getExpiryTick() {
        return expiryTick;
    }

    public Command getNextCommand() {
        return nextCommand;
    }

    public void setNextCommand(Command nextCommand) {
        this.nextCommand = nextCommand;
    }
}
