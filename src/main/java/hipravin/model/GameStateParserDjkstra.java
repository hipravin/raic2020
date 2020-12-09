package hipravin.model;

import hipravin.strategy.StrategyParams;
import model.Entity;
import model.EntityType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * (c)Copypaste 2020 hipravin
 */

public class GameStateParserDjkstra {//wide search actually

    public static Map<Position2d, NearestEntity> shortWideSearch(ParsedGameState pgs, Set<Position2d> additionalNotEmptyCells,
                                                                 Set<Position2d> startPositions,
                                                                 int maxPathLen, boolean ignoreFog) {
        return shortWideSearch(pgs, additionalNotEmptyCells, startPositions, maxPathLen, ignoreFog, false, Set.of());
    }


    public static Map<Position2d, NearestEntity> shortWideSearch(ParsedGameState pgs, Set<Position2d> additionalNotEmptyCells,
                                                  Set<Position2d> startPositions,
                                                  int maxPathLen, boolean ignoreFog, boolean ignoreUnits, Set<Position2d> additionalEmpty) {
        Map<Position2d, NearestEntity> result = new HashMap<>();

        Set<Position2d> visited = new HashSet<>();
        Set<Position2d> currentSet = new HashSet<>(startPositions);

        for (Position2d path0Pos : currentSet) {
            result.put(path0Pos, new NearestEntity(pgs.at(path0Pos), pgs.at(path0Pos), 0));
            visited.add(path0Pos);
        }

        Set<Position2d> nextCurrentSet = new HashSet<>();

        for (int len = 1; len <= maxPathLen && !currentSet.isEmpty(); len++) {

            for (Position2d curPathLenPos : currentSet) {
                final int pathLen = len;

                List<Position2d> neighbourstoAdd = Position2dUtil.upRightLeftDownFiltered(curPathLenPos,
                        Arrays.asList(
                                p -> !visited.contains(p),
                                p -> !result.containsKey(p) || result.get(p).pathLenEmptyCellsToThisCell > pathLen,
                                p -> ignoreFog || pgs.at(p).test(c -> !c.fog || c.isFogEdge)
                        ));

                neighbourstoAdd.forEach(p -> {
                    visited.add(p);
                    Cell atP  = pgs.at(p);
                    result.put(p, result.get(curPathLenPos).pathPlus1(atP));
                    if((pgs.at(p).isEmpty || ignoreUnits && pgs.at(p).isUnit || additionalEmpty.contains(p)) && !additionalNotEmptyCells.contains(p)) {
                        nextCurrentSet.add(p);
                    }
                });
            }
            currentSet.clear();
            currentSet.addAll(nextCurrentSet);
            nextCurrentSet.clear();
        }

        return result;
    }

    //to find closes minerals
    public static void computeClosestMinerals(ParsedGameState pgs) {

        Set<Position2d> visited = new HashSet<>();
        Set<Position2d> currentSet = Arrays.stream(pgs.getPlayerView().getEntities())
                .filter(e -> e.getEntityType() == EntityType.RESOURCE)
                .map(Entity::getPosition)
                .map(Position2d::of)
                .collect(Collectors.toSet());

        for (Position2d path0Pos : currentSet) {
            pgs.at(path0Pos).nearestMineralField = new NearestEntity(pgs.at(path0Pos), pgs.at(path0Pos), 0);
            visited.add(path0Pos);
        }

        Set<Position2d> nextCurrentSet = new HashSet<>();

        for (int len = 1; len <= StrategyParams.NEAREST_MINERALS_COMPUTE_PATH_LIMIT && !currentSet.isEmpty(); len++) {

            for (Position2d curPathLenPos : currentSet) {
                final int pathLen = len;

                List<Position2d> neighbourstoAdd = Position2dUtil.upRightLeftDownFiltered(curPathLenPos,
                        Arrays.asList(
                                p -> !visited.contains(p),
                                p -> pgs.at(p).test(c -> c.isEmpty || c.isMyWorker()),
                                p -> pgs.at(p).nearestMineralField == null || pgs.at(p).nearestMineralField.pathLenEmptyCellsToThisCell > pathLen
                        ));

                neighbourstoAdd.forEach(p -> {
                    visited.add(p);
                    Cell atP  = pgs.at(p);
                    atP.nearestMineralField = pgs.at(curPathLenPos).nearestMineralField.pathPlus1(atP);
                    if(!pgs.at(p).isMyWorker()) {
                        nextCurrentSet.add(p);
                    }
                });
            }
            currentSet.clear();
            currentSet.addAll(nextCurrentSet);
            nextCurrentSet.clear();
        }
    }

    //to find some nearest workers, not all
    public static void computeMyNonUniqueNearestWorkers(ParsedGameState pgs) {

        Set<Position2d> visited = new HashSet<>();
        Set<Position2d> currentSet = pgs.myWorkers.values().stream()
                .map(Cell::getPosition)
                .collect(Collectors.toSet());

        for (Position2d path0Pos : currentSet) {
            pgs.at(path0Pos).myNearestWorker = new NearestEntity(pgs.at(path0Pos), pgs.at(path0Pos), 0);
            visited.add(path0Pos);
        }

        Set<Position2d> nextCurrentSet = new HashSet<>();


        for (int len = 1; len <= StrategyParams.NEAREST_WORKERS_COMPUTE_PATH_LIMIT && !currentSet.isEmpty(); len++) {

            for (Position2d curPathLenPos : currentSet) {
                final int pathLen = len;
                List<Position2d> neighbourstoAdd = Position2dUtil.upRightLeftDownFiltered(curPathLenPos,
                        Arrays.asList(
                                p -> !visited.contains(p),
                                p -> pgs.at(p).isEmpty,
                                p -> pgs.at(p).myNearestWorker == null || pgs.at(p).myNearestWorker.pathLenEmptyCellsToThisCell > pathLen
                        ));

                neighbourstoAdd.forEach(p -> {
                    visited.add(p);
                    Cell atP  = pgs.at(p);
                    atP.myNearestWorker = pgs.at(curPathLenPos).myNearestWorker.pathPlus1(atP);

                    nextCurrentSet.add(p);
                });
            }
            currentSet.clear();
            currentSet.addAll(nextCurrentSet);
            nextCurrentSet.clear();
        }
    }

}
