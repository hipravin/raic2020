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
    void testLimitWayTo() {
        assertEquals(of(0,10), Position2dUtil.limitWayTo(of(0,0), of(0,20), 10));
        assertEquals(of(10,0), Position2dUtil.limitWayTo(of(0,0), of(20,0), 10));
        assertEquals(of(10,10), Position2dUtil.limitWayTo(of(5,5), of(50,50), 10));
    }

    @Test
    void testRuinAway() {

        assertEquals(of(15,0), Position2dUtil.runAwayDoubleDistance(of(5,0), of(0,0)));
        assertEquals(of(7,7), Position2dUtil.runAwayDoubleDistance(of(3,3), of(1,1)));
    }

    @Test
    void testRandomInMap() {
        for (int i = 0; i < 10000; i++) {
             assertTrue(Position2dUtil.isPositionWithinMapBorder(Position2dUtil.randomMapPosition()));
        }
    }

    @Test
    void testIterAllPositionsInRange() {
        Set<Position2d> inRange = new HashSet<>();
        Position2d dot = of(40, 40);
        int range = 10;

        Position2dUtil.iterAllPositionsInRangeInclusive(dot, range, inRange::add);

        inRange.forEach(p ->
                assertTrue(p.lenShiftSum(dot) <= range));

        assertEquals(1 + 4 + 8 + 12 + 16 + 20 + 24 + 28 + 32 + 36 + 40, inRange.size());
    }

    @Test
    void testIterAllPositionsInRangeBuilding() {
        Set<Position2d> inRange = new HashSet<>();
        Position2d dot = of(40, 40);
        int range = 5;

        Position2dUtil.iterAllPositionsBuildingSightRange(dot, 5, range, inRange::add);

        inRange.forEach(p ->
                assertTrue(p.lenShiftSum(dot) <= range));

        assertEquals(61, inRange.size());
    }

    @Test
    void testBuildByTwoWorkers() {
        Set<Position2d> houses = Position2dUtil.housesThatCanBeBuildByTwoWorkers(
                List.of(
                        of(0, 3),
                        of(4, 4),
                        of(2, 2)
                ));

        assertEquals(2, houses.size());
        assertTrue(houses.contains(of(1, 3)));
        assertTrue(houses.contains(of(3, 1)));
    }

    @Test
    void testHousesBlockedBy() {
        Position2d worker = of(4, 4);
        assertEquals(9, Position2dUtil.housePositionsThatAreBlockedByWorker(worker).distinct().count());

        Set<Position2d> blocked = Position2dUtil.housePositionsThatAreBlockedByWorker(worker).collect(Collectors.toSet());
        Set<Position2d> allowed = Position2dUtil.housePositionsThatWorkerCanBuildIfNoObstacle(worker).collect(Collectors.toSet());

        int size = blocked.size();
        blocked.addAll(allowed);
        assertEquals(size + allowed.size(), blocked.size());
    }

    @Test
    void testHousesCanBuild() {
        Position2d worker = of(10, 11);
        assertEquals(12, Position2dUtil.housePositionsThatWorkerCanBuildIfNoObstacle(worker).distinct().count());

        Position2dUtil.housePositionsThatWorkerCanBuildIfNoObstacle(worker)
                .forEach(corner -> {
                    assertTrue(Position2dUtil.buildingOuterEdgeWithoutCorners(corner, Position2dUtil.HOUSE_SIZE).contains(worker));
                });
    }

    @Test
    void testHousesCanBuild2() {
        Position2d worker = of(1, 1);

        Position2dUtil.housePositionsThatWorkerCanBuildIfNoObstacle(worker)
                .forEach(corner -> {
                    assertTrue(Position2dUtil.buildingOuterEdgeWithoutCorners(corner, Position2dUtil.HOUSE_SIZE).contains(worker));
                });
    }

    @Test
    void testNeighbours() {
        Position2d p = of(1, 1);
        Position2dUtil.upRightLeftDown(p).forEach(p2 ->
                assertEquals(1, p2.lenShiftSum(p)));

        assertEquals(4, Position2dUtil.upRightLeftDown(p).distinct().count());
    }

    @Test
    void testIntersection() {
        assertHaveSpace(of(5, 5), 5, of(0, 5), 3);
        assertHaveSpace(of(0, 5), 3, of(5, 5), 5);


        assertDontHaveSpace(of(1, 1), 1, of(2, 2), 1);
        assertDontHaveSpace(of(2, 2), 1, of(1, 1), 1);
        assertDontHaveSpace(of(0, 0), 5, of(5, 3), 3);
        assertHaveSpace(of(5, 3), 5, of(0, 0), 3);
        assertHaveSpace(of(5, 4), 5, of(0, 0), 3);

    }

    void assertHaveSpace(Position2d c1, int size1, Position2d c2, int size2) {
        assertTrue(Position2dUtil.buildingsHaveSpaceInBetween(c1, size1, c2, size2));
    }

    void assertDontHaveSpace(Position2d c1, int size1, Position2d c2, int size2) {
        assertFalse(Position2dUtil.buildingsHaveSpaceInBetween(c1, size1, c2, size2));
    }

    @Test
    void testEdges() {

        Set<Position2d> edge = Position2dUtil.squareEdgeWithCorners(of(1, 1), 5);
        assertEquals(4 * 4, edge.size());

        Set<Position2d> outerEdge = Position2dUtil.buildingOuterEdgeWithoutCorners(of(1, 1), 5);
        assertEquals(20, outerEdge.size());
        Set<Position2d> outerEdgeWith = Position2dUtil.buildingOuterEdgeWithCorners(of(1, 1), 5);
        assertEquals(24, outerEdgeWith.size());

        Set<Position2d> combined = new HashSet<>();
        combined.addAll(edge);
        combined.addAll(outerEdge);

        assertEquals(36, combined.size());
    }

    @Test
    void testEdge2() {
        //house on map edge

        Set<Position2d> edge = Position2dUtil.squareEdgeWithCorners(of(0, 0), 3);
        assertEquals(8, edge.size());

        Set<Position2d> outerEdge = Position2dUtil.buildingOuterEdgeWithoutCorners(of(0, 0), 3);
        assertEquals(6, outerEdge.size());

        Set<Position2d> combined = new HashSet<>();
        combined.addAll(edge);
        combined.addAll(outerEdge);

        assertEquals(14, combined.size());
    }

    @Test
    void testEdgeWall() {
        Set<Position2d> edge = Position2dUtil.squareEdgeWithCorners(of(1, 1), 1);
        assertEquals(1, edge.size());

        Set<Position2d> outerEdge = Position2dUtil.buildingOuterEdgeWithoutCorners(of(1, 1), 1);
        assertEquals(4, outerEdge.size());

        Set<Position2d> combined = new HashSet<>();
        combined.addAll(edge);
        combined.addAll(outerEdge);

        assertEquals(5, combined.size());
    }

    @Test
    void testClosePositionsIterator() {
        long totalCells = Position2dUtil.MAP_SIZE * Position2dUtil.MAP_SIZE;
        assertEquals(totalCells, Position2dUtil.closeToPositionWideSearchStream(of(0, 0)).distinct().count());
        assertEquals(totalCells, Position2dUtil.closeToPositionWideSearchStream(of(10, 0)).distinct().count());
        assertEquals(totalCells, Position2dUtil.closeToPositionWideSearchStream(of(10, 20)).distinct().count());
        assertEquals(totalCells, Position2dUtil.closeToPositionWideSearchStream(of(79, 79)).distinct().count());
        assertEquals(totalCells, Position2dUtil.closeToPositionWideSearchStream(of(79, 1)).distinct().count());
        assertEquals(totalCells, Position2dUtil.closeToPositionWideSearchStream(of(50, 50)).distinct().count());
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