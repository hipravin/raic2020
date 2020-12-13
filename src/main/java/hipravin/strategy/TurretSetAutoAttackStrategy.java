package hipravin.strategy;

import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import model.*;

import java.util.*;
import java.util.stream.Collectors;

import static hipravin.model.Position2d.of;

public class TurretSetAutoAttackStrategy implements SubStrategy {
    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        return true;
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Set<Integer> busyEntities = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();

        Set<Integer> turrets = pgs.getBuildingsByEntityId().values()
                .stream()
                .filter(b -> b.isMyBuilding() && b.getCornerCell().getEntity().isActive())
                .filter(b -> b.getCornerCell().getEntity().getEntityType() == EntityType.TURRET)
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

        //home turrets to attack minerals
        for (Integer turret : turrets) {
            Cell tcell = pgs.getEntityIdToCell().get(turret);
            if (tcell.getPosition().lenShiftSum(Position2dUtil.MY_CC.shift(5, 5)) < strategyParams.turretsForCleanupRange) {

                Integer attackTarget = mineralFieldIdToAttack(tcell, pgs).orElse(null);

                EntityAction autoAttack = new EntityAction();
                AttackAction attackAction = new AttackAction(attackTarget, new AutoAttack(Position2dUtil.RANGER_RANGE,
                        EnumSet.allOf(EntityType.class).toArray(new EntityType[0])));
                autoAttack.setAttackAction(attackAction);
                assignedActions.put(turret, new ValuedEntityAction(0.5, turret, autoAttack));
            }
        }
    }

    Optional<Integer> mineralFieldIdToAttack(Cell tcell, ParsedGameState pgs) {
        Optional<Integer> mineral;

        if (tcell.getPosition().getX() > tcell.getPosition().getY()) {

            mineral = Arrays.stream(pgs.getPlayerView().getEntities())
                    .filter(e -> e.getEntityType() == EntityType.RESOURCE)
                    .filter(e -> Position2dUtil.turretCanAttackPosition(tcell.getPosition(), of(e.getPosition())))
                    .filter(e -> e.getPosition().getY() == tcell.getPosition().getY() + 1)
                    .findFirst().map(Entity::getId);


        } else {
            mineral = Arrays.stream(pgs.getPlayerView().getEntities())
                    .filter(e -> e.getEntityType() == EntityType.RESOURCE)
                    .filter(e -> Position2dUtil.turretCanAttackPosition(tcell.getPosition(), of(e.getPosition())))
                    .filter(e -> e.getPosition().getX() == tcell.getPosition().getX() + 1)
                    .findFirst().map(Entity::getId);
        }

        return mineral.or(() ->
                Arrays.stream(pgs.getPlayerView().getEntities())
                        .filter(e -> e.getEntityType() == EntityType.RESOURCE)
                        .filter(e -> Position2dUtil.turretCanAttackPosition(tcell.getPosition(), of(e.getPosition())))
                        .min(Comparator.comparingInt(e -> e.getPosition().getX() + e.getPosition().getY()))
                        .map(Entity::getId)
        );
    }
}
