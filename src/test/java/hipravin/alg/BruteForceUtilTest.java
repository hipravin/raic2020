package hipravin.alg;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class BruteForceUtilTest {

    @Test
    void testCombinations0() {
//        BruteForceUtil.allCombinatonsOf(4,2).forEach(c -> System.out.println(Arrays.toString(c)));
        assertEquals(6, BruteForceUtil.allCombinatonsOf(4,2).count());
    }

    @Test
    void testCombinations() {
        assertEquals(1820, BruteForceUtil.allCombinatonsOf(16,4).count());
        assertEquals(4845, BruteForceUtil.allCombinatonsOf(20,4).count());
    }
    @Test
    void testCombinations2() {
        assertEquals(1, BruteForceUtil.allCombinatonsOf(1,1).count());
        assertEquals(1, BruteForceUtil.allCombinatonsOf(20,20).count());
    }


}