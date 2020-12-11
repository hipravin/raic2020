package hipravin.strategy;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2dUtil;
import model.AttackAction;
import model.AutoAttack;
import model.EntityAction;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TurretSetAutoAttackStrategy implements SubStrategy {
    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        return true;
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Set<Integer> busyEntities = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();

        Set<Integer> turrets = pgs.getBuildingsByEntityId().values()
                .stream().filter(b->b.isMyBuilding() && b.getCornerCell().getEntity().isActive())
                .map(b -> b.getCornerCell().getEntity().getId())
                .collect(Collectors.toSet());


        turrets.removeAll(busyEntities);

        for (Integer turret : turrets) {
            EntityAction autoAttack = new EntityAction();
            AttackAction attackAction = new AttackAction(null, new AutoAttack(Position2dUtil.RANGER_RANGE,
                    strategyParams.rangerDefaultAttackTargets));
            autoAttack.setAttackAction(attackAction);
            assignedActions.put(turret, new ValuedEntityAction(0.5, turret, autoAttack));
        }
    }
}
