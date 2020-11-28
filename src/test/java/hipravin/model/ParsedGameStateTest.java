package hipravin.model;

import hipravin.strategy.TestServerUtil;
import model.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParsedGameStateTest {
    @Test
    void testParseInitial() {
        ServerMessage.GetAction get0 = TestServerUtil.readGet(1, 1, 0);

        PlayerView pw = get0.getPlayerView();
        ParsedGameState pgs = ParsedGameState.parse(pw);

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

    }

    long countCells(ParsedGameState pgs, Predicate<? super Cell> predicate) {
        return pgs.allCellsAsStream().filter(predicate).count();
    }

    long countEntities(PlayerView pw, Predicate<? super Entity> predicate) {
        return Arrays.stream(pw.getEntities()).filter(predicate).count();
    }
}