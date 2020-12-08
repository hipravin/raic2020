package hipravin.strategy;

import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import model.EntityType;

import java.util.Map;
import java.util.Set;

public class StrategyParams {
    public static int MAX_VAL = Integer.MAX_VALUE >> 1;

    public static final int NEAREST_WORKERS_COMPUTE_PATH_LIMIT = 20;
    public static final int NEAREST_MINERALS_COMPUTE_PATH_LIMIT = 20;

    public static final int HOUSE_WORKERS_NEARBY_MAX_PATH = 5;
    public static final int BARRACK_WORKERS_NEARBY_MAX_PATH = 7;
    public static final int BARRACK_WORKERS_NEARBY_MAX_PATH_CENTER = 15;

    public static final int MAX_COMBINATIONS_BF = 2000;
    public static final int FREE_SPACE_COMPUTE_RANGE = 10;

    public static final int MAP_CORNER_SIZE = 10;

    public int buildCommandMaxWaitTicks = 5;
    public int autoRepairMaxWaitTicks = 5;

    public double mapCornerMiningRatio = 3/10.0; //spawn rokers only if workers / minerals < {value}

    public int maxHousesBeforeMandatorySpacing = 5;

    public int maxSpawnToMineralsRememberCount = 5;

    public int leftCornerSpacingDoesntMatterXPlusy = 7;

    public double bestMineralSpawnProb = 1.0;
    public double worstMineralSpawnProb = 0.2;
    public int switchToAutoMineRange = 4;

//    public int populationOfWorkersToBuildBeforeRangers = 60;
    public int populationOfWorkersToBuildBeforeRangers = 50;
    public int populationOfWorkersToBuildAfterRangers = 60;

    public int maxNumberOfRangers = 100;

    public int barrackAheadBuildResourceTick = 4;

    public int magnetMaxToSinglePoint = 2;

    public int numberOfRandomScoutChoices = 20;

    public EntityType[] rangerDefaultAttackTargets = new EntityType[]{EntityType.RANGED_UNIT, EntityType.MELEE_UNIT, EntityType.BUILDER_UNIT, EntityType.TURRET, EntityType.HOUSE, EntityType.RANGED_BASE,
            EntityType.MELEE_BASE, EntityType.BUILDER_BASE, EntityType.WALL};


    public Map<EntityType, Integer> magnetRepairRanges = Map.of(
            EntityType.HOUSE, 2,
            EntityType.RANGED_BASE, 20,
            EntityType.BUILDER_BASE, 20,
            EntityType.MELEE_BASE, 20,
            EntityType.TURRET, 15,
            EntityType.WALL, 1
    );

    public Map<EntityType, Integer> magnetRepairDesiredWorkers = Map.of(
            EntityType.HOUSE, 5, //2
            EntityType.BUILDER_BASE, 20,
            EntityType.RANGED_BASE, 20,
            EntityType.MELEE_BASE, 20,
            EntityType.TURRET, 8,
            EntityType.WALL, 1
    );

    public boolean sendToCenter = true;
    public Set<Integer> sendToCenterWorkerNumbers = Set.of(15, 16,17,18,19,20,  30,31,32,33,34);

    public int minHouseDistanceToCenter = 12;
    public double rangerScoutRateProb = 0.1;

    public boolean ifRandom(double prob) {
        return GameHistoryAndSharedState.random.nextDouble() < prob;
    }

    public Position2d randomScoutPointNearEnemy() {

        int x = Position2dUtil.MAP_SIZE - 5 - GameHistoryAndSharedState.random.nextInt(30);
        int y = Position2dUtil.MAP_SIZE - 5 - GameHistoryAndSharedState.random.nextInt(30);
        return Position2d.of(x,y);
    }


    public int getHousesAheadPopulationBeforeRangers(int currentPopulation) {
        return 7;
    }
    public int getHousesAheadPopulationWhenBuildingRangers(int currentPopulation) {
        return 11;
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
