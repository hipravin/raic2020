package hipravin.strategy;

import hipravin.model.ParsedGameState;

import java.util.Map;

public class MicroSubStrategy implements SubStrategy {
    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

    }
}
