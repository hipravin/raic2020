package hipravin.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class Position2dUtil {
    public static int MAP_SIZE = 80;

    public static int FIELD_MIN_X = 0;
    public static int FIELD_MIN_Y = 0;
    public static int FIELD_MAX_X = MAP_SIZE - 1;
    public static int FIELD_MAX_Y = MAP_SIZE - 1;


    public static Optional<List<Position2d>> squareInclusiveCorner(Position2d minCorner, int size) {
        if(!isSquareWithinMapBorder(minCorner, size)) {
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
        if(!isSquareWithinMapBorder(minCorner, size)) {
            return Optional.empty();
        }

        int maxX = minCorner.x + size;
        int maxY = minCorner.y + size;

        return Optional.of(Stream.iterate(minCorner,
                p -> (p.x < maxX) && (p.y < maxY),
                p -> (p.x < maxX - 1) ? p.right() : p.right().shift(-size , 1)));
    }

    static boolean isSquareWithinMapBorder(Position2d corner, int size) {
        return isPositionWithinMapBorder(corner) && isPositionWithinMapBorder(corner.shift(size, size));
    }

    static boolean isPositionWithinMapBorder(Position2d p) {
        return p.x >= FIELD_MIN_X && p.x < FIELD_MAX_X
                && p.y >= FIELD_MIN_Y && p.y < FIELD_MAX_Y;
    }

    private Position2dUtil() {
    }

}
