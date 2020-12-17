package hipravin.strategy;

import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import model.EntityType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static hipravin.model.Position2d.of;

public class StrategyParams {
    public static int MOVE_TOWARDS_MAX_HIST = 10;

    public static int MAX_VAL = Integer.MAX_VALUE >> 1;

    public static int NEAREST_WORKERS_COMPUTE_PATH_LIMIT = 20;
    public static int NEAREST_MINERALS_COMPUTE_PATH_LIMIT = 10;

    public static final int HOUSE_WORKERS_NEARBY_MAX_PATH = 5;
    public static final int BARRACK_WORKERS_NEARBY_MAX_PATH = 7;
    public static final int TURRET_WORKERS_NEARBY_MAX_PATH = 10;
    public static final int BARRACK_WORKERS_NEARBY_MAX_PATH_CENTER = 15;
    public static final int RESP_SURROUNDED_DETECT_RANGE = 25;
    public static final int SEND_TO_CENTER_WS_RANGE = 20;

    public static final int MAX_COMBINATIONS_BF = 2000;
    public static final int FREE_SPACE_COMPUTE_RANGE = 10;

    public static Position2d DESIRED_BARRACK = of(30, 30);
    public static Position2d sendToDesiredBarrackPosition = DESIRED_BARRACK.shift(6, 6);

    public static final int MAP_CORNER_SIZE = 10;

    public int moveTowardsBarracksDistanceTreshold = 10;
    public int moveTowardsMineralsDistanceTreshold = 6;
    public boolean useAttackHoldOverCountTreshold = true;
    public int attackHoldEnemyRange = 10;
    public int attackHoldMyRange = 10;
    public double attackOverCountTreshhold = 1.2;
    public int rangersCloserHold = 2;
    public int retreatStopRange = 6;

    public int wayOutMinHouses = 1;
    public int wayOutFindPathLen = 22;
    public int wayOutDiffDetectTreshhold = 9;
    public double wayOutDiffDetectTreshholdMul = 0.2;
    public int wayOutBlockFindMaxPopulation = 60;
    public int wayOutBlockFindMinPopulation = 10;
    public int wayOutWorkerCountDiff = 8;
    public int wayOutWorkerMaxPullCount = 2;

    public int rangerCountToAddSomeWorkers = 10;


    public int workerScoutStartTickShiftAfterRangComplete = 10;
    public int workerScoutFrequency = 10; //pull 1 worker every n ticks
    public int workerScoutMaxRange = 11;
    public int workerScoutToBarrackCloseMinRange = 15;

    public int minCountOfRangersBeforeScouts = 15;
    public int maxNumberOfScouts = 5;
    public double scoutProb = 0.1;

    public int turretsForCleanupRange = 25;
    public int turretsForCleanupMaxCount = 10;
    public int turretsForCleanupEdgeShift = 2; //turret should atttack edge + X to be able to clear X lines of mineral guaranteed

    public int buildCommandMaxWaitTicks = 5;
    public int buildBarrackMaxWaitTicks = 10;
    public int buildTurretMaxWaitTicks = 13;
    public int autoRepairMaxWaitTicks = 5;

    public double mapCornerMiningRatio = 3 / 10.0; //spawn rokers only if workers / minerals < {value}

    public int maxHousesBeforeMandatorySpacing = 5;

    public int vragUVorotRange = 13;

    public Map<EntityType, Integer> armyValues = Map.of(
            EntityType.RANGED_UNIT, 1,
            EntityType.MELEE_UNIT, 2,
            EntityType.TURRET, 7
    );

    public double defendArmyOvercomeRatio = 1.5;

    public boolean useWorkerDefendingTurrets = false;//worker nearest to enemy cc or rang base requets turret
    public int turretsFrequency = 6;
    public int turretsMinRangers = 15;

    public int turretMinMinerals = 50;

    public int maxSpawnToMineralsRememberCount = 5;

    public int leftCornerSpacingDoesntMatterXPlusy = 7;

    public double bestMineralSpawnProb = 1.0;
    public double worstMineralSpawnProb = 0.2;
    public int switchToAutoMineRange = 4;

    public int dontSpawnWorkersVragUVorotPathLen = 25;

    //    public int populationOfWorkersToBuildBeforeRangers = 60;

    public int maxNumberOfRangers = 100;
    public int extraMoney = 100;
    public int outOfMoney = 100;
    public int outOfPopulation = 5;

    public int barrackAheadBuildResourceTick = 4;

    public int magnetMaxToSinglePointBarrack = 2;
    public int magnetMaxToSinglePointOthers = 1;

    public int numberOfRandomScoutChoices = 20;


    public boolean useWorkerPush = true;
    public int minWorkersToApplyPush = 10;
    public int maxWorkersToApplyPush = 60;

    public int respSize = 20;
    public int maxWorkerRespCountBeforeSendToFog = 12;

    public EntityType[] rangerDefaultAttackTargets = new EntityType[]{EntityType.RANGED_UNIT, EntityType.MELEE_UNIT, EntityType.BUILDER_UNIT, EntityType.TURRET, EntityType.HOUSE, EntityType.RANGED_BASE,
            EntityType.MELEE_BASE, EntityType.BUILDER_BASE, EntityType.WALL};

    public EntityType[] rangerWorkHuntAttackTargets = new EntityType[]{EntityType.BUILDER_UNIT};


    public Map<EntityType, Integer> magnetRepairRanges = Map.of(
            EntityType.HOUSE, 6,
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
            EntityType.TURRET, 20,
            EntityType.WALL, 1
    );

