package hipravin.strategy;

import hipravin.model.*;
import hipravin.strategy.command.BuildingBuildCommand;
import model.BuildAction;
import model.EntityAction;
import model.EntityType;

import java.util.*;

/**
 * builds units and buildings
 */
public class BuildOrderSubStrategy implements SubStrategy {
    static Random r = new Random();

    private BuidingPosititioningLogic buildingPostitioningLogic = new BuidingPosititioningLogic();

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {


        stubBuildWorkersNonStop(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
        stubBuildHouses(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }

    public void stubBuildWorkersNonStop(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                                        StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        List<Building> ccs = currentParsedGameState.findMyBuildings(EntityType.BUILDER_BASE);

        for (Building cc : ccs) {
            EntityAction buildWorker = new EntityAction();

            Optional<Position2d> randomPositionToBuildWorker = cc.getBuildingEmptyOuterEdgeWithoutCorners().stream()
                    .reduce((p1, p2) -> r.nextInt() % 2 == 0 ? p1 : p2);

            if(randomPositionToBuildWorker.isPresent()) {

                BuildAction ba = new BuildAction(EntityType.BUILDER_UNIT, randomPositionToBuildWorker.get().toVec2dInt());
                buildWorker.setBuildAction(ba);

                assignedActions.put(cc.getId(), new ValuedEntityAction(0.5, cc.getId(), buildWorker));
            }
        }
    }

    public Optional<NearestEntity> nearestWorkerToBuild(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                                                        Position2d buildingCorner, int buildingSize) {

        Set<Integer> lockedWorkers = gameHistoryState.buildOrRepairAssignedWorkersAtCurrentTick();
        Optional<NearestEntity> nearestWorker = Position2dUtil.buildingOuterEdgeWithoutCorners(buildingCorner, buildingSize)
                .stream()
                .filter(p -> currentParsedGameState.at(p).getMyNearestWorker() != null)
                .map(p -> currentParsedGameState.at(p).getMyNearestWorker())
                .filter(w -> !lockedWorkers.contains(w.getSourceCell().getEntityId()))
                .min(Comparator.comparingInt(NearestEntity::getPathLenEmptyCellsToThisCell));

        return nearestWorker;
    }

    public void stubBuildHouses(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                                StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        int inAdvance = 10;

        int hCost = currentParsedGameState.getPlayerView().getEntityProperties().get(EntityType.HOUSE).getCost();
        int hSize = currentParsedGameState.getPlayerView().getEntityProperties().get(EntityType.HOUSE).getSize();
        int res = currentParsedGameState.getMyPlayer().getResource();

        if (currentParsedGameState.getPopulation().getPotentialLimit() - currentParsedGameState.getPopulation().getPopulationUse() < inAdvance
              && res >= hCost) {
            Optional<Position2d> housePosition = buildingPostitioningLogic.nearestCornerPositionForHouse(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);

            if(housePosition.isPresent()) {

                Optional<NearestEntity> myNearestWorker =
                        nearestWorkerToBuild(gameHistoryState, currentParsedGameState, housePosition.get(), hSize);

                if(myNearestWorker.isPresent()) {
                    NearestEntity builder = myNearestWorker.get();
                    BuildingBuildCommand bc = BuildingBuildCommand.buildSingleWorker(
                            EntityType.HOUSE, builder.getSourceCell().getEntityId(), builder.getThisCell().getPosition(),
                            housePosition.get(),  builder.getPathLenEmptyCellsToThisCell(), currentParsedGameState);

                    gameHistoryState.addBuildCommand(bc);
                }
            }
        }
    }




}
