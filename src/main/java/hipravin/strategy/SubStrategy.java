package hipravin.strategy;

import hipravin.model.ParsedGameState;

import java.util.Map;

public interface SubStrategy {
    default boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState,
                                ParsedGameState currentParsedGameState,
                                StrategyParams strategyParams,
                                Map<Integer, ValuedEntityAction> assignedActions) {
        return true;
    }

    @Deprecated
    default boolean isExclusivelyApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState,
                                ParsedGameState currentParsedGameState,
                                StrategyParams strategyParams,
                                Map<Integer, ValuedEntityAction> assignedActions) {
        return false;
    }

    void decide(GameHistoryAndSharedState gameHistoryState,
                ParsedGameState currentParsedGameState,
                StrategyParams strategyParams,
                Map<Integer, ValuedEntityAction> assignedActions);
}
