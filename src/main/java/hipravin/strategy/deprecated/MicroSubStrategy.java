package hipravin.strategy.deprecated;

import hipravin.model.ParsedGameState;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.SubStrategy;
import hipravin.strategy.ValuedEntityAction;

import java.util.Map;

@Deprecated
public class MicroSubStrategy implements SubStrategy {
    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

    }
}
