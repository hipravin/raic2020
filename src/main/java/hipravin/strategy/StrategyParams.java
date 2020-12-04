package hipravin.strategy;

import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;

import java.util.Set;

public class StrategyParams {
    public static int MAX_VAL = Integer.MAX_VALUE >> 1;

    public static final int NEAREST_WORKERS_COMPUTE_PATH_LIMIT = MAX_VAL;
    public static final int NEAREST_MINERALS_COMPUTE_PATH_LIMIT = MAX_VAL;

    public static final int MAX_COMBINATIONS_BF = 5000;

    public int buildCommandMaxWaitTicks = 5;
    public int autoRepairMaxWaitTicks = 5;



    public int housesCLoseToEachOtherTreshhold = 4; // if 5 houses on map -> next should have space in between



    public Set<Position2d> firstHouseNonDesiredPositions() {
        Position2d cc = Position2dUtil.MY_CC;

        return Set.of(); //better to lock CC entrance than not build a house

//        return Set.of(
//            cc.shift(5,0),
//            cc.shift(5,1),
//            cc.shift(5,2),
//            cc.shift(5,3),
//            cc.shift(5,4),
//            cc.shift(5,5),
//            cc.shift(0,5),
//            cc.shift(1,5),
//            cc.shift(2,5),
//            cc.shift(3,5),
//            cc.shift(4,5),
//            cc.shift(7,6),
//            cc.shift(6,7)
//        );
    }

}
