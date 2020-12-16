package hipravin.strategy;

import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.command.RangerAttackHoldRetreatMicroCommand;
import model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static hipravin.model.Position2d.of;


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


        if (currentParsedGameState.isRound1()) {
            Vec2Int toPosition = currentParsedGameState.isRound1()
                    ? new Vec2Int(15, 15)
                    : new Vec2Int(79, 79);

            notBusyRangers.forEach(w -> {
                EntityAction autoAttack = new EntityAction();
                AttackAction attackAction = new AttackAction(null, new AutoAttack(10,
                        strategyParams.rangerDefaultAttackTargets));
                autoAttack.setAttackAction(attackAction);

                autoAttack.setMoveAction(new MoveAction(toPosition, true, true));

                assignedActions.put(w.getId(), new ValuedEntityAction(0.5, w.getId(), autoAttack));
            });
        } else {
            Position2d myRangBase = Optional.ofNullable(currentParsedGameState.getMyRangerBase()).map(b -> of(b.getPosition()))
                    .orElse(of(40, 40));
            notBusyRangers.forEach(w -> {
                gameHistoryState.addOngoingCommand(
                        new RangerAttackHoldRetreatMicroCommand(
                                w.getId(), strategyParams.attackPoints.get(0), myRangBase.shift(2, 2)),
                        false);
            });
        }
    }
}
