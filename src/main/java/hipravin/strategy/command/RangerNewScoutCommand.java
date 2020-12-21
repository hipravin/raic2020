package hipravin.strategy.command;

import hipravin.DebugOut;
import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.*;

import java.util.*;

import static hipravin.model.Position2d.of;
import static hipravin.strategy.StrategyParams.MAX_VAL;

public class RangerNewScoutCommand extends SingleEntityCommand {

    boolean scoutCompleted = false;

    Position2d currentTarget = null;
    Map<Integer, Position2d> lastPositions = new HashMap<>();

    protected RangerNewScoutCommand(int entityId) {
        super(MAX_VAL, entityId);
    }

    public RangerNewScoutCommand(EntityType entityType) {
        super(MAX_VAL, entityType);
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        tryResolveEntityId(pgs);

        if (entityId == null) {
            return false;
        }

        Cell c = pgs.getEntityIdToCell().get(entityId);
        if (c == null) {
            return false;
        }

        lastPositions.put(pgs.curTick(), c.getPosition());
        lastPositions.entrySet().removeIf(e -> e.getKey().equals(pgs.curTick() - StrategyParams.RANGER_POS_HIST));


        return true;
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        tryResolveEntityId(pgs);

        if (entityId != null && pgs.getEntityIdToCell().containsKey(entityId)) {
            int attacker = pgs.getEntityIdToCell().get(entityId).getTotalNearAttackerCount();

            if (attacker > 0) {
                return true;
            }

            if (scoutCompleted) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Position2d currentPos = currentParsedGameState.getEntityIdToCell().get(entityId).getPosition();
        Position2d moveTo;
        if (currentTarget == null || currentTarget.lenShiftSum(currentPos) < Position2dUtil.RANGER_RANGE
             || isStuck(currentPos, gameHistoryState, currentParsedGameState, strategyParams)) {
            moveTo = nearestFog(gameHistoryState, currentParsedGameState, strategyParams);
            currentTarget = moveTo;
        } else {
            moveTo = currentTarget;
        }

        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(Position2dUtil.RANGER_RANGE,
                strategyParams.rangerDefaultAttackTargets));
        autoAttack.setAttackAction(attackAction);

        autoAttack.setMoveAction(new MoveAction(moveTo.toVec2dInt(), true, true));

        assignedActions.put(entityId, new ValuedEntityAction(0.5, entityId, autoAttack));
    }

    Position2d nearestFog(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        List<Position2d> fogEdges = pgs.getFogEdgePositions();
        Position2d currentPos = pgs.getEntityIdToCell().get(entityId).getPosition();
        Position2d mainAttackPoint = strategyParams.attackPoints.get(0);
        Position2d barrackPos = Optional.ofNullable(pgs.getMyRangerBase()).map(e -> of(e.getPosition())).orElse(of(40, 40));

        Position2d nearestFogCloserToOpp = fogEdges.stream()
                .filter(fe -> mainAttackPoint.lenShiftSum(fe) < mainAttackPoint.lenShiftSum(currentPos)) //closer than me to opp
                .filter(fe -> barrackPos.lenShiftSum(fe) > currentPos.lenShiftSum(fe)) //closer than me to opp
                .min(Comparator.comparingInt(fe -> fe.lenShiftSum(currentPos))).orElse(null);

        if (nearestFogCloserToOpp == null) {
            scoutCompleted = true;
            DebugOut.println("Nowhere to scout ranger: " + currentPos);
            return strategyParams.attackPoints.get(0);
        } else {
            DebugOut.println("Scout decided to move: " + currentPos + " -> " + nearestFogCloserToOpp);

            return nearestFogCloserToOpp;
        }
    }

    boolean isStuck(Position2d currentPosition, GameHistoryAndSharedState gameHistoryState,
                    ParsedGameState pgs, StrategyParams strategyParams) {

        if(lastPositions.size() < 3) {
            return false;
        }
        int curTick = pgs.curTick();

        Position2d pr = lastPositions.get(curTick - 1);

        if(pr == null) {
            return false;
        }

        int countSame = (int) lastPositions.values().stream()
                .filter(lp -> lp.equals(currentPosition))
                .count();

        return countSame > 1 && !currentPosition.equals(pr);
    }
}
