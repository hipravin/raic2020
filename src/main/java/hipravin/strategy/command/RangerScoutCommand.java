package hipravin.strategy.command;

import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.*;

import java.util.*;
import java.util.stream.Collectors;

import static hipravin.strategy.StrategyParams.MAX_VAL;

public class RangerScoutCommand extends Command {
    Integer entityId;
    final NextDirectionSelector directionSelector;
    Position2d startPosition;
    Position2d targetPosition;

    public RangerScoutCommand(Integer entityId,
                              NextDirectionSelector directionSelector, Position2d startPosition) {
        super(MAX_VAL, new HashSet<>());
        if(entityId != null) {
            this.getRelatedEntityIds().add(entityId);
        }
        this.entityId = entityId;
        this.directionSelector = directionSelector;
        this.startPosition = startPosition;
    }

    public static RangerScoutCommand toFogNearEnemyHalfWay(Integer entityId, Position2d startPosition, Position2d scoutFinalPosition) {
        NextDirectionSelector fogHalfWay = new ToFogNearPointHalfWay(scoutFinalPosition);
        return new RangerScoutCommand(entityId, fogHalfWay, startPosition);
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if(entityId == null) {
            entityId = pgs.getMyUnitSpawned(EntityType.RANGED_UNIT).orElse(null);
            this.getRelatedEntityIds().add(entityId);
            if(entityId == null) {
                return false;
            }
        }

        Cell c = pgs.getEntityIdToCell().get(entityId);
        if (c == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if(entityId == null) {
            entityId = pgs.getMyUnitSpawned(EntityType.RANGED_UNIT).orElse(null);
            this.getRelatedEntityIds().add(entityId);
            if(entityId == null) {
                return false;
            }
        }

        Position2d currentPosition = pgs.getMyRangers().get(entityId).getPosition();
        if(directionSelector.needToSelectNextTarget(startPosition, currentPosition, targetPosition)) {
            Optional<Position2d> newTarget = directionSelector.nextPosition(currentPosition, gameHistoryState, pgs, strategyParams);
            if(newTarget.isPresent()) {
                targetPosition = newTarget.get();
            }

            return newTarget.isEmpty();

        } else {
            return false;
        }


    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                                      StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(Position2dUtil.MAP_SIZE,
                strategyParams.rangerDefaultAttackTargets));
        autoAttack.setAttackAction(attackAction);

        autoAttack.setMoveAction(new MoveAction(targetPosition.toVec2dInt(), true, true));

        assignedActions.put(entityId, new ValuedEntityAction(0.5, entityId, autoAttack));
    }

    @Override
    public String toString() {
        return "RangerScoutCommand{" +
                "entityId=" + entityId +
                ", startPosition=" + startPosition +
                ", targetPosition=" + targetPosition +
                '}';
    }

    public interface NextDirectionSelector {
        boolean needToSelectNextTarget(Position2d startPosition, Position2d currentPosition, Position2d currentTarget);

        Optional<Position2d> nextPosition(Position2d currentPosition, GameHistoryAndSharedState gameHistoryState,
                                          ParsedGameState currentParsedGameState, StrategyParams strategyParams);
    }

    public static class ToFogNearPointHalfWay implements NextDirectionSelector {
        final Position2d scoutFinalPosition;

        public ToFogNearPointHalfWay(Position2d scoutFinalPosition) {
            this.scoutFinalPosition = scoutFinalPosition;
        }

        @Override
        public boolean needToSelectNextTarget(Position2d startPosition, Position2d currentPosition, Position2d currentTarget) {

            return currentPosition == null || startPosition == null || currentTarget == null
                    || startPosition.lenShiftSum(currentPosition) > currentPosition.lenShiftSum(currentTarget);
        }

        @Override
        public Optional<Position2d> nextPosition(Position2d currentPosition, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {

            List<Position2d> fogEdges = pgs.getFogEdgePositions();

            if (fogEdges.isEmpty()) {
                return Optional.empty();
            }

            List<Integer> randomFogEdgeIndices = GameHistoryAndSharedState.splittableRandom
                    .ints(strategyParams.numberOfRandomScoutChoices, 0, fogEdges.size())
                    .limit(strategyParams.numberOfRandomScoutChoices)
                    .boxed().collect(Collectors.toList());

            Position2d nextScoutPos = randomFogEdgeIndices.stream().map(fogEdges::get)
                    .min(Comparator.comparingInt(p -> (int) p.lenShiftSum(currentPosition))).orElse(null);

            if (nextScoutPos == null) {
                return Optional.empty();
            }

            return Optional.of(nextScoutPos);
        }
    }
}
