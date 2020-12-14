package hipravin.strategy;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.command.BuildSwordmanCommand;
import model.*;

import java.util.*;

import static hipravin.model.Position2d.of;

public class Round1Strategy implements SubStrategy {
    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState,
                                          ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {


        return pgs.isRound1();
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        spawnSwordmans(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
        assignAttack(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }

    void spawnSwordmans(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                        StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        if (pgs.getMyWorkers().size() < strategyParams.round1WorkersFirst && pgs.curTick() < 200
                || pgs.getSwordCount() > 10
                || pgs.getMyRangers().size() < 15) {
            return;
        }

        Entity swordBase = pgs.getMySwordBase();
        if (swordBase != null) {

            Set<Position2d> meleeEdge = new HashSet<>(pgs.findMyBuildings(EntityType.MELEE_BASE).get(0)
                    .getBuildingOuterEdgeWithoutCorners());

            meleeEdge.removeIf(p -> !pgs.at(p).isEmpty());

            if (meleeEdge.isEmpty()) {
                return;
            }

            List<Position2d> edgeSorted = new ArrayList<>(meleeEdge);
            edgeSorted.sort(Comparator.comparing(p -> p.y, Comparator.reverseOrder()));

            gameHistoryState.addOngoingCommand(new BuildSwordmanCommand(edgeSorted.get(0), pgs, 1), false);
        }
    }

    void assignAttack(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                      StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {



        for (Entity sword : pgs.getPlayerView().getEntities()) {
            if (sword.getPlayerId() != null && sword.getPlayerId() == pgs.getPlayerView().getMyId()
                    && sword.getEntityType() == EntityType.MELEE_UNIT) {

                Position2d attackPos = pgs.findClosesEnemyArmyInDefArea(of(sword.getPosition())).orElse(of(25, 25));

                EntityAction autoAttack = new EntityAction();
                AttackAction attackAction = new AttackAction(null, new AutoAttack(Position2dUtil.MAP_SIZE,
                        strategyParams.rangerDefaultAttackTargets));
                autoAttack.setAttackAction(attackAction);

                autoAttack.setMoveAction(new MoveAction(attackPos.toVec2dInt(), true, true));

                assignedActions.put(sword.getId(), new ValuedEntityAction(0.5, sword.getId(), autoAttack));
            }
        }
    }
}
