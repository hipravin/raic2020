package hipravin.strategy;

import hipravin.model.FreeSpace;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import model.EntityType;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static hipravin.model.Position2dUtil.buildingsHaveSpaceInBetween;

public class BuidingPosititioningLogic {
    public Optional<Position2d> bestPositionFor(EntityType entityType, GameHistoryState gameHistoryState, ParsedGameState currentParsedGameState,
                                                StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        return switch (entityType) {
            case HOUSE -> bestPositionForHouse(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
            default -> Optional.empty();
        };
    }

    public Optional<Position2d> bestPositionForHouse(GameHistoryState gameHistoryState, ParsedGameState currentParsedGameState,
                                                StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Stream<Position2d> closeToCorner = Position2dUtil.closeToPositionWideSearchStream(Position2dUtil.MY_CORNER);

        int size = currentParsedGameState.getPlayerView().getEntityProperties().get(EntityType.HOUSE).getSize();

        Optional<Position2d> bestBuildPos = closeToCorner
                .filter(p -> currentParsedGameState.at(p).getFreeSpace(size).map(FreeSpace::isCompletelyFree).orElse(false))
                .filter(p -> doesntTouchProducingBuildings(p, size, currentParsedGameState))
                .findFirst();

        //TODO: no more than N houses

        return bestBuildPos;
    }

    static boolean doesntTouchProducingBuildings(Position2d corner, int size, ParsedGameState currentParsedGameState) {
        return currentParsedGameState.findMyProducingBuildings()
                .stream().allMatch(b ->
                        buildingsHaveSpaceInBetween(corner, size,
                                b.getCornerCell().getPosition(), b.getCornerCell().getBuildingSize()));
    }

}
