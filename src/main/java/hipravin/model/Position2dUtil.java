package hipravin.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Position2dUtil {

    public static List<Position2d> squareInclusiveCorner(Position2d minCorner, int size) {
        List<Position2d> positions = new ArrayList<>(size * size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                 positions.add(minCorner.shift(i, j));
            }
        }
        return positions;
    }

    private Position2dUtil() {
    }

}
