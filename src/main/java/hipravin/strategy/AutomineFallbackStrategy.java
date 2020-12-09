package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.SubStrategy;
import hipravin.strategy.ValuedEntityAction;
import hipravin.strategy.command.MoveOneStepTowardsCommand;
import hipravin.strategy.command.MoveSingleCommand;
import model.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static hipravin.model.Position2d.of;


public class AutomineFallbackStrategy implements SubStrategy {
    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        autoAttackAllUnboundWorkers(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }

    public void handleWayBlockers(Set<Integer> busyEntities, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                  StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        if(pgs.getPopulation().getPopulationUse() <= strategyParams.wayOutBlockFindMaxPopulation) {

            for (Map.Entry<Position2d, Position2d> moved : pgs.getWorkersMovedSinceLastTick().entrySet()) {
                if(pgs.at(moved.getValue()).test(c -> busyEntities.contains(c.getEntityId()))) {
                    continue;//if busy worker block way - dont' touch him
                }

                if (BlockDetector.checkIfWorkerBlocksWayOut(moved.getValue(), gameHistoryState, pgs, strategyParams)) {

                    DebugOut.println("Worker at " + moved.getValue() + " blocks way out");

                    Position2d cur = moved.getValue();

                    Position2d stepForwardTo = null;

                    Position2d right = cur.shift(1, 0);
                    Position2d up = cur.shift(0, 1);

                    if(cur.x > cur.y && Position2dUtil.isPositionWithinMapBorder(right) && pgs.at(right).isEmpty()) {
                        stepForwardTo = right;

                    } else if(Position2dUtil.isPositionWithinMapBorder(up) && pgs.at(up).isEmpty()) {
                        stepForwardTo = up;
                    }

                    if(stepForwardTo != null) {
                        MoveOneStepTowardsCommand moveOneStepTowardsCommand = new MoveOneStepTowardsCommand(pgs, pgs.at(moved.getValue()).getEntityId(),
                                stepForwardTo);

                        gameHistoryState.addOngoingCommand(moveOneStepTowardsCommand, false);
                    }
                }
            }
        }
    }
    
    public void autoAttackAllUnboundWorkers(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                             StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Set<Integer> busyEntities = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();

        List<Entity> notBusyWorkers = currentParsedGameState.getMyWorkers()
                .values().stream().map(Cell::getEntity)
                .filter(e -> !busyEntities.contains(e.getId()))
                .collect(Collectors.toList());

        notBusyWorkers.forEach(w -> {
            EntityAction autoAttack = new EntityAction();
            AttackAction attackAction = new AttackAction(null, new AutoAttack(Position2dUtil.MAP_SIZE,
                    new EntityType[]{EntityType.RESOURCE}));
            autoAttack.setAttackAction(attackAction);
            assignedActions.put(w.getId(), new ValuedEntityAction(0.5, w.getId(), autoAttack));
        });

        handleWayBlockers(busyEntities, gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }
}
