package hipravin.strategy;

import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;

import java.util.Set;

public class StrategyParams {
    public static int MAX_VAL = Integer.MAX_VALUE >> 1;

    public static final int NEAREST_WORKERS_COMPUTE_PATH_LIMIT = 20;
    public static final int NEAREST_MINERALS_COMPUTE_PATH_LIMIT = 20;

    public static final int WORKERS_NEARBY_MAX_PATH = 5;

    public static final int MAX_COMBINATIONS_BF = 2000;
    public static final int FREE_SPACE_COMPUTE_RANGE = 10;

    public static final int MAP_CORNER_SIZE = 10;

    public int buildCommandMaxWaitTicks = 5;
    public int autoRepairMaxWaitTicks = 5;

    public double mapCornerMiningRatio = 3/10.0; //spawn rokers only if workers / minerals < {value}

    public int maxHousesBeforeMandatorySpacing = 5;

    public int getHousesAheadPopulation(int currentPopulation) {
        return 7;
    }

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

    public Set<Position2d> houseNonDesiredPositions() {//try not to block cc outer exit
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
