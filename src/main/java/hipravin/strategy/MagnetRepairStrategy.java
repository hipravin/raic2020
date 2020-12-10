package hipravin.strategy;

import hipravin.model.*;
import hipravin.strategy.command.AutoRepairCommand;
import hipravin.strategy.command.Command;
import hipravin.strategy.command.MoveOneStepTowardsCommand;
import model.EntityType;

import java.util.*;

/**
 * If there are inactive buildings then tries to send all nearby workers to repair it
 */
public class MagnetRepairStrategy implements SubStrategy {
    void decideForInactiveBuilding(Building building, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                   StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Set<Position2d> buildingOuterEdge = building.getBuildingOuterEdgeWithoutCorners();

        Set<Integer> busyEntitiIds = gameHistoryState.allOngoingCommandRelatedEntitiIdsSet();

        Set<Position2d> requiredRepairPositions = new HashSet<>();

        int alreadyRepairing = 0;

        for (Position2d edge : buildingOuterEdge) { //determine required positions
            Cell atP = pgs.at(edge);

            if(atP.isMyWorker()) {
                alreadyRepairing ++;
//                if(!busyEntitiIds.contains(atP.getEntityId())) {
                    //start repair
                    AutoRepairCommand command = new AutoRepairCommand(building.getCornerCell().getPosition(), atP.getEntityId(), pgs, strategyParams);
                    gameHistoryState.addOngoingCommand(command, true);
//                }
            } else if(atP.isEmpty()) {
                requiredRepairPositions.add(edge);
            }
        }
        int countWorkersRequired = strategyParams.magnetRepairDesiredWorkers.get(building.getCornerCell().getEntityType()) - alreadyRepairing;

        if(countWorkersRequired <= 0) {
            return;
        }

        int maxPathLenToWorkers = strategyParams.magnetRepairRanges.get(building.getCornerCell().getEntityType());


        //perform wide search to find workers
        Map<Position2d, NearestEntity> nearestEntities = GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), requiredRepairPositions, maxPathLenToWorkers, false);

        nearestEntities.entrySet().removeIf(e -> !e.getValue().getThisCell().isMyWorker());
        nearestEntities.entrySet().removeIf(e -> busyEntitiIds.contains(e.getValue().getThisCell().getEntityId()));
        nearestEntities.entrySet().removeIf(e -> buildingOuterEdge.contains(e.getValue().getThisCell().getPosition()));

        List<NearestEntity> workersNeSorted = new ArrayList<>(nearestEntities.values());
        Comparator<NearestEntity> byPathLenNearestNe = Comparator.comparingInt(NearestEntity::getPathLenEmptyCellsToThisCell);
        workersNeSorted.sort(byPathLenNearestNe);

        Map<Position2d, Integer> toThisPoint = new HashMap<>();

        int maxToSinglePoint = EnumSet.of(EntityType.RANGED_BASE, EntityType.BUILDER_UNIT).contains(building.getCornerCell().getEntityType())
                ? strategyParams.magnetMaxToSinglePointBarrack
                : strategyParams.magnetMaxToSinglePointOthers;

        for (int i = 0; i < Math.min(workersNeSorted.size(), countWorkersRequired); i++) {
            NearestEntity workerNe = workersNeSorted.get(i);
            Position2d toPosition = workerNe.getSourceCell().getPosition();

            toThisPoint.merge(toPosition, 1, Integer::sum);
            if(toThisPoint.get(toPosition) <= maxToSinglePoint) {
                Command stepForward = new MoveOneStepTowardsCommand(pgs, workerNe.getThisCell().getEntityId(), toPosition);
                gameHistoryState.addOngoingCommand(stepForward, false);
            }
        }
    }

    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        //not apply before first house
        return pgs.getActiveHouseCount() > 0;
    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        List<Building> toRepairBuilding = new ArrayList<>(pgs.getBuildingsByEntityId().values());

        toRepairBuilding.sort(Comparator.comparingInt(b->b.getCornerCell().getMaxHealth())); // barracks, turret, house

        toRepairBuilding.forEach(b -> {
            if(b.isMyBuilding() && b.getCornerCell().getEntity().getHealth() < b.getCornerCell().getMaxHealth()) {
                decideForInactiveBuilding(b, gameHistoryState, pgs, strategyParams, assignedActions);
            }
        });
    }

//    void cleanupCompleted(Position2d corner, GameHistoryAndSharedState gameHistoryState,
//                               ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
//        BuildingMagnetRepairMetadata meta = buildingsInProgress.stream().filter(bp -> bp.corner.equals(corner))
//                .findFirst().orElse(null);
//
//        buildingsInProgress.removeIf(bp -> bp.corner.equals(corner));
//    }
//
//
//    static class BuildingMagnetRepairMetadata {
//        public Position2d corner;
//        public EntityType buildingEntityType;
//    }
}
