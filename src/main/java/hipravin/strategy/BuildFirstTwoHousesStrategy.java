package hipravin.strategy;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import model.EntityType;

import java.util.List;
import java.util.Map;

public class BuildFirstTwoHousesStrategy implements SubStrategy {
    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {





    }

    private List<Position2d> firstWorkerFirstTickNearNearestMinerals() {
        return  null;
    }





    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        return currentParsedGameState.findMyBuildings(EntityType.HOUSE).size() < 2
                && currentParsedGameState.getPlayerView().getCurrentTick() < 100; //just in case

    }

    @Override
    public boolean isExclusivelyApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        return isApplicableAtThisTick(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }

}
