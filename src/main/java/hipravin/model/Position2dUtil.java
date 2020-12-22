package hipravin.model;

import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hipravin.model.Position2d.of;

public abstract class Position2dUtil {
    public static Position2d MY_CORNER = of(0, 0);
    public static Position2d ENEMY_CORNER = of(70, 70);
    public static Position2d MY_CC = of(5, 5);
    public static int TURRET_RANGE = 5;
    public static int RANGER_RANGE = 5;
    public static int WORKER_SIGHT_RANGE = 10;


    public static int MAP_SIZE = 80;

    public static int FIELD_MIN_X = 0;
    public static int FIELD_MIN_Y = 0;

    public static int HOUSE_SIZE = 3;
    public static int CC_SIZE = 5;
    public static int MELEE_BASE_SIZE = 5;
    public static int RANGED_BASE_SIZE = 5;
    public static int TURRET_SIZE = 2;
    public static int WALL_SIZE = 1;

    public static Position2d midPoint(Position2d p1, Position2d p2) {
        if (p1 == null || p2 == null) {
            return null;
        }
        return crop(of((p1.x + p2.x) / 2, (p1.y + p2.y) / 2));
    }


    public static boolean isPositionOutside(Position2d position, int line) {
        return position.x <= line || position.y <= line;
    }

    public static void iterAllPositionsBuildingSightRange(Position2d corner, int buildingSize, int buildingSightRange,
                                                          Consumer<Position2d> positionConsumer) {

        Set<Position2d> edge = squareEdgeWithCorners(corner, buildingSize);

        for (Position2d edgePosition : edge) {
            if (isPositionWithinMapBorder(edgePosition)) {
                iterAllPositionsInRangeInclusive(corner, buildingSightRange, positionConsumer);
            }
        }
    }

    public static Position2d runAwayDoubleDistance(Position2d unit, Position2d runFrom) {
        int dx = runFrom.x - unit.x;
        int dy = runFrom.y - unit.y;

        Position2d runTo = unit.shift(-2 * dx, -2 * dy);

        return crop(runTo);
    }
    public static Position2d runAwayABit(Position2d unit, Position2d runFrom) {
        int dx = runFrom.x - unit.x;
        int dy = runFrom.y - unit.y;

        Position2d runTo = unit.shift(-dx / 2, -dy / 2);

        return crop(runTo);
    }

    public static Position2d randomShift(Position2d p, int shift) {
        if (shift == 0) {
            return p;
        }

        int xshift = -shift + GameHistoryAndSharedState.random.nextInt(2 * shift);
        int yshift = -shift + GameHistoryAndSharedState.random.nextInt(2 * shift);

        return crop(p.shift(xshift, yshift));
    }

    public static Position2d crop(Position2d p) {

        int x = Math.max(0, p.x);
        x = Math.min(x, MAP_SIZE - 1);
        int y = Math.max(0, p.y);
        y = Math.min(y, MAP_SIZE - 1);

        return of(x, y);

    }

    public static Position2d limitWayTo(Position2d from, Position2d to, int len) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;

        int asum = Math.abs(dx + dy);

        if (asum == 0) {
            return to;

        }

        double div = (double) len / asum;

