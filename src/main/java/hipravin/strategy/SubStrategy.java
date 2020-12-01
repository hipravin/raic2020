package hipravin.strategy;

import hipravin.model.ParsedGameState;

import java.util.Map;

public interface SubStrategy {
    void decide(GameHistoryAndSharedState gameHistoryState,
                ParsedGameState currentParsedGameState,
                StrategyParams strategyParams,
                Map<Integer, ValuedEntityAction> assignedActions);
}
