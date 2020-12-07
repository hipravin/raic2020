package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;

public interface CommandPredicate {
    boolean test(Command command, ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState, StrategyParams strategyParams);
}
