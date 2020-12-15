package hipravin.strategy;

import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import model.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class RangerAutoattackFallbackStrategy implements SubStrategy {
    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        autoAttackAllUnboundRangers(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }

    public void autoAttackAllUnboundRangers(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                                            StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Set<Integer> busyEntities = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();

        List<Entity> notBusyRangers = currentParsedGameState.getMyRangers()
                .values().stream().map(Cell::getEntity)
                .filter(e -> !busyEntities.contains(e.getId()))
                .collect(Collectors.toList());


        Vec2Int toPosition = currentParsedGameState.isRound1()
                ? new Vec2Int(25,25)
                : new Vec2Int(79,79);

        notBusyRangers.forEach(w -> {
            EntityAction autoAttack = new EntityAction();
            AttackAction attackAction = new AttackAction(null, new AutoAttack(10,
                    strategyParams.rangerDefaultAttackTargets));
            autoAttack.setAttackAction(attackAction);

            autoAttack.setMoveAction(new MoveAction(toPosition, true, true));

            assignedActions.put(w.getId(), new ValuedEntityAction(0.5, w.getId(), autoAttack));
        });
    }
}
