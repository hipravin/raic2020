package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.model.Building;
import hipravin.model.ParsedGameState;
import model.EntityAction;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UnsetBuildCommandsStrategy implements SubStrategy {
    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        return true;
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Set<Integer> busyEntities = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();

        Set<Integer> notBusyProducingEntityIds = pgs.getBuildingsByEntityId().values().stream()
                .filter(Building::isMyBuilding)
                .filter(Building::isProducingBuilding)
                .map(Building::getId)
                .collect(Collectors.toSet());

        notBusyProducingEntityIds.removeAll(busyEntities);

        for (Integer id : notBusyProducingEntityIds) {
            DebugOut.println("Unset build for: " + id);

            EntityAction emptyEntityAction = new EntityAction();

            assignedActions.put(id, new ValuedEntityAction(0.5, id, emptyEntityAction));
        }
    }
}
