package hipravin.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class Position2dUtilsTest {

    @Test
    void testSquareConsistency() {
        for (int x = 0; x < Position2dUtil.MAP_SIZE; x++) {
            for (int y = 0; y < Position2dUtil.MAP_SIZE; y++) {

                for (int size = 1; size <= Cell.MAX_FP_SIZE; size++) {

                    Optional<List<Position2d>> squareListOptional = Position2dUtil.squareInclusiveCorner(Position2d.of(x, y), size);
                    Optional<Stream<Position2d>> squareStreamOptional = Position2dUtil.squareInclusiveCornerStream(Position2d.of(x, y), size);

                    assertEquals(squareListOptional.isPresent(), squareStreamOptional.isPresent());

                    squareListOptional.ifPresent(position2ds ->
                            assertEquals(new HashSet<>(position2ds), squareStreamOptional.get().collect(Collectors.toSet())));
                }
            }
        }
    }

    @Test
    void testSquare5() {
        Set<Position2d> pss = new HashSet<>(Position2dUtil.squareInclusiveCorner(Position2d.of(1,1), 5).get());
        assertEquals(25, pss.size());
    }
    @Test
    void testSquare1() {
        Set<Position2d> pss = new HashSet<>(Position2dUtil.squareInclusiveCorner(Position2d.of(1,1), 1).get());
        assertEquals(1, pss.size());
    }
}