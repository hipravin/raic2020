package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;

import java.util.*;

public abstract class Command {
    private final Set<Integer> relatedEntityIds = new HashSet<>();
    private final int expiryTick;//if currentTick >= expiryTick then command is removed at start of processing

    private List<Command> nextCommands = new ArrayList<>();

    protected Command(int expiryTick, Collection<Integer> relatedEntityIds) {
        this.expiryTick = expiryTick;
        this.relatedEntityIds.addAll(relatedEntityIds);
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

    public List<Command> getNextCommands() {
        return nextCommands;
    }
}
