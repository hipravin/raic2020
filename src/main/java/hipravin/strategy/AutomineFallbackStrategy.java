package hipravin.strategy;

import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2dUtil;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.SubStrategy;
import hipravin.strategy.ValuedEntityAction;
import model.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class AutomineFallbackStrategy implements SubStrategy {
    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        autoAttackAllUnboundWorkers(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }

    public void autoAttackAllUnboundWorkers(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                             StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Set<Integer> busyWorkers = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();


        List<Entity> notBusyWorkers = currentParsedGameState.getMyWorkers()
                .values().stream().map(Cell::getEntity)
                .filter(e -> !busyWorkers.contains(e.getId()))
                .collect(Collectors.toList());


        notBusyWorkers.forEach(w -> {
            EntityAction autoAttack = new EntityAction();
            AttackAction attackAction = new AttackAction(null, new AutoAttack(2 * Position2dUtil.MAP_SIZE, new EntityType[]{EntityType.RESOURCE}));
            autoAttack.setAttackAction(attackAction);
            assignedActions.put(w.getId(), new ValuedEntityAction(0.5, w.getId(), autoAttack));
        });
    }
}
