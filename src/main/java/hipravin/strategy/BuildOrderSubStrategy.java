package hipravin.strategy;

import hipravin.model.Building;
import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import model.BuildAction;
import model.EntityAction;
import model.EntityType;
import model.MoveAction;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * builds units and buildings
 */
public class BuildOrderSubStrategy implements SubStrategy {
    private BuidingPosititioningLogic buildingPostitioningLogic = new BuidingPosititioningLogic();

    @Override
    public void decide(GameHistoryState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {


        stubBuildWorkersNonStop(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
        stubBuildHouses(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }

    public void stubBuildWorkersNonStop(GameHistoryState gameHistoryState, ParsedGameState currentParsedGameState,
                                        StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        List<Building> ccs = currentParsedGameState.findMyBuildings(EntityType.BUILDER_BASE);

        for (Building cc : ccs) {
            EntityAction buildWorker = new EntityAction();

            Optional<Position2d> randomPositionToBuildWorker = cc.getBuildingEmptyOuterEdgeWithoutCorners().stream()
                    .reduce((p1, p2) -> p1.hashCode() < p2.hashCode() ? p1 : p2);

            if(randomPositionToBuildWorker.isPresent()) {

                BuildAction ba = new BuildAction(EntityType.BUILDER_UNIT, randomPositionToBuildWorker.get().toVec2dInt());
                buildWorker.setBuildAction(ba);

                assignedActions.put(cc.getId(), new ValuedEntityAction(0.5, cc.getId(), buildWorker));
            }
        }
    }

    public void stubBuildHouses(GameHistoryState gameHistoryState, ParsedGameState currentParsedGameState,
                                          StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        int inAdvance = 10;

        int hCost = currentParsedGameState.getPlayerView().getEntityProperties().get(EntityType.HOUSE).getCost();
        int res = currentParsedGameState.getMyPlayer().getResource();

        if (currentParsedGameState.getPopulation().getPotentialLimit() - currentParsedGameState.getPopulation().getPopulationUse() < inAdvance
              && res >= hCost) {
            Optional<Position2d> housePosition = buildingPostitioningLogic.bestPositionForHouse(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);

            Optional<Cell> worker = stubSelectFirstWorker(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);

            if(housePosition.isPresent() && worker.isPresent()) {

                EntityAction entityAction = new EntityAction();

                if(worker.get().getPosition().equals(Position2d.of(0,3))) {
                    BuildAction buildAction = new BuildAction(EntityType.HOUSE, Position2d.of(0,0).toVec2dInt());
                    //
                    entityAction.setBuildAction(buildAction);

                } else {
                    MoveAction moveAction = new MoveAction(Position2d.of(0,3).toVec2dInt(), false, false);
                    entityAction.setMoveAction(moveAction);
                }
                assignedActions.put(worker.get().getEntityId(), new ValuedEntityAction(0.5, worker.get().getEntityId(), entityAction));

//                BuildAction buildAction = new BuildAction(EntityType.HOUSE, housePosition.get().toVec2dInt());
//                //
//                entityAction.setBuildAction(buildAction);
//                assignedActions.put(worker.get().getEntityId(), new ValuedEntityAction(0.5, worker.get().getEntityId(), entityAction));
            }
        }
    }

    public Optional<Cell> stubSelectFirstWorker(GameHistoryState gameHistoryState, ParsedGameState currentParsedGameState,
                                                StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        return currentParsedGameState.getMyWorkers().values().stream().findFirst();
    }



}
