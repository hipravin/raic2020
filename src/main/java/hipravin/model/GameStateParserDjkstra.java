package hipravin.model;

import hipravin.strategy.StrategyParams;
import model.Entity;
import model.EntityType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameStateParserDjkstra {//wide search actually

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

        for (int len = 1; len < StrategyParams.NEAREST_MINERALS_COMPUTE_PATH_LIMIT && !currentSet.isEmpty(); len++) {

            for (Position2d curPathLenPos : currentSet) {
                final int pathLen = len;
                Stream<Position2d> neighbourstoAdd =
                        Position2dUtil.upRightLeftDown(curPathLenPos)
                                .filter(p -> !visited.contains(p))
                                .filter(p -> pgs.at(p).isEmpty)
                                .filter(p -> pgs.at(p).nearestMineralField == null || pgs.at(p).nearestMineralField.pathLenEmptyCellsToThisCell > pathLen);
                neighbourstoAdd.forEach(p -> {
                    visited.add(p);
                    Cell atP  = pgs.at(p);
                    atP.nearestMineralField = pgs.at(curPathLenPos).nearestMineralField.pathPlus1(atP);

                    nextCurrentSet.add(p);
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


        for (int len = 1; len < StrategyParams.NEAREST_WORKERS_COMPUTE_PATH_LIMIT && !currentSet.isEmpty(); len++) {

            for (Position2d curPathLenPos : currentSet) {
                final int pathLen = len;
                Stream<Position2d> neighbourstoAdd =
                        Position2dUtil.upRightLeftDown(curPathLenPos)
                                .filter(p -> !visited.contains(p))
                                .filter(p -> pgs.at(p).isEmpty)
                                .filter(p -> pgs.at(p).myNearestWorker == null || pgs.at(p).myNearestWorker.pathLenEmptyCellsToThisCell > pathLen);
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
