package hipravin.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static hipravin.model.Position2d.of;
import static org.junit.jupiter.api.Assertions.*;

class Position2dTest {

    @Test
    void testPerf() {
        double val1 = 15.0/16;
        double val2 = 45.0/46;


        System.out.println(val2 / val1);
        System.out.println(510/ val1);
        System.out.println(510/ val2);

    }

    @Test
    void diagTest1() {

        Position2d p = of(10,4);

        assertEquals(p, p.diag1().diag2().diag3().diag4());
        assertEquals(p, p.diag(1).diag(2).diag(3).diag(4));


        Set<Position2d> positions = new HashSet<>();
        positions.add(p.diag(1));
        positions.add(p.diag(1).diag(2));
        positions.add(p.diag(1).diag(2).diag(3));
        positions.add(p.diag(1).diag(2).diag(3).diag(4));

        assertEquals(4, positions.size());
    }
}