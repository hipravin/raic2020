package hipravin.strategy;

import hipravin.model.ParsedGameState;
import model.Entity;

import java.util.Map;

public class SpawnWorkersStrategy implements SubStrategy {
    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                          StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Entity mycc = pgs.getMyCc();
        if(mycc != null) {
            boolean ccLocked = gameHistoryState.allOngoingCommandRelatedEntitiIds().anyMatch(id -> id.equals(mycc.getId()));

            return !ccLocked;
        }

        return false;
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

    }
}
