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

        return checkIfCellsAreBLockingWayOut(positions, gameHistoryState, pgs, strategyParams, true, Set.of())
                || checkIfCellsAreBLockingWayOut(positions, gameHistoryState, pgs, strategyParams, false, Set.of()); //house shouldnt block a way out irrespective of units

    }

    public static boolean checkIfWorkerBlocksWayOut(Position2d workerPosition, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                    StrategyParams strategyParams) {
        return checkIfCellsAreBLockingWayOut(Set.of(workerPosition), gameHistoryState, pgs, strategyParams, false, Set.of(workerPosition));
    }

    static boolean checkIfCellsAreBLockingWayOut(Set<Position2d> positions, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                 StrategyParams strategyParams, boolean ignoreUnits, Set<Position2d> additionalEmpty) {
        if (pgs.findMyBuildings(EntityType.BUILDER_BASE).size() == 0) {
            return false;
        }
        Position2d cc = pgs.findMyBuildings(EntityType.BUILDER_BASE).get(0).getCornerCell().getPosition();

        Set<Position2d> top = new HashSet<>();
        Set<Position2d> right = new HashSet<>();
        for (int i = 0; i < Position2dUtil.CC_SIZE; i++) {
            top.add(cc.shift(i, Position2dUtil.CC_SIZE));
            right.add(cc.shift(Position2dUtil.CC_SIZE, i));
        }

        //check separately if blocks top or right way out
        return checkIfCellsAreBLockingWayOut(positions, top, gameHistoryState, pgs, strategyParams, ignoreUnits, additionalEmpty)
                || checkIfCellsAreBLockingWayOut(positions, right, gameHistoryState, pgs, strategyParams, ignoreUnits, additionalEmpty);
    }

    static boolean checkIfCellsAreBLockingWayOut(Set<Position2d> checkPositions, Set<Position2d> startPositionsOrig, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                 StrategyParams strategyParams, boolean ignoreUnits, Set<Position2d> additionalEmpty) {

        if (pgs.findMyBuildings(EntityType.BUILDER_BASE).size() == 0) {
            return false;
        }
        Set<Position2d> positions = new HashSet<>(checkPositions);
        Set<Position2d> startPositions = new HashSet<>(startPositionsOrig);
        positions.removeAll(startPositions);

        Map<Position2d, NearestEntity> wsWith = GameStateParserDjkstra.shortWideSearch(pgs, positions, startPositions,
                strategyParams.wayOutFindPathLen, false, ignoreUnits, additionalEmpty);

        positions = new HashSet<>(checkPositions);
        startPositions = new HashSet<>(startPositionsOrig);
        positions.removeAll(startPositions);

        Map<Position2d, NearestEntity> wsWithout = GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), startPositions, strategyParams.wayOutFindPathLen,
                false, ignoreUnits, additionalEmpty);

        int areaDiff = wsWithout.size() - 1 - wsWith.size();

        return areaDiff >= strategyParams.wayOutDiffDetectTreshhold
                || areaDiff > strategyParams.wayOutDiffDetectTreshholdMul * wsWithout.size();
    }
}
