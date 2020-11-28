package hipravin.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class Position2dUtilsTest {

    @Test
    void testSquare5() {
        Set<Position2d> pss = new HashSet<>(Position2dUtil.squareInclusiveCorner(Position2d.of(1,1), 5));
        assertEquals(25, pss.size());
    }
    @Test
    void testSquare1() {
        Set<Position2d> pss = new HashSet<>(Position2dUtil.squareInclusiveCorner(Position2d.of(1,1), 1));
        assertEquals(1, pss.size());
    }
}