    public int populationOfWorkersToBuildBeforeRangers = 30;// is optimal rush?
    public int populationOfWorkersToBuildBeforeRangersIfSurrounded = 35;//35 is optimal rush?
    public int populationOfWorkersToBuildBeforeRangersIfDoubleSurrounded = 45;//35 is optimal rush?
    public int populationOfWorkersToBuildAfterRangers = 60;
    public int populationOfWorkersToIfExtraResources = 80;
    public boolean useWorkerFollow = true;

    public boolean sendToCenter = true;

    //    public Set<Integer> sendToCenterWorkerNumbers = Set.of(13, 17, 20, 23, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35);
//    DESIRED_BARRACK = of(33, 33);
    public Set<Integer> sendToCenterWorkerNumbers = Set.of(11, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29); // before 'fix'


//    public int populationOfWorkersToBuildBeforeRangers = 25;//35 is optimal rush?


    public Set<Integer> surroundedSendToCenterWorkerNumbers = Set.of(13, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40);
    public Set<Integer> doubleSurroundedSendToCenterWorkerNumbers = Set.of(13, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50);

    public int minDetectSurroundPopulation = 10;
    public int minCleanupTurretPopulation = 15;

    public int minHouseDistanceToCenter = 15;

    public int barrackHealthToHoldResources = 240;
    public int resourcesToHold = 50;

    public static boolean ifRandom(double prob) {
        return GameHistoryAndSharedState.random.nextDouble() < prob;
    }

    public Position2d randomScoutPointNearEnemy() {

        int x = Position2dUtil.MAP_SIZE - 5 - GameHistoryAndSharedState.random.nextInt(30);
        int y = Position2dUtil.MAP_SIZE - 5 - GameHistoryAndSharedState.random.nextInt(30);
        return of(x, y);
    }

    //    public List<Position2d> attackPoints = List.of(of(70, 70), of(30, 75), of(75, 30));
//    public List<Double> attackPointRates = List.of(0.9, 0.5);
    public List<Position2d> attackPoints = List.of(of(76, 76), of(40, 76), of(76, 40));
    public List<Double> attackPointRates = List.of(0.4, 0.5);

    public int cleanBaseRangeTreshhold = 15;
    public int useWorkerFollowMinRange = 13;//close to cc follow can stuck workers
    public boolean useOldRangerMicro = false;


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
                cc.shift(5, 0),
                cc.shift(5, 1),
                cc.shift(5, 2),
                cc.shift(5, 3),
                cc.shift(5, 4),
                cc.shift(5, 5),
                cc.shift(0, 5),
                cc.shift(1, 5),
                cc.shift(2, 5),
                cc.shift(3, 5),
                cc.shift(4, 5),
                cc.shift(7, 6),
                cc.shift(6, 7)
        );
    }

    public static <T> T selectRandomAccordingDistribution(List<? extends T> options, List<Double> prob) {
        for (int i = 0; i < options.size() - 1; i++) {
            if (ifRandom(prob.get(i))) {
                return options.get(i);
            }
        }

        return options.get(options.size() - 1);
    }

    public void activateOption2() {//super fast home
        GameHistoryAndSharedState.random.nextInt();

//        populationOfWorkersToBuildBeforeRangers = 35;
        populationOfWorkersToBuildBeforeRangers = 20;

        rangerCountToAddSomeWorkers = 10;

        DESIRED_BARRACK = of(25, 25);
        sendToDesiredBarrackPosition = DESIRED_BARRACK.shift(6, 6);

        sendToCenterWorkerNumbers = Set.of(13, 14, 15, 16, 17, 18, 19, 20);

//        attackPoints = List.of(of(70, 70), of(35, 70), of(70, 35));
//        attackPointRates = List.of(1.0, 0.0);

        useWorkerPush = true;

        useWorkerFollow = true;

        useWorkerDefendingTurrets = false;

//        useWorkerDefendingTurrets = true;
    }

    public void activateRound1() {
        StrategyParams.NEAREST_MINERALS_COMPUTE_PATH_LIMIT = 7;
        StrategyParams.NEAREST_WORKERS_COMPUTE_PATH_LIMIT = 15;

        populationOfWorkersToBuildAfterRangers = 60;
        populationOfWorkersToBuildBeforeRangers = 15;

        DESIRED_BARRACK = of(15, 15);
        sendToDesiredBarrackPosition = of(19, 19);

        sendToCenterWorkerNumbers = Set.of();
        minHouseDistanceToCenter = 3;



        attackPoints = List.of(of(76, 7), of(7, 76), of(76, 76));
        attackPointRates = List.of(0.5, 0.9);
        scoutProb = 0;
        useWorkerFollow = false;
        cleanBaseRangeTreshhold = 12;
        workerScoutFrequency = 1000; //no w scout
        useWorkerDefendingTurrets = true;

        turretsFrequency = 5;
    }

    public void activateRound2() {
        scoutProb = 0;

        populationOfWorkersToBuildBeforeRangers = 25;
        populationOfWorkersToBuildAfterRangers = 60;

        DESIRED_BARRACK = of(15, 15);
        sendToDesiredBarrackPosition = of(19, 19);

//        sendToCenterWorkerNumbers = Set.of(15, 16, 17, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35);
        sendToCenterWorkerNumbers = Set.of(17, 18, 19, 20, 21, 22, 23, 24, 25);
        minHouseDistanceToCenter = 3;
        useWorkerFollow = false;

        attackPoints = List.of(of(76, 7), of(7, 76), of(76, 76));
        attackPointRates = List.of(0.5, 0.9);

        cleanBaseRangeTreshhold = 12;
        workerScoutFrequency = 1000; //no w scout

        useWorkerDefendingTurrets = true;
        turretsFrequency = 5;
    }


    public int round1WorkersFirst = 15;
}