        return crop(of((int) (from.x + dx * div), (int) (from.y + dy * div)));
    }

    public static void iterAllPositionsInRangeInclusive(Position2d dotPosition, int range, Consumer<Position2d> positionConsumer) {
        positionConsumer.accept(dotPosition);
        iterAllPositionsInRangeExclusive(dotPosition, range, positionConsumer);
    }

    public static void iterAllPositionsInRangeExclusive(Position2d dotPosition, int range, Consumer<Position2d> positionConsumer) {
        for (int i = 1; i <= range; i++) {
            //inv: abs(xshift) + abs(yshift) = i
            for (int xshift = 0; xshift <= i; xshift++) {
                int yshift = i - xshift;
                Position2d p1 = dotPosition.shift(xshift, yshift);
                Position2d p2 = dotPosition.shift(xshift, -yshift);
                Position2d p3 = dotPosition.shift(-xshift, yshift);
                Position2d p4 = dotPosition.shift(-xshift, -yshift);

                if (isPositionWithinMapBorder(p1)) {
                    positionConsumer.accept(p1);
                }
                if (isPositionWithinMapBorder(p2)) {
                    positionConsumer.accept(p2);
                }
                if (isPositionWithinMapBorder(p3)) {
                    positionConsumer.accept(p3);
                }
                if (isPositionWithinMapBorder(p4)) {
                    positionConsumer.accept(p4);
                }
            }
        }
    }

    public static void iterAllPositionsInExactRange(Position2d dotPosition, int range, Consumer<Position2d> positionConsumer) {
        //inv: abs(xshift) + abs(yshift) = i
        for (int xshift = 0; xshift <= range; xshift++) {
            int yshift = range - xshift;
            Position2d p1 = dotPosition.shift(xshift, yshift);
            Position2d p2 = dotPosition.shift(xshift, -yshift);
            Position2d p3 = dotPosition.shift(-xshift, yshift);
            Position2d p4 = dotPosition.shift(-xshift, -yshift);

            if (isPositionWithinMapBorder(p1)) {
                positionConsumer.accept(p1);
            }
            if (isPositionWithinMapBorder(p2)) {
                positionConsumer.accept(p2);
            }
            if (isPositionWithinMapBorder(p3)) {
                positionConsumer.accept(p3);
            }
            if (isPositionWithinMapBorder(p4)) {
                positionConsumer.accept(p4);
            }
        }
    }


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

    public int[][] UPRIGHTLEFTDOWN_SHIFTS = {
            {-1, 0},
            {1, 0},
            {0, 1},
            {0, -1}
    };

    public static Position2d[] upRightLeftDownWithinMap(Position2d p) {
        if(p.x > 0 && p.y > 0 && p.x < Position2dUtil.MAP_SIZE -1 && p.y < Position2dUtil.MAP_SIZE - 1) {
            return new Position2d[] {p.shift(0, 1), p.shift(1, 0), p.shift(-1, 0), p.shift(0, -1)};
        }

        return  upRightLeftDownFiltered(p, Position2dUtil::isPositionWithinMapBorder).toArray(new Position2d[0]);
    }

    public static List<Position2d> upRightLeftDownFiltered(Position2d p, Predicate<? super Position2d>... filters) {
        List<Position2d> pfiltered = new ArrayList<>(4);

        withingMapBorderAndPassesAllFilters(p.shift(0, 1), pfiltered, filters);
        withingMapBorderAndPassesAllFilters(p.shift(1, 0), pfiltered, filters);
        withingMapBorderAndPassesAllFilters(p.shift(-1, 0), pfiltered, filters);
        withingMapBorderAndPassesAllFilters(p.shift(0, -1), pfiltered, filters);

        return pfiltered;
    }

    public static List<Position2d> upRightLeftDownDiagsFiltered(Position2d p, Predicate<? super Position2d>... filters) {
        List<Position2d> pfiltered = new ArrayList<>(4);

        withingMapBorderAndPassesAllFilters(p.shift(0, 1), pfiltered, filters);
        withingMapBorderAndPassesAllFilters(p.shift(1, 0), pfiltered, filters);
        withingMapBorderAndPassesAllFilters(p.shift(-1, 0), pfiltered, filters);
        withingMapBorderAndPassesAllFilters(p.shift(0, -1), pfiltered, filters);
        withingMapBorderAndPassesAllFilters(p.shift(1, 1), pfiltered, filters);
        withingMapBorderAndPassesAllFilters(p.shift(1, -1), pfiltered, filters);
        withingMapBorderAndPassesAllFilters(p.shift(-1, 1), pfiltered, filters);
        withingMapBorderAndPassesAllFilters(p.shift(-1, -1), pfiltered, filters);

        return pfiltered;
    }

    public static void withingMapBorderAndPassesAllFilters(Position2d p, List<Position2d> toAdd, Predicate<? super Position2d>... filters) {
        if (!Position2dUtil.isPositionWithinMapBorder(p)) {
            return;
        }
        for (Predicate<? super Position2d> filter : filters) {
            if (!filter.test(p)) {
                return;
            }
        }

        toAdd.add(p);
    }

    public static Position2d randomMapPosition() {
        //but not in center
        int genEdge = Position2dUtil.MAP_SIZE / 5;

        int x = GameHistoryAndSharedState.random.nextInt(2) % 2 == 0
                ? GameHistoryAndSharedState.random.nextInt(genEdge)
                : Position2dUtil.MAP_SIZE - 1 - GameHistoryAndSharedState.random.nextInt(genEdge);
        int y = GameHistoryAndSharedState.random.nextInt(2) % 2 == 0
                ? GameHistoryAndSharedState.random.nextInt(genEdge)
                : Position2dUtil.MAP_SIZE - 1 - GameHistoryAndSharedState.random.nextInt(genEdge);

        return of(x, y);
    }

    public static boolean withingMapBorderAndPassesFilter(Position2d p, Predicate<? super Position2d> filter) {
        return isPositionWithinMapBorder(p) && filter.test(p);
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
        return isPositionWithinMapBorder(corner) && isPositionWithinMapBorder(corner.shift(size - 1, size - 1));
    }

    public static boolean isPositionWithinMapBorder(Position2d p) {
        return p.x >= FIELD_MIN_X && p.x < MAP_SIZE
                && p.y >= FIELD_MIN_Y && p.y < MAP_SIZE;
    }

    public static boolean turretCanAttackPosition(Position2d turretPosition, Position2d position) {
        return turretPosition.lenShiftSum(position) <= TURRET_RANGE
                || turretPosition.shift(0, 1).lenShiftSum(position) <= TURRET_RANGE
                || turretPosition.shift(1, 0).lenShiftSum(position) <= TURRET_RANGE
                || turretPosition.shift(1, 1).lenShiftSum(position) <= TURRET_RANGE;
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
