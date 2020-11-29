package hipravin.model;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class Position2dUtil {
    public static Position2d MY_CORNER = Position2d.of(0, 0);
    public static int MAP_SIZE = 80;

    public static int FIELD_MIN_X = 0;
    public static int FIELD_MIN_Y = 0;

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

    static boolean isSquareWithinMapBorder(Position2d corner, int size) {
        return isPositionWithinMapBorder(corner) && isPositionWithinMapBorder(corner.shift(size, size));
    }

    static boolean isPositionWithinMapBorder(Position2d p) {
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
            while(!isPositionWithinMapBorder(nextPos)) {
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
