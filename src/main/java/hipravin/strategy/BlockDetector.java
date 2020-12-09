package hipravin.strategy;

import hipravin.model.*;
import model.EntityType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockDetector {
    public static boolean checkIfHouseBlocksWayOut(Position2d position2d, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                   StrategyParams strategyParams) {

        Set<Position2d> positions = new HashSet<>(Position2dUtil.squareInclusiveCorner(position2d, Position2dUtil.HOUSE_SIZE).orElse(Collections.emptyList()));

        return  checkIfCellsAreBLockingWayOut(positions, gameHistoryState, pgs, strategyParams, true, Set.of());

    }
    public static boolean checkIfWorkerBlocksWayOut(Position2d workerPosition, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                    StrategyParams strategyParams) {
        return checkIfCellsAreBLockingWayOut(Set.of(workerPosition), gameHistoryState, pgs, strategyParams, false, Set.of(workerPosition));
    }

    static boolean checkIfCellsAreBLockingWayOut(Set<Position2d> positions, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                 StrategyParams strategyParams, boolean ignoreUnits, Set<Position2d> additionalEmpty) {

        if(pgs.findMyBuildings(EntityType.BUILDER_BASE).size() == 0) {
            return false;
        }

        Set<Position2d> ccOuterEdge = new HashSet<>(pgs.findMyBuildings(EntityType.BUILDER_BASE).get(0)
                .getBuildingOuterEdgeWithoutCorners());

        Map<Position2d, NearestEntity> wsWith = GameStateParserDjkstra.shortWideSearch(pgs, positions, ccOuterEdge,
                strategyParams.wayOutFindPathLen, false, ignoreUnits, additionalEmpty);

        ccOuterEdge = new HashSet<>(pgs.findMyBuildings(EntityType.BUILDER_BASE).get(0)
                .getBuildingOuterEdgeWithoutCorners());

        Map<Position2d, NearestEntity> wsWithout = GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), ccOuterEdge, strategyParams.wayOutFindPathLen,
                false, ignoreUnits, additionalEmpty);


        int areaDiff = wsWithout.size() - 1 - wsWith.size();

        return areaDiff >= strategyParams.wayOutDiffDetectTreshhold;
    }
}
