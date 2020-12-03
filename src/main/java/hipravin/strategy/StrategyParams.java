package hipravin.strategy;

import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;

import java.util.Set;

public class StrategyParams {
    public static final int NEAREST_WORKERS_COMPUTE_PATH_LIMIT = Integer.MAX_VALUE;
    public static final int NEAREST_MINERALS_COMPUTE_PATH_LIMIT = Integer.MAX_VALUE;

    public static final int MAX_COMBINATIONS_BF = 2000;

    public int buildCommandMaxWaitTicks = 5;

    public Set<Position2d> firstHouseNonDesiredPositions() {
        Position2d cc = Position2dUtil.MY_CC;

        return Set.of(
            cc.shift(5,0),
            cc.shift(5,1),
            cc.shift(5,2),
            cc.shift(5,3),
            cc.shift(5,4),
            cc.shift(5,5),
            cc.shift(0,5),
            cc.shift(1,5),
            cc.shift(2,5),
            cc.shift(3,5),
            cc.shift(4,5),
            cc.shift(7,6),
            cc.shift(6,7)
        );
    }

}
