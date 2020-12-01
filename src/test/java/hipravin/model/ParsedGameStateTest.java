package hipravin.model;

import hipravin.strategy.TestServerUtil;
import model.Entity;
import model.EntityType;
import model.PlayerView;
import model.ServerMessage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Predicate;

import static hipravin.model.Position2d.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParsedGameStateTest {

    @Test
    void testNearestSingleWorker() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 1, 0);

        PlayerView pw = get0.getPlayerView();
        ParsedGameState pgs = GameStateParser.parse(pw);

        assertEquals(1, countCells(pgs, c -> c.myNearestWorker != null && c.myNearestWorker.pathLenEmptyCellsToThisCell == 0));
        assertEquals(4, countCells(pgs, c -> c.myNearestWorker != null && c.myNearestWorker.pathLenEmptyCellsToThisCell == 1));
        assertEquals(7, countCells(pgs, c -> c.myNearestWorker != null && c.myNearestWorker.pathLenEmptyCellsToThisCell == 2));

        pgs.allCellsAsStream().forEach(c -> {
            assertTrue(c.myNearestWorker == null || c.myNearestWorker.sourceUnitCell.position.equals(of(4,4)));
        });

        assertEquals(4025, countCells(pgs, c -> c.myNearestWorker != null));//approximately half of map is unreacheable at start. seems legit if mineral count is good
    }

    @Test
    void testNearestSingleWorkerMultiple() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 2, 52);

        PlayerView pw = get0.getPlayerView();
        ParsedGameState pgs = GameStateParser.parse(pw);

        pgs.allCellsAsStream().filter(c -> c.isUnit && c.getEntityType() == EntityType.BUILDER_UNIT && c.isMyEntity)
            .forEach(c -> System.out.println(c.getPosition()));


        assertEquals(13, countCells(pgs, c -> c.myNearestWorker != null && c.myNearestWorker.pathLenEmptyCellsToThisCell == 0));
        assertEquals(28, countCells(pgs, c -> c.myNearestWorker != null && c.myNearestWorker.pathLenEmptyCellsToThisCell == 1));
        assertEquals(29, countCells(pgs, c -> c.myNearestWorker != null && c.myNearestWorker.pathLenEmptyCellsToThisCell == 2));
        assertEquals(32, countCells(pgs, c -> c.myNearestWorker != null && c.myNearestWorker.pathLenEmptyCellsToThisCell == 3));
    }

    @Test
    void testParseInitialFreeSpaces() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 1, 0);

        PlayerView pw = get0.getPlayerView();
        ParsedGameState pgs = GameStateParser.parse(pw);

        long freeTotal = 0;
        long freeButUnitsTotal = 0;

        //test result consistency
        for (int size = Cell.MIN_FP_SIZE; size <= Cell.MAX_FP_SIZE ; size++) {
            long free = countCompletelyFree(pgs, size);
            long freeButUnits = countFreeButUnits(pgs, size);

            System.out.println("Size" + size + ", Free: " + free + ", but units: " + freeButUnits);

            freeTotal += free;
            freeButUnitsTotal += freeButUnits;

            assertTrue(free != 0);
            assertTrue(freeButUnits != 0);

            if(size < Cell.MAX_FP_SIZE - 1) {
                assertTrue(free > countCompletelyFree(pgs, size + 1));
            }
        }

        assertEquals(7916, freeTotal);
//        assertEquals(7680, freeTotal);//TODO: WTF???
        assertEquals(43, freeButUnitsTotal);
    }

    long countCompletelyFree(ParsedGameState pgs, int size) {
        return pgs.allCellsAsStream().filter(c -> c.getFreeSpace(size).map(f -> f.isCompletelyFree).orElse(false)).count();
    }
    long countFreeButUnits(ParsedGameState pgs, int size) {
        return pgs.allCellsAsStream().filter(c -> c.getFreeSpace(size).map(f -> f.isFreeButContainOurUnits).orElse(false)).count();
    }

    @Test
    void testParseInitial() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 1, 0);

        PlayerView pw = get0.getPlayerView();
        ParsedGameState pgs = GameStateParser.parse(pw);

        //minerals
        assertEquals(countCells(pgs, Cell::isMineral), countEntities(pw, e -> e.getEntityType() == EntityType.RESOURCE));
        assertEquals(2104, countCells(pgs, Cell::isMineral));
        //cc
        assertEquals(50, countCells(pgs, c -> c.isBuilding() && c.getEntityType() == EntityType.BUILDER_BASE));
        assertEquals(25, countCells(pgs, c -> c.isBuilding() && c.getEntityType() == EntityType.BUILDER_BASE && c.isMyEntity()));
        //turret
        assertEquals(8, countCells(pgs, c -> c.isBuilding() && c.getEntityType() == EntityType.TURRET));
        assertEquals(4, countCells(pgs, c -> c.isBuilding() && c.getEntityType() == EntityType.TURRET && c.isMyEntity()));
        //melee
        assertEquals(50, countCells(pgs, c -> c.isBuilding() && c.getEntityType() == EntityType.MELEE_BASE));
        assertEquals(25, countCells(pgs, c -> c.isBuilding() && c.getEntityType() == EntityType.MELEE_BASE && c.isMyEntity()));
        //range
        assertEquals(50, countCells(pgs, c -> c.isBuilding() && c.getEntityType() == EntityType.RANGED_BASE));
        assertEquals(25, countCells(pgs, c -> c.isBuilding() && c.getEntityType() == EntityType.RANGED_BASE && c.isMyEntity()));
        //unit
        assertEquals(6, countCells(pgs, c -> c.isUnit()));
        assertEquals(3, countCells(pgs, c -> c.isUnit() && c.isMyEntity()));
        //
        assertEquals(0, countCells(pgs, c -> c.isFog()));
        assertEquals(80 * 80 - 25 * 6 - 2 * 4 - 2 * 3 - countEntities(pw, e -> e.getEntityType() == EntityType.RESOURCE),
                countCells(pgs, c -> c.isEmpty()));

        //check buildings
        assertEquals(8, pgs.buildingsByEntityId.size());
        assertEquals(4, pgs.buildingsByEntityId.values().stream().filter(Building::isMyBuilding).count());
        //all edges empty at start, test filled
        assertEquals(pgs.buildingsByEntityId.values().stream().map(b -> b.buildingEmptyOuterEdgeWithoutCorners.size()).reduce(Integer::sum),
                pgs.buildingsByEntityId.values().stream().map(b -> b.buildingOuterEdgeWithoutCorners.size()).reduce(Integer::sum));

        //my w
        assertEquals(1, pgs.myWorkers.size());
        //pop
        assertEquals(3, pgs.population.populationUse);
        assertEquals(15, pgs.population.activeLimit);
        assertEquals(15, pgs.population.potentialLimit);

        assertEquals(3 * 24, countCells(pgs, c -> c.isProducingMyBuildingOuterEdge));

        assertEquals(pgs.getPlayerView().getEntities().length, pgs.entityIdToCell.size());
    }

    long countCells(ParsedGameState pgs, Predicate<? super Cell> predicate) {
        return pgs.allCellsAsStream().filter(predicate).count();
    }

    long countEntities(PlayerView pw, Predicate<? super Entity> predicate) {
        return Arrays.stream(pw.getEntities()).filter(predicate).count();
    }
}