package hipravin.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hipravin.model.Position2d.of;
import static org.junit.jupiter.api.Assertions.*;

class Position2dUtilsTest {

    @Test
    void testIntersection() {

        assertDontHaveSpace(of(1,1), 1, of(2,2), 1);
        assertDontHaveSpace(of(2,2), 1, of(1,1), 1);
        assertDontHaveSpace(of(0,0), 5, of(5,3), 3);
        assertDontHaveSpace(of(5,3), 5, of(0,0), 3);
        assertHaveSpace(of(5,4), 5, of(0,0), 3);
    }

    void assertHaveSpace(Position2d c1, int size1, Position2d c2, int size2) {
        assertTrue(Position2dUtil.buildingsHaveSpaceInBetween(c1, size1, c2, size2));
    }
    void assertDontHaveSpace(Position2d c1, int size1, Position2d c2, int size2) {
        assertFalse(Position2dUtil.buildingsHaveSpaceInBetween(c1, size1, c2, size2));
    }

    @Test
    void testEdges() {

        Set<Position2d> edge = Position2dUtil.squareEdgeWithCorners(of(1,1), 5);
        assertEquals(4 * 4, edge.size());

        Set<Position2d> outerEdge = Position2dUtil.buildingOuterEdgeWithoutCorners(of(1,1), 5);
        assertEquals(20, outerEdge.size());
        Set<Position2d> outerEdgeWith = Position2dUtil.buildingOuterEdgeWithCorners(of(1,1), 5);
        assertEquals(24, outerEdgeWith.size());

        Set<Position2d> combined = new HashSet<>();
        combined.addAll(edge);
        combined.addAll(outerEdge);

        assertEquals(36, combined.size());
    }
    @Test
    void testEdge2() {
        //house on map edge

        Set<Position2d> edge = Position2dUtil.squareEdgeWithCorners(of(0,0), 3);
        assertEquals(8, edge.size());

        Set<Position2d> outerEdge = Position2dUtil.buildingOuterEdgeWithoutCorners(of(0,0), 3);
        assertEquals(6, outerEdge.size());

        Set<Position2d> combined = new HashSet<>();
        combined.addAll(edge);
        combined.addAll(outerEdge);

        assertEquals(14, combined.size());
    }
    @Test
    void testEdgeWall() {
        Set<Position2d> edge = Position2dUtil.squareEdgeWithCorners(of(1,1), 1);
        assertEquals(1, edge.size());

        Set<Position2d> outerEdge = Position2dUtil.buildingOuterEdgeWithoutCorners(of(1,1), 1);
        assertEquals(4, outerEdge.size());

        Set<Position2d> combined = new HashSet<>();
        combined.addAll(edge);
        combined.addAll(outerEdge);

        assertEquals(5, combined.size());
    }

    @Test
    void testClosePositionsIterator() {
        long totalCells = Position2dUtil.MAP_SIZE * Position2dUtil.MAP_SIZE;
        assertEquals(totalCells, Position2dUtil.closeToPositionWideSearchStream(of(0,0)).distinct().count());
        assertEquals(totalCells, Position2dUtil.closeToPositionWideSearchStream(of(10,0)).distinct().count());
        assertEquals(totalCells, Position2dUtil.closeToPositionWideSearchStream(of(10,20)).distinct().count());
        assertEquals(totalCells, Position2dUtil.closeToPositionWideSearchStream(of(79,79)).distinct().count());
        assertEquals(totalCells, Position2dUtil.closeToPositionWideSearchStream(of(79,1)).distinct().count());
        assertEquals(totalCells, Position2dUtil.closeToPositionWideSearchStream(of(50,50)).distinct().count());
    }

    @Test
    void testCloseToCornerComparator() {
        long cnt = Position2dUtil.squareInclusiveCornerStream(of(0, 0), 5).get()
                .sorted(Position2dUtil.distanceToMyCornerComparator())
//                .peek(System.out::println)
                .count();

        assertEquals(25, cnt);
    }

    @Test
    void testSquareConsistency() {
        for (int x = 0; x < Position2dUtil.MAP_SIZE; x++) {
            for (int y = 0; y < Position2dUtil.MAP_SIZE; y++) {

                for (int size = 1; size <= Cell.MAX_FP_SIZE; size++) {

                    Optional<List<Position2d>> squareListOptional = Position2dUtil.squareInclusiveCorner(of(x, y), size);
                    Optional<Stream<Position2d>> squareStreamOptional = Position2dUtil.squareInclusiveCornerStream(of(x, y), size);

                    assertEquals(squareListOptional.isPresent(), squareStreamOptional.isPresent());

                    squareListOptional.ifPresent(position2ds ->
                            assertEquals(new HashSet<>(position2ds), squareStreamOptional.get().collect(Collectors.toSet())));
                }
            }
        }
    }

    @Test
    void testSquare5() {
        Set<Position2d> pss = new HashSet<>(Position2dUtil.squareInclusiveCorner(of(1, 1), 5).get());
        assertEquals(25, pss.size());
    }

    @Test
    void testSquare1() {
        Set<Position2d> pss = new HashSet<>(Position2dUtil.squareInclusiveCorner(of(1, 1), 1).get());
        assertEquals(1, pss.size());
    }
}