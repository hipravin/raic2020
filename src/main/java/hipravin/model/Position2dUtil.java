package hipravin.model;

import hipravin.strategy.StrategyParams;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class Position2dUtil {
    public static Position2d MY_CORNER = Position2d.of(0, 0);
    public static Position2d MY_CC = Position2d.of(5, 5);
    public static int MAP_SIZE = 80;

    public static int FIELD_MIN_X = 0;
    public static int FIELD_MIN_Y = 0;

    public static int HOUSE_SIZE = 3;
    public static int CC_SIZE = 5;
    public static int MELEE_BASE_SIZE = 5;
    public static int RANGED_BASE_SIZE = 5;
    public static int TURRET_SIZE = 2;
    public static int WALL_SIZE = 1;

    public static boolean isMapMyCornerPosition(Position2d pos) {
        return pos.x < StrategyParams.MAP_CORNER_SIZE && pos.y < StrategyParams.MAP_CORNER_SIZE;
    }

    public static Stream<Position2d> housePositionsThatWorkerCanBuildIfNoObstacle(Position2d wp) {
        return Stream.of(
                wp.shift(-3, -2),
                wp.shift(-3, -1),
                wp.shift(-3, 0),
                wp.shift(1, -2),
                wp.shift(1, -1),
                wp.shift(1, 0),
                wp.shift(-2, -3),
                wp.shift(-1, -3),
                wp.shift(0, -3),
                wp.shift(-2, 1),
                wp.shift(-1, 1),
                wp.shift(0, 1)
        ).filter(p -> isSquareWithinMapBorder(p, HOUSE_SIZE));
    }

    public static Stream<Position2d> housePositionsThatAreBlockedByWorker(Position2d wp) {
        return Stream.of(
                wp.shift(-2, -2),
                wp.shift(-2, -1),
                wp.shift(-2, 0),
                wp.shift(-1, -2),
                wp.shift(-1, -1),
                wp.shift(-1, 0),
                wp.shift(0, -2),
                wp.shift(0, -1),
                wp.shift(0, 0)
        ).filter(p -> isSquareWithinMapBorder(p, HOUSE_SIZE));
    }

    public static Set<Position2d> housesThatCanBeBuildByTwoWorkers(List<Position2d> workerPositions) {
        Set<Position2d> result = new HashSet<>();

        Set<Position2d> candidates = new HashSet<>();

        Set<Position2d> allBlockedPositions = workerPositions.stream()
                .flatMap(Position2dUtil::housePositionsThatAreBlockedByWorker).collect(Collectors.toSet());

        workerPositions.forEach(wp -> {
            housePositionsThatWorkerCanBuildIfNoObstacle(wp)
                    .filter(bp -> !allBlockedPositions.contains(bp)).forEach(bp -> {
                if (candidates.contains(bp)) {
                    result.add(bp);
                }
                candidates.add(bp);
            });
        });

        return result;
    }

    public static Stream<Position2d> upRightLeftDown(Position2d p) {
        return Stream.of(
                p.shift(0, 1),
                p.shift(1, 0),
                p.shift(-1, 0),
                p.shift(0, -1))
                .filter(Position2dUtil::isPositionWithinMapBorder);

    }

    public static boolean buildingsHaveSpaceInBetween(Position2d corner1, int size1, Position2d corner2, int size2) {
        boolean xOk;
        boolean yOk;

        if (corner1.x < corner2.x) {
            xOk = corner1.x + size1 < corner2.x;
        } else {
            xOk = corner2.x + size2 < corner1.x;
        }

        if (corner1.y < corner2.y) {
            yOk = corner1.y + size1 < corner2.y;
        } else {
            yOk = corner2.y + size2 < corner1.y;
        }

        return xOk || yOk;
    }

    public static Set<Position2d> squareEdgeWithCorners(Position2d corner, int size) {
        Set<Position2d> result = new HashSet<>();

        for (int i = 0; i < size; i++) {
            result.add(corner.shift(i, 0));
            result.add(corner.shift(0, i));
            result.add(corner.shift(i, size - 1));
            result.add(corner.shift(size - 1, i));

        }
        result.removeIf(p -> !isPositionWithinMapBorder(p));
        return result;
    }

    public static Set<Position2d> buildingOuterEdgeWithCorners(Position2d corner, int size) {
        return squareEdgeWithCorners(corner.shift(-1, -1), size + 2);
    }

    public static Set<Position2d> buildingOuterEdgeWithoutCorners(Position2d corner, int size) {
        Set<Position2d> outerSquare = buildingOuterEdgeWithCorners(corner, size);
        outerSquare.remove(corner.shift(-1, -1));
        outerSquare.remove(corner.shift(-1, size));
        outerSquare.remove(corner.shift(size, size));
        outerSquare.remove(corner.shift(size, -1));

        return outerSquare;
    }

    public static Stream<Position2d> closeToPositionWideSearchStream(Position2d startPosition) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new PositionsDistanceOrderedIterator(startPosition), Spliterator.ORDERED), false);
    }

    public static Comparator<Position2d> distanceToMyCornerComparator() {
        return distanceToPositionComparator(MY_CORNER);
    }

    public static Comparator<Position2d> distanceToPositionComparator(Position2d to) {
        Comparator<Position2d> lenComparator = Comparator
                .comparingLong(p -> p.lenShiftSum(to));
        return lenComparator
                .thenComparing(Position2d::getX)
                .thenComparing(Position2d::getY);
    }


    public static Optional<List<Position2d>> squareInclusiveCorner(Position2d minCorner, int size) {
        if (!isSquareWithinMapBorder(minCorner, size)) {
            return Optional.empty();
        }

        List<Position2d> positions = new ArrayList<>(size * size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                positions.add(minCorner.shift(i, j));
            }
        }
        return Optional.of(positions);
    }

    public static Optional<Stream<Position2d>> squareInclusiveCornerStream(Position2d minCorner, int size) {
        if (!isSquareWithinMapBorder(minCorner, size)) {
            return Optional.empty();
        }

        int maxX = minCorner.x + size;
        int maxY = minCorner.y + size;

        return Optional.of(Stream.iterate(minCorner,
                p -> (p.x < maxX) && (p.y < maxY),
                p -> (p.x < maxX - 1) ? p.right() : p.right().shift(-size, 1)));
    }

    public static boolean isSquareWithinMapBorder(Position2d corner, int size) {
        return isPositionWithinMapBorder(corner) && isPositionWithinMapBorder(corner.shift(size, size));
    }

    public static boolean isPositionWithinMapBorder(Position2d p) {
        return p.x >= FIELD_MIN_X && p.x < MAP_SIZE
                && p.y >= FIELD_MIN_Y && p.y < MAP_SIZE;
    }

    private Position2dUtil() {
    }

    private static class PositionsDistanceOrderedIterator implements Iterator<Position2d> {
        final Position2d startPosition;
        int visited = 0;
        int currentLenToStart = 1;
        Position2d currentStartCircle;
        Position2d currentPosition;
        int diagRoundCounter = 1;

        public PositionsDistanceOrderedIterator(Position2d startPosition) {
            this.startPosition = startPosition;
            this.currentStartCircle = startPosition;
            this.currentPosition = startPosition;
        }

        @Override
        public boolean hasNext() {
            return visited < MAP_SIZE * MAP_SIZE;
        }

        @Override
        public Position2d next() {
            Position2d nextPos = nextUnchecked();
            while (!isPositionWithinMapBorder(nextPos)) {
                nextPos = nextUnchecked();
            }
            return nextPos;
        }

        private Position2d nextUnchecked() {
            Position2d result = currentPosition;

            if (!currentPosition.equals(startPosition)) {
                Position2d nextDiag = currentPosition.diag(diagRoundCounter);
                if (nextDiag.lenShiftSum(startPosition) > currentLenToStart) {
                    diagRoundCounter++;
                    nextDiag = currentPosition.diag(diagRoundCounter);
                }
                if (nextDiag.equals(currentStartCircle)) {
                    currentStartCircle = currentStartCircle.up();
                    currentPosition = currentStartCircle;
                    currentLenToStart++;
                    diagRoundCounter++;
                } else {
                    currentPosition = nextDiag;
                }
            } else {
                currentStartCircle = currentStartCircle.up();
                currentPosition = currentStartCircle;
            }
            if (isPositionWithinMapBorder(result)) {
                visited++;
            }

            return result;
        }
    }

}